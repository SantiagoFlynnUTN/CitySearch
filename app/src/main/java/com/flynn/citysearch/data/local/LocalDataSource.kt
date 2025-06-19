package com.flynn.citysearch.data.local

import com.flynn.citysearch.domain.City

interface LocalDataSource {

    /**
     * Returns a paginated list of cities that start with the given query.
     *
     * @param query The prefix to filter cities by name.
     * @param limit The max number of results to return.
     * @param page The page index (starting from 0).
     */
    suspend fun filterCities(query: String, limit: Int, page: Int): List<City>

    /**
     * Saves a city into the local database.
     *
     * @param city The city object to be persisted.
     */
    suspend fun saveCity(city: City)

    /**
     * Toggles the favorite status of a city by its ID.
     *
     * @param cityId The unique identifier of the city.
     */
    suspend fun toggleFavorite(cityId: Int)

    /**
     * Returns a paginated list of favorite cities that match the query prefix.
     *
     * @param query The prefix to filter favorite cities by name.
     * @param limit The max number of results to return.
     * @param page The page index (starting from 0).
     */
    suspend fun getFilteredFavoriteCities(query: String, limit: Int, page: Int): List<City>

    /**
     * Returns the total number of cities stored in the local database.
     */
    suspend fun countCities(): Int
}
