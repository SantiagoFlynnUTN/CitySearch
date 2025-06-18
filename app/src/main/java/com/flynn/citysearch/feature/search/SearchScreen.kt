package com.flynn.citysearch.feature.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.flynn.citysearch.domain.City
import com.flynn.citysearch.domain.Coordinates
import com.flynn.citysearch.feature.search.SearchViewModel.PaginationState
import com.flynn.citysearch.feature.search.SearchViewModel.SearchState
import com.flynn.citysearch.ui.theme.Favorite

@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    searchViewModel: SearchViewModel = hiltViewModel(),
    onBackPressed: () -> Unit = {},
    onCitySelected: (City) -> Unit = {}
) {
    val state by searchViewModel.state.collectAsState()
    val intentProcessor = searchViewModel::onIntent

    SearchScreenContent(
        modifier = modifier,
        state = state,
        onBackPressed = onBackPressed,
        onCitySelected = onCitySelected,
        intentProcessor = intentProcessor
    )
}

@Composable
fun SearchScreenContent(
    modifier: Modifier,
    state: SearchState,
    onBackPressed: () -> Unit,
    onCitySelected: (City) -> Unit,
    intentProcessor: (SearchIntent) -> Unit
) {
    if (state.showCityDetail && state.selectedCity != null) {
        CityDetailScreen(
            city = state.selectedCity,
            onBackPressed = { intentProcessor(SearchIntent.ClearSelection) },
            onFavoriteToggle = {
                intentProcessor(SearchIntent.ToggleCityFavorite(state.selectedCity.id))
            }
        )
    } else {
        MainSearchScaffold(
            modifier = modifier,
            state = state,
            onBackPressed = onBackPressed,
            onCitySelected = onCitySelected,
            intentProcessor = intentProcessor
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainSearchScaffold(
    modifier: Modifier,
    state: SearchState,
    onBackPressed: () -> Unit,
    onCitySelected: (City) -> Unit,
    intentProcessor: (SearchIntent) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("City Search") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            SearchBox(state = state, intentProcessor = intentProcessor)
            Spacer(modifier = Modifier.height(4.dp))
            CityList(
                state = state,
                onCitySelected = onCitySelected,
                intentProcessor = intentProcessor
            )
        }
    }
}

@Composable
fun CityList(
    state: SearchState,
    onCitySelected: (City) -> Unit,
    intentProcessor: (SearchIntent) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            BookmarkedCitiesChip(state = state, intentProcessor = intentProcessor)
            Spacer(modifier = Modifier.height(16.dp))
        }
        when {
            state.isLoading -> {
                item {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            state.cities.isEmpty() -> {
                item {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No cities found",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            else -> {
                itemsIndexed(items = state.cities, key = { _, city -> city.id }) { index, city ->
                    CityItem(
                        city = city,
                        onCityClick = { onCitySelected(city) },
                        onInfoClick = { intentProcessor(SearchIntent.SelectCity(city)) },
                        onFavoriteToggle = {
                            intentProcessor(
                                SearchIntent.ToggleCityFavorite(
                                    city.id
                                )
                            )
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (shouldLoadMore(index, state)) {
                        intentProcessor(SearchIntent.LoadMore)
                    }
                }

                if (state.paginationState.isLoadingMore) {
                    item {
                        LoadingMoreItem()
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingMoreItem() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun BookmarkedCitiesChip(state: SearchState, intentProcessor: (SearchIntent) -> Unit) {
    FilterChip(
        selected = state.showFavoritesOnly,
        onClick = { intentProcessor(SearchIntent.ToggleFavoriteFilter) },
        label = { Text("Favorites Only") },
        leadingIcon = {
            Icon(
                imageVector = if (state.showFavoritesOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Favorites filter",
                tint = if (state.showFavoritesOnly) Favorite else MaterialTheme.colorScheme.onSurface
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedLeadingIconColor = if (state.showFavoritesOnly) Favorite else MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@Composable
fun SearchBox(state: SearchState, intentProcessor: (SearchIntent) -> Unit) {
    OutlinedTextField(
        value = state.searchQuery,
        onValueChange = { intentProcessor(SearchIntent.UpdateSearchQuery(it)) },
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        placeholder = { Text("Search cities...") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.primary
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun CityItem(
    city: City,
    onCityClick: () -> Unit,
    onInfoClick: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCityClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = city.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                IconButton(onClick = onFavoriteToggle) {
                    Icon(
                        imageVector = if (city.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Toggle Favorite",
                        tint = if (city.isFavorite) Favorite else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = city.country,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(
                    onClick = onInfoClick,
                    modifier = Modifier.padding(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "City Info",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

private fun shouldLoadMore(index: Int, state: SearchState): Boolean {
    return index >= state.cities.lastIndex - LoadMoreThreshold &&
            state.paginationState.canLoadMore && !state.paginationState.isLoadingMore
}

private const val LoadMoreThreshold = 5

@Preview(showBackground = true)
@Composable
fun SearchScreenContentPreview() {
    val mockCities = listOf(
        City(
            id = 1,
            name = "Buenos Aires",
            country = "Argentina",
            coordinates = Coordinates(longitude = -58.3816, latitude = -34.6037),
            isFavorite = true
        ),
        City(
            id = 2,
            name = "New York",
            country = "USA",
            coordinates = Coordinates(longitude = -74.0060, latitude = 40.7128),
            isFavorite = false
        )
    )

    val mockState = SearchState(
        isLoading = false,
        searchQuery = "Bu",
        showFavoritesOnly = false,
        cities = mockCities,
        selectedCity = null,
        showCityDetail = false,
        errorMessage = null,
        paginationState = PaginationState(isLoadingMore = true)
    )

    SearchScreenContent(
        modifier = Modifier.fillMaxSize(),
        state = mockState,
        onBackPressed = {},
        onCitySelected = {},
        intentProcessor = {}
    )
}
