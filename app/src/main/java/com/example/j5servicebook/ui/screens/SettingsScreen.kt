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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.j5servicebook.ui.components.Caption
import com.example.j5servicebook.ui.components.CardPadding
import com.example.j5servicebook.ui.components.CardRow
import com.example.j5servicebook.ui.components.Chip
import com.example.j5servicebook.ui.components.ChipRow
import com.example.j5servicebook.ui.components.IconBadge
import com.example.j5servicebook.ui.components.PlainField
import com.example.j5servicebook.ui.components.RowDivider
import com.example.j5servicebook.ui.components.SectionHeader
import com.example.j5servicebook.ui.components.SoftCard
import com.example.j5servicebook.ui.components.accentText
import com.example.j5servicebook.ui.theme.Accent
import com.example.j5servicebook.ui.theme.OnAccent
import kotlinx.coroutines.delay
import java.time.LocalDate

@Composable
fun SettingsScreen(
    insets: PaddingValues,
    settings: Settings,
    onBack: () -> Unit,
    onLeadDays: (Int) -> Unit,
    onWallpaperColors: (Boolean) -> Unit,
    onOwnerName: (String) -> Unit,
    onExport: (Uri) -> Unit,
    onImport: (Uri) -> Unit,
    onRunReminderCheck: () -> Unit,
) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity
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

    // Name is saved shortly after typing stops.
    var name by rememberSaveable { mutableStateOf(settings.ownerName) }
    LaunchedEffect(name) {
        delay(400)
        if (name.trim() != settings.ownerName) onOwnerName(name)
    }

    fun askForNotifications() {
        // After a second denial Android stops showing the prompt, so open system settings.
        if (askedOnce && activity?.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) == false) {
            context.startActivity(
                Intent(AndroidSettings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(AndroidSettings.EXTRA_APP_PACKAGE, context.packageName),
            )
        } else {
            askedOnce = true
            permission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    SubPage("Settings", insets, onBack) {
        item { SectionHeader("Profile") }
        item {
            PlainField("Your name", name, { name = it }, capitalizeWords = true)
            Caption("Used for the greeting on Home.", Modifier.padding(start = 4.dp, top = 8.dp))
        }

        item { SectionHeader("Reminders") }
        item {
            SoftCard {
                Column(Modifier.padding(CardPadding)) {
                    Text("Remind me before a service", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(12.dp))
                    ChipRow {
                        Settings.LEAD_OPTIONS.forEach { days ->
                            Chip("$days days", settings.leadDays == days, onClick = { onLeadDays(days) })
                        }
                    }
                }
                RowDivider()
                CardRow(
                    title = "Notifications",
                    supporting = if (notificationsAllowed) "On · daily check around 9:00" else "Off · reminders can't be shown",
                    leading = { IconBadge(Icons.Rounded.Notifications) },
                    onClick = if (notificationsAllowed) null else ::askForNotifications,
                    trailing = if (notificationsAllowed) null else {
                        { TextButton(onClick = ::askForNotifications) { Text("Turn on", color = accentText()) } }
                    },
                )
                if (BuildConfig.DEBUG) {
                    RowDivider()
                    CardRow(
                        title = "Run reminder check now",
                        supporting = "Debug builds only",
                        leading = { IconBadge(Icons.Rounded.BugReport) },
                        onClick = onRunReminderCheck,
                    )
                }
            }
        }

        item { SectionHeader("Appearance") }
        item {
            SoftCard {
                CardRow(
                    title = "Wallpaper colours",
                    supporting = "Use your phone's palette instead of yellow",
                    leading = { IconBadge(Icons.Rounded.Palette) },
                    onClick = { onWallpaperColors(!settings.wallpaperColors) },
                    trailing = {
                        Switch(
                            checked = settings.wallpaperColors,
                            onCheckedChange = onWallpaperColors,
                            colors = SwitchDefaults.colors(checkedTrackColor = Accent, checkedThumbColor = OnAccent),
                        )
                    },
                )
            }
        }

        item { SectionHeader("Backup") }
        item {
            SoftCard {
                CardRow(
                    title = "Export data",
                    supporting = "Save everything to a JSON file",
                    leading = { IconBadge(Icons.Rounded.FileUpload) },
                    onClick = { exportLauncher.launch("j5-service-book-${LocalDate.now()}.json") },
                    trailing = { Chevron() },
                )
                RowDivider(start = 74.dp)
                CardRow(
                    title = "Import data",
                    supporting = "Replace everything with a backup file",
                    leading = { IconBadge(Icons.Rounded.FileDownload) },
                    onClick = { confirmImport = true },
                    trailing = { Chevron() },
                )
            }
            Caption(
                "Your data stays on this phone. The app has no internet access, so export a backup to Google Drive or your PC from time to time.",
                Modifier.padding(start = 4.dp, top = 12.dp),
            )
        }

        item { SectionHeader("About") }
        item {
            Caption("J5 Service Book ${BuildConfig.VERSION_NAME}", Modifier.padding(start = 4.dp))
            Spacer(Modifier.height(8.dp))
            Caption("Font: Geist, SIL Open Font License 1.1.", Modifier.padding(start = 4.dp))
        }
    }

    if (confirmImport) {
        AlertDialog(
            onDismissRequest = { confirmImport = false },
            containerColor = MaterialTheme.colorScheme.background,
            title = { Text("Replace all data?") },
            text = { Text("Your current services, readings and car details will be replaced by the backup.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmImport = false
                    importLauncher.launch(arrayOf("application/json", "application/octet-stream", "text/plain"))
                }) { Text("Choose file", color = MaterialTheme.colorScheme.onSurface) }
            },
            dismissButton = {
                TextButton(onClick = { confirmImport = false }) { Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            },
        )
    }
}

@Composable
private fun Chevron() {
    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
}

private fun notificationsGranted(context: android.content.Context): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
