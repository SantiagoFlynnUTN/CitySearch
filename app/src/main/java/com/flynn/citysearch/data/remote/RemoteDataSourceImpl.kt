package com.flynn.citysearch.data.remote

import android.content.Context
import com.flynn.citysearch.core.utils.JsonDownloadHelper
import com.flynn.citysearch.domain.City
import com.flynn.citysearch.domain.Storage
import com.flynn.citysearch.domain.Storage.DOWNLOADED
import com.flynn.citysearch.domain.Storage.INDEXED
import com.flynn.citysearch.domain.Storage.INDEXING
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
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
            val geometry = response.features.firstOrNull {
                it.geometry.type == "Polygon" || it.geometry.type == "MultiPolygon"
            }?.geometry ?: return Result.success(emptyList())

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

    override suspend fun fetchCities(
        localEntries: Int,
        saveToLocal: suspend (City) -> Unit
    ) = flow {
        val fileName = "cities.json"
        val file = File(context.cacheDir, fileName)
        if (!file.exists()) {
            emit(Storage.DOWNLOADING)
        } else {
            emit(DOWNLOADED)
        }

        val shouldResumeProcessing = JsonDownloadHelper.downloadToFile(
            fileName,
            { cityApiService.fetchCities() },
            context = context
        )?.let {
            emit(DOWNLOADED)
            emitAll(persistCities(it, saveToLocal))
            false
        } ?: checkLocalEntries(localEntries, file)

        if (shouldResumeProcessing) {
            emitAll(persistCities(file, saveToLocal))
        }
    }

    private fun persistCities(
        file: File,
        saveToLocal: suspend (City) -> Unit
    ) = flow {
        emit(INDEXING)
        JsonDownloadHelper.readArrayInChunks<CityDto>(file = file) { city ->
            val domainCity = city.toCity()
            saveToLocal(domainCity)
        }
        emit(INDEXED)
    }

    private suspend fun checkLocalEntries(localEntries: Int, file: File): Boolean {
        val entries =
            JsonDownloadHelper.readArrayInChunks<CityDto>(countOnly = true, file = file) { _ -> }
        return entries != localEntries
    }
}