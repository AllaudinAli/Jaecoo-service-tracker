package com.example.j5servicebook.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.j5servicebook.domain.Fmt
import com.example.j5servicebook.domain.ServiceKind
import com.example.j5servicebook.domain.ServiceRecord
import com.example.j5servicebook.ui.Dashboard
import com.example.j5servicebook.ui.components.ListRow
import com.example.j5servicebook.ui.components.Panel
import com.example.j5servicebook.ui.components.ScreenTitle
import com.example.j5servicebook.ui.components.Tag
import com.example.j5servicebook.ui.components.screenPadding

@Composable
fun HistoryScreen(d: Dashboard, insets: PaddingValues, onAdd: () -> Unit, onEdit: (ServiceRecord) -> Unit) {
    // Number periodic services in the order they happened.
    val periodicNumbers = d.records
        .filter { it.kind == ServiceKind.PERIODIC }
        .sortedWith(compareBy({ it.date }, { it.km }, { it.id }))
        .mapIndexed { i, r -> r.id to i + 1 }
        .toMap()

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = screenPadding(insets),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            ScreenTitle(
                "History",
                subtitle = when (d.records.size) {
                    0 -> "No services yet"
                    1 -> "1 record"
                    else -> "${d.records.size} records"
                },
            ) {
                FilledTonalIconButton(onClick = onAdd) { Icon(Icons.Outlined.Add, contentDescription = "Add service") }
            }
        }
        if (d.records.isEmpty()) {
            item {
                Text(
                    "Services you log appear here, newest first.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                )
            }
        }
        items(d.records, key = { it.id }) { r ->
            Panel(Modifier.animateItem()) {
                ListRow(
                    title = Fmt.date(r.date),
                    supporting = listOfNotNull(Fmt.km(r.km), r.workshop?.takeIf { it.isNotBlank() }).joinToString(" · "),
                    onClick = { onEdit(r) },
                    trailing = {
                        Column(horizontalAlignment = Alignment.End) {
                            Tag(periodicNumbers[r.id]?.let { "Service $it" } ?: r.kind.label)
                            r.cost?.let {
                                Spacer(Modifier.height(4.dp))
                                Text(Fmt.pkr(it), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    },
                    below = {
                        val details = buildList {
                            if (r.itemsDone.isNotEmpty()) add(r.itemsDone.sortedBy { it.ordinal }.joinToString(", ") { it.label })
                            if (r.fittedParts.isNotEmpty()) add("Genuine parts: " + r.fittedParts.joinToString(", ") { it.name.ifBlank { it.kind.label } })
                            r.jobCardNo?.takeIf { it.isNotBlank() }?.let { add("Job card $it") }
                            r.notes?.takeIf { it.isNotBlank() }?.let { add(it) }
                        }
                        details.forEach {
                            Spacer(Modifier.height(6.dp))
                            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                )
            }
        }
    }
}
