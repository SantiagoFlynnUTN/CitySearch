package com.flynn.citysearch.navigation

import kotlinx.serialization.Serializable

sealed interface Directions

@Serializable
data object Main : Directions

@Serializable
data object MapMain : Directions

@Serializable
data object MapPortrait : Directions

@Serializable
data object MapLandscape : Directions