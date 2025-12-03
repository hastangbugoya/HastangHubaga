package com.example.hastanghubaga.model.dao.supplement

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.hastanghubaga.model.entity.supplement.SupplementIngredientEntity

@Dao
interface SupplementIngredientDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLinks(links: List<SupplementIngredientEntity>)

    @Query("SELECT * FROM supplement_ingredients WHERE supplementId = :supplementId")
    suspend fun getLinksForSupplement(supplementId: Long): List<SupplementIngredientEntity>

    @Query("SELECT * FROM supplement_ingredients")
    suspend fun getAllLinks(): List<SupplementIngredientEntity>
}