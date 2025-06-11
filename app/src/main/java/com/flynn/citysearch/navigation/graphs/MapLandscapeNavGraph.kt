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
import androidx.navigation.compose.composable
import com.flynn.citysearch.feature.map.MapViewModel
import com.flynn.citysearch.navigation.Main
import com.flynn.citysearch.navigation.MapMain

fun NavGraphBuilder.mapLandscapeNavigation(provideParentEntry: () -> NavBackStackEntry) {
    composable<MapMain> {
        val parentEntry = remember { provideParentEntry() }
        val viewModel = hiltViewModel<MapViewModel>(parentEntry)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Green)
                .clickable {
                    viewModel.count.intValue += 1
                    println("Count: ${viewModel.count.intValue}")
                }
        )
    }
}