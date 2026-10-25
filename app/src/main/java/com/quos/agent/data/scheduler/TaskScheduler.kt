package com.quos.agent.data.scheduler

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.quos.agent.data.repository.QuosRepository
import javax.inject.Inject
import java.util.concurrent.TimeUnit

class TaskScheduler @Inject constructor(
    private val context: Context,
    private val workManager: WorkManager,
    private val repository: QuosRepository
) {
    fun schedule(taskId: String, networkRequired: Boolean) {
        val constraints = if (networkRequired) {
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        } else {
            Constraints.Builder().build()
        }

        val request = OneTimeWorkRequestBuilder<TaskExecutionWorker>()
            .setInputData(workDataOf("task_id" to taskId))
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
            .build()

        workManager.enqueueUniqueWork(
            "task_$taskId",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}
