package com.quos.agent.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quos.agent.data.email.EmailDraft
import com.quos.agent.ui.theme.Navy700
import com.quos.agent.ui.theme.Navy800

@Composable
fun EmailApprovalPanel(
    draft: EmailDraft,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Navy800)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Explicit send approval")
            Text("To: ${draft.to.joinToString()}")
            Text("Subject: ${draft.subject}")
            Text("Preview: ${draft.body}")
            Button(onClick = onApprove) { Text("Approve send") }
            Button(onClick = onReject) { Text("Reject") }
        }
    }
}
