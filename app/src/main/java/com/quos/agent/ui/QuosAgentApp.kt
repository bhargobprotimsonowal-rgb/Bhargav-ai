package com.quos.agent.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.quos.agent.data.model.NoteEntity
import com.quos.agent.data.model.TaskEntity
import com.quos.agent.data.model.TaskStatus
import com.quos.agent.data.parser.ActionType
import com.quos.agent.ui.theme.Cyan400
import com.quos.agent.ui.theme.Navy700
import com.quos.agent.ui.theme.Navy800
import com.quos.agent.ui.theme.Navy900
import com.quos.agent.viewModel.QuosViewModel

@Composable
fun QuosAgentApp(
    viewModel: QuosViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(containerColor = Navy900) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            HeaderCard(
                status = state.agentStatus,
                online = state.isOnline,
                pendingCount = state.tasks.count { it.status == TaskStatus.PENDING.name || it.status == TaskStatus.WAITING_FOR_NETWORK.name }
            )

            CommandInputCard(
                value = state.command,
                onValueChange = viewModel::onCommandChanged,
                onRun = { viewModel.executeCommand(state.command) },
                onQueue = { viewModel.queueTask(state.command) }
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DashboardStatCard(title = "Created", value = state.tasks.size.toString(), icon = Icons.Default.TaskAlt)
                DashboardStatCard(title = "Notes", value = state.notes.size.toString(), icon = Icons.Default.CheckCircle)
                DashboardStatCard(title = "Alerts", value = state.errorCount.toString(), icon = Icons.Default.Notifications)
            }

            TaskListCard(tasks = state.tasks)
            NotesListCard(notes = state.notes)
            EmailWorkspaceCard(draft = state.emailDraft)
            ActivityLogCard(logs = state.logs)
        }
    }
}

@Composable
private fun HeaderCard(status: String, online: Boolean, pendingCount: Int) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Navy800),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Quos Agent", style = MaterialTheme.typography.headlineMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (online) Icons.Default.Wifi else Icons.Default.WifiOff,
                        contentDescription = null,
                        tint = if (online) Cyan400 else androidx.compose.ui.graphics.Color(0xFFEA6A77)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (online) "Online" else "Offline")
                }
            }

            Text("Agent status: $status", color = MaterialTheme.colorScheme.primary)
            Text("Pending work: $pendingCount tasks waiting for execution")
        }
    }
}

@Composable
private fun CommandInputCard(
    value: String,
    onValueChange: (String) -> Unit,
    onRun: () -> Unit,
    onQueue: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Navy800),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Command input", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text("Example: Save a note about my new app idea.") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onRun) { Text("Run command") }
                Button(onClick = onQueue) { Text("Queue task") }
            }
        }
    }
}

@Composable
private fun DashboardStatCard(title: String, value: String, icon: ImageVector) {
    Card(
        modifier = Modifier.width(110.dp),
        colors = CardDefaults.cardColors(containerColor = Navy800)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = Cyan400)
            Text(value, style = MaterialTheme.typography.headlineSmall)
            Text(title, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun TaskListCard(tasks: List<TaskEntity>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Navy800),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Task queue", style = MaterialTheme.typography.titleLarge)
            if (tasks.isEmpty()) {
                Text("No delegated tasks yet. Ask Quos to create one.")
            } else {
                tasks.take(5).forEach { task ->
                    TaskRow(task = task)
                }
            }
        }
    }
}

@Composable
private fun TaskRow(task: TaskEntity) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Navy700),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(task.description)
            Text(
                "${task.taskType} • ${task.status}",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun NotesListCard(notes: List<NoteEntity>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Navy800),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Notes and local tasks", style = MaterialTheme.typography.titleLarge)
            if (notes.isEmpty()) {
                Text("No local notes saved yet.")
            } else {
                notes.take(4).forEach { note ->
                    Card(colors = CardDefaults.cardColors(containerColor = Navy700)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(note.title, style = MaterialTheme.typography.titleMedium)
                            Text(note.body, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmailWorkspaceCard(draft: com.quos.agent.data.email.EmailDraft) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Navy800),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Email automation workspace", style = MaterialTheme.typography.titleLarge)
            Text("To: ${draft.to.joinToString()}")
            Text("Subject: ${draft.subject}")
            Text("Preview: ${draft.body}")
            Text("Draft-only mode active until you explicitly approve sending.")
        }
    }
}

@Composable
private fun ActivityLogCard(logs: List<String>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Navy800),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Activity log", style = MaterialTheme.typography.titleLarge)
            logs.forEach { message ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = Cyan400)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(message)
                }
            }
        }
    }
}
