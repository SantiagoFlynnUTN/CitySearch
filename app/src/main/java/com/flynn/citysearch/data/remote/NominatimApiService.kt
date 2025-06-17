package com.flynn.citysearch.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

interface NominatimApiService {
    @GET("search")
    suspend fun searchCity(
        @Query("q") cityName: String,
        @Query("format") format: String = "json",
        @Query("polygon_geojson") polygon: Int = 1
    ): List<NominatimCityResponse>
}

@JsonClass(generateAdapter = true)
data class NominatimCityResponse(
    @Json(name = "display_name") val displayName: String,
    val lat: String,
    val lon: String,
    val geojson: GeoJson?
)

@JsonClass(generateAdapter = true)
data class GeoJson(
    val type: String,
    val coordinates: List<List<List<Double>>>
)

