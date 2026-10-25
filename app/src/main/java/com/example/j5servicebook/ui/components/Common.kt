package com.example.j5servicebook.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.FilterAlt
import androidx.compose.material.icons.rounded.LocalGasStation
import androidx.compose.material.icons.rounded.OilBarrel
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.TireRepair
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.j5servicebook.R
import com.example.j5servicebook.domain.ItemId

/**
 * The Jaecoo J5 cut-out with a soft floor shadow, so it sits on whatever is behind it.
 * The photo is roughly 2:1; [height] sets its size.
 */
@Composable
fun CarImage(modifier: Modifier = Modifier, height: Dp = 170.dp, shadowColor: Color = Color.Black) {
    Box(modifier.height(height), contentAlignment = Alignment.BottomCenter) {
        Canvas(Modifier.fillMaxWidth().height(height * 0.22f)) {
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(shadowColor.copy(alpha = 0.28f), Color.Transparent),
                    center = Offset(size.width / 2, size.height * 0.55f),
                    radius = size.width * 0.48f,
                ),
                topLeft = Offset(size.width * 0.04f, 0f),
                size = Size(size.width * 0.92f, size.height),
            )
        }
        Image(
            painter = painterResource(R.drawable.jaecoo_j5),
            contentDescription = "Jaecoo J5",
            contentScale = ContentScale.Fit,
            modifier = Modifier.height(height).padding(bottom = height * 0.04f),
        )
    }
}

fun ItemId.icon(): ImageVector = when (this) {
    ItemId.OIL_AND_FILTER -> Icons.Rounded.OilBarrel
    ItemId.AIR_FILTER -> Icons.Rounded.Air
    ItemId.AC_FILTER -> Icons.Rounded.AcUnit
    ItemId.TIRE_ROTATION -> Icons.Rounded.TireRepair
    ItemId.THROTTLE_CLEAN -> Icons.Rounded.Tune
    ItemId.ALIGNMENT -> Icons.Rounded.Straighten
    ItemId.SPARK_PLUGS -> Icons.Rounded.Bolt
    ItemId.BRAKE_FLUID -> Icons.Rounded.WaterDrop
    ItemId.COOLANT -> Icons.Rounded.Thermostat
    ItemId.TRANSMISSION_FLUID -> Icons.Rounded.Settings
    ItemId.FUEL_FILTER -> Icons.Rounded.LocalGasStation
    ItemId.CHARCOAL_CANISTER -> Icons.Rounded.FilterAlt
    ItemId.FUEL_FILLER_HOSE -> Icons.Rounded.LocalGasStation
    ItemId.FUEL_VAPOR_PIPE -> Icons.Rounded.Air
}

/** Reference-style top bar: round back button, centred title, optional round action. */
@Composable
fun TopBar(title: String, onBack: () -> Unit, action: (@Composable () -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        CircleButton(Icons.AutoMirrored.Rounded.ArrowBack, "Back", onBack)
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )
        if (action != null) action() else Spacer(Modifier.size(52.dp))
    }
}
