package com.nutrify.repository



import android.content.Context
import com.nutrify.services.ServiceFactory



object RepositoryFactory {
    private var usuarioRepository: UsuarioRepository? = null
    private var registroComidaRepository: RegistroComidaRepository? = null

    // Para testing
    private var useMemoryDataSource = false

    fun enableMemoryDataSource() {
        useMemoryDataSource = true
    }

    fun disableMemoryDataSource() {
        useMemoryDataSource = false
    }

    fun getUsuarioRepository(context: Context): UsuarioRepository {
        return usuarioRepository ?: run {
            val storageService = ServiceFactory.getStorageService(context)
            val dataSource = UsuarioLocalDataSource(storageService)
            UsuarioRepository(dataSource).also { usuarioRepository = it }
        }
    }

    fun getRegistroComidaRepository(context: Context): RegistroComidaRepository {
        return registroComidaRepository ?: run {

            // ✅ SIEMPRE crea el storageService una vez
            val storageService = ServiceFactory.getStorageService(context)

            val dataSource: RegistroComidaDataSource = if (useMemoryDataSource) {
                // ✅ ANTES: RegistroComidaMemoryDataSource()
                RegistroComidaMemoryDataSource(storageService)
            } else {
                // si tienes este local datasource, déjalo igual
                RegistroComidaLocalDataSource(storageService)
            }

            RegistroComidaRepository(dataSource).also { registroComidaRepository = it }
        }
    }


    // Métodos para testing
    fun getUsuarioRepositoryWithDataSource(dataSource: UsuarioDataSource): UsuarioRepository {
        return UsuarioRepository(dataSource)
    }

    fun getRegistroComidaRepositoryWithDataSource(dataSource: RegistroComidaDataSource): RegistroComidaRepository {
        return RegistroComidaRepository(dataSource)
    }

    fun clearAll() {
        usuarioRepository = null
        registroComidaRepository = null
    }
}
