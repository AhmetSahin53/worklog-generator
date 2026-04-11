package com.atmosware.worklog

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.ui.components.JBPasswordField
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent

class GroqSettingsConfigurable : Configurable {

    private var passwordField: JBPasswordField? = null

    override fun getDisplayName(): String = "JWL Work Log (Groq)"

    override fun createComponent(): JComponent {
        val field = JBPasswordField()
        field.emptyText.text = "Buraya_Key Yapıştırın"
        passwordField = field

        return FormBuilder.createFormBuilder()
            .addLabeledComponent("Groq API Key:", field, 1, false)
            .addComponentFillVertically(javax.swing.JPanel(), 0)
            .panel
    }

    override fun isModified(): Boolean {
        val service = ApplicationManager.getApplication().service<GroqApiKeyService>()
        val saved = service.getApiKey().orEmpty()
        val current = passwordField?.password?.concatToString().orEmpty().trim()
        // Güvenlik nedeniyle UI'ye kayıtlı anahtarı basmıyoruz. Kullanıcı alanı boş bırakırsa değişiklik yok sayalım.
        return current.isNotBlank() && current != saved
    }

    override fun apply() {
        val current = passwordField?.password?.concatToString().orEmpty().trim()
        if (current.isBlank()) return
        ApplicationManager.getApplication().service<GroqApiKeyService>().setApiKey(current)
        // Alanı hemen temizle (omuz üzerinden okunmasın)
        passwordField?.text = ""
    }

    override fun reset() {
        // Kayıtlı anahtarı UI'ye geri yazmıyoruz.
        passwordField?.text = ""
    }

    override fun disposeUIResources() {
        passwordField = null
    }
}

