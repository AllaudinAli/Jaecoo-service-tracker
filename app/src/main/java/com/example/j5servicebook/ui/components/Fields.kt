package com.example.j5servicebook.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import com.example.j5servicebook.domain.Fmt
import com.example.j5servicebook.ui.theme.Accent
import com.example.j5servicebook.ui.theme.OnAccent
import java.time.LocalDate

private const val DAY_MS = 86_400_000L

/** Filled grey field, label inside, no underline. */
@Composable
fun softFieldColors(): TextFieldColors {
    val c = MaterialTheme.colorScheme
    return TextFieldDefaults.colors(
        focusedContainerColor = c.surfaceContainerHigh,
        unfocusedContainerColor = c.surfaceContainer,
        disabledContainerColor = c.surfaceContainer,
        errorContainerColor = c.surfaceContainer,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
        errorIndicatorColor = Color.Transparent,
        focusedLabelColor = c.onSurfaceVariant,
        unfocusedLabelColor = c.onSurfaceVariant,
        cursorColor = c.onSurface,
    )
}

/** Read-only field that opens a date picker. Future dates can't be picked. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateField(label: String, value: LocalDate, onChange: (LocalDate) -> Unit, modifier: Modifier = Modifier) {
    var open by rememberSaveable { mutableStateOf(false) }
    val interaction = remember { MutableInteractionSource() }
    LaunchedEffect(interaction) {
        interaction.interactions.collect { if (it is PressInteraction.Release) open = true }
    }
    TextField(
        value = Fmt.date(value),
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        trailingIcon = { Icon(Icons.Rounded.CalendarMonth, contentDescription = null) },
        interactionSource = interaction,
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        colors = softFieldColors(),
        modifier = modifier.fillMaxWidth(),
    )
    if (open) {
        val todayMs = LocalDate.now().toEpochDay() * DAY_MS
        val state = rememberDatePickerState(
            initialSelectedDateMillis = value.toEpochDay() * DAY_MS,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= todayMs
            },
        )
        val c = MaterialTheme.colorScheme
        DatePickerDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { onChange(LocalDate.ofEpochDay(it / DAY_MS)) }
                    open = false
                }) { Text("Done", color = c.onSurface) }
            },
            dismissButton = { TextButton(onClick = { open = false }) { Text("Cancel", color = c.onSurfaceVariant) } },
            colors = DatePickerDefaults.colors(containerColor = c.background),
        ) {
            DatePicker(
                state = state,
                showModeToggle = false,
                colors = DatePickerDefaults.colors(
                    containerColor = c.background,
                    selectedDayContainerColor = Accent,
                    selectedDayContentColor = OnAccent,
                    todayDateBorderColor = c.onSurface,
                    todayContentColor = c.onSurface,
                    selectedYearContainerColor = Accent,
                    selectedYearContentColor = OnAccent,
                ),
            )
        }
    }
}

/** Digits-only field for km and PKR amounts. */
@Composable
fun NumberField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    suffix: String? = null,
    error: String? = null,
    supporting: String? = null,
    imeAction: ImeAction = ImeAction.Next,
) {
    TextField(
        value = value,
        onValueChange = { text -> onChange(text.filter(Char::isDigit).take(7)) },
        label = { Text(label) },
        suffix = suffix?.let { { Text(it) } },
        isError = error != null,
        supportingText = (error ?: supporting)?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = imeAction),
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        colors = softFieldColors(),
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
fun PlainField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    capitalizeWords: Boolean = false,
) {
    TextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        keyboardOptions = KeyboardOptions(
            capitalization = if (capitalizeWords) KeyboardCapitalization.Words else KeyboardCapitalization.Sentences,
            imeAction = if (singleLine) ImeAction.Next else ImeAction.Default,
        ),
        shape = MaterialTheme.shapes.medium,
        colors = softFieldColors(),
        modifier = modifier.fillMaxWidth(),
    )
}
