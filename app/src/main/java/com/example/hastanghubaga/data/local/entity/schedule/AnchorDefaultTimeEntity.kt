package com.example.hastanghubaga.data.local.entity.schedule

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "anchor_default_times")
data class AnchorDefaultTimeEntity(
    @PrimaryKey
    val anchor: String,
    val timeSeconds: Int
)
