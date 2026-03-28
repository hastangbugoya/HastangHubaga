package com.example.hastanghubaga.domain.usecase.meal

import android.content.Context
import android.net.Uri
import com.example.hastanghubaga.data.local.dao.meal.AkImportedLogDao
import com.example.hastanghubaga.data.local.entity.meal.AkImportedLogEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

class ImportAkLogsUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val akImportedLogDao: AkImportedLogDao
) {

    sealed interface Result {
        data class Success(
            val importedCount: Int,
            val changedCount: Int,
            val affectedDateIsos: Set<String>
        ) : Result

        data class Error(val message: String) : Result
    }

    suspend operator fun invoke(force: Boolean = false): Result {
        return try {
            val jsonString = readAkLogsJson()
                ?: return Result.Error("Could not read AdobongKangkong logs provider.")

            val parsedLogs = parseLogs(jsonString)

            if (force) {
                akImportedLogDao.insertOrReplaceAll(parsedLogs)
                Result.Success(
                    importedCount = parsedLogs.size,
                    changedCount = parsedLogs.size,
                    affectedDateIsos = parsedLogs.map { it.logDateIso }.toSet()
                )
            } else {
                var changedCount = 0
                val affectedDateIsos = linkedSetOf<String>()

                parsedLogs.forEach { log ->
                    val changed = akImportedLogDao.upsertIfNewer(log)
                    if (changed) {
                        changedCount++
                        affectedDateIsos += log.logDateIso
                    }
                }

                Result.Success(
                    importedCount = parsedLogs.size,
                    changedCount = changedCount,
                    affectedDateIsos = affectedDateIsos
                )
            }
        } catch (t: Throwable) {
            Result.Error(t.message ?: "Import failed.")
        }
    }

    private fun readAkLogsJson(): String? {
        val uri = Uri.parse("content://com.example.adobongkangkong.shared/logs")
        return context.contentResolver
            .openInputStream(uri)
            ?.bufferedReader()
            ?.use { it.readText() }
    }

    private fun parseLogs(jsonString: String): List<AkImportedLogEntity> {
        val root = Json.parseToJsonElement(jsonString).jsonObject
        val logs = root["logs"]?.jsonArray ?: JsonArray(emptyList())

        return logs.map { element ->
            val obj = element.jsonObject
            AkImportedLogEntity(
                stableId = obj.requiredString("stableId"),
                modifiedAt = obj.requiredLong("modifiedAt"),
                timestamp = obj.requiredLong("timestamp"),
                logDateIso = obj.requiredString("logDateIso"),
                mealSlot = obj.optionalString("mealSlot"),
                itemName = obj.requiredString("itemName"),
                nutrientsJson = obj.requiredString("nutrientsJson")
            )
        }
    }

    private fun JsonObject.requiredString(key: String): String =
        this[key]?.jsonPrimitive?.content
            ?: throw IllegalArgumentException("Missing required field: $key")

    private fun JsonObject.optionalString(key: String): String? =
        this[key]?.jsonPrimitive?.contentOrNull

    private fun JsonObject.requiredLong(key: String): Long =
        this[key]?.jsonPrimitive?.longOrNull
            ?: throw IllegalArgumentException("Missing or invalid required field: $key")
}