package com.flynn.citysearch.feature.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
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
    modifier: Modifier,
    mapViewModel: MapViewModel = hiltViewModel(),
) {
    val mapState by mapViewModel.state.collectAsState()

    val intentProcessor = mapViewModel::onIntent

    val coroutineScope = rememberCoroutineScope()

    GoogleMap(
        modifier = modifier,
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
}
