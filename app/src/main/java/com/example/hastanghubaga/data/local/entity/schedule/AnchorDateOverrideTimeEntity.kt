package com.example.hastanghubaga.data.local.entity.schedule

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "anchor_date_override_times",
    primaryKeys = ["anchor", "date"],
    indices = [
        Index(value = ["date"])
    ]
)
data class AnchorDateOverrideTimeEntity(
    val anchor: String,
    val date: String,
    val timeSeconds: Int
)