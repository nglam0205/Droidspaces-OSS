package com.droidspaces.app.ui.design

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.droidspaces.app.R
import com.droidspaces.app.ui.theme.JetBrainsMono

/**
 * The top of every screen: an optional eyebrow, the title, an optional line of
 * context, and its actions. It sits on the background rather than on its own
 * raised bar, so the header and the content read as one page.
 */
@Composable
fun DsPageHeader(
    title: String,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    // A landscape phone with the keyboard up has no vertical room for a 90dp
    // header, so the title drops a step and the supporting line goes away.
    val short = isShortWindow()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(
                start = if (onBack == null) DsSpace.lg else DsSpace.sm,
                end = DsSpace.sm,
                top = if (short) DsSpace.xs else DsSpace.md,
                bottom = if (short) DsSpace.xs else DsSpace.md
            )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) {
                DsIconAction(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    onClick = onBack
                )
                Spacer(Modifier.width(DsSpace.sm))
            }
            Column(modifier = Modifier.weight(1f)) {
                if (eyebrow != null) {
                    Text(
                        text = eyebrow.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMono),
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(DsSpace.xxs))
                }
                Text(
                    text = title,
                    style = if (short) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null && !short) {
                    Spacer(Modifier.height(DsSpace.xxs))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(DsSpace.xs),
                content = actions
            )
        }
    }
}

/**
 * A panel. The optional [accent] draws a 2dp line along the top edge and is the
 * only decoration a panel gets: state belongs in the accent, never in a tinted
 * background.
 */
@Composable
fun DsCard(
    modifier: Modifier = Modifier,
    accent: Color? = null,
    contentPadding: PaddingValues = PaddingValues(DsSpace.lg),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(DsRadius.panel)
    val border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f))
    val body: @Composable () -> Unit = {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(contentPadding),
                content = content
            )
            if (accent != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(accent.copy(alpha = 0.9f))
                )
            }
        }
    }
    if (onClick != null) {
        Surface(onClick = onClick, modifier = modifier.fillMaxWidth(), shape = shape, color = MaterialTheme.colorScheme.surfaceContainer, border = border) { body() }
    } else {
        Surface(modifier = modifier.fillMaxWidth(), shape = shape, color = MaterialTheme.colorScheme.surfaceContainer, border = border) { body() }
    }
}

/**
 * Section heading inside a page. The mono index is the app's signature: it makes
 * a long page scannable and marks the start of a block without a second colour.
 */
@Composable
fun DsSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    index: String? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = DsSpace.xxl, bottom = DsSpace.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (index != null) {
            Text(
                text = index,
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMono),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = DsSpace.sm)
            )
        }
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        trailing?.invoke()
    }
}

/** A compact state chip. Colour is the state, the fill only makes it readable. */
@Composable
fun DsBadge(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    dot: Boolean = true
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(DsRadius.tag),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.28f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = DsSpace.sm, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            if (dot) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .background(color, CircleShape)
                )
            }
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMono),
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

enum class DsButtonStyle { Filled, Tonal, Outline, Danger }

