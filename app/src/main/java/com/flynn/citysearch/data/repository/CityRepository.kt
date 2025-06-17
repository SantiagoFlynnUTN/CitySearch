package com.flynn.citysearch.data.repository

import android.content.Context
import com.flynn.citysearch.core.utils.JsonDownloadHelper
import com.flynn.citysearch.data.local.LocalDataSource
import com.flynn.citysearch.data.remote.CityApiService
import com.flynn.citysearch.data.remote.CityDto
import com.flynn.citysearch.data.remote.NominatimApiService
import com.flynn.citysearch.data.remote.toCity
import com.flynn.citysearch.domain.City
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import java.io.File
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Named("CityRepository")
@Singleton
class CityRepository @Inject constructor(
    @ApplicationContext val context: Context,
    private val localDataSource: LocalDataSource,
    private val cityApiService: CityApiService,
    private val nominatimApi: NominatimApiService

) : CityRepositoryInterface {

    //TODO local persistence
    private val favoriteIds = mutableSetOf<Int>()

    override suspend fun fetchCities(): List<City> {
        /*JsonDownloadHelper.downloadToFile(
           { cityApiService.fetchCities() },
            context = context
        )

        val file = File(context.cacheDir, "cities.json")

        val cities = mutableListOf<City>()

        JsonDownloadHelper.readArrayInChunks<CityDto>(file) { city ->
            val domainCity = city.toCity()
            localDataSource.saveCity(domainCity)
        }*/

        println("Cities saved to local data source")

        return listOf()
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

    override suspend fun getCityPolygon(cityName: String): List<Pair<Double, Double>>? {
        return try {
            val response = nominatimApi.searchCity(cityName)
            val polygon = response.firstOrNull()?.geojson?.coordinates?.firstOrNull()
            polygon?.map { lonLat -> lonLat[1] to lonLat[0] }
        } catch (e: Exception) {
            println("Failed to fetch polygon: ${e.message}")
            null
        }
    }
}
