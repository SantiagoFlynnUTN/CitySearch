package com.flynn.citysearch.feature.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun MapTopUi(
    modifier: Modifier = Modifier,
    mapViewModel: MapViewModel = hiltViewModel(),
    shouldShowTopBar: Boolean,
    onBackPressed: () -> Unit = {}
) {
    val mapState by mapViewModel.state.collectAsState()

    MapTopUiContent(modifier, shouldShowTopBar, onBackPressed, mapState)
}

@Composable
private fun MapTopUiContent(
    modifier: Modifier,
    shouldShowTopBar: Boolean,
    onBackPressed: () -> Unit,
    mapState: MapViewModel.MapState
) {
    Column(modifier = modifier.fillMaxSize()) {
        if (shouldShowTopBar) {
            MapTopBar(onBackPressed)
        }

        if (mapState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapTopBar(onBackPressed: () -> Unit) {
    TopAppBar(
        title = { Text("Volver") },
        navigationIcon = {
            IconButton(onClick = onBackPressed) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun PreviewMapTopUiContent() {
    MapTopUiContent(
        modifier = Modifier,
        shouldShowTopBar = true,
        onBackPressed = {},
        mapState = MapViewModel.MapState(isLoading = true)
    )
}
