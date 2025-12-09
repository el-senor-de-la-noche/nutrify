package com.example.nutrify.persistencia

import com.example.nutrify.Dominio.Usuario
import com.example.nutrify.Dominio.NivelActividad
import com.example.nutrify.Dominio.ObjetivoNutricional
import com.example.nutrify.Dominio.Sexo

import org.json.JSONObject
import java.util.Date

class UsuarioLocalDataSource(
    private val storage: IAppStorage
) : IUsuarioDataSource {

    private val archivo = "usuario.json"

    override fun guardarUsuario(usuario: Usuario) {
        val json = JSONObject()
        json.put("nombre", usuario.nombre)
        json.put("email", usuario.email)
        json.put("passwordHash", usuario.passwordHash)
        json.put("fechaNacimiento", usuario.fechaNacimiento.time)
        json.put("sexo", usuario.sexo.name)
        json.put("pesoActualKg", usuario.pesoActualKg)
        json.put("alturaM", usuario.alturaCm)
        json.put("nivelActividad", usuario.nivelActividad.name)
        json.put("objetivo", usuario.objetivo.name)

        storage.escribirArchivo(archivo, json.toString())
    }

    override fun obtenerUsuario(): Usuario? {
        if (!storage.existeArchivo(archivo)) return null

        val contenido = storage.leerArchivo(archivo)
        if (contenido.isEmpty()) return null

        val json = JSONObject(contenido)

        return Usuario(
            nombre = json.getString("nombre"),
            email = json.getString("email"),
            passwordHash = json.getString("passwordHash"),
            fechaNacimiento = Date(json.getLong("fechaNacimiento")),
            sexo = Sexo.valueOf(json.getString("sexo")),
            pesoActualKg = json.getDouble("pesoActualKg"),
            alturaCm = json.getDouble("alturaCM"),
            nivelActividad = NivelActividad.valueOf(json.getString("nivelActividad")),
            objetivo = ObjetivoNutricional.valueOf(json.getString("objetivo"))
        )
    }

    override fun borrarUsuario() {
        if (storage.existeArchivo(archivo)) {
            storage.escribirArchivo(archivo, "")
        }
    }
}
