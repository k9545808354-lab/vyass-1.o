package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.DaySchedule
import com.example.data.model.DivisionId
import com.example.data.model.DivisionInfo
import com.example.data.model.PdfNoteItem
import com.example.data.model.TimetableCell
import com.example.data.repository.PdfStorageRepository
import com.example.data.repository.TimetableRepository
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
    private val timetableRepo = TimetableRepository(application.applicationContext)

    private val _lockState = MutableStateFlow(LockUiState())
    val lockState: StateFlow<LockUiState> = _lockState.asStateFlow()

    private val _activeDivision = MutableStateFlow(DivisionId.E)
    val activeDivision: StateFlow<DivisionId> = _activeDivision.asStateFlow()

    private val _divisions = MutableStateFlow(timetableRepo.loadAllDivisions())
    val divisions: StateFlow<List<DivisionInfo>> = _divisions.asStateFlow()

    private val _schedules = MutableStateFlow(
        DivisionId.entries.associateWith { timetableRepo.loadSchedule(it) }
    )
    val schedules: StateFlow<Map<DivisionId, List<DaySchedule>>> = _schedules.asStateFlow()

    private val _customizedDivisions = MutableStateFlow(
        DivisionId.entries.filter { timetableRepo.hasCustomChanges(it) }.toSet()
    )
    val customizedDivisions: StateFlow<Set<DivisionId>> = _customizedDivisions.asStateFlow()

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

    fun updateTimetableCell(
        divisionId: DivisionId,
        dayIndex: Int,
        isPreLunch: Boolean,
        cellIndex: Int,
        newCell: TimetableCell
    ) {
        val currentSchedule = _schedules.value[divisionId]?.toMutableList() ?: return
        if (dayIndex !in currentSchedule.indices) return
        val day = currentSchedule[dayIndex]
        val updatedDay = if (isPreLunch) {
            val cells = day.preLunchCells.toMutableList()
            if (cellIndex !in cells.indices) return
            cells[cellIndex] = newCell
            day.copy(preLunchCells = cells)
        } else {
            val cells = day.postLunchCells.toMutableList()
            if (cellIndex !in cells.indices) return
            cells[cellIndex] = newCell
            day.copy(postLunchCells = cells)
        }
        currentSchedule[dayIndex] = updatedDay
        commitSchedule(divisionId, currentSchedule)
    }

    fun splitTimetableCell(
        divisionId: DivisionId,
        dayIndex: Int,
        isPreLunch: Boolean,
        cellIndex: Int
    ) {
        val currentSchedule = _schedules.value[divisionId]?.toMutableList() ?: return
        if (dayIndex !in currentSchedule.indices) return
        val day = currentSchedule[dayIndex]
        val sourceList = if (isPreLunch) day.preLunchCells else day.postLunchCells
        if (cellIndex !in sourceList.indices) return
        val target = sourceList[cellIndex]
        if (target.colSpan != 2) return

        val firstHalf = when (target) {
            is TimetableCell.Lecture -> target.copy(colSpan = 1)
            is TimetableCell.BatchSplit -> target.copy(colSpan = 1)
            is TimetableCell.Empty -> TimetableCell.Empty(colSpan = 1)
        }
        val secondHalf = TimetableCell.Empty(colSpan = 1)

        val updatedList = sourceList.toMutableList().apply {
            removeAt(cellIndex)
            add(cellIndex, secondHalf)
            add(cellIndex, firstHalf)
        }
        currentSchedule[dayIndex] = if (isPreLunch) {
            day.copy(preLunchCells = updatedList)
        } else {
            day.copy(postLunchCells = updatedList)
        }
        commitSchedule(divisionId, currentSchedule)
    }

    fun mergeTimetableCells(
        divisionId: DivisionId,
        dayIndex: Int,
        isPreLunch: Boolean,
        cellIndex: Int
    ) {
        val currentSchedule = _schedules.value[divisionId]?.toMutableList() ?: return
        if (dayIndex !in currentSchedule.indices) return
        val day = currentSchedule[dayIndex]
        val sourceList = if (isPreLunch) day.preLunchCells else day.postLunchCells
        if (cellIndex < 0 || cellIndex + 1 >= sourceList.size) return
        val first = sourceList[cellIndex]
        val second = sourceList[cellIndex + 1]
        if (first.colSpan != 1 || second.colSpan != 1) return

        val merged = when (first) {
            is TimetableCell.Lecture -> first.copy(colSpan = 2)
            is TimetableCell.BatchSplit -> first.copy(colSpan = 2)
            is TimetableCell.Empty -> TimetableCell.Empty(colSpan = 2)
        }

        val updatedList = sourceList.toMutableList().apply {
            removeAt(cellIndex + 1)
            set(cellIndex, merged)
        }
        currentSchedule[dayIndex] = if (isPreLunch) {
            day.copy(preLunchCells = updatedList)
        } else {
            day.copy(postLunchCells = updatedList)
        }
        commitSchedule(divisionId, currentSchedule)
    }

    fun updateDivisionInfo(divisionId: DivisionId, classRoom: String, wefDate: String) {
        val updated = timetableRepo.loadDivisionInfo(divisionId).copy(
            classRoom = classRoom,
            wefDate = wefDate,
            subtitle = "Computer Engineering • Class Room $classRoom"
        )
        timetableRepo.saveDivisionInfo(updated)
        _divisions.value = timetableRepo.loadAllDivisions()
        _customizedDivisions.update { it + divisionId }
    }

    fun resetDivisionToDefault(divisionId: DivisionId) {
        timetableRepo.resetDivision(divisionId)
        _divisions.value = timetableRepo.loadAllDivisions()
        _schedules.update { map ->
            map + (divisionId to timetableRepo.loadSchedule(divisionId))
        }
        _customizedDivisions.update { it - divisionId }
    }

    private fun commitSchedule(divisionId: DivisionId, schedule: List<DaySchedule>) {
        timetableRepo.saveSchedule(divisionId, schedule)
        _schedules.update { it + (divisionId to schedule) }
        _customizedDivisions.update { it + divisionId }
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
