package com.punhanaghayev.bimmerpro.data

import android.content.Context

/**
 * Process-wide holder so both the Compose UI ViewModel and Android Auto CarAppService
 * observe and control the exact same live vehicle diagnostic session.
 */
object BimmerRepositoryProvider {
    @Volatile
    private var instance: BimmerRepository? = null

    fun get(context: Context): BimmerRepository {
        return instance ?: synchronized(this) {
            instance ?: BimmerRepository(context.applicationContext).also { instance = it }
        }
    }
}
