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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.droidspaces.app.R
import com.droidspaces.app.ui.component.PullToRefreshWrapper
import com.droidspaces.app.ui.design.DsActionButton
import com.droidspaces.app.ui.design.DsBadge
import com.droidspaces.app.ui.design.DsButtonStyle
import com.droidspaces.app.ui.design.DsCard
import com.droidspaces.app.ui.design.DsDivider
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
import com.droidspaces.app.util.ContainerInfo
import com.droidspaces.app.util.ContainerOSInfoManager
import com.droidspaces.app.util.IconUtils
import com.droidspaces.app.util.SystemInfoManager
import kotlinx.coroutines.delay

/**
 * The landing page. It answers three questions before anything else: is the
 * engine healthy, what is the phone doing, and what is running. The host
 * readings are the kernel's own counters, refreshed on an interval while the
 * app is in the foreground.
 */
@Composable
fun OverviewScreen(
    containers: List<ContainerInfo>,
    isBackendAvailable: Boolean,
    isRootAvailable: Boolean,
    isKernelSupported: Boolean,
    onRefresh: () -> Unit,
    onOpenContainers: () -> Unit,
    onOpenMonitor: () -> Unit,
    onOpenSystem: () -> Unit,
    onOpenSettings: () -> Unit,
    onNewContainer: () -> Unit,
    onContainerDetails: (String) -> Unit,
    onTerminal: (String) -> Unit,
    bottomBarInset: Dp = 0.dp
) {
    val context = LocalContext.current
    val running = remember(containers) { containers.filter { it.isRunning } }
    var snapshot by remember { mutableStateOf(HostSnapshot()) }
    var backendVersion by remember { mutableStateOf<String?>(null) }
    val metricsReader = remember { HostMetricsReader() }
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                snapshot = metricsReader.read()
                delay(3000)
            }
        }
    }
    LaunchedEffect(Unit) {
        backendVersion = SystemInfoManager.getDroidspacesVersion(context)
    }

    val statusColor = when {
        !isRootAvailable || !isBackendAvailable -> MaterialTheme.colorScheme.error
        !isKernelSupported -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }
    val statusLabel = when {
        !isRootAvailable -> context.getString(R.string.root_access_not_available)
        !isBackendAvailable -> context.getString(R.string.backend_not_available)
        !isKernelSupported -> context.getString(R.string.kernel_unsupported)
        else -> "Engine online"
    }

    Column(modifier = Modifier.fillMaxSize()) {
        DsPageHeader(
            title = context.getString(R.string.overview),
            eyebrow = "Droidspaces workstation",
            subtitle = "${containers.size} containers, ${running.size} running",
            actions = {
                DsIconAction(
                    icon = Icons.Default.Refresh,
                    contentDescription = context.getString(R.string.refresh),
                    onClick = onRefresh
                )
                DsIconAction(
                    icon = Icons.Default.Settings,
                    contentDescription = context.getString(R.string.settings),
                    onClick = onOpenSettings
                )
            }
        )

        PullToRefreshWrapper(onRefresh = { onRefresh() }, modifier = Modifier.weight(1f)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = DsSpace.lg,
                    end = DsSpace.lg,
                    top = DsSpace.xs,
                    bottom = bottomBarInset + DsSpace.lg
                )
            ) {
                item(key = "status") {
                    DsCard(accent = statusColor) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                DsBadge(label = statusLabel, color = statusColor)
                                Spacer(Modifier.height(DsSpace.sm))
                                Text(
                                    text = if (isKernelSupported) "All systems nominal" else "Kernel limits detected",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.height(DsSpace.xxs))
                                Text(
                                    text = buildString {
                                        append(if (isBackendAvailable) "Backend ready" else "Backend unavailable")
                                        backendVersion?.let { append(" · v$it") }
                                        append(" · root ")
                                        append(if (isRootAvailable) "granted" else "denied")
                                    },
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMono),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(Modifier.width(DsSpace.md))
                            DsRing(
                                progress = snapshot.cpuPercent,
                                size = 74.dp,
                                description = "CPU ${formatPercent(snapshot.cpuPercent)}"
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = formatPercent(snapshot.cpuPercent),
                                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMono),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "CPU",
                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMono),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                item(key = "metrics") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = DsSpace.md),
                        horizontalArrangement = Arrangement.spacedBy(DsSpace.md)
                    ) {
                        DsMetric(
                            label = "Memory",
                            value = formatBytes(snapshot.memUsedBytes),
                            unit = "/ ${formatBytes(snapshot.memTotalBytes)}",
                            progress = snapshot.memProgress,
                            icon = Icons.Default.Memory,
                            modifier = Modifier.weight(1f)
                        )
                        DsMetric(
                            label = "Storage",
                            value = formatBytes(snapshot.storageUsedBytes),
                            unit = "/ ${formatBytes(snapshot.storageTotalBytes)}",
                            progress = snapshot.storageProgress,
                            icon = Icons.Default.Storage,
                            accent = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item(key = "network") {
                    DsCard(
                        modifier = Modifier.padding(top = DsSpace.md),
                        contentPadding = PaddingValues(horizontal = DsSpace.lg, vertical = DsSpace.md)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.SwapVert,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(DsSpace.sm))
                            Text(
                                text = "Network",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                text = "↓ ${formatBytes(snapshot.networkRxBytes)}   ↑ ${formatBytes(snapshot.networkTxBytes)}",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMono),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                item(key = "quick") {
                    DsSectionHeader(title = "Quick actions", index = "01")
                    Row(horizontalArrangement = Arrangement.spacedBy(DsSpace.md)) {
                        QuickAction(
                            icon = Icons.Default.Add,
                            label = "New container",
                            detail = "From a rootfs archive",
                            onClick = onNewContainer,
                            modifier = Modifier.weight(1f)
                        )
                        QuickAction(
                            icon = Icons.Default.Dns,
                            label = "Containers",
                            detail = "${containers.size} installed",
                            onClick = onOpenContainers,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.padding(top = DsSpace.md),
                        horizontalArrangement = Arrangement.spacedBy(DsSpace.md)
                    ) {
                        QuickAction(
                            icon = Icons.Default.Monitor,
                            label = "Monitor",
                            detail = "${running.size} running",
                            onClick = onOpenMonitor,
                            modifier = Modifier.weight(1f)
                        )
                        QuickAction(
                            icon = Icons.Default.Settings,
                            label = "System",
                            detail = "Health and settings",
                            onClick = onOpenSystem,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item(key = "running_header") {
                    DsSectionHeader(
                        title = "Running now",
                        index = "02",
                        trailing = {
                            Text(
                                text = if (running.isEmpty()) "None" else "${running.size}",
                                style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMono),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }

                if (running.isEmpty()) {
                    item(key = "running_empty") {
                        DsCard(contentPadding = PaddingValues(DsSpace.lg)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "No containers are running",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(Modifier.height(DsSpace.xxs))
                                    Text(
                                        text = "Start one from the container list.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                DsActionButton(
                                    label = "Open",
                                    onClick = onOpenContainers,
                                    icon = Icons.Default.ArrowForward,
                                    style = DsButtonStyle.Tonal
                                )
                            }
                        }
                    }
                } else {
                    items(running.take(3), key = { "running_${it.name}" }) { container ->
                        RunningRow(
                            container = container,
                            onClick = { onContainerDetails(container.name) },
                            onTerminal = { onTerminal(container.name) },
                            modifier = Modifier.padding(bottom = DsSpace.sm)
                        )
                    }
                    if (running.size > 3) {
                        item(key = "running_more") {
                            DsActionButton(
                                label = "All running containers",
                                onClick = onOpenMonitor,
                                icon = Icons.Default.ArrowForward,
                                style = DsButtonStyle.Outline,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                item(key = "facts") {
                    DsSectionHeader(title = "Host", index = "03")
                    DsCard {
                        DsKeyValueRow("Kernel", SystemInfoManager.kernelVersion)
                        DsDivider(Modifier.padding(vertical = DsSpace.sm))
                        DsKeyValueRow("Architecture", SystemInfoManager.architecture)
                        DsDivider(Modifier.padding(vertical = DsSpace.sm))
                        DsKeyValueRow("Android", SystemInfoManager.androidVersion)
                        DsDivider(Modifier.padding(vertical = DsSpace.sm))
                        DsKeyValueRow("CPU cores", "${snapshot.cpuCores} · load ${String.format("%.2f", snapshot.loadAverage)}")
                        DsDivider(Modifier.padding(vertical = DsSpace.sm))
                        DsKeyValueRow("Backend", backendVersion?.let { "v$it" } ?: "unknown")
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    detail: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    DsCard(onClick = onClick, modifier = modifier, contentPadding = PaddingValues(DsSpace.md)) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(DsSpace.sm))
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = detail,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun RunningRow(
    container: ContainerInfo,
    onClick: () -> Unit,
    onTerminal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val osInfo = remember(container.name) { ContainerOSInfoManager.getCachedOSInfo(container.name, context) }
    val distro = osInfo?.prettyName ?: osInfo?.name ?: "linux"
    Box(modifier = modifier) {
        DsCard(onClick = onClick, contentPadding = PaddingValues(DsSpace.md)) {
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
                        text = buildString {
                            append(distro)
                            container.pid?.let { append(" · pid $it") }
                            append(" · ${container.netMode}")
                        },
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMono),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                DsBadge(label = context.getString(R.string.status_running), color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(DsSpace.sm))
                DsIconAction(
                    icon = Icons.Default.Terminal,
                    contentDescription = context.getString(R.string.terminal),
                    onClick = onTerminal
                )
            }
        }
    }
}
