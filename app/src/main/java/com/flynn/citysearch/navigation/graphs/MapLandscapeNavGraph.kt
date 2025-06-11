package com.flynn.citysearch.navigation.graphs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.flynn.citysearch.navigation.Main

fun NavGraphBuilder.mapLandscapeNavigation() {
    composable<Main> {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color.Green)
        )
    }
}