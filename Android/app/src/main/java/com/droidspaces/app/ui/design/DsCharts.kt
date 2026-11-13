package com.droidspaces.app.ui.design

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A capacity ring. Reserved for a number with a known maximum, memory, storage,
 * a CPU quota. It is deliberately not a sparkline: with no history stored there
 * would be nothing truthful to draw between samples.
 *
 * The arc animates to its target on every new reading, so a change is visible
 * without a label having to flash.
 */
@Composable
fun DsRing(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    size: Dp = 84.dp,
    strokeWidth: Dp = 7.dp,
    description: String? = null,
    center: @Composable () -> Unit = {}
) {
    val target = progress.coerceIn(0f, 1f)
    val animated by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = DsMotion.slow, easing = FastOutSlowInEasing),
        label = "ringProgress"
    )
    val strokePx = with(LocalDensity.current) { strokeWidth.toPx() }
    Box(
        modifier = modifier
            .size(size)
            .then(
                if (description != null) Modifier.semantics { contentDescription = description } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val diameter = this.size.minDimension - strokePx
            val topLeft = Offset((this.size.width - diameter) / 2f, (this.size.height - diameter) / 2f)
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = Size(diameter, diameter),
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
            if (animated > 0f) {
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = 360f * animated,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(diameter, diameter),
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }
        center()
    }
}
