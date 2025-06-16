package com.flynn.citysearch.data.local

import com.flynn.citysearch.domain.City
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalDataSource @Inject constructor(
    private val cityDao: CityDao
) {

    suspend fun filterCities(query: String): List<City> {
        return cityDao.getCitiesByPrefix(query,10, 0).map { it.toDomain() }
    }

    suspend fun saveCity(city: City) {
        cityDao.insert(city.toEntity())
    }

    suspend fun getCityById(id: Int): City? {
        return cityDao.getById(id)?.toDomain()
    }
}
