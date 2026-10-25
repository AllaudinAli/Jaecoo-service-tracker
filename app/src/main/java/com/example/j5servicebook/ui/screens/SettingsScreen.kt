package com.example.j5servicebook.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings as AndroidSettings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.example.j5servicebook.BuildConfig
import com.example.j5servicebook.data.Settings
import com.example.j5servicebook.ui.components.FootNote
import com.example.j5servicebook.ui.components.ListRow
import com.example.j5servicebook.ui.components.Panel
import com.example.j5servicebook.ui.components.PanelPadding
import com.example.j5servicebook.ui.components.RowDivider
import com.example.j5servicebook.ui.components.SectionLabel
import java.time.LocalDate

@Composable
fun SettingsScreen(
    insets: PaddingValues,
    settings: Settings,
    onBack: () -> Unit,
    onLeadDays: (Int) -> Unit,
    onWallpaperColors: (Boolean) -> Unit,
    onExport: (Uri) -> Unit,
    onImport: (Uri) -> Unit,
    onRunReminderCheck: () -> Unit,
) {
    val context = LocalContext.current
    var notificationsAllowed by remember { mutableStateOf(notificationsGranted(context)) }
    LifecycleResumeEffect(Unit) {
        notificationsAllowed = notificationsGranted(context)
        onPauseOrDispose { }
    }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { notificationsAllowed = it }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let(onExport)
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let(onImport)
    }
    var confirmImport by rememberSaveable { mutableStateOf(false) }
    var askedOnce by rememberSaveable { mutableStateOf(false) }
    val activity = context as? android.app.Activity

    SubPage("Settings", insets, onBack) {
        item { SectionLabel("Reminders") }
        item {
            Panel {
                Column(Modifier.padding(PanelPadding)) {
                    Text("Remind me before a service", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(12.dp))
                    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                        Settings.LEAD_OPTIONS.forEachIndexed { i, days ->
                            SegmentedButton(
                                selected = settings.leadDays == days,
                                onClick = { onLeadDays(days) },
                                shape = SegmentedButtonDefaults.itemShape(i, Settings.LEAD_OPTIONS.size),
                                icon = {},
                            ) { Text("$days days") }
                        }
                    }
                }
                RowDivider()
                ListRow(
                    title = "Notifications",
                    supporting = if (notificationsAllowed) "Allowed · daily check around 9:00" else "Off · reminders can't be shown",
                    trailing = if (notificationsAllowed) null else {
                        {
                            TextButton(onClick = {
                                // After a second denial Android stops showing the prompt, so open system settings.
                                if (activity?.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) == false && askedOnce) {
                                    context.startActivity(
                                        Intent(AndroidSettings.ACTION_APP_NOTIFICATION_SETTINGS)
                                            .putExtra(AndroidSettings.EXTRA_APP_PACKAGE, context.packageName),
                                    )
                                } else {
                                    askedOnce = true
                                    permission.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            }) { Text("Allow") }
                        }
                    },
                )
                if (BuildConfig.DEBUG) {
                    RowDivider()
                    ListRow(title = "Run reminder check now", supporting = "Debug builds only", onClick = onRunReminderCheck)
                }
            }
        }

        item { SectionLabel("Appearance") }
        item {
            Panel {
                ListRow(
                    title = "Wallpaper colours",
                    supporting = "Tint the app with your phone's colour palette",
                    onClick = { onWallpaperColors(!settings.wallpaperColors) },
                    trailing = { Switch(checked = settings.wallpaperColors, onCheckedChange = onWallpaperColors) },
                )
            }
        }

        item { SectionLabel("Backup") }
        item {
            Panel {
                ListRow(
                    title = "Export data",
                    supporting = "Save everything to a JSON file",
                    onClick = { exportLauncher.launch("j5-service-book-${LocalDate.now()}.json") },
                )
                RowDivider()
                ListRow(
                    title = "Import data",
                    supporting = "Replace everything with a backup file",
                    onClick = { confirmImport = true },
                )
            }
        }
        item {
            FootNote(
                "Your data stays on this phone. The app has no internet access, so export a backup " +
                    "to Google Drive or your PC from time to time.",
            )
        }
        item { FootNote("J5 Service Book ${BuildConfig.VERSION_NAME}", Modifier.padding(top = 0.dp)) }
    }

    if (confirmImport) {
        AlertDialog(
            onDismissRequest = { confirmImport = false },
            title = { Text("Replace all data?") },
            text = { Text("Your current services, readings and car details will be replaced by the backup.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmImport = false
                    importLauncher.launch(arrayOf("application/json", "application/octet-stream", "text/plain"))
                }) { Text("Choose file") }
            },
            dismissButton = { TextButton(onClick = { confirmImport = false }) { Text("Cancel") } },
        )
    }
}

private fun notificationsGranted(context: android.content.Context): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
