package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.repository.TimetableData
import com.example.ui.components.VyasNavHeader
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalVyasColors
import com.example.ui.theme.VyasBackground

@Composable
fun SubjectUnitsScreen(
    subjectCode: String,
    onBackToTimetable: () -> Unit,
    onOpenCompilers: () -> Unit,
    onUnitClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val vyas = LocalVyasColors.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VyasBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxWidth()
        ) {
            VyasNavHeader(
                backLabel = stringResource(R.string.btn_back_timetable),
                onBack = onBackToTimetable,
                rightContent = {
                    Text(
                        text = "$subjectCode - Units (1 to 5)",
                        color = vyas.cyan,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            )

            if (subjectCode == "PPS") {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(onClick = onOpenCompilers)
                        .testTag("open_dual_compilers_card"),
                    shape = RoundedCornerShape(14.dp),
                    color = Color.Transparent,
                    border = BorderStroke(1.5.dp, vyas.cyan)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        vyas.cyan.copy(alpha = 0.14f),
                                        vyas.green.copy(alpha = 0.09f)
                                    )
                                )
                            )
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(text = "💻", fontSize = 28.sp)

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Dual Dark C Compilers (IDE)",
                                    color = vyas.cyan,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = vyas.green.copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, vyas.green)
                                ) {
                                    Text(
                                        text = "2 ENGINES",
                                        color = vyas.green,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Two fast dark-mode GCC compilers with stdin input and live execution",
                                color = vyas.textDim,
                                fontSize = 12.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = vyas.cyan
                        ) {
                            Text(
                                text = "OPEN →",
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }

            TimetableData.unitNames.forEachIndexed { idx, unit ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onUnitClick(unit) }
                        .testTag("unit_card_${idx + 1}"),
                    shape = RoundedCornerShape(12.dp),
                    color = vyas.cardBg,
                    border = BorderStroke(1.dp, vyas.border)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF151D2D)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "0${idx + 1}",
                                color = vyas.green,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = unit,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Notes, assignments and PDFs",
                                color = vyas.textDim,
                                fontSize = 12.sp
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = unit,
                            tint = vyas.cyan
                        )
                    }
                }
            }
        }
    }
}
