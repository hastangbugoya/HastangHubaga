package com.example.hastanghubaga.domain.usecase.nutrition

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

/**
 * Reads the current AK nutrition-goals snapshot from the shared content provider
 * and maps it into an HH-side external/imported model.
 *
 * Why this exists:
 * - keeps AK transport parsing out of UI code
 * - gives HH a stable, comparison-ready foundation before any import UI exists
 * - preserves AK source metadata and min/target/max semantics without
 *   prematurely flattening into HH persistence
 *
 * Important boundaries:
 * - this use case DOES read AK external state
 * - this use case does NOT persist anything into HH
 * - this use case does NOT create/update HH NutritionPlan rows
 * - this use case does NOT normalize imported goals into HH canonical plan rows yet
 *
 * Intended later flow:
 * - read AK snapshot here
 * - compare against HH-local plans/goals elsewhere
 * - allow user to explicitly create a new HH goal or copy selected values into
 *   an existing HH goal
 *
 * Future AI/dev note:
 * - Keep this as a source adapter / transport parser
 * - Do not add Room writes here
 * - If AK schemaVersion changes, extend parsing carefully and preserve backward
 *   compatibility where possible
 * - Macro canonical keys should stay aligned with HH/AK canonical nutrient space
 */
class ReadAkNutritionGoalsUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {

    sealed interface Result {
        data class Success(
            val snapshot: AkNutritionGoalsSnapshot
        ) : Result

        data class Error(
            val message: String
        ) : Result
    }

    suspend operator fun invoke(): Result {
        return try {
            val jsonString = readAkNutritionGoalsJson()
                ?: return Result.Error("Could not read AdobongKangkong nutrition goals provider.")

            Result.Success(
                snapshot = parseSnapshot(jsonString)
            )
        } catch (t: Throwable) {
            Result.Error(t.message ?: "Failed to read AK nutrition goals.")
        }
    }

    private fun readAkNutritionGoalsJson(): String? {
        val uri = Uri.parse("content://com.example.adobongkangkong.shared/goals/current")
        return context.contentResolver
            .openInputStream(uri)
            ?.bufferedReader()
            ?.use { it.readText() }
    }

    private fun parseSnapshot(jsonString: String): AkNutritionGoalsSnapshot {
        val root = Json.parseToJsonElement(jsonString).jsonObject

        val macrosObject = root["macros"]?.jsonObject ?: JsonObject(emptyMap())
        val nutrientsArray = root["nutrients"]?.jsonArray ?: JsonArray(emptyList())

        return AkNutritionGoalsSnapshot(
            schemaVersion = root.requiredInt("schemaVersion"),
            exportedAtEpochMs = root.requiredLong("exportedAtEpochMs"),
            source = root.requiredString("source"),
            macros = parseMacros(macrosObject),
            nutrients = parseNutrients(nutrientsArray)
        )
    }

    private fun parseMacros(macrosObject: JsonObject): List<AkImportedGoalValue> {
        return macrosObject.entries.map { (rawKey, rawValue) ->
            val metadata = macroMetadataFor(rawKey)
            val bounds = rawValue.jsonObject.toBounds()

            AkImportedGoalValue(
                sourceKind = AkImportedGoalSourceKind.MACRO,
                sourceKey = rawKey,
                canonicalKey = metadata.canonicalKey,
                displayName = metadata.displayName,
                unit = metadata.unit,
                minValue = bounds.minValue,
                targetValue = bounds.targetValue,
                maxValue = bounds.maxValue
            )
        }
    }

    private fun parseNutrients(nutrientsArray: JsonArray): List<AkImportedGoalValue> {
        return nutrientsArray.map { element ->
            val obj = element.jsonObject
            val bounds = obj.toBounds()

            AkImportedGoalValue(
                sourceKind = AkImportedGoalSourceKind.NUTRIENT,
                sourceKey = obj.requiredString("code"),
                canonicalKey = obj.requiredString("code"),
                displayName = obj.requiredString("name"),
                unit = obj.requiredString("unit"),
                minValue = bounds.minValue,
                targetValue = bounds.targetValue,
                maxValue = bounds.maxValue
            )
        }
    }

