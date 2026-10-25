package com.example.j5servicebook.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.j5servicebook.domain.ItemId
import com.example.j5servicebook.reminders.Notifier
import com.example.j5servicebook.ui.screens.HistoryScreen
import com.example.j5servicebook.ui.screens.HomeScreen
import com.example.j5servicebook.ui.screens.ItemDetailScreen
import com.example.j5servicebook.ui.screens.ScheduleScreen
import com.example.j5servicebook.ui.screens.SettingsScreen
import com.example.j5servicebook.ui.screens.SetupScreen
import com.example.j5servicebook.ui.screens.SpecsScreen
import com.example.j5servicebook.ui.screens.WarrantyScreen
import com.example.j5servicebook.ui.sheets.LogServiceSheet
import com.example.j5servicebook.ui.sheets.OdometerSheet
import com.example.j5servicebook.ui.theme.Accent
import com.example.j5servicebook.ui.theme.OnAccent
import kotlinx.coroutines.launch

enum class Tab(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Rounded.Home),
    SCHEDULE("Schedule", Icons.Rounded.CalendarMonth),
    HISTORY("History", Icons.Rounded.History),
    WARRANTY("Warranty", Icons.Rounded.Shield),
}

private enum class Overlay { SPECS, SETTINGS, ITEM }

/** Id sentinel for "log a new service" in the log sheet state. */
private const val NEW_RECORD = 0L

@Composable
fun AppRoot(viewModel: MainViewModel, pendingAction: String?, onActionHandled: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // Surface (not a plain background) so text outside the Scaffold, like Setup, gets onBackground.
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(Modifier.fillMaxSize()) {
            when (val s = state) {
                UiState.Loading -> Unit
                UiState.NeedsSetup -> SetupScreen(onDone = viewModel::completeSetup)
                is UiState.Ready -> MainScaffold(s.dashboard, viewModel, pendingAction, onActionHandled)
            }
            if (state is UiState.Ready) {
                // Solid strip behind the status bar so scrolled content doesn't run under the clock.
                Box(
                    Modifier
                        .fillMaxWidth()
                        .windowInsetsTopHeight(WindowInsets.statusBars)
                        .background(MaterialTheme.colorScheme.background),
                )
            }
        }
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
    var detailItem by rememberSaveable { mutableStateOf<ItemId?>(null) }
    var logSheetId by rememberSaveable { mutableStateOf<Long?>(null) }
    var logPreset by rememberSaveable { mutableStateOf<ItemId?>(null) }
    var odometerSheet by rememberSaveable { mutableStateOf(false) }

    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun openLog(preset: ItemId? = null) {
        logPreset = preset
        logSheetId = NEW_RECORD
    }

    LaunchedEffect(viewModel) {
        viewModel.messages.collect { snackbar.showSnackbar(it, duration = SnackbarDuration.Short) }
    }
    LaunchedEffect(pendingAction) {
        if (pendingAction == Notifier.ACTION_LOG_SERVICE) {
            overlay = null
            tab = Tab.HOME
            openLog()
        }
        if (pendingAction != null) onActionHandled()
    }

    BackHandler(enabled = overlay != null) { overlay = null }
    BackHandler(enabled = overlay == null && tab != Tab.HOME) { tab = Tab.HOME }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = {
            SnackbarHost(snackbar) {
                Snackbar(
                    it,
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = Color.White,
                    actionColor = Accent,
                )
            }
        },
        bottomBar = {
            AnimatedVisibility(overlay == null, enter = fadeIn(), exit = fadeOut()) {
                FloatingNavBar(selected = tab, onSelect = { tab = it })
            }
        },
    ) { insets ->
        AnimatedContent(
            targetState = overlay to tab,
            transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(160)) },
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
                    onOwnerName = viewModel::setOwnerName,
                    onExport = viewModel::exportTo,
                    onImport = viewModel::importFrom,
                    onRunReminderCheck = viewModel::runReminderCheck,
                )
                Overlay.ITEM -> detailItem?.let { item ->
                    ItemDetailScreen(
                        d = d,
                        item = item,
                        insets = insets,
                        onBack = { overlay = null },
                        onLog = { openLog(item) },
                    )
                }
                null -> when (t) {
                    Tab.HOME -> HomeScreen(
                        d = d,
                        ownerName = settings.ownerName,
                        insets = insets,
                        onUpdateOdometer = { odometerSheet = true },
                        onLogService = { openLog() },
                        onOpenSettings = { overlay = Overlay.SETTINGS },
                        onOpenSchedule = { tab = Tab.SCHEDULE },
                    )
                    Tab.SCHEDULE -> ScheduleScreen(d, insets, onItemClick = {
                        detailItem = it
                        overlay = Overlay.ITEM
                    })
                    Tab.HISTORY -> HistoryScreen(
                        d = d,
                        insets = insets,
                        onAdd = { openLog() },
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
            preset = logPreset,
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
}

/** Dark floating pill bar; the selected tab expands into a yellow pill with its label. */
@Composable
private fun FloatingNavBar(selected: Tab, onSelect: (Tab) -> Unit) {
    // Content scrolls behind the bar, so fade it into the background around the floating pill.
    val bg = MaterialTheme.colorScheme.background
    Box(
        Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(0f to bg.copy(alpha = 0f), 0.4f to bg))
            .navigationBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 12.dp),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(68.dp)
                .clip(CircleShape)
                .background(Color(0xFF1C1C1C))
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Tab.entries.forEach { t ->
                val isSelected = t == selected
                Row(
                    Modifier
                        .height(52.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Accent else Color.Transparent)
                        .clickable { onSelect(t) }
                        .animateContentSize()
                        .padding(horizontal = if (isSelected) 18.dp else 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        t.icon,
                        contentDescription = t.label,
                        tint = if (isSelected) OnAccent else Color(0xFF8E8E8E),
                        modifier = Modifier.size(24.dp),
                    )
                    if (isSelected) {
                        Spacer(Modifier.width(8.dp))
                        Text(t.label, style = MaterialTheme.typography.labelLarge, color = OnAccent)
                    }
                }
            }
        }
    }
}
