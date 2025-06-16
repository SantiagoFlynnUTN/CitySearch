package com.flynn.citysearch.domain

import kotlinx.serialization.Serializable

@Serializable
data class City(
    val id: Int,
    val name: String,
    val country: String,
    val coordinates: Coordinates,
    var isFavorite: Boolean = false
) {
    val displayName: String
        get() = "$name, $country"
}

@Serializable
data class Coordinates(
    val longitude: Double,
    val latitude: Double
) {
    override fun toString(): String = "lat: $latitude, lon: $longitude"
}