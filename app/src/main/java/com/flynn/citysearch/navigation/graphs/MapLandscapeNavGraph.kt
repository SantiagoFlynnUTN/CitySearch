package com.flynn.citysearch.navigation.graphs

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.flynn.citysearch.feature.map.MapScreen
import com.flynn.citysearch.feature.map.MapViewModel
import com.flynn.citysearch.feature.search.SearchScreen
import com.flynn.citysearch.feature.search.SearchViewModel
import com.flynn.citysearch.navigation.MapMain

fun NavGraphBuilder.mapLandscapeNavigation(provideParentEntry: () -> NavBackStackEntry) {
    composable<MapMain> {
        val parentEntry = remember { provideParentEntry() }
        val mapViewModel = hiltViewModel<MapViewModel>(parentEntry)
        val searchViewModel = hiltViewModel<SearchViewModel>(parentEntry)
        Row {
            SearchScreen(searchViewModel)
            MapScreen(mapViewModel, shouldShowTopBar = false)
        }
    }
}