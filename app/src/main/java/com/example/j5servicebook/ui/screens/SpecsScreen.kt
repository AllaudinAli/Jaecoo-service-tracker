package com.example.j5servicebook.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.j5servicebook.domain.FLUID_SPECS
import com.example.j5servicebook.domain.MAINTENANCE_FREE
import com.example.j5servicebook.ui.components.FootNote
import com.example.j5servicebook.ui.components.ListRow
import com.example.j5servicebook.ui.components.Panel
import com.example.j5servicebook.ui.components.RowDivider
import com.example.j5servicebook.ui.components.SectionLabel
import com.example.j5servicebook.ui.components.screenPadding

/** A full-screen page with a back arrow, used for Specs and Settings. */
@Composable
fun SubPage(title: String, insets: PaddingValues, onBack: () -> Unit, content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = screenPadding(insets)) {
        item {
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                }
            }
            Text(
                title,
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(start = 8.dp, top = 8.dp, bottom = 12.dp),
            )
        }
        content()
    }
}

@Composable
fun SpecsScreen(insets: PaddingValues, onBack: () -> Unit) {
    SubPage("Fluids and specs", insets, onBack) {
        item { SectionLabel("Owner's manual p. 234") }
        item {
            Panel {
                FLUID_SPECS.forEachIndexed { i, f ->
                    if (i > 0) RowDivider()
                    ListRow(
                        title = f.item,
                        supporting = listOfNotNull(f.capacity, f.note).joinToString("\n").ifEmpty { null },
                        trailing = { Text(f.spec, style = MaterialTheme.typography.titleMedium) },
                    )
                }
            }
        }
        item { FootNote("Maintenance-free: ${MAINTENANCE_FREE.joinToString(" and ")}.") }
    }
}
