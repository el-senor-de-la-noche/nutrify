package com.nutrify.services

import android.content.Context

object ServiceFactory {

    @Volatile private var storageService: StorageService? = null
    @Volatile private var nutricionCalculatorService: NutricionCalculatorService? = null

    @Volatile private var analizadorIAService: AnalizadorIAService? = null
    @Volatile private var cachedApiKey: String? = null

    fun getStorageService(ctx: Context): StorageService {
        return storageService ?: synchronized(this) {
            storageService ?: StorageService.getInstance(ctx.applicationContext).also {
                storageService = it
            }
        }
    }

    fun getNutricionCalculatorService(): NutricionCalculatorService {
        return nutricionCalculatorService ?: synchronized(this) {
            nutricionCalculatorService ?: NutricionCalculatorService().also {
                nutricionCalculatorService = it
            }
        }
    }

    fun getAnalizadorIAService(apiKey: String): AnalizadorIAService {
        val key = apiKey.trim()
        val shouldRecreate = (analizadorIAService == null) || (cachedApiKey != key)

        return if (shouldRecreate) {
            synchronized(this) {
                val inner = (analizadorIAService == null) || (cachedApiKey != key)
                if (inner) {
                    analizadorIAService?.close()
                    cachedApiKey = key
                    AnalizadorIAService(key).also { analizadorIAService = it }
                } else analizadorIAService!!
            }
        } else analizadorIAService!!
    }

    fun clearAll() {
        analizadorIAService?.close()
        storageService = null
        nutricionCalculatorService = null
        analizadorIAService = null
        cachedApiKey = null
    }
}
