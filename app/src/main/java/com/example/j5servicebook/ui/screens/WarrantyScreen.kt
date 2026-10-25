package com.example.j5servicebook.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.j5servicebook.domain.Fmt
import com.example.j5servicebook.domain.WARRANTY_CONDITIONS
import com.example.j5servicebook.domain.WARRANTY_EXCLUSIONS
import com.example.j5servicebook.domain.WARRANTY_KM_NOTE
import com.example.j5servicebook.domain.WarrantyState
import com.example.j5servicebook.domain.WarrantyStatus
import com.example.j5servicebook.ui.Dashboard
import com.example.j5servicebook.ui.components.BulletText
import com.example.j5servicebook.ui.components.FootNote
import com.example.j5servicebook.ui.components.ListRow
import com.example.j5servicebook.ui.components.Panel
import com.example.j5servicebook.ui.components.PanelPadding
import com.example.j5servicebook.ui.components.RowDivider
import com.example.j5servicebook.ui.components.ScreenTitle
import com.example.j5servicebook.ui.components.SectionLabel
import com.example.j5servicebook.ui.components.StatusDot
import com.example.j5servicebook.ui.components.ThinProgress
import com.example.j5servicebook.ui.components.label
import com.example.j5servicebook.ui.components.screenPadding
import com.example.j5servicebook.ui.components.warrantyColor

@Composable
fun WarrantyScreen(d: Dashboard, insets: PaddingValues, onOpenSpecs: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = screenPadding(insets)) {
        item {
            ScreenTitle("Warranty", subtitle = "From ${Fmt.date(d.profile.purchaseDate)} · whichever limit comes first")
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                d.mainWarranties.forEach { MainCoverage(it) }
            }
        }

        item { SectionLabel("Special parts") }
        item {
            Panel {
                d.specialWarranties.forEachIndexed { i, w ->
                    if (i > 0) RowDivider()
                    WarrantyRow(w)
                }
            }
        }

        if (d.sparePartWarranties.isNotEmpty()) {
            item { SectionLabel("Genuine parts fitted later") }
            item {
                Panel {
                    d.sparePartWarranties.forEachIndexed { i, w ->
                        if (i > 0) RowDivider()
                        WarrantyRow(w, extra = "Fitted ${Fmt.date(w.startDate)}")
                    }
                }
            }
        }
        item {
            FootNote(
                "Genuine parts fitted at an authorised dealer get 12 months or 20,000 km. " +
                    "Glass, mirrors, wiper blades, clutch plate and brake parts get 3 months or 5,000 km.",
            )
        }

        item { SectionLabel("Conditions") }
        item {
            Panel {
                Column(Modifier.padding(PanelPadding), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    WARRANTY_CONDITIONS.forEach { BulletText(it) }
                    BulletText(WARRANTY_KM_NOTE)
                }
            }
        }

        item { SectionLabel("Not covered") }
        item {
            Panel {
                Text(
                    WARRANTY_EXCLUSIONS.joinToString(", "),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(PanelPadding),
                )
            }
        }

        item {
            Spacer(Modifier.height(12.dp))
            Panel {
                ListRow(
                    title = "Fluids and specs",
                    supporting = "Oil, coolant, transmission and brake fluid",
                    onClick = onOpenSpecs,
                    trailing = {
                        Icon(
                            Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun MainCoverage(w: WarrantyState) {
    val color = warrantyColor(w.status)
    Panel {
        Column(Modifier.padding(PanelPadding)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(w.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                StatusDot(w.status.label(), color)
            }
            Spacer(Modifier.height(6.dp))
            Text(Fmt.date(w.expiryDate), style = MaterialTheme.typography.headlineSmall)
            Text(
                listOfNotNull("or ${Fmt.km(w.limitKm)}", w.note, if (w.status != WarrantyStatus.EXPIRED) "${Fmt.span(w.daysLeft)} left" else null)
                    .joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(14.dp))
            ThinProgress(w.progress.toFloat(), if (w.status == WarrantyStatus.ACTIVE) MaterialTheme.colorScheme.onSurface else color)
        }
    }
}

@Composable
private fun WarrantyRow(w: WarrantyState, extra: String? = null) {
    ListRow(
        title = w.name,
        supporting = listOfNotNull(extra, "Ends ${Fmt.date(w.expiryDate)} or ${Fmt.km(w.limitKm)}").joinToString(" · "),
        trailing = {
            // Quiet when active; colour only when it needs attention.
            if (w.status == WarrantyStatus.ACTIVE) {
                Text(w.status.label(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                StatusDot(w.status.label(), warrantyColor(w.status))
            }
        },
    )
}
