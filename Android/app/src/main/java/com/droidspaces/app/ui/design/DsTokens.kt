package com.droidspaces.app.ui.design

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

/**
 * The spacing scale. Every gap in the app comes from here, so a screen can be
 * tightened or loosened in one place instead of by eye at each call site.
 */
object DsSpace {
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val huge = 32.dp
}

/** Panels stay nearly square, controls get a little rounder, overlays rounder still. */
object DsRadius {
    val tag = 4.dp
    val control = 8.dp
    val panel = 12.dp
    val overlay = 16.dp
    val dock = 20.dp
}

object DsSize {
    /** The accessibility floor for anything tappable. Controls use it directly. */
    val touch = 48.dp
    val control = 48.dp
    val barAction = 48.dp
    val headerIcon = 20.dp
    val metric = 28.dp
}

object DsMotion {
    const val fast = 140
    const val normal = 220
    const val slow = 320
}

/** Phone portrait or narrow window: the dock and one column. */
@Composable
fun isCompactLayout(): Boolean = LocalConfiguration.current.screenWidthDp < 600

/** Tablet, foldable unfolded, or landscape phone: the rail and wider grids. */
@Composable
fun isWideLayout(): Boolean = LocalConfiguration.current.screenWidthDp >= 600

/** Below the width where two fields or two buttons still fit side by side. */
@Composable
fun isNarrowWidth(): Boolean = LocalConfiguration.current.screenWidthDp < 320

/** A short window cannot afford a 64dp bar above and a dock below. */
@Composable
fun isShortWindow(): Boolean = LocalConfiguration.current.screenHeightDp < 480
