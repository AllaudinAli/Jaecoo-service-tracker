package com.example.j5servicebook.data

import com.example.j5servicebook.domain.Backup
import com.example.j5servicebook.domain.CarProfile
import com.example.j5servicebook.domain.Reading
import com.example.j5servicebook.domain.ReadingSource
import com.example.j5servicebook.domain.ServiceKind
import com.example.j5servicebook.domain.ServiceRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

data class CarData(
    val profile: CarProfile,
    val readings: List<Reading>,
    /** Newest first. */
    val records: List<ServiceRecord>,
)

class CarRepository(private val dao: CarDao) {
    val profile: Flow<CarProfile?> = dao.profile().map { it?.toDomain() }
    val readings: Flow<List<Reading>> = dao.readings().map { list -> list.map { it.toDomain() } }
    val records: Flow<List<ServiceRecord>> = dao.records().map { list -> list.map { it.toDomain() } }

    suspend fun snapshot(): CarData? {
        val profile = dao.profileNow()?.toDomain() ?: return null
        return CarData(profile, dao.readingsNow().map { it.toDomain() }, dao.recordsNow().map { it.toDomain() })
    }

    suspend fun setup(name: String, purchaseDate: LocalDate, km: Int, today: LocalDate, seedInspection: Boolean) {
        val records = if (seedInspection) {
            listOf(
                ServiceRecord(
                    date = SEED_INSPECTION_DATE,
                    km = SEED_INSPECTION_KM,
                    kind = ServiceKind.INSPECTION,
                    workshop = "Authorised dealer",
                    notes = "Free 1 month / 1,000 km inspection. Date and km approximate.",
                ).toEntity(),
            )
        } else {
            emptyList()
        }
        val readings = if (km > 0) listOf(OdometerReadingEntity(date = today, km = km, source = ReadingSource.MANUAL)) else emptyList()
        dao.replaceAll(CarProfileEntity(name = name, purchaseDate = purchaseDate, createdAt = System.currentTimeMillis()), readings, records)
    }

    suspend fun addReading(km: Int, date: LocalDate) {
        dao.insertReading(OdometerReadingEntity(date = date, km = km, source = ReadingSource.MANUAL))
    }

    /** Saves the record, and adds an odometer reading when it moves the odometer forward. */
    suspend fun saveRecord(record: ServiceRecord, latestKm: Int) {
        val reading = if (record.km > latestKm) {
            OdometerReadingEntity(date = record.date, km = record.km, source = ReadingSource.SERVICE)
        } else {
            null
        }
        dao.saveRecordWithReading(record.toEntity(), reading)
    }

    suspend fun deleteRecord(record: ServiceRecord) = dao.deleteRecord(record.toEntity())

    suspend fun restore(backup: Backup) {
        dao.replaceAll(
            CarProfileEntity(name = backup.profile.name, purchaseDate = backup.profile.purchaseDate, createdAt = backup.profile.createdAt),
            backup.readings.map { OdometerReadingEntity(it.id, it.date, it.km, it.source) },
            backup.records.map { it.toEntity() },
        )
    }

    companion object {
        const val DEFAULT_NAME = "Jaecoo J5 SHS"
        val DEFAULT_PURCHASE: LocalDate = LocalDate.of(2026, 8, 20)
        const val DEFAULT_KM = 2_000
        val SEED_INSPECTION_DATE: LocalDate = LocalDate.of(2026, 9, 21)
        const val SEED_INSPECTION_KM = 1_000
    }
}

private fun CarProfileEntity.toDomain() = CarProfile(name, purchaseDate, createdAt)
private fun OdometerReadingEntity.toDomain() = Reading(date, km, source, id)

private fun ServiceRecordEntity.toDomain() =
    ServiceRecord(id, date, km, kind, itemsDone, workshop, cost, jobCardNo, notes, fittedParts)

private fun ServiceRecord.toEntity() =
    ServiceRecordEntity(id, date, km, kind, itemsDone, workshop, cost, jobCardNo, notes, fittedParts)
