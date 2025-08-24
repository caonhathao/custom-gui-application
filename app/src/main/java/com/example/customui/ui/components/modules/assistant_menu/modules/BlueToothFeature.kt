package com.example.customui.ui.components.modules.assistant_menu.modules

import android.Manifest
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.content.ContextCompat
import compose.icons.FeatherIcons
import compose.icons.feathericons.Bluetooth


class BluetoothFeature(private val context: Context): _interfaceHelper {
    override val name = "Bluetooth"

    private val defaultIcon = FeatherIcons.Bluetooth
    private val changedIcon = FeatherIcons.Bluetooth

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            // API 18+ sử dụng BluetoothManager
            val bluetoothManager = context.applicationContext
                .getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            bluetoothManager?.adapter
        } else {
            // API < 18 sử dụng BluetoothAdapter trực tiếp
            @Suppress("DEPRECATION")
            BluetoothAdapter.getDefaultAdapter()
        }
    }

    override fun toggle(enable: Boolean) {
        bluetoothAdapter?.let { adapter ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12+ (API 31+): Cannot programmatically enable/disable Bluetooth
                // Open Bluetooth settings panel instead
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        // API 29+ has Settings Panel support
                        val panelIntent = Intent("android.settings.panel.action.BLUETOOTH").apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(panelIntent)
                    } else {
                        // Fallback to regular settings
                        openBluetoothSettings()
                    }
                } catch (e: Exception) {
                    // Fallback to Bluetooth settings if panel not supported
                    openBluetoothSettings()
                }
            } else {
                // Android < 12: Can enable/disable programmatically (but deprecated)
                try {
                    // Check if we have the required permissions
                    if (hasBluetoothPermissions()) {
                        @Suppress("DEPRECATION")
                        if (enable && !adapter.isEnabled) {
                            adapter.enable()
                        } else if (!enable && adapter.isEnabled) {
                            @Suppress("DEPRECATION")
                            adapter.disable()
                        }
                    } else {
                        // If no permissions, open settings
                        openBluetoothSettings()
                    }
                } catch (e: SecurityException) {
                    // Permission denied
                    false
                } catch (e: Exception) {
                    Log.e("BluetoothFeature", "Error checking Bluetooth state", e)
                    false
                }
            }
        } ?: run {
            // Bluetooth not supported
            Toast.makeText(context, "Bluetooth not supported on this device", Toast.LENGTH_SHORT).show()
        }
    }

    override fun isEnabled(): Boolean {
        return try {
            bluetoothAdapter?.isEnabled == true
        } catch (e: SecurityException) {
            // Permission denied
            false
        } catch (e: Exception) {
            Log.e("BluetoothFeature", "Error checking Bluetooth state", e)
            false
        }
    }

    override fun getChangedIcon(): ImageVector = changedIcon

    override fun getDefaultIcon(): ImageVector = defaultIcon

    private fun hasBluetoothPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ requires BLUETOOTH_CONNECT permission
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Pre-Android 12 requires BLUETOOTH and BLUETOOTH_ADMIN
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_ADMIN
                    ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun openBluetoothSettings() {
        try {
            val settingsIntent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(settingsIntent)
        } catch (e: Exception) {
            Log.e("BluetoothFeature", "Cannot open Bluetooth settings", e)
            Toast.makeText(context, "Cannot open Bluetooth settings", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        // Helper method to request permissions if needed
        fun getRequiredPermissions(): Array<String> {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN  // If you need scanning
                )
            } else {
                arrayOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN
                )
            }
        }
    }
}
