package com.flynn.citysearch.feature.adaptive

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AdaptiveMapViewModelTest {

    private lateinit var viewModel: AdaptiveMapViewModel

    @Before
    fun setup() {
        viewModel = AdaptiveMapViewModel()
    }

    @Test
    fun `onIntent SwitchScreen should update current screen`() = runTest {
        viewModel.onIntent(ContainerIntent.SwitchScreen(AdaptiveMapViewModel.Screen.MAP))
        val state = viewModel.state.value
        assertEquals(AdaptiveMapViewModel.Screen.MAP, state.currentScreen)
    }

    @Test
    fun `onIntent SetMapLoaded should update map loaded flag`() = runTest {
        viewModel.onIntent(ContainerIntent.SetMapLoaded(true))
        val state = viewModel.state.value
        assertTrue(state.isMapAlreadyLoaded)
    }

    @Test
    fun `onIntent UpdateCity should update city value`() = runTest {
        viewModel.onIntent(ContainerIntent.UpdateCity("Buenos Aires"))
        val state = viewModel.state.value
        assertEquals("Buenos Aires", state.city)
    }

    @Test
    fun `reducer should handle SwitchScreen`() {
        val initial =
            AdaptiveMapViewModel.ContainerState(currentScreen = AdaptiveMapViewModel.Screen.SEARCH)
        val result = viewModel.run {
            reducer(initial, ContainerAction.SwitchScreen(AdaptiveMapViewModel.Screen.MAP))
        }
        assertEquals(AdaptiveMapViewModel.Screen.MAP, result.currentScreen)
    }

    @Test
    fun `reducer should handle SetMapLoaded`() {
        val initial = AdaptiveMapViewModel.ContainerState(isMapAlreadyLoaded = false)
        val result = viewModel.run {
            reducer(initial, ContainerAction.SetMapLoaded(true))
        }
        assertTrue(result.isMapAlreadyLoaded)
    }

    @Test
    fun `reducer should handle UpdateCity`() {
        val initial = AdaptiveMapViewModel.ContainerState(city = "Old City")
        val result = viewModel.run {
            reducer(initial, ContainerAction.UpdateCity("New City"))
        }
        assertEquals("New City", result.city)
    }
}