    private fun JsonObject.toBounds(): GoalBounds {
        return GoalBounds(
            minValue = optionalDouble("min"),
            targetValue = optionalDouble("target"),
            maxValue = optionalDouble("max")
        )
    }

    private fun macroMetadataFor(rawKey: String): MacroMetadata {
        return when (rawKey) {
            "calories" -> MacroMetadata(
                canonicalKey = "CALORIES_KCAL",
                displayName = "Calories",
                unit = "kcal"
            )

            "protein" -> MacroMetadata(
                canonicalKey = "PROTEIN_G",
                displayName = "Protein",
                unit = "g"
            )

            "carbs" -> MacroMetadata(
                canonicalKey = "CARBS_G",
                displayName = "Carbs",
                unit = "g"
            )

            "fat" -> MacroMetadata(
                canonicalKey = "FAT_G",
                displayName = "Fat",
                unit = "g"
            )

            else -> MacroMetadata(
                canonicalKey = rawKey.uppercase(),
                displayName = rawKey,
                unit = null
            )
        }
    }

    private fun JsonObject.requiredString(key: String): String =
        this[key]?.jsonPrimitive?.content
            ?: throw IllegalArgumentException("Missing required field: $key")

    private fun JsonObject.requiredLong(key: String): Long =
        this[key]?.jsonPrimitive?.longOrNull
            ?: throw IllegalArgumentException("Missing or invalid required field: $key")

    private fun JsonObject.requiredInt(key: String): Int =
        this[key]?.jsonPrimitive?.intOrNull
            ?: throw IllegalArgumentException("Missing or invalid required field: $key")

    private fun JsonObject.optionalDouble(key: String): Double? =
        this[key]?.jsonPrimitive?.doubleOrNull

    private data class GoalBounds(
        val minValue: Double?,
        val targetValue: Double?,
        val maxValue: Double?
    )

    private data class MacroMetadata(
        val canonicalKey: String,
        val displayName: String,
        val unit: String?
    )
}

/**
 * HH-side external snapshot of the current AK active nutrition goal profile.
 *
 * This is intentionally NOT an HH persistence entity and NOT an HH plan domain
 * object yet.
 *
 * It preserves the imported snapshot as external source state so later layers
 * can:
 * - compare AK vs HH
 * - show side-by-side differences
 * - selectively import into a new or existing HH goal
 */
data class AkNutritionGoalsSnapshot(
    val schemaVersion: Int,
    val exportedAtEpochMs: Long,
    val source: String,
    val macros: List<AkImportedGoalValue>,
    val nutrients: List<AkImportedGoalValue>
)

/**
 * Represents one imported goal dimension from AK.
 *
 * Notes:
 * - [sourceKey] preserves the AK-side identity exactly as received
 * - [canonicalKey] is HH's comparison-friendly key for later mapping work
 * - [minValue], [targetValue], and [maxValue] preserve AK semantics faithfully
 *
 * For macros:
 * - sourceKey may be "calories", "protein", "carbs", "fat"
 * - canonicalKey is mapped into HH/AK canonical nutrient-key space
 *
 * For nutrients:
 * - sourceKey and canonicalKey are currently both the AK nutrient code
 */
data class AkImportedGoalValue(
    val sourceKind: AkImportedGoalSourceKind,
    val sourceKey: String,
    val canonicalKey: String,
    val displayName: String,
    val unit: String?,
    val minValue: Double? = null,
    val targetValue: Double? = null,
    val maxValue: Double? = null
)

/**
 * Distinguishes whether an imported row came from the AK "macros" object or the
 * AK "nutrients" array.
 */
enum class AkImportedGoalSourceKind {
    MACRO,
    NUTRIENT
}