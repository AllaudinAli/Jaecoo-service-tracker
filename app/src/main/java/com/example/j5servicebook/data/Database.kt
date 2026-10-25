package com.example.j5servicebook.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Upsert
import com.example.j5servicebook.domain.FittedPart
import com.example.j5servicebook.domain.ItemId
import com.example.j5servicebook.domain.ReadingSource
import com.example.j5servicebook.domain.ServiceKind
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.time.LocalDate

@Entity(tableName = "car_profile")
data class CarProfileEntity(
    @PrimaryKey val id: Long = PROFILE_ID,
    val name: String,
    val purchaseDate: LocalDate,
    val createdAt: Long,
)

@Entity(tableName = "odometer_reading")
data class OdometerReadingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val km: Int,
    val source: ReadingSource,
)

@Entity(tableName = "service_record")
data class ServiceRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val km: Int,
    val kind: ServiceKind,
    val itemsDone: Set<ItemId>,
    val workshop: String?,
    val cost: Int?,
    val jobCardNo: String?,
    val notes: String?,
    val fittedParts: List<FittedPart>,
)

const val PROFILE_ID = 1L

class Converters {
    // Explicit type: KSP can't resolve plugin-generated serializers through inference.
    private val partsSerializer: KSerializer<List<FittedPart>> = ListSerializer(FittedPart.serializer())

    @TypeConverter fun dateToLong(d: LocalDate): Long = d.toEpochDay()
    @TypeConverter fun longToDate(v: Long): LocalDate = LocalDate.ofEpochDay(v)

    @TypeConverter fun itemsToText(items: Set<ItemId>): String = items.joinToString(",") { it.name }

    @TypeConverter
    fun textToItems(text: String): Set<ItemId> = text.split(',')
        .mapNotNull { name -> ItemId.entries.firstOrNull { it.name == name } }
        .toSet()

    @TypeConverter fun partsToText(parts: List<FittedPart>): String = Json.encodeToString(partsSerializer, parts)

    @TypeConverter
    fun textToParts(text: String): List<FittedPart> =
        runCatching { Json.decodeFromString(partsSerializer, text) }.getOrDefault(emptyList())
}

@Dao
abstract class CarDao {
    @Query("SELECT * FROM car_profile WHERE id = $PROFILE_ID")
    abstract fun profile(): Flow<CarProfileEntity?>

    @Query("SELECT * FROM odometer_reading ORDER BY date, km")
    abstract fun readings(): Flow<List<OdometerReadingEntity>>

    @Query("SELECT * FROM service_record ORDER BY date DESC, km DESC, id DESC")
    abstract fun records(): Flow<List<ServiceRecordEntity>>

    @Query("SELECT * FROM car_profile WHERE id = $PROFILE_ID")
    abstract suspend fun profileNow(): CarProfileEntity?

    @Query("SELECT * FROM odometer_reading ORDER BY date, km")
    abstract suspend fun readingsNow(): List<OdometerReadingEntity>

    @Query("SELECT * FROM service_record ORDER BY date DESC, km DESC, id DESC")
    abstract suspend fun recordsNow(): List<ServiceRecordEntity>

    @Upsert abstract suspend fun upsertProfile(profile: CarProfileEntity)
    @Insert abstract suspend fun insertReading(reading: OdometerReadingEntity): Long
    @Insert abstract suspend fun insertReadings(readings: List<OdometerReadingEntity>)
    @Upsert abstract suspend fun upsertRecord(record: ServiceRecordEntity): Long
    @Insert abstract suspend fun insertRecords(records: List<ServiceRecordEntity>)
    @Delete abstract suspend fun deleteRecord(record: ServiceRecordEntity)

    @Query("DELETE FROM car_profile") abstract suspend fun clearProfile()
    @Query("DELETE FROM odometer_reading") abstract suspend fun clearReadings()
    @Query("DELETE FROM service_record") abstract suspend fun clearRecords()

    @Transaction
    open suspend fun saveRecordWithReading(record: ServiceRecordEntity, reading: OdometerReadingEntity?) {
        upsertRecord(record)
        reading?.let { insertReading(it) }
    }

    @Transaction
    open suspend fun replaceAll(
        profile: CarProfileEntity,
        readings: List<OdometerReadingEntity>,
        records: List<ServiceRecordEntity>,
    ) {
        clearRecords()
        clearReadings()
        clearProfile()
        upsertProfile(profile)
        insertReadings(readings)
        insertRecords(records)
    }
}

@Database(
    entities = [CarProfileEntity::class, OdometerReadingEntity::class, ServiceRecordEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun carDao(): CarDao
}
