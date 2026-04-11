package com.atmosware.worklog

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.credentialStore.generateServiceName
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.components.Service

@Service(Service.Level.APP)
class GroqApiKeyService {

    private val credentialAttributes = CredentialAttributes(
        generateServiceName("worklog-generator", "GROQ_API_KEY")
    )

    fun getApiKey(): String? {
        val creds = PasswordSafe.instance.get(credentialAttributes)
        return creds?.getPasswordAsString()?.trim()?.takeIf { it.isNotBlank() }
    }

    fun setApiKey(apiKey: String?) {
        val value = apiKey?.trim().orEmpty()
        if (value.isBlank()) {
            PasswordSafe.instance.set(credentialAttributes, null)
            return
        }
        PasswordSafe.instance.set(credentialAttributes, Credentials("groq", value))
    }
}

