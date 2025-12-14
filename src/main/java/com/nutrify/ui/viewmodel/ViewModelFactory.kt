package com.nutrify.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nutrify.app.BuildConfig
import com.nutrify.managers.*
import com.nutrify.repository.*
import com.nutrify.services.*

class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    private val storageService by lazy { StorageService.getInstance(context.applicationContext) }

    private val usuarioDataSource: UsuarioDataSource by lazy {
        UsuarioLocalDataSource(storageService)
    }

    private val registroComidaDataSource: RegistroComidaDataSource by lazy {
        RegistroComidaLocalDataSource(storageService) // ✅ persistente
    }

    private val usuarioRepository by lazy { UsuarioRepository(usuarioDataSource) }
    private val registroComidaRepository by lazy { RegistroComidaRepository(registroComidaDataSource) }

    private val nutricionCalculator by lazy { ServiceFactory.getNutricionCalculatorService() }
    private val analizadorIAService by lazy { ServiceFactory.getAnalizadorIAService(BuildConfig.GEMINI_API_KEY) }

    private val authManager by lazy {
        UsuarioAuthManager(usuarioRepository, storageService)
    }
    private val profileManager by lazy { UsuarioProfileManager(usuarioRepository, authManager) }

    private val registroAlimentosManager by lazy {
        RegistroAlimentosManager(registroComidaRepository, authManager)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {

        modelClass.isAssignableFrom(LoginViewModel::class.java) ->
            LoginViewModel(authManager) as T

        modelClass.isAssignableFrom(RegistroViewModel::class.java) ->
            RegistroViewModel(authManager, profileManager) as T

        modelClass.isAssignableFrom(HomeViewModel::class.java) ->
            HomeViewModel(registroAlimentosManager, authManager) as T

        modelClass.isAssignableFrom(PerfilViewModel::class.java) ->
            PerfilViewModel(profileManager, nutricionCalculator) as T // ✅ 2 args

        modelClass.isAssignableFrom(CamaraViewModel::class.java) ->
            CamaraViewModel(analizadorIAService, registroAlimentosManager) as T

        modelClass.isAssignableFrom(IMCViewModel::class.java) ->
            IMCViewModel(profileManager, nutricionCalculator) as T

        modelClass.isAssignableFrom(NavegacionViewModel::class.java) ->
            NavegacionViewModel(authManager) as T

        modelClass.isAssignableFrom(RegistroDiarioViewModel::class.java) ->
            RegistroDiarioViewModel(registroAlimentosManager) as T

        else -> throw IllegalArgumentException("ViewModel no encontrado: ${modelClass.name}")
    }
}
