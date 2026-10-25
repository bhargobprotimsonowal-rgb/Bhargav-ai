package com.quos.agent.data.repository

import com.quos.agent.data.local.NoteDao
import com.quos.agent.data.local.TaskDao
import com.quos.agent.data.model.NoteEntity
import com.quos.agent.data.model.TaskEntity
import com.quos.agent.data.model.TaskStatus
import com.quos.agent.data.model.TaskType
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class QuosRepository @Inject constructor(
    private val taskDao: TaskDao,
    private val noteDao: NoteDao
) {
    fun observeTasks(): Flow<List<TaskEntity>> = taskDao.observeTasks()
    fun observeNotes(): Flow<List<NoteEntity>> = noteDao.observeNotes()
    fun observePendingTasks(): Flow<List<TaskEntity>> = taskDao.observeTasksByStatus(TaskStatus.PENDING.name)
    fun observeWaitingForNetworkTasks(): Flow<List<TaskEntity>> = taskDao.observeTasksByStatus(TaskStatus.WAITING_FOR_NETWORK.name)
    suspend fun getPendingTasks(): List<TaskEntity> = taskDao.getTasksByStatus(TaskStatus.PENDING.name) +
        taskDao.getTasksByStatus(TaskStatus.WAITING_FOR_NETWORK.name)

    suspend fun createTask(
        taskType: TaskType,
        description: String,
        payloadJson: String = "{}",
        networkRequired: Boolean = false,
        idempotencyKey: String? = null,
        status: TaskStatus = TaskStatus.PENDING
    ): TaskEntity {
        val key = idempotencyKey ?: UUID.randomUUID().toString()
        val existing = taskDao.findByIdempotencyKey(key)
        if (existing != null) return existing

        val task = TaskEntity(
            taskType = taskType.name,
            description = description,
            payloadJson = payloadJson,
            networkRequired = networkRequired,
            idempotencyKey = key,
            status = status.name,
            updatedAt = System.currentTimeMillis()
        )
        taskDao.insertTask(task)
        return task
    }

    suspend fun createNote(title: String, body: String): NoteEntity {
        val note = NoteEntity(title = title, body = body)
        noteDao.insertNote(note)
        return note
    }

    suspend fun findTaskByTaskId(taskId: String): TaskEntity? = taskDao.getTask(taskId)

    suspend fun findDuplicateByIdempotencyKey(key: String): TaskEntity? = taskDao.findByIdempotencyKey(key)

    suspend fun updateTaskStatus(
        taskId: String,
        status: TaskStatus,
        retryCount: Int? = null,
        lastError: String? = null
    ) {
        val task = taskDao.getTask(taskId) ?: return
        taskDao.updateTask(
            task.copy(
                status = status.name,
                retryCount = retryCount ?: task.retryCount,
                lastError = lastError ?: task.lastError,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun cancelTask(taskId: String) {
        val task = taskDao.getTask(taskId) ?: return
        taskDao.updateTask(task.copy(status = TaskStatus.CANCELLED.name, updatedAt = System.currentTimeMillis()))
    }

    suspend fun markTaskRetry(taskId: String, errorMessage: String) {
        val current = taskDao.getTask(taskId) ?: return
        val nextRetry = current.retryCount + 1
        taskDao.updateTask(
            current.copy(
                status = if (nextRetry >= 3) TaskStatus.FAILED.name else TaskStatus.WAITING_FOR_NETWORK.name,
                retryCount = nextRetry,
                lastError = errorMessage,
                updatedAt = System.currentTimeMillis()
            )
        )
    }
}
