package com.flynn.citysearch.data.repository

import com.flynn.citysearch.domain.City
import com.flynn.citysearch.feature.map.model.MapLocation
import com.google.android.gms.maps.model.LatLng

interface CityRepositoryInterface {
    /**
     * Fetches all cities from the data source
     */
    suspend fun fetchCities()

    /**
     * Returns filtered list of cities based on prefix and favorites with pagination
     * @param prefix The prefix to filter by (e.g., "A" to get cities starting with A)
     * @param favoritesOnly If true, only return favorite cities
     * @param limit Number of items to fetch per page
     * @param page Page number (0-indexed)
     */
    suspend fun getFilteredCities(
        prefix: String,
        favoritesOnly: Boolean,
        limit: Int,
        page: Int
    ): List<City>

    /**
     * Toggles the favorite status of a city
     * @param cityId The ID of the city to toggle
     */
    fun toggleFavorite(cityId: Int)

    /**
     * Returns polygon data for a city
     * @param cityName The name of the city to get polygon data for
     * @return A list of polygons, each represented as a list of LatLng points
     */
    suspend fun getCityPolygon(cityLocation: MapLocation): Result<List<List<LatLng>>>
}
