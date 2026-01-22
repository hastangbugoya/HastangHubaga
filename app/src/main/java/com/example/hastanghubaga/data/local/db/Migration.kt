package com.example.hastanghubaga.data.local.db

import android.util.Log
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        Log.d("DB", "Running MIGRATION_1_2")

        db.execSQL(
            "ALTER TABLE supplements ADD COLUMN sendAlert INTEGER NOT NULL DEFAULT 0"
        )
        db.execSQL(
            "ALTER TABLE supplements ADD COLUMN alertOffsetMinutes INTEGER NOT NULL DEFAULT 0"
        )

        db.execSQL(
            "ALTER TABLE activities ADD COLUMN sendAlert INTEGER NOT NULL DEFAULT 0"
        )
        db.execSQL(
            "ALTER TABLE activities ADD COLUMN alertOffsetMinutes INTEGER NOT NULL DEFAULT 0"
        )

        db.execSQL(
            "ALTER TABLE meals ADD COLUMN sendAlert INTEGER NOT NULL DEFAULT 0"
        )
        db.execSQL(
            "ALTER TABLE meals ADD COLUMN alertOffsetMinutes INTEGER NOT NULL DEFAULT 0"
        )
    }
}
