package com.droidspaces.app.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.droidspaces.app.R
import com.droidspaces.app.ui.design.DsActionButton
import com.droidspaces.app.ui.design.DsActionStrip
import com.droidspaces.app.ui.design.DsBadge
import com.droidspaces.app.ui.design.DsButtonStyle
import com.droidspaces.app.ui.design.DsCard
import com.droidspaces.app.ui.design.DsDivider
import com.droidspaces.app.ui.design.DsIconAction
import com.droidspaces.app.ui.design.DsSpace
import com.droidspaces.app.ui.theme.JetBrainsMono
import com.droidspaces.app.util.AnimationUtils
import com.droidspaces.app.util.ContainerInfo
import com.droidspaces.app.util.ContainerOSInfoManager
import com.droidspaces.app.util.ContainerStatus
import com.droidspaces.app.util.IconUtils
import com.droidspaces.app.util.ResourceLimits

/**
 * Per-item callbacks for [ContainerCard], grouped so the call site passes one
 * stable object instead of eleven separate lambdas.
 */
data class ContainerCardActions(
    val onStart: () -> Unit = {},
    val onStop: () -> Unit = {},
    val onRestart: () -> Unit = {},
    val onEdit: () -> Unit = {},
    val onEnter: () -> Unit = {},
    val onUninstall: () -> Unit = {},
    val onMigrate: () -> Unit = {},
    val onResize: () -> Unit = {},
    val onExport: () -> Unit = {},
    val onToggleExpand: () -> Unit = {},
    val onShowLogs: () -> Unit = {},
)

