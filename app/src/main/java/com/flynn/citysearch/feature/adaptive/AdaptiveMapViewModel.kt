package com.flynn.citysearch.feature.adaptive

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AdaptiveMapViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow(ContainerState())
    val state: StateFlow<ContainerState> = _state.asStateFlow()

    data class ContainerState(
        val currentScreen: Screen = Screen.SEARCH,
        val city: String = "New York City"
    )

    enum class Screen {
        MAP, SEARCH
    }

    fun onIntent(intent: ContainerIntent) {
        when (intent) {
            is ContainerIntent.SwitchScreen -> {
                dispatch(ContainerAction.SwitchScreen(intent.screen))
            }

            is ContainerIntent.UpdateCity -> {
                dispatch(ContainerAction.UpdateCity(intent.city))
            }
        }
    }

    private fun dispatch(action: ContainerAction) {
        _state.value = reducer(_state.value, action)
    }

    private fun reducer(state: ContainerState, action: ContainerAction): ContainerState {
        return when (action) {
            is ContainerAction.SwitchScreen -> state.copy(
                currentScreen = action.screen
            )

            is ContainerAction.UpdateCity -> state.copy(
                city = action.city
            )
        }
    }
}

sealed class ContainerIntent {
    data class SwitchScreen(val screen: AdaptiveMapViewModel.Screen) : ContainerIntent()
    data class UpdateCity(val city: String) : ContainerIntent()
}

sealed class ContainerAction {
    data class SwitchScreen(val screen: AdaptiveMapViewModel.Screen) : ContainerAction()
    data class UpdateCity(val city: String) : ContainerAction()
}
