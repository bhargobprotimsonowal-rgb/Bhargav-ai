package com.quos.agent.data.model

enum class TaskStatus {
    PENDING,
    RUNNING,
    WAITING_FOR_NETWORK,
    NEEDS_CONFIRMATION,
    SUCCEEDED,
    FAILED,
    CANCELLED
}
