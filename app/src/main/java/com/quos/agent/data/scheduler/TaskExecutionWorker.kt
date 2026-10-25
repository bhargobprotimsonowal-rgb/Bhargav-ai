package com.quos.agent.data.scheduler

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.quos.agent.data.model.TaskStatus
import com.quos.agent.data.model.TaskType
import com.quos.agent.data.repository.QuosRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.util.Locale

class TaskExecutionWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    private val repository: QuosRepository by lazy {
        val hiltEntryPoint = EntryPointAccessors.fromApplication(
            appContext,
            TaskExecutionWorkerEntryPoint::class.java
        )
        hiltEntryPoint.repository()
    }

    override suspend fun doWork(): Result {
        val taskId = inputData.getString("task_id") ?: return Result.failure()
        val task = repository.findTaskByTaskId(taskId) ?: return Result.failure()

        if (task.status == TaskStatus.CANCELLED.name) {
            return Result.success()
        }

        if (task.networkRequired && !NetworkAvailability.isConnected(applicationContext)) {
            repository.updateTaskStatus(taskId, TaskStatus.WAITING_FOR_NETWORK)
            return Result.retry()
        }

        repository.updateTaskStatus(taskId, TaskStatus.RUNNING)

        return try {
            val taskType = runCatching { TaskType.valueOf(task.taskType) }.getOrDefault(TaskType.UNKNOWN)
            when (taskType) {
                TaskType.NOTE -> repository.createNote(title = "Local task", body = task.description)
                TaskType.EMAIL_DRAFT -> {
                    val payload = task.payloadJson
                    if (payload.isBlank()) {
                        throw IllegalArgumentException("Email payload is missing.")
                    }
                }
                TaskType.OPEN_URL -> {
                    // This is a safe, user-approved action. The actual Android intent would be launched from a UI action trigger.
                }
                TaskType.OPEN_APP -> {
                    // Safe action placeholder for future app launch approval.
                }
                TaskType.SCHEDULE_LOCAL_TASK -> {
                    // Placeholder for local scheduling tasks.
                }
                TaskType.CANCEL_TASK -> repository.cancelTask(taskId)
                TaskType.UNKNOWN -> throw IllegalStateException("Unsupported task type.")
                else -> Unit
            }
            repository.updateTaskStatus(taskId, TaskStatus.SUCCEEDED)
            Result.success()
        } catch (exception: Exception) {
            val retryCount = task.retryCount + 1
            repository.updateTaskStatus(
                taskId,
                TaskStatus.FAILED,
                retryCount = retryCount,
                lastError = exception.message ?: "Unknown failure"
            )
            if (retryCount < 3) {
                Result.retry()
            } else {
                Result.failure(workDataOf("error" to (exception.message ?: "Unknown failure")))
            }
        }
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface TaskExecutionWorkerEntryPoint {
        fun repository(): QuosRepository
    }
}

object NetworkAvailability {
    fun isConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
            ?: return false
        return runCatching {
            val networkInfo = connectivityManager.javaClass.getMethod("getActiveNetworkInfo").invoke(connectivityManager)
            networkInfo != null && networkInfo.javaClass.getMethod("isConnected").invoke(networkInfo) as Boolean
        }.getOrDefault(false)
    }
}
