package com.example.j5servicebook.ui.sheets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import com.example.j5servicebook.domain.FittedPart
import com.example.j5servicebook.domain.Fmt
import com.example.j5servicebook.domain.ItemId
import com.example.j5servicebook.domain.Odometer
import com.example.j5servicebook.domain.ServiceKind
import com.example.j5servicebook.domain.ServiceRecord
import com.example.j5servicebook.domain.SparePartKind
import com.example.j5servicebook.ui.Dashboard
import com.example.j5servicebook.ui.components.Caption
import com.example.j5servicebook.ui.components.Chip
import com.example.j5servicebook.ui.components.ChipRow
import com.example.j5servicebook.ui.components.DateField
import com.example.j5servicebook.ui.components.Gutter
import com.example.j5servicebook.ui.components.NumberField
import com.example.j5servicebook.ui.components.PlainField
import com.example.j5servicebook.ui.components.PrimaryButton
import com.example.j5servicebook.ui.components.SecondaryButton
import com.example.j5servicebook.ui.components.SectionHeader
import com.example.j5servicebook.ui.components.SoftCard
import com.example.j5servicebook.ui.components.accentText
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Sheet(title: String, onDismiss: () -> Unit, subtitle: String? = null, content: @Composable ColumnScope.() -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Gutter)
                .padding(bottom = 24.dp),
        ) {
            Text(title, style = MaterialTheme.typography.headlineMedium)
            if (subtitle != null) {
                Spacer(Modifier.height(6.dp))
                Caption(subtitle)
            }
            Spacer(Modifier.height(24.dp))
            content()
        }
    }
}

@Composable
fun OdometerSheet(latest: Odometer, onDismiss: () -> Unit, onSave: (Int) -> Unit) {
    var text by rememberSaveable { mutableStateOf("") }
    val km = text.toIntOrNull()
    val error = if (km != null && km < latest.km) "Can't be lower than the last reading, ${Fmt.km(latest.km)}" else null
    Sheet("Update odometer", onDismiss, subtitle = "Last reading ${Fmt.km(latest.km)} on ${Fmt.date(latest.date)}.") {
        NumberField(
            label = "Odometer",
            value = text,
            onChange = { text = it },
            suffix = "km",
            error = error,
            imeAction = ImeAction.Done,
        )
        Spacer(Modifier.height(20.dp))
        PrimaryButton("Save", onClick = { km?.let(onSave) }, enabled = km != null && error == null)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LogServiceSheet(
    d: Dashboard,
    existing: ServiceRecord?,
    preset: ItemId?,
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

    fun suggestions(k: ServiceKind) = d.engine.suggestedItems(k, date, km ?: d.latest.km, d.today) + listOfNotNull(preset)

    val items = remember { mutableStateListOf<ItemId>().apply { addAll(existing?.itemsDone ?: suggestions(initialKind)) } }
    var itemsTouched by rememberSaveable { mutableStateOf(existing != null) }
    val parts = remember { mutableStateListOf<FittedPart>().apply { addAll(existing?.fittedParts.orEmpty()) } }

    Sheet(
        if (existing == null) "Log service" else "Edit service",
        onDismiss,
        subtitle = if (existing == null && kind == ServiceKind.PERIODIC) "This will be Service ${d.next.number}." else null,
    ) {
        ChipRow {
            ServiceKind.entries.forEach { k ->
                Chip(k.label, kind == k, onClick = {
                    kind = k
                    if (!itemsTouched) {
                        items.clear()
                        items.addAll(suggestions(k))
                    }
                })
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DateField("Date", date, { epochDay = it.toEpochDay() }, Modifier.weight(1f))
            NumberField("Odometer", kmText, { kmText = it }, Modifier.weight(1f), suffix = "km", error = if (km == null) "Required" else null)
        }

        SectionHeader("Work done")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ItemId.entries.forEach { item ->
                val checked = item in items
                Chip(
                    item.label,
                    selected = checked,
                    icon = if (checked) Icons.Rounded.Check else null,
                    onClick = {
                        itemsTouched = true
                        if (checked) items.remove(item) else items.add(item)
                    },
                )
            }
        }

        SectionHeader("Genuine parts fitted")
        if (parts.isNotEmpty()) {
            SoftCard {
                Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    parts.forEachIndexed { index, part ->
                        PartEditor(part, onChange = { parts[index] = it }, onRemove = { parts.removeAt(index) })
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
        }
        Chip("Add a genuine part", selected = false, icon = Icons.Rounded.Add, onClick = { parts.add(FittedPart("", SparePartKind.STANDARD)) })
        Caption(
            "Starts a spare-part warranty from this date: 12 months or 20,000 km, or 3 months or 5,000 km for glass, wipers, clutch and brake parts.",
            Modifier.padding(start = 4.dp, top = 10.dp),
        )

        SectionHeader("Details")
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            PlainField("Workshop", workshop, { workshop = it }, capitalizeWords = true)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NumberField("Cost", costText, { costText = it }, Modifier.weight(1f), suffix = "PKR")
                PlainField("Job card no.", jobCard, { jobCard = it }, Modifier.weight(1f))
            }
            PlainField("Notes", notes, { notes = it }, singleLine = false)
        }

        Spacer(Modifier.height(28.dp))
        PrimaryButton(
            "Save",
            enabled = km != null,
            onClick = {
                val value = km ?: return@PrimaryButton
                onSave(
                    ServiceRecord(
                        id = existing?.id ?: 0,
                        date = date,
                        km = value,
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
        )
        if (onDelete != null) {
            Spacer(Modifier.height(10.dp))
            SecondaryButton("Delete", onDelete, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun PartEditor(part: FittedPart, onChange: (FittedPart) -> Unit, onRemove: () -> Unit) {
    var menu by remember { mutableStateOf(false) }
    Column(Modifier.padding(vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PlainField("Part", part.name, { onChange(part.copy(name = it)) }, Modifier.weight(1f))
            IconButton(onClick = onRemove) { Icon(Icons.Rounded.Close, contentDescription = "Remove part") }
        }
        Row(
            Modifier.clickable { menu = true }.padding(vertical = 8.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Caption("${part.kind.label} · ${part.kind.months} months or ${Fmt.km(part.kind.km)}")
            Spacer(Modifier.width(8.dp))
            Text("Change", style = MaterialTheme.typography.labelMedium, color = accentText())
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
