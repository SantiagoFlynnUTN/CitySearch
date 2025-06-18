package com.flynn.citysearch.data.repository

import com.flynn.citysearch.data.local.LocalDataSource
import com.flynn.citysearch.data.remote.RemoteDataSource
import com.flynn.citysearch.domain.City
import com.flynn.citysearch.feature.map.model.MapLocation
import com.google.android.gms.maps.model.LatLng
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Named("CityRepository")
@Singleton
class CityRepository @Inject constructor(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource
) : CityRepositoryInterface {

    //TODO local persistence
    private val favoriteIds = mutableSetOf<Int>()

    override suspend fun fetchCities() {
        remoteDataSource.fetchCities { city ->
            localDataSource.saveCity(city)
        }
    }

    override suspend fun getFilteredCities(
        prefix: String,
        favoritesOnly: Boolean,
        limit: Int,
        page: Int
    ): List<City> {
        val cities = localDataSource.filterCities(prefix, limit, page)

        return cities
            .filter { !favoritesOnly || it.id in favoriteIds }
            .map { city ->
                if (city.id in favoriteIds) city.copy(isFavorite = true) else city
            }
    }

    override fun toggleFavorite(cityId: Int) {
        if (!favoriteIds.add(cityId)) {
            favoriteIds.remove(cityId)
        }
    }

    override suspend fun getCityPolygon(cityLocation: MapLocation): Result<List<List<LatLng>>> {
        return remoteDataSource.getCityPolygons(cityName = "${cityLocation.name} ${cityLocation.latitude} ${cityLocation.longitude}")
    }
}