@Composable
fun DsActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    style: DsButtonStyle = DsButtonStyle.Tonal,
    enabled: Boolean = true
) {
    val color = MaterialTheme.colorScheme
    val fill = when (style) {
        DsButtonStyle.Filled -> if (enabled) color.primary else color.onSurface.copy(alpha = 0.08f)
        DsButtonStyle.Tonal -> color.surfaceContainerHigh
        DsButtonStyle.Outline -> Color.Transparent
        DsButtonStyle.Danger -> if (enabled) color.errorContainer.copy(alpha = 0.45f) else color.onSurface.copy(alpha = 0.08f)
    }
    val contentColor = when {
        !enabled -> color.onSurface.copy(alpha = 0.38f)
        style == DsButtonStyle.Filled -> color.onPrimary
        style == DsButtonStyle.Danger -> color.error
        else -> color.onSurface
    }
    val borderColor = when {
        !enabled -> color.outlineVariant.copy(alpha = 0.3f)
        style == DsButtonStyle.Filled -> Color.Transparent
        style == DsButtonStyle.Danger -> color.error.copy(alpha = 0.35f)
        style == DsButtonStyle.Outline -> color.outlineVariant
        else -> color.outlineVariant.copy(alpha = 0.5f)
    }
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(DsSize.control),
        shape = RoundedCornerShape(DsRadius.control),
        color = fill,
        contentColor = contentColor,
        border = if (borderColor == Color.Transparent) null else BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = DsSpace.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(DsSpace.sm))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun DsIconAction(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    showBackground: Boolean = true
) {
    val label = contentDescription
    Surface(
        onClick = onClick,
        modifier = modifier
            .size(DsSize.barAction)
            .semantics { this.contentDescription = label },
        shape = RoundedCornerShape(DsRadius.control),
        color = if (showBackground) MaterialTheme.colorScheme.surfaceContainerHigh else Color.Transparent,
        border = if (showBackground) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)) else null
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        }
    }
}

/** A label on the left, a machine value on the right. The value ellipsizes first. */
@Composable
fun DsKeyValueRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    mono: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.width(DsSpace.md))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = if (mono) JetBrainsMono else null),
            fontWeight = FontWeight.Medium,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun DsDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
    )
}

/** Metric tile: label, big number, unit, and an optional capacity bar. */
@Composable
fun DsMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    unit: String? = null,
    detail: String? = null,
    progress: Float? = null,
    accent: Color = MaterialTheme.colorScheme.primary,
    icon: ImageVector? = null
) {
    DsCard(modifier = modifier, contentPadding = PaddingValues(DsSpace.md)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMono),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
            }
        }
        Spacer(Modifier.height(DsSpace.sm))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(fontFamily = JetBrainsMono),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (unit != null) {
                Spacer(Modifier.width(3.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMono),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
        if (progress != null) {
            Spacer(Modifier.height(DsSpace.sm))
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = accent,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )
        } else if (detail != null) {
            Spacer(Modifier.height(DsSpace.xxs))
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** Search field for a list. It sits on the page rather than inside a card. */
@Composable
fun DsSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(DsRadius.control),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            trailingIcon = {
                if (value.isNotEmpty()) {
                    IconButton(onClick = { onValueChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun DsFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    count: Int? = null
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(32.dp),
        shape = RoundedCornerShape(DsRadius.control),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        border = BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = DsSpace.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (count != null) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMono),
                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun DsEmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(DsSpace.huge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(DsRadius.panel),
            color = MaterialTheme.colorScheme.surfaceContainer,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
        ) {
            Box(modifier = Modifier.size(72.dp), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(30.dp), tint = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(Modifier.height(DsSpace.lg))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(DsSpace.sm))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(DsSpace.xl))
            DsActionButton(
                label = actionLabel,
                onClick = onAction,
                icon = Icons.Default.Refresh,
                style = DsButtonStyle.Filled,
                modifier = Modifier.widthIn(min = 160.dp)
            )
        }
    }
}

@Composable
fun DsLoadingState(modifier: Modifier = Modifier, label: String? = null) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(28.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 2.dp
        )
        if (label != null) {
            Spacer(Modifier.height(DsSpace.md))
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun DsErrorState(
    title: String? = null,
    description: String? = null,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    DsEmptyState(
        icon = Icons.Default.Refresh,
        title = title ?: context.getString(R.string.backend_not_available),
        description = description ?: context.getString(R.string.backend_not_available_description),
        modifier = modifier,
        actionLabel = if (onRetry != null) context.getString(R.string.repo_retry) else null,
        onAction = onRetry
    )
}

/**
 * A row of peer actions. The buttons are one step rounder than the strip they
 * sit in, so they read as controls on a rail rather than cells in a grid.
 */
@Composable
fun DsActionStrip(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(DsRadius.panel),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DsSpace.xs),
            horizontalArrangement = Arrangement.spacedBy(DsSpace.xs),
            content = content
        )
    }
}
