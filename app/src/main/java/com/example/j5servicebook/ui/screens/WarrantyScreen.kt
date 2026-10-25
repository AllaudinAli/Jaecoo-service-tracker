package com.example.j5servicebook.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.j5servicebook.domain.Fmt
import com.example.j5servicebook.domain.WARRANTY_CONDITIONS
import com.example.j5servicebook.domain.WARRANTY_EXCLUSIONS
import com.example.j5servicebook.domain.WARRANTY_KM_NOTE
import com.example.j5servicebook.domain.WarrantyState
import com.example.j5servicebook.domain.WarrantyStatus
import com.example.j5servicebook.ui.Dashboard
import com.example.j5servicebook.ui.components.BulletText
import com.example.j5servicebook.ui.components.Caption
import com.example.j5servicebook.ui.components.CardPadding
import com.example.j5servicebook.ui.components.CardRow
import com.example.j5servicebook.ui.components.DarkPanel
import com.example.j5servicebook.ui.components.DarkRaised
import com.example.j5servicebook.ui.components.IconBadge
import com.example.j5servicebook.ui.components.OnDarkMuted
import com.example.j5servicebook.ui.components.RowDivider
import com.example.j5servicebook.ui.components.ScreenTitle
import com.example.j5servicebook.ui.components.SectionHeader
import com.example.j5servicebook.ui.components.SoftCard
import com.example.j5servicebook.ui.components.StatusDot
import com.example.j5servicebook.ui.components.Tag
import com.example.j5servicebook.ui.components.ThinProgress
import com.example.j5servicebook.ui.components.label
import com.example.j5servicebook.ui.components.screenPadding
import com.example.j5servicebook.ui.components.warrantyColor
import com.example.j5servicebook.ui.theme.Accent

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WarrantyScreen(d: Dashboard, insets: PaddingValues, onOpenSpecs: () -> Unit) {
    val (whole, others) = d.mainWarranties.partition { it.name == "Whole vehicle" }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = screenPadding(insets)) {
        item { ScreenTitle("Warranty", "From ${Fmt.date(d.profile.purchaseDate)}. Whichever limit comes first.") }
        whole.firstOrNull()?.let { w -> item { WholeVehicle(w) } }
        item {
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                others.forEach { CoverageTile(it, Modifier.weight(1f).fillMaxHeight()) }
            }
        }

        item { SectionHeader("Special parts") }
        item {
            SoftCard {
                d.specialWarranties.forEachIndexed { i, w ->
                    if (i > 0) RowDivider()
                    WarrantyRow(w)
                }
            }
        }

        if (d.sparePartWarranties.isNotEmpty()) {
            item { SectionHeader("Genuine parts fitted later") }
            item {
                SoftCard {
                    d.sparePartWarranties.forEachIndexed { i, w ->
                        if (i > 0) RowDivider()
                        WarrantyRow(w, extra = "Fitted ${Fmt.shortDate(w.startDate)}")
                    }
                }
            }
        }
        item {
            Caption(
                "Genuine parts fitted at an authorised dealer get 12 months or 20,000 km. " +
                    "Glass, mirrors, wiper blades, clutch plate and brake parts get 3 months or 5,000 km.",
                Modifier.padding(horizontal = 4.dp, vertical = 12.dp),
            )
        }

        item { SectionHeader("Conditions") }
        item {
            SoftCard {
                Column(Modifier.padding(CardPadding), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    WARRANTY_CONDITIONS.forEach { BulletText(it) }
                    BulletText(WARRANTY_KM_NOTE)
                }
            }
        }

        item { SectionHeader("Not covered") }
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                WARRANTY_EXCLUSIONS.forEach { Tag(it) }
            }
        }

        item {
            Spacer(Modifier.height(24.dp))
            SoftCard(onClick = onOpenSpecs) {
                CardRow(
                    title = "Fluids and specs",
                    supporting = "Oil, coolant, transmission and brake fluid",
                    leading = { IconBadge(Icons.Rounded.WaterDrop) },
                    trailing = { Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                )
            }
        }
    }
}

@Composable
private fun WholeVehicle(w: WarrantyState) {
    DarkPanel {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(Icons.Rounded.Shield, size = 44.dp, background = DarkRaised, tint = Accent)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(w.name, style = MaterialTheme.typography.titleMedium, color = Color.White)
                Text("Main warranty", style = MaterialTheme.typography.bodySmall, color = OnDarkMuted)
            }
            StatusDot(w.status.label(), warrantyColor(w.status), textColor = Color.White)
        }
        Spacer(Modifier.height(24.dp))
        Text("Covered until", style = MaterialTheme.typography.bodySmall, color = OnDarkMuted)
        Spacer(Modifier.height(2.dp))
        Text(Fmt.date(w.expiryDate), style = MaterialTheme.typography.displaySmall, color = Color.White)
        Text(
            "or ${Fmt.km(w.limitKm)}${if (w.status != WarrantyStatus.EXPIRED) " · ${Fmt.span(w.daysLeft)} left" else ""}",
            style = MaterialTheme.typography.bodySmall,
            color = OnDarkMuted,
        )
        Spacer(Modifier.height(18.dp))
        ThinProgress(w.progress.toFloat(), Accent, track = DarkRaised)
    }
}

@Composable
private fun CoverageTile(w: WarrantyState, modifier: Modifier) {
    SoftCard(modifier) {
        Column(Modifier.fillMaxHeight().padding(18.dp)) {
            Text(w.name, style = MaterialTheme.typography.titleSmall, minLines = 2)
            Spacer(Modifier.height(14.dp))
            Text(Fmt.date(w.expiryDate), style = MaterialTheme.typography.titleLarge)
            Caption(listOfNotNull("or ${Fmt.km(w.limitKm)}", w.note).joinToString(" · "))
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(14.dp))
            ThinProgress(
                w.progress.toFloat(),
                if (w.status == WarrantyStatus.ACTIVE) Accent else warrantyColor(w.status),
                track = MaterialTheme.colorScheme.background,
            )
        }
    }
}

@Composable
private fun WarrantyRow(w: WarrantyState, extra: String? = null) {
    CardRow(
        title = w.name,
        supporting = listOfNotNull(extra, "${Fmt.date(w.expiryDate)} or ${Fmt.km(w.limitKm)}").joinToString(" · "),
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
