package com.example.j5servicebook.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material.icons.rounded.Update
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.j5servicebook.domain.EVERY_VISIT_CHECKS
import com.example.j5servicebook.domain.FIRST_SERVICE_CHECKLIST
import com.example.j5servicebook.domain.Fmt
import com.example.j5servicebook.domain.Limit
import com.example.j5servicebook.domain.PERIODIC_PATTERN
import com.example.j5servicebook.domain.SEVERE_USE_NOTE
import com.example.j5servicebook.domain.ScheduleEngine
import com.example.j5servicebook.domain.Status
import com.example.j5servicebook.ui.Dashboard
import com.example.j5servicebook.ui.components.Avatar
import com.example.j5servicebook.ui.components.CarImage
import com.example.j5servicebook.ui.components.CardRow
import com.example.j5servicebook.ui.components.CircleButton
import com.example.j5servicebook.ui.components.DarkPanel
import com.example.j5servicebook.ui.components.DarkRaised
import com.example.j5servicebook.ui.components.ExpandableCard
import com.example.j5servicebook.ui.components.IconBadge
import com.example.j5servicebook.ui.components.NoteCard
import com.example.j5servicebook.ui.components.OnDarkMuted
import com.example.j5servicebook.ui.components.OverlayPill
import com.example.j5servicebook.ui.components.PrimaryButton
import com.example.j5servicebook.ui.components.RowDivider
import com.example.j5servicebook.ui.components.SectionHeader
import com.example.j5servicebook.ui.components.SoftCard
import com.example.j5servicebook.ui.components.SpecPill
import com.example.j5servicebook.ui.components.StatusDot
import com.example.j5servicebook.ui.components.Tag
import com.example.j5servicebook.ui.components.icon
import com.example.j5servicebook.ui.components.label
import com.example.j5servicebook.ui.components.screenPadding
import com.example.j5servicebook.ui.components.statusColor
import com.example.j5servicebook.ui.theme.Accent
import java.time.LocalTime

@Composable
fun HomeScreen(
    d: Dashboard,
    ownerName: String,
    insets: PaddingValues,
    onUpdateOdometer: () -> Unit,
    onLogService: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSchedule: () -> Unit,
) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = screenPadding(insets)) {
        item { Header(d, ownerName, onUpdateOdometer, onOpenSettings) }
        item { Greeting(d, ownerName) }
        item { CarCard(d, onUpdateOdometer) }
        item {
            Spacer(Modifier.height(12.dp))
            NextServicePanel(d, onLogService, onOpenSchedule)
        }
        item { SectionHeader("At this visit", action = "Full schedule", onAction = onOpenSchedule) }
        item {
            SoftCard {
                d.visit.forEachIndexed { i, v ->
                    if (i > 0) RowDivider(start = 74.dp)
                    CardRow(
                        title = v.label,
                        leading = { IconBadge(v.item.icon()) },
                        trailing = { Tag(v.action, emphasised = v.replace) },
                    )
                }
            }
        }
        item {
            Spacer(Modifier.height(12.dp))
            ExpandableCard("Inspections", "${EVERY_VISIT_CHECKS.size} checks at every service", Icons.Rounded.Checklist, EVERY_VISIT_CHECKS)
        }
        if (d.next.number == 1) {
            item {
                Spacer(Modifier.height(12.dp))
                ExpandableCard(
                    "First service checklist",
                    "${FIRST_SERVICE_CHECKLIST.size}-point check at the first service",
                    Icons.Rounded.Verified,
                    FIRST_SERVICE_CHECKLIST,
                )
            }
        }
        item {
            Spacer(Modifier.height(12.dp))
            NoteCard("Severe use. $SEVERE_USE_NOTE", Icons.Rounded.Info)
        }
    }
}

@Composable
private fun Header(d: Dashboard, ownerName: String, onUpdateOdometer: () -> Unit, onOpenSettings: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("My car", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.DirectionsCar, null, Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(d.profile.name, style = MaterialTheme.typography.titleMedium)
            }
        }
        CircleButton(Icons.Rounded.Speed, "Update odometer", onUpdateOdometer)
        Spacer(Modifier.width(10.dp))
        Avatar(ownerName, onOpenSettings)
    }
}

