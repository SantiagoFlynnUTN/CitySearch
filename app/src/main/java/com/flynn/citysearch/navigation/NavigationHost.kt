package com.flynn.citysearch.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.flynn.citysearch.navigation.graphs.mainSearchMapNavigation

@Composable
fun NavigationHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Main) {
        mainSearchMapNavigation()
    }
}