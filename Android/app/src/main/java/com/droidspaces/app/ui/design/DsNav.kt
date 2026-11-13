package com.droidspaces.app.ui.design

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.droidspaces.app.ui.theme.JetBrainsMono

/** One destination in the dock or rail. [id] is stable across recompositions. */
data class DsDestination(
    val id: String,
    val label: String,
    val icon: ImageVector
)

/**
 * The compact shell's navigation: a floating dock, inset from the edges and
 * clear of the system navigation bar. The selected item is a filled pill with
 * the signal colour, so the current page is readable at a glance.
 */
@Composable
fun DsBottomDock(
    destinations: List<DsDestination>,
    selectedId: String,
    onSelect: (DsDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = DsSpace.md, vertical = DsSpace.sm)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(DsRadius.dock),
            color = MaterialTheme.colorScheme.surfaceContainer,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(DsSpace.xs),
                horizontalArrangement = Arrangement.spacedBy(DsSpace.xxs)
            ) {
                destinations.forEach { destination ->
                    val selected = destination.id == selectedId
                    val tint by animateColorAsState(
                        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        label = "dockTint"
                    )
                    Surface(
                        onClick = { onSelect(destination) },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(DsRadius.control),
                        color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else androidx.compose.ui.graphics.Color.Transparent
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = null,
                                tint = tint,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = destination.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = tint,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * The wide shell's navigation: a fixed rail with the mark at the top and the
 * same destinations as the dock. The rail is part of the page rather than an
 * overlay, so a tablet or a landscape phone gets the extra width for content.
 */
@Composable
fun DsNavRail(
    destinations: List<DsDestination>,
    selectedId: String,
    onSelect: (DsDestination) -> Unit,
    modifier: Modifier = Modifier,
    footer: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .width(96.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(vertical = DsSpace.lg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "DS",
            style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMono),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = DsSpace.xxl)
        )
        destinations.forEach { destination ->
            val selected = destination.id == selectedId
            Surface(
                onClick = { onSelect(destination) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = DsSpace.sm, vertical = DsSpace.xxs)
                    .height(60.dp),
                shape = RoundedCornerShape(DsRadius.control),
                color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else androidx.compose.ui.graphics.Color.Transparent
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        destination.icon,
                        contentDescription = null,
                        tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = destination.label,
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMono, fontSize = 9.sp),
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        Spacer(Modifier.weight(1f))
        footer?.invoke()
    }
}

/** Divider used between the rail and the page. */
@Composable
fun DsRailDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    )
}
