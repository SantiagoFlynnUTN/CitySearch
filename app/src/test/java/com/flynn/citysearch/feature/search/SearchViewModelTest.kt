package com.flynn.citysearch.feature.search

import com.flynn.citysearch.MainDispatcherRule
import com.flynn.citysearch.data.repository.CityRepositoryInterface
import com.flynn.citysearch.domain.City
import com.flynn.citysearch.domain.Coordinates
import com.flynn.citysearch.domain.Storage
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SearchViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var repository: CityRepositoryInterface
    private lateinit var viewModel: SearchViewModel

    @Before
    fun setup() {
        repository = mockk(relaxed = true)

        coEvery { repository.fetchCities() } returns flowOf(Storage.DOWNLOADED)

        viewModel = SearchViewModel(repository, dispatcherRule.testDispatcher)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `initial state should trigger fetch and not be loading after DOWNLOADED`() = runTest {
        val state = viewModel.state.first()
        assertFalse(state.isLoading)
    }

    @Test
    fun `on UpdateSearchQuery should update query and call getFilteredCities`() = runTest {
        val mockCities = listOf(City(id = 1, name = "Test", country = "AR", Coordinates(0.0, 0.0)))
        coEvery {
            repository.getFilteredCities(any(), any(), any(), any())
        } returns mockCities

        viewModel.onIntent(SearchIntent.UpdateSearchQuery("Te"))

        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("Te", state.searchQuery)
        assertEquals(mockCities, state.cities)
    }

    @Test
    fun `on ToggleFavoriteFilter should flip state and call getFilteredCities`() = runTest {
        val mockCities = listOf(City(id = 1, name = "Fav", country = "AR", Coordinates(0.0, 0.0)))
        coEvery {
            repository.getFilteredCities(any(), any(), any(), any())
        } returns mockCities

        viewModel.onIntent(SearchIntent.ToggleFavoriteFilter)

        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.showFavoritesOnly)
        assertEquals(mockCities, state.cities)
    }

    @Test
    fun `on ToggleCityFavorite should update city state`() = runTest {
        val city =
            City(id = 1, name = "City", country = "AR", Coordinates(0.0, 0.0), isFavorite = false)
        coEvery { repository.getFilteredCities(any(), any(), any(), any()) } returns listOf(city)

        viewModel.onIntent(SearchIntent.UpdateSearchQuery("City"))
        advanceUntilIdle()

        viewModel.onIntent(SearchIntent.ToggleCityFavorite(city.id))
        advanceUntilIdle()

        val updatedList = viewModel.state.value.cities
        assertEquals(updatedList[0].isFavorite, true)
    }

    @Test
    fun `on SelectCity should update selectedCity and show detail`() = runTest {
        val city = City(1, "A", "AR", Coordinates(0.0, 0.0))
        viewModel.onIntent(SearchIntent.SelectCity(city))
        val state = viewModel.state.value
        assertEquals(city, state.selectedCity)
        assertTrue(state.showCityDetail)
    }

    @Test
    fun `on ClearSelection should reset selectedCity and hide detail`() = runTest {
        val city = City(1, "A", "AR", Coordinates(0.0, 0.0))
        viewModel.onIntent(SearchIntent.SelectCity(city))
        viewModel.onIntent(SearchIntent.ClearSelection)
        val state = viewModel.state.value
        assertNull(state.selectedCity)
        assertFalse(state.showCityDetail)
    }

    @Test
    fun `on LoadMore should append more cities and increment page`() = runTest {
        val initialCities = List(20) { City(it, "C$it", "AR", Coordinates(0.0, 0.0)) }
        val nextCities = List(5) { City(it + 20, "C${it + 20}", "AR", Coordinates(0.0, 0.0)) }

        coEvery { repository.getFilteredCities(any(), any(), any(), eq(0)) } returns initialCities
        coEvery { repository.getFilteredCities(any(), any(), any(), eq(1)) } returns nextCities

        viewModel.onIntent(SearchIntent.UpdateSearchQuery("C"))
        advanceUntilIdle()

        viewModel.onIntent(SearchIntent.LoadMore)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(25, state.cities.size)
        assertEquals(1, state.paginationState.currentPage)
    }

    @Test
    fun `tryLoadCitiesIfEmpty should trigger filtering if empty`() = runTest {
        coEvery { repository.getFilteredCities(any(), any(), any(), any()) } returns emptyList()

        viewModel.onIntent(SearchIntent.UpdateSearchQuery("X"))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.cities.isEmpty())
    }

    @Test
    fun `reducer ToggleFavoriteFilter flips the value`() {
        val initial = SearchViewModel.SearchState(showFavoritesOnly = false)
        val result = viewModel.run {
            reducer(initial, SearchAction.ToggleFavoriteFilter)
        }
        assertTrue(result.showFavoritesOnly)
    }

    @Test
    fun `reducer SetLoading updates loading state`() {
        val initial = SearchViewModel.SearchState(isLoading = false)
        val result = viewModel.run {
            reducer(initial, SearchAction.SetLoading(true))
        }
        assertTrue(result.isLoading)
    }

    @Test
    fun `reducer SetIndexing updates indexing state`() {
        val initial = SearchViewModel.SearchState(isIndexing = false)
        val result = viewModel.run {
            reducer(initial, SearchAction.SetIndexing(true))
        }
        assertTrue(result.isIndexing)
    }

    @Test
    fun `reducer SetShowCityDetail toggles detail visibility`() {
        val initial = SearchViewModel.SearchState(showCityDetail = false)
        val result = viewModel.run {
            reducer(initial, SearchAction.SetShowCityDetail(true))
        }
        assertTrue(result.showCityDetail)
    }

    @Test
    fun `reducer SetLoadingMore updates pagination state`() {
        val initial = SearchViewModel.SearchState(
            paginationState = SearchViewModel.PaginationState(isLoadingMore = false)
        )
        val result = viewModel.run {
            reducer(initial, SearchAction.SetLoadingMore(true))
        }
        assertTrue(result.paginationState.isLoadingMore)
    }

    @Test
    fun `reducer SetCanLoadMore updates pagination state`() {
        val initial = SearchViewModel.SearchState(
            paginationState = SearchViewModel.PaginationState(canLoadMore = false)
        )
        val result = viewModel.run {
            reducer(initial, SearchAction.SetCanLoadMore(true))
        }
        assertTrue(result.paginationState.canLoadMore)
    }

    @Test
    fun `reducer IncrementPage increases currentPage`() {
        val initial = SearchViewModel.SearchState(
            paginationState = SearchViewModel.PaginationState(currentPage = 1)
        )
        val result = viewModel.run {
            reducer(initial, SearchAction.IncrementPage)
        }
        assertEquals(2, result.paginationState.currentPage)
    }

    @Test
    fun `reducer ResetPage sets currentPage to zero`() {
        val initial = SearchViewModel.SearchState(
            paginationState = SearchViewModel.PaginationState(currentPage = 5)
        )
        val result = viewModel.run {
            reducer(initial, SearchAction.ResetPage)
        }
        assertEquals(0, result.paginationState.currentPage)
    }

    @Test
    fun `reducer AppendCities appends to existing city list`() {
        val existing = listOf(City(1, "A", "AR", Coordinates(0.0, 0.0)))
        val more = listOf(City(2, "B", "AR", Coordinates(0.0, 0.0)))

        val initial = SearchViewModel.SearchState(cities = existing)
        val result = viewModel.run {
            reducer(initial, SearchAction.AppendCities(more))
        }
        assertEquals(2, result.cities.size)
    }

}
