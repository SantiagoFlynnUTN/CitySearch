package com.flynn.citysearch.navigation

import kotlinx.serialization.Serializable

sealed interface Directions

@Serializable
data object Main : Directions
