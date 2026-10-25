package com.example.j5servicebook.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.j5servicebook.domain.Fmt
import com.example.j5servicebook.domain.ItemId
import com.example.j5servicebook.domain.Status
import com.example.j5servicebook.ui.Dashboard
import com.example.j5servicebook.ui.components.CarImage
import com.example.j5servicebook.ui.components.CardRow
import com.example.j5servicebook.ui.components.Caption
import com.example.j5servicebook.ui.components.Gutter
import com.example.j5servicebook.ui.components.OnDarkMuted
import com.example.j5servicebook.ui.components.PrimaryButton
import com.example.j5servicebook.ui.components.RowDivider
import com.example.j5servicebook.ui.components.SectionHeader
import com.example.j5servicebook.ui.components.SoftCard
import com.example.j5servicebook.ui.components.SpecPill
import com.example.j5servicebook.ui.components.StatusDot
import com.example.j5servicebook.ui.components.TopBar
import com.example.j5servicebook.ui.components.label
import com.example.j5servicebook.ui.components.statusColor

@Composable
fun ItemDetailScreen(d: Dashboard, item: ItemId, insets: PaddingValues, onBack: () -> Unit, onLog: () -> Unit) {
    val due = d.intervals.firstOrNull { it.rule.item == item } ?: return
    val rule = due.rule
    val done = d.records.filter { item in it.itemsDone }
    // "Replace every 2 years or 40,000 km · DOT4" -> the spec after the dot, if any.
    val spec = rule.detail.substringAfter(" · ", "").takeIf { it.isNotEmpty() }

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Gutter),
        ) {
            TopBar("Maintenance", onBack)
            Spacer(Modifier.height(20.dp))
            Box(Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth(0.62f)) {
                    Text(rule.label, style = MaterialTheme.typography.headlineLarge)
                    Spacer(Modifier.height(10.dp))
                    Caption(rule.detail.substringBefore(" · ") + ".")
                    Spacer(Modifier.height(24.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SpecPill("Every ${Fmt.km(rule.km)}", Icons.Rounded.Speed)
                        rule.months?.let { m ->
                            SpecPill(if (m % 12 == 0) "Every ${m / 12} year${if (m == 12) "" else "s"}" else "Every $m months", Icons.Rounded.Event)
                        }
                        spec?.let { SpecPill(it.replaceFirstChar(Char::uppercase), Icons.Rounded.Inventory2) }
                        SpecPill(
                            if (due.doneBefore) "Last done ${Fmt.shortDate(due.lastDate)}" else "From purchase",
                            Icons.Rounded.History,
                        )
                    }
                }
                // The car bleeds off the right edge, as in the reference's detail page.
                CarImage(
                    Modifier
                        .align(Alignment.CenterEnd)
                        .requiredWidth(330.dp)
                        .offset(x = 150.dp),
                    height = 170.dp,
                )
            }

            SectionHeader("Done before")
            if (done.isEmpty()) {
                Caption("Not done yet. Counted from purchase on ${Fmt.date(d.profile.purchaseDate)}.")
            } else {
                SoftCard {
                    done.forEachIndexed { i, r ->
                        if (i > 0) RowDivider()
                        CardRow(
                            title = Fmt.date(r.date),
                            supporting = listOfNotNull(Fmt.km(r.km), r.workshop).joinToString(" · "),
                        )
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }

        // Dark bottom panel with the key date and the main action.
        Surface(
            color = MaterialTheme.colorScheme.inverseSurface,
            contentColor = Color.White,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        ) {
            Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(Gutter)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Next due", style = MaterialTheme.typography.bodyMedium, color = OnDarkMuted, modifier = Modifier.weight(1f))
                    if (due.status != Status.OK) StatusDot(due.status.label(), statusColor(due.status), textColor = Color.White)
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(Fmt.date(due.effectiveDate), style = MaterialTheme.typography.displaySmall, color = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "/ ${Fmt.km(due.dueKm)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnDarkMuted,
                        modifier = Modifier.padding(bottom = 6.dp),
                    )
                }
                Spacer(Modifier.height(20.dp))
                PrimaryButton("Log ${rule.label.lowercase()}", onLog)
            }
        }
    }
}
