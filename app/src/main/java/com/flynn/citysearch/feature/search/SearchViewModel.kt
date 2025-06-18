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

    companion object {
        private const val PAGE_SIZE = 20
    }

    data class PaginationState(
        val currentPage: Int = 0,
        val isLoadingMore: Boolean = false,
        val canLoadMore: Boolean = true
    )

    data class SearchState(
        val isLoading: Boolean = false,
        val searchQuery: String = "",
        val showFavoritesOnly: Boolean = false,
        val cities: List<City> = emptyList(),
        val selectedCity: City? = null,
        val showCityDetail: Boolean = false,
        val errorMessage: String? = null,
        val paginationState: PaginationState = PaginationState()
    )

    init {
        fetchCities()
    }

    private fun fetchCities() {
        viewModelScope.launch(Dispatchers.IO) {
            cityRepository.fetchCities()
            filterCities()
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
                filterCities(false)
            }

            is SearchIntent.ClearSelection -> {
                dispatch(SearchAction.SelectCity(null))
                dispatch(SearchAction.SetShowCityDetail(false))
            }

            is SearchIntent.LoadMore -> {
                loadMoreCities()
            }
        }
    }

    private fun filterCities(resetPage: Boolean = true) {
        viewModelScope.launch {
            if (resetPage) {
                dispatch(SearchAction.ResetPage)
                dispatch(SearchAction.SetLoading(true))
                val filteredCities = cityRepository.getFilteredCities(
                    prefix = state.value.searchQuery,
                    favoritesOnly = state.value.showFavoritesOnly,
                    limit = PAGE_SIZE,
                    page = 0
                )
                dispatch(SearchAction.UpdateFilteredCities(filteredCities))
                dispatch(SearchAction.SetCanLoadMore(filteredCities.size == PAGE_SIZE))
                dispatch(SearchAction.SetLoading(false))

            } else {
                loadMoreCities()
            }
        }
    }

    private fun loadMoreCities() {
        if (state.value.paginationState.isLoadingMore || !state.value.paginationState.canLoadMore) return

        viewModelScope.launch {
            dispatch(SearchAction.SetLoadingMore(true))
            val nextPage = state.value.paginationState.currentPage + 1
            val moreCities = cityRepository.getFilteredCities(
                prefix = state.value.searchQuery,
                favoritesOnly = state.value.showFavoritesOnly,
                limit = PAGE_SIZE,
                page = nextPage
            )

            if (moreCities.isNotEmpty()) {
                dispatch(SearchAction.AppendCities(moreCities))
                dispatch(SearchAction.IncrementPage)
                dispatch(SearchAction.SetCanLoadMore(moreCities.size == PAGE_SIZE))
            } else {
                dispatch(SearchAction.SetCanLoadMore(false))
            }

            dispatch(SearchAction.SetLoadingMore(false))

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

            is SearchAction.SetLoadingMore -> state.copy(
                paginationState = state.paginationState.copy(isLoadingMore = action.isLoading)
            )

            is SearchAction.SetCanLoadMore -> state.copy(
                paginationState = state.paginationState.copy(canLoadMore = action.canLoadMore)
            )

            is SearchAction.IncrementPage -> state.copy(
                paginationState = state.paginationState.copy(currentPage = state.paginationState.currentPage + 1)
            )

            is SearchAction.ResetPage -> state.copy(
                paginationState = state.paginationState.copy(currentPage = 0)
            )

            is SearchAction.AppendCities -> state.copy(
                cities = state.cities + action.cities
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
    data object LoadMore : SearchIntent()
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
    data class SetLoadingMore(val isLoading: Boolean) : SearchAction()
    data class SetCanLoadMore(val canLoadMore: Boolean) : SearchAction()
    data object IncrementPage : SearchAction()
    data object ResetPage : SearchAction()
    data class AppendCities(val cities: List<City>) : SearchAction()
}
