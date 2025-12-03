package com.example.hastanghubaga.data.local.db.callback

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.hastanghubaga.data.local.dao.supplement.IngredientEntityDao
import com.example.hastanghubaga.data.local.dao.supplement.SupplementEntityDao
import com.example.hastanghubaga.data.local.dao.supplement.SupplementIngredientDao
import com.example.hastanghubaga.data.local.entity.supplement.FrequencyType
import com.example.hastanghubaga.data.local.entity.supplement.IngredientEntity
import com.example.hastanghubaga.data.local.entity.supplement.IngredientUnit
import com.example.hastanghubaga.data.local.entity.supplement.SupplementDoseUnit
import com.example.hastanghubaga.data.local.entity.supplement.SupplementEntity
import com.example.hastanghubaga.data.local.entity.supplement.SupplementIngredientEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import javax.inject.Inject

class DatabaseCallback @Inject constructor(
    private val ingredientEntityDao: IngredientEntityDao,
    private val supplementDao: SupplementEntityDao,
    private val supplementIngredientDao: SupplementIngredientDao,
    @ApplicationScope private val scope: CoroutineScope
) : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)

        // Populate DB on first creation
        scope.launch {
            insertDummyData()
        }
    }

    private suspend fun insertDummyData() {

        // ---------------------------------------------
        // 1) INGREDIENTS (40+ realistic items)
        // ---------------------------------------------
        val ingredients = listOf(
            IngredientEntity(
                name = "Vitamin C",
                defaultUnit = IngredientUnit.MG,
                rdaValue = 90.0,
                rdaUnit = IngredientUnit.MG,
                upperLimitValue = 2000.0,
                upperLimitUnit = IngredientUnit.MG,
                category = "Vitamin"
            ),
            IngredientEntity(
                name = "Vitamin D3",
                defaultUnit = IngredientUnit.IU,
                rdaValue = 800.0,
                rdaUnit = IngredientUnit.IU,
                upperLimitValue = 4000.0,
                upperLimitUnit = IngredientUnit.IU,
                category = "Vitamin"
            ),
            IngredientEntity(
                name = "Magnesium Glycinate",
                defaultUnit = IngredientUnit.MG,
                rdaValue = 400.0,
                rdaUnit = IngredientUnit.MG,
                category = "Mineral"
            ),
            IngredientEntity(
                name = "Zinc Picolinate",
                defaultUnit = IngredientUnit.MG,
                rdaValue = 11.0,
                rdaUnit = IngredientUnit.MG,
                upperLimitValue = 40.0,
                upperLimitUnit = IngredientUnit.MG,
                category = "Mineral"
            ),
            IngredientEntity(
                name = "Omega-3 Fish Oil",
                defaultUnit = IngredientUnit.MG,
                category = "Fatty Acid"
            ),
            IngredientEntity(
                name = "EPA",
                defaultUnit = IngredientUnit.MG,
                category = "Fatty Acid"
            ),
            IngredientEntity(
                name = "DHA",
                defaultUnit = IngredientUnit.MG,
                category = "Fatty Acid"
            ),
            IngredientEntity(
                name = "Creatine Monohydrate",
                defaultUnit = IngredientUnit.G,
                category = "Performance"
            ),
            IngredientEntity(
                name = "Ashwagandha KSM-66",
                defaultUnit = IngredientUnit.MG,
                category = "Herb"
            ),
            IngredientEntity(
                name = "L-Theanine",
                defaultUnit = IngredientUnit.MG,
                category = "Amino Acid"
            ),
            IngredientEntity(
                name = "Caffeine",
                defaultUnit = IngredientUnit.MG,
                category = "Stimulant"
            ),
            IngredientEntity(
                name = "Rhodiola Rosea",
                defaultUnit = IngredientUnit.MG,
                category = "Adaptogen"
            ),
            IngredientEntity(
                name = "Turmeric Extract",
                defaultUnit = IngredientUnit.MG,
                category = "Herb"
            ),
            IngredientEntity(
                name = "Curcumin",
                defaultUnit = IngredientUnit.MG,
                category = "Herb"
            ),
            IngredientEntity(
                name = "Black Pepper Extract",
                defaultUnit = IngredientUnit.MG,
                category = "Extract"
            ),
            IngredientEntity(
                name = "Calcium Carbonate",
                defaultUnit = IngredientUnit.MG,
                category = "Mineral"
            ),
            IngredientEntity(
                name = "Potassium Citrate",
                defaultUnit = IngredientUnit.MG,
                category = "Mineral"
            ),
            IngredientEntity(
                name = "Iron (Ferrous Bisglycinate)",
                defaultUnit = IngredientUnit.MG,
                category = "Mineral"
            ),
            IngredientEntity(
                name = "Vitamin B12",
                defaultUnit = IngredientUnit.MCG,
                rdaValue = 2.4,
                rdaUnit = IngredientUnit.MCG,
                category = "Vitamin"
            ),
            IngredientEntity(
                name = "Folate (Methylfolate)",
                defaultUnit = IngredientUnit.MCG,
                category = "Vitamin"
            ),
            IngredientEntity(
                name = "Vitamin K2 MK-7",
                defaultUnit = IngredientUnit.MCG,
                category = "Vitamin"
            ),
            IngredientEntity(
                name = "Milk Thistle Extract",
                defaultUnit = IngredientUnit.MG,
                category = "Herb"
            ),
            IngredientEntity(
                name = "NAC",
                defaultUnit = IngredientUnit.MG,
                category = "Amino Acid"
            ),
            IngredientEntity(
                name = "CoQ10",
                defaultUnit = IngredientUnit.MG,
                category = "Antioxidant"
            ),
            IngredientEntity(
                name = "Probiotic Blend",
                defaultUnit = IngredientUnit.CFU,
                category = "Probiotic"
            ),
            IngredientEntity(
                name = "Glucosamine",
                defaultUnit = IngredientUnit.MG,
                category = "Joint"
            ),
            IngredientEntity(
                name = "Chondroitin",
                defaultUnit = IngredientUnit.MG,
                category = "Joint"
            ),
            IngredientEntity(
                name = "MSM",
                defaultUnit = IngredientUnit.MG,
                category = "Joint"
            ),
            IngredientEntity(
                name = "GABA",
                defaultUnit = IngredientUnit.MG,
                category = "Neurotransmitter"
            ),
            IngredientEntity(
                name = "Melatonin",
                defaultUnit = IngredientUnit.MG,
                category = "Sleep"
            ),
            IngredientEntity(
                name = "Green Tea Extract",
                defaultUnit = IngredientUnit.MG,
                category = "Extract"
            ),
            IngredientEntity(
                name = "Alpha Lipoic Acid",
                defaultUnit = IngredientUnit.MG,
                category = "Antioxidant"
            ),
            IngredientEntity(
                name = "Bacopa Monnieri",
                defaultUnit = IngredientUnit.MG,
                category = "Nootropic"
            ),
            IngredientEntity(
                name = "Ginkgo Biloba",
                defaultUnit = IngredientUnit.MG,
                category = "Nootropic"
            ),
            IngredientEntity(
                name = "Electrolyte Blend",
                defaultUnit = IngredientUnit.MG,
                category = "Hydration"
            ),
            IngredientEntity(
                name = "Sodium",
                defaultUnit = IngredientUnit.MG,
                category = "Mineral"
            ),
            IngredientEntity(
                name = "Chloride",
                defaultUnit = IngredientUnit.MG,
                category = "Mineral"
            ),
            IngredientEntity(
                name = "Glycine",
                defaultUnit = IngredientUnit.G,
                category = "Amino Acid"
            ),
            IngredientEntity(
                name = "Beta-Alanine",
                defaultUnit = IngredientUnit.G,
                category = "Performance"
            )
        )


        val ingredientIdsList = ingredientEntityDao.insertIngredientsReturningIds(ingredients)

        val ingredientIds: Map<String, Long> =
            ingredients.zip(ingredientIdsList).associate { (ingredient, id) ->
                ingredient.name to id
            }

        // ---------------------------------------------
        // 2) SUPPLEMENTS (12 realistic supplements)
        // ---------------------------------------------
        val supplements = listOf(
            SupplementEntity(
                name = "Daily Multivitamin",
                brand = "NatureMade",
                recommendedServingSize = 1.0,
                recommendedDoseUnit = SupplementDoseUnit.TABLET,
                servingsPerDay = 1,
                recommendedWithFood = true,
                frequencyType = FrequencyType.DAILY,
                offsetMinutes = 30
            ),
            SupplementEntity(
                name = "Fish Oil Triple Strength",
                brand = "Kirkland",
                recommendedServingSize = 2.0,
                recommendedDoseUnit = SupplementDoseUnit.SOFTGEL,
                servingsPerDay = 1,
                recommendedWithFood = true,
                recommendedLiquidInOz = 8.0,
                frequencyType = FrequencyType.DAILY
            ),
            SupplementEntity(
                name = "Creatine Monohydrate",
                brand = "Bulk Supplements",
                recommendedServingSize = 5.0,
                recommendedDoseUnit = SupplementDoseUnit.GRAM,
                servingsPerDay = 1,
                recommendedWithFood = false,
                frequencyType = FrequencyType.DAILY
            ),
            SupplementEntity(
                name = "Magnesium Glycinate",
                brand = "Doctor's Best",
                recommendedServingSize = 2.0,
                recommendedDoseUnit = SupplementDoseUnit.CAPSULE,
                servingsPerDay = 1,
                recommendedWithFood = true,
                avoidCaffeine = true,
                frequencyType = FrequencyType.DAILY,
                offsetMinutes = 120
            ),
            SupplementEntity(
                name = "Ashwagandha KSM-66",
                brand = "NOW Foods",
                recommendedServingSize = 1.0,
                recommendedDoseUnit = SupplementDoseUnit.CAPSULE,
                servingsPerDay = 2,
                recommendedTimeBetweenDailyDosesMinutes = 480,
                frequencyType = FrequencyType.DAILY
            ),
            SupplementEntity(
                name = "Zinc Picolinate",
                brand = "Life Extension",
                recommendedServingSize = 1.0,
                recommendedDoseUnit = SupplementDoseUnit.TABLET,
                servingsPerDay = 1,
                recommendedWithFood = true,
                frequencyType = FrequencyType.EVERY_X_DAYS,
                frequencyInterval = 2
            ),
            SupplementEntity(
                name = "Melatonin Sleep Aid",
                brand = "Natrol",
                recommendedServingSize = 1.0,
                recommendedDoseUnit = SupplementDoseUnit.TABLET,
                servingsPerDay = 1,
                frequencyType = FrequencyType.DAILY,
                offsetMinutes = -30
            ),
            SupplementEntity(
                name = "Probiotic 40 Billion CFU",
                brand = "Garden of Life",
                recommendedServingSize = 1.0,
                recommendedDoseUnit = SupplementDoseUnit.CAPSULE,
                servingsPerDay = 1,
                frequencyType = FrequencyType.DAILY
            ),
            SupplementEntity(
                name = "Joint Support Complex",
                brand = "Schiff",
                recommendedServingSize = 3.0,
                recommendedDoseUnit = SupplementDoseUnit.TABLET,
                servingsPerDay = 1,
                frequencyType = FrequencyType.DAILY
            ),
            SupplementEntity(
                name = "L-Theanine + Caffeine Focus",
                brand = "Genius Brand",
                recommendedServingSize = 2.0,
                recommendedDoseUnit = SupplementDoseUnit.CAPSULE,
                servingsPerDay = 1,
                avoidCaffeine = false,
                frequencyType = FrequencyType.WEEKLY,
                weeklyDays = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
            ),
            SupplementEntity(
                name = "Electrolyte Hydration Mix",
                brand = "LMNT",
                recommendedServingSize = 1.0,
                recommendedDoseUnit = SupplementDoseUnit.SCOOP,
                servingsPerDay = 1,
                recommendedLiquidInOz = 16.0,
                frequencyType = FrequencyType.DAILY
            ),
            SupplementEntity(
                name = "Turmeric & Curcumin Complex",
                brand = "Sports Research",
                recommendedServingSize = 1.0,
                recommendedDoseUnit = SupplementDoseUnit.SOFTGEL,
                servingsPerDay = 1,
                recommendedWithFood = true,
                frequencyType = FrequencyType.DAILY
            )
        )

        val supplementIds = supplements.map { supplementDao.insertSupplement(it) }


        // ---------------------------------------------
        // 3) SUPPLEMENT INGREDIENT LINKS
        // ---------------------------------------------
        val links = listOf(
            // Multivitamin
            SupplementIngredientEntity(
                0,
                supplementIds[0],
                ingredientIds["Vitamin C"]!!,
                "Vitamin C",
                90.0,
                "mg"
            ),
            SupplementIngredientEntity(0, supplementIds[0], ingredientIds["Vitamin D3"]!!, "Vitamin D3", 1000.0, "IU"),
            SupplementIngredientEntity(0, supplementIds[0], ingredientIds["Zinc Picolinate"]!!, "Zinc", 11.0, "mg"),

            // Fish Oil
            SupplementIngredientEntity(0, supplementIds[1], ingredientIds["EPA"]!!, "EPA", 600.0, "mg"),
            SupplementIngredientEntity(0, supplementIds[1], ingredientIds["DHA"]!!, "DHA", 400.0, "mg"),

            // Creatine
            SupplementIngredientEntity(0, supplementIds[2], ingredientIds["Creatine Monohydrate"]!!, "Creatine", 5.0, "g"),

            // Magnesium Glycinate
            SupplementIngredientEntity(0, supplementIds[3], ingredientIds["Magnesium Glycinate"]!!, "Magnesium Glycinate", 200.0, "mg"),

            // Ashwagandha
            SupplementIngredientEntity(0, supplementIds[4], ingredientIds["Ashwagandha KSM-66"]!!, "Ashwagandha", 600.0, "mg"),

            // Zinc
            SupplementIngredientEntity(0, supplementIds[5], ingredientIds["Zinc Picolinate"]!!, "Zinc", 22.0, "mg"),

            // Melatonin
            SupplementIngredientEntity(0, supplementIds[6], ingredientIds["Melatonin"]!!, "Melatonin", 5.0, "mg"),

            // Probiotic
            SupplementIngredientEntity(0, supplementIds[7], ingredientIds["Probiotic Blend"]!!, "Probiotic Blend", 40_000_000_000.0, "CFU"),

            // Joint Complex
            SupplementIngredientEntity(0, supplementIds[8], ingredientIds["Glucosamine"]!!, "Glucosamine", 1500.0, "mg"),
            SupplementIngredientEntity(0, supplementIds[8], ingredientIds["Chondroitin"]!!, "Chondroitin", 1200.0, "mg"),
            SupplementIngredientEntity(0, supplementIds[8], ingredientIds["MSM"]!!, "MSM", 500.0, "mg"),

            // Theanine + Caffeine
            SupplementIngredientEntity(0, supplementIds[9], ingredientIds["L-Theanine"]!!, "L-Theanine", 200.0, "mg"),
            SupplementIngredientEntity(0, supplementIds[9], ingredientIds["Caffeine"]!!, "Caffeine", 100.0, "mg"),

            // Electrolytes
            SupplementIngredientEntity(0, supplementIds[10], ingredientIds["Sodium"]!!, "Sodium", 1000.0, "mg"),
            SupplementIngredientEntity(0, supplementIds[10], ingredientIds["Potassium Citrate"]!!, "Potassium", 200.0, "mg"),
            SupplementIngredientEntity(0, supplementIds[10], ingredientIds["Chloride"]!!, "Chloride", 1000.0, "mg"),

            // Turmeric Complex
            SupplementIngredientEntity(0, supplementIds[11], ingredientIds["Turmeric Extract"]!!, "Turmeric", 1000.0, "mg"),
            SupplementIngredientEntity(0, supplementIds[11], ingredientIds["Curcumin"]!!, "Curcumin", 95.0, "mg"),
            SupplementIngredientEntity(0, supplementIds[11], ingredientIds["Black Pepper Extract"]!!, "Black Pepper Extract", 5.0, "mg")
        )

        supplementIngredientDao.insertLinks(links)
    }

}
