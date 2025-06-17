package com.flynn.citysearch.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flynn.citysearch.data.repository.CityRepositoryInterface
import com.flynn.citysearch.domain.City
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val cityRepository: CityRepositoryInterface
) : ViewModel() {
    private val _state = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = _state.asStateFlow()

    data class SearchState(
        val isLoading: Boolean = false,
        val searchQuery: String = "",
        val showFavoritesOnly: Boolean = false,
        val cities: List<City> = emptyList(),
        val selectedCity: City? = null,
        val showCityDetail: Boolean = false,
        val errorMessage: String? = null
    )

    init {
        fetchCities()
    }

    private fun fetchCities() {
        viewModelScope.launch(Dispatchers.IO) {
            try { //TODO move this try catch
                val cities = cityRepository.fetchCities()
                dispatch(SearchAction.SetCities(cities))
                filterCities()
            } catch (e: Exception) {
                dispatch(SearchAction.SetError("Failed to load cities: ${e.message}"))
            }
        }
    }

    fun onIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.UpdateSearchQuery -> {
                dispatch(SearchAction.UpdateSearchQuery(intent.query))
                filterCities()
            }

            is SearchIntent.ToggleFavoriteFilter -> {
                dispatch(SearchAction.ToggleFavoriteFilter)
                filterCities()
            }

            is SearchIntent.SelectCity -> {
                dispatch(SearchAction.SelectCity(intent.city))
                dispatch(SearchAction.SetShowCityDetail(true))
            }

            is SearchIntent.ToggleCityFavorite -> {
                cityRepository.toggleFavorite(intent.cityId)
                filterCities()
            }

            is SearchIntent.ClearSelection -> {
                dispatch(SearchAction.SelectCity(null))
                dispatch(SearchAction.SetShowCityDetail(false))
            }
        }
    }

    private fun filterCities() {
        viewModelScope.launch {
            val filteredCities = cityRepository.getFilteredCities(
                prefix = state.value.searchQuery,
                favoritesOnly = state.value.showFavoritesOnly
            )
            dispatch(SearchAction.UpdateFilteredCities(filteredCities))
        }
    }

    private fun dispatch(action: SearchAction) {
        _state.value = reducer(state.value, action)
    }

    private fun reducer(state: SearchState, action: SearchAction): SearchState {
        return when (action) {
            is SearchAction.SetLoading -> state.copy(
                isLoading = action.isLoading
            )

            is SearchAction.SetCities -> state.copy(
                cities = action.cities
            )

            is SearchAction.UpdateSearchQuery -> state.copy(
                searchQuery = action.query
            )

            is SearchAction.ToggleFavoriteFilter -> state.copy(
                showFavoritesOnly = !state.showFavoritesOnly
            )

            is SearchAction.UpdateFilteredCities -> state.copy(
                cities = action.cities
            )

            is SearchAction.SelectCity -> state.copy(
                selectedCity = action.city
            )

            is SearchAction.SetError -> state.copy(
                errorMessage = action.message
            )

            is SearchAction.SetShowCityDetail -> state.copy(
                showCityDetail = action.show
            )
        }
    }
}

sealed class SearchIntent {
    data class UpdateSearchQuery(val query: String) : SearchIntent()
    data object ToggleFavoriteFilter : SearchIntent()
    data class SelectCity(val city: City) : SearchIntent()
    data class ToggleCityFavorite(val cityId: Int) : SearchIntent()
    data object ClearSelection : SearchIntent()
}

sealed class SearchAction {
    data class SetLoading(val isLoading: Boolean) : SearchAction()
    data class SetCities(val cities: List<City>) : SearchAction()
    data class UpdateSearchQuery(val query: String) : SearchAction()
    data object ToggleFavoriteFilter : SearchAction()
    data class UpdateFilteredCities(val cities: List<City>) : SearchAction()
    data class SelectCity(val city: City?) : SearchAction()
    data class SetError(val message: String) : SearchAction()
    data class SetShowCityDetail(val show: Boolean) : SearchAction()
}
