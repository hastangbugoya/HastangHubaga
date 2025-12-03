package com.example.hastanghubaga.data.local.converters

import androidx.room.TypeConverter
import com.example.hastanghubaga.data.local.entity.supplement.FrequencyType
import com.example.hastanghubaga.data.local.entity.supplement.IngredientUnit
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

    @TypeConverter
    fun fromUnit(unit: IngredientUnit): String? {
        return unit.name
    }

    @TypeConverter
    fun toUnit(value: String?): IngredientUnit? {
        return value?.let { IngredientUnit.valueOf(it) }
    }
}