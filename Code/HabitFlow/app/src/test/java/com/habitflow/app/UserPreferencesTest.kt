package com.habitflow.app

import com.habitflow.app.core.datastore.AppTheme
import com.habitflow.app.core.datastore.UserPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UserPreferencesTest {

    @Test
    fun testDefaultPreferences() {
        val prefs = UserPreferences()
        assertEquals(AppTheme.SYSTEM, prefs.appTheme)
        assertTrue(prefs.isNotificationEnabled)
    }

    @Test
    fun testThemeEnumValues() {
        val themes = AppTheme.entries
        assertEquals(3, themes.size)
        assertTrue(themes.contains(AppTheme.SYSTEM))
        assertTrue(themes.contains(AppTheme.LIGHT))
        assertTrue(themes.contains(AppTheme.DARK))
    }
}
