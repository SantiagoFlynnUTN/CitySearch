package com.flynn.citysearch.feature.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flynn.citysearch.data.repository.CityRepositoryInterface
import com.flynn.citysearch.feature.map.model.MapLocation
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val cityRepository: CityRepositoryInterface
) : ViewModel() {

    private val _state = MutableStateFlow(MapState())
    val state: StateFlow<MapState> = _state.asStateFlow()

    data class MapState(
        val isLoading: Boolean = true,
        val selectedLocation: MapLocation = defaultLocation,
        val cameraPositionState: CameraPositionState = CameraPositionState(
            position = CameraPosition.fromLatLngZoom(
                LatLng(defaultLocation.latitude, defaultLocation.longitude),
                12f
            )
        ),
        val mapProperties: MapProperties = MapProperties(
            mapType = MapType.NORMAL,
            isMyLocationEnabled = false
        ),
        val uiSettings: MapUiSettings = MapUiSettings(
            zoomControlsEnabled = true,
            myLocationButtonEnabled = true,
            mapToolbarEnabled = true,
            compassEnabled = true,
            rotationGesturesEnabled = true,
        ),
        val polygonPoints: List<List<LatLng>> = emptyList()
    )

    companion object {
        val defaultLocation = MapLocation(
            name = "New York City",
            latitude = 40.7128,
            longitude = -74.0060,
            description = "The Big Apple"
        )
    }

    fun onIntent(intent: MapIntent) {
        when (intent) {
            is MapIntent.SelectLocation -> {
                onSelectLocation(intent.location)
            }

            is MapIntent.SetLoading -> {
                dispatch(MapAction.SetLoading(intent.isLoading))
            }

            is MapIntent.UpdateCameraPosition -> {
                dispatch(MapAction.UpdateCameraPosition(intent.cameraPositionState))
            }

            is MapIntent.UpdateMapProperties -> {
                dispatch(MapAction.UpdateMapProperties(intent.mapProperties))
            }

            is MapIntent.UpdateUiSettings -> {
                dispatch(MapAction.UpdateUiSettings(intent.uiSettings))
            }
        }
    }

    private fun onSelectLocation(location: MapLocation) {
        viewModelScope.launch {
            dispatch(MapAction.SelectLocation(location))

            cityRepository.getCityPolygon(location)
                .onSuccess { data ->
                    dispatch(MapAction.UpdatePolygonPoints(data))
                }.onFailure {
                    dispatch(MapAction.UpdatePolygonPoints(emptyList()))
                }
        }
    }


    private fun dispatch(action: MapAction) {
        _state.value = reducer(_state.value, action)
    }

    private fun reducer(state: MapState, action: MapAction): MapState {
        return when (action) {
            is MapAction.SelectLocation -> state.copy(
                selectedLocation = action.location
            )

            is MapAction.SetLoading -> state.copy(
                isLoading = action.isLoading
            )

            is MapAction.UpdateCameraPosition -> state.copy(
                cameraPositionState = action.cameraPositionState
            )

            is MapAction.UpdateMapProperties -> state.copy(
                mapProperties = action.mapProperties
            )

            is MapAction.UpdateUiSettings -> state.copy(
                uiSettings = action.uiSettings
            )

            is MapAction.UpdatePolygonPoints -> state.copy(
                polygonPoints = action.points
            )
        }
    }
}

sealed class MapIntent {
    data class SelectLocation(val location: MapLocation) : MapIntent()
    data class SetLoading(val isLoading: Boolean) : MapIntent()
    data class UpdateCameraPosition(val cameraPositionState: CameraPositionState) : MapIntent()
    data class UpdateMapProperties(val mapProperties: MapProperties) : MapIntent()
    data class UpdateUiSettings(val uiSettings: MapUiSettings) : MapIntent()
}

internal sealed class MapAction {
    data class SelectLocation(val location: MapLocation) : MapAction()
    data class SetLoading(val isLoading: Boolean) : MapAction()
    data class UpdateCameraPosition(val cameraPositionState: CameraPositionState) : MapAction()
    data class UpdateMapProperties(val mapProperties: MapProperties) : MapAction()
    data class UpdateUiSettings(val uiSettings: MapUiSettings) : MapAction()
    data class UpdatePolygonPoints(val points: List<List<LatLng>>) : MapAction()
}
