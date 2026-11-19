package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.DivisionId
import com.example.data.model.PdfNoteItem
import com.example.data.repository.PdfStorageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class LockUiState(
    val passwordInput: String = "",
    val showError: Boolean = false
)

data class PdfHubUiState(
    val subject: String = "PPS",
    val unit: String = "Unit 1",
    val items: List<PdfNoteItem> = emptyList(),
    val activePdfForViewer: PdfNoteItem? = null,
    val activePdfFile: File? = null,
    val pendingExportItem: PdfNoteItem? = null,
    val statusMessage: String? = null
)

class VyasViewModel(application: Application) : AndroidViewModel(application) {

    private val pdfRepo = PdfStorageRepository(application.applicationContext)

    private val _lockState = MutableStateFlow(LockUiState())
    val lockState: StateFlow<LockUiState> = _lockState.asStateFlow()

    private val _activeDivision = MutableStateFlow(DivisionId.E)
    val activeDivision: StateFlow<DivisionId> = _activeDivision.asStateFlow()

    private val _activeCompilerTab = MutableStateFlow(1)
    val activeCompilerTab: StateFlow<Int> = _activeCompilerTab.asStateFlow()

    private val _pdfHubState = MutableStateFlow(PdfHubUiState())
    val pdfHubState: StateFlow<PdfHubUiState> = _pdfHubState.asStateFlow()

    fun onPasswordChanged(value: String) {
        _lockState.update { it.copy(passwordInput = value, showError = false) }
    }

    fun verifyPassword(): Boolean {
        val normalized = _lockState.value.passwordInput.trim().uppercase()
        return if (normalized == ACCESS_PASSWORD) {
            _lockState.update { it.copy(showError = false) }
            true
        } else {
            _lockState.update { it.copy(showError = true) }
            false
        }
    }

    fun selectDivision(divisionId: DivisionId) {
        _activeDivision.value = divisionId
    }

    fun selectCompilerTab(tab: Int) {
        _activeCompilerTab.value = tab
    }

    fun loadPdfsForUnit(subject: String, unit: String) {
        viewModelScope.launch {
            val items = withContext(Dispatchers.IO) {
                pdfRepo.getPdfsForUnit(subject, unit)
            }
            _pdfHubState.update {
                it.copy(
                    subject = subject,
                    unit = unit,
                    items = items
                )
            }
        }
    }

    fun importPdf(uri: Uri) {
        val subject = _pdfHubState.value.subject
        val unit = _pdfHubState.value.unit
        viewModelScope.launch {
            val imported = withContext(Dispatchers.IO) {
                pdfRepo.importPdfFromUri(uri, subject, unit)
            }
            if (imported != null) {
                val updated = withContext(Dispatchers.IO) {
                    pdfRepo.getPdfsForUnit(subject, unit)
                }
                _pdfHubState.update {
                    it.copy(
                        items = updated,
                        statusMessage = "\"${imported.name}\" uploaded! Tap anytime to open and read."
                    )
                }
            } else {
                _pdfHubState.update {
                    it.copy(statusMessage = "Unable to read selected PDF file.")
                }
            }
        }
    }

    fun openPdfViewer(item: PdfNoteItem) {
        viewModelScope.launch {
            val file = withContext(Dispatchers.IO) {
                pdfRepo.getOrCreateReadablePdfFile(item)
            }
            _pdfHubState.update {
                it.copy(
                    activePdfForViewer = item,
                    activePdfFile = file
                )
            }
        }
    }

    fun closePdfViewer() {
        _pdfHubState.update {
            it.copy(
                activePdfForViewer = null,
                activePdfFile = null
            )
        }
    }

    fun prepareExport(item: PdfNoteItem) {
        _pdfHubState.update { it.copy(pendingExportItem = item) }
    }

    fun completeExport(destinationUri: Uri?) {
        val item = _pdfHubState.value.pendingExportItem
        _pdfHubState.update { it.copy(pendingExportItem = null) }
        if (item == null || destinationUri == null) return

        viewModelScope.launch {
            val ok = withContext(Dispatchers.IO) {
                pdfRepo.exportPdfToUri(item, destinationUri)
            }
            _pdfHubState.update {
                it.copy(
                    statusMessage = if (ok) {
                        "Saved \"${item.name}\" successfully."
                    } else {
                        "Failed to save \"${item.name}\"."
                    }
                )
            }
        }
    }

    fun clearStatusMessage() {
        _pdfHubState.update { it.copy(statusMessage = null) }
    }

    companion object {
        private const val ACCESS_PASSWORD = "SSBTCOMP"
    }
}
