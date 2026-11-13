package com.droidspaces.app.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.droidspaces.app.R
import com.droidspaces.app.ui.component.ConfirmPhraseField
import com.droidspaces.app.ui.component.ContainerCard
import com.droidspaces.app.ui.component.ContainerCardActions
import com.droidspaces.app.ui.component.DialogFooterRow
import com.droidspaces.app.ui.component.DsDialog
import com.droidspaces.app.ui.component.DsSnackbarHost
import com.droidspaces.app.ui.component.DsTextFieldDefaults
import com.droidspaces.app.ui.component.ExportContainerDialog
import com.droidspaces.app.ui.component.RootfsRepoSheet
import com.droidspaces.app.ui.component.TerminalDialog
import com.droidspaces.app.ui.design.DsActionButton
import com.droidspaces.app.ui.design.DsButtonStyle
import com.droidspaces.app.ui.design.DsEmptyState
import com.droidspaces.app.ui.design.DsErrorState
import com.droidspaces.app.ui.design.DsFilterChip
import com.droidspaces.app.ui.design.DsIconAction
import com.droidspaces.app.ui.design.DsLoadingState
import com.droidspaces.app.ui.design.DsPageHeader
import com.droidspaces.app.ui.design.DsSearchField
import com.droidspaces.app.ui.design.DsSpace
import com.droidspaces.app.ui.theme.DsIcons
import com.droidspaces.app.ui.util.AnimatedListEntry
import com.droidspaces.app.ui.util.ErrorLogsDialog
import com.droidspaces.app.ui.util.ProgressDialog
import com.droidspaces.app.ui.util.rememberAnimatedEntries
import com.droidspaces.app.ui.util.showError
import com.droidspaces.app.ui.util.showSuccess
import com.droidspaces.app.ui.viewmodel.ContainerOperationsViewModel
import com.droidspaces.app.ui.viewmodel.ContainerViewModel
import com.droidspaces.app.ui.viewmodel.SparseOp
import com.droidspaces.app.ui.viewmodel.SystemStatsViewModel
import com.droidspaces.app.ui.viewmodel.UninstallState
import com.droidspaces.app.util.ContainerInfo
import com.droidspaces.app.util.FilePickerUtils
import com.droidspaces.app.util.PreferencesManager
import com.droidspaces.app.util.ValidationUtils
import kotlinx.coroutines.launch

private enum class ContainerFilter(val labelRes: Int) {
    All(R.string.all),
    Running(R.string.running),
    Stopped(R.string.stopped)
}

