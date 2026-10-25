package com.example.j5servicebook.ui.screens

import android.Manifest
import android.app.Activity
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.example.j5servicebook.data.CarRepository
import com.example.j5servicebook.domain.Fmt
import com.example.j5servicebook.ui.components.CarImage
import com.example.j5servicebook.ui.components.Caption
import com.example.j5servicebook.ui.components.CardRow
import com.example.j5servicebook.ui.components.DateField
import com.example.j5servicebook.ui.components.Gutter
import com.example.j5servicebook.ui.components.NumberField
import com.example.j5servicebook.ui.components.OnDarkMuted
import com.example.j5servicebook.ui.components.OverlayPill
import com.example.j5servicebook.ui.components.PlainField
import com.example.j5servicebook.ui.components.PrimaryButton
import com.example.j5servicebook.ui.components.SoftCard
import com.example.j5servicebook.ui.components.TopBar
import com.example.j5servicebook.ui.theme.Accent
import com.example.j5servicebook.ui.theme.OnAccent
import java.time.LocalDate

@Composable
fun SetupScreen(onDone: (name: String, purchaseDate: LocalDate, km: Int, seedInspection: Boolean) -> Unit) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    BackHandler(enabled = step == 1) { step = 0 }
    AnimatedContent(
        targetState = step,
        transitionSpec = { (fadeIn() + slideInHorizontally { it / 6 }) togetherWith fadeOut() },
        label = "setup",
    ) { s ->
        if (s == 0) Welcome(onStart = { step = 1 }) else CarForm(onBack = { step = 0 }, onDone = onDone)
    }
}

private val HeroBackground = Color(0xFF0B0B0B)

@Composable
private fun Welcome(onStart: () -> Unit) {
    // White system bar icons while this dark screen shows, whatever the theme.
    val view = LocalView.current
    DisposableEffect(view) {
        val window = (view.context as? Activity)?.window
        val bars = window?.let { WindowCompat.getInsetsController(it, view) }
        val light = bars?.isAppearanceLightStatusBars
        val lightNav = bars?.isAppearanceLightNavigationBars
        bars?.isAppearanceLightStatusBars = false
        bars?.isAppearanceLightNavigationBars = false
        onDispose {
            light?.let { bars.isAppearanceLightStatusBars = it }
            lightNav?.let { bars.isAppearanceLightNavigationBars = it }
        }
    }
    Box(Modifier.fillMaxSize().background(HeroBackground)) {
        // Warm glow behind the car.
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                Brush.radialGradient(
                    listOf(Accent.copy(alpha = 0.22f), Color.Transparent),
                    center = Offset(size.width * 0.5f, size.height * 0.42f),
                    radius = size.width * 0.75f,
                ),
                radius = size.width * 0.75f,
                center = Offset(size.width * 0.5f, size.height * 0.42f),
            )
        }
        Column(
            Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = Gutter),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(20.dp))
            BrandMark()
            Spacer(Modifier.weight(1f))
            CarImage(
                Modifier.requiredWidth(460.dp).offset(x = 30.dp),
                height = 230.dp,
            )
            Spacer(Modifier.weight(0.6f))
            Text(
                "Every service,\nright on time.",
                style = MaterialTheme.typography.displayMedium,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(14.dp))
            Text(
                "Service dates, maintenance and warranty for your Jaecoo J5, worked out from the owner's manual.",
                style = MaterialTheme.typography.bodyLarge,
                color = OnDarkMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
            Spacer(Modifier.height(32.dp))
            PrimaryButton("Get started", onStart)
            Spacer(Modifier.height(18.dp))
            OverlayPill("Offline · your data stays on this phone", Icons.Rounded.Lock)
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun BrandMark() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(34.dp).clip(RoundedCornerShape(11.dp)).background(Accent),
            contentAlignment = Alignment.Center,
        ) {
            Text("J5", style = MaterialTheme.typography.labelLarge, color = OnAccent)
        }
        Spacer(Modifier.width(10.dp))
        Text("Service Book", style = MaterialTheme.typography.titleMedium, color = Color.White)
    }
}

@Composable
private fun CarForm(onBack: () -> Unit, onDone: (String, LocalDate, Int, Boolean) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var purchaseEpoch by rememberSaveable { mutableStateOf(CarRepository.DEFAULT_PURCHASE.toEpochDay()) }
    var km by rememberSaveable { mutableStateOf(CarRepository.DEFAULT_KM.toString()) }
    var seed by rememberSaveable { mutableStateOf(true) }
    val purchase = LocalDate.ofEpochDay(purchaseEpoch)
    val kmValue = km.toIntOrNull()
    val seedApplies = CarRepository.SEED_INSPECTION_DATE >= purchase && (kmValue ?: 0) >= CarRepository.SEED_INSPECTION_KM

    val finish = { onDone(name.trim(), purchase, kmValue ?: 0, seed && seedApplies) }
    // Ask for notifications as part of setup, then finish whatever the answer.
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { finish() }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = Gutter),
    ) {
        TopBar("Your car", onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(20.dp))
            Text("Just the\nbasics.", style = MaterialTheme.typography.displaySmall)
            Spacer(Modifier.height(10.dp))
            Caption("The app works out every service, maintenance item and warranty date from these.")
            Spacer(Modifier.height(28.dp))
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                PlainField("Your name (optional)", name, { name = it }, capitalizeWords = true)
                DateField("Purchase date (sales invoice)", purchase, { purchaseEpoch = it.toEpochDay() })
                NumberField(
                    label = "Current odometer",
                    value = km,
                    onChange = { km = it },
                    suffix = "km",
                    error = if (kmValue == null) "Enter the km shown on the cluster" else null,
                    imeAction = ImeAction.Done,
                )
            }
            if (seedApplies) {
                Spacer(Modifier.height(14.dp))
                SoftCard(onClick = { seed = !seed }) {
                    CardRow(
                        title = "Add the free inspection",
                        supporting = "${Fmt.date(CarRepository.SEED_INSPECTION_DATE)} · about ${Fmt.km(CarRepository.SEED_INSPECTION_KM)}. Doesn't count as Service 1.",
                        trailing = {
                            Switch(
                                checked = seed,
                                onCheckedChange = { seed = it },
                                colors = SwitchDefaults.colors(checkedTrackColor = Accent, checkedThumbColor = OnAccent),
                            )
                        },
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
        PrimaryButton(
            "Continue",
            onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permission.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    finish()
                }
            },
            enabled = kmValue != null,
        )
        Caption(
            "Next, allow notifications so you get service reminders.",
            Modifier.fillMaxWidth().padding(vertical = 14.dp),
        )
    }
}
