package com.flynn.citysearch.feature.map

import com.flynn.citysearch.SnapshotTest
import org.junit.Test

class MapScreenTest : SnapshotTest() {
    @Test
    fun testMapTopUiScreen() {
        captureSnapshot { PreviewMapTopUiContent() }
    }
}