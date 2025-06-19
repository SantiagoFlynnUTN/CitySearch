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

    @Query("UPDATE cities SET isFavorite = :isFavorite WHERE id = :cityId")
    suspend fun updateFavoriteStatus(cityId: Int, isFavorite: Boolean)

    @Query(
        """SELECT * FROM cities 
    WHERE isFavorite = 1 AND name LIKE :prefix || '%' 
    ORDER BY name ASC 
    LIMIT :limit OFFSET :offset"""
    )
    suspend fun getFavoriteCitiesByPrefix(
        prefix: String,
        limit: Int,
        offset: Int
    ): List<CityEntity>

    @Query("SELECT COUNT(*) FROM cities")
    suspend fun countAll(): Int

}
