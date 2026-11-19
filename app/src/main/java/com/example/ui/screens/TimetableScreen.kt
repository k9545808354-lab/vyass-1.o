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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.BatchAccent
import com.example.data.model.DaySchedule
import com.example.data.model.DivisionId
import com.example.data.model.TimetableCell
import com.example.data.repository.TimetableData
import com.example.ui.components.VyasNavHeader
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalVyasColors
import com.example.ui.theme.VyasBackground

private val DAY_COL_W = 58.dp
private val SLOT_W = 128.dp
private val LUNCH_W = 42.dp
private val HEADER_H = 56.dp
private val ROW_H = 114.dp
private val GRID_BORDER = Color(0xFF1F2C42)

@Composable
fun TimetableScreen(
    divisionId: DivisionId,
    onBackToDivisions: () -> Unit,
    onSubjectClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val vyas = LocalVyasColors.current
    val divInfo = TimetableData.getDivisionInfo(divisionId)
    val schedule = TimetableData.getSchedule(divisionId)

    val wefColor = when (divisionId) {
        DivisionId.E -> vyas.cyan
        DivisionId.F -> vyas.purple
        DivisionId.G -> vyas.green
    }

    val tipMessage = if (divisionId == DivisionId.F) {
        "💡 Tap any subject (EM-I, PPS, BEEE, EP, etc.) to view units or run the C compilers."
    } else {
        "💡 Tap PPS to access the Dual Dark C Compilers and Unit Notes 1 to 5."
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VyasBackground)
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 960.dp)
                .fillMaxWidth()
        ) {
            VyasNavHeader(
                backLabel = stringResource(R.string.btn_back_divisions),
                onBack = onBackToDivisions,
                rightContent = {
                    Text(
                        text = "W.E.F.: ${divInfo.wefDate}",
                        color = wefColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            )

            // Meta Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = vyas.cardBg,
                border = BorderStroke(1.dp, vyas.border)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color.White)) { append("CLASS: ") }
                            append("FE  |  ")
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color.White)) { append("DIVISION: ") }
                            append("${divInfo.id.code}  |  ")
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color.White)) { append("BRANCH: ") }
                            append("COMPUTER")
                        },
                        color = Color(0xFFDDE6F5),
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color.White)) { append("ROOM: ") }
                            append(divInfo.classRoom)
                        },
                        color = vyas.cyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tip Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = vyas.yellow.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, vyas.yellow.copy(alpha = 0.25f))
            ) {
                Text(
                    text = tipMessage,
                    color = vyas.yellow,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Official Scrollable Timetable Matrix
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("official_timetable_grid"),
                shape = RoundedCornerShape(12.dp),
                color = vyas.cardBg,
                border = BorderStroke(1.5.dp, vyas.border)
            ) {
                Box(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                ) {
                    OfficialTimetableGrid(
                        schedule = schedule,
                        onSubjectClick = onSubjectClick
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun OfficialTimetableGrid(
    schedule: List<DaySchedule>,
    onSubjectClick: (String) -> Unit
) {
    val vyas = LocalVyasColors.current

    Column {
        // Table Header
        Row(
            modifier = Modifier
                .height(HEADER_H)
                .background(Color(0xFF0D1421))
        ) {
            HeaderBox(text = "Time", width = DAY_COL_W, height = HEADER_H)
            TimeSlotHeader(time = "11.00 - 12.00", slotNum = "1", width = SLOT_W)
            TimeSlotHeader(time = "12.00 - 1.00", slotNum = "2", width = SLOT_W)
            HeaderBox(text = "1.00 -\n1.45", width = LUNCH_W, height = HEADER_H)
            TimeSlotHeader(time = "1.45 - 2.45", slotNum = "4", width = SLOT_W)
            TimeSlotHeader(time = "2.45 - 3.45", slotNum = "5", width = SLOT_W)
            TimeSlotHeader(time = "3.45 - 4.45", slotNum = "6", width = SLOT_W)
            TimeSlotHeader(time = "4.45 - 5.45", slotNum = "7", width = SLOT_W)
        }

        // Body: Pre-Lunch Columns + Vertical Lunch Break + Post-Lunch Columns
        Row {
            // Left section: Day + Slots 1 & 2
            Column {
                schedule.forEach { dayRow ->
                    Row(modifier = Modifier.height(ROW_H)) {
                        DayCell(day = dayRow.day)
                        dayRow.preLunchCells.forEach { cell ->
                            TimetableCellView(
                                cell = cell,
                                width = SLOT_W * cell.colSpan,
                                onSubjectClick = onSubjectClick
                            )
                        }
                    }
                }
            }

            // Center Lunch Break spanning all 6 rows
            Box(
                modifier = Modifier
                    .width(LUNCH_W)
                    .height(ROW_H * schedule.size)
                    .background(vyas.yellow.copy(alpha = 0.08f))
                    .border(0.5.dp, GRID_BORDER),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    "LUNCH BREAK".forEach { ch ->
                        Text(
                            text = if (ch == ' ') "" else ch.toString(),
                            color = vyas.yellow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 13.sp
                        )
                    }
                }
            }

            // Right section: Slots 4, 5, 6, 7
            Column {
                schedule.forEach { dayRow ->
                    Row(modifier = Modifier.height(ROW_H)) {
                        dayRow.postLunchCells.forEach { cell ->
                            TimetableCellView(
                                cell = cell,
                                width = SLOT_W * cell.colSpan,
                                onSubjectClick = onSubjectClick
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderBox(text: String, width: Dp, height: Dp) {
    val vyas = LocalVyasColors.current
    Box(
        modifier = Modifier
            .width(width)
            .height(height)
            .border(0.5.dp, GRID_BORDER)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = vyas.cyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            lineHeight = 13.sp
        )
    }
}

@Composable
private fun TimeSlotHeader(time: String, slotNum: String, width: Dp) {
    val vyas = LocalVyasColors.current
    Column(
        modifier = Modifier
            .width(width)
            .height(HEADER_H)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(0.5.dp, GRID_BORDER),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = time,
                color = vyas.cyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.85f)
                .border(0.5.dp, GRID_BORDER),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = slotNum,
                color = vyas.cyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun DayCell(day: String) {
    Box(
        modifier = Modifier
            .width(DAY_COL_W)
            .fillMaxHeight()
            .background(Color(0xFF111A2A))
            .border(0.5.dp, GRID_BORDER),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun TimetableCellView(
    cell: TimetableCell,
    width: Dp,
    onSubjectClick: (String) -> Unit
) {
    val vyas = LocalVyasColors.current
    Box(
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
            .border(0.5.dp, GRID_BORDER)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        when (cell) {
            is TimetableCell.Empty -> {
                Text(
                    text = "-",
                    color = vyas.textDim,
                    fontSize = 13.sp
                )
            }
            is TimetableCell.Lecture -> {
                val borderAndText = when {
                    cell.isPpsHighlight || cell.isWplHighlight -> vyas.green
                    cell.isGuardianHour -> vyas.amber
                    else -> Color.White
                }
                val borderColor = when {
                    cell.isPpsHighlight || cell.isWplHighlight -> vyas.green
                    cell.isGuardianHour -> vyas.amber
                    else -> vyas.cyan.copy(alpha = 0.35f)
                }
                val bgColor = when {
                    cell.isPpsHighlight || cell.isWplHighlight -> vyas.green.copy(alpha = 0.10f)
                    cell.isGuardianHour -> vyas.amber.copy(alpha = 0.10f)
                    else -> vyas.cyan.copy(alpha = 0.12f)
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(bgColor)
                            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
                            .then(
                                if (cell.clickable) {
                                    Modifier
                                        .clickable { onSubjectClick(cell.subjectCode) }
                                        .testTag("subject_chip_${cell.subjectCode}")
                                } else Modifier
                            )
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (cell.isPpsHighlight) "PPS 💻" else cell.subjectCode,
                            color = borderAndText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = cell.faculty,
                        color = if (cell.isWplHighlight) vyas.green else vyas.textDim,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
            is TimetableCell.BatchSplit -> {
                val accentColor = when (cell.accent) {
                    BatchAccent.CYAN -> vyas.cyan
                    BatchAccent.GREEN -> vyas.green
                    BatchAccent.PURPLE -> vyas.purple
                }
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
                ) {
                    cell.batches.forEach { entry ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF0A0E17))
                                .drawBehind {
                                    drawLine(
                                        color = accentColor,
                                        start = Offset(0f, 0f),
                                        end = Offset(0f, size.height),
                                        strokeWidth = 3.dp.toPx()
                                    )
                                }
                                .then(
                                    if (entry.subjectCode != null) {
                                        Modifier.clickable { onSubjectClick(entry.subjectCode) }
                                    } else Modifier
                                )
                                .padding(start = 7.dp, end = 6.dp, top = 5.dp, bottom = 5.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = entry.batchLabel,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (entry.subjectCode != null) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(vyas.cyan.copy(alpha = 0.12f))
                                            .border(0.8.dp, vyas.cyan.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = entry.displayLabel,
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                } else {
                                    Text(
                                        text = entry.displayLabel,
                                        color = vyas.textDim,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            if (entry.room != null) {
                                Text(
                                    text = entry.room,
                                    color = vyas.textDim,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
