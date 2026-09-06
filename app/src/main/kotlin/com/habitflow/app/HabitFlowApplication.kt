package com.habitflow.app

import dagger.hilt.android.HiltAndroidApp
import android.app.Application
@HiltAndroidApp
class HabitFlowApplication : Application() {
    val database by lazy { HabitFlowDatabase.get(this) }
    val repository by lazy { HabitRepository(database) }
}
