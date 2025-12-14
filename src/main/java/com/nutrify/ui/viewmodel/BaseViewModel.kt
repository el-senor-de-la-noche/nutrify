package com.nutrify.ui.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

open class BaseViewModel : ViewModel() {

    protected fun launchWithErrorHandling(
        errorMessage: String = "Error desconocido",
        onError: ((Exception) -> Unit)? = null,
        block:  CoroutineScope.() -> Unit
    ): Job {
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            val exception = if (throwable is Exception) throwable else Exception(throwable)
            onError?.invoke(exception)
        }

        return viewModelScope.launch(exceptionHandler, block = block)
    }
}

// Estados comunes para UI
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
    object Empty : UiState<Nothing>()
}

data class ErrorState(
    val message: String,
    val retryAction: (() -> Unit)? = null
)
