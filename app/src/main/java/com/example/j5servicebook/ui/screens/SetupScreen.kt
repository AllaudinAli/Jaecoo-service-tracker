package com.example.j5servicebook.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.j5servicebook.data.CarRepository
import com.example.j5servicebook.domain.Fmt
import com.example.j5servicebook.ui.components.DateField
import com.example.j5servicebook.ui.components.Gutter
import com.example.j5servicebook.ui.components.NumberField
import com.example.j5servicebook.ui.components.Panel
import com.example.j5servicebook.ui.components.PanelPadding
import java.time.LocalDate

@Composable
fun SetupScreen(onDone: (purchaseDate: LocalDate, km: Int, seedInspection: Boolean) -> Unit) {
    var purchaseEpoch by rememberSaveable { mutableStateOf(CarRepository.DEFAULT_PURCHASE.toEpochDay()) }
    var km by rememberSaveable { mutableStateOf(CarRepository.DEFAULT_KM.toString()) }
    var seed by rememberSaveable { mutableStateOf(true) }
    val purchase = LocalDate.ofEpochDay(purchaseEpoch)
    val kmValue = km.toIntOrNull()
    val seedApplies = CarRepository.SEED_INSPECTION_DATE >= purchase && (kmValue ?: 0) >= CarRepository.SEED_INSPECTION_KM

    val finish = { onDone(purchase, kmValue ?: 0, seed && seedApplies) }
    // Ask for notifications as part of setup, then finish whatever the answer.
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { finish() }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Gutter),
    ) {
        Spacer(Modifier.height(56.dp))
        Text("J5 Service Book", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(horizontal = 8.dp))
        Spacer(Modifier.height(8.dp))
        Text(
            "Enter two things and the app works out every service, maintenance item and warranty date.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
        Spacer(Modifier.height(28.dp))
        Panel {
            Column(Modifier.padding(PanelPadding), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column {
                    Text("Car", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(CarRepository.DEFAULT_NAME, style = MaterialTheme.typography.titleMedium)
                }
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
        }
        if (seedApplies) {
            Spacer(Modifier.height(12.dp))
            Panel {
                Row(
                    Modifier.fillMaxWidth().clickable { seed = !seed }.padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(checked = seed, onCheckedChange = { seed = it })
                    Spacer(Modifier.width(4.dp))
                    Column(Modifier.weight(1f).padding(end = 12.dp)) {
                        Text("Add the free inspection", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "${Fmt.date(CarRepository.SEED_INSPECTION_DATE)} · about ${Fmt.km(CarRepository.SEED_INSPECTION_KM)}. Doesn't count as Service 1.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permission.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    finish()
                }
            },
            enabled = kmValue != null,
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) { Text("Get started") }
        Spacer(Modifier.height(8.dp))
        Text(
            "You'll be asked to allow notifications for service reminders.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
        )
        Spacer(Modifier.height(16.dp))
    }
}
