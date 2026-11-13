package com.droidspaces.app.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.droidspaces.app.R
import com.droidspaces.app.ui.component.DsSnackbarHost
import com.droidspaces.app.ui.component.PullToRefreshWrapper
import com.droidspaces.app.ui.design.DsActionButton
import com.droidspaces.app.ui.design.DsBadge
import com.droidspaces.app.ui.design.DsButtonStyle
import com.droidspaces.app.ui.design.DsCard
import com.droidspaces.app.ui.design.DsDivider
import com.droidspaces.app.ui.design.DsEmptyState
import com.droidspaces.app.ui.design.DsErrorState
import com.droidspaces.app.ui.design.DsIconAction
import com.droidspaces.app.ui.design.DsKeyValueRow
import com.droidspaces.app.ui.design.DsMetric
import com.droidspaces.app.ui.design.DsPageHeader
import com.droidspaces.app.ui.design.DsRing
import com.droidspaces.app.ui.design.DsSectionHeader
import com.droidspaces.app.ui.design.DsSpace
import com.droidspaces.app.ui.theme.JetBrainsMono
import com.droidspaces.app.ui.util.HostMetricsReader
import com.droidspaces.app.ui.util.HostSnapshot
import com.droidspaces.app.ui.util.formatBytes
import com.droidspaces.app.ui.util.formatPercent
import com.droidspaces.app.ui.util.showError
import com.droidspaces.app.ui.viewmodel.SystemStatsViewModel
import com.droidspaces.app.util.AnlandUtils
import com.droidspaces.app.util.ContainerInfo
import com.droidspaces.app.util.ContainerOSInfoManager
import com.droidspaces.app.util.IconUtils
import com.droidspaces.app.util.TermuxX11Utils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Live telemetry for the phone and every running container. The host numbers
 * refresh on the same three second cadence as before; container readings come
 * from one batched backend call, so adding containers does not add polling cost.
 */
