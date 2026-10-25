package com.example.j5servicebook.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.j5servicebook.domain.EVERY_VISIT_CHECKS
import com.example.j5servicebook.domain.FIRST_SERVICE_CHECKLIST
import com.example.j5servicebook.domain.Fmt
import com.example.j5servicebook.domain.Limit
import com.example.j5servicebook.domain.SEVERE_USE_NOTE
import com.example.j5servicebook.domain.ScheduleEngine
import com.example.j5servicebook.domain.Status
import com.example.j5servicebook.ui.Dashboard
import com.example.j5servicebook.ui.components.ExpandableList
import com.example.j5servicebook.ui.components.FootNote
import com.example.j5servicebook.ui.components.ListRow
import com.example.j5servicebook.ui.components.Metric
import com.example.j5servicebook.ui.components.Panel
import com.example.j5servicebook.ui.components.PanelPadding
import com.example.j5servicebook.ui.components.RowDivider
import com.example.j5servicebook.ui.components.ScreenTitle
import com.example.j5servicebook.ui.components.SectionLabel
import com.example.j5servicebook.ui.components.StatusDot
import com.example.j5servicebook.ui.components.Tag
import com.example.j5servicebook.ui.components.label
import com.example.j5servicebook.ui.components.screenPadding
import com.example.j5servicebook.ui.components.statusColor

@Composable
fun HomeScreen(
    d: Dashboard,
    insets: PaddingValues,
    onUpdateOdometer: () -> Unit,
    onLogService: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = screenPadding(insets)) {
        item {
            ScreenTitle(d.profile.name, subtitle = "Since ${Fmt.date(d.profile.purchaseDate)}") {
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Outlined.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item { NextServicePanel(d, onLogService) }
        item {
            Spacer(Modifier.height(12.dp))
            OdometerPanel(d, onUpdateOdometer)
        }
        item { SectionLabel("At this visit") }
        item {
            Panel {
                d.visit.forEachIndexed { i, v ->
                    if (i > 0) RowDivider()
                    ListRow(title = v.label, trailing = { Tag(v.action, emphasised = v.replace) })
                }
            }
        }
        item {
            Spacer(Modifier.height(12.dp))
            ExpandableList("Inspections", "${EVERY_VISIT_CHECKS.size} checks at every periodic service", EVERY_VISIT_CHECKS)
        }
        if (d.next.number == 1) {
            item {
                Spacer(Modifier.height(12.dp))
                ExpandableList("First service checklist", "${FIRST_SERVICE_CHECKLIST.size}-point check, first 5,000 km or 3 months", FIRST_SERVICE_CHECKLIST)
            }
        }
        item { FootNote("Severe use. $SEVERE_USE_NOTE", Modifier.padding(top = 12.dp)) }
    }
}

@Composable
private fun NextServicePanel(d: Dashboard, onLogService: () -> Unit) {
    val s = d.next
    val color = statusColor(s.status)
    Panel {
        Column(Modifier.padding(PanelPadding)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Next · Service ${s.number}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                StatusDot(s.status.label(), color)
            }
            Spacer(Modifier.height(10.dp))
            Text(Fmt.date(s.effectiveDate), style = MaterialTheme.typography.displaySmall)
            Spacer(Modifier.height(4.dp))
            Text(
                if (s.limit == Limit.DATE) "Date limit comes first · expect about ${Fmt.km(s.expectedKm)}"
                else "Km limit comes first · at ${Fmt.km(s.dueKm)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                val overdueByDate = s.status == Status.OVERDUE && s.daysLeft < 0
                Metric(
                    value = if (s.daysLeft == 0L) "Today" else Fmt.span(s.daysLeft),
                    caption = if (overdueByDate) "overdue" else if (s.daysLeft == 0L) "due" else "left",
                    modifier = Modifier.weight(1f),
                )
                Metric(
                    value = Fmt.km(kotlin.math.abs(s.kmLeft)),
                    caption = if (s.kmLeft < 0) "over the limit" else "to go",
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(20.dp))
            Button(onClick = onLogService, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                Text("Log service")
            }
        }
    }
}

@Composable
private fun OdometerPanel(d: Dashboard, onUpdate: () -> Unit) {
    val stale = ScheduleEngine.days(d.latest.date, d.today)
    Panel {
        Row(Modifier.padding(PanelPadding), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Odometer", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Text(Fmt.km(d.latest.km), style = MaterialTheme.typography.headlineSmall)
                Text(
                    "${if (stale == 0L) "Updated today" else "Updated ${Fmt.date(d.latest.date)}"} · about ${"%.0f".format(d.engine.kmPerDay)} km a day",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            FilledTonalButton(onClick = onUpdate) { Text("Update") }
        }
    }
}
