package com.example.j5servicebook.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.j5servicebook.domain.Status
import com.example.j5servicebook.domain.WarrantyStatus
import com.example.j5servicebook.ui.theme.Accent
import com.example.j5servicebook.ui.theme.LocalStatusColors
import com.example.j5servicebook.ui.theme.OnAccent

val Gutter = 20.dp
val CardPadding = 20.dp

/** Content padding for a screen: side gutters plus the scaffold's insets. */
fun screenPadding(insets: PaddingValues, bottomExtra: Dp = 24.dp): PaddingValues = PaddingValues(
    start = Gutter,
    end = Gutter,
    top = insets.calculateTopPadding() + 8.dp,
    bottom = insets.calculateBottomPadding() + bottomExtra,
)

// ---- Text blocks -------------------------------------------------------------------------

@Composable
fun ScreenTitle(title: String, subtitle: String? = null, modifier: Modifier = Modifier) {
    Column(modifier.padding(top = 20.dp, bottom = 20.dp)) {
        Text(title, style = MaterialTheme.typography.headlineLarge)
        if (subtitle != null) {
            Spacer(Modifier.height(8.dp))
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** "Nearby You ........ See All" style section header. */
@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(
        modifier.fillMaxWidth().padding(top = 28.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        if (action != null && onAction != null) {
            Text(
                action,
                style = MaterialTheme.typography.labelLarge,
                color = accentText(),
                modifier = Modifier.clip(CircleShape).clickable(onClick = onAction).padding(horizontal = 6.dp, vertical = 4.dp),
            )
        }
    }
}

/** Yellow reads poorly as text on white, so use a deeper amber there. */
@Composable
fun accentText(): Color = if (MaterialTheme.colorScheme.background.luminance() > 0.5f) Color(0xFFD4A200) else Accent

private fun Color.luminance(): Float = 0.2126f * red + 0.7152f * green + 0.0722f * blue

@Composable
fun Caption(text: String, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Text(text, style = MaterialTheme.typography.bodySmall, color = color, modifier = modifier)
}

// ---- Surfaces ----------------------------------------------------------------------------

/** Soft grey rounded card, the default container. */
@Composable
fun SoftCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = MaterialTheme.shapes.large
    Column(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        content = content,
    )
}

/** Dark panel for the one thing that matters most on a screen. Always dark, in both themes. */
@Composable
fun DarkPanel(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.inverseSurface,
        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
    ) {
        Column(Modifier.padding(CardPadding), content = content)
    }
}

/** Secondary text colour inside a [DarkPanel]. */
val OnDarkMuted = Color(0xFF9A9A9A)
val DarkRaised = Color(0xFF2C2C2C)

@Composable
fun RowDivider(start: Dp = CardPadding) {
    Box(
        Modifier
            .padding(start = start, end = CardPadding)
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.9f)),
    )
}

// ---- Buttons -----------------------------------------------------------------------------

/** Full-width yellow pill, the main action. */
@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val bg by animateColorAsState(if (enabled) Accent else MaterialTheme.colorScheme.surfaceContainerHighest, label = "bg")
    Box(
        modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(CircleShape)
            .background(bg)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelLarge,
            color = if (enabled) OnAccent else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Quiet secondary pill with a border. */
@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.onSurface) {
    Box(
        modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(CircleShape)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge, color = color)
    }
}

/** 52dp round icon button with a hairline border, as in the reference's header buttons. */
@Composable
fun CircleButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 52.dp,
    filled: Color? = null,
    tint: Color = MaterialTheme.colorScheme.onSurface,
) {
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .then(
                if (filled != null) Modifier.background(filled)
                else Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription, tint = tint, modifier = Modifier.size(22.dp))
    }
}

/** Round avatar with the owner's initial. */
@Composable
fun Avatar(name: String, onClick: () -> Unit, size: Dp = 52.dp) {
    Box(
        Modifier.size(size).clip(CircleShape).background(Accent).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            name.trim().firstOrNull()?.uppercase() ?: "J5",
            style = MaterialTheme.typography.titleMedium,
            color = OnAccent,
        )
    }
}

// ---- Pills and chips ---------------------------------------------------------------------

/** Filter chip: yellow when selected, outlined otherwise. */
@Composable
fun Chip(text: String, selected: Boolean, onClick: () -> Unit, icon: ImageVector? = null) {
    val bg by animateColorAsState(if (selected) Accent else Color.Transparent, label = "chip")
    Row(
        Modifier
            .height(44.dp)
            .clip(CircleShape)
            .background(bg)
            .then(if (selected) Modifier else Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, null, Modifier.size(18.dp), tint = if (selected) OnAccent else MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge, color = if (selected) OnAccent else MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun ChipRow(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    Row(
        modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        content = content,
    )
}

/** Small outlined pill with an icon, like "155 mph" in the reference. */
@Composable
fun SpecPill(text: String, icon: ImageVector? = null, modifier: Modifier = Modifier, onDark: Boolean = false) {
    val border = if (onDark) DarkRaised else MaterialTheme.colorScheme.outlineVariant
    val bg = if (onDark) Color.Transparent else MaterialTheme.colorScheme.background
    Row(
        modifier
            .heightIn(min = 34.dp)
            .clip(CircleShape)
            .background(bg)
            .border(1.dp, border, CircleShape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, null, Modifier.size(16.dp), tint = if (onDark) OnDarkMuted else MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.width(6.dp))
        }
        Text(text, style = MaterialTheme.typography.labelMedium, color = if (onDark) Color.White else MaterialTheme.colorScheme.onSurface)
    }
}

/** Dark translucent pill laid over an image, like "Biscayne Boulevard" in the reference. */
@Composable
fun OverlayPill(text: String, icon: ImageVector?, modifier: Modifier = Modifier) {
    Row(
        modifier
            .clip(CircleShape)
            .background(Color(0xE61C1C1C))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, null, Modifier.size(16.dp), tint = Color.White)
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.labelMedium, color = Color.White)
    }
}