/**
 * The container list. Search, a state filter and a sort that swaps between name
 * and running-first; everything else is the lifecycle the screen always had.
 * Installation moved out of the floating menu and into the header, so the list
 * owns the whole page and nothing floats over the last row.
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ContainersScreen(
    isBackendAvailable: Boolean,
    isRootAvailable: Boolean = true,
    isKernelSupported: Boolean = true,
    onNavigateToInstallation: (Uri) -> Unit = {},
    onNavigateToEditContainer: (String) -> Unit = {},
    onNavigateToContainerDetails: (String) -> Unit = {},
    containerViewModel: ContainerViewModel,
    expandedContainerName: String?,
    onExpandedContainerNameChange: (String?) -> Unit,
    bottomBarInset: Dp = 0.dp,
    autoOpenAdd: Boolean = false,
    onAutoOpenAddConsumed: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val systemStatsViewModel: SystemStatsViewModel = viewModel()
    val prefsManager = PreferencesManager.getInstance(context)
    val snackbarHostState = remember { SnackbarHostState() }
    val opsViewModel: ContainerOperationsViewModel = viewModel()

    var showUninstallConfirmation by remember { mutableStateOf<ContainerInfo?>(null) }
    var pendingSparseOperation by remember { mutableStateOf<Pair<ContainerInfo, SparseOp>?>(null) }
    var pendingExportContainer by remember { mutableStateOf<ContainerInfo?>(null) }
    var pendingExportEnv by remember { mutableStateOf(false) }
    var askExportEnv by remember { mutableStateOf<ContainerInfo?>(null) }
    var showRepoSheet by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }

    // The overview's New container action lands on this tab with the picker already
    // open, so the shortcut is one tap rather than two.
    LaunchedEffect(autoOpenAdd) {
        if (autoOpenAdd) {
            showAddDialog = true
            onAutoOpenAddConsumed()
        }
    }

    val exportFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/gzip")
    ) { uri: Uri? ->
        val container = pendingExportContainer
        val includeEnv = pendingExportEnv
        pendingExportContainer = null
        if (uri != null && container != null) {
            scope.launch {
                opsViewModel.executeExport(container, uri, includeEnv, onError = { msg -> scope.showError(snackbarHostState, msg) })
            }
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val (isValid, fileName) = FilePickerUtils.isValidTarball(context, uri)
                if (isValid && fileName != null) {
                    onNavigateToInstallation(uri)
                } else {
                    val errorMessage = if (fileName != null) {
                        context.getString(R.string.file_picker_error, fileName)
                    } else {
                        context.getString(R.string.file_picker_error_unknown)
                    }
                    scope.showError(snackbarHostState, errorMessage)
                }
            }
        }
    }

    val containers = containerViewModel.containerList
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var filterOrdinal by rememberSaveable { mutableIntStateOf(0) }
    var sortRunningFirst by rememberSaveable { mutableStateOf(false) }
    val filter = ContainerFilter.entries[filterOrdinal.coerceIn(ContainerFilter.entries.indices)]
    val trimmedQuery = searchQuery.trim()

    val visibleContainers = remember(containers, trimmedQuery, filter, sortRunningFirst) {
        containers
            .filter { container ->
                val matchesQuery = trimmedQuery.isEmpty() ||
                    container.name.contains(trimmedQuery, ignoreCase = true) ||
                    container.hostname.contains(trimmedQuery, ignoreCase = true)
                val matchesFilter = when (filter) {
                    ContainerFilter.All -> true
                    ContainerFilter.Running -> container.isRunning
                    ContainerFilter.Stopped -> !container.isRunning
                }
                matchesQuery && matchesFilter
            }
            .let { list ->
                if (sortRunningFirst) list.sortedWith(compareByDescending<ContainerInfo> { it.isRunning }.thenBy { it.name.lowercase() })
                else list.sortedBy { it.name.lowercase() }
            }
    }
    val entries = rememberAnimatedEntries(visibleContainers) { it.name }

    Column(modifier = Modifier.fillMaxSize()) {
        DsPageHeader(
            title = context.getString(R.string.containers),
            eyebrow = "Installed images",
            subtitle = "${containers.size} installed · ${containers.count { it.isRunning }} running",
            actions = {
                DsIconAction(
                    icon = Icons.Default.Refresh,
                    contentDescription = context.getString(R.string.refresh),
                    onClick = { containerViewModel.refresh() }
                )
                DsIconAction(
                    icon = Icons.Default.Add,
                    contentDescription = context.getString(R.string.add_container),
                    onClick = { showAddDialog = true }
                )
            }
        )

        when {
            !isRootAvailable -> DsErrorState(
                title = context.getString(R.string.root_access_not_available),
                description = context.getString(R.string.root_access_not_available_description),
                modifier = Modifier.weight(1f)
            )
            !isBackendAvailable -> DsErrorState(modifier = Modifier.weight(1f))
            !isKernelSupported -> DsErrorState(
                title = context.getString(R.string.kernel_unsupported),
                description = context.getString(R.string.kernel_unsupported_description),
                modifier = Modifier.weight(1f)
            )
            entries.isEmpty() && containers.isEmpty() -> {
                if (containerViewModel.isRefreshing) {
                    DsLoadingState(modifier = Modifier.weight(1f), label = context.getString(R.string.loading))
                } else {
                    DsEmptyState(
                        icon = DsIcons.Logo,
                        title = context.getString(R.string.no_containers_installed),
                        description = context.getString(R.string.install_container_description),
                        actionLabel = context.getString(R.string.add_container),
                        onAction = { showAddDialog = true },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            else -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = DsSpace.lg),
                    horizontalArrangement = Arrangement.spacedBy(DsSpace.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DsSearchField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = stringResource(R.string.search) + "...",
                        modifier = Modifier.weight(1f)
                    )
                    DsIconAction(
                        icon = Icons.Default.SwapVert,
                        contentDescription = "Sort",
                        onClick = { sortRunningFirst = !sortRunningFirst },
                        tint = if (sortRunningFirst) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }

                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = DsSpace.lg, end = DsSpace.lg, top = DsSpace.md),
                    horizontalArrangement = Arrangement.spacedBy(DsSpace.sm),
                    verticalArrangement = Arrangement.spacedBy(DsSpace.sm)
                ) {
                    ContainerFilter.entries.forEach { option ->
                        DsFilterChip(
                            label = context.getString(option.labelRes),
                            selected = filter == option,
                            onClick = { filterOrdinal = option.ordinal },
                            count = when (option) {
                                ContainerFilter.All -> containers.size
                                ContainerFilter.Running -> containers.count { it.isRunning }
                                ContainerFilter.Stopped -> containers.count { !it.isRunning }
                            }
                        )
                    }
                }

                if (visibleContainers.isEmpty()) {
                    DsEmptyState(
                        icon = Icons.Default.Search,
                        title = stringResource(R.string.no_matches),
                        description = "\"$trimmedQuery\"",
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = DsSpace.lg),
                        contentPadding = PaddingValues(top = DsSpace.md, bottom = bottomBarInset + DsSpace.lg),
                        verticalArrangement = Arrangement.spacedBy(DsSpace.md)
                    ) {
                        items(entries, key = { it.key }) { entry ->
                            val container = entry.item
                            val isRunning = opsViewModel.runningOperationContainer == container.name
                            fun run(op: String) = scope.launch {
                                opsViewModel.executeOperation(
                                    container, op,
                                    onRefresh = { containerViewModel.refresh() },
                                    onClearUsage = { systemStatsViewModel.clearContainerUsage(it) },
                                    onFailureSnackbar = { scope.showError(snackbarHostState, it) }
                                )
                            }
                            AnimatedListEntry(entry) {
                                ContainerCard(
                                    container = container,
                                    isOperationRunning = isRunning,
                                    isExpanded = expandedContainerName == container.name,
                                    actions = ContainerCardActions(
                                        onToggleExpand = {
                                            onExpandedContainerNameChange(
                                                if (expandedContainerName == container.name) null else container.name
                                            )
                                        },
                                        onShowLogs = { opsViewModel.showLogViewerFor = container.name },
                                        onStart = { run("start") },
                                        onStop = { run("stop") },
                                        onRestart = { run("restart") },
                                        onEdit = {
                                            onExpandedContainerNameChange(null)
                                            onNavigateToEditContainer(container.name)
                                        },
                                        onEnter = {
                                            onNavigateToContainerDetails(container.name)
                                        },
                                        onUninstall = {
                                            onExpandedContainerNameChange(null)
                                            showUninstallConfirmation = container
                                        },
                                        onMigrate = {
                                            onExpandedContainerNameChange(null)
                                            pendingSparseOperation = container to SparseOp.MIGRATE
                                        },
                                        onResize = {
                                            onExpandedContainerNameChange(null)
                                            pendingSparseOperation = container to SparseOp.RESIZE
                                        },
                                        onExport = {
                                            onExpandedContainerNameChange(null)
                                            if (ValidationUtils.countEnvVars(container.envFileContent) > 0) {
                                                askExportEnv = container
                                            } else {
                                                pendingExportEnv = false
                                                pendingExportContainer = container
                                                exportFileLauncher.launch(exportFileName(container))
                                            }
                                        }
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        DsSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }

    if (showAddDialog) {
        AddContainerDialog(
            onDismiss = { showAddDialog = false },
            onFromFile = {
                showAddDialog = false
                filePickerLauncher.launch("*/*")
            },
            onFromRepo = {
                showAddDialog = false
                showRepoSheet = true
            }
        )
    }

    if (showRepoSheet) {
        RootfsRepoSheet(
            onDismiss = { showRepoSheet = false },
            onInstall = { uri -> onNavigateToInstallation(uri) }
        )
    }

    opsViewModel.showLogViewerFor?.let { containerName ->
        val memoryLogs = opsViewModel.containerLogs[containerName]
        val cachedLogs = remember(containerName) { prefsManager.loadContainerLogs(containerName) }
        val logs: List<Pair<Int, String>> = memoryLogs ?: cachedLogs
        val isBlocking = opsViewModel.runningOperationContainer == containerName
        TerminalDialog(
            title = context.getString(R.string.logs_title, containerName),
            logs = logs,
            onDismiss = {
                opsViewModel.showLogViewerFor = null
                containerViewModel.refresh()
            },
            onClear = { opsViewModel.clearLogsBuffer(containerName) },
            isBlocking = isBlocking
        )
    }

    askExportEnv?.let { container ->
        ExportContainerDialog(
            containerName = container.name,
            envCount = ValidationUtils.countEnvVars(container.envFileContent),
            onConfirm = { includeEnv ->
                askExportEnv = null
                pendingExportEnv = includeEnv
                pendingExportContainer = container
                exportFileLauncher.launch(exportFileName(container))
            },
            onDismiss = { askExportEnv = null }
        )
    }

    showUninstallConfirmation?.let { container ->
        UninstallConfirmationDialog(
            containerName = container.name,
            onConfirm = {
                showUninstallConfirmation = null
                scope.launch {
                    opsViewModel.executeUninstall(
                        container,
                        onError = { msg -> scope.showError(snackbarHostState, msg) },
                        onSuccess = { msg -> scope.showSuccess(snackbarHostState, msg) },
                        onRefresh = { containerViewModel.refresh() }
                    )
                }
            },
            onDismiss = { showUninstallConfirmation = null }
        )
    }

    (opsViewModel.uninstallState as? UninstallState.InProgress)?.let { state ->
        ProgressDialog(message = state.message)
    }

    opsViewModel.uninstallLogsDialog?.let { logs ->
        ErrorLogsDialog(logs = logs, onDismiss = { opsViewModel.dismissUninstallLogs() })
    }

    pendingSparseOperation?.let { (container, op) ->
        SparseSizeDialog(
            title = context.getString(
                if (op == SparseOp.MIGRATE) R.string.migrate_dialog_title else R.string.resize_dialog_title
            ),
            message = context.getString(
                if (op == SparseOp.MIGRATE) R.string.migrate_dialog_message else R.string.resize_dialog_message,
                container.name
            ),
            initialSize = container.sparseImageSizeGB ?: 8,
            currentSize = if (op == SparseOp.RESIZE) container.sparseImageSizeGB else null,
            onConfirm = { size ->
                pendingSparseOperation = null
                scope.launch {
                    opsViewModel.executeSparseOperation(container, op, size, onRefresh = { containerViewModel.refresh() })
                }
            },
            onDismiss = { pendingSparseOperation = null }
        )
    }
}

