package com.example.j5servicebook.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.j5servicebook.J5App
import com.example.j5servicebook.data.CarRepository
import com.example.j5servicebook.data.Settings
import com.example.j5servicebook.data.SettingsStore
import com.example.j5servicebook.domain.Backup
import com.example.j5servicebook.domain.BackupCodec
import com.example.j5servicebook.domain.CarProfile
import com.example.j5servicebook.domain.IntervalDue
import com.example.j5servicebook.domain.Odometer
import com.example.j5servicebook.domain.PeriodicService
import com.example.j5servicebook.domain.Reading
import com.example.j5servicebook.domain.ScheduleEngine
import com.example.j5servicebook.domain.ServiceRecord
import com.example.j5servicebook.domain.VisitItem
import com.example.j5servicebook.domain.WarrantyCategory
import com.example.j5servicebook.domain.WarrantyState
import com.example.j5servicebook.reminders.ReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime

/** Everything the screens show, computed once per data change. */
data class Dashboard(
    val today: LocalDate,
    val profile: CarProfile,
    val readings: List<Reading>,
    /** Newest first. */
    val records: List<ServiceRecord>,
    val engine: ScheduleEngine,
    val latest: Odometer,
    val next: PeriodicService,
    val upcoming: List<PeriodicService>,
    val intervals: List<IntervalDue>,
    val visit: List<VisitItem>,
    val mainWarranties: List<WarrantyState>,
    val specialWarranties: List<WarrantyState>,
    val sparePartWarranties: List<WarrantyState>,
) {
    companion object {
        fun build(profile: CarProfile, readings: List<Reading>, records: List<ServiceRecord>, today: LocalDate): Dashboard {
            val engine = ScheduleEngine(profile.purchaseDate, readings, records)
            val next = engine.nextService(today)
            val warranties = engine.warranties(today)
            return Dashboard(
                today = today,
                profile = profile,
                readings = readings,
                records = records,
                engine = engine,
                latest = engine.latest,
                next = next,
                upcoming = engine.upcoming(today),
                intervals = engine.intervalItems(today).sortedBy { it.effectiveDate },
                visit = engine.visitPlan(next, today),
                mainWarranties = warranties.filter { it.category == WarrantyCategory.MAIN },
                specialWarranties = warranties.filter { it.category == WarrantyCategory.SPECIAL },
                sparePartWarranties = engine.sparePartWarranties(today).sortedByDescending { it.startDate },
            )
        }
    }
}

sealed interface UiState {
    data object Loading : UiState
    data object NeedsSetup : UiState
    data class Ready(val dashboard: Dashboard) : UiState
}

class MainViewModel(
    private val app: Application,
    private val repository: CarRepository,
    private val settingsStore: SettingsStore,
) : ViewModel() {
    private val today = MutableStateFlow(LocalDate.now())

    val state: StateFlow<UiState> =
        combine(repository.profile, repository.readings, repository.records, today) { profile, readings, records, day ->
            if (profile == null) UiState.NeedsSetup else UiState.Ready(Dashboard.build(profile, readings, records, day))
        }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    val settings: StateFlow<Settings> =
        settingsStore.settings.stateIn(viewModelScope, SharingStarted.Eagerly, Settings())

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages = _messages.receiveAsFlow()

    fun refreshToday() {
        today.value = LocalDate.now()
    }

    fun completeSetup(name: String, purchaseDate: LocalDate, km: Int, seedInspection: Boolean) = viewModelScope.launch {
        settingsStore.setOwnerName(name)
        repository.setup(CarRepository.DEFAULT_NAME, purchaseDate, km, LocalDate.now(), seedInspection)
        ReminderScheduler.schedule(app)
    }

    fun updateOdometer(km: Int) = viewModelScope.launch {
        repository.addReading(km, LocalDate.now())
        _messages.send("Odometer updated")
    }

    fun saveRecord(record: ServiceRecord, latestKm: Int) = viewModelScope.launch {
        repository.saveRecord(record, latestKm)
        _messages.send(if (record.id == 0L) "Service logged" else "Service updated")
    }

    fun deleteRecord(record: ServiceRecord) = viewModelScope.launch { repository.deleteRecord(record) }

    /** Puts a deleted record back with its original id. */
    fun restoreRecord(record: ServiceRecord) = viewModelScope.launch { repository.saveRecord(record, Int.MAX_VALUE) }

    fun setLeadDays(days: Int) = viewModelScope.launch { settingsStore.setLeadDays(days) }

    fun setWallpaperColors(on: Boolean) = viewModelScope.launch { settingsStore.setWallpaperColors(on) }

    fun setOwnerName(name: String) = viewModelScope.launch { settingsStore.setOwnerName(name) }

    fun runReminderCheck() {
        ReminderScheduler.runNow(app)
        viewModelScope.launch { _messages.send("Reminder check started") }
    }

    fun exportTo(uri: Uri) = viewModelScope.launch {
        val result = runCatching {
            val data = requireNotNull(repository.snapshot())
            val text = BackupCodec.encode(
                Backup(
                    exportedAt = LocalDateTime.now().withNano(0).toString(),
                    profile = data.profile,
                    readings = data.readings,
                    records = data.records,
                    leadDays = settings.value.leadDays,
                    ownerName = settings.value.ownerName.ifBlank { null },
                ),
            )
            withContext(Dispatchers.IO) {
                requireNotNull(app.contentResolver.openOutputStream(uri, "wt")).use { it.write(text.toByteArray()) }
            }
        }
        _messages.send(if (result.isSuccess) "Backup saved" else "Couldn't save the backup")
    }

    fun importFrom(uri: Uri) = viewModelScope.launch {
        val result = runCatching {
            val text = withContext(Dispatchers.IO) {
                requireNotNull(app.contentResolver.openInputStream(uri)).use { it.readBytes().decodeToString() }
            }
            val backup = BackupCodec.decode(text)
            repository.restore(backup)
            backup.leadDays?.let { settingsStore.setLeadDays(it) }
            backup.ownerName?.let { settingsStore.setOwnerName(it) }
            backup
        }
        _messages.send(
            result.fold(
                onSuccess = { "Restored ${it.records.size} services and ${it.readings.size} readings" },
                onFailure = { "That file isn't a J5 Service Book backup" },
            ),
        )
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as J5App
                MainViewModel(app, app.container.repository, app.container.settings)
            }
        }
    }
}
