package com.flynn.citysearch.data.remote

import com.flynn.citysearch.domain.City
import com.flynn.citysearch.domain.Storage
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow

interface RemoteDataSource {
    /**
     * Fetches polygon data for a city
     * @param cityName The name of the city
     * @return A list of polygons (each a list of LatLng points), or null if not found
     */
    suspend fun getCityPolygons(cityName: String): Result<List<List<LatLng>>>

    /**
     * Fetches all cities from the gist
     * @param localEntries entries qty of the local storage
     * @param saveToLocal A function to save each city to local storage
     */
    suspend fun fetchCities(localEntries: Int, saveToLocal: suspend (City) -> Unit): Flow<Storage>
}