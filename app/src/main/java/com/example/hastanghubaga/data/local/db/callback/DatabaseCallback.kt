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
 *
 * Debugging note:
 * This callback now logs before and after each seed section so failures can be
 * pinpointed on-device instead of silently producing an empty database.
 */
class DatabaseCallback @Inject constructor() : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        Log.e("Meow", "DB onCreate CALLED")
        Log.d("SeedDebug", "Room onCreate() callback CALLED")

        val today: LocalDate = DomainTimePolicy.todayLocal()

        fun millisAt(hour: Int, minute: Int): Long =
            JavaTimeAdapter.domainLocalDateTimeToUtcMillis(
                today.atTime(LocalTime(hour, minute))
            )

        fun mealMillis(h: Int, m: Int): Long =
            JavaTimeAdapter.domainLocalDateTimeToUtcMillis(
                today.atTime(LocalTime(h, m))
            )

        fun runSection(
            name: String,
            block: () -> Unit
        ) {
            Log.d("SeedDebug", "BEGIN section=$name")
            try {
                block()
                Log.d("SeedDebug", "END section=$name")
            } catch (t: Throwable) {
                Log.e("SeedDebug", "FAILED section=$name", t)
                throw t
            }
        }

        runSection("ingredients") {
            db.execSQL(
                """
INSERT INTO ingredients
(id, name, code, defaultUnit, rdaValue, rdaUnit, upperLimitValue, upperLimitUnit, category)
VALUES
  (1,  'Vitamin A (Retinol)', 'VITAMIN_A_MCG', 'MCG', 900.0, 'MCG', 3000.0, 'MCG', 'Vitamin'),
  (2,  'Vitamin B1 (Thiamine)', 'THIAMINE_MG', 'MG', 1.2, 'MG', NULL, NULL, 'Vitamin'),
  (3,  'Vitamin B2 (Riboflavin)', 'RIBOFLAVIN_MG', 'MG', 1.3, 'MG', NULL, NULL, 'Vitamin'),
  (4,  'Vitamin B3 (Niacin)', 'NIACIN_MG', 'MG', 16.0, 'MG', 35.0, 'MG', 'Vitamin'),
  (5,  'Vitamin B5 (Pantothenic Acid)', 'PANTOTHENIC_ACID_MG', 'MG', 5.0, 'MG', NULL, NULL, 'Vitamin'),
  (6,  'Vitamin B6 (Pyridoxine)', 'VITAMIN_B6_MG', 'MG', 1.3, 'MG', 100.0, 'MG', 'Vitamin'),
  (7,  'Vitamin B7 (Biotin)', 'BIOTIN_MCG', 'MCG', 30.0, 'MCG', NULL, NULL, 'Vitamin'),
  (8,  'Vitamin B9 (Folate / Methylfolate)', 'FOLATE_DFE_MCG', 'MCG', 400.0, 'MCG', 1000.0, 'MCG', 'Vitamin'),
  (9,  'Vitamin B12 (Cobalamin)', 'VITAMIN_B12_MCG', 'MCG', 2.4, 'MCG', NULL, NULL, 'Vitamin'),
  (10, 'Vitamin C (Ascorbic Acid)', 'VITAMIN_C_MG', 'MG', 90.0, 'MG', 2000.0, 'MG', 'Vitamin'),
  (11, 'Vitamin D3 (Cholecalciferol)', 'VITAMIN_D_MCG', 'IU', 800.0, 'IU', 4000.0, 'IU', 'Vitamin'),
  (12, 'Vitamin E (Tocopherol)', 'VITAMIN_E_MG', 'MG', 15.0, 'MG', 1000.0, 'MG', 'Vitamin'),
  (13, 'Vitamin K2 (MK-7)', 'VITAMIN_K_MCG', 'MCG', 120.0, 'MCG', NULL, NULL, 'Vitamin'),
  (14, 'Calcium', 'CALCIUM_MG', 'MG', 1000.0, 'MG', 2500.0, 'MG', 'Mineral'),
  (15, 'Iron (Ferrous Bisglycinate)', 'IRON_MG', 'MG', 8.0, 'MG', 45.0, 'MG', 'Mineral'),
  (16, 'Magnesium (Glycinate)', 'MAGNESIUM_MG', 'MG', 420.0, 'MG', 350.0, 'MG', 'Mineral'),
  (17, 'Zinc (Picolinate)', 'ZINC_MG', 'MG', 11.0, 'MG', 40.0, 'MG', 'Mineral'),
  (18, 'Copper', 'COPPER_MG', 'MG', 0.9, 'MG', 10.0, 'MG', 'Mineral'),
  (19, 'Selenium', 'SELENIUM_MCG', 'MCG', 55.0, 'MCG', 400.0, 'MCG', 'Mineral'),
  (20, 'Iodine', 'IODINE_MCG', 'MCG', 150.0, 'MCG', 1100.0, 'MCG', 'Mineral'),
  (21, 'Potassium', 'POTASSIUM_MG', 'MG', 4700.0, 'MG', NULL, NULL, 'Mineral'),
  (22, 'Sodium', 'SODIUM_MG', 'MG', NULL, NULL, NULL, NULL, 'Mineral'),
  (23, 'Omega-3 (Fish Oil)', 'OMEGA_3_MG', 'MG', NULL, NULL, NULL, NULL, 'Fatty Acid'),
  (24, 'EPA (Eicosapentaenoic Acid)', 'EPA_MG', 'MG', NULL, NULL, NULL, NULL, 'Fatty Acid'),
  (25, 'DHA (Docosahexaenoic Acid)', 'DHA_MG', 'MG', NULL, NULL, NULL, NULL, 'Fatty Acid'),
  (26, 'CoQ10 (Ubiquinone)', 'COQ10_MG', 'MG', NULL, NULL, NULL, NULL, 'Antioxidant'),
  (27, 'Creatine Monohydrate', 'CREATINE_G', 'G', NULL, NULL, NULL, NULL, 'Performance'),
  (28, 'L-Theanine', 'L_THEANINE_MG', 'MG', NULL, NULL, NULL, NULL, 'Amino Acid'),
  (29, 'Caffeine', 'CAFFEINE_MG', 'MG', NULL, NULL, NULL, NULL, 'Stimulant'),
  (30, 'Ashwagandha (KSM-66)', 'ASHWAGANDHA_MG', 'MG', NULL, NULL, NULL, NULL, 'Herb'),
  (31, 'Rhodiola Rosea', 'RHODIOLA_MG', 'MG', NULL, NULL, NULL, NULL, 'Herb'),
  (32, 'Turmeric Extract', 'TURMERIC_MG', 'MG', NULL, NULL, NULL, NULL, 'Herb'),
  (33, 'Curcumin', 'CURCUMIN_MG', 'MG', NULL, NULL, NULL, NULL, 'Extract'),
  (34, 'Black Pepper Extract (Piperine)', 'PIPERINE_MG', 'MG', NULL, NULL, NULL, NULL, 'Extract'),
  (35, 'NAC (N-Acetyl Cysteine)', 'NAC_MG', 'MG', NULL, NULL, NULL, NULL, 'Amino Acid'),
  (36, 'Probiotic Blend', 'PROBIOTICS_CFU', 'CFU', NULL, NULL, NULL, NULL, 'Probiotic'),
  (37, 'Melatonin', 'MELATONIN_MG', 'MG', NULL, NULL, NULL, NULL, 'Sleep'),
  (38, 'Glycine', 'GLYCINE_G', 'G', NULL, NULL, NULL, NULL, 'Amino Acid'),
  (39, 'Glucosamine', 'GLUCOSAMINE_MG', 'MG', NULL, NULL, NULL, NULL, 'Joint'),
  (40, 'Chondroitin', 'CHONDROITIN_MG', 'MG', NULL, NULL, NULL, NULL, 'Joint'),
  (41, 'MSM', 'MSM_MG', 'MG', NULL, NULL, NULL, NULL, 'Joint'),
  (42, 'Alpha Lipoic Acid (ALA)', 'ALA_MG', 'MG', NULL, NULL, NULL, NULL, 'Antioxidant'),
  (43, 'Bacopa Monnieri', 'BACOPA_MG', 'MG', NULL, NULL, NULL, NULL, 'Nootropic'),
  (44, 'Ginkgo Biloba', 'GINKGO_MG', 'MG', NULL, NULL, NULL, NULL, 'Nootropic'),
  (45, 'Electrolyte Blend', 'ELECTROLYTES_MG', 'MG', NULL, NULL, NULL, NULL, 'Hydration')
;
                """.trimIndent()
            )
        }

        runSection("supplements") {
            db.execSQL(
                """
INSERT INTO supplements
(
    id, name, brand, notes,
    recommendedServingSize, recommendedDoseUnit, servingsPerDay,
    recommendedWithFood, recommendedLiquidInOz, recommendedTimeBetweenDailyDosesMinutes,
    avoidCaffeine,
    frequencyType, frequencyInterval, weeklyDays, offsetMinutes,
    isActive, doseAnchorType,
    sendAlert, alertOffsetMinutes
)
VALUES
  (1, 'Daily Multivitamin', 'NOW Foods', 'All-in-one daily multivitamin',
   2.0, 'CAPSULE', 1, 1, 8.0, NULL, 0, 'DAILY', NULL, NULL, 30, 1, 'MIDNIGHT', 0, 0),
  (2, 'Fish Oil Triple Strength', 'Kirkland', 'High EPA/DHA fish oil',
   2.0, 'SOFTGEL', 1, 1, 8.0, NULL, 0, 'DAILY', NULL, NULL, 45, 1, 'MIDNIGHT', 0, 0),
  (3, 'Creatine Monohydrate', 'BulkSupps', 'Performance supplement',
   5.0, 'GRAM', 1, 0, NULL, NULL, 0, 'DAILY', NULL, NULL, 60, 1, 'MIDNIGHT', 0, 0),
  (4, 'Magnesium Glycinate', 'Doctor''s Best', 'Supports sleep & recovery',
   2.0, 'CAPSULE', 1, 1, 8.0, NULL, 1, 'DAILY', NULL, NULL, 120, 1, 'MIDNIGHT', 0, 0),
  (5, 'Ashwagandha KSM-66', 'NOW', 'Adaptogen for stress & sleep',
   1.0, 'CAPSULE', 2, NULL, NULL, 480, 0, 'DAILY', NULL, NULL, 20, 1, 'MIDNIGHT', 0, 0),
  (6, 'Zinc Picolinate', 'Life Extension', 'Mineral for immune support',
   1.0, 'TABLET', 1, 1, NULL, NULL, 0, 'EVERY_X_DAYS', 2, NULL, 0, 1, 'MIDNIGHT', 0, 0),
  (7, 'Melatonin 5mg', 'Natrol', 'Helps initiate sleep',
   1.0, 'TABLET', 1, 0, NULL, NULL, 0, 'DAILY', NULL, NULL, -30, 1, 'MIDNIGHT', 0, 0),
  (8, 'Probiotic 40 Billion CFU', 'Garden of Life', 'Probiotic blend',
   1.0, 'CAPSULE', 1, NULL, NULL, NULL, 0, 'DAILY', NULL, NULL, 10, 1, 'MIDNIGHT', 0, 0),
  (9, 'L-Theanine + Caffeine Focus', 'Genius', 'Stack for focus',
   2.0, 'CAPSULE', 1, 0, NULL, NULL, 0, 'WEEKLY', NULL, 'MONDAY,WEDNESDAY,FRIDAY', 60, 1, 'MIDNIGHT', 0, 0),
  (10, 'Electrolyte Hydration Mix', 'LMNT', 'Sodium/potassium/magnesium',
   1.0, 'SCOOP', 1, 0, 16.0, NULL, 0, 'DAILY', NULL, NULL, 15, 1, 'MIDNIGHT', 0, 0),
  (11, 'Turmeric & Curcumin', 'Sports Research', 'Anti-inflammatory complex',
   1.0, 'SOFTGEL', 1, 1, 8.0, NULL, 0, 'DAILY', NULL, NULL, 20, 1, 'MIDNIGHT', 0, 0),
  (12, 'Vitamin D3 Weekly', 'Thorne', 'High-dose weekly D',
   1.0, 'SOFTGEL', 1, NULL, NULL, NULL, 0, 'WEEKLY', NULL, 'SUNDAY', 0, 1, 'MIDNIGHT', 0, 0),
  (13, 'CoQ10 200mg', 'Qunol', 'Ubiquinone antioxidant',
   1.0, 'MG', 1, 1, NULL, NULL, 0, 'DAILY', NULL, NULL, 30, 1, 'MIDNIGHT', 0, 0),
  (14, 'Vitamin C 1000', 'NOW', 'Immune support',
   1.0, 'TABLET', 1, 1, NULL, NULL, 0, 'DAILY', NULL, NULL, 30, 1, 'MIDNIGHT', 0, 0),
  (15, 'Calcium + D', 'Generic', 'Bone support combo',
   2.0, 'TABLET', 1, 1, NULL, NULL, 0, 'EVERY_X_DAYS', 2, NULL, 120, 1, 'MIDNIGHT', 0, 0),
  (16, 'Collagen Peptides', 'Vital Proteins', 'Joint/skin support',
   1.0, 'SCOOP', 1, 1, 8.0, NULL, 0, 'DAILY', NULL, NULL, 10, 1, 'MIDNIGHT', 0, 0),
  (17, 'NAC 600mg', 'Jarrow', 'Liver support',
   1.0, 'TABLET', 1, 0, NULL, NULL, 0, 'DAILY', NULL, NULL, 30, 1, 'MIDNIGHT', 0, 0),
  (18, 'High Potency Multi (Q3)', 'BrandX', 'Every 3 days',
   2.0, 'TABLET', 1, 1, NULL, NULL, 0, 'EVERY_X_DAYS', 3, NULL, 60, 1, 'MIDNIGHT', 0, 0)
;
                """.trimIndent()
            )
        }

        runSection("supplement_ingredients") {
            db.execSQL(
                """
INSERT INTO supplement_ingredients
(id, supplementId, ingredientId, displayName, amountPerServing, unit)
VALUES
  (1, 1, 10, 'Vitamin C (Ascorbic Acid)', 500.0, 'MG'),
  (2, 1, 11, 'Vitamin D3 (Cholecalciferol)', 1000.0, 'IU'),
  (3, 1, 16, 'Magnesium (Glycinate)', 75.0, 'MG'),
  (4, 1, 17, 'Zinc (Picolinate)', 11.0, 'MG'),
  (5, 1, 9, 'Vitamin B12 (Cobalamin)', 6.0, 'MCG'),
  (6, 1, 8, 'Folate (Methylfolate)', 400.0, 'MCG'),
  (7, 2, 24, 'EPA', 600.0, 'MG'),
  (8, 2, 25, 'DHA', 400.0, 'MG'),
  (9, 3, 27, 'Creatine Monohydrate', 5.0, 'G'),
  (10, 4, 16, 'Magnesium (Glycinate)', 200.0, 'MG'),
  (11, 5, 30, 'Ashwagandha (KSM-66)', 300.0, 'MG'),
  (12, 6, 17, 'Zinc (Picolinate)', 22.0, 'MG'),
  (13, 7, 37, 'Melatonin', 5.0, 'MG'),
  (14, 8, 36, 'Probiotic Blend (CFU)', 40000000000.0, 'CFU'),
  (15, 9, 28, 'L-Theanine', 200.0, 'MG'),
  (16, 9, 29, 'Caffeine', 100.0, 'MG'),
  (17, 10, 45, 'Electrolyte Blend', 2000.0, 'MG'),
  (18, 10, 21, 'Potassium', 300.0, 'MG'),
  (19, 10, 22, 'Sodium', 1000.0, 'MG'),
  (20, 11, 32, 'Turmeric Extract', 500.0, 'MG'),
  (21, 11, 33, 'Curcumin', 95.0, 'MG'),
  (22, 11, 34, 'Black Pepper Extract (Piperine)', 5.0, 'MG'),
  (23, 12, 11, 'Vitamin D3 (weekly)', 50000.0, 'IU'),
  (24, 13, 26, 'CoQ10 (Ubiquinone)', 200.0, 'MG'),
  (25, 14, 10, 'Vitamin C (Ascorbic Acid)', 1000.0, 'MG'),
  (26, 15, 14, 'Calcium (as Carbonate)', 500.0, 'MG'),
  (27, 15, 11, 'Vitamin D3', 1000.0, 'IU'),
  (28, 16, 38, 'Glycine', 3.0, 'G'),
  (29, 17, 35, 'NAC (N-Acetyl Cysteine)', 600.0, 'MG'),
  (30, 18, 10, 'Vitamin C (Ascorbic Acid)', 1000.0, 'MG'),
  (31, 18, 11, 'Vitamin D3', 2000.0, 'IU'),
  (32, 18, 16, 'Magnesium', 100.0, 'MG')
;
                """.trimIndent()
            )
        }

        runSection("supplement_schedules") {
            db.execSQL(
                """
INSERT INTO supplement_schedules
(id, supplementId, recurrenceType, interval, weeklyDays, startDate, endDate, timingType, isEnabled)
VALUES
    (1, 1, 'DAILY', 1, NULL, '${today}', NULL, 'FIXED', 1),
    (2, 2, 'DAILY', 1, NULL, '${today}', NULL, 'FIXED', 1),
    (3, 3, 'DAILY', 1, NULL, '${today}', NULL, 'FIXED', 1),
    (4, 4, 'DAILY', 1, NULL, '${today}', NULL, 'FIXED', 1),
    (5, 5, 'DAILY', 1, NULL, '${today}', NULL, 'FIXED', 1)
;
        """.trimIndent()
            )
        }

        runSection("supplement_schedule_fixed_times") {
            db.execSQL(
                """
INSERT INTO supplement_schedule_fixed_times
(id, scheduleId, time, label, sortOrder)
VALUES
    (1, 1, '08:00:00', 'Morning', 0),
    (2, 2, '12:00:00', 'Lunch', 0),
    (3, 3, '17:00:00', 'Afternoon', 0),
    (4, 4, '21:00:00', 'Evening', 0),
    (5, 5, '22:00:00', 'Night', 0)
;
        """.trimIndent()
            )
        }

        runSection("daily_start_time") {
            db.execSQL(
                """
INSERT INTO daily_start_time (date, hourZero)
VALUES ('${java.time.LocalDate.now()}', 28800);
                """.trimIndent()
            )
        }

        runSection("event_default_times") {
            db.execSQL(
                """
INSERT OR IGNORE INTO event_default_times (anchor, timeSeconds)
VALUES
    ('MIDNIGHT',        0),
    ('WAKEUP',          25200),
    ('BREAKFAST',       28800),
    ('LUNCH',           43200),
    ('DINNER',          64800),
    ('SNACK',           52200),
    ('BEFORE_WORKOUT',  59400),
    ('DURING_WORKOUT',  61200),
    ('AFTER_WORKOUT',   63900),
    ('SLEEP',           75600);
        """.trimIndent()
            )
        }

        runSection("supplement_user_settings") {
            db.execSQL(
                """
INSERT INTO supplement_user_settings
(
    supplementId,
    preferredServingSize,
    preferredUnit,
    preferredServingPerDay,
    isEnabled,
    scheduleType
)
VALUES
    (1, 2.0, 'CAPSULE', 1, 1, 'FIXED_TIMES'),
    (3, 5.0, 'GRAM',    1, 1, 'FIXED_TIMES'),
    (7, 1.0, 'TABLET',  2, 0, 'FIXED_TIMES');
                """.trimIndent()
            )
        }

        runSection("event_day_of_week_times") {
            db.execSQL(
                """
INSERT INTO event_day_of_week_times (anchor, dayOfWeek, timeSeconds)
VALUES
    ('BREAKFAST', 'SATURDAY', ${LocalTime(10, 0).toSecondOfDay()}),
    ('BREAKFAST', 'SUNDAY', ${LocalTime(6, 0).toSecondOfDay()}),
    ('BREAKFAST', 'WEDNESDAY', ${LocalTime(9, 0).toSecondOfDay()});
                """.trimIndent()
            )
        }

        runSection("meals") {
            db.execSQL(
                """
INSERT INTO meals (type, timestamp, sendAlert, alertOffsetMinutes)
VALUES
    ('BREAKFAST', ${mealMillis(8, 0)}, 0, 0),
    ('LUNCH',     ${mealMillis(12, 0)}, 0, 0),
    ('DINNER',    ${mealMillis(18, 0)}, 0, 0);
                """.trimIndent()
            )
            Log.d("SeedDebug", "Inserted meals for date=$today")
            Log.d("SeedDebug", "Breakfast ts=${mealMillis(8, 0)}")
            Log.d("SeedDebug", "Lunch ts=${mealMillis(12, 0)}")
            Log.d("SeedDebug", "Dinner ts=${mealMillis(18, 0)}")
        }

        runSection("activities") {
            db.execSQL(
                """
INSERT INTO activities
(id, type, startTimestamp, endTimestamp, notes, intensity, isWorkout, isActive, sendAlert, alertOffsetMinutes)
VALUES
    (1, 'STRENGTH_TRAINING', ${millisAt(7, 0)}, ${millisAt(7, 45)}, 'Morning strength training', 7, 1, 1, 0, 0),
    (2, 'WALKING', ${millisAt(8, 15)}, ${millisAt(8, 35)}, 'Post-breakfast walk', 3, 1, 1, 0, 0),
    (3, 'WORK', ${millisAt(10, 0)}, ${millisAt(11, 30)}, 'Deep work session', NULL, 0, 1, 0, 0),
    (4, 'RELAX', ${millisAt(20, 0)}, NULL, 'Evening relaxation', NULL, 0, 1, 0, 0),
    (5, 'SLEEP', ${millisAt(21, 0)}, NULL, 'Sleep', NULL, 0, 0, 0, 0);
                """.trimIndent()
            )
            Log.d("SeedDebug", "Inserted activities for date=$today")
            Log.d("SeedDebug", "Sample activity ts=${millisAt(7, 0)}")
        }

        runSection("activity_schedules") {
            db.execSQL(
                """
INSERT INTO activity_schedules
(id, activityId, recurrenceType, interval, weeklyDays, startDate, endDate, timingType, isEnabled)
VALUES
    (1, 1, 'DAILY', 1, NULL, '${today}', NULL, 'FIXED', 1),
    (2, 2, 'DAILY', 1, NULL, '${today}', NULL, 'ANCHORED', 1),
    (3, 4, 'DAILY', 1, NULL, '${today}', NULL, 'FIXED', 1),
    (4, 5, 'DAILY', 1, NULL, '${today}', NULL, 'FIXED', 1);
                """.trimIndent()
            )
        }

        runSection("activity_schedule_fixed_times") {
            db.execSQL(
                """
INSERT INTO activity_schedule_fixed_times
(id, scheduleId, time, label, sortOrder)
VALUES
    (1, 1, '07:00:00', 'Morning strength', 0),
    (2, 3, '20:00:00', 'Evening relaxation', 0),
    (3, 4, '21:00:00', 'Sleep', 0);
                """.trimIndent()
            )
        }

        runSection("activity_schedule_anchored_times") {
            db.execSQL(
                """
INSERT INTO activity_schedule_anchored_times
(id, scheduleId, anchor, offsetMinutes, label, sortOrder)
VALUES
    (1, 2, 'AFTER_WORKOUT', 30, 'Post-workout walk', 0);
                """.trimIndent()
            )
        }

        runSection("nutrition_goals") {
            db.execSQL(
                """
INSERT INTO nutrition_goals (
    type,
    name,
    startDate,
    endDate,
    dailyProteinTarget,
    dailyFatTarget,
    dailyCarbTarget,
    dailyCalorieTarget,
    sodiumLimitMg,
    cholesterolLimitMg,
    fiberTargetGrams,
    isActive
)
VALUES (
    'CUTTING',
    'Sustainable Cut 50+',
    ${System.currentTimeMillis()},
    NULL,
    165.0,
    70.0,
    180.0,
    2100.0,
    2300.0,
    300.0,
    30.0,
    1
);
                """.trimIndent()
            )
        }

        Log.d("SeedDebug", "Room onCreate() callback FINISHED")
    }
}