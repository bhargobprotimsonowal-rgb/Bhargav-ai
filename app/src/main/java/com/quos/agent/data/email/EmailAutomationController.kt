package com.quos.agent.data.email

import java.util.UUID

private const val MAX_BULK_RECIPIENTS = 50

class EmailApprovalController {
    private val sentIds = mutableSetOf<String>()

    fun createSendApprovalRequest(draft: EmailDraft): EmailSendApproval {
        val validation = EmailDraftValidator.validate(draft)
        require(validation.isSuccess) { validation.exceptionOrNull()?.message ?: "Invalid draft." }
        return EmailSendApproval(
            requestId = UUID.randomUUID().toString(),
            draft = draft,
            approved = false,
            duplicateDetected = sentIds.contains(draft.subject + draft.body + draft.to.joinToString())
        )
    }

    fun approveSend(request: EmailSendApproval): Result<Unit> {
        if (request.duplicateDetected) {
            return Result.failure(IllegalStateException("Duplicate email send blocked."))
        }
        val validation = EmailDraftValidator.validate(request.draft)
        if (validation.isFailure) return Result.failure(validation.exceptionOrNull() ?: IllegalArgumentException("Draft validation failed."))
        val identity = request.draft.subject + request.draft.body + request.draft.to.joinToString()
        sentIds.add(identity)
        return Result.success(Unit)
    }

    fun validateBulkRecipients(recipients: List<String>): Result<Unit> {
        if (recipients.isEmpty()) return Result.failure(IllegalArgumentException("At least one recipient is required."))
        if (recipients.size > MAX_BULK_RECIPIENTS) return Result.failure(IllegalArgumentException("Bulk send exceeds the approved local limit."))
        val invalid = recipients.firstOrNull { !it.contains("@") || !it.contains(".") }
        if (invalid != null) return Result.failure(IllegalArgumentException("Recipient '$invalid' is not valid."))
        return Result.success(Unit)
    }
}

data class EmailSendApproval(
    val requestId: String,
    val draft: EmailDraft,
    val approved: Boolean,
    val duplicateDetected: Boolean
)
