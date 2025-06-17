package com.flynn.citysearch.data.remote

import android.content.Context
import com.flynn.citysearch.core.utils.JsonDownloadHelper
import com.flynn.citysearch.domain.City
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

typealias Polygon = List<List<List<Double>>>

@Suppress("UNCHECKED_CAST")
@Singleton
class RemoteDataSourceImpl @Inject constructor(
    @ApplicationContext val context: Context,
    private val cityApiService: CityApiService,
    private val nominatimApi: NominatimApiService
) : RemoteDataSource {

    override suspend fun getCityPolygons(cityName: String): Result<List<List<LatLng>>> {
        return runCatching {
            val response = nominatimApi.searchCity(cityName)
            val geometry =
                response.features.firstOrNull()?.geometry ?: return Result.success(emptyList())

            val polygons: List<Polygon> = when (geometry.type) {
                "Polygon" -> listOf(geometry.coordinates as Polygon)
                "MultiPolygon" -> geometry.coordinates as List<Polygon>
                else -> return Result.success(emptyList())
            }

            polygons.mapNotNull { polygon ->
                polygon.firstOrNull()?.map { point ->
                    LatLng(point[1], point[0])
                }
            }
        }
    }

    override suspend fun fetchCities(saveToLocal: suspend (City) -> Unit) {
        JsonDownloadHelper.downloadToFile(
            { cityApiService.fetchCities() },
            context = context
        )

        val file = File(context.cacheDir, "cities.json")

        JsonDownloadHelper.readArrayInChunks<CityDto>(file) { city ->
            val domainCity = city.toCity()
            saveToLocal(domainCity)
        }
    }
}