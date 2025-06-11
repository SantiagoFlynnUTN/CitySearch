package com.flynn.citysearch.navigation

import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.flynn.citysearch.navigation.graphs.mainSearchMapNavigation

@Composable
fun NavigationHost(){
    val navController = rememberNavController()
    val isLandscape = LocalConfiguration.current.orientation == ORIENTATION_LANDSCAPE

    NavHost(navController = navController, startDestination = Main) {
        mainSearchMapNavigation(isLandscape, navController)
    }
}