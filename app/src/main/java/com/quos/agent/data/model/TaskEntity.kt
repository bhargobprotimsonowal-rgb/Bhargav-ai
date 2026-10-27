package com.quos.agent.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskId: String = UUID.randomUUID().toString(),
    val taskType: String,
    val description: String,
    val payloadJson: String = "{}",
    val networkRequired: Boolean = false,
    val status: String = TaskStatus.PENDING.name,
    val retryCount: Int = 0,
    val lastError: String? = null,
    val idempotencyKey: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = createdAt
)
