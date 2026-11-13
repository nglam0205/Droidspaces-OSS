package com.droidspaces.app.ui.screen

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.droidspaces.app.R
import com.droidspaces.app.service.TerminalSessionService
import com.droidspaces.app.ui.component.ContainerUsersCard
import com.droidspaces.app.ui.component.DsSnackbarHost
import com.droidspaces.app.ui.design.DsActionButton
import com.droidspaces.app.ui.design.DsBadge
import com.droidspaces.app.ui.design.DsButtonStyle
import com.droidspaces.app.ui.design.DsCard
import com.droidspaces.app.ui.design.DsDivider
import com.droidspaces.app.ui.design.DsIconAction
import com.droidspaces.app.ui.design.DsKeyValueRow
import com.droidspaces.app.ui.design.DsPageHeader
import com.droidspaces.app.ui.design.DsSectionHeader
import com.droidspaces.app.ui.design.DsSpace
import com.droidspaces.app.ui.design.DsLoadingState
import com.droidspaces.app.ui.theme.JetBrainsMono
import com.droidspaces.app.ui.util.showError
import com.droidspaces.app.util.AnlandUtils
import com.droidspaces.app.util.ContainerDiskUsageManager
import com.droidspaces.app.util.ContainerInfo
import com.droidspaces.app.util.ContainerOSInfoManager
import com.droidspaces.app.util.ContainerUsersManager
import com.droidspaces.app.util.IconUtils
import com.droidspaces.app.util.ResourceLimits
import com.droidspaces.app.util.TermuxX11Utils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * One container. Identity and live readings first, then the things that can be
 * opened from here: a terminal, a display, the users, and the init system.
 * The page polls only while it is visible, and leaves on its own when the
 * container disappears from the backend.
 */
