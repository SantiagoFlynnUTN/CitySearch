package com.flynn.citysearch.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(city: CityEntity)

    @Query("SELECT * FROM cities WHERE id = :id")
    suspend fun getById(id: Int): CityEntity?

    @Query(
        """SELECT * FROM cities 
    WHERE name LIKE :prefix || '%' 
    ORDER BY name ASC 
    LIMIT :limit OFFSET :offset"""
    )
    suspend fun getCitiesByPrefix(
        prefix: String,
        limit: Int,
        offset: Int
    ): List<CityEntity>
}
