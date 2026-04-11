package com.atmosware.worklog

import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBScrollPane
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionEvent
import javax.swing.Action
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextArea
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vcs.changes.ChangeListManager
import com.intellij.openapi.util.IconLoader
import java.awt.datatransfer.StringSelection
import com.intellij.openapi.diagnostic.Logger
import java.net.http.HttpTimeoutException
import java.time.Duration
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service

class GenerateWorkLogAction : AnAction() {

    // Özel JWL Logomuzu Yüklüyoruz (icons/JWL_Log.svg)
    private val jwlIcon = IconLoader.getIcon("/icons/JWL_Log.svg", javaClass)
    private val log = Logger.getInstance(GenerateWorkLogAction::class.java)

    override fun update(e: AnActionEvent) {
        // Butonun metnini ve simgesini dinamik olarak ayarlıyoruz
        e.presentation.text = "JWL Log Üret"
        e.presentation.icon = jwlIcon
        e.presentation.description = "Değişiklikleri JWL Log olarak analiz et"
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project: Project = e.project ?: return
        val changeListManager = ChangeListManager.getInstance(project)
        val changes = changeListManager.defaultChangeList.changes

        if (changes.isEmpty()) {
            Messages.showWarningDialog("Commit edilecek herhangi bir değişiklik bulunamadı.", "Değişiklik Yok")
            return
        }

        // 1. Kod farklarını (Diff) toplayacağımız metin oluşturucu
        val diffBuilder = StringBuilder()
        // Prompt'u daha sabit/deterministik yap: çıktı biçimini netleştir.
        diffBuilder.append(
            """
            Aşağıdaki kod değişikliklerini inceleyip Jira için Work Log üret.

            ÇIKTI BİÇİMİ (AYNEN UY):
            - En fazla 4 madde.
            - Her madde kısa bir cümle olsun.
            - Başlık, selamlama, kapanış, emoji, kod bloğu YOK.

            KURALLAR:
            1) Dosya isimlerini tek tek sayma.
            2) Sadece gerçekten yapılan değişiklikleri yaz; olmayan özellik/test/performans iyileştirmesi uydurma.
            3) %100 Türkçe yaz. İngilizce/başka dilde tek bir kelime bile kullanma.
            """.trimIndent() + "\n\n"
        )

        for (change in changes) {
            val fileName = change.virtualFile?.name ?: "Bilinmeyen Dosya"

            // IntelliJ API'si ile eski ve yeni dosya içeriklerini alıyoruz
            val beforeContent = change.beforeRevision?.content ?: ""
            val afterContent = change.afterRevision?.content ?: ""

            diffBuilder.append("Dosya: $fileName\n")
            diffBuilder.append("--- ESKİ KOD ---\n$beforeContent\n")
            diffBuilder.append("--- YENİ KOD ---\n$afterContent\n")
            diffBuilder.append("--------------------------\n\n")
        }

        val finalPrompt = diffBuilder.toString()

        val aiResponse = callAiApi(finalPrompt)

        WorkLogDialog(aiResponse).show()
    }

    private fun callAiApi(prompt: String): String {
        val envKey = System.getenv("GROQ_API_KEY")?.trim().orEmpty()
        val storedKey = ApplicationManager.getApplication().service<GroqApiKeyService>().getApiKey().orEmpty()
        val apiKey = (if (envKey.isNotBlank()) envKey else storedKey).trim()

        if (apiKey.isBlank()) {
            return "Groq API anahtarı bulunamadı. Çözüm: (1) Windows ortam değişkeni GROQ_API_KEY tanımlayın veya (2) IDE Settings > Tools > JWL Work Log (Groq) ekranından anahtarı kaydedin."
        }

        val url = "https://api.groq.com/openai/v1/chat/completions"
        val gson = com.google.gson.Gson()

        fun buildBody(userPrompt: String, seed: Int): String {
            val requestBodyMap = linkedMapOf(
                "model" to "llama-3.3-70b-versatile",
                // Deterministikliğe yaklaşmak için sampling'i kıs.
                // Daha katı deterministiklik için temperature=0.0 ve top_p=0.0
                "temperature" to 0.0,
                "top_p" to 0.0,
                // max_tokens ekleyerek çıktı uzunluğunu sınırla
                "max_tokens" to 200,
                // Groq/OpenAI uyumlu uçlarda seed desteklenebiliyor; desteklenmezse sorun olmaz.
                "seed" to seed,
                "messages" to listOf(
                    mapOf(
                        "role" to "system",
                        "content" to "Sen bir Türkçe teknik yazım asistanısın. " +
                                "Sadece Türkçe çıktı üret. Sadece 1-4 madde yaz; her madde '-' ile başlamalı. " +
                                "Başlık, selamlama, kapanış, emoji veya kod bloğu yazma. Her madde kısa ve açık bir cümle olsun. Başka hiçbir şey yazma."
                    ),
                    mapOf("role" to "user", "content" to userPrompt)
                )
            )
            return gson.toJson(requestBodyMap)
        }

        val client = java.net.http.HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .build()

        fun sendOnce(body: String): Pair<Int, String> {
            val request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(url))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer $apiKey")
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(body, Charsets.UTF_8))
                .build()

