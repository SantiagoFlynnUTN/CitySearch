package com.flynn.citysearch.data.remote

import com.flynn.citysearch.domain.City
import com.flynn.citysearch.domain.Coordinates
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Streaming

interface CityApiService {
    @GET("cities.json")
    @Streaming
    suspend fun fetchCities(): Response<ResponseBody>
}

@JsonClass(generateAdapter = true)
data class CityDto(
    @Json(name = "_id") val id: Int,
    val name: String,
    val country: String,
    val coord: CoordinatesDto
)

@JsonClass(generateAdapter = true)
data class CoordinatesDto(
    @Json(name = "lon") val longitude: Double,
    @Json(name = "lat") val latitude: Double
)

fun List<CityDto>.toCities() = map { it.toCity() }

fun CityDto.toCity() = City(
    id = id,
    name = name,
    country = country,
    coordinates = Coordinates(
        longitude = coord.longitude,
        latitude = coord.latitude
    )
)