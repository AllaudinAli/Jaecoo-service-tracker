package com.example.j5servicebook

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.example.j5servicebook.data.AppDatabase
import com.example.j5servicebook.data.CarRepository
import com.example.j5servicebook.data.SettingsStore
import com.example.j5servicebook.reminders.Notifier
import com.example.j5servicebook.reminders.ReminderScheduler

class J5App : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        Notifier.createChannel(this)
        ReminderScheduler.schedule(this)
    }
}

/** Manual dependency wiring; the app is small enough not to need a DI framework. */
class AppContainer(context: Context) {
    private val database = Room.databaseBuilder(context, AppDatabase::class.java, "j5.db").build()
    val repository = CarRepository(database.carDao())
    val settings = SettingsStore(context)
}
