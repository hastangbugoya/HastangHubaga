# AK ↔ HH Import Pipeline — FULL SPEC (v2)

## PURPOSE
Authoritative specification for importing AdobongKangkong (AK) data into HastangHubaga (HH).

This document is intended for:
- Developers
- Future AI assistants
- Architectural reference

---

# 1. CORE PRINCIPLES

1. AK is the source of truth
2. HH stores snapshot copies
3. Imported data is read-only
4. Replace-by-day (never merge)
5. Strict separation from native HH data

---

# 2. END-TO-END FLOW

HH Request
→ AK Content Provider
→ JSON Payload
→ Parse (DTO)
→ Map (Domain)
→ Map (Entity)
→ Replace Snapshot (DB Transaction)
→ Timeline Build
→ UI Display

---

# 3. DATA CONTRACT (AK → HH)

## Example JSON

{
  "schemaVersion": 1,
  "logDateIso": "2026-04-08",
  "meals": [
    {
      "groupingKey": "DINNER_1",
      "timestamp": 1775608006071,
      "mealType": "DINNER",
      "notes": "Adobong Kangkong",
      "totalCalories": 402,
      "totalProtein": 4.8,
      "totalCarbs": 102,
      "totalFat": 1.5
    }
  ]
}

---

# 4. DTO DEFINITIONS

data class AkImportedMealDto(
    val groupingKey: String,
    val logDateIso: String,
    val mealType: String,
    val timestamp: Long,
    val notes: String?,
    val totalCalories: Int,
    val totalProtein: Double,
    val totalCarbs: Double,
    val totalFat: Double
)

---

# 5. DOMAIN MODELS

data class ImportedAkMeal(
    val groupingKey: String,
    val logDateIso: String,
    val mealType: String,
    val timestamp: Long,
    val notes: String?,
    val totalCalories: Int,
    val totalProtein: Double,
    val totalCarbs: Double,
    val totalFat: Double
)

---

# 6. ENTITY MODELS

@Entity(tableName = "ak_imported_meals")
data class AkImportedMealEntity(
    @PrimaryKey val groupingKey: String,
    val logDateIso: String,
    val type: MealType,
    val timestamp: Long,
    val notes: String?,
    val totalCalories: Int,
    val totalProtein: Double,
    val totalCarbs: Double,
    val totalFat: Double
)

---

# 7. MAPPERS

DTO → Domain

fun AkImportedMealDto.toDomain(): ImportedAkMeal

Domain → Entity

fun ImportedAkMeal.toEntity(): AkImportedMealEntity

---

# 8. DAO

@Dao
interface AkImportedMealDao {

    @Query("SELECT * FROM ak_imported_meals WHERE logDateIso = :date")
    suspend fun getByDate(date: String): List<AkImportedMealEntity>

    @Query("DELETE FROM ak_imported_meals WHERE logDateIso = :date")
    suspend fun deleteByDate(date: String)

    @Insert(onConflict = REPLACE)
    suspend fun insertAll(items: List<AkImportedMealEntity>)
}

---

# 9. REPOSITORY

interface AkImportedRepository {
    suspend fun replaceDay(
        date: String,
        meals: List<AkImportedMealEntity>
    )
}

---

# 10. USE CASE

class RefreshImportedMealsUseCase(
    private val client: AkSharedImportClient,
    private val parser: ParseUseCase,
    private val repository: AkImportedRepository
) {
    suspend operator fun invoke(date: String) {

        val json = client.read(date)

        val dto = parser.parse(json)

        val entities = dto.meals.map { it.toDomain().toEntity() }

        repository.replaceDay(date, entities)
    }
}

---

# 11. TIMELINE INTEGRATION

AkImportedMealEntity
→ TimelineItem.ImportedMealTimelineItem
→ ImportedMealUiModel
→ Bottom Sheet

---

# 12. UI RULES

- Read-only display
- No editing
- No merging with HH meals

---

# 13. REFRESH STRATEGY

ALWAYS:

DELETE FROM ak_imported_meals WHERE date
INSERT new snapshot

---

# 14. DO / DO NOT

DO:
✔ Keep imports isolated
✔ Replace full day
✔ Keep mapping layers separate

DO NOT:
✘ Merge imported with HH meals
✘ Modify imported rows
✘ Skip parsing layer

---

# 15. FUTURE EXTENSIONS

- Drill-down imported logs
- Compliance comparison
- Goal sync
- Change detection

---

# END OF SPEC
