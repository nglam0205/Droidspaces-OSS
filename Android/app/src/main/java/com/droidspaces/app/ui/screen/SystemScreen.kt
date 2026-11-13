package com.droidspaces.app.ui.screen

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.droidspaces.app.R
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
import com.droidspaces.app.ui.theme.JetBrainsMono
import com.droidspaces.app.util.ContainerInfo
import com.droidspaces.app.util.SystemInfoManager

/**
 * The fourth destination: everything that is about the installation rather than
 * about a container. Status first, then the pages that change how the app
 * behaves, then the paths into maintenance.
 */
@Composable
fun SystemScreen(
    containers: List<ContainerInfo>,
    isBackendAvailable: Boolean,
    isRootAvailable: Boolean,
    isKernelSupported: Boolean,
    onOpenSettings: () -> Unit,
    onOpenRequirements: () -> Unit,
    onOpenAutoBoot: () -> Unit,
    onOpenTerminalAppearance: () -> Unit,
    onOpenInstallation: () -> Unit,
    onRefresh: () -> Unit,
    bottomBarInset: Dp = 0.dp
) {
    val context = LocalContext.current
    var backendVersion by remember { mutableStateOf<String?>(null) }
    var backendMode by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        backendVersion = SystemInfoManager.getDroidspacesVersion(context)
        backendMode = SystemInfoManager.getBackendMode(context)
    }

    val running = containers.count { it.isRunning }
    val statusColor = when {
        !isRootAvailable || !isBackendAvailable -> MaterialTheme.colorScheme.error
        !isKernelSupported -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }

    Column(modifier = Modifier.fillMaxSize()) {
        DsPageHeader(
            title = context.getString(R.string.system),
            eyebrow = "Installation",
            subtitle = "Backend, kernel and application",
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

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = DsSpace.lg, end = DsSpace.lg, bottom = bottomBarInset + DsSpace.lg)
        ) {
            DsCard(accent = statusColor) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        DsBadge(
                            label = if (isBackendAvailable) "Backend available" else "Backend missing",
                            color = statusColor
                        )
                        Spacer(Modifier.height(DsSpace.sm))
                        Text(
                            text = if (isKernelSupported) "Installation healthy" else "Kernel constraints",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(DsSpace.xxs))
                        Text(
                            text = "${containers.size} containers · $running running",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMono),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
                if (!isBackendAvailable) {
                    DsActionButton(
                        label = if (isBackendAvailable) context.getString(R.string.reinstall_backend) else "Install backend",
                        onClick = onOpenInstallation,
                        style = DsButtonStyle.Filled,
                        icon = Icons.Default.ArrowForward,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = DsSpace.md)
                    )
                }
            }

            DsSectionHeader(title = "Runtime", index = "01")
            DsCard {
                DsKeyValueRow("Version", backendVersion?.let { "v$it" } ?: "unknown")
                DsDivider(Modifier.padding(vertical = DsSpace.sm))
                DsKeyValueRow("Mode", backendMode ?: "unknown")
                DsDivider(Modifier.padding(vertical = DsSpace.sm))
                DsKeyValueRow("Root", if (isRootAvailable) "granted" else "denied")
                DsDivider(Modifier.padding(vertical = DsSpace.sm))
                DsKeyValueRow("Kernel probe", if (isKernelSupported) "passed" else "failed")
            }

            DsSectionHeader(title = "Device", index = "02")
            DsCard {
                DsKeyValueRow("Kernel", SystemInfoManager.kernelVersion)
                DsDivider(Modifier.padding(vertical = DsSpace.sm))
                DsKeyValueRow("Architecture", SystemInfoManager.architecture)
                DsDivider(Modifier.padding(vertical = DsSpace.sm))
                DsKeyValueRow("Android", SystemInfoManager.androidVersion)
                DsDivider(Modifier.padding(vertical = DsSpace.sm))
                DsKeyValueRow("SELinux", SystemInfoManager.cachedSelinuxStatus ?: "unknown")
            }

            DsSectionHeader(title = "Controls", index = "03")
            SystemLinkRow(
                icon = Icons.Default.Settings,
                title = context.getString(R.string.settings),
                summary = "Theme, behaviour, language and integrations",
                onClick = onOpenSettings
            )
            Spacer(Modifier.height(DsSpace.sm))
            SystemLinkRow(
                icon = Icons.Default.Memory,
                title = context.getString(R.string.requirements),
                summary = "Kernel features Droidspaces needs",
                onClick = onOpenRequirements
            )
            Spacer(Modifier.height(DsSpace.sm))
            SystemLinkRow(
                icon = Icons.Default.Schedule,
                title = "Boot order",
                summary = "Which containers start with the phone",
                onClick = onOpenAutoBoot
            )
            Spacer(Modifier.height(DsSpace.sm))
            SystemLinkRow(
                icon = Icons.Default.Terminal,
                title = "Terminal appearance",
                summary = "Font, size and colour of the terminal",
                onClick = onOpenTerminalAppearance
            )

            DsSectionHeader(title = "Maintenance", index = "04")
            DsCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Dns,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(DsSpace.sm))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Backend binary",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Reinstall or update the bundled engine",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DsActionButton(
                        label = if (isBackendAvailable) "Reinstall" else "Install",
                        onClick = onOpenInstallation,
                        icon = Icons.Default.Refresh,
                        style = DsButtonStyle.Outline
                    )
                }
            }
        }
    }
}

@Composable
private fun SystemLinkRow(
    icon: ImageVector,
    title: String,
    summary: String,
    onClick: () -> Unit
) {
    DsCard(contentPadding = PaddingValues(DsSpace.md), onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(DsSpace.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                Icons.Default.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
