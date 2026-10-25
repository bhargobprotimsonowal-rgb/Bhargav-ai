package com.quos.agent.data.parser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CommandParserTest {
    private val parser = DefaultCommandParser()

    @Test
    fun `parse note command`() {
        val result = parser.parse("Save a note about my new app idea.")
        assertEquals(ActionType.CREATE_NOTE, result.type)
        assertTrue(result.isValid)
    }

    @Test
    fun `parse show tasks command`() {
        val result = parser.parse("Show pending tasks.")
        assertEquals(ActionType.SHOW_TASKS, result.type)
        assertTrue(result.isValid)
    }

    @Test
    fun `parse queue task command`() {
        val result = parser.parse("Queue this task until the internet returns.")
        assertEquals(ActionType.QUEUE_TASK, result.type)
        assertTrue(result.networkRequired)
    }

    @Test
    fun `parse cancellation task command`() {
        val result = parser.parse("Cancel task 123.")
        assertEquals(ActionType.CANCEL_TASK, result.type)
        assertEquals("123", result.arguments["taskId"])
    }

    @Test
    fun `invalid unknown command is rejected`() {
        val result = parser.parse("Open the moon and send a secret message!")
        assertEquals(ActionType.UNKNOWN, result.type)
        assertFalse(result.isValid)
    }
}
