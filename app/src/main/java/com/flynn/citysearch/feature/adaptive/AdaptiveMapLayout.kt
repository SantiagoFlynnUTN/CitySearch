package com.flynn.citysearch.feature.adaptive

import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.hilt.navigation.compose.hiltViewModel
import com.flynn.citysearch.feature.adaptive.AdaptiveMapViewModel.Screen.MAP
import com.flynn.citysearch.feature.adaptive.AdaptiveMapViewModel.Screen.SEARCH
import com.flynn.citysearch.feature.map.MapScreen
import com.flynn.citysearch.feature.map.MapTopUi
import com.flynn.citysearch.feature.map.MapViewModel
import com.flynn.citysearch.feature.search.SearchScreen
import com.flynn.citysearch.feature.search.SearchViewModel

@Composable
fun AdaptiveMapLayout(viewModel: AdaptiveMapViewModel = hiltViewModel()) {
    val isLandscape = LocalConfiguration.current.orientation == ORIENTATION_LANDSCAPE
    val state = viewModel.state.collectAsState().value
    val currentScreen = state.currentScreen
    val searchViewModel = hiltViewModel<SearchViewModel>()
    val mapViewModel = hiltViewModel<MapViewModel>()

    Row {
        val mapModifier = if (isLandscape) Modifier.weight(0.5f) else Modifier.weight(1f)
        val searchModifier = if (isLandscape) Modifier.weight(0.5f) else Modifier
        Box(modifier = searchModifier)
        MapScreen(modifier = mapModifier, mapViewModel = mapViewModel)
    }

    when {
        isLandscape -> Row(modifier = Modifier.fillMaxSize()) {
            SearchScreen(
                modifier = Modifier.weight(0.5f),
                searchViewModel = searchViewModel
            )
            MapTopUi(
                modifier = Modifier.weight(0.5f),
                mapViewModel = mapViewModel,
                shouldShowTopBar = false
            )
        }

        currentScreen == SEARCH -> SearchScreen(searchViewModel = searchViewModel)

        currentScreen == MAP -> MapTopUi(
            mapViewModel = mapViewModel,
            shouldShowTopBar = true,
            onBackPressed = { viewModel.onIntent(ContainerIntent.SwitchScreen(SEARCH)) },
        )
    }
}
