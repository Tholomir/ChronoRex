package com.dino.chronorex.ui.export

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dino.chronorex.export.ExportManager
import com.dino.chronorex.export.ExportResult
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ExportViewModel(private val exportManager: ExportManager) : ViewModel() {

    private val _state = MutableStateFlow(ExportUiState())
    val state: StateFlow<ExportUiState> = _state.asStateFlow()

    fun generateCsv() {
        if (_state.value.processingCsv) return
        viewModelScope.launch {
            _state.value = _state.value.copy(processingCsv = true, errorMessage = null)
            runCatching { exportManager.createCsvExport() }
                .onSuccess { result ->
                    _state.value = _state.value.copy(csvResult = result, processingCsv = false)
                }
                .onFailure { throwable ->
                    _state.value = _state.value.copy(processingCsv = false, errorMessage = throwable.message ?: "CSV export failed")
                }
        }
    }

    fun generatePdf() {
        if (_state.value.processingPdf) return
        viewModelScope.launch {
            _state.value = _state.value.copy(processingPdf = true, errorMessage = null)
            runCatching { exportManager.createPdfExport() }
                .onSuccess { result ->
                    _state.value = _state.value.copy(pdfResult = result, processingPdf = false)
                }
                .onFailure { throwable ->
                    _state.value = _state.value.copy(processingPdf = false, errorMessage = throwable.message ?: "PDF export failed")
                }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }

    suspend fun saveToDownloads(result: ExportResult) = exportManager.saveToDownloads(result)
}


data class ExportUiState(
    val csvResult: ExportResult? = null,
    val pdfResult: ExportResult? = null,
    val processingCsv: Boolean = false,
    val processingPdf: Boolean = false,
    val errorMessage: String? = null
)