@Composable
fun ContainerDetailsScreen(
    container: ContainerInfo,
    onNavigateBack: () -> Unit,
    onNavigateToServices: (InitSystem) -> Unit = {},
    onNavigateToTerminal: () -> Unit = {}
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var osInfo by remember {
        mutableStateOf(ContainerOSInfoManager.getCachedOSInfo(container.name, context))
    }
    var initSystemState by remember { mutableStateOf<InitSystemCardState>(InitSystemCardState.Checking) }
    var diskUsage by remember {
        mutableStateOf(
            if (container.useSparseImage) ContainerDiskUsageManager.getCached(container.rootfsPath) else null
        )
    }
    var users by remember { mutableStateOf(ContainerUsersManager.getCachedUsers(container.name)) }

    LaunchedEffect(container.name) {
        val probe = ContainerUsersManager.probe(container.name)
        users = probe?.second ?: users ?: emptyList()
        initSystemState = when (probe?.first) {
            "systemd" -> InitSystemCardState.Available(InitSystem.SYSTEMD)
            "procd" -> InitSystemCardState.Available(InitSystem.PROCD)
            "openrc" -> InitSystemCardState.Available(InitSystem.OPENRC)
            "runit" -> InitSystemCardState.Available(InitSystem.RUNIT)
            else -> InitSystemCardState.NotAvailable
        }
    }

    if (container.useSparseImage) {
        LaunchedEffect(container.name) {
            ContainerDiskUsageManager.getUsage(container.rootfsPath)?.let { diskUsage = it }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(container.name) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                val fresh = ContainerOSInfoManager.fetchAll(context)[container.name]
                if (fresh == null) {
                    context.startService(
                        android.content.Intent(context, TerminalSessionService::class.java).apply {
                            action = TerminalSessionService.ACTION_STOP_CONTAINER_SESSIONS
                            putExtra(TerminalSessionService.EXTRA_CONTAINER_NAME, container.name)
                        }
                    )
                    onNavigateBack()
                    break
                }
                if (fresh != osInfo) osInfo = fresh
                delay(2000)
            }
        }
    }

    val running = container.isRunning
    val distro = osInfo?.prettyName ?: osInfo?.name ?: "linux"
    val statusColor = if (running) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

    Column(modifier = Modifier.fillMaxSize()) {
        DsPageHeader(
            title = container.name,
            eyebrow = distro,
            subtitle = buildString {
                append(if (running) "running" else "stopped")
                container.pid?.let { append(" · pid $it") }
                append(" · ${container.netMode}")
            },
            onBack = onNavigateBack,
            actions = {
                if (running) {
                    DsIconAction(
                        icon = Icons.Default.Terminal,
                        contentDescription = context.getString(R.string.terminal),
                        onClick = onNavigateToTerminal
                    )
                }
            }
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(
                start = DsSpace.lg,
                end = DsSpace.lg,
                bottom = DsSpace.huge
            ),
            verticalArrangement = Arrangement.spacedBy(DsSpace.md)
        ) {
            item(key = "identity") {
                DsCard(accent = statusColor) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = IconUtils.getDistroIcon(distro),
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(34.dp)
                        )
                        Spacer(Modifier.width(DsSpace.md))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = osInfo?.hostname ?: container.hostname.ifEmpty { container.name },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (osInfo?.version != null) "$distro ${osInfo?.version}" else distro,
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMono),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        DsBadge(
                            label = if (running) context.getString(R.string.status_running) else context.getString(R.string.status_stopped),
                            color = statusColor
                        )
                    }

                    DsDivider(Modifier.padding(vertical = DsSpace.md))

                    DsKeyValueRow("Uptime", osInfo?.uptime ?: "---")
                    DsKeyValueRow("Address", osInfo?.ipAddress ?: "---")
                    if (running) {
                        DsKeyValueRow("CPU", osInfo?.cpuUsage?.let { context.getString(R.string.cpu_percent_label, it) } ?: "---")
                        DsKeyValueRow("Memory", osInfo?.ramLabel(context) ?: "---")
                    }
                    DsKeyValueRow("Rootfs", if (container.useSparseImage) "sparse image" else "directory")
                }
            }

            if (container.useSparseImage) {
                item(key = "disk") {
                    SparseDiskCard(usage = diskUsage)
                }
            }

            item(key = "terminal") {
                TerminalCard(
                    containerName = container.name,
                    enabled = running,
                    onOpenTerminal = onNavigateToTerminal
                )
            }

            val anlandSocket = osInfo?.anlandSocket
            if (anlandSocket != null) {
                item(key = "anland") {
                    DisplayCard(
                        icon = null,
                        title = context.getString(R.string.anland_option),
                        description = context.getString(R.string.enable_anland_description),
                        onLaunch = {
                            if (!AnlandUtils.launchWindow(context, container.name, anlandSocket)) {
                                scope.showError(snackbarHostState, context.getString(R.string.anland_not_installed))
                            }
                        }
                    )
                }
            }

            if (osInfo?.x11 == true) {
                item(key = "x11") {
                    DisplayCard(
                        icon = painterResource(R.drawable.ic_x11),
                        title = context.getString(R.string.termux_x11_option),
                        description = context.getString(R.string.launch_x11_description),
                        onLaunch = {
                            if (!TermuxX11Utils.launch(context)) {
                                scope.showError(snackbarHostState, context.getString(R.string.termux_x11_not_installed))
                            }
                        }
                    )
                }
            }

            item(key = "users") {
                ContainerUsersCard(
                    containerName = container.name,
                    users = users,
                    onRefresh = { users = ContainerUsersManager.getUsers(container.name, useCache = false) },
                    snackbarHostState = snackbarHostState
                )
            }

            item(key = "init") {
                InitSystemCard(
                    state = initSystemState,
                    onNavigateToServices = onNavigateToServices
                )
            }
        }
    }

    DsSnackbarHost(hostState = snackbarHostState)
}

enum class InitSystem { SYSTEMD, PROCD, OPENRC, RUNIT }

private sealed class InitSystemCardState {
    data object Checking : InitSystemCardState()
    data class Available(val initSystem: InitSystem) : InitSystemCardState()
    data object NotAvailable : InitSystemCardState()
}

