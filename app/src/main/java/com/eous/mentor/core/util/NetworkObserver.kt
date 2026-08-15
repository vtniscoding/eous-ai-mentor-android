package com.eous.mentor.core.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.net.InetSocketAddress
import java.net.Socket

class NetworkObserver(private val context: Context) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    val isConnected: Flow<Boolean> = flow {
        var consecutiveFailures = 0
        var lastEmittedStatus = true

        while (true) {
            val isConnectedNow = checkConnection()
            if (isConnectedNow) {
                consecutiveFailures = 0
                if (!lastEmittedStatus) {
                    lastEmittedStatus = true
                    emit(true)
                }
            } else {
                consecutiveFailures++
                // Only consider disconnected if 2 consecutive checks fail (prevents false positives)
                if (consecutiveFailures >= 2 && lastEmittedStatus) {
                    lastEmittedStatus = false
                    emit(false)
                }
            }
            delay(2500)
        }
    }.distinctUntilChanged().flowOn(Dispatchers.IO)

    suspend fun isCurrentlyConnected(): Boolean {
        return checkConnection()
    }

    private fun checkConnection(): Boolean {
        // 1. First check Android OS Network Capabilities
        try {
            val activeNetwork = connectivityManager?.activeNetwork
            if (activeNetwork != null) {
                val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
                if (capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                    return true
                }
            }
        } catch (e: Throwable) {
            // Exception catch
        }

        // 2. Fallback check: Socket reachability to primary or secondary DNS
        return pingSocket("8.8.8.8", 53) || pingSocket("1.1.1.1", 53)
    }

    private fun pingSocket(host: String, port: Int): Boolean {
        return try {
            val socket = Socket()
            socket.connect(InetSocketAddress(host, port), 1200)
            socket.close()
            true
        } catch (e: Throwable) {
            false
        }
    }
}
