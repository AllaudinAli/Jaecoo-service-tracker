package com.example.j5servicebook.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.j5servicebook.domain.Code
import com.example.j5servicebook.domain.Fmt
import com.example.j5servicebook.domain.ItemId
import com.example.j5servicebook.domain.Limit
import com.example.j5servicebook.domain.MAINTENANCE_FREE
import com.example.j5servicebook.domain.Status
import com.example.j5servicebook.ui.Dashboard
import com.example.j5servicebook.ui.components.FootNote
import com.example.j5servicebook.ui.components.ListRow
import com.example.j5servicebook.ui.components.Panel
import com.example.j5servicebook.ui.components.RowDivider
import com.example.j5servicebook.ui.components.ScreenTitle
import com.example.j5servicebook.ui.components.SectionLabel
import com.example.j5servicebook.ui.components.StatusDot
import com.example.j5servicebook.ui.components.Tag
import com.example.j5servicebook.ui.components.ThinProgress
import com.example.j5servicebook.ui.components.label
import com.example.j5servicebook.ui.components.screenPadding
import com.example.j5servicebook.ui.components.statusColor

@Composable
fun ScheduleScreen(d: Dashboard, insets: PaddingValues, onItemClick: (ItemId) -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = screenPadding(insets)) {
        item { ScreenTitle("Schedule", subtitle = "Every 3 months or 5,000 km, whichever comes first") }

        item { SectionLabel("Periodic services") }
        item {
            Panel {
                d.upcoming.forEachIndexed { i, s ->
                    if (i > 0) RowDivider()
                    ListRow(
                        title = "Service ${s.number}",
                        supporting = buildString {
                            append(Fmt.km(s.dueKm))
                            if (s.limit == Limit.DATE) append(" · expect ~${Fmt.km(s.expectedKm)}") else append(" · km limit first")
                        },
                        trailing = {
                            androidx.compose.foundation.layout.Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                                Text(Fmt.date(s.effectiveDate), style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Tag("Air ${s.airFilter.short}", emphasised = s.airFilter == Code.R)
                                    Tag("A/C ${s.acFilter.short}", emphasised = s.acFilter == Code.R)
                                }
                            }
                        },
                    )
                }
            }
        }
        item { FootNote("I inspect · I/R inspect or replace · R replace. Oil and filter are replaced at every service.") }

        item { SectionLabel("Distance and time items") }
        item {
            Panel {
                d.intervals.forEachIndexed { i, item ->
                    if (i > 0) RowDivider()
                    ListRow(
                        title = item.rule.label,
                        supporting = item.rule.detail,
                        onClick = { onItemClick(item.rule.item) },
                        trailing = {
                            androidx.compose.foundation.layout.Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                                Text(Fmt.date(item.effectiveDate), style = MaterialTheme.typography.titleMedium)
                                if (item.status != Status.OK) {
                                    StatusDot(item.status.label(), statusColor(item.status))
                                } else {
                                    Text(
                                        Fmt.km(item.dueKm),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        },
                        below = {
                            Spacer(Modifier.height(10.dp))
                            ThinProgress(
                                item.progress.toFloat(),
                                if (item.status == Status.OK) MaterialTheme.colorScheme.onSurface else statusColor(item.status),
                            )
                        },
                    )
                }
            }
        }
        item { FootNote("Counted from the last time each was done, or from purchase. Maintenance-free: ${MAINTENANCE_FREE.joinToString(" and ")}.") }
    }
}
