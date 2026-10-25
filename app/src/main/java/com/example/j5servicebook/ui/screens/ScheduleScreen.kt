package com.example.j5servicebook.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.j5servicebook.domain.Code
import com.example.j5servicebook.domain.Fmt
import com.example.j5servicebook.domain.ItemId
import com.example.j5servicebook.domain.Limit
import com.example.j5servicebook.domain.MAINTENANCE_FREE
import com.example.j5servicebook.domain.Status
import com.example.j5servicebook.ui.Dashboard
import com.example.j5servicebook.ui.components.CardRow
import com.example.j5servicebook.ui.components.Chip
import com.example.j5servicebook.ui.components.ChipRow
import com.example.j5servicebook.ui.components.IconBadge
import com.example.j5servicebook.ui.components.NoteCard
import com.example.j5servicebook.ui.components.NumberBadge
import com.example.j5servicebook.ui.components.RowDivider
import com.example.j5servicebook.ui.components.ScreenTitle
import com.example.j5servicebook.ui.components.SectionHeader
import com.example.j5servicebook.ui.components.SoftCard
import com.example.j5servicebook.ui.components.StatusDot
import com.example.j5servicebook.ui.components.Tag
import com.example.j5servicebook.ui.components.ThinProgress
import com.example.j5servicebook.ui.components.icon
import com.example.j5servicebook.ui.components.label
import com.example.j5servicebook.ui.components.screenPadding
import com.example.j5servicebook.ui.components.statusColor
import com.example.j5servicebook.ui.theme.Accent

private enum class ScheduleFilter(val label: String) { ALL("All"), SERVICES("Services"), ITEMS("Parts and fluids") }

@Composable
fun ScheduleScreen(d: Dashboard, insets: PaddingValues, onItemClick: (ItemId) -> Unit) {
    var filter by rememberSaveable { mutableStateOf(ScheduleFilter.ALL) }
    val showServices = filter != ScheduleFilter.ITEMS
    val showItems = filter != ScheduleFilter.SERVICES

    LazyColumn(Modifier.fillMaxSize(), contentPadding = screenPadding(insets)) {
        item { ScreenTitle("Schedule", "Every 3 months or 5,000 km, whichever comes first.") }
        item {
            ChipRow {
                ScheduleFilter.entries.forEach { f -> Chip(f.label, filter == f, onClick = { filter = f }) }
            }
        }

        if (showServices) {
            item { SectionHeader("Periodic services") }
            item {
                SoftCard {
                    d.upcoming.forEachIndexed { i, s ->
                        if (i > 0) RowDivider(start = 78.dp)
                        CardRow(
                            title = Fmt.date(s.effectiveDate),
                            supporting = if (s.limit == Limit.DATE) "Service ${s.number} · or ${Fmt.km(s.dueKm)}" else "Service ${s.number} · at ${Fmt.km(s.dueKm)}",
                            leading = { NumberBadge(s.number, highlighted = i == 0) },
                            trailing = {
                                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Tag("Air ${s.airFilter.short}", emphasised = s.airFilter == Code.R)
                                    Tag("A/C ${s.acFilter.short}", emphasised = s.acFilter == Code.R)
                                }
                            },
                        )
                    }
                }
            }
            item {
                Spacer(Modifier.height(12.dp))
                NoteCard("I inspect · I/R inspect or replace · R replace. Oil and filter are replaced at every service.", Icons.Rounded.Info)
            }
        }

        if (showItems) {
            item { SectionHeader("Parts and fluids") }
            item {
                SoftCard {
                    d.intervals.forEachIndexed { i, item ->
                        if (i > 0) RowDivider(start = 74.dp)
                        CardRow(
                            title = item.rule.label,
                            supporting = "Due ${Fmt.date(item.effectiveDate)}",
                            leading = { IconBadge(item.rule.item.icon()) },
                            onClick = { onItemClick(item.rule.item) },
                            trailing = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (item.status != Status.OK) {
                                        StatusDot(item.status.label(), statusColor(item.status))
                                    } else {
                                        Text(
                                            "${(item.progress * 100).toInt().coerceIn(0, 100)}%",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    Icon(
                                        Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                        null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            },
                            below = {
                                Spacer(Modifier.height(12.dp))
                                ThinProgress(
                                    item.progress.toFloat(),
                                    if (item.status == Status.OK) Accent else statusColor(item.status),
                                    track = MaterialTheme.colorScheme.background,
                                )
                            },
                        )
                    }
                }
            }
            item {
                Spacer(Modifier.height(12.dp))
                NoteCard(
                    "Counted from the last time each was done, or from purchase. Maintenance-free: ${MAINTENANCE_FREE.joinToString(" and ")}.",
                    Icons.Rounded.Info,
                )
            }
        }
    }
}
