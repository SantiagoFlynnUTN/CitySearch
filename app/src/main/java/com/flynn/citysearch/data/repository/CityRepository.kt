package com.flynn.citysearch.data.repository

import com.flynn.citysearch.data.local.LocalDataSource
import com.flynn.citysearch.data.remote.RemoteDataSource
import com.flynn.citysearch.domain.City
import com.flynn.citysearch.feature.map.model.MapLocation
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Named("CityRepository")
@Singleton
class CityRepository @Inject constructor(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource
) : CityRepositoryInterface {

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
        return if (favoritesOnly) {
            localDataSource.getFilteredFavoriteCities(prefix, limit, page)
        } else {
            localDataSource.filterCities(prefix, limit, page)
        }
    }

    override fun toggleFavorite(cityId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            localDataSource.toggleFavorite(cityId)
        }
    }

    override suspend fun getCityPolygon(cityLocation: MapLocation): Result<List<List<LatLng>>> {
        return remoteDataSource.getCityPolygons(cityName = "${cityLocation.name} ${cityLocation.latitude} ${cityLocation.longitude}")
    }
}
