package com.flynn.citysearch.data.remote

import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

interface NominatimApiService {
    @GET("search")
    suspend fun searchCity(
        @Query("q") cityName: String,
        @Query("format") format: String = "geojson",
        @Query("polygon_geojson") polygon: Int = 1
    ): FeatureCollection
}

@JsonClass(generateAdapter = true)
data class FeatureCollection(
    val type: String,
    val features: List<Feature>
)

@JsonClass(generateAdapter = true)
data class Feature(
    val type: String,
    val geometry: Geometry,
    val properties: Map<String, Any> = emptyMap()
)

@JsonClass(generateAdapter = true)
data class Geometry(
    val type: String,
    val coordinates: Any
)


