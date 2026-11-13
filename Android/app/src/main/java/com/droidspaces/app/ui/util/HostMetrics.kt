package com.droidspaces.app.ui.util

import android.os.Environment
import android.os.StatFs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * One reading of the phone itself, not of a container. Everything here comes
 * from the kernel's own files, so an empty value means the file could not be
 * read rather than a made-up default.
 */
data class HostSnapshot(
    val cpuPercent: Float = 0f,
    val cpuCores: Int = 1,
    val memUsedBytes: Long = 0,
    val memTotalBytes: Long = 0,
    val storageUsedBytes: Long = 0,
    val storageTotalBytes: Long = 0,
    val networkRxBytes: Long = 0,
    val networkTxBytes: Long = 0,
    val loadAverage: Float = 0f
) {
    val memProgress: Float
        get() = if (memTotalBytes > 0) memUsedBytes.toFloat() / memTotalBytes else 0f
    val storageProgress: Float
        get() = if (storageTotalBytes > 0) storageUsedBytes.toFloat() / storageTotalBytes else 0f
}

/**
 * Reads host counters. The CPU percentage is a delta between two /proc/stat
 * samples, so the first call establishes the baseline and reports zero; every
 * call after that is a real interval. One instance per polling screen: a shared
 * baseline would measure the gap between whichever screens sampled last.
 */
class HostMetricsReader {

    private var lastTotalJiffies = 0L
    private var lastIdleJiffies = 0L

    suspend fun read(): HostSnapshot = withContext(Dispatchers.IO) {
        val cpu = readCpu()
        val memory = readMemory()
        val storage = readStorage()
        val network = readNetwork()
        HostSnapshot(
            cpuPercent = cpu,
            cpuCores = Runtime.getRuntime().availableProcessors().coerceAtLeast(1),
            memUsedBytes = memory.first,
            memTotalBytes = memory.second,
            storageUsedBytes = storage.first,
            storageTotalBytes = storage.second,
            networkRxBytes = network.first,
            networkTxBytes = network.second,
            loadAverage = readLoadAverage()
        )
    }

    private fun readCpu(): Float {
        val line = runCatching { File("/proc/stat").useLines { it.firstOrNull { row -> row.startsWith("cpu ") } } }.getOrNull()
            ?: return 0f
        val parts = line.trim().split(Regex("\\s+")).drop(1).mapNotNull { it.toLongOrNull() }
        if (parts.size < 4) return 0f
        val idle = parts[3] + (parts.getOrNull(4) ?: 0L)
        val total = parts.sum()
        val previousTotal = lastTotalJiffies
        val previousIdle = lastIdleJiffies
        lastTotalJiffies = total
        lastIdleJiffies = idle
        if (previousTotal == 0L || total <= previousTotal) return 0f
        val totalDelta = total - previousTotal
        val idleDelta = idle - previousIdle
        return ((totalDelta - idleDelta).toFloat() / totalDelta).coerceIn(0f, 1f)
    }

    private fun readMemory(): Pair<Long, Long> {
        var totalKb = 0L
        var availableKb = 0L
        var freeKb = 0L
        var buffersKb = 0L
        var cachedKb = 0L
        runCatching {
            File("/proc/meminfo").forEachLine { line ->
                val value = line.filter { it.isDigit() }.toLongOrNull() ?: 0L
                when {
                    line.startsWith("MemTotal:") -> totalKb = value
                    line.startsWith("MemAvailable:") -> availableKb = value
                    line.startsWith("MemFree:") -> freeKb = value
                    line.startsWith("Buffers:") -> buffersKb = value
                    line.startsWith("Cached:") -> cachedKb = value
                }
            }
        }
        if (totalKb == 0L) return 0L to 0L
        // MemAvailable arrived in 3.14, and the app supports 3.10. On those kernels
        // the old free plus reclaimable estimate is the best real answer there is.
        if (availableKb == 0L) availableKb = freeKb + buffersKb + cachedKb
        val total = totalKb * 1024
        val used = (totalKb - availableKb).coerceAtLeast(0L) * 1024
        return used to total
    }

    private fun readStorage(): Pair<Long, Long> {
        return runCatching {
            val stat = StatFs(Environment.getDataDirectory().absolutePath)
            val total = stat.totalBytes
            val available = stat.availableBytes
            (total - available).coerceAtLeast(0L) to total
        }.getOrDefault(0L to 0L)
    }

    private fun readNetwork(): Pair<Long, Long> {
        var rx = 0L
        var tx = 0L
        runCatching {
            File("/proc/net/dev").forEachLine { line ->
                val trimmed = line.trim()
                if (trimmed.startsWith("lo:")) return@forEachLine
                val colon = trimmed.indexOf(':')
                if (colon < 0) return@forEachLine
                val fields = trimmed.substring(colon + 1).trim().split(Regex("\\s+"))
                if (fields.size >= 9) {
                    rx += fields[0].toLongOrNull() ?: 0L
                    tx += fields[8].toLongOrNull() ?: 0L
                }
            }
        }
        return rx to tx
    }

    private fun readLoadAverage(): Float =
        runCatching { File("/proc/loadavg").readText().trim().split(" ").firstOrNull()?.toFloatOrNull() ?: 0f }
            .getOrDefault(0f)
}

/** Bytes in the shortest unit that still shows a useful digit. */
fun formatBytes(bytes: Long): String {
    if (bytes <= 0L) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var unit = 0
    while (value >= 1024 && unit < units.lastIndex) {
        value /= 1024
        unit++
    }
    return if (value >= 100 || unit == 0) "${value.toInt()} ${units[unit]}" else String.format("%.1f %s", value, units[unit])
}

/** 0.427 becomes "43%". */
fun formatPercent(value: Float): String = "${(value.coerceIn(0f, 1f) * 100).toInt()}%"
