package com.quos.agent.data.planner

import com.quos.agent.data.parser.ActionProposal
import com.quos.agent.data.parser.ActionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlannerValidationTest {
    @Test
    fun `local planner accepts approved safe actions`() {
        val planner = LocalDeterministicPlanner()
        val proposal = planner.plan("Save a note about my new app idea.")
        val result = PlannerValidator.validate(proposal)
        assertTrue(result.isSuccess)
        assertEquals(ActionType.CREATE_NOTE, proposal.type)
    }

    @Test
    fun `planner rejects invalid AI proposals`() {
        val planner = RemoteAiPlanner()
        val proposal = planner.plan("Open the moon and send a secret message!")
        val result = PlannerValidator.validate(proposal)
        assertTrue(result.isFailure)
        assertEquals(ActionType.UNKNOWN, proposal.type)
    }

    @Test
    fun `safe planner requires explicit confirmation for email actions`() {
        val planner = LocalDeterministicPlanner()
        val proposal = planner.plan("Prepare an email to my project team.")
        assertTrue(proposal.requiresConfirmation)
        assertEquals(ActionType.PREPARE_EMAIL, proposal.type)
    }
}
