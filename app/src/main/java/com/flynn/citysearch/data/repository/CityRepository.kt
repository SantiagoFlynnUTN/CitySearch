package com.flynn.citysearch.data.repository

import android.content.Context
import com.flynn.citysearch.core.utils.JsonDownloadHelper
import com.flynn.citysearch.data.local.LocalDataSource
import com.flynn.citysearch.data.remote.CityApiService
import com.flynn.citysearch.data.remote.CityDto
import com.flynn.citysearch.data.remote.RemoteDataSource
import com.flynn.citysearch.data.remote.toCity
import com.flynn.citysearch.domain.City
import com.flynn.citysearch.feature.map.model.MapLocation
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
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

    override suspend fun getFilteredCities(prefix: String, favoritesOnly: Boolean): List<City> {
        val cities = localDataSource.filterCities(prefix)
        return if (favoritesOnly) {
            cities.filter { it.id in favoriteIds }
        } else {
            cities
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
