package com.example.hastanghubaga.data.local.converters

import androidx.room.TypeConverter
import com.example.hastanghubaga.data.local.entity.supplement.DoseAnchorType
import com.example.hastanghubaga.data.local.entity.supplement.FrequencyType
import com.example.hastanghubaga.data.local.entity.supplement.IngredientUnit
import com.example.hastanghubaga.data.local.entity.supplement.SupplementDoseUnit
import com.example.hastanghubaga.domain.model.activity.ActivityType
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class Converters {
    @TypeConverter
    fun fromDayOfWeekList(days: List<DayOfWeek>?): String? =
        days?.joinToString(",") { it.name }

    @TypeConverter
    fun toDayOfWeekList(data: String?): List<DayOfWeek>? =
        data?.takeIf { it.isNotBlank() }?.split(",")?.map { DayOfWeek.valueOf(it) }

    @TypeConverter
    fun fromFrequencyType(type: FrequencyType): String = type.name

    @TypeConverter
    fun toFrequencyType(value: String): FrequencyType =
        FrequencyType.valueOf(value)

    @TypeConverter
    fun fromUnit(unit: IngredientUnit?): String? = unit?.name

    @TypeConverter
    fun toUnit(value: String?): IngredientUnit? =
        value?.let { IngredientUnit.valueOf(it) }

    // -------------------------
    // SupplementDoseUnit converters
    // -------------------------
    @TypeConverter
    fun fromDoseUnit(unit: SupplementDoseUnit?): String? = unit?.name

    @TypeConverter
    fun toDoseUnit(value: String?): SupplementDoseUnit? =
        value?.let { SupplementDoseUnit.valueOf(it) }

    @TypeConverter
    fun fromLocalTime(time: LocalTime?): Int? =
        time?.toSecondOfDay()

    @TypeConverter
    fun toLocalTime(seconds: Int?): LocalTime? =
        seconds?.let { LocalTime.ofSecondOfDay(it.toLong()) }

    @TypeConverter
    fun toString(type: DoseAnchorType): String = type.name

    @TypeConverter
    fun fromString(value: String): DoseAnchorType =
        DoseAnchorType.valueOf(value)

    @TypeConverter
    fun fromDayOfWeek(day: DayOfWeek?): String? = day?.name

    @TypeConverter
    fun toDayOfWeek(value: String?): DayOfWeek? =
        value?.let { DayOfWeek.valueOf(it) }

    @TypeConverter
    fun fromActivityType(type: ActivityType?): String? =
        type?.name

    @TypeConverter
    fun toActivityType(value: String?): ActivityType? =
        value?.let { ActivityType.valueOf(it) }

    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): Long? =
        value?.atZone(ZoneId.systemDefault())
            ?.toInstant()
            ?.toEpochMilli()

    @TypeConverter
    fun toLocalDateTime(value: Long?): LocalDateTime? =
        value?.let {
            Instant.ofEpochMilli(it)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
        }
}