package com.example.j5servicebook.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.j5servicebook.domain.ItemId
import com.example.j5servicebook.reminders.Notifier
import com.example.j5servicebook.ui.screens.HistoryScreen
import com.example.j5servicebook.ui.screens.HomeScreen
import com.example.j5servicebook.ui.screens.ScheduleScreen
import com.example.j5servicebook.ui.screens.SettingsScreen
import com.example.j5servicebook.ui.screens.SetupScreen
import com.example.j5servicebook.ui.screens.SpecsScreen
import com.example.j5servicebook.ui.screens.WarrantyScreen
import com.example.j5servicebook.ui.sheets.ItemHistorySheet
import com.example.j5servicebook.ui.sheets.LogServiceSheet
import com.example.j5servicebook.ui.sheets.OdometerSheet
import kotlinx.coroutines.launch

private enum class Tab(val label: String, val icon: ImageVector, val selectedIcon: ImageVector) {
    HOME("Home", Icons.Outlined.Home, Icons.Filled.Home),
    SCHEDULE("Schedule", Icons.Outlined.CalendarMonth, Icons.Filled.CalendarMonth),
    HISTORY("History", Icons.Outlined.History, Icons.Filled.History),
    WARRANTY("Warranty", Icons.Outlined.VerifiedUser, Icons.Filled.VerifiedUser),
}

private enum class Overlay { SPECS, SETTINGS }

/** Id sentinel for "log a new service" in the log sheet state. */
private const val NEW_RECORD = 0L

@Composable
fun AppRoot(viewModel: MainViewModel, pendingAction: String?, onActionHandled: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        when (val s = state) {
            UiState.Loading -> Unit
            UiState.NeedsSetup -> SetupScreen(onDone = { date, km, seed -> viewModel.completeSetup(date, km, seed) })
            is UiState.Ready -> MainScaffold(s.dashboard, viewModel, pendingAction, onActionHandled)
        }
        // Solid strip behind the status bar so scrolled content doesn't run under the clock.
        Box(
            Modifier
                .fillMaxWidth()
                .windowInsetsTopHeight(WindowInsets.statusBars)
                .background(MaterialTheme.colorScheme.background),
        )
    }
}

@Composable
private fun MainScaffold(
    d: Dashboard,
    viewModel: MainViewModel,
    pendingAction: String?,
    onActionHandled: () -> Unit,
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableStateOf(Tab.HOME) }
    var overlay by rememberSaveable { mutableStateOf<Overlay?>(null) }
    var logSheetId by rememberSaveable { mutableStateOf<Long?>(null) }
    var odometerSheet by rememberSaveable { mutableStateOf(false) }
    var historyItem by rememberSaveable { mutableStateOf<ItemId?>(null) }

    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(viewModel) {
        viewModel.messages.collect { snackbar.showSnackbar(it, duration = SnackbarDuration.Short) }
    }
    LaunchedEffect(pendingAction) {
        if (pendingAction == Notifier.ACTION_LOG_SERVICE) {
            overlay = null
            tab = Tab.HOME
            logSheetId = NEW_RECORD
        }
        if (pendingAction != null) onActionHandled()
    }

    BackHandler(enabled = overlay != null) { overlay = null }
    BackHandler(enabled = overlay == null && tab != Tab.HOME) { tab = Tab.HOME }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            if (overlay == null) {
                Column {
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    NavigationBar(containerColor = MaterialTheme.colorScheme.background, tonalElevation = 0.dp) {
                        Tab.entries.forEach { t ->
                            NavigationBarItem(
                                selected = tab == t,
                                onClick = { tab = t },
                                icon = { Icon(if (tab == t) t.selectedIcon else t.icon, contentDescription = null) },
                                label = { Text(t.label) },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                                    selectedIconColor = MaterialTheme.colorScheme.onSurface,
                                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                ),
                            )
                        }
                    }
                }
            }
        },
    ) { insets ->
        AnimatedContent(
            targetState = overlay to tab,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "screen",
        ) { (o, t) ->
            when (o) {
                Overlay.SPECS -> SpecsScreen(insets, onBack = { overlay = null })
                Overlay.SETTINGS -> SettingsScreen(
                    insets = insets,
                    settings = settings,
                    onBack = { overlay = null },
                    onLeadDays = viewModel::setLeadDays,
                    onWallpaperColors = viewModel::setWallpaperColors,
                    onExport = viewModel::exportTo,
                    onImport = viewModel::importFrom,
                    onRunReminderCheck = viewModel::runReminderCheck,
                )
                null -> when (t) {
                    Tab.HOME -> HomeScreen(
                        d = d,
                        insets = insets,
                        onUpdateOdometer = { odometerSheet = true },
                        onLogService = { logSheetId = NEW_RECORD },
                        onOpenSettings = { overlay = Overlay.SETTINGS },
                    )
                    Tab.SCHEDULE -> ScheduleScreen(d, insets, onItemClick = { historyItem = it })
                    Tab.HISTORY -> HistoryScreen(
                        d = d,
                        insets = insets,
                        onAdd = { logSheetId = NEW_RECORD },
                        onEdit = { logSheetId = it.id },
                    )
                    Tab.WARRANTY -> WarrantyScreen(d, insets, onOpenSpecs = { overlay = Overlay.SPECS })
                }
            }
        }
    }

    if (odometerSheet) {
        OdometerSheet(
            latest = d.latest,
            onDismiss = { odometerSheet = false },
            onSave = {
                viewModel.updateOdometer(it)
                odometerSheet = false
            },
        )
    }

    logSheetId?.let { id ->
        val existing = d.records.firstOrNull { it.id == id }
        LogServiceSheet(
            d = d,
            existing = existing,
            onDismiss = { logSheetId = null },
            onSave = {
                viewModel.saveRecord(it, d.latest.km)
                logSheetId = null
            },
            onDelete = existing?.let { record ->
                {
                    logSheetId = null
                    viewModel.deleteRecord(record)
                    scope.launch {
                        val result = snackbar.showSnackbar("Service deleted", actionLabel = "Undo", duration = SnackbarDuration.Long)
                        if (result == SnackbarResult.ActionPerformed) viewModel.restoreRecord(record)
                    }
                }
            },
        )
    }

    historyItem?.let { item ->
        ItemHistorySheet(d = d, item = item, onDismiss = { historyItem = null })
    }
}
