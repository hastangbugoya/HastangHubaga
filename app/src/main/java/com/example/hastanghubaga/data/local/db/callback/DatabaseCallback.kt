package com.example.hastanghubaga.data.local.db.callback

import android.util.Log
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.hastanghubaga.data.time.JavaTimeAdapter
import com.example.hastanghubaga.domain.time.DomainTimePolicy
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.atTime
import javax.inject.Inject

/**
 * Prepopulates the DB using raw SQL on creation.
 * Uses table names:
 *  - supplements
 *  - ingredients
 *  - supplement_ingredients
 *  - daily_start_time
 *
 * Make sure AppDatabase is annotated with @TypeConverters(Converters::class).
 */
class DatabaseCallback @Inject constructor() : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        java.time.ZoneId.systemDefault()
        val today: LocalDate =
            DomainTimePolicy.todayLocal()

        fun millisAt(hour: Int, minute: Int): Long =
            JavaTimeAdapter.domainLocalDateTimeToUtcMillis(
                today.atTime(LocalTime(hour, minute))
            )
        // -------------------------------
        // INGREDIENTS (45)
        // -------------------------------
        db.execSQL("""
            INSERT INTO ingredients
            (id, name, defaultUnit, rdaValue, rdaUnit, upperLimitValue, upperLimitUnit, category)
            VALUES
              (1,  'Vitamin A (Retinol)', 'MCG', 900.0, 'MCG', 3000.0, 'MCG', 'Vitamin'),
              (2,  'Vitamin B1 (Thiamine)', 'MG', 1.2, 'MG', NULL, NULL, 'Vitamin'),
              (3,  'Vitamin B2 (Riboflavin)', 'MG', 1.3, 'MG', NULL, NULL, 'Vitamin'),
              (4,  'Vitamin B3 (Niacin)', 'MG', 16.0, 'MG', 35.0, 'MG', 'Vitamin'),
              (5,  'Vitamin B5 (Pantothenic Acid)', 'MG', 5.0, 'MG', NULL, NULL, 'Vitamin'),
              (6,  'Vitamin B6 (Pyridoxine)', 'MG', 1.3, 'MG', 100.0, 'MG', 'Vitamin'),
              (7,  'Vitamin B7 (Biotin)', 'MCG', 30.0, 'MCG', NULL, NULL, 'Vitamin'),
              (8,  'Vitamin B9 (Folate / Methylfolate)', 'MCG', 400.0, 'MCG', 1000.0, 'MCG', 'Vitamin'),
              (9,  'Vitamin B12 (Cobalamin)', 'MCG', 2.4, 'MCG', NULL, NULL, 'Vitamin'),
              (10, 'Vitamin C (Ascorbic Acid)', 'MG', 90.0, 'MG', 2000.0, 'MG', 'Vitamin'),
              (11, 'Vitamin D3 (Cholecalciferol)', 'IU', 800.0, 'IU', 4000.0, 'IU', 'Vitamin'),
              (12, 'Vitamin E (Tocopherol)', 'MG', 15.0, 'MG', 1000.0, 'MG', 'Vitamin'),
              (13, 'Vitamin K2 (MK-7)', 'MCG', 120.0, 'MCG', NULL, NULL, 'Vitamin'),
              (14, 'Calcium', 'MG', 1000.0, 'MG', 2500.0, 'MG', 'Mineral'),
              (15, 'Iron (Ferrous Bisglycinate)', 'MG', 8.0, 'MG', 45.0, 'MG', 'Mineral'),
              (16, 'Magnesium (Glycinate)', 'MG', 420.0, 'MG', 350.0, 'MG', 'Mineral'),
              (17, 'Zinc (Picolinate)', 'MG', 11.0, 'MG', 40.0, 'MG', 'Mineral'),
              (18, 'Copper', 'MG', 0.9, 'MG', 10.0, 'MG', 'Mineral'),
              (19, 'Selenium', 'MCG', 55.0, 'MCG', 400.0, 'MCG', 'Mineral'),
              (20, 'Iodine', 'MCG', 150.0, 'MCG', 1100.0, 'MCG', 'Mineral'),
              (21, 'Potassium', 'MG', 4700.0, 'MG', NULL, NULL, 'Mineral'),
              (22, 'Sodium', 'MG', NULL, NULL, NULL, NULL, 'Mineral'),
              (23, 'Omega-3 (Fish Oil)', 'MG', NULL, NULL, NULL, NULL, 'Fatty Acid'),
              (24, 'EPA (Eicosapentaenoic Acid)', 'MG', NULL, NULL, NULL, NULL, 'Fatty Acid'),
              (25, 'DHA (Docosahexaenoic Acid)', 'MG', NULL, NULL, NULL, NULL, 'Fatty Acid'),
              (26, 'CoQ10 (Ubiquinone)', 'MG', NULL, NULL, NULL, NULL, 'Antioxidant'),
              (27, 'Creatine Monohydrate', 'G', NULL, NULL, NULL, NULL, 'Performance'),
              (28, 'L-Theanine', 'MG', NULL, NULL, NULL, NULL, 'Amino Acid'),
              (29, 'Caffeine', 'MG', NULL, NULL, NULL, NULL, 'Stimulant'),
              (30, 'Ashwagandha (KSM-66)', 'MG', NULL, NULL, NULL, NULL, 'Herb'),
              (31, 'Rhodiola Rosea', 'MG', NULL, NULL, NULL, NULL, 'Herb'),
              (32, 'Turmeric Extract', 'MG', NULL, NULL, NULL, NULL, 'Herb'),
              (33, 'Curcumin', 'MG', NULL, NULL, NULL, NULL, 'Extract'),
              (34, 'Black Pepper Extract (Piperine)', 'MG', NULL, NULL, NULL, NULL, 'Extract'),
              (35, 'NAC (N-Acetyl Cysteine)', 'MG', NULL, NULL, NULL, NULL, 'Amino Acid'),
              (36, 'Probiotic Blend', 'CFU', NULL, NULL, NULL, NULL, 'Probiotic'),
              (37, 'Melatonin', 'MG', NULL, NULL, NULL, NULL, 'Sleep'),
              (38, 'Glycine', 'G', NULL, NULL, NULL, NULL, 'Amino Acid'),
              (39, 'Glucosamine', 'MG', NULL, NULL, NULL, NULL, 'Joint'),
              (40, 'Chondroitin', 'MG', NULL, NULL, NULL, NULL, 'Joint'),
              (41, 'MSM', 'MG', NULL, NULL, NULL, NULL, 'Joint'),
              (42, 'Alpha Lipoic Acid (ALA)', 'MG', NULL, NULL, NULL, NULL, 'Antioxidant'),
              (43, 'Bacopa Monnieri', 'MG', NULL, NULL, NULL, NULL, 'Nootropic'),
              (44, 'Ginkgo Biloba', 'MG', NULL, NULL, NULL, NULL, 'Nootropic'),
              (45, 'Electrolyte Blend', 'MG', NULL, NULL, NULL, NULL, 'Hydration')
            ;
        """)

        // -------------------------------
        // SUPPLEMENTS
        // -------------------------------
        db.execSQL("""
            INSERT INTO supplements
            (
                id, name, brand, notes,
                recommendedServingSize, recommendedDoseUnit, servingsPerDay,
                recommendedWithFood, recommendedLiquidInOz, recommendedTimeBetweenDailyDosesMinutes,
                avoidCaffeine,
                frequencyType, frequencyInterval, weeklyDays, offsetMinutes,
                isActive, doseAnchorType
            )
            VALUES
              -- 1: Daily Multivitamin
              (1, 'Daily Multivitamin', 'NOW Foods', 'All-in-one daily multivitamin',
               2.0, 'CAPSULE', 1,
               1, 8.0, NULL,
               0,
               'DAILY', NULL, NULL, 30,
               1, 'MIDNIGHT'),

              -- 2: Fish Oil (daily)
              (2, 'Fish Oil Triple Strength', 'Kirkland', 'High EPA/DHA fish oil',
               2.0, 'SOFTGEL', 1,
               1, 8.0, NULL,
               0,
               'DAILY', NULL, NULL, 45,
               1, 'MIDNIGHT'),

              -- 3: Creatine Monohydrate
              (3, 'Creatine Monohydrate', 'BulkSupps', 'Performance supplement',
               5.0, 'GRAM', 1,
               0, NULL, NULL,
               0,
               'DAILY', NULL, NULL, 60,
               1, 'MIDNIGHT'),

              -- 4: Magnesium Glycinate
              (4, 'Magnesium Glycinate', 'Doctor''s Best', 'Supports sleep & recovery',
               2.0, 'CAPSULE', 1,
               1, 8.0, NULL,
               1,
               'DAILY', NULL, NULL, 120,
               1, 'MIDNIGHT'),

              -- 5: Ashwagandha (twice daily)
              (5, 'Ashwagandha KSM-66', 'NOW', 'Adaptogen for stress & sleep',
               1.0, 'CAPSULE', 2,
               NULL, NULL, 480,
               0,
               'DAILY', NULL, NULL, 20,
               1, 'MIDNIGHT'),

              -- 6: Zinc Picolinate (every 2 days)
              (6, 'Zinc Picolinate', 'Life Extension', 'Mineral for immune support',
               1.0, 'TABLET', 1,
               1, NULL, NULL,
               0,
               'EVERY_X_DAYS', 2, NULL, 0,
               1, 'MIDNIGHT'),

              -- 7: Melatonin (night, negative offset)
              (7, 'Melatonin 5mg', 'Natrol', 'Helps initiate sleep',
               1.0, 'TABLET', 1,
               0, NULL, NULL,
               0,
               'DAILY', NULL, NULL, -30,
               1, 'MIDNIGHT'),

              -- 8: Probiotic 40B
              (8, 'Probiotic 40 Billion CFU', 'Garden of Life', 'Probiotic blend',
               1.0, 'CAPSULE', 1,
               NULL, NULL, NULL,
               0,
               'DAILY', NULL, NULL, 10,
               1, 'MIDNIGHT'),

              -- 9: L-Theanine + Caffeine (M/W/F)
              (9, 'L-Theanine + Caffeine Focus', 'Genius', 'Stack for focus',
               2.0, 'CAPSULE', 1,
               0, NULL, NULL,
               0,
               'WEEKLY', NULL, 'MONDAY,WEDNESDAY,FRIDAY', 60,
               1, 'MIDNIGHT'),

              -- 10: Electrolyte Mix
              (10, 'Electrolyte Hydration Mix', 'LMNT', 'Sodium/potassium/magnesium',
               1.0, 'SCOOP', 1,
               0, 16.0, NULL,
               0,
               'DAILY', NULL, NULL, 15,
               1, 'MIDNIGHT'),

              -- 11: Turmeric Curcumin
              (11, 'Turmeric & Curcumin', 'Sports Research', 'Anti-inflammatory complex',
               1.0, 'SOFTGEL', 1,
               1, 8.0, NULL,
               0,
               'DAILY', NULL, NULL, 20,
               1, 'MIDNIGHT'),

              -- 12: Vitamin D3 Weekly
              (12, 'Vitamin D3 Weekly', 'Thorne', 'High-dose weekly D',
               1.0, 'SOFTGEL', 1,
               NULL, NULL, NULL,
               0,
               'WEEKLY', NULL, 'SUNDAY', 0,
               1, 'MIDNIGHT'),

              -- 13: CoQ10 (daily)
              (13, 'CoQ10 200mg', 'Qunol', 'Ubiquinone antioxidant',
               1.0, 'MG', 1,
               1, NULL, NULL,
               0,
               'DAILY', NULL, NULL, 30,
               1, 'MIDNIGHT'),

              -- 14: Vitamin C 1000
              (14, 'Vitamin C 1000', 'NOW', 'Immune support',
               1.0, 'TABLET', 1,
               1, NULL, NULL,
               0,
               'DAILY', NULL, NULL, 30,
               1, 'MIDNIGHT'),

              -- 15: Calcium + Vitamin D combo (every other day)
              (15, 'Calcium + D', 'Generic', 'Bone support combo',
               2.0, 'TABLET', 1,
               1, NULL, NULL,
               0,
               'EVERY_X_DAYS', 2, NULL, 120,
               1, 'MIDNIGHT'),

              -- 16: Collagen Peptides (daily)
              (16, 'Collagen Peptides', 'Vital Proteins', 'Joint/skin support',
               1.0, 'SCOOP', 1,
               1, 8.0, NULL,
               0,
               'DAILY', NULL, NULL, 10,
               1, 'MIDNIGHT'),

              -- 17: NAC (daily)
              (17, 'NAC 600mg', 'Jarrow', 'Liver support',
               1.0, 'TABLET', 1,
               0, NULL, NULL,
               0,
               'DAILY', NULL, NULL, 30,
               1, 'MIDNIGHT'),

              -- 18: Multivitamin (alternate high dose every 3 days)
              (18, 'High Potency Multi (Q3)', 'BrandX', 'Every 3 days',
               2.0, 'TABLET', 1,
               1, NULL, NULL,
               0,
               'EVERY_X_DAYS', 3, NULL, 60,
               1, 'MIDNIGHT')
            ;
        """)

        // -------------------------------
        // SUPPLEMENT -> INGREDIENT LINKS
        // -------------------------------
        db.execSQL("""
            INSERT INTO supplement_ingredients
            (id, supplementId, ingredientId, displayName, amountPerServing, unit)
            VALUES
              -- Supplement 1 (Daily Multivitamin)
              (1, 1, 10, 'Vitamin C (Ascorbic Acid)', 500.0, 'MG'),
              (2, 1, 11, 'Vitamin D3 (Cholecalciferol)', 1000.0, 'IU'),
              (3, 1, 16, 'Magnesium (Glycinate)', 75.0, 'MG'),
              (4, 1, 17, 'Zinc (Picolinate)', 11.0, 'MG'),
              (5, 1, 9, 'Vitamin B12 (Cobalamin)', 6.0, 'MCG'),
              (6, 1, 8, 'Folate (Methylfolate)', 400.0, 'MCG'),

              -- Supplement 2 (Fish Oil)
              (7, 2, 24, 'EPA', 600.0, 'MG'),
              (8, 2, 25, 'DHA', 400.0, 'MG'),

              -- Supplement 3 (Creatine)
              (9, 3, 27, 'Creatine Monohydrate', 5.0, 'G'),

              -- Supplement 4 (Magnesium Glycinate)
              (10, 4, 16, 'Magnesium (Glycinate)', 200.0, 'MG'),

              -- Supplement 5 (Ashwagandha)
              (11, 5, 30, 'Ashwagandha (KSM-66)', 300.0, 'MG'),

              -- Supplement 6 (Zinc Picolinate)
              (12, 6, 17, 'Zinc (Picolinate)', 22.0, 'MG'),

              -- Supplement 7 (Melatonin)
              (13, 7, 37, 'Melatonin', 5.0, 'MG'),

              -- Supplement 8 (Probiotic)
              (14, 8, 36, 'Probiotic Blend (CFU)', 40000000000.0, 'CFU'),

              -- Supplement 9 (Theanine + Caffeine)
              (15, 9, 28, 'L-Theanine', 200.0, 'MG'),
              (16, 9, 29, 'Caffeine', 100.0, 'MG'),

              -- Supplement 10 (Electrolyte Mix)
              (17, 10, 45, 'Electrolyte Blend', 2000.0, 'MG'),
              (18, 10, 21, 'Potassium', 300.0, 'MG'),
              (19, 10, 22, 'Sodium', 1000.0, 'MG'),

              -- Supplement 11 (Turmeric)
              (20, 11, 32, 'Turmeric Extract', 500.0, 'MG'),
              (21, 11, 33, 'Curcumin', 95.0, 'MG'),
              (22, 11, 34, 'Black Pepper Extract (Piperine)', 5.0, 'MG'),

              -- Supplement 12 (Vitamin D weekly)
              (23, 12, 11, 'Vitamin D3 (weekly)', 50000.0, 'IU'),

              -- Supplement 13 (CoQ10)
              (24, 13, 26, 'CoQ10 (Ubiquinone)', 200.0, 'MG'),

              -- Supplement 14 (Vitamin C 1000)
              (25, 14, 10, 'Vitamin C (Ascorbic Acid)', 1000.0, 'MG'),

              -- Supplement 15 (Calcium + D)
              (26, 15, 14, 'Calcium (as Carbonate)', 500.0, 'MG'),
              (27, 15, 11, 'Vitamin D3', 1000.0, 'IU'),

              -- Supplement 16 (Collagen)
              (28, 16, 38, 'Glycine', 3.0, 'G'),

              -- Supplement 17 (NAC)
              (29, 17, 35, 'NAC (N-Acetyl Cysteine)', 600.0, 'MG'),

              -- Supplement 18 (High Potency Multi Q3)
              (30, 18, 10, 'Vitamin C (Ascorbic Acid)', 1000.0, 'MG'),
              (31, 18, 11, 'Vitamin D3', 2000.0, 'IU'),
              (32, 18, 16, 'Magnesium', 100.0, 'MG')
            ;
        """)

        // -------------------------------
        // DAILY START TIME
        // -------------------------------
        db.execSQL("""
            INSERT INTO daily_start_time (date, hourZero)
            VALUES ('${java.time.LocalDate.now()}', 28800);
        """)
        Log.d("Meow", "🔥 Room onCreate() callback RUNNING")
        // -------------------------------
        // DEFAULT EVENT TIME
        // -------------------------------
        db.execSQL(
            """
        INSERT OR IGNORE INTO event_default_times (anchor, timeSeconds)
            VALUES
            ('MIDNIGHT',        0),
            ('WAKEUP',          25200),
            ('BREAKFAST',       28800),
            ('LUNCH',           43200),
            ('DINNER',          64800),
            ('BEFORE_WORKOUT',  59400),
            ('AFTER_WORKOUT',   63900),
            ('SLEEP',           75600);
        """.trimIndent()
        )
        db.execSQL(
            """
                INSERT INTO supplement_user_settings 
                    (supplementId, preferredServingSize, preferredUnit, preferredServingPerDay, isEnabled)
                VALUES
                    (1, 2.0, 'CAPSULE', 1, 1),   -- Multivitamin: user prefers 2 capsules
                    (3, 5.0, 'GRAM', 1, 1),      -- Creatine: prefer 5g
                    (7, 1.0, 'TABLET', 2, 0);    -- Melatonin disabled by user
                """)

        db.execSQL(
            """
            INSERT INTO event_day_of_week_times (anchor, dayOfWeek, timeSeconds)
            VALUES
                ('BREAKFAST', 'SATURDAY', ${LocalTime(10, 0).toSecondOfDay()}),
                ('BREAKFAST', 'SUNDAY', ${LocalTime(6, 0).toSecondOfDay()}),
                ('BREAKFAST', 'WEDNESDAY', ${LocalTime(9, 0).toSecondOfDay()})
            ;
            """.trimIndent()
                )

        // -------------------------------
        // MEALS ADDED HERE
        // -------------------------------
        DomainTimePolicy.todayLocal()

        fun mealMillis(h: Int, m: Int): Long {
            val localDateTime =
                today.atTime(
                    LocalTime(h, m)
                )

            return JavaTimeAdapter.domainLocalDateTimeToUtcMillis(localDateTime)
        }

        db.execSQL(
                """
                INSERT INTO meals (type, timestamp) VALUES
                    ('BREAKFAST', ${mealMillis(8, 0)}),
                    ('LUNCH',     ${mealMillis(12, 0)}),
                    ('DINNER',    ${mealMillis(18, 0)});
                """.trimIndent()
        )

        db.execSQL(
            """
                INSERT INTO activities (type, startTimestamp, endTimestamp, notes) VALUES
                -- Morning workout (45 min)
                ('STRENGTH_TRAINING',
                  ${millisAt(7, 0)},
                  ${millisAt(7, 45)},
                  'Morning strength training'
                ),
                -- Breakfast walk (20 min)
                ('WALKING',
                  ${millisAt(8, 15)},
                  ${millisAt(8, 35)},
                  'Post-breakfast walk'
                ),
                -- Focus work session (90 min)
                ('WORK',
                  ${millisAt(10, 0)},
                  ${millisAt(11, 30)},
                  'Deep work session'
                ),
                -- Lunch break (30 min)
                ('MEAL',
                  ${millisAt(12, 0)},
                  ${millisAt(12, 30)},
                  'Lunch break'
                ),
                -- Evening relaxation (no end time)
                ('RELAX',
                  ${millisAt(20, 0)},
                  NULL,
                  'Evening relaxation'
                ),
                ('SLEEP',
                  ${millisAt(21, 0)},
                  NULL,
                  'Sleep'
                );
                """.trimIndent()
        )
        Log.d("Meow", "Inserted meal millis: ${mealMillis(8, 0)}")
    }
}
