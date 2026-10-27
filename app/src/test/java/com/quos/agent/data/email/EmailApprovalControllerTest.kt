package com.quos.agent.data.email

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EmailApprovalControllerTest {
    @Test
    fun `duplicate email send is rejected`() {
        val controller = EmailApprovalController()
        val firstDraft = EmailDraft(
            to = listOf("team@example.com"),
            subject = "Status update",
            body = "This message is already approved."
        )

        val firstApproval = controller.createSendApprovalRequest(firstDraft)
        val firstResult = controller.approveSend(firstApproval)
        assertTrue(firstResult.isSuccess)

        val secondApproval = controller.createSendApprovalRequest(firstDraft)
        val secondResult = controller.approveSend(secondApproval)
        assertTrue(secondResult.isFailure)
    }

    @Test
    fun `bulk recipient validation blocks over-limit messages`() {
        val controller = EmailApprovalController()
        val recipients = (1..51).map { "person$it@example.com" }
        val result = controller.validateBulkRecipients(recipients)
        assertTrue(result.isFailure)
    }

    @Test
    fun `valid recipient list passes validation`() {
        val controller = EmailApprovalController()
        val recipients = listOf("first@example.com", "second@example.com")
        val result = controller.validateBulkRecipients(recipients)
        assertTrue(result.isSuccess)
    }
}
