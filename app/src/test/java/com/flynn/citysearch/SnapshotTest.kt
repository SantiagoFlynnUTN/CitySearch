package com.flynn.citysearch

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.dropbox.differ.SimpleImageComparator
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.RoborazziRule
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.LooperMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h1000dp-480dpi")
@LooperMode(LooperMode.Mode.PAUSED)
abstract class SnapshotTest {

    @get:Rule(order = 0)
    val composeRule = createComposeRule()

    @get:Rule(order = 1)
    val roborazziRule = RoborazziRule(
        RoborazziRule.Options(outputDirectoryPath = "src/test/snapshots")
    )

    fun captureSnapshot(block: @Composable () -> Unit) {
        composeRule.setContent {
            block()
        }
        composeRule.onRoot().captureRoboImage(
            roborazziOptions = RoborazziOptions(
                compareOptions = RoborazziOptions.CompareOptions(
                    imageComparator = SimpleImageComparator(
                        maxDistance = 0.007F, // 0.001F is default value from Differ
                        vShift = 2, // 0 default value
                        hShift = 2 // 0 default value
                    )
                )
            )
        )
    }
}