            val response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString())
            return response.statusCode() to response.body()
        }

        fun extractContent(body: String): String? {
            return try {
                val jsonResponse = gson.fromJson(body, com.google.gson.JsonObject::class.java)
                if (!jsonResponse.has("choices")) return null
                val choices = jsonResponse.getAsJsonArray("choices")
                if (choices.size() == 0) return null
                choices.get(0).asJsonObject
                    .getAsJsonObject("message")
                    .get("content")
                    .asString
                    .trim()
            } catch (t: Throwable) {
                log.warn("Groq yanıtı parse edilemedi", t)
                null
            }
        }

        fun looksNonTurkish(text: String): Boolean {
            // Heuristik: bariz İngilizce bağlaç/kelimeler veya çok sayıda ASCII kelime.
            val lowered = text.lowercase()
            val forbiddenMarkers = listOf(
                "the ", "and ", "fixed", "improved", "added", "changed", "refactor", "performance", "test",
                "commit", "worklog", "feature"
            )
            if (forbiddenMarkers.any { lowered.contains(it) }) return true

            // Türkçe karakter yoksa ve metin uzunsa şüpheli.
            val hasTurkishChars = text.any { it in "çğıöşüÇĞİÖŞÜ" }
            if (!hasTurkishChars && text.length > 80) return true

            // Çok kısa metinler için esneklik sağla
            if (text.length < 10) return false

            // Latin harf dışı (Çince vb.) yakala.
            val hasCjk = text.any { Character.UnicodeScript.of(it.code) == Character.UnicodeScript.HAN }
            return hasCjk
        }

        fun enforceBulletFormat(text: String): Boolean {
            val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }
            // En az 1, en fazla 4 madde; her satır - ile başlamalı.
            if (lines.isEmpty() || lines.size > 4) return false
            // Her satır '-' ile başlamalı ve Türkçe karakter içermeli
            val turkishLetters = "çğıöşüÇĞİÖŞÜ"
            return lines.all {
                it.startsWith("-") && it.length <= 250 && it.drop(1).any { ch -> ch in turkishLetters }
            }
        }

        return try {
            // 1) İlk deneme
            val (code1, body1) = sendOnce(buildBody(prompt, seed = 42))
            val content1 = extractContent(body1)
            if (code1 in 200..299 && content1 != null && !looksNonTurkish(content1) && enforceBulletFormat(content1)) {
                content1
            } else {
                // 2) İkinci deneme: daha sert talimatla yeniden iste
                val retryPrompt =
                    prompt +
                        "\n\nUYARI: Önceki çıktı kurallara uymadı. Şimdi SADECE Türkçe ve SADECE 1-4 madde olacak şekilde '-' ile başlayan maddeler yaz. Başka hiç bir şey yazma."
                val (code2, body2) = sendOnce(buildBody(retryPrompt, seed = 42))
                val content2 = extractContent(body2)
                if (code2 in 200..299 && content2 != null) {
                    // Son çare: yine de bir şey döndür.
                    content2
                } else {
                    "API Hatası: HTTP $code2 - ${body2.take(500)}"
                }
            }
        } catch (e: HttpTimeoutException) {
            "Bağlantı Zaman Aşımı: ${e.message}"
        } catch (e: Exception) {
            "Bağlantı Hatası: ${e.message}"
        }
    }
}

// Özel Tasarım Pop-up Penceremiz
class WorkLogDialog(private val workLogText: String) : DialogWrapper(true) {
    init {
        init()
        title = "JWL Work Log Sonucu 🚀"
    }

    // Pencerenin ortasındaki metin alanı
    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout())
        val textArea = JTextArea(workLogText)
        textArea.isEditable = false // Kullanıcı elle değiştiremesin
        textArea.lineWrap = true
        textArea.wrapStyleWord = true

        val scrollPane = JBScrollPane(textArea)
        scrollPane.preferredSize = Dimension(550, 300) // Pencere boyutu
        panel.add(scrollPane, BorderLayout.CENTER)

        return panel
    }

    // Tam işaretlediğin yere (Sol Alt) buton ekleyen metod
    override fun createLeftSideActions(): Array<Action> {
        val copyAction = object : DialogWrapperAction("Panoya Kopyala") {
            override fun doAction(e: ActionEvent?) {
                // Tıklanınca metni kopyala
                CopyPasteManager.getInstance().setContents(StringSelection(workLogText))

                // Kopyaladıktan sonra pencereyi otomatik kapatsın
                close(OK_EXIT_CODE)
            }
        }
        return arrayOf(copyAction)
    }
}