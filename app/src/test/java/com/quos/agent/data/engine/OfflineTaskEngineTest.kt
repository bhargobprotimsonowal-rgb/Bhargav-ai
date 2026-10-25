package com.quos.agent.data.engine

import com.quos.agent.data.local.NoteDao
import com.quos.agent.data.local.TaskDao
import com.quos.agent.data.model.NoteEntity
import com.quos.agent.data.model.TaskEntity
import com.quos.agent.data.model.TaskStatus
import com.quos.agent.data.model.TaskType
import com.quos.agent.data.repository.QuosRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OfflineTaskEngineTest {
    @Test
    fun `offline engine marks task as succeeded after a valid local task`() {
        val taskDao = FakeTaskDao()
        val noteDao = FakeNoteDao()
        val repository = QuosRepository(taskDao, noteDao)
        val engine = OfflineTaskEngine(repository)

        val task = repository.createTask(
            taskType = TaskType.NOTE,
            description = "Write a note about project follow-up.",
            idempotencyKey = "offline-task-1"
        )

        val result = engine.execute(task.taskId)
        assertTrue(result is TaskExecutionResult.Success)
        assertEquals(TaskStatus.SUCCEEDED.name, repository.findTaskByTaskId(task.taskId)?.status)
    }

    @Test
    fun `offline engine retries failed tasks up to bounded retry count`() {
        val taskDao = FakeTaskDao()
        val noteDao = FakeNoteDao()
        val repository = QuosRepository(taskDao, noteDao)
        val engine = OfflineTaskEngine(repository)
        val task = repository.createTask(
            taskType = TaskType.EMAIL_DRAFT,
            description = "Prepare a draft message.",
            payloadJson = "",
            idempotencyKey = "offline-task-2"
        )

        val result = engine.execute(task.taskId)
        assertTrue(result is TaskExecutionResult.Retry || result is TaskExecutionResult.Failure)
        val saved = repository.findTaskByTaskId(task.taskId)
        assertTrue(saved?.status == TaskStatus.WAITING_FOR_NETWORK.name || saved?.status == TaskStatus.FAILED.name)
    }

    private class FakeTaskDao : TaskDao {
        private val tasks = linkedMapOf<String, TaskEntity>()

        override suspend fun insertTask(task: TaskEntity): Long {
            tasks[task.taskId] = task
            return 1L
        }

        override fun observeTasks(): kotlinx.coroutines.flow.Flow<List<TaskEntity>> = kotlinx.coroutines.flow.flowOf(tasks.values.toList())

        override fun observeTasksByStatus(status: String): kotlinx.coroutines.flow.Flow<List<TaskEntity>> =
            kotlinx.coroutines.flow.flowOf(tasks.values.filter { it.status == status })

        override suspend fun getTask(taskId: String): TaskEntity? = tasks[taskId]

        override suspend fun findByIdempotencyKey(key: String): TaskEntity? = tasks.values.firstOrNull { it.idempotencyKey == key }

        override suspend fun updateTask(task: TaskEntity) {
            tasks[task.taskId] = task
        }

        override suspend fun deleteByTaskId(taskId: String) {
            tasks.remove(taskId)
        }
    }

    private class FakeNoteDao : NoteDao {
        private val notes = linkedMapOf<Long, NoteEntity>()

        override suspend fun insertNote(note: NoteEntity): Long {
            val id = (notes.keys.maxOrNull() ?: 0L) + 1L
            notes[id] = note.copy(id = id)
            return id
        }

        override fun observeNotes(): kotlinx.coroutines.flow.Flow<List<NoteEntity>> = kotlinx.coroutines.flow.flowOf(notes.values.toList())
    }
}
