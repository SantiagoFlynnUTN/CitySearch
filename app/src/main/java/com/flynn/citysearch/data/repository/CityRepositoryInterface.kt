package com.flynn.citysearch.data.repository

import com.flynn.citysearch.domain.City

interface CityRepositoryInterface {
    /**
     * Fetches all cities from the data source
     */
    suspend fun fetchCities(): List<City>

    /**
     * Returns filtered list of cities based on prefix and favorites
     * @param prefix The prefix to filter by (e.g., "A" to get cities starting with A)
     * @param favoritesOnly If true, only return favorite cities
     */
    suspend fun getFilteredCities(prefix: String, favoritesOnly: Boolean): List<City>

    /**
     * Toggles the favorite status of a city
     * @param cityId The ID of the city to toggle
     */
    fun toggleFavorite(cityId: Int)
}