package com.flynn.citysearch.feature.map.model

data class MapLocation(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val description: String = ""
)