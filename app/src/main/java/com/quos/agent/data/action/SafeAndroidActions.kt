package com.quos.agent.data.action

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.pm.ShortcutInfoCompat
import java.util.UUID

interface SafeAndroidActionExecutor {
    fun openWebsite(context: Context, url: String): Result<Unit>
    fun openApp(context: Context, packageName: String): Result<Unit>
    fun createLocalNote(context: Context, title: String, body: String): Result<Unit>
    fun displayNotification(context: Context, title: String, message: String): Result<Unit>
    fun shareText(context: Context, text: String): Result<Unit>
    fun schedulePermittedTask(taskDescription: String): Result<Unit>
}

class AndroidSafeActionExecutor : SafeAndroidActionExecutor {

    override fun openWebsite(context: Context, url: String): Result<Unit> {
        if (url.isBlank()) return Result.failure(IllegalArgumentException("URL cannot be empty."))
        return runCatching {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    override fun openApp(context: Context, packageName: String): Result<Unit> {
        if (packageName.isBlank()) return Result.failure(IllegalArgumentException("Package name cannot be empty."))
        return runCatching {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
                ?: throw IllegalArgumentException("No launch intent available for package: $packageName")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    override fun createLocalNote(context: Context, title: String, body: String): Result<Unit> {
        return runCatching {
            val noteIntent = Intent(Intent.ACTION_SEND)
            noteIntent.type = "text/plain"
            noteIntent.putExtra(Intent.EXTRA_SUBJECT, title)
            noteIntent.putExtra(Intent.EXTRA_TEXT, body)
            noteIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(Intent.createChooser(noteIntent, "Save note"))
        }
    }

    override fun displayNotification(context: Context, title: String, message: String): Result<Unit> {
        val isAllowed = ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (!isAllowed) return Result.failure(SecurityException("POST_NOTIFICATIONS permission is required."))

        return runCatching {
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            val channelId = "quos_agent_notifications"
            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.stat_notify_chat)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            notificationManager?.notify(UUID.randomUUID().hashCode(), builder.build())
        }
    }

    override fun shareText(context: Context, text: String): Result<Unit> {
        if (text.isBlank()) return Result.failure(IllegalArgumentException("Share text cannot be empty."))
        return runCatching {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, text)
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(Intent.createChooser(shareIntent, "Share with"))
        }
    }

    override fun schedulePermittedTask(taskDescription: String): Result<Unit> {
        if (taskDescription.isBlank()) return Result.failure(IllegalArgumentException("Task description cannot be blank."))
        return Result.success(Unit)
    }
}

object AppPermissionStatus {
    fun canPostNotifications(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    fun openSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.fromParts("package", context.packageName, null)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
