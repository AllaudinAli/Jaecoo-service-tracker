package com.example.j5servicebook

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.j5servicebook.reminders.Notifier
import com.example.j5servicebook.ui.AppRoot
import com.example.j5servicebook.ui.MainViewModel
import com.example.j5servicebook.ui.theme.J5Theme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels { MainViewModel.Factory }

    /** Set when a notification's "Log service" action opens the app. */
    private val pendingAction = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) pendingAction.value = intent.getStringExtra(Notifier.EXTRA_ACTION)
        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            J5Theme(wallpaperColors = settings.wallpaperColors) {
                AppRoot(
                    viewModel = viewModel,
                    pendingAction = pendingAction.value,
                    onActionHandled = { pendingAction.value = null },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        pendingAction.value = intent.getStringExtra(Notifier.EXTRA_ACTION)
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshToday()
    }
}
