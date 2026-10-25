package com.quos.agent.data.engine

import com.quos.agent.data.model.TaskEntity
import com.quos.agent.data.model.TaskStatus
import com.quos.agent.data.model.TaskType
import com.quos.agent.data.repository.QuosRepository
import javax.inject.Inject

sealed class TaskExecutionResult {
    data class Success(val taskId: String) : TaskExecutionResult()
    data class Retry(val taskId: String, val nextStatus: TaskStatus, val reason: String) : TaskExecutionResult()
    data class Failure(val taskId: String, val reason: String) : TaskExecutionResult()
}

class OfflineTaskEngine @Inject constructor(
    private val repository: QuosRepository
) {
    suspend fun execute(taskId: String): TaskExecutionResult {
        val task = repository.findTaskByTaskId(taskId) ?: return TaskExecutionResult.Failure(taskId, "Task not found.")

        if (task.status == TaskStatus.CANCELLED.name) {
            return TaskExecutionResult.Success(taskId)
        }

        return try {
            repository.updateTaskStatus(taskId, TaskStatus.RUNNING)
            when (TaskType.valueOf(task.taskType)) {
                TaskType.NOTE -> repository.createNote(title = "Offline task", body = task.description)
                TaskType.EMAIL_DRAFT -> {
                    val payload = task.payloadJson
                    if (payload.isBlank()) throw IllegalArgumentException("Email payload is missing.")
                }
                TaskType.SCHEDULE_LOCAL_TASK -> Unit
                TaskType.OPEN_URL, TaskType.OPEN_APP, TaskType.CANCEL_TASK, TaskType.SHOW_TASKS, TaskType.UNKNOWN -> Unit
            }

            repository.updateTaskStatus(taskId, TaskStatus.SUCCEEDED)
            TaskExecutionResult.Success(taskId)
        } catch (exception: Exception) {
            val nextRetry = task.retryCount + 1
            val nextStatus = if (nextRetry < 3) TaskStatus.WAITING_FOR_NETWORK else TaskStatus.FAILED
            repository.updateTaskStatus(
                taskId,
                nextStatus,
                retryCount = nextRetry,
                lastError = exception.message ?: "Unknown task error"
            )

            when (nextStatus) {
                TaskStatus.WAITING_FOR_NETWORK -> TaskExecutionResult.Retry(taskId, nextStatus, exception.message ?: "Unknown task error")
                TaskStatus.FAILED -> TaskExecutionResult.Failure(taskId, exception.message ?: "Unknown task error")
                else -> TaskExecutionResult.Failure(taskId, exception.message ?: "Unknown task error")
            }
        }
    }
}
