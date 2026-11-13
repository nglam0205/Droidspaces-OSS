package com.droidspaces.app.ui.screen

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.graphics.Color

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import com.droidspaces.app.ui.component.PullToRefreshWrapper
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.droidspaces.app.R
import com.droidspaces.app.ui.component.*
import com.droidspaces.app.ui.theme.JetBrainsMono
import com.droidspaces.app.util.ContainerManager
import com.droidspaces.app.util.SystemInfoManager
import com.droidspaces.app.util.ResourceLimits
import com.droidspaces.app.viewmodel.AppStateViewModel
import com.droidspaces.app.viewmodel.ContainerListViewModel
import com.droidspaces.app.viewmodel.SystemStatsViewModel
import java.io.File
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainTabScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToDetails: (String) -> Unit,
    onNavigateToTerminal: (String) -> Unit,
    onNavigateToNewContainer: () -> Unit,
    onNavigateToControlPanel: () -> Unit,
    onNavigateToRequirements: () -> Unit,
    onNavigateToContributors: () -> Unit,
    appStateViewModel: AppStateViewModel = viewModel(),
    containerListViewModel: ContainerListViewModel = viewModel(),
    systemStatsViewModel: SystemStatsViewModel = viewModel()
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "DROIDSPACES",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = JetBrainsMono,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "WORKSTATION v2.6",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = JetBrainsMono
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onNavigateToControlPanel,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Monitor,
                                contentDescription = "Monitor",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = onNavigateToSettings,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    tonalElevation = 0.dp,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    val items = listOf(
                        Triple(0, "Overview", Icons.Default.Dashboard),
                        Triple(1, "Containers", Icons.Default.Dns),
                        Triple(2, "Monitor", Icons.Default.Memory)
                    )
                    items.forEach { (index, label, icon) ->
                        val selected = pagerState.currentPage == index
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label
                                )
                            },
                            label = {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontFamily = JetBrainsMono
                                    )
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { page ->
            when (page) {
                0 -> DashboardOverviewTab(
                    onNavigateToDetails = onNavigateToDetails,
                    onNavigateToTerminal = onNavigateToTerminal,
                    onNavigateToNewContainer = onNavigateToNewContainer,
                    onNavigateToControlPanel = onNavigateToControlPanel,
                    containerListViewModel = containerListViewModel
                )
                1 -> ContainersScreenContent(
                    onNavigateToDetails = onNavigateToDetails,
                    onNavigateToTerminal = onNavigateToTerminal,
                    onNavigateToNewContainer = onNavigateToNewContainer,
                    viewModel = containerListViewModel
                )
                2 -> ControlPanelScreen(
                    onBackClick = {}
                )
            }
        }
    }
}

