package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.BatchAccent
import com.example.data.model.BatchEntry
import com.example.data.model.DivisionId
import com.example.data.model.DivisionInfo
import com.example.data.model.TimetableCell
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalVyasColors

private enum class SlotEditorMode(val label: String) {
    LECTURE("Lecture"),
    BATCH("Lab Batches"),
    GUARDIAN("TGS"),
    FREE("Free (-)")
}

private data class EditableBatchRow(
    val batchLabel: String,
    val subjectCode: String,
    val displayLabel: String,
    val room: String
)

private val COMMON_SUBJECTS = listOf("EM-I", "EP", "BEEE", "PPS", "ENG", "CC", "WPL")

@Composable
fun EditSlotDialog(
    divisionId: DivisionId,
    dayName: String,
    slotTitle: String,
    initialCell: TimetableCell,
    canSplit: Boolean,
    canMergeNext: Boolean,
    onSave: (TimetableCell) -> Unit,
    onSplitCell: () -> Unit,
    onMergeNextCell: () -> Unit,
    onDismiss: () -> Unit
) {
    val vyas = LocalVyasColors.current

    val initialMode = when (initialCell) {
        is TimetableCell.Lecture -> if (initialCell.isGuardianHour) SlotEditorMode.GUARDIAN else SlotEditorMode.LECTURE
        is TimetableCell.BatchSplit -> SlotEditorMode.BATCH
        is TimetableCell.Empty -> SlotEditorMode.FREE
    }

    var selectedMode by remember { mutableStateOf(initialMode) }

    var subjectInput by remember {
        mutableStateOf(
            when (initialCell) {
                is TimetableCell.Lecture -> if (initialCell.isGuardianHour) "TGS" else initialCell.subjectCode
                else -> "PPS"
            }
        )
    }
    var facultyInput by remember {
        mutableStateOf(
            when (initialCell) {
                is TimetableCell.Lecture -> initialCell.faculty
                else -> ""
            }
        )
    }

    var batchAccent by remember {
        mutableStateOf(
            when (initialCell) {
                is TimetableCell.BatchSplit -> initialCell.accent
                else -> BatchAccent.GREEN
            }
        )
    }

    val batchRows = remember {
        val prefix = divisionId.code
        val initial = when (initialCell) {
            is TimetableCell.BatchSplit -> initialCell.batches.map {
                EditableBatchRow(
                    batchLabel = it.batchLabel,
                    subjectCode = it.subjectCode.orEmpty(),
                    displayLabel = it.displayLabel,
                    room = it.room.orEmpty()
                )
            }
            else -> listOf(
                EditableBatchRow("${prefix}1:", "PPS", "PPS-PRS", "(128)"),
                EditableBatchRow("${prefix}2:", "BEEE", "BEEE-RC", "(217)"),
                EditableBatchRow("${prefix}3:", "EP", "EP-CUN", "(242)")
            )
        }
        mutableStateListOf<EditableBatchRow>().apply { addAll(initial) }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .testTag("edit_slot_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = vyas.cardBg,
            border = BorderStroke(1.5.dp, vyas.cyan)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "EDIT SLOT • $dayName",
                            color = vyas.cyan,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = slotTitle,
                            color = vyas.textDim,
                            fontSize = 11.sp,
                            fontFamily = JetBrainsMonoFontFamily
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = vyas.surfaceDeep,
                        border = BorderStroke(1.dp, vyas.border)
                    ) {
                        Text(
                            text = "${initialCell.colSpan} HR",
                            color = vyas.green,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Slot Type Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SlotEditorMode.entries.forEach { mode ->
                        val active = selectedMode == mode
                        Surface(
                            modifier = Modifier
                                .heightIn(min = 40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedMode = mode }
                                .testTag("mode_chip_${mode.name.lowercase()}"),
                            shape = RoundedCornerShape(8.dp),
                            color = if (active) vyas.cyan else vyas.surfaceDeep,
                            border = BorderStroke(1.dp, if (active) vyas.cyan else vyas.border)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = mode.label,
                                    color = if (active) Color.Black else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                when (selectedMode) {
                    SlotEditorMode.LECTURE -> {
                        Text(
                            text = "QUICK SUBJECTS",
                            color = vyas.textDim,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            COMMON_SUBJECTS.forEach { sub ->
                                val isSel = subjectInput.trim().equals(sub, ignoreCase = true)
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { subjectInput = sub },
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isSel) vyas.green.copy(alpha = 0.18f) else vyas.surfaceDeep,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSel) vyas.green else vyas.border
                                    )
                                ) {
                                    Text(
                                        text = sub,
                                        color = if (isSel) vyas.green else Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        VyasDialogTextField(
                            value = subjectInput,
                            onValueChange = { subjectInput = it.uppercase() },
                            label = "Subject Code (e.g. PPS, EM-I, EP)",
                            testTag = "edit_subject_input"
                        )

                        VyasDialogTextField(
                            value = facultyInput,
                            onValueChange = { facultyInput = it.uppercase() },
                            label = "Faculty Code / Lab Staff (e.g. PRS, CUN)",
                            testTag = "edit_faculty_input"
                        )
                    }

                    SlotEditorMode.BATCH -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ACCENT COLOR",
                                color = vyas.textDim,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                BatchAccent.entries.forEach { acc ->
                                    val c = when (acc) {
                                        BatchAccent.CYAN -> vyas.cyan
                                        BatchAccent.GREEN -> vyas.green
                                        BatchAccent.PURPLE -> vyas.purple
                                    }
                                    val sel = batchAccent == acc
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable { batchAccent = acc },
                                        shape = RoundedCornerShape(6.dp),
                                        color = c.copy(alpha = if (sel) 0.28f else 0.08f),
                                        border = BorderStroke(if (sel) 1.5.dp else 1.dp, c)
                                    ) {
                                        Text(
                                            text = acc.name,
                                            color = c,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        batchRows.forEachIndexed { idx, row ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = vyas.surfaceDeep,
                                border = BorderStroke(1.dp, vyas.border)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Batch Row ${idx + 1}",
                                            color = vyas.cyan,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (batchRows.size > 1) {
                                            IconButton(
                                                onClick = { batchRows.removeAt(idx) }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Delete,
                                                    contentDescription = "Remove batch row",
                                                    tint = vyas.red
                                                )
                                            }
                                        }
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Box(modifier = Modifier.weight(0.8f)) {
                                            VyasDialogTextField(
                                                value = row.batchLabel,
                                                onValueChange = {
                                                    batchRows[idx] = row.copy(batchLabel = it.uppercase())
                                                },
                                                label = "Batch"
                                            )
                                        }
                                        Box(modifier = Modifier.weight(1f)) {
                                            VyasDialogTextField(
                                                value = row.subjectCode,
                                                onValueChange = {
                                                    batchRows[idx] = row.copy(subjectCode = it.uppercase())
                                                },
                                                label = "Subject (opt)"
                                            )
                                        }
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Box(modifier = Modifier.weight(1.3f)) {
                                            VyasDialogTextField(
                                                value = row.displayLabel,
                                                onValueChange = {
                                                    batchRows[idx] = row.copy(displayLabel = it.uppercase())
                                                },
                                                label = "Label (e.g. PPS-PRS)"
                                            )
                                        }
                                        Box(modifier = Modifier.weight(0.8f)) {
                                            VyasDialogTextField(
                                                value = row.room,
                                                onValueChange = {
                                                    batchRows[idx] = row.copy(room = it)
                                                },
                                                label = "Room"
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                val nextIdx = batchRows.size + 1
                                batchRows.add(
                                    EditableBatchRow(
                                        batchLabel = "${divisionId.code}$nextIdx:",
                                        subjectCode = "PPS",
                                        displayLabel = "PPS-PRS",
                                        room = "(128)"
                                    )
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, vyas.cyan.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, tint = vyas.cyan)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Batch Row", color = vyas.cyan, fontSize = 12.sp)
                        }
                    }

                    SlotEditorMode.GUARDIAN -> {
                        VyasDialogTextField(
                            value = subjectInput.ifBlank { "TGS" },
                            onValueChange = { subjectInput = it.uppercase() },
                            label = "Badge Label (e.g. TGS)"
                        )
                        VyasDialogTextField(
                            value = facultyInput.ifBlank { "Guardian Hour" },
                            onValueChange = { facultyInput = it },
                            label = "Subtitle (e.g. Guardian Hour)"
                        )
                    }

                    SlotEditorMode.FREE -> {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = vyas.surfaceDeep,
                            border = BorderStroke(1.dp, vyas.border)
                        ) {
                            Text(
                                text = "This slot will be marked as Free / No Lecture (-).",
                                color = vyas.textDim,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }

                // Split / Merge Slot Span Controls
                if (canSplit || canMergeNext) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (canSplit) {
                            OutlinedButton(
                                onClick = onSplitCell,
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 44.dp),
                                border = BorderStroke(1.dp, vyas.amber)
                            ) {
                                Text(
                                    text = "Split 2-Hr → Two 1-Hr",
                                    color = vyas.amber,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        if (canMergeNext) {
                            OutlinedButton(
                                onClick = onMergeNextCell,
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 44.dp),
                                border = BorderStroke(1.dp, vyas.purple)
                            ) {
                                Text(
                                    text = "Merge with Next Hr (2-Hr)",
                                    color = vyas.purple,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Text("Cancel", color = vyas.textDim, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val span = initialCell.colSpan
                            val updatedCell: TimetableCell = when (selectedMode) {
                                SlotEditorMode.LECTURE -> {
                                    val code = subjectInput.trim().ifEmpty { "PPS" }
                                    val fac = facultyInput.trim().ifEmpty { "-" }
                                    TimetableCell.Lecture(
                                        subjectCode = code,
                                        faculty = fac,
                                        isPpsHighlight = code.equals("PPS", ignoreCase = true),
                                        isWplHighlight = code.equals("WPL", ignoreCase = true),
                                        isGuardianHour = false,
                                        clickable = true,
                                        colSpan = span
                                    )
                                }
                                SlotEditorMode.BATCH -> {
                                    val entries = batchRows.map { row ->
                                        val cleanSub = row.subjectCode.trim().takeIf { it.isNotEmpty() }
                                        val cleanDisp = row.displayLabel.trim().ifEmpty {
                                            cleanSub ?: "LIB"
                                        }
                                        BatchEntry(
                                            batchLabel = row.batchLabel.trim().ifEmpty { "${divisionId.code}:" },
                                            subjectCode = cleanSub,
                                            displayLabel = cleanDisp,
                                            room = row.room.trim().takeIf { it.isNotEmpty() },
                                            accent = batchAccent
                                        )
                                    }
                                    TimetableCell.BatchSplit(
                                        batches = entries,
                                        accent = batchAccent,
                                        colSpan = span
                                    )
                                }
                                SlotEditorMode.GUARDIAN -> {
                                    TimetableCell.Lecture(
                                        subjectCode = subjectInput.trim().ifEmpty { "TGS" },
                                        faculty = facultyInput.trim().ifEmpty { "Guardian Hour" },
                                        isGuardianHour = true,
                                        clickable = false,
                                        colSpan = span
                                    )
                                }
                                SlotEditorMode.FREE -> TimetableCell.Empty(colSpan = span)
                            }
                            onSave(updatedCell)
                        },
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("save_slot_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = vyas.cyan,
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Save Slot", fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

@Composable
fun EditDivisionInfoDialog(
    divisionInfo: DivisionInfo,
    onSave: (classRoom: String, wefDate: String) -> Unit,
    onDismiss: () -> Unit
) {
    val vyas = LocalVyasColors.current
    var room by remember { mutableStateOf(divisionInfo.classRoom) }
    var wef by remember { mutableStateOf(divisionInfo.wefDate) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = vyas.cardBg,
        titleContentColor = vyas.cyan,
        title = {
            Text(
                text = "Edit ${divisionInfo.title} Details",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                VyasDialogTextField(
                    value = room,
                    onValueChange = { room = it },
                    label = "Class Room No. (e.g. 230)",
                    testTag = "edit_room_input"
                )
                VyasDialogTextField(
                    value = wef,
                    onValueChange = { wef = it },
                    label = "W.E.F. Date (e.g. 28/09/2026)",
                    testTag = "edit_wef_input"
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        room.trim().ifEmpty { divisionInfo.classRoom },
                        wef.trim().ifEmpty { divisionInfo.wefDate }
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = vyas.cyan,
                    contentColor = Color.Black
                ),
                modifier = Modifier.testTag("save_division_info_button")
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = vyas.textDim)
            }
        }
    )
}

@Composable
fun ResetTimetableConfirmDialog(
    divisionTitle: String,
    onConfirmReset: () -> Unit,
    onDismiss: () -> Unit
) {
    val vyas = LocalVyasColors.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = vyas.cardBg,
        titleContentColor = vyas.red,
        textContentColor = Color.White,
        title = {
            Text(
                text = "Reset $divisionTitle Timetable?",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "This will restore all lectures, labs, room numbers, and W.E.F. date for $divisionTitle back to the original official schedule.",
                fontSize = 13.sp,
                color = vyas.textDim
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmReset,
                colors = ButtonDefaults.buttonColors(
                    containerColor = vyas.red,
                    contentColor = Color.White
                )
            ) {
                Text("Reset Default", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = vyas.textDim)
            }
        }
    )
}

@Composable
private fun VyasDialogTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    testTag: String? = null
) {
    val vyas = LocalVyasColors.current
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                fontSize = 11.sp,
                color = vyas.textDim
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = vyas.surfaceDeep,
            unfocusedContainerColor = vyas.surfaceDeep,
            focusedBorderColor = vyas.cyan,
            unfocusedBorderColor = vyas.border,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = vyas.cyan
        ),
        modifier = Modifier
            .fillMaxWidth()
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
    )
}
