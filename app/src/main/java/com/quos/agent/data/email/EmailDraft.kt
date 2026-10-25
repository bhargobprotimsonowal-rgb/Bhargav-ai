package com.quos.agent.data.email

import android.net.Uri
import java.util.Locale

data class EmailDraft(
    val to: List<String> = emptyList(),
    val subject: String = "",
    val body: String = "",
    val draftOnly: Boolean = true,
    val approved: Boolean = false
) {
    fun preview(): String = "To: ${to.joinToString()}\nSubject: $subject\n\n$body"

    fun hasValidRecipients(): Boolean = to.all { it.contains("@") && it.contains(".") }
}

interface EmailProvider {
    suspend fun authorize(): Result<Unit>
    suspend fun createDraft(draft: EmailDraft): Result<String>
    suspend fun sendDraft(draftId: String): Result<Unit>
}

data class OAuthConfig(
    val clientId: String = "",
    val clientSecret: String = "",
    val redirectUri: String = "com.quos.agent:/oauth2callback",
    val scopes: List<String> = listOf("https://www.googleapis.com/auth/gmail.compose")
)

class GmailOAuthProvider : EmailProvider {
    override suspend fun authorize(): Result<Unit> = Result.success(Unit)
    override suspend fun createDraft(draft: EmailDraft): Result<String> = Result.success("draft-id-placeholder")
    override suspend fun sendDraft(draftId: String): Result<Unit> = Result.success(Unit)
}

object EmailDraftValidator {
    fun validate(draft: EmailDraft): Result<Unit> {
        if (draft.to.isEmpty()) return Result.failure(IllegalArgumentException("At least one recipient is required."))
        if (!draft.hasValidRecipients()) return Result.failure(IllegalArgumentException("Recipient list includes invalid addresses."))
        if (draft.subject.isBlank()) return Result.failure(IllegalArgumentException("Subject cannot be blank."))
        if (draft.body.isBlank()) return Result.failure(IllegalArgumentException("Message body cannot be blank."))
        return Result.success(Unit)
    }
}
