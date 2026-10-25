package com.example.j5servicebook.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.OilBarrel
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.j5servicebook.domain.FLUID_SPECS
import com.example.j5servicebook.domain.MAINTENANCE_FREE
import com.example.j5servicebook.ui.components.CarImage
import com.example.j5servicebook.ui.components.CardRow
import com.example.j5servicebook.ui.components.Caption
import com.example.j5servicebook.ui.components.IconBadge
import com.example.j5servicebook.ui.components.NoteCard
import com.example.j5servicebook.ui.components.RowDivider
import com.example.j5servicebook.ui.components.SectionHeader
import com.example.j5servicebook.ui.components.SoftCard
import com.example.j5servicebook.ui.components.TopBar
import com.example.j5servicebook.ui.components.screenPadding

/** A full-screen page with the round back button, used for Specs and Settings. */
@Composable
fun SubPage(title: String, insets: PaddingValues, onBack: () -> Unit, content: LazyListScope.() -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = screenPadding(insets)) {
        item { TopBar(title, onBack) }
        content()
    }
}

private fun specIcon(item: String) = when {
    item.startsWith("Engine oil") -> Icons.Rounded.OilBarrel
    item.startsWith("Transmission") -> Icons.Rounded.Settings
    item.startsWith("Power battery") -> Icons.Rounded.AcUnit
    item.contains("coolant") -> Icons.Rounded.Thermostat
    item.startsWith("Brake") -> Icons.Rounded.WaterDrop
    else -> Icons.Rounded.Bolt
}

@Composable
fun SpecsScreen(insets: PaddingValues, onBack: () -> Unit) {
    SubPage("Specs", insets, onBack) {
        item {
            Text("Jaecoo J5 SHS", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(top = 20.dp))
            Spacer(Modifier.height(8.dp))
            Caption("Fluids, capacities and parts from the owner's manual, page 234.")
        }
        item {
            CarImage(Modifier.fillMaxWidth().padding(vertical = 20.dp), height = 190.dp)
        }
        item { SectionHeader("Fluids and parts") }
        item {
            SoftCard {
                FLUID_SPECS.forEachIndexed { i, f ->
                    if (i > 0) RowDivider(start = 74.dp)
                    CardRow(
                        title = f.spec,
                        supporting = listOfNotNull(f.item, f.capacity).joinToString(" · "),
                        leading = { IconBadge(specIcon(f.item)) },
                        below = f.note?.let { note -> { Spacer(Modifier.height(8.dp)); Caption(note, Modifier.padding(start = 54.dp)) } },
                    )
                }
            }
        }
        item {
            Spacer(Modifier.height(12.dp))
            NoteCard("Maintenance-free: ${MAINTENANCE_FREE.joinToString(" and ")}.", Icons.Rounded.Info)
        }
    }
}