@Composable
private fun Greeting(d: Dashboard, ownerName: String) {
    val hour = LocalTime.now().hour
    val partOfDay = when (hour) {
        in 5..11 -> "Morning"
        in 12..16 -> "Afternoon"
        else -> "Evening"
    }
    val first = ownerName.trim().substringBefore(' ')
    val s = d.next
    val line = when (s.status) {
        Status.OVERDUE -> "Service ${s.number} is overdue. Book the dealer soon."
        Status.DUE_SOON -> if (s.daysLeft <= 0) "Service ${s.number} is due today." else "Service ${s.number} is due ${Fmt.relativeDays(s.daysLeft).lowercase()}. Time to book."
        Status.OK -> "All good. Service ${s.number} is ${Fmt.relativeDays(s.daysLeft).lowercase()}."
    }
    Column(Modifier.padding(top = 28.dp, bottom = 24.dp)) {
        Text(
            if (first.isNotEmpty()) "$partOfDay, $first!" else "Good ${partOfDay.lowercase()}!",
            style = MaterialTheme.typography.displayMedium,
        )
        Spacer(Modifier.height(10.dp))
        Text(line, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun CarCard(d: Dashboard, onUpdateOdometer: () -> Unit) {
    val stale = ScheduleEngine.days(d.latest.date, d.today)
    SoftCard(onClick = onUpdateOdometer) {
        Box(Modifier.fillMaxWidth().height(256.dp)) {
            Column(Modifier.padding(20.dp)) {
                Text("Jaecoo J5", style = MaterialTheme.typography.headlineMedium)
                Text("SHS hybrid", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(
                Modifier.align(Alignment.TopEnd).padding(16.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SpecPill(Fmt.km(d.latest.km), Icons.Rounded.Speed)
                SpecPill("${"%.0f".format(d.engine.kmPerDay)} km/day", Icons.Rounded.Timeline)
            }
            CarImage(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 4.dp, vertical = 10.dp),
                height = 168.dp,
            )
            OverlayPill(
                if (stale == 0L) "Updated today" else "Updated ${Fmt.shortDate(d.latest.date)}",
                Icons.Rounded.Update,
                Modifier.align(Alignment.BottomEnd).padding(14.dp),
            )
        }
    }
}

@Composable
private fun NextServicePanel(d: Dashboard, onLogService: () -> Unit, onOpenSchedule: () -> Unit) {
    val s = d.next
    DarkPanel {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(Icons.Rounded.Build, size = 44.dp, background = DarkRaised, tint = Accent)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Service ${s.number}", style = MaterialTheme.typography.titleMedium, color = Color.White)
                Text(
                    "Visit ${s.cyclePosition} of ${PERIODIC_PATTERN.size} in the cycle",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnDarkMuted,
                )
            }
            StatusDot(s.status.label(), statusColor(s.status), textColor = Color.White)
        }
        Spacer(Modifier.height(24.dp))
        Text("Next service", style = MaterialTheme.typography.bodySmall, color = OnDarkMuted)
        Spacer(Modifier.height(2.dp))
        Text(Fmt.date(s.effectiveDate), style = MaterialTheme.typography.displaySmall, color = Color.White)
        Spacer(Modifier.height(4.dp))
        Text(
            if (s.limit == Limit.DATE) "Date limit first · expect about ${Fmt.km(s.expectedKm)}" else "Km limit first · at ${Fmt.km(s.dueKm)}",
            style = MaterialTheme.typography.bodySmall,
            color = OnDarkMuted,
        )
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val days = when {
                s.daysLeft > 0 -> "${Fmt.span(s.daysLeft)} left"
                s.daysLeft == 0L -> "Due today"
                else -> "${Fmt.span(s.daysLeft)} late"
            }
            SpecPill(days, Icons.Rounded.Schedule, onDark = true)
            SpecPill(
                if (s.kmLeft >= 0) "${Fmt.km(s.kmLeft)} to go" else "${Fmt.km(-s.kmLeft)} over",
                Icons.Rounded.Speed,
                onDark = true,
            )
        }
        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            CircleButton(
                Icons.Rounded.CalendarMonth,
                "Open schedule",
                onOpenSchedule,
                size = 58.dp,
                filled = DarkRaised,
                tint = Color.White,
            )
            Spacer(Modifier.width(12.dp))
            PrimaryButton("Log service", onLogService, Modifier.weight(1f))
        }
    }
}
