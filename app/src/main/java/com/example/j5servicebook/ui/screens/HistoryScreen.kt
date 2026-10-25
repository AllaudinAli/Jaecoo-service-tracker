package com.example.j5servicebook.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.History
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.j5servicebook.domain.Fmt
import com.example.j5servicebook.domain.ServiceKind
import com.example.j5servicebook.domain.ServiceRecord
import com.example.j5servicebook.ui.Dashboard
import com.example.j5servicebook.ui.components.Caption
import com.example.j5servicebook.ui.components.Chip
import com.example.j5servicebook.ui.components.ChipRow
import com.example.j5servicebook.ui.components.CircleButton
import com.example.j5servicebook.ui.components.ScreenTitle
import com.example.j5servicebook.ui.components.SoftCard
import com.example.j5servicebook.ui.components.Tag
import com.example.j5servicebook.ui.components.screenPadding
import com.example.j5servicebook.ui.theme.Accent
import com.example.j5servicebook.ui.theme.OnAccent
import java.time.format.DateTimeFormatter
import java.util.Locale

private val MonthYear = DateTimeFormatter.ofPattern("MMM yyyy", Locale.US)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HistoryScreen(d: Dashboard, insets: PaddingValues, onAdd: () -> Unit, onEdit: (ServiceRecord) -> Unit) {
    var filter by rememberSaveable { mutableStateOf<ServiceKind?>(null) }
    // Number periodic services in the order they happened.
    val periodicNumbers = d.records
        .filter { it.kind == ServiceKind.PERIODIC }
        .sortedWith(compareBy({ it.date }, { it.km }, { it.id }))
        .mapIndexed { i, r -> r.id to i + 1 }
        .toMap()
    val shown = d.records.filter { filter == null || it.kind == filter }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = screenPadding(insets),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ScreenTitle(
                    "History",
                    when (d.records.size) {
                        0 -> "No services yet."
                        1 -> "1 record, newest first."
                        else -> "${d.records.size} records, newest first."
                    },
                    Modifier.weight(1f),
                )
                CircleButton(Icons.Rounded.Add, "Add service", onAdd, filled = Accent, tint = OnAccent)
            }
        }
        item {
            ChipRow {
                Chip("All", filter == null, onClick = { filter = null })
                ServiceKind.entries.forEach { k -> Chip(k.label, filter == k, onClick = { filter = k }) }
            }
        }
        if (shown.isEmpty()) {
            item {
                Column(Modifier.fillMaxWidth().padding(top = 64.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.History, null, Modifier.size(40.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        if (d.records.isEmpty()) "Services you log appear here." else "Nothing in this filter yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
        items(shown, key = { it.id }) { r ->
            SoftCard(Modifier.animateItem(), onClick = { onEdit(r) }) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        DateBlock(r)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                periodicNumbers[r.id]?.let { "Service $it" } ?: r.kind.label,
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Spacer(Modifier.height(2.dp))
                            Caption(listOfNotNull(Fmt.km(r.km), r.workshop?.takeIf { it.isNotBlank() }).joinToString(" · "))
                        }
                        r.cost?.let { Text(Fmt.pkr(it), style = MaterialTheme.typography.titleSmall) }
                    }
                    if (r.itemsDone.isNotEmpty() || r.fittedParts.isNotEmpty()) {
                        Spacer(Modifier.height(14.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            r.itemsDone.sortedBy { it.ordinal }.forEach { Tag(it.label) }
                            r.fittedParts.forEach { Tag("Genuine: ${it.name.ifBlank { it.kind.label }}", emphasised = true) }
                        }
                    }
                    val extra = listOfNotNull(
                        r.jobCardNo?.takeIf { it.isNotBlank() }?.let { "Job card $it" },
                        r.notes?.takeIf { it.isNotBlank() },
                    )
                    if (extra.isNotEmpty()) {
                        Spacer(Modifier.height(10.dp))
                        Caption(extra.joinToString("\n"))
                    }
                }
            }
        }
    }
}

/** Calendar-style date: big day number over month and year. */
@Composable
private fun DateBlock(r: ServiceRecord) {
    Box(
        Modifier
            .size(width = 58.dp, height = 62.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(r.date.dayOfMonth.toString(), style = MaterialTheme.typography.headlineSmall)
            Text(r.date.format(MonthYear), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
