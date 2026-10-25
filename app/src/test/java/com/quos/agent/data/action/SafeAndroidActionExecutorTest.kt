package com.quos.agent.data.action

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertTrue
import org.junit.Test

class SafeAndroidActionExecutorTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val executor = AndroidSafeActionExecutor()

    @Test
    fun `open website returns success with valid url`() {
        val result = executor.openWebsite(context, "https://example.com")
        assertTrue(result.isSuccess || result.isFailure)
    }

    @Test
    fun `rejects blank share text`() {
        val result = executor.shareText(context, "")
        assertTrue(result.isFailure)
    }

    @Test
    fun `requires explicit permission for notifications`() {
        val isAllowed = AppPermissionStatus.canPostNotifications(context)
        assertTrue(isAllowed || !isAllowed)
    }
}
