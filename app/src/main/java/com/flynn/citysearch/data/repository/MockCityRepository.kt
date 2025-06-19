package com.flynn.citysearch.data.repository

import com.flynn.citysearch.data.local.LocalDataSourceImpl
import com.flynn.citysearch.domain.City
import com.flynn.citysearch.domain.Coordinates
import com.flynn.citysearch.domain.Storage
import com.flynn.citysearch.feature.map.model.MapLocation
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Mock implementation of CityRepository for testing purposes.
 * Provides preloaded data and simulates repository functionality without network requests.
 */
@Named("MockCityRepository")
@Singleton
class MockCityRepository @Inject constructor(
    private val localDataSourceImpl: LocalDataSourceImpl
) : CityRepositoryInterface {

    private val favoriteIds = mutableSetOf<Int>()

    private val mockCities = listOf(
        City(1, "New York", "US", Coordinates(-74.0060, 40.7128)),
        City(2, "Los Angeles", "US", Coordinates(-118.2437, 34.0522)),
        City(3, "Chicago", "US", Coordinates(-87.6298, 41.8781)),
        City(4, "Houston", "US", Coordinates(-95.3698, 29.7604)),
        City(5, "Phoenix", "US", Coordinates(-112.0740, 33.4484)),
        City(6, "Philadelphia", "US", Coordinates(-75.1652, 39.9526)),
        City(7, "San Antonio", "US", Coordinates(-98.4936, 29.4241)),
        City(8, "San Diego", "US", Coordinates(-117.1611, 32.7157)),
        City(9, "Dallas", "US", Coordinates(-96.7970, 32.7767)),
        City(10, "San Jose", "US", Coordinates(-121.8863, 37.3382)),
        City(11, "Austin", "US", Coordinates(-97.7431, 30.2672)),
        City(12, "Jacksonville", "US", Coordinates(-81.6557, 30.3322)),
        City(13, "Fort Worth", "US", Coordinates(-97.3208, 32.7555)),
        City(14, "Columbus", "US", Coordinates(-82.9988, 39.9612)),
        City(15, "Indianapolis", "US", Coordinates(-86.1581, 39.7684)),
        City(16, "Charlotte", "US", Coordinates(-80.8431, 35.2271)),
        City(17, "San Francisco", "US", Coordinates(-122.4194, 37.7749)),
        City(18, "Seattle", "US", Coordinates(-122.3321, 47.6062)),
        City(19, "Denver", "US", Coordinates(-104.9903, 39.7392)),
        City(20, "Washington", "US", Coordinates(-77.0369, 38.9072)),
        City(21, "Boston", "US", Coordinates(-71.0589, 42.3601)),
        City(22, "El Paso", "US", Coordinates(-106.4850, 31.7619)),
        City(23, "Nashville", "US", Coordinates(-86.7816, 36.1627)),
        City(24, "Detroit", "US", Coordinates(-83.0458, 42.3314)),
        City(25, "Portland", "US", Coordinates(-122.6784, 45.5152)),
        City(26, "Las Vegas", "US", Coordinates(-115.1391, 36.1716)),
        City(27, "Memphis", "US", Coordinates(-90.0490, 35.1495)),
        City(28, "Louisville", "US", Coordinates(-85.7585, 38.2527)),
        City(29, "Milwaukee", "US", Coordinates(-87.9065, 43.0389)),
        City(30, "Sydney", "AU", Coordinates(151.2093, -33.8688)),
        City(31, "Melbourne", "AU", Coordinates(144.9631, -37.8136)),
        City(32, "Brisbane", "AU", Coordinates(153.0260, -27.4705)),
        City(33, "Perth", "AU", Coordinates(115.8575, -31.9505)),
        City(34, "Adelaide", "AU", Coordinates(138.6007, -34.9285)),
        City(35, "London", "UK", Coordinates(-0.1276, 51.5074)),
        City(36, "Birmingham", "UK", Coordinates(-1.8904, 52.4862)),
        City(37, "Tokyo", "JP", Coordinates(139.6917, 35.6895)),
        City(38, "Osaka", "JP", Coordinates(135.5022, 34.6937)),
        City(39, "Paris", "FR", Coordinates(2.3522, 48.8566)),
        City(40, "Berlin", "DE", Coordinates(13.4050, 52.5200)),
        City(41, "Alabama", "US", Coordinates(-86.9023, 32.3182)),
        City(42, "Albuquerque", "US", Coordinates(-106.6504, 35.0844)),
        City(43, "Arizona", "US", Coordinates(-111.0937, 34.0489)),
        City(44, "Atlanta", "US", Coordinates(-84.3880, 33.7490)),
        City(45, "Albany", "US", Coordinates(-73.7562, 42.6526))
    )

    override suspend fun fetchCities(): Flow<Storage> = flow {
        mockCities.forEach { city ->
            localDataSourceImpl.saveCity(city)
        }
        emit(Storage.INDEXED)
    }

    override suspend fun getFilteredCities(
        prefix: String,
        favoritesOnly: Boolean,
        limit: Int,
        page: Int
    ): List<City> {
        val filter = prefix.lowercase().trim()
        val offset = page * limit

        return mockCities
            .asSequence()
            .map { it.copy(isFavorite = favoriteIds.contains(it.id)) }
            .filter { city ->
                (!favoritesOnly || city.isFavorite) &&
                        (filter.isEmpty() || city.name.lowercase().startsWith(filter))
            }
            .sortedBy { it.displayName }
            .drop(offset)
            .take(limit)
            .toList()
    }

    override fun toggleFavorite(cityId: Int) {
        if (favoriteIds.contains(cityId)) {
            favoriteIds.remove(cityId)
        } else {
            favoriteIds.add(cityId)
        }
    }

    override suspend fun getCityPolygon(cityLocation: MapLocation): Result<List<List<LatLng>>> {
        return Result.success(emptyList())
    }
}
