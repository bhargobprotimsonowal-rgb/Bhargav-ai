package com.quos.agent.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quos.agent.data.action.LocalActionExecutor
import com.quos.agent.data.email.EmailDraft
import com.quos.agent.data.email.EmailApprovalController
import com.quos.agent.data.model.NoteEntity
import com.quos.agent.data.model.TaskEntity
import com.quos.agent.data.model.TaskStatus
import com.quos.agent.data.parser.ActionType
import com.quos.agent.data.parser.CommandParser
import com.quos.agent.data.repository.QuosRepository
import com.quos.agent.data.scheduler.TaskScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class QuosUiState(
    val command: String = "",
    val agentStatus: String = "Ready",
    val isOnline: Boolean = true,
    val tasks: List<TaskEntity> = emptyList(),
    val notes: List<NoteEntity> = emptyList(),
    val errorCount: Int = 0,
    val logs: List<String> = listOf("Agent ready for a user-approved action."),
    val emailDraft: EmailDraft = EmailDraft(
        to = listOf("team@quosagent.example"),
        subject = "Project sync",
        body = "Hi team, here is the latest update from Quos Agent."
    ),
    val emailApprovalRequested: Boolean = false,
    val emailApprovalId: String? = null,
    val requiresPermission: Boolean = false,
    val permissionMessage: String? = null
)

@HiltViewModel
class QuosViewModel @Inject constructor(
    private val repository: QuosRepository,
    private val parser: CommandParser,
    private val scheduler: TaskScheduler,
    private val executor: LocalActionExecutor,
    private val emailApprovalController: EmailApprovalController
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuosUiState())
    val uiState: StateFlow<QuosUiState> = _uiState

    init {
        viewModelScope.launch {
            combine(repository.observeTasks(), repository.observeNotes()) { tasks, notes ->
                _uiState.value = _uiState.value.copy(tasks = tasks, notes = notes)
            }.collect { }
        }
    }

    fun onCommandChanged(command: String) {
        _uiState.value = _uiState.value.copy(command = command)
    }

    fun executeCommand(command: String) {
        if (command.isBlank()) {
            appendLog("Request is empty.")
            return
        }

        val proposal = parser.parse(command)
        if (!proposal.isValid) {
            appendLog(proposal.description)
            _uiState.value = _uiState.value.copy(agentStatus = "Blocked")
            return
        }

        viewModelScope.launch {
            if (proposal.type == ActionType.PREPARE_EMAIL) {
                val approval = emailApprovalController.createSendApprovalRequest(_uiState.value.emailDraft)
                _uiState.value = _uiState.value.copy(
                    agentStatus = "Pending approval",
                    emailApprovalRequested = true,
                    emailApprovalId = approval.requestId,
                    logs = listOf("Email draft is ready for explicit approval.") + _uiState.value.logs.take(8)
                )
                return@launch
            }

            val result = executor.execute(proposal)
            appendLog(result.getOrElse { "Command failed: $it" })

            if (proposal.type == ActionType.QUEUE_TASK || proposal.type == ActionType.LOCAL_TASK) {
                val task = repository.createTask(
                    taskType = com.quos.agent.data.model.TaskType.SCHEDULE_LOCAL_TASK,
                    description = proposal.description,
                    networkRequired = proposal.networkRequired,
                    idempotencyKey = "cmd_${System.currentTimeMillis()}"
                )
                scheduler.schedule(task.taskId, proposal.networkRequired)
                appendLog("Task ${task.taskId} queued for execution.")
            }

            if (proposal.type == ActionType.CREATE_NOTE) {
                val content = proposal.arguments["content"] ?: "New note"
                repository.createNote(title = "Saved note", body = content)
            }

            if (proposal.type == ActionType.CANCEL_TASK) {
                val taskId = proposal.arguments["taskId"] ?: return@launch
                repository.cancelTask(taskId)
            }
        }
    }

    fun queueTask(command: String) {
        val proposal = parser.parse(command)
        viewModelScope.launch {
            val task = repository.createTask(
                taskType = com.quos.agent.data.model.TaskType.SCHEDULE_LOCAL_TASK,
                description = proposal.description,
                networkRequired = true,
                status = TaskStatus.PENDING,
                idempotencyKey = "queue_${System.currentTimeMillis()}"
            )
            scheduler.schedule(task.taskId, true)
            appendLog("Queued task ${task.taskId} on the local scheduler.")
        }
    }

    fun approveEmailSend() {
        val approvalId = _uiState.value.emailApprovalId ?: return
        val draft = _uiState.value.emailDraft
        val approval = emailApprovalController.createSendApprovalRequest(draft)
        val result = emailApprovalController.approveSend(approval.copy(approved = true))
        if (result.isSuccess) {
            _uiState.value = _uiState.value.copy(
                agentStatus = "Email approved",
                emailApprovalRequested = false,
                emailApprovalId = null,
                logs = listOf("Email send approved and queued for delivery.") + _uiState.value.logs.take(8)
            )
        } else {
            _uiState.value = _uiState.value.copy(
                agentStatus = "Blocked",
                emailApprovalRequested = false,
                emailApprovalId = null,
                logs = listOf(result.exceptionOrNull()?.message ?: "Email approval rejected.") + _uiState.value.logs.take(8)
            )
        }
    }

    fun rejectEmailSend() {
        _uiState.value = _uiState.value.copy(
            agentStatus = "Email not sent",
            emailApprovalRequested = false,
            emailApprovalId = null,
            logs = listOf("Email send was rejected by the user.") + _uiState.value.logs.take(8)
        )
    }

    fun setPermissionState(requiresPermission: Boolean, permissionMessage: String?) {
        _uiState.value = _uiState.value.copy(
            requiresPermission = requiresPermission,
            permissionMessage = permissionMessage
        )
    }

    private fun appendLog(message: String) {
        val current = _uiState.value.logs
        _uiState.value = _uiState.value.copy(
            logs = listOf(message) + current.take(8),
            agentStatus = if (message.contains("failed", ignoreCase = true)) "Needs attention" else "Ready"
        )
    }
}
