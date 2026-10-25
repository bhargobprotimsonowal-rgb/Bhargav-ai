package com.quos.agent.data.email

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EmailDraftTest {
    @Test
    fun `draft validation accepts valid recipients`() {
        val draft = EmailDraft(
            to = listOf("hello@example.com"),
            subject = "Status update",
            body = "Team update body"
        )

        val result = EmailDraftValidator.validate(draft)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `draft validation rejects invalid recipient`() {
        val draft = EmailDraft(
            to = listOf("bad-address"),
            subject = "Status update",
            body = "Team update body"
        )

        val result = EmailDraftValidator.validate(draft)
        assertTrue(result.isFailure)
    }

    @Test
    fun `preview includes all core message content`() {
        val draft = EmailDraft(
            to = listOf("first@example.com", "second@example.com"),
            subject = "Daily summary",
            body = "This is a preview body."
        )

        val preview = draft.preview()
        assertTrue(preview.contains("Subject: Daily summary"))
        assertTrue(preview.contains("first@example.com"))
        assertFalse(preview.isBlank())
    }
}