/**
 * One container, as a row in a workbench: identity and state at the top, the
 * limits it was given below, then the actions that act on it. The card itself is
 * never tinted by state; the accent line and the status chip carry it.
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun ContainerCard(
    container: ContainerInfo,
    actions: ContainerCardActions = ContainerCardActions(),
    isOperationRunning: Boolean = false,
    isExpanded: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cacheVersion by ContainerOSInfoManager.iconCacheVersion
    val cachedOsInfo = remember(container.name, cacheVersion) {
        ContainerOSInfoManager.getCachedOSInfo(container.name, context)
    }
    val distro = cachedOsInfo?.prettyName ?: cachedOsInfo?.name ?: "linux"
    val isRunning = container.status == ContainerStatus.RUNNING
    val statusColor = if (isRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    val displayHostname = container.hostname.takeIf { it.isNotEmpty() } ?: container.name

    DsCard(
        modifier = modifier
            .combinedClickable(
                onClick = { if (isRunning) actions.onEnter() else actions.onToggleExpand() },
                onLongClick = actions.onToggleExpand
            )
            .then(
                Modifier.animateContentSize(animationSpec = AnimationUtils.mediumSpec())
            ),
        accent = if (isRunning) MaterialTheme.colorScheme.primary else null,
        contentPadding = PaddingValues(DsSpace.md)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = IconUtils.getDistroIcon(distro),
                contentDescription = null,
                modifier = Modifier.size(26.dp),
                tint = if (isRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(DsSpace.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = container.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$displayHostname · ${container.netMode}",
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMono),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            DsBadge(
                label = if (isRunning) context.getString(R.string.status_running) else context.getString(R.string.status_stopped),
                color = statusColor
            )
            if (isOperationRunning) {
                Spacer(Modifier.width(DsSpace.sm))
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 1.5.dp
                )
            }
            Spacer(Modifier.width(DsSpace.xs))
            DsIconAction(
                icon = Icons.AutoMirrored.Filled.Article,
                contentDescription = context.getString(R.string.view_logs),
                onClick = actions.onShowLogs
            )
        }

        DsDivider(Modifier.padding(vertical = DsSpace.md))

        val limits = buildList {
            if (container.useSparseImage && container.sparseImageSizeGB != null) {
                add(painterResource(R.drawable.ic_disk) to context.getString(R.string.gb_size, container.sparseImageSizeGB))
            }
            ResourceLimits.memoryLabel(context, container)?.let { add(rememberVectorPainter(Icons.Default.Memory) to it) }
            ResourceLimits.cpuLabel(context, container)?.let { add(rememberVectorPainter(Icons.Default.Speed) to it) }
            ResourceLimits.pidsLabel(context, container)?.let { add(rememberVectorPainter(Icons.Default.Tag) to it) }
            container.pid?.let { add(rememberVectorPainter(Icons.Default.Terminal) to "pid $it") }
        }
        if (limits.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(DsSpace.md),
                verticalArrangement = Arrangement.spacedBy(DsSpace.xs)
            ) {
                limits.forEach { (icon, label) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = icon,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMono),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        val options = remember(container) {
            buildList {
                if (container.disableIPv6) add(context.getString(R.string.ipv6_option))
                if (container.enableAndroidStorage) add(context.getString(R.string.storage_option))
                if (container.enableHwAccess) add(context.getString(R.string.hw_option))
                if (container.enableGpuMode || container.enableHwAccess) add(context.getString(R.string.gpu_option))
                if (container.enableTermuxX11) add(context.getString(R.string.x11_option))
                if (container.enableAnland) add(context.getString(R.string.anland_option))
                if (container.enableVirgl) add(context.getString(R.string.virgl_option))
                if (container.enablePulseaudio) add(context.getString(R.string.pulseaudio_option))
                if (container.selinuxPermissive) add(context.getString(R.string.selinux_permissive_option))
                if (container.allowSandboxing) add(context.getString(R.string.userns_option))
                if (container.volatileMode) add(context.getString(R.string.volatile_option))
                if (container.forceCgroupv1) add(context.getString(R.string.cgroup_v1_option))
                if (container.privileged.isNotEmpty()) add(context.getString(R.string.privileged_option))
                if (container.customInit.isNotEmpty()) add(context.getString(R.string.custom_init_option))
                if (container.runAtBoot) add(context.getString(R.string.run_at_boot))
            }
        }
        if (options.isNotEmpty()) {
            Text(
                text = options.joinToString("  ·  "),
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMono),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = DsSpace.sm)
            )
        }

        DsActionStrip(
            modifier = Modifier.padding(top = DsSpace.md)
        ) {
            DsActionButton(
                label = if (isRunning) context.getString(R.string.stop) else context.getString(R.string.start),
                icon = if (isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                onClick = if (isRunning) actions.onStop else actions.onStart,
                enabled = !isOperationRunning,
                style = if (isRunning) DsButtonStyle.Danger else DsButtonStyle.Filled,
                modifier = Modifier.weight(1f)
            )
            if (isRunning) {
                DsActionButton(
                    label = context.getString(R.string.restart),
                    icon = Icons.Default.Refresh,
                    onClick = actions.onRestart,
                    enabled = !isOperationRunning,
                    style = DsButtonStyle.Tonal,
                    modifier = Modifier.weight(1f)
                )
            }
            DsActionButton(
                label = if (isExpanded) context.getString(R.string.close) else context.getString(R.string.manage),
                icon = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                onClick = actions.onToggleExpand,
                style = DsButtonStyle.Outline,
                modifier = Modifier.weight(1f)
            )
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(animationSpec = AnimationUtils.mediumSpec(), expandFrom = Alignment.Top) + fadeIn(),
            exit = shrinkVertically(animationSpec = AnimationUtils.mediumSpec(), shrinkTowards = Alignment.Top) + fadeOut()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(Modifier.height(DsSpace.sm))
                DsDivider()
                Spacer(Modifier.height(DsSpace.sm))
                CardMenuItem(
                    icon = rememberVectorPainter(Icons.Default.Edit),
                    label = context.getString(R.string.edit_container_configuration),
                    tint = MaterialTheme.colorScheme.primary,
                    onClick = actions.onEdit
                )
                if (!container.useSparseImage) {
                    CardMenuItem(
                        icon = painterResource(R.drawable.ic_disk),
                        label = context.getString(R.string.migrate_to_sparse_image),
                        tint = MaterialTheme.colorScheme.secondary,
                        onClick = actions.onMigrate
                    )
                } else {
                    CardMenuItem(
                        icon = painterResource(R.drawable.ic_disk),
                        label = context.getString(R.string.resize_sparse_image),
                        tint = MaterialTheme.colorScheme.secondary,
                        onClick = actions.onResize
                    )
                }
                CardMenuItem(
                    icon = rememberVectorPainter(Icons.Default.FileDownload),
                    label = context.getString(R.string.export_container),
                    tint = MaterialTheme.colorScheme.tertiary,
                    onClick = actions.onExport
                )
                CardMenuItem(
                    icon = rememberVectorPainter(Icons.Default.Delete),
                    label = context.getString(R.string.uninstall_container_menu),
                    tint = MaterialTheme.colorScheme.error,
                    onClick = actions.onUninstall
                )
            }
        }
    }
}

@Composable
private fun CardMenuItem(
    icon: Painter,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    androidx.compose.material3.Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(horizontal = DsSpace.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DsSpace.md)
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (tint == MaterialTheme.colorScheme.error) tint else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