@Composable
private fun AddContainerDialog(
    onDismiss: () -> Unit,
    onFromFile: () -> Unit,
    onFromRepo: () -> Unit
) {
    DsDialog(
        onDismiss = onDismiss,
        footer = {
            com.droidspaces.app.ui.component.DialogDismissButton(
                label = stringResource(R.string.close),
                onDismiss = onDismiss
            )
        }
    ) {
        Text(
            text = stringResource(R.string.add_container),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.install_container_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        DsActionButton(
            label = stringResource(R.string.install_from_file),
            onClick = onFromFile,
            icon = Icons.Default.FolderOpen,
            style = DsButtonStyle.Filled,
            modifier = Modifier.fillMaxWidth()
        )
        DsActionButton(
            label = stringResource(R.string.repo_fab_label),
            onClick = onFromRepo,
            icon = Icons.Default.CloudDownload,
            style = DsButtonStyle.Tonal,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun exportFileName(container: ContainerInfo): String {
    val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US).format(java.util.Date())
    return "${container.name}_${timestamp}.tar.gz"
}

@Composable
private fun SparseSizeDialog(
    title: String,
    message: String,
    initialSize: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit,
    currentSize: Int? = null
) {
    val context = LocalContext.current
    var sizeText by remember { mutableStateOf(initialSize.toString()) }
    val size = sizeText.toIntOrNull()
    val isOutOfRange = size == null || size !in 4..512
    val isSameSize = currentSize != null && size == currentSize
    val isValid = !isOutOfRange && !isSameSize

    DsDialog(
        onDismiss = onDismiss,
        footer = {
            DialogFooterRow(
                dismissLabel = context.getString(R.string.cancel),
                confirmLabel = context.getString(R.string.continue_button),
                onDismiss = onDismiss,
                onConfirm = { size?.let { onConfirm(it) } },
                confirmEnabled = isValid
            )
        }
    ) {
        Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedTextField(
            value = sizeText,
            onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() }) sizeText = it },
            label = { Text(context.getString(R.string.size_gb)) },
            placeholder = { Text(context.getString(R.string.size_range_4_512_hint)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            colors = DsTextFieldDefaults.colors(),
            isError = !isValid && sizeText.isNotEmpty(),
            supportingText = {
                if (sizeText.isNotEmpty()) {
                    if (isOutOfRange) {
                        Text(context.getString(R.string.enter_size_between_4_512_gb))
                    } else if (isSameSize) {
                        Text(context.getString(R.string.resize_same_as_current, currentSize))
                    }
                }
            },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
}

@Composable
private fun UninstallConfirmationDialog(
    containerName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var confirmText by remember { mutableStateOf("") }
    val isConfirmed = confirmText == containerName

    DsDialog(
        onDismiss = onDismiss,
        borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.3f),
        footer = {
            DialogFooterRow(
                dismissLabel = context.getString(R.string.cancel),
                confirmLabel = context.getString(R.string.uninstall),
                onDismiss = onDismiss,
                onConfirm = onConfirm,
                confirmEnabled = isConfirmed,
                destructive = true
            )
        }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DsSpace.md)
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = context.getString(R.string.uninstall_container_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = buildAnnotatedString {
                val template = context.getString(R.string.uninstall_container_message)
                val parts = template.split("%1\$s")
                append(parts.getOrElse(0) { "" })
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(containerName) }
                append(parts.getOrElse(1) { "" })
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        ConfirmPhraseField(
            value = confirmText,
            onValueChange = { confirmText = it },
            isError = confirmText.isNotEmpty() && !isConfirmed,
            instruction = context.getString(R.string.type_container_name_to_confirm),
            phrase = containerName,
            capitalization = KeyboardCapitalization.None
        )
    }
}
