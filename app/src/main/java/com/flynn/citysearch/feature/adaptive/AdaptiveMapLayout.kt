package com.flynn.citysearch.feature.adaptive

import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.hilt.navigation.compose.hiltViewModel
import com.flynn.citysearch.domain.City
import com.flynn.citysearch.feature.adaptive.AdaptiveMapViewModel.Screen.MAP
import com.flynn.citysearch.feature.adaptive.AdaptiveMapViewModel.Screen.SEARCH
import com.flynn.citysearch.feature.map.MapIntent
import com.flynn.citysearch.feature.map.MapScreen
import com.flynn.citysearch.feature.map.MapTopUi
import com.flynn.citysearch.feature.map.MapViewModel
import com.flynn.citysearch.feature.map.model.MapLocation
import com.flynn.citysearch.feature.search.SearchScreen

@Composable
fun AdaptiveMapLayout(viewModel: AdaptiveMapViewModel = hiltViewModel()) {
    val isLandscape = LocalConfiguration.current.orientation == ORIENTATION_LANDSCAPE
    val state = viewModel.state.collectAsState().value
    val intentProcessor = viewModel::onIntent
    val currentScreen = state.currentScreen
    val mapViewModel = hiltViewModel<MapViewModel>()
    val mapIntentProcessor = mapViewModel::onIntent

    val onCitySelected: (City) -> Unit = { city ->
        val mapLocation = MapLocation(
            name = city.displayName,
            latitude = city.coordinates.latitude,
            longitude = city.coordinates.longitude,
            description = city.country
        )
        mapIntentProcessor(MapIntent.SelectLocation(mapLocation))
        intentProcessor(ContainerIntent.SwitchScreen(MAP))
    }

    LaunchedEffect(state.currentScreen) {
        if (state.currentScreen == MAP || isLandscape) {
            intentProcessor(ContainerIntent.SetMapLoaded(true))
        }
    }

    if (state.isMapAlreadyLoaded) {
        Row {
            val mapModifier = if (isLandscape) Modifier.weight(0.5f) else Modifier.weight(1f)
            val searchModifier = if (isLandscape) Modifier.weight(0.5f) else Modifier
            Box(modifier = searchModifier)
            MapScreen(modifier = mapModifier)
        }
    }

    when {
        isLandscape -> Row(modifier = Modifier.fillMaxSize()) {
            SearchScreen(
                modifier = Modifier.weight(0.5f),
                onCitySelected = onCitySelected
            )
            MapTopUi(
                modifier = Modifier.weight(0.5f),
                shouldShowTopBar = false
            )
        }

        currentScreen == SEARCH -> SearchScreen(
            onCitySelected = onCitySelected
        )

        currentScreen == MAP -> MapTopUi(
            shouldShowTopBar = true,
            onBackPressed = {
                intentProcessor(
                    ContainerIntent.SwitchScreen(
                        SEARCH
                    )
                )
            },
        )
    }
}
