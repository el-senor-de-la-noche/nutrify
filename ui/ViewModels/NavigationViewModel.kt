package com.example.nutrify.ui.ViewModels
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class NavigationViewModel : ViewModel() {
    var destino = mutableStateOf("login")

    fun navegarA(nuevo: String) {
        destino.value = nuevo
    }
}
