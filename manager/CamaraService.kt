package com.example.nutrify.manager

import android.content.Context
import android.graphics.Bitmap
import android.provider.MediaStore

class CamaraService(private val context: Context) {

    fun convertirBitmapABytes(bitmap: Bitmap): ByteArray {
        val stream = java.io.ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    fun guardarImagenEnGaleria(bitmap: Bitmap, nombre: String): String? {
        val uri = MediaStore.Images.Media.insertImage(
            context.contentResolver,
            bitmap,
            nombre,
            "Imagen capturada por NutriFy"
        )
        return uri
    }
}