/** Compact tag. Emphasised = yellow (e.g. "Replace"), otherwise outlined (e.g. "Inspect"). */
@Composable
fun Tag(text: String, emphasised: Boolean = false, onDark: Boolean = false) {
    val shape = RoundedCornerShape(50)
    val outline = if (onDark) DarkRaised else MaterialTheme.colorScheme.outline
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        color = when {
            emphasised -> OnAccent
            onDark -> Color.White
            else -> MaterialTheme.colorScheme.onSurface
        },
        maxLines = 1,
        modifier = Modifier
            .clip(shape)
            .then(if (emphasised) Modifier.background(Accent) else Modifier.border(1.dp, outline, shape))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

/** Round icon badge used at the start of list rows. */
@Composable
fun IconBadge(icon: ImageVector, size: Dp = 40.dp, background: Color = MaterialTheme.colorScheme.background, tint: Color = MaterialTheme.colorScheme.onSurface) {
    Box(Modifier.size(size).clip(CircleShape).background(background), contentAlignment = Alignment.Center) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(size * 0.5f))
    }
}

/** Number in a circle, e.g. the service number. */
@Composable
fun NumberBadge(number: Int, highlighted: Boolean) {
    Box(
        Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(if (highlighted) Accent else MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            number.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = if (highlighted) OnAccent else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
    }
}

// ---- Status ------------------------------------------------------------------------------

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

@Composable
fun StatusDot(text: String, color: Color, modifier: Modifier = Modifier, textColor: Color = color) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(6.dp))
        Text(text, style = MaterialTheme.typography.labelMedium, color = textColor)
    }
}

/** Rounded progress bar. */
@Composable
fun ThinProgress(
    fraction: Float,
    color: Color,
    modifier: Modifier = Modifier,
    height: Dp = 6.dp,
    track: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
) {
    val value by animateFloatAsState(fraction.coerceIn(0f, 1f), label = "progress")
    Canvas(modifier.fillMaxWidth().height(height)) {
        val r = CornerRadius(size.height / 2)
        drawRoundRect(track, cornerRadius = r)
        if (value > 0f) drawRoundRect(color, size = Size(maxOf(size.height, size.width * value), size.height), cornerRadius = r)
    }
}

// ---- Lists -------------------------------------------------------------------------------

/** Row inside a [SoftCard]: optional leading badge, two lines of text, optional trailing. */
@Composable
fun CardRow(
    title: String,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    leading: @Composable (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    below: @Composable (ColumnScope.() -> Unit)? = null,
) {
    Column(
        modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = CardPadding, vertical = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leading != null) {
                leading()
                Spacer(Modifier.width(14.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (supporting != null) {
                    Spacer(Modifier.height(3.dp))
                    Caption(supporting)
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

/** Card that expands to a plain bullet list. */
@Composable
fun ExpandableCard(title: String, supporting: String, icon: ImageVector, items: List<String>) {
    var open by rememberSaveable(title) { mutableStateOf(false) }
    val angle by animateFloatAsState(if (open) 180f else 0f, label = "chevron")
    SoftCard(onClick = { open = !open }) {
        CardRow(
            title = title,
            supporting = supporting,
            leading = { IconBadge(icon) },
            trailing = {
                Icon(
                    Icons.Rounded.KeyboardArrowDown,
                    contentDescription = if (open) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.rotate(angle),
                )
            },
        )
        AnimatedVisibility(open) {
            Column(
                Modifier.padding(start = CardPadding, end = CardPadding, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items.forEach { BulletText(it) }
            }
        }
    }
}

@Composable
fun BulletText(text: String, color: Color = MaterialTheme.colorScheme.onSurface) {
    Row {
        Box(
            Modifier
                .padding(top = 8.dp)
                .size(5.dp)
                .clip(CircleShape)
                .background(Accent),
        )
        Spacer(Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = color)
    }
}

/** Big value with a caption under it. */
@Composable
fun Metric(value: String, caption: String, modifier: Modifier = Modifier, captionColor: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Column(modifier) {
        Text(value, style = MaterialTheme.typography.headlineSmall)
        Text(caption, style = MaterialTheme.typography.bodySmall, color = captionColor)
    }
}

@Composable
fun NoteCard(text: String, icon: ImageVector) {
    SoftCard {
        Row(Modifier.padding(CardPadding)) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Caption(text)
        }
    }
}
