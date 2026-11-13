package com.droidspaces.app.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.droidspaces.app.util.ContainerInfo
import com.droidspaces.app.util.ContainerOSInfoManager
import com.droidspaces.app.util.ContainerUsersManager
import com.droidspaces.app.util.ContainerManager
import com.droidspaces.app.util.PreferencesManager
import com.topjohnwu.superuser.Shell
import com.droidspaces.app.util.Constants
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for container list management - optimized for performance and correctness.
 *
 * Key improvements over previous version:
 * 1. NO static companion object - state is instance-scoped (proper MVVM)
 * 2. Uses proper Compose state management inside ViewModel
 * 3. Single refresh job with cancellation support
 * 4. Caches counts in preferences for instant display on restart
 * 5. Thread-safe state updates via Main dispatcher
 *
 * Performance characteristics:
 * - fetchContainerList(): ~50-200ms (IO bound)
 * - State reads: ~0 overhead (Compose snapshot system)
 * - Memory: Single list instance, no unnecessary copies
 */
class ContainerViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "ContainerViewModel"
    }

    private val prefsManager = PreferencesManager.getInstance(application)

    // Seeded from the cache so a restart shows the last list at once, then replaced by
    // the first fetch. An empty list here really means no containers.
    private var _containers by mutableStateOf(prefsManager.cachedContainers)

    // Cached configs all read as stopped, so until the first fetch lands the running
    // count comes from the number saved with them.
    private var loaded by mutableStateOf(false)

    // Refresh state
    var isRefreshing by mutableStateOf(false)
        private set

    // Current refresh job for cancellation
    private var refreshJob: Job? = null

    // A cancelled refresh still runs its finally block, which used to clear the
    // indicator after the next refresh had already set it.
    private var refreshGeneration = 0

    // Running names from the previous refresh. A refresh lands on every operation and
    // on log-close, so re-running the per-container shell work for the whole list is
    // what made those feel slow on a phone with many containers.
    private var lastRunningNames: Set<String> = emptySet()

    val containerList: List<ContainerInfo>
        get() = _containers

    val containerCount by derivedStateOf { _containers.size }

    val runningCount by derivedStateOf {
        if (loaded) _containers.count { it.isRunning } else prefsManager.cachedRunningCount
    }

    /**
     * Fetch container list from backend.
     * Cancels any ongoing fetch and starts a new one.
     *
     * Performance: ~50-200ms (IO dispatcher)
     * Thread safety: Updates state on Main dispatcher
     */
    fun fetchContainerList() {
        // Cancel any ongoing refresh
        refreshJob?.cancel()
        val generation = ++refreshGeneration

        refreshJob = viewModelScope.launch {
            isRefreshing = true

            try {
                val result = withContext(Dispatchers.IO) {
                    ContainerManager.listContainers()
                }

                updateState(result)

                Log.i(TAG, "Container list refreshed: ${result.size} containers, ${result.count { it.isRunning }} running")

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch container list", e)
                // Keep existing containers on error - don't clear

            } finally {
                if (generation == refreshGeneration) isRefreshing = false
            }
        }
    }

    /**
     * Updates the internal state and cache. Main-thread safe.
     */
    fun updateState(result: List<ContainerInfo>) {
        _containers = result
        loaded = true
        prefsManager.saveCachedContainers(result)
        prefsManager.cachedRunningCount = result.count { it.isRunning }

        val runningNames = result.filter { it.isRunning }.mapTo(mutableSetOf()) { it.name }
        val started = runningNames - lastRunningNames
        lastRunningNames = runningNames

        // Only containers that just started need the icon prefetch. `prefetchDistroIcon`
        // re-reads os-release, so doing it on every refresh is a shell call per container
        // for a value that only changes when the container is replaced.
        started.forEach { name ->
            viewModelScope.launch(Dispatchers.IO) {
                ContainerOSInfoManager.prefetchDistroIcon(name, getApplication())
            }
        }

        // Prime the user list once per process. The manager caches misses, so the
        // null check keeps cached containers off the root shell entirely.
        result.asSequence()
            .filter { it.isRunning && ContainerUsersManager.getCachedUsers(it.name) == null }
            .forEach { container ->
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        ContainerUsersManager.getUsers(container.name)
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to prime user cache for ${container.name}")
                    }
                }
            }
    }

    /** The backend's scan, then a fresh list. The indicator is for the user's own pull. */
    suspend fun scan(showIndicator: Boolean) {
        val generation = if (showIndicator) ++refreshGeneration else refreshGeneration
        if (showIndicator) isRefreshing = true
        try {
            withContext(Dispatchers.IO) {
                Shell.cmd("${Constants.getDroidspacesCommand()} scan").exec()
                val result = ContainerManager.listContainers()
                withContext(Dispatchers.Main) { updateState(result) }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Scan failed", e)
        } finally {
            if (showIndicator && generation == refreshGeneration) isRefreshing = false
        }
    }

    /**
     * Alias for fetchContainerList() for semantic clarity.
     */
    fun refresh() = fetchContainerList()

    init {
        // Trigger initial fetch on creation to prime state
        fetchContainerList()
    }
}