@Composable
private fun TerminalCard(
    containerName: String,
    enabled: Boolean,
    onOpenTerminal: () -> Unit
) {
    val context = LocalContext.current
    val sessionCount by remember {
        derivedStateOf { TerminalSessionService.globalSessionList.values.count { it.containerName == containerName } }
    }
    DsCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Terminal,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(DsSpace.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = context.getString(R.string.terminal),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = when {
                        !enabled -> "Start the container to open a shell"
                        sessionCount == 0 -> context.getString(R.string.terminal_card_desc)
                        sessionCount == 1 -> context.getString(R.string.session_running_singular)
                        else -> context.getString(R.string.sessions_running_plural, sessionCount)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            DsActionButton(
                label = context.getString(if (sessionCount > 0) R.string.restore else R.string.open),
                onClick = onOpenTerminal,
                enabled = enabled,
                icon = Icons.Default.Terminal,
                style = if (sessionCount > 0) DsButtonStyle.Filled else DsButtonStyle.Tonal
            )
        }
    }
}

@Composable
private fun SparseDiskCard(usage: ContainerDiskUsageManager.DiskUsage?) {
    val context = LocalContext.current
    val fraction = usage?.let {
        if (it.totalBytes > 0L) (it.usedBytes.toFloat() / it.totalBytes.toFloat()).coerceIn(0f, 1f) else 0f
    } ?: 0f
    DsCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = context.getString(R.string.disk_usage),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = usage?.let {
                        context.getString(
                            R.string.disk_usage_used_of_total,
                            ResourceLimits.formatBytes(it.usedBytes),
                            ResourceLimits.formatBytes(it.totalBytes)
                        )
                    } ?: context.getString(R.string.disk_usage_calculating),
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMono),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "${(fraction * 100).toInt()}%",
                style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMono),
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(DsSpace.sm))
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    }
}

@Composable
private fun DisplayCard(
    icon: Painter?,
    title: String,
    description: String,
    onLaunch: () -> Unit
) {
    val context = LocalContext.current
    DsCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
            } else {
                Icon(Icons.Default.DesktopWindows, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(DsSpace.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            DsActionButton(
                label = context.getString(R.string.launch_anland_window),
                onClick = onLaunch,
                icon = Icons.Default.DesktopWindows,
                style = DsButtonStyle.Tonal
            )
        }
    }
}

@Composable
private fun InitSystemCard(
    state: InitSystemCardState,
    onNavigateToServices: (InitSystem) -> Unit
) {
    val context = LocalContext.current
    val available = state is InitSystemCardState.Available
    DsCard(accent = if (available) MaterialTheme.colorScheme.primary else null) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = when (state) {
                    is InitSystemCardState.Available -> Icons.Default.Settings
                    is InitSystemCardState.NotAvailable -> Icons.Default.Block
                    is InitSystemCardState.Checking -> Icons.Default.HourglassEmpty
                },
                contentDescription = null,
                tint = if (available) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(DsSpace.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when (state) {
                        is InitSystemCardState.Available -> when (state.initSystem) {
                            InitSystem.SYSTEMD -> context.getString(R.string.systemd)
                            InitSystem.PROCD -> context.getString(R.string.openwrt)
                            InitSystem.OPENRC -> context.getString(R.string.openrc)
                            InitSystem.RUNIT -> context.getString(R.string.runit)
                        }
                        else -> context.getString(R.string.init_system)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = when (state) {
                        is InitSystemCardState.Available -> "Service manager detected"
                        is InitSystemCardState.NotAvailable -> context.getString(R.string.not_available)
                        is InitSystemCardState.Checking -> context.getString(R.string.checking)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            when (state) {
                is InitSystemCardState.Available -> DsActionButton(
                    label = context.getString(R.string.manage),
                    onClick = { onNavigateToServices(state.initSystem) },
                    style = DsButtonStyle.Filled
                )
                is InitSystemCardState.Checking -> DsLoadingState(modifier = Modifier.size(32.dp))
                is InitSystemCardState.NotAvailable -> Text(
                    text = "---",
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMono),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
