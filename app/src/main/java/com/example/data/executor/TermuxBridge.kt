package com.example.data.executor

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import java.io.File

data class SystemTelemetry(
    val isTermuxInstalled: Boolean,
    val termuxStatusText: String,
    val cpuArch: String,
    val androidVersion: String,
    val shellPath: String,
    val homeDir: String,
    val totalRamMb: Long,
    val freeRamMb: Long,
    val ramUsedPercent: Int,
    val totalStorageGb: Long,
    val freeStorageGb: Long,
    val storageUsedPercent: Int,
    val batteryPercent: Int,
    val isCharging: Boolean
)

class TermuxBridge(private val context: Context) {

    companion object {
        const val TERMUX_PACKAGE = "com.termux"
        const val TERMUX_BIN = "/data/data/com.termux/files/usr/bin"
        const val TERMUX_HOME = "/data/data/com.termux/files/home"
        const val TERMUX_BASH = "/data/data/com.termux/files/usr/bin/bash"
    }

    fun isTermuxInstalled(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    TERMUX_PACKAGE,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(TERMUX_PACKAGE, 0)
            }
            true
        } catch (e: Exception) {
            // Check if directory exists
            File(TERMUX_HOME).exists() || File(TERMUX_BIN).exists()
        }
    }

    fun getPreferredShell(): String {
        return if (File(TERMUX_BASH).canExecute()) {
            TERMUX_BASH
        } else {
            "/system/bin/sh"
        }
    }

    fun getTelemetry(): SystemTelemetry {
        val termuxInstalled = isTermuxInstalled()
        val cpuArch = Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a"
        val androidVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
        val shell = getPreferredShell()
        val home = if (termuxInstalled) TERMUX_HOME else context.filesDir.absolutePath

        // RAM
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager?.getMemoryInfo(memInfo)
        val totalRamMb = memInfo.totalMem / (1024 * 1024)
        val availRamMb = memInfo.availMem / (1024 * 1024)
        val usedRamMb = totalRamMb - availRamMb
        val ramUsedPercent = if (totalRamMb > 0) ((usedRamMb * 100) / totalRamMb).toInt() else 50

        // Storage
        val stat = StatFs(Environment.getDataDirectory().path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        val availBlocks = stat.availableBlocksLong
        val totalStorageGb = (totalBlocks * blockSize) / (1024 * 1024 * 1024)
        val freeStorageGb = (availBlocks * blockSize) / (1024 * 1024 * 1024)
        val usedStorageGb = totalStorageGb - freeStorageGb
        val storageUsedPercent = if (totalStorageGb > 0) ((usedStorageGb * 100) / totalStorageGb).toInt() else 40

        // Battery
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val batteryLevel = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 85
        val batteryStatus = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS) ?: BatteryManager.BATTERY_STATUS_DISCHARGING
        val isCharging = batteryStatus == BatteryManager.BATTERY_STATUS_CHARGING || batteryStatus == BatteryManager.BATTERY_STATUS_FULL

        return SystemTelemetry(
            isTermuxInstalled = termuxInstalled,
            termuxStatusText = if (termuxInstalled) "● TERMUX CONNECTED" else "○ Local Shell Ready (Termux Bridge Standby)",
            cpuArch = cpuArch,
            androidVersion = androidVersion,
            shellPath = shell,
            homeDir = home,
            totalRamMb = totalRamMb,
            freeRamMb = availRamMb,
            ramUsedPercent = ramUsedPercent,
            totalStorageGb = totalStorageGb,
            freeStorageGb = freeStorageGb,
            storageUsedPercent = storageUsedPercent,
            batteryPercent = batteryLevel,
            isCharging = isCharging
        )
    }

    fun openTermuxApp(): Boolean {
        return try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(TERMUX_PACKAGE)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                true
            } else {
                openTermuxInstallPage()
                false
            }
        } catch (e: Exception) {
            openTermuxInstallPage()
            false
        }
    }

    fun openTermuxInstallPage() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://f-droid.org/packages/com.termux/")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }
}
