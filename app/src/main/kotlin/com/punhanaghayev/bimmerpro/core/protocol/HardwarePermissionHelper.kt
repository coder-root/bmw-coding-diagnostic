package com.punhanaghayev.bimmerpro.core.protocol

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.usb.UsbManager
import android.os.Build
import androidx.core.content.ContextCompat

object HardwarePermissionHelper {

    fun requiredBluetoothPermissions(): Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            emptyArray()
        }

    fun hasAllBluetoothPermissions(context: Context): Boolean =
        requiredBluetoothPermissions().all { perm ->
            ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
        }

    fun requestUsbKdcanPermissionIfNeeded(context: Context): Boolean {
        val usbManager = context.getSystemService(Context.USB_SERVICE) as? UsbManager ?: return false
        val device = usbManager.deviceList.values.firstOrNull() ?: return false
        if (usbManager.hasPermission(device)) return true

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent("com.punhanaghayev.bimmerpro.USB_PERMISSION").setPackage(context.packageName),
            PendingIntent.FLAG_IMMUTABLE
        )
        usbManager.requestPermission(device, pendingIntent)
        return false
    }
}
