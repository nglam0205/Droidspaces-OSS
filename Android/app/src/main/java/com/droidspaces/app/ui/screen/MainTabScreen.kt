package com.droidspaces.app.ui.screen

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.droidspaces.app.R
import com.droidspaces.app.ui.component.PullToRefreshWrapper
import com.droidspaces.app.ui.design.DsBottomDock
import com.droidspaces.app.ui.design.DsDestination
import com.droidspaces.app.ui.design.DsNavRail
import com.droidspaces.app.ui.design.DsRailDivider
import com.droidspaces.app.ui.design.isCompactLayout
import com.droidspaces.app.ui.viewmodel.AppStateViewModel
import com.droidspaces.app.ui.viewmodel.ContainerViewModel
import com.droidspaces.app.util.Constants
import com.droidspaces.app.util.ContainerManager
import com.droidspaces.app.util.HostCapabilities
import com.droidspaces.app.util.SystemInfoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** The four destinations of the shell. Order is the order in the dock and rail. */
enum class TabItem(val titleResId: Int, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Overview(R.string.overview, Icons.Default.Home),
    Containers(R.string.containers, Icons.Default.Dns),
    Monitor(R.string.monitor, Icons.Default.Monitor),
    System(R.string.system, Icons.Default.Settings)
}

/**
 * The application shell. It owns the four pages, the shared view models, and the
 * one navigation surface, which is a dock on a phone and a rail when there is
 * width for one. Every tab keeps its own back stack position because the pages
 * live in a pager rather than being recreated on selection.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainTabScreen(
    appStateViewModel: AppStateViewModel,
    containerViewModel: ContainerViewModel,
    skipInitialRefresh: Boolean = false,
    requestedTab: TabItem? = null,
    onRequestedTabConsumed: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToInstallation: () -> Unit = {},
    onNavigateToContainerInstallation: (Uri) -> Unit = {},
    onNavigateToEditContainer: (String) -> Unit = {},
    onNavigateToContainerDetails: (String) -> Unit = {},
    onNavigateToTerminal: (String) -> Unit = {},
    onNavigateToRequirements: () -> Unit = {},
    onNavigateToAutoBootPriority: () -> Unit = {},
    onNavigateToTerminalAppearance: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val tabs = TabItem.entries
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val selectedTab = tabs[pagerState.currentPage.coerceIn(tabs.indices)]
    val compact = isCompactLayout()

    val caps by HostCapabilities.state.collectAsStateWithLifecycle()
    val isKernelSupported = caps?.requirementsMet != false
    val isBackendAvailable = appStateViewModel.isBackendAvailable
    val isRootAvailable = appStateViewModel.isRootAvailable
    val containers = containerViewModel.containerList

    var expandedContainerName by rememberSaveable { mutableStateOf<String?>(null) }
    var openAddOnContainers by rememberSaveable { mutableStateOf(false) }
    var bottomBarInset by remember { mutableStateOf(0.dp) }
    var hasTriggeredInitialLoad by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(requestedTab) {
        val target = requestedTab ?: return@LaunchedEffect
        val index = tabs.indexOf(target)
        if (index >= 0) pagerState.scrollToPage(index)
        onRequestedTabConsumed()
    }

    BackHandler(enabled = selectedTab != TabItem.Overview) {
        scope.launch { pagerState.scrollToPage(tabs.indexOf(TabItem.Overview)) }
    }

    LaunchedEffect(skipInitialRefresh) {
        if (!hasTriggeredInitialLoad) {
            hasTriggeredInitialLoad = true
            if (!skipInitialRefresh) appStateViewModel.resetForPostInstallation()
            appStateViewModel.checkBackendStatus(force = true)
            if (appStateViewModel.isBackendAvailable) {
                containerViewModel.scan(showIndicator = true)
            } else {
                containerViewModel.fetchContainerList()
            }
        }
    }

    LaunchedEffect(isBackendAvailable) {
        if (isBackendAvailable) {
            HostCapabilities.refreshIfStale(context)
            if (containerViewModel.containerList.isEmpty()) containerViewModel.fetchContainerList()
        }
    }

    suspend fun performRefresh(tab: TabItem) {
        appStateViewModel.checkRootStatus()
        appStateViewModel.checkBackendStatus(force = true)
        if (appStateViewModel.isBackendAvailable) {
            SystemInfoManager.getDroidspacesVersion(context, force = true)
            SystemInfoManager.getBackendMode(context, force = true)
            HostCapabilities.refresh(context)
            when (tab) {
                TabItem.Overview, TabItem.Containers, TabItem.System -> containerViewModel.scan(showIndicator = true)
                TabItem.Monitor -> {
                    val raw = withContext(Dispatchers.IO) { ContainerManager.listContainers() }
                    if (raw.none { it.isRunning }) {
                        containerViewModel.scan(showIndicator = true)
                    } else {
                        withContext(Dispatchers.Main) { containerViewModel.updateState(raw) }
                        scope.launch { containerViewModel.scan(showIndicator = false) }
                    }
                }
            }
        }
    }

    val destinations = remember(tabs) {
        tabs.map { DsDestination(id = it.name, label = context.getString(it.titleResId), icon = it.icon) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxSize()) {
            if (!compact) {
                DsNavRail(
                    destinations = destinations,
                    selectedId = selectedTab.name,
                    onSelect = { destination ->
                        val index = tabs.indexOfFirst { it.name == destination.id }
                        if (index >= 0) {
                            scope.launch { pagerState.scrollToPage(index) }
                            expandedContainerName = null
                        }
                    }
                )
                DsRailDivider()
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f).fillMaxSize()
            ) { page ->
                when (tabs[page]) {
                    TabItem.Overview -> OverviewScreen(
                        containers = containers,
                        isBackendAvailable = isBackendAvailable,
                        isRootAvailable = isRootAvailable,
                        isKernelSupported = isKernelSupported,
                        onRefresh = { scope.launch { performRefresh(TabItem.Overview) } },
                        onOpenContainers = { scope.launch { pagerState.scrollToPage(tabs.indexOf(TabItem.Containers)) } },
                        onOpenMonitor = { scope.launch { pagerState.scrollToPage(tabs.indexOf(TabItem.Monitor)) } },
                        onOpenSystem = { scope.launch { pagerState.scrollToPage(tabs.indexOf(TabItem.System)) } },
                        onOpenSettings = onNavigateToSettings,
                        onNewContainer = {
                            openAddOnContainers = true
                            scope.launch { pagerState.scrollToPage(tabs.indexOf(TabItem.Containers)) }
                        },
                        onContainerDetails = onNavigateToContainerDetails,
                        onTerminal = onNavigateToTerminal,
                        bottomBarInset = if (compact) bottomBarInset else 0.dp
                    )

                    TabItem.Containers -> PullToRefreshWrapper(
                        onRefresh = { performRefresh(TabItem.Containers) }
                    ) {
                        ContainersScreen(
                            isBackendAvailable = isBackendAvailable,
                            isRootAvailable = isRootAvailable,
                            isKernelSupported = isKernelSupported,
                            onNavigateToInstallation = onNavigateToContainerInstallation,
                            onNavigateToEditContainer = onNavigateToEditContainer,
                            onNavigateToContainerDetails = onNavigateToContainerDetails,
                            containerViewModel = containerViewModel,
                            expandedContainerName = expandedContainerName,
                            onExpandedContainerNameChange = { expandedContainerName = it },
                            bottomBarInset = if (compact) bottomBarInset else 0.dp,
                            autoOpenAdd = openAddOnContainers,
                            onAutoOpenAddConsumed = { openAddOnContainers = false }
                        )
                    }

                    TabItem.Monitor -> MonitorScreen(
                        containers = containers,
                        isBackendAvailable = isBackendAvailable,
                        isRootAvailable = isRootAvailable,
                        isKernelSupported = isKernelSupported,
                        onRefresh = { scope.launch { performRefresh(TabItem.Monitor) } },
                        onContainerDetails = onNavigateToContainerDetails,
                        onTerminal = onNavigateToTerminal,
                        bottomBarInset = if (compact) bottomBarInset else 0.dp
                    )

                    TabItem.System -> SystemScreen(
                        containers = containers,
                        isBackendAvailable = isBackendAvailable,
                        isRootAvailable = isRootAvailable,
                        isKernelSupported = isKernelSupported,
                        onOpenSettings = onNavigateToSettings,
                        onOpenRequirements = onNavigateToRequirements,
                        onOpenAutoBoot = onNavigateToAutoBootPriority,
                        onOpenTerminalAppearance = onNavigateToTerminalAppearance,
                        onOpenInstallation = onNavigateToInstallation,
                        onRefresh = { scope.launch { performRefresh(TabItem.System) } },
                        bottomBarInset = if (compact) bottomBarInset else 0.dp
                    )
                }
            }
        }

        if (compact) {
            val density = LocalDensity.current
            DsBottomDock(
                destinations = destinations,
                selectedId = selectedTab.name,
                onSelect = { destination ->
                    val index = tabs.indexOfFirst { it.name == destination.id }
                    if (index >= 0) {
                        scope.launch { pagerState.scrollToPage(index) }
                        expandedContainerName = null
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .onSizeChanged { bottomBarInset = with(density) { it.height.toDp() } }
            )
        }
    }
}