@Composable
fun DashboardOverviewTab(
    onNavigateToDetails: (String) -> Unit,
    onNavigateToTerminal: (String) -> Unit,
    onNavigateToNewContainer: () -> Unit,
    onNavigateToControlPanel: () -> Unit,
    containerListViewModel: ContainerListViewModel
) {
    val context = LocalContext.current
    val containers by containerListViewModel.containers.collectAsState()
    val runningContainers = containers.filter { it.running }

    var hostCpuUsage by remember { mutableStateOf("0.0%") }
    var hostMemUsage by remember { mutableStateOf("0 MB") }
    var hostMemPercent by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        try {
            val memInfo = File("/proc/meminfo").readText()
            var totalKb = 0L
            var freeKb = 0L
            var buffersKb = 0L
            var cachedKb = 0L
            for (line in memInfo.lines()) {
                val parts = line.split("\\s+".toRegex())
                if (parts.size >= 2) {
                    when (parts[0]) {
                        "MemTotal:" -> totalKb = parts[1].toLongOrNull() ?: 0L
                        "MemFree:" -> freeKb = parts[1].toLongOrNull() ?: 0L
                        "Buffers:" -> buffersKb = parts[1].toLongOrNull() ?: 0L
                        "Cached:" -> cachedKb = parts[1].toLongOrNull() ?: 0L
                    }
                }
            }
            if (totalKb > 0) {
                val usedKb = totalKb - freeKb - buffersKb - cachedKb
                val usedMb = usedKb / 1024
                val totalMb = totalKb / 1024
                val ratio = usedKb.toFloat() / totalKb.toFloat()
                hostMemUsage = "$usedMb MB / $totalMb MB"
                hostMemPercent = ratio
            }
        } catch (e: Exception) {
            val total = ResourceLimits.totalMemoryMb(context)
            hostMemUsage = "Total $total MB"
            hostMemPercent = 0.35f
        }

        val cores = ResourceLimits.cpuCores()
        hostCpuUsage = "$cores Cores Active"
    }

    PullToRefreshWrapper(
        onRefresh = { containerListViewModel.refresh() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DsPanel {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Text(
                                text = "ENGINE ONLINE",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = JetBrainsMono,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${containers.size} Total • ${runningContainers.size} Running",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = JetBrainsMono,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Button(
                        onClick = onNavigateToNewContainer,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "NEW",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontFamily = JetBrainsMono,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DsMetricTile(
                    label = "Host RAM",
                    value = hostMemUsage,
                    subtext = "System Memory",
                    icon = Icons.Default.Memory,
                    progress = hostMemPercent,
                    modifier = Modifier.weight(1f)
                )
                DsMetricTile(
                    label = "CPU Cores",
                    value = hostCpuUsage,
                    subtext = "Architecture ${SystemInfoManager.architecture}",
                    icon = Icons.Default.Speed,
                    modifier = Modifier.weight(1f)
                )
            }

            DsSectionLabel(
                text = "Running Instances",
                tag = runningContainers.size.toString(),
                action = {
                    TextButton(onClick = onNavigateToControlPanel) {
                        Text(
                            text = "Monitor →",
                            style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMono),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )

            if (runningContainers.isEmpty()) {
                DsPanel(
                    contentPadding = PaddingValues(24.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inbox,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No running containers",
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMono),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                runningContainers.forEach { container ->
                    DsPanel(
                        modifier = Modifier.clickable { onNavigateToDetails(container.name) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = container.name,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = JetBrainsMono,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = container.distribution.ifEmpty { "linux" },
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMono),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = "RUNNING",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = JetBrainsMono,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onNavigateToTerminal(container.name) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Terminal,
                                        contentDescription = "Terminal",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            DsSectionLabel(text = "Environment Specs")
            DsPanel {
                DsCommandRow(label = "Kernel", value = SystemInfoManager.kernelVersion)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                DsCommandRow(label = "Architecture", value = SystemInfoManager.architecture)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                DsCommandRow(label = "Android", value = SystemInfoManager.androidVersion)
            }
        }
    }
}

@Composable
fun ContainersScreenContent(
    onNavigateToDetails: (String) -> Unit,
    onNavigateToTerminal: (String) -> Unit,
    onNavigateToNewContainer: () -> Unit,
    viewModel: ContainerListViewModel
) {
    val containers by viewModel.containers.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var filterRunning by remember { mutableStateOf(false) }

    val filteredContainers = containers.filter { c ->
        val matchesSearch = c.name.contains(searchQuery, ignoreCase = true) ||
                c.distribution.contains(searchQuery, ignoreCase = true)
        val matchesFilter = if (filterRunning) c.running else true
        matchesSearch && matchesFilter
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        "Search containers...",
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMono)
                    )
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null)
                },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
            FilterChip(
                selected = filterRunning,
                onClick = { filterRunning = !filterRunning },
                label = {
                    Text(
                        "Running",
                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMono)
                    )
                },
                shape = RoundedCornerShape(8.dp)
            )
        }

        if (filteredContainers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No matching containers",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = JetBrainsMono,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredContainers, key = { it.name }) { container ->
                    DsPanel(
                        modifier = Modifier.clickable { onNavigateToDetails(container.name) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = container.name,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = JetBrainsMono,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "distro: ${container.distribution.ifEmpty { "alpine" }} • pid: ${container.pid}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMono),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (container.running) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
                                ) {
                                    Text(
                                        text = if (container.running) "UP" else "STOPPED",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = JetBrainsMono,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = if (container.running) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onNavigateToTerminal(container.name) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Terminal,
                                        contentDescription = "Terminal",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
