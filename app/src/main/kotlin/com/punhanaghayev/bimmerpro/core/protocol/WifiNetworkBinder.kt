package com.punhanaghayev.bimmerpro.core.protocol

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import java.net.Socket

/**
 * Ensures TCP sockets to Wi-Fi OBD adapters (vLinker BM Wi-Fi 192.168.0.10, ENET Wi-Fi 169.254.0.1)
 * route strictly over the active Wi-Fi interface even when the Wi-Fi hotspot has no Internet
 * and Android would otherwise route traffic over 4G/5G mobile data.
 */
object WifiNetworkBinder {

    fun findWifiNetwork(context: Context): Network? {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return null
        @Suppress("DEPRECATION")
        return cm.allNetworks.firstOrNull { network ->
            val caps = cm.getNetworkCapabilities(network) ?: return@firstOrNull false
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        }
    }

    fun createSocket(context: Context?): Socket {
        if (context != null) {
            val wifiNet = findWifiNetwork(context)
            if (wifiNet != null) {
                runCatching {
                    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                    cm?.bindProcessToNetwork(wifiNet)
                    return wifiNet.socketFactory.createSocket()
                }
            }
        }
        return Socket()
    }
}