@Composable
fun MonitorScreen(
    containers: List<ContainerInfo>,
    isBackendAvailable: Boolean,
    isRootAvailable: Boolean,
    isKernelSupported: Boolean,
    onRefresh: () -> Unit,
    onContainerDetails: (String) -> Unit,
    onTerminal: (String) -> Unit,
    bottomBarInset: Dp = 0.dp
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val systemStatsViewModel: SystemStatsViewModel = viewModel()
    val lifecycleOwner = LocalLifecycleOwner.current
    val metricsReader = remember { HostMetricsReader() }
    var host by remember { mutableStateOf(HostSnapshot()) }

    val running = remember(containers) { containers.filter { it.isRunning } }
    val runningNames = remember(containers) { running.map { it.name } }

    LaunchedEffect(runningNames) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            systemStatsViewModel.monitorContainers(running, onStopped = onRefresh)
        }
    }
    LaunchedEffect(Unit) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                host = metricsReader.read()
                delay(3000)
            }
        }
    }

    val usage = systemStatsViewModel.containerUsageMap

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            DsPageHeader(
                title = context.getString(R.string.monitor),
                eyebrow = "Live telemetry",
                subtitle = "${running.size} running · host sampled every 3 s",
                actions = {
                    DsIconAction(
                        icon = Icons.Default.Refresh,
                        contentDescription = context.getString(R.string.refresh),
                        onClick = onRefresh
                    )
                }
            )

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when {
                !isRootAvailable -> DsErrorState(
                    title = context.getString(R.string.root_access_not_available),
                    description = context.getString(R.string.root_access_not_available_description)
                )
                !isBackendAvailable -> DsErrorState()
                !isKernelSupported -> DsErrorState(
                    title = context.getString(R.string.kernel_unsupported),
                    description = context.getString(R.string.kernel_unsupported_description)
                )
                else -> PullToRefreshWrapper(onRefresh = { onRefresh() }) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = DsSpace.lg,
                            end = DsSpace.lg,
                            top = DsSpace.xs,
                            bottom = bottomBarInset + DsSpace.lg
                        )
                    ) {
                        item(key = "host") {
                            DsCard(accent = MaterialTheme.colorScheme.primary) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "HOST LOAD",
                                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMono),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(Modifier.height(DsSpace.xs))
                                        Text(
                                            text = formatPercent(host.cpuPercent),
                                            style = MaterialTheme.typography.displaySmall.copy(fontFamily = JetBrainsMono),
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${host.cpuCores} cores · load ${String.format("%.2f", host.loadAverage)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    DsRing(
                                        progress = host.memProgress,
                                        size = 76.dp,
                                        color = MaterialTheme.colorScheme.secondary,
                                        description = "Memory ${formatPercent(host.memProgress)}"
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = formatPercent(host.memProgress),
                                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMono),
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "RAM",
                                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMono),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        item(key = "host_metrics") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = DsSpace.md),
                                horizontalArrangement = Arrangement.spacedBy(DsSpace.md)
                            ) {
                                DsMetric(
                                    label = "Memory",
                                    value = formatBytes(host.memUsedBytes),
                                    detail = "of ${formatBytes(host.memTotalBytes)}",
                                    icon = Icons.Default.Memory,
                                    modifier = Modifier.weight(1f)
                                )
                                DsMetric(
                                    label = "Storage",
                                    value = formatBytes(host.storageUsedBytes),
                                    detail = "of ${formatBytes(host.storageTotalBytes)}",
                                    icon = Icons.Default.Speed,
                                    accent = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        item(key = "running_header") {
                            DsSectionHeader(title = "Running containers", index = "01")
                        }

                        if (running.isEmpty()) {
                            item(key = "empty") {
                                DsEmptyState(
                                    icon = Icons.Default.MonitorHeart,
                                    title = context.getString(R.string.no_containers_running),
                                    description = context.getString(R.string.start_container_first)
                                )
                            }
                        } else {
                            items(running, key = { it.name }) { container ->
                                MonitorCard(
                                    container = container,
                                    osInfo = usage[container.name],
                                    onClick = { onContainerDetails(container.name) },
                                    onTerminal = { onTerminal(container.name) },
                                    onLaunchDisplay = {
                                        val info = usage[container.name]
                                        if (info?.anlandSocket != null) {
                                            if (!AnlandUtils.launchWindow(context, container.name, info.anlandSocket))
                                                scope.showError(snackbarHostState, context.getString(R.string.anland_not_installed))
                                        } else if (info?.x11 == true) {
                                            if (!TermuxX11Utils.launch(context))
                                                scope.showError(snackbarHostState, context.getString(R.string.termux_x11_not_installed))
                                        }
                                    },
                                    modifier = Modifier.padding(bottom = DsSpace.md)
                                )
                            }
                        }
                    }
                }
            }
            }
        }

        DsSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = bottomBarInset + DsSpace.lg)
        )
    }
}

@Composable
private fun MonitorCard(
    container: ContainerInfo,
    osInfo: ContainerOSInfoManager.OSInfo?,
    onClick: () -> Unit,
    onTerminal: () -> Unit,
    onLaunchDisplay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val distro = osInfo?.prettyName ?: osInfo?.name ?: "linux"
    val hasDisplay = osInfo?.anlandSocket != null || osInfo?.x11 == true

    DsCard(modifier = modifier, onClick = onClick, accent = MaterialTheme.colorScheme.primary) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = IconUtils.getDistroIcon(distro),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(26.dp)
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
                    text = osInfo?.hostname ?: "hostname pending",
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMono),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            DsBadge(label = context.getString(R.string.status_running), color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(DsSpace.xs))
            DsIconAction(
                icon = Icons.Default.Terminal,
                contentDescription = context.getString(R.string.terminal),
                onClick = onTerminal
            )
        }

        DsDivider(Modifier.padding(vertical = DsSpace.md))

        if (osInfo == null) {
            Text(
                text = "Reading container telemetry...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            DsKeyValueRow("CPU", osInfo.cpuUsage?.let { context.getString(R.string.cpu_percent_label, it) } ?: "---")
            DsKeyValueRow("RAM", osInfo.ramLabel(context) ?: "---")
            DsKeyValueRow("Uptime", osInfo.uptime ?: "---")
            DsKeyValueRow("Address", osInfo.ipAddress ?: "---")
        }

        if (hasDisplay) {
            DsActionButton(
                label = if (osInfo?.anlandSocket != null) context.getString(R.string.anland_option) else context.getString(R.string.termux_x11_option),
                onClick = onLaunchDisplay,
                icon = Icons.Default.DesktopWindows,
                style = DsButtonStyle.Tonal,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = DsSpace.md)
            )
        }
    }
}
