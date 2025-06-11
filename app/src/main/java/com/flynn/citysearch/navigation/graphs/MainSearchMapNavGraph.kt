package com.flynn.citysearch.navigation.graphs

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.navigation
import com.flynn.citysearch.navigation.Main
import com.flynn.citysearch.navigation.MapMain

fun NavGraphBuilder.mainSearchMapNavigation(isLandscape: Boolean, navController: NavHostController) {
    navigation<Main>(startDestination = MapMain) {
        val provideParentEntry = { navController.getBackStackEntry<Main>()}
        if(isLandscape){
            mapLandscapeNavigation(provideParentEntry)
        } else {
            mapPortraitNavigation(provideParentEntry)
        }
    }
}