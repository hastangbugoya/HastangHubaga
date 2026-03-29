package com.example.hastanghubaga.data.local.converters

import androidx.room.TypeConverter
import com.example.hastanghubaga.data.local.entity.supplement.DoseAnchorType
import com.example.hastanghubaga.data.local.entity.supplement.FrequencyType
import com.example.hastanghubaga.data.local.entity.supplement.IngredientUnit
import com.example.hastanghubaga.data.local.entity.supplement.ScheduleRecurrenceType
import com.example.hastanghubaga.data.local.entity.supplement.ScheduleTimingType
import com.example.hastanghubaga.data.local.entity.supplement.SupplementDoseUnit
import com.example.hastanghubaga.data.local.entity.user.ScheduleTypeEntity
import com.example.hastanghubaga.domain.model.activity.ActivityType
import com.example.hastanghubaga.domain.schedule.model.TimeAnchor
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.datetime.DayOfWeek as KtxDayOfWeek
import kotlinx.datetime.LocalDate as KtxLocalDate
import kotlinx.datetime.LocalTime as KtxLocalTime

class Converters {

    // -------------------------
    // Legacy java.time DayOfWeek list converters
    // -------------------------
    @TypeConverter
    fun fromDayOfWeekList(days: List<DayOfWeek>?): String? =
        days?.joinToString(",") { it.name }

    @TypeConverter
    fun toDayOfWeekList(data: String?): List<DayOfWeek>? =
        data?.takeIf { it.isNotBlank() }?.split(",")?.map { DayOfWeek.valueOf(it) }

    // -------------------------
    // Legacy supplement converters
    // -------------------------
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

    // -------------------------
    // Activity converters
    // -------------------------
    @TypeConverter
    fun fromActivityType(type: ActivityType?): String? =
        type?.name

    @TypeConverter
    fun toActivityType(value: String?): ActivityType? =
        value?.let { ActivityType.valueOf(it) }

    // -------------------------
    // LocalDateTime converters
    // -------------------------
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

    // -------------------------
    // Existing schedule type converters
    // -------------------------
    @TypeConverter
    fun scheduleTypeToString(type: ScheduleTypeEntity): String = type.name

    @TypeConverter
    fun stringToScheduleType(value: String): ScheduleTypeEntity =
        ScheduleTypeEntity.valueOf(value)

    // -------------------------
    // New shared scheduler converters (kotlinx.datetime)
    // -------------------------
    @TypeConverter
    fun fromKtxLocalDate(value: KtxLocalDate?): String? =
        value?.toString()

    @TypeConverter
    fun toKtxLocalDate(value: String?): KtxLocalDate? =
        value?.takeIf { it.isNotBlank() }?.let(KtxLocalDate::parse)

    @TypeConverter
    fun fromKtxLocalTime(value: KtxLocalTime?): Int? =
        value?.let { (it.hour * 3600) + (it.minute * 60) + it.second }

    @TypeConverter
    fun toKtxLocalTime(value: Int?): KtxLocalTime? =
        value?.let { totalSeconds ->
            val hour = totalSeconds / 3600
            val minute = (totalSeconds % 3600) / 60
            val second = totalSeconds % 60
            KtxLocalTime(hour = hour, minute = minute, second = second)
        }

    @TypeConverter
    fun fromKtxDayOfWeek(value: KtxDayOfWeek?): String? =
        value?.name

    @TypeConverter
    fun toKtxDayOfWeek(value: String?): KtxDayOfWeek? =
        value?.takeIf { it.isNotBlank() }?.let(KtxDayOfWeek::valueOf)

    @TypeConverter
    fun fromKtxDayOfWeekList(value: List<KtxDayOfWeek>?): String? =
        value?.joinToString(",") { it.name }

    @TypeConverter
    fun toKtxDayOfWeekList(value: String?): List<KtxDayOfWeek>? =
        value
            ?.takeIf { it.isNotBlank() }
            ?.split(",")
            ?.map(KtxDayOfWeek::valueOf)

    @TypeConverter
    fun fromScheduleRecurrenceType(value: ScheduleRecurrenceType): String =
        value.name

    @TypeConverter
    fun toScheduleRecurrenceType(value: String): ScheduleRecurrenceType =
        ScheduleRecurrenceType.valueOf(value)

    @TypeConverter
    fun fromScheduleTimingType(value: ScheduleTimingType): String =
        value.name

    @TypeConverter
    fun toScheduleTimingType(value: String): ScheduleTimingType =
        ScheduleTimingType.valueOf(value)

    @TypeConverter
    fun fromTimeAnchor(value: TimeAnchor?): String? =
        value?.name

    @TypeConverter
    fun toTimeAnchor(value: String?): TimeAnchor? =
        value?.takeIf { it.isNotBlank() }?.let(TimeAnchor::valueOf)
}