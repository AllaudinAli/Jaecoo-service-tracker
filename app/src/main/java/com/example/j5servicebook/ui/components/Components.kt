package com.example.j5servicebook.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.j5servicebook.domain.Status
import com.example.j5servicebook.domain.WarrantyStatus
import com.example.j5servicebook.ui.theme.LocalStatusColors
import com.example.j5servicebook.ui.theme.SectionLabelStyle

val Gutter = 16.dp
val PanelPadding = 20.dp

/** Content padding for a tab: side gutters plus the system bar insets from the scaffold. */
fun screenPadding(insets: PaddingValues): PaddingValues = PaddingValues(
    start = Gutter,
    end = Gutter,
    top = insets.calculateTopPadding() + 8.dp,
    bottom = insets.calculateBottomPadding() + 32.dp,
)

@Composable
fun ScreenTitle(title: String, subtitle: String? = null, actions: @Composable RowScope.() -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 28.dp, bottom = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.headlineLarge)
            if (subtitle != null) {
                Spacer(Modifier.height(4.dp))
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        actions()
    }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = SectionLabelStyle,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(start = 8.dp, top = 24.dp, bottom = 8.dp),
    )
}

@Composable
fun Panel(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(content = content)
    }
}

@Composable
fun RowDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = PanelPadding),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}

/** A two-line list row with an optional trailing slot. */
@Composable
fun ListRow(
    title: String,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
    below: @Composable (ColumnScope.() -> Unit)? = null,
) {
    Column(
        modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = PanelPadding, vertical = 14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (supporting != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(supporting, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (trailing != null) {
                Spacer(Modifier.width(12.dp))
                trailing()
            }
        }
        below?.invoke(this)
    }
}

@Composable
fun statusColor(status: Status): Color = when (status) {
    Status.OK -> LocalStatusColors.current.ok
    Status.DUE_SOON -> LocalStatusColors.current.soon
    Status.OVERDUE -> LocalStatusColors.current.overdue
}

@Composable
fun warrantyColor(status: WarrantyStatus): Color = when (status) {
    WarrantyStatus.ACTIVE -> LocalStatusColors.current.ok
    WarrantyStatus.ENDING_SOON -> LocalStatusColors.current.soon
    WarrantyStatus.EXPIRED -> MaterialTheme.colorScheme.onSurfaceVariant
}

fun Status.label() = when (this) {
    Status.OK -> "On track"
    Status.DUE_SOON -> "Due soon"
    Status.OVERDUE -> "Overdue"
}

fun WarrantyStatus.label() = when (this) {
    WarrantyStatus.ACTIVE -> "Active"
    WarrantyStatus.ENDING_SOON -> "Ending soon"
    WarrantyStatus.EXPIRED -> "Expired"
}

/** A coloured dot with a short label. */
@Composable
fun StatusDot(text: String, color: Color, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(6.dp))
        Text(text, style = MaterialTheme.typography.labelMedium, color = color)
    }
}

/** A 4dp rounded progress bar, quieter than the Material indicator. */
@Composable
fun ThinProgress(fraction: Float, color: Color, modifier: Modifier = Modifier, height: Dp = 4.dp) {
    val value by animateFloatAsState(fraction.coerceIn(0f, 1f), label = "progress")
    val track = MaterialTheme.colorScheme.surfaceContainerHighest
    Canvas(modifier.fillMaxWidth().height(height)) {
        val r = CornerRadius(size.height / 2)
        drawRoundRect(track, cornerRadius = r)
        if (value > 0f) drawRoundRect(color, size = Size(size.width * value, size.height), cornerRadius = r)
    }
}

/** A small monochrome tag, e.g. "Replace" or "A/C R". */
@Composable
fun Tag(text: String, emphasised: Boolean = false) {
    val scheme = MaterialTheme.colorScheme
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        color = if (emphasised) scheme.onPrimary else scheme.onSurfaceVariant,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (emphasised) scheme.primary else scheme.surfaceContainerHighest)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

/** A panel row that expands to show a plain list. */
@Composable
fun ExpandableList(title: String, supporting: String, items: List<String>) {
    var open by rememberSaveable(title) { mutableStateOf(false) }
    val angle by animateFloatAsState(if (open) 180f else 0f, label = "chevron")
    Panel {
        ListRow(
            title = title,
            supporting = supporting,
            onClick = { open = !open },
            trailing = {
                Icon(
                    Icons.Outlined.KeyboardArrowDown,
                    contentDescription = if (open) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.rotate(angle),
                )
            },
        )
        AnimatedVisibility(open) {
            Column(
                Modifier.padding(start = PanelPadding, end = PanelPadding, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items.forEach { BulletText(it) }
            }
        }
    }
}

@Composable
fun BulletText(text: String) {
    Row {
        Box(
            Modifier
                .padding(top = 8.dp)
                .size(4.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.outline),
        )
        Spacer(Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

/** Big number with a caption under it. */
@Composable
fun Metric(value: String, caption: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(value, style = MaterialTheme.typography.headlineSmall)
        Text(caption, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun FootNote(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(horizontal = 8.dp, vertical = 12.dp),
    )
}
