package com.quos.agent.data.parser

import com.quos.agent.data.model.TaskType
import java.util.regex.Pattern

enum class ActionType {
    CREATE_NOTE,
    SHOW_TASKS,
    PREPARE_EMAIL,
    QUEUE_TASK,
    CANCEL_TASK,
    OPEN_URL,
    OPEN_APP,
    LOCAL_TASK,
    UNKNOWN
}

data class ActionProposal(
    val type: ActionType,
    val description: String,
    val arguments: Map<String, String> = emptyMap(),
    val requiresConfirmation: Boolean = false,
    val networkRequired: Boolean = false,
    val isValid: Boolean = true
)

interface CommandParser {
    fun parse(rawCommand: String): ActionProposal
}

class DefaultCommandParser : CommandParser {
    override fun parse(rawCommand: String): ActionProposal {
        val command = rawCommand.trim()
        if (command.isBlank()) {
            return ActionProposal(
                type = ActionType.UNKNOWN,
                description = "The request is empty.",
                isValid = false
            )
        }

        val lower = command.lowercase()

        if (lower.contains("save a note") || lower.startsWith("note ")) {
            val text = command.substringAfter("note", missingDelimiterValue = command).trim()
            return ActionProposal(
                type = ActionType.CREATE_NOTE,
                description = "Create a local note.",
                arguments = mapOf("content" to text.ifBlank { "New note" }),
                requiresConfirmation = false,
                networkRequired = false,
                isValid = true
            )
        }

        if (lower.contains("show pending tasks") || lower.contains("show tasks") || lower.contains("pending tasks")) {
            return ActionProposal(
                type = ActionType.SHOW_TASKS,
                description = "Display pending tasks.",
                isValid = true
            )
        }

        if (lower.contains("prepare an email") || lower.contains("email to") || lower.contains("draft an email")) {
            return ActionProposal(
                type = ActionType.PREPARE_EMAIL,
                description = "Prepare an email draft.",
                arguments = mapOf("subject" to "Project update"),
                requiresConfirmation = true,
                isValid = true
            )
        }

        if (lower.contains("queue this task until the internet returns") || lower.contains("queue this task") || lower.contains("wait for network")) {
            return ActionProposal(
                type = ActionType.QUEUE_TASK,
                description = "Queue a task that waits for connectivity.",
                requiresConfirmation = true,
                networkRequired = true,
                isValid = true
            )
        }

        val cancelMatcher = Pattern.compile("cancel task (\\d+)", Pattern.CASE_INSENSITIVE).matcher(command)
        if (cancelMatcher.find()) {
            return ActionProposal(
                type = ActionType.CANCEL_TASK,
                description = "Cancel a task.",
                arguments = mapOf("taskId" to cancelMatcher.group(1)),
                requiresConfirmation = true,
                isValid = true
            )
        }

        val urlMatcher = Pattern.compile("https?://[^\\s]+", Pattern.CASE_INSENSITIVE).matcher(command)
        if (urlMatcher.find()) {
            return ActionProposal(
                type = ActionType.OPEN_URL,
                description = "Open website.",
                arguments = mapOf("url" to urlMatcher.group(0)),
                requiresConfirmation = false,
                isValid = true
            )
        }

        if (lower.contains("schedule") || lower.contains("local task")) {
            return ActionProposal(
                type = ActionType.LOCAL_TASK,
                description = "Schedule a local task.",
                requiresConfirmation = true,
                isValid = true
            )
        }

        return ActionProposal(
            type = ActionType.UNKNOWN,
            description = "The command is not on the approved action list.",
            isValid = false
        )
    }
}
