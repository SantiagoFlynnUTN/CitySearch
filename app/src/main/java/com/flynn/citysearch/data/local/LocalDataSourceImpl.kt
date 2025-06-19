package com.flynn.citysearch.data.local

import com.flynn.citysearch.domain.City
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalDataSourceImpl @Inject constructor(
    private val cityDao: CityDao
) : LocalDataSource {
    override suspend fun filterCities(query: String, limit: Int, page: Int): List<City> {
        val offset = page * limit
        return cityDao.getCitiesByPrefix(query, limit, offset).map { it.toDomain() }
    }

    override suspend fun saveCity(city: City) {
        cityDao.insert(city.toEntity())
    }

    override suspend fun toggleFavorite(cityId: Int) {
        cityDao.getById(cityId)?.let { city ->
            cityDao.updateFavoriteStatus(cityId, !city.isFavorite)
        }
    }

    override suspend fun getFilteredFavoriteCities(
        query: String,
        limit: Int,
        page: Int
    ): List<City> {
        val offset = page * limit
        return cityDao.getFavoriteCitiesByPrefix(query, limit, offset).map { it.toDomain() }
    }

    override suspend fun countCities(): Int {
        return cityDao.countAll()
    }
}
