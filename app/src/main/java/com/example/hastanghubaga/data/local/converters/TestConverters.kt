package com.example.hastanghubaga.data.local.converters

import androidx.room.TypeConverter
import com.example.hastanghubaga.data.local.entity.supplement.SupplementDoseUnit

object TestConverters {

    @TypeConverter
    fun fromDoseUnit(unit: SupplementDoseUnit?): String? = unit?.name

    @TypeConverter
    fun toDoseUnit(value: String?): SupplementDoseUnit? =
        value?.let { SupplementDoseUnit.valueOf(it) }
}
