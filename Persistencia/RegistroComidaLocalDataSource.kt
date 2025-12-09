package com.example.nutrify.persistencia

import com.example.nutrify.Dominio.AnalisisIA
import com.example.nutrify.Dominio.PorcionAlimento
import com.example.nutrify.Dominio.RegistroComida
import com.example.nutrify.Dominio.UnidadPorcion
import com.example.nutrify.persistencia.IAppStorage
import org.json.JSONArray
import org.json.JSONObject
import java.util.Date

class RegistroComidaLocalDataSource(
    private val storage: IAppStorage
) : IRegistroComidaDataSource {

    private val archivo = "registros_comida.json"

    override fun guardarRegistros(registros: List<RegistroComida>) {
        val array = JSONArray()

        registros.forEach { reg ->
            val o = JSONObject()
            o.put("usuarioId", reg.usuarioId)
            o.put("fechaHora", reg.fechaHora.time)

            // porciones
            val porcionesJson = JSONArray()
            reg.porciones.forEach { p ->
                val pObj = JSONObject()
                pObj.put("alimentoNombre", p.alimentoNombre)
                pObj.put("cantidad", p.cantidad)
                pObj.put("unidad", p.unidad.name)
                pObj.put("caloriasTotales", p.caloriasTotales)
                pObj.put("proteinasTotales", p.proteinasTotales)
                pObj.put("carbohidratosTotales", p.carbohidratosTotales)
                pObj.put("grasasTotales", p.grasasTotales)
                pObj.put("fibraTotal", p.fibraTotal)
                porcionesJson.put(pObj)
            }
            o.put("porciones", porcionesJson)

            // análisis IA opcional
            reg.analisisIA?.let {
                val iaObj = JSONObject()
                iaObj.put("alimentoDetectado", it.alimentoDetectado)
                iaObj.put("confianza", it.confianza)
                iaObj.put("calorias", it.caloriasEstimadas)
                iaObj.put("proteinas", it.proteinasEstimadas)
                iaObj.put("carbohidratos", it.carbohidratosEstimados)
                iaObj.put("grasas", it.grasasEstimadas)
                iaObj.put("fibra", it.fibraEstimada)
                iaObj.put("jsonBruto", it.respuestaBrutaJSON)
                o.put("analisisIA", iaObj)
            }

            o.put("rutaFoto", reg.rutaFoto)
            o.put("notaUsuario", reg.notaUsuario)

            array.put(o)
        }

        storage.escribirArchivo(archivo, array.toString())
    }

    override fun cargarRegistros(): List<RegistroComida> {
        if (!storage.existeArchivo(archivo)) return emptyList()

        val contenido = storage.leerArchivo(archivo)
        if (contenido.isEmpty()) return emptyList()

        val array = JSONArray(contenido)
        val lista = mutableListOf<RegistroComida>()

        for (i in 0 until array.length()) {
            val o = array.getJSONObject(i)
            val fecha = Date(o.getLong("fechaHora"))

            // porciones
            val porcionesJson = o.getJSONArray("porciones")
            val porciones = mutableListOf<PorcionAlimento>()

            for (j in 0 until porcionesJson.length()) {
                val p = porcionesJson.getJSONObject(j)
                porciones.add(
                    PorcionAlimento(
                        alimentoNombre = p.getString("alimentoNombre"),
                        cantidad = p.getDouble("cantidad"),
                        unidad = UnidadPorcion.valueOf(p.getString("unidad")),
                        caloriasTotales = p.getDouble("caloriasTotales"),
                        proteinasTotales = p.getDouble("proteinasTotales"),
                        carbohidratosTotales = p.getDouble("carbohidratosTotales"),
                        grasasTotales = p.getDouble("grasasTotales"),
                        fibraTotal = p.getDouble("fibraTotal")
                    )
                )
            }

            val analisis = if (o.has("analisisIA")) {
                val ia = o.getJSONObject("analisisIA")
                AnalisisIA(
                    alimentoDetectado = ia.getString("alimentoDetectado"),
                    confianza = ia.getDouble("confianza"),
                    caloriasEstimadas = ia.getDouble("calorias"),
                    proteinasEstimadas = ia.getDouble("proteinas"),
                    carbohidratosEstimados = ia.getDouble("carbohidratos"),
                    grasasEstimadas = ia.getDouble("grasas"),
                    fibraEstimada = ia.getDouble("fibra"),
                    respuestaBrutaJSON = ia.getString("jsonBruto")
                )
            } else null

            lista.add(
                RegistroComida(
                    usuarioId = o.getString("usuarioId"),
                    fechaHora = fecha,
                    porciones = porciones,
                    analisisIA = analisis,
                    rutaFoto = o.optString("rutaFoto"),
                    notaUsuario = o.optString("notaUsuario")
                )
            )
        }

        return lista
    }
}
