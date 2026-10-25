package com.quos.agent.data.planner

import com.quos.agent.data.parser.ActionProposal
import com.quos.agent.data.parser.ActionType
import com.quos.agent.data.parser.DefaultCommandParser

interface CommandPlanner {
    fun plan(rawCommand: String): ActionProposal
}

class LocalDeterministicPlanner(
    private val fallbackParser: DefaultCommandParser = DefaultCommandParser()
) : CommandPlanner {
    override fun plan(rawCommand: String): ActionProposal {
        val proposal = fallbackParser.parse(rawCommand)
        if (!proposal.isValid) {
            return proposal
        }

        val requiresConfirmation = proposal.requiresConfirmation ||
            proposal.type == ActionType.PREPARE_EMAIL ||
            proposal.type == ActionType.CANCEL_TASK ||
            proposal.type == ActionType.QUEUE_TASK

        return proposal.copy(requiresConfirmation = requiresConfirmation)
    }
}

class RemoteAiPlanner : CommandPlanner {
    override fun plan(rawCommand: String): ActionProposal {
        if (rawCommand.isBlank()) {
            return ActionProposal(
                type = ActionType.UNKNOWN,
                description = "The AI request is empty.",
                isValid = false
            )
        }

        // Safe AI integration: never executes arbitrary model output. The remote AI is only
        // allowed to produce a structured proposal, which must be validated by the app before dispatch.
        val localProposal = LocalDeterministicPlanner().plan(rawCommand)
        return if (localProposal.isValid) {
            localProposal.copy(requiresConfirmation = true)
        } else {
            localProposal
        }
    }
}

object PlannerValidator {
    fun validate(proposal: ActionProposal): Result<ActionProposal> {
        if (!proposal.isValid) {
            return Result.failure(IllegalArgumentException(proposal.description))
        }

        val approvedActions = setOf(
            ActionType.CREATE_NOTE,
            ActionType.SHOW_TASKS,
            ActionType.PREPARE_EMAIL,
            ActionType.QUEUE_TASK,
            ActionType.CANCEL_TASK,
            ActionType.OPEN_URL,
            ActionType.OPEN_APP,
            ActionType.LOCAL_TASK
        )

        if (proposal.type !in approvedActions) {
            return Result.failure(IllegalArgumentException("AI proposal contains a non-approved action."))
        }

        return Result.success(proposal)
    }
}
