package com.flynn.citysearch.feature.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import kotlinx.coroutines.launch

@Composable
fun MapScreen(mapViewModel: MapViewModel) {

    val mapState by mapViewModel.state.collectAsState()

    val intentProcessor = mapViewModel::onIntent

    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
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
            Marker(
                state = MarkerState(position = position),
                title = location.name,
                snippet = location.description,
                onClick = {
                    intentProcessor(MapIntent.SelectLocation(location))
                    true
                }
            )
        }

        if (mapState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}
