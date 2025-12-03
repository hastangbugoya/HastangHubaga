package com.example.hastanghubaga.data.local.db

import androidx.room.TypeConverter
import com.example.hastanghubaga.data.local.entity.supplement.FrequencyType
import java.time.DayOfWeek

class Converters {

    @TypeConverter
    fun fromDayOfWeekList(days: List<DayOfWeek>?): String? =
        days?.joinToString(",") { it.name }

    @TypeConverter
    fun toDayOfWeekList(data: String?): List<DayOfWeek>? =
        data?.split(",")?.map { DayOfWeek.valueOf(it) }

    @TypeConverter
    fun fromFrequencyType(type: FrequencyType): String = type.name

    @TypeConverter
    fun toFrequencyType(value: String): FrequencyType =
        FrequencyType.valueOf(value)
}