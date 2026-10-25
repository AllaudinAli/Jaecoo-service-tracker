package com.example.j5servicebook.ui.sheets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.j5servicebook.domain.FittedPart
import com.example.j5servicebook.domain.Fmt
import com.example.j5servicebook.domain.INTERVAL_RULES
import com.example.j5servicebook.domain.ItemId
import com.example.j5servicebook.domain.Odometer
import com.example.j5servicebook.domain.ServiceKind
import com.example.j5servicebook.domain.ServiceRecord
import com.example.j5servicebook.domain.SparePartKind
import com.example.j5servicebook.ui.Dashboard
import com.example.j5servicebook.ui.components.DateField
import com.example.j5servicebook.ui.components.Gutter
import com.example.j5servicebook.ui.components.NumberField
import com.example.j5servicebook.ui.components.Panel
import com.example.j5servicebook.ui.components.PlainField
import com.example.j5servicebook.ui.components.SectionLabel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Sheet(title: String, onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Gutter + 4.dp)
                .padding(bottom = 24.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 20.dp))
            content()
        }
    }
}

@Composable
fun OdometerSheet(latest: Odometer, onDismiss: () -> Unit, onSave: (Int) -> Unit) {
    var text by rememberSaveable { mutableStateOf("") }
    val km = text.toIntOrNull()
    val error = when {
        km != null && km < latest.km -> "Can't be lower than the last reading, ${Fmt.km(latest.km)}"
        else -> null
    }
    Sheet("Update odometer", onDismiss) {
        NumberField(
            label = "Odometer",
            value = text,
            onChange = { text = it },
            suffix = "km",
            error = error,
            supporting = "Last reading ${Fmt.km(latest.km)} on ${Fmt.date(latest.date)}",
            imeAction = ImeAction.Done,
        )
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = { km?.let(onSave) },
            enabled = km != null && error == null,
            modifier = Modifier.fillMaxWidth().height(48.dp),
        ) { Text("Save") }
    }
}

