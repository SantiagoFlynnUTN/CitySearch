package com.flynn.citysearch.feature.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.launch

@Composable
fun MapScreen(
    mapViewModel: MapViewModel = hiltViewModel(),
    shouldShowTopBar: Boolean,
    onBackPressed: () -> Unit = {},
) {

    val mapState by mapViewModel.state.collectAsState()

    val intentProcessor = mapViewModel::onIntent

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = mapTopBar(shouldShowTopBar, onBackPressed)
    ) { paddingValues ->
        GoogleMap(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            cameraPositionState = mapState.cameraPositionState,
            properties = mapState.mapProperties,
            uiSettings = mapState.uiSettings,
            onMapLoaded = {
                intentProcessor(MapIntent.SetLoading(false))

                coroutineScope.launch {
                    val locationLatLng = LatLng(
                        mapState.selectedLocation.latitude,
                        mapState.selectedLocation.longitude
                    )
                    mapState.cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(locationLatLng, 12f),
                        1000
                    )
                }
            }
        ) {
            val location = mapState.selectedLocation
            val position = LatLng(location.latitude, location.longitude)
            val markerState = rememberMarkerState(position = position)

            LaunchedEffect(location) {
                markerState.position = position
            }

            Marker(
                state = markerState,
                title = location.name,
                snippet = location.description,
                onClick = {
                    true
                }
            )
        }

        if (mapState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
fun mapTopBar(shouldShowTopBar: Boolean, onBackPressed: () -> Unit): @Composable () -> Unit {
    return if (shouldShowTopBar) {
        {
            TopAppBar(
                title = { Text("Volver") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    } else {
        {}
    }
}

