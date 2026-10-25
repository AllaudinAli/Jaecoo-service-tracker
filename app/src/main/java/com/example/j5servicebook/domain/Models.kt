@file:UseSerializers(LocalDateSerializer::class)

package com.example.j5servicebook.domain

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDate

@Serializable
data class CarProfile(
    val name: String,
    val purchaseDate: LocalDate,
    val createdAt: Long = 0,
)

enum class ReadingSource { MANUAL, SERVICE }

@Serializable
data class Reading(
    val date: LocalDate,
    val km: Int,
    val source: ReadingSource = ReadingSource.MANUAL,
    val id: Long = 0,
)

enum class ServiceKind(val label: String) {
    PERIODIC("Periodic"),
    INSPECTION("Inspection"),
    REPAIR("Repair"),
    OTHER("Other"),
}

@Serializable
data class FittedPart(val name: String, val kind: SparePartKind)

@Serializable
data class ServiceRecord(
    val id: Long = 0,
    val date: LocalDate,
    val km: Int,
    val kind: ServiceKind,
    val itemsDone: Set<ItemId> = emptySet(),
    val workshop: String? = null,
    val cost: Int? = null,
    val jobCardNo: String? = null,
    val notes: String? = null,
    val fittedParts: List<FittedPart> = emptyList(),
)

object LocalDateSerializer : KSerializer<LocalDate> {
    override val descriptor = PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: LocalDate) = encoder.encodeString(value.toString())
    override fun deserialize(decoder: Decoder): LocalDate = LocalDate.parse(decoder.decodeString())
}
