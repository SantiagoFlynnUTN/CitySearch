package com.flynn.citysearch.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.flynn.citysearch.domain.City
import com.flynn.citysearch.domain.Coordinates

@Entity(tableName = "cities")
data class CityEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val country: String,
    val longitude: Double,
    val latitude: Double,
    val isFavorite: Boolean = false
)

fun CityEntity.toDomain(): City = City(
    id = id,
    name = name,
    country = country,
    coordinates = Coordinates(longitude, latitude),
    isFavorite = isFavorite
)

fun City.toEntity(): CityEntity = CityEntity(
    id = id,
    name = name,
    country = country,
    longitude = coordinates.longitude,
    latitude = coordinates.latitude,
    isFavorite = isFavorite
)
