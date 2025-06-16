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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.flynn.citysearch.domain.City
import com.flynn.citysearch.domain.Coordinates
import com.flynn.citysearch.feature.search.SearchViewModel.SearchState

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
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            SearchBox(state = state, intentProcessor = intentProcessor)
            Spacer(modifier = Modifier.height(8.dp))
            BookmarkedCitiesChip(state = state, intentProcessor = intentProcessor)
            Spacer(modifier = Modifier.height(16.dp))
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
    when {
        state.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        state.cities.isEmpty() -> {
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
        else -> {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(items = state.cities, key = { it.id }) { city ->
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
                }
            }
        }
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
                contentDescription = "Favorites filter"
            )
        }
    )
}

@Composable
fun SearchBox(state: SearchState, intentProcessor: (SearchIntent) -> Unit) {
    // Search Box
    OutlinedTextField(
        value = state.searchQuery,
        onValueChange = { intentProcessor(SearchIntent.UpdateSearchQuery(it)) },
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Search cities...") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search"
            )
        },
        singleLine = true
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
            .clickable(onClick = onCityClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${city.name}, ${city.country}",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = city.coordinates.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            IconButton(onClick = onInfoClick) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "City Info"
                )
            }

            IconButton(onClick = onFavoriteToggle) {
                Icon(
                    imageVector = if (city.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Toggle Favorite",
                    tint = if (city.isFavorite) Color.Red else Color.Gray
                )
            }
        }
    }
}

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
        errorMessage = null
    )

    SearchScreenContent(
        modifier = Modifier.fillMaxSize(),
        state = mockState,
        onBackPressed = {},
        onCitySelected = {},
        intentProcessor = {}
    )
}
