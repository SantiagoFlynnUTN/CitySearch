package com.flynn.citysearch.navigation.graphs

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.flynn.citysearch.feature.adaptive.AdaptiveMapLayout
import com.flynn.citysearch.navigation.Main

fun NavGraphBuilder.mainSearchMapNavigation() {
    composable<Main> {
        AdaptiveMapLayout()
    }
}