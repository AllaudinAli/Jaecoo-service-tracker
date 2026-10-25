package com.example.j5servicebook.domain

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Backup(
    val format: String = FORMAT,
    val version: Int = 1,
    val exportedAt: String,
    val profile: CarProfile,
    val readings: List<Reading>,
    val records: List<ServiceRecord>,
    val leadDays: Int? = null,
    val ownerName: String? = null,
) {
    companion object {
        const val FORMAT = "j5-service-book"
    }
}

object BackupCodec {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun encode(backup: Backup): String = json.encodeToString(Backup.serializer(), backup)

    /** Throws [IllegalArgumentException] if the text is not a J5 Service Book backup. */
    fun decode(text: String): Backup {
        val backup = runCatching { json.decodeFromString(Backup.serializer(), text) }
            .getOrElse { throw IllegalArgumentException("Not a valid backup file", it) }
        require(backup.format == Backup.FORMAT) { "Not a J5 Service Book backup" }
        return backup
    }
}
