package com.flynn.citysearch.feature.search

import com.flynn.citysearch.SnapshotTest
import org.junit.Test

class SearchScreenTest : SnapshotTest() {
    @Test
    fun testSearchScreen() {
        captureSnapshot { SearchScreenContentPreview() }
    }

    @Test
    fun testCityDetailScreenPreview() {
        captureSnapshot { CityDetailScreenPreview() }
    }
}