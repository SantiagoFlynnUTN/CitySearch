package com.flynn.citysearch.feature.map

import com.flynn.citysearch.MainDispatcherRule
import com.flynn.citysearch.data.repository.CityRepositoryInterface
import com.flynn.citysearch.feature.map.model.MapLocation
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var repository: CityRepositoryInterface
    private lateinit var viewModel: MapViewModel

    @Before
    fun setup() {
        repository = mockk()
        viewModel = MapViewModel(repository)
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `onIntent SetLoading should update loading state`() = runTest {
        viewModel.onIntent(MapIntent.SetLoading(false))
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `onIntent UpdateCameraPosition should update state`() = runTest {
        val position = CameraPositionState(CameraPosition.fromLatLngZoom(LatLng(1.0, 2.0), 10f))
        viewModel.onIntent(MapIntent.UpdateCameraPosition(position))
        assertEquals(position, viewModel.state.value.cameraPositionState)
    }

    @Test
    fun `onIntent UpdateMapProperties should update state`() = runTest {
        val properties = MapProperties(mapType = MapType.SATELLITE, isMyLocationEnabled = true)
        viewModel.onIntent(MapIntent.UpdateMapProperties(properties))
        assertEquals(properties, viewModel.state.value.mapProperties)
    }

    @Test
    fun `onIntent SelectLocation should update location and load polygon success`() = runTest {
        val location = MapLocation("Test", 10.0, 20.0)
        val polygon = listOf(listOf(LatLng(1.0, 2.0), LatLng(3.0, 4.0)))

        coEvery { repository.getCityPolygon(location) } returns Result.success(polygon)

        viewModel.onIntent(MapIntent.SelectLocation(location))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(location, state.selectedLocation)
        assertEquals(polygon, state.polygonPoints)
    }

    @Test
    fun `onIntent SelectLocation should fallback to empty polygon on failure`() = runTest {
        val location = MapLocation("Fail", 10.0, 20.0)

        coEvery { repository.getCityPolygon(location) } returns Result.failure(Throwable("error"))

        viewModel.onIntent(MapIntent.SelectLocation(location))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(location, state.selectedLocation)
        assertTrue(state.polygonPoints.isEmpty())
    }

    @Test
    fun `reducer should handle all actions`() {
        val location = MapLocation("Test", 1.0, 2.0)
        val position = CameraPositionState(CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 5f))
        val properties = MapProperties(mapType = MapType.HYBRID, isMyLocationEnabled = true)
        val polygon = listOf(listOf(LatLng(1.0, 1.0)))

        val state = MapViewModel.MapState()

        with(viewModel) {
            assertEquals(
                location,
                reducer(state, MapAction.SelectLocation(location)).selectedLocation
            )
            assertEquals(false, reducer(state, MapAction.SetLoading(false)).isLoading)
            assertEquals(
                position,
                reducer(state, MapAction.UpdateCameraPosition(position)).cameraPositionState
            )
            assertEquals(
                properties,
                reducer(state, MapAction.UpdateMapProperties(properties)).mapProperties
            )
            assertEquals(
                polygon,
                reducer(state, MapAction.UpdatePolygonPoints(polygon)).polygonPoints
            )
        }
    }
}
