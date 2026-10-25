package com.quos.agent.data.action

import com.quos.agent.data.model.TaskStatus
import com.quos.agent.data.model.TaskType
import com.quos.agent.data.parser.ActionProposal
import com.quos.agent.data.parser.ActionType
import com.quos.agent.data.repository.QuosRepository
import javax.inject.Inject

interface ActionExecutor {
    suspend fun execute(proposal: ActionProposal): Result<String>
}

class LocalActionExecutor @Inject constructor(
    private val repository: QuosRepository
) : ActionExecutor {
    override suspend fun execute(proposal: ActionProposal): Result<String> {
        if (!proposal.isValid) return Result.failure(IllegalArgumentException(proposal.description))

        return when (proposal.type) {
            ActionType.CREATE_NOTE -> {
                val content = proposal.arguments["content"] ?: "New note"
                repository.createNote(title = "Saved note", body = content)
                Result.success("Saved note locally.")
            }
            ActionType.SHOW_TASKS -> Result.success("Pending tasks are shown in the dashboard.")
            ActionType.PREPARE_EMAIL -> Result.success("Email draft prepared for review and approval.")
            ActionType.QUEUE_TASK -> {
                repository.createTask(
                    taskType = TaskType.SCHEDULE_LOCAL_TASK,
                    description = proposal.description,
                    networkRequired = true,
                    status = TaskStatus.PENDING
                )
                Result.success("Task queued and will resume when connectivity is available.")
            }
            ActionType.CANCEL_TASK -> Result.success("Task cancellation requested.")
            ActionType.OPEN_URL -> Result.success("Website action prepared.")
            ActionType.OPEN_APP -> Result.success("App launch approved for the user.")
            ActionType.LOCAL_TASK -> {
                repository.createTask(
                    taskType = TaskType.SCHEDULE_LOCAL_TASK,
                    description = proposal.description,
                    networkRequired = false,
                    status = TaskStatus.PENDING
                )
                Result.success("Local task scheduled.")
            }
            ActionType.UNKNOWN -> Result.failure(IllegalArgumentException(proposal.description))
        }
    }
}