@Composable
fun LogServiceSheet(
    d: Dashboard,
    existing: ServiceRecord?,
    onDismiss: () -> Unit,
    onSave: (ServiceRecord) -> Unit,
    onDelete: (() -> Unit)?,
) {
    val initialKind = existing?.kind ?: ServiceKind.PERIODIC
    var epochDay by rememberSaveable { mutableStateOf((existing?.date ?: d.today).toEpochDay()) }
    var kmText by rememberSaveable { mutableStateOf((existing?.km ?: d.latest.km).toString()) }
    var kind by rememberSaveable { mutableStateOf(initialKind) }
    var workshop by rememberSaveable { mutableStateOf(existing?.workshop.orEmpty()) }
    var costText by rememberSaveable { mutableStateOf(existing?.cost?.toString().orEmpty()) }
    var jobCard by rememberSaveable { mutableStateOf(existing?.jobCardNo.orEmpty()) }
    var notes by rememberSaveable { mutableStateOf(existing?.notes.orEmpty()) }
    val date = LocalDate.ofEpochDay(epochDay)
    val km = kmText.toIntOrNull()

    fun suggestions(k: ServiceKind) = d.engine.suggestedItems(k, date, km ?: d.latest.km, d.today)

    val items = remember { mutableStateListOf<ItemId>().apply { addAll(existing?.itemsDone ?: suggestions(initialKind)) } }
    var itemsTouched by rememberSaveable { mutableStateOf(existing != null) }
    val parts = remember { mutableStateListOf<FittedPart>().apply { addAll(existing?.fittedParts.orEmpty()) } }

    Sheet(if (existing == null) "Log service" else "Edit service", onDismiss) {
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            ServiceKind.entries.forEachIndexed { i, k ->
                SegmentedButton(
                    selected = kind == k,
                    onClick = {
                        kind = k
                        if (!itemsTouched) {
                            items.clear()
                            items.addAll(suggestions(k))
                        }
                    },
                    shape = SegmentedButtonDefaults.itemShape(i, ServiceKind.entries.size),
                    icon = {},
                ) { Text(k.label, maxLines = 1, overflow = TextOverflow.Clip, style = MaterialTheme.typography.labelMedium) }
            }
        }
        if (kind == ServiceKind.PERIODIC && existing == null) {
            Text(
                "This will be Service ${d.next.number}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp),
            )
        }
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DateField("Date", date, { epochDay = it.toEpochDay() }, Modifier.weight(1f))
            NumberField("Odometer", kmText, { kmText = it }, Modifier.weight(1f), suffix = "km", error = if (km == null) "Required" else null)
        }

        SectionLabel("Work done", Modifier.padding(top = 4.dp))
        Panel {
            ItemId.entries.forEachIndexed { i, item ->
                if (i > 0) HorizontalDivider(Modifier.padding(start = 56.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                val checked = item in items
                val toggle = {
                    itemsTouched = true
                    if (checked) items.remove(item) else items.add(item)
                    Unit
                }
                Row(
                    Modifier.fillMaxWidth().clickable(onClick = toggle).padding(start = 4.dp, end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(checked = checked, onCheckedChange = { toggle() })
                    Spacer(Modifier.width(4.dp))
                    Column(Modifier.weight(1f).padding(vertical = 10.dp)) {
                        Text(item.label, style = MaterialTheme.typography.bodyLarge)
                        INTERVAL_RULES.firstOrNull { it.item == item }?.let {
                            Text(it.detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        SectionLabel("Genuine parts fitted", Modifier.padding(top = 4.dp))
        Panel {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                parts.forEachIndexed { index, part ->
                    PartEditor(
                        part = part,
                        onChange = { parts[index] = it },
                        onRemove = { parts.removeAt(index) },
                    )
                }
                TextButton(onClick = { parts.add(FittedPart("", SparePartKind.STANDARD)) }) {
                    Icon(Icons.Outlined.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add a genuine part")
                }
            }
        }
        Text(
            "Starts a spare-part warranty from this date: 12 months or 20,000 km, or 3 months or 5,000 km for glass, wipers, clutch and brake parts.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, top = 8.dp),
        )

        SectionLabel("Details", Modifier.padding(top = 4.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            PlainField("Workshop", workshop, { workshop = it })
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NumberField("Cost", costText, { costText = it }, Modifier.weight(1f), suffix = "PKR")
                PlainField("Job card no.", jobCard, { jobCard = it }, Modifier.weight(1f))
            }
            PlainField("Notes", notes, { notes = it }, singleLine = false)
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                onSave(
                    ServiceRecord(
                        id = existing?.id ?: 0,
                        date = date,
                        km = km ?: return@Button,
                        kind = kind,
                        itemsDone = items.toSet(),
                        workshop = workshop.trim().ifEmpty { null },
                        cost = costText.toIntOrNull(),
                        jobCardNo = jobCard.trim().ifEmpty { null },
                        notes = notes.trim().ifEmpty { null },
                        fittedParts = parts.map { it.copy(name = it.name.trim()) },
                    ),
                )
            },
            enabled = km != null,
            modifier = Modifier.fillMaxWidth().height(48.dp),
        ) { Text("Save") }
        if (onDelete != null) {
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth().height(48.dp),
            ) { Text("Delete", color = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
private fun PartEditor(part: FittedPart, onChange: (FittedPart) -> Unit, onRemove: () -> Unit) {
    var menu by remember { mutableStateOf(false) }
    Column(Modifier.padding(vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PlainField("Part", part.name, { onChange(part.copy(name = it)) }, Modifier.weight(1f))
            IconButton(onClick = onRemove) { Icon(Icons.Outlined.Close, contentDescription = "Remove part") }
        }
        Row(
            Modifier.clickable { menu = true }.padding(vertical = 8.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "${part.kind.label} · ${part.kind.months} months or ${Fmt.km(part.kind.km)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text("  Change", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                SparePartKind.entries.forEach { k ->
                    DropdownMenuItem(
                        text = { Text(k.label) },
                        onClick = {
                            onChange(part.copy(kind = k))
                            menu = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun ItemHistorySheet(d: Dashboard, item: ItemId, onDismiss: () -> Unit) {
    val due = d.intervals.firstOrNull { it.rule.item == item }
    val done = d.records.filter { item in it.itemsDone }
    Sheet(item.label, onDismiss) {
        if (due != null) {
            Text(due.rule.detail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
            Panel {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Next due", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(Fmt.date(due.effectiveDate), style = MaterialTheme.typography.headlineSmall)
                    Text(
                        listOfNotNull(due.dueDate?.let { "by ${Fmt.date(it)}" }, "at ${Fmt.km(due.dueKm)}").joinToString(" or "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        SectionLabel("Done before", Modifier.padding(top = 4.dp))
        if (done.isEmpty()) {
            Text(
                "Not done yet. Counted from purchase on ${Fmt.date(d.profile.purchaseDate)}.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp),
            )
        } else {
            Panel {
                done.forEachIndexed { i, r ->
                    if (i > 0) HorizontalDivider(Modifier.padding(horizontal = 20.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    Column(Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                        Text(Fmt.date(r.date), style = MaterialTheme.typography.bodyLarge)
                        Text(
                            listOfNotNull(Fmt.km(r.km), r.workshop).joinToString(" · "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
