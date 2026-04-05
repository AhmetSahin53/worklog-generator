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

class GenerateWorkLogAction : AnAction() {

    // Özel JWL Logomuzu Yüklüyoruz (icons/JWL_Log.svg)
    private val jwlIcon = IconLoader.getIcon("/icons/JWL_Log.svg", javaClass)

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
        diffBuilder.append("Sen kıdemli bir yazılım geliştiricisin. Aşağıdaki kod değişikliklerini inceleyip Jira için kısa, öz ve profesyonel bir Türkçe Work Log yazar mısın?\n")
        diffBuilder.append("KURALLAR:\n")
        diffBuilder.append("1. Dosya isimlerini tek tek sayarak aşırı detaya GİRME.\n")
        diffBuilder.append("2. Sadece eklenen ana özellikleri ve çözülen sorunları maksimum 3-4 kısa madde halinde özetle.\n")
        diffBuilder.append("3. Gereksiz teknik detaylardan kaçın, yöneticilerin okuyacağı kıvamda net ve anlaşılır olsun.\n\n")

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

        // 2. Groq'a gönderilecek veriyi simüle et / test et
        val aiResponse = callAiApi(finalPrompt)

        // 3. Kullanıcıya dialog penceresinde göster ve kopyalama ikonunu ekle
        WorkLogDialog(aiResponse).show()
    }

    // API'ye HTTP isteği atacağımız gerçek metod
    private fun callAiApi(prompt: String): String {
        // BURAYA GROQ API ANAHTARINI YAZ (gsk_ ile başlayan)
        val apiKey = "SENIN_GROQ_API_ANAHTARIN_BURAYA_GELECEK"
        val url = "https://api.groq.com/openai/v1/chat/completions"

        // Groq (OpenAI formatı) için JSON gövdesini hazırlıyoruz
        val gson = com.google.gson.Gson()
        val requestBodyMap = mapOf(
            "model" to "llama-3.3-70b-versatile",
            "messages" to listOf(
                mapOf("role" to "user", "content" to prompt)
            )
        )
        val jsonBody = gson.toJson(requestBodyMap)

        // İsteği gönderiyoruz (Authorization header'ı eklendi)
        val client = java.net.http.HttpClient.newHttpClient()
        val request = java.net.http.HttpRequest.newBuilder()
            .uri(java.net.URI.create(url))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer $apiKey")
            .POST(java.net.http.HttpRequest.BodyPublishers.ofString(jsonBody, Charsets.UTF_8))
            .build()

        return try {
            val response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString())

            // Gelen JSON yanıtını parse ediyoruz
            val jsonResponse = gson.fromJson(response.body(), com.google.gson.JsonObject::class.java)

            // Yanıtın içinden sadece metin (content) kısmını çekiyoruz
            if (jsonResponse.has("choices")) {
                val choices = jsonResponse.getAsJsonArray("choices")
                choices.get(0).asJsonObject.getAsJsonObject("message").get("content").asString
            } else {
                "API'den beklenmeyen yanıt: ${response.body()}"
            }
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