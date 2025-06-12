package com.flynn.citysearch.navigation.graphs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.flynn.citysearch.feature.map.MapScreen
import com.flynn.citysearch.feature.map.MapViewModel
import com.flynn.citysearch.navigation.Main
import com.flynn.citysearch.navigation.MapMain
import kotlinx.serialization.Serializable

internal sealed interface MapDirections

@Serializable
internal data object Map : MapDirections

@Serializable
internal data object Search : MapDirections

fun NavGraphBuilder.mapPortraitNavigation(provideParentEntry: () -> NavBackStackEntry) {
    composable<MapMain> {
        val parentEntry = remember { provideParentEntry() }
        val viewModel = hiltViewModel<MapViewModel>(parentEntry)
        val nestedNavController = rememberNavController()
        NavHost(navController = nestedNavController, startDestination = Map) {
            composable<Search> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Blue)
                        .clickable {
                            nestedNavController.navigate(Map)
                        }
                )
            }
            composable<Map> {
                MapScreen(viewModel)
            }
        }
    }
}