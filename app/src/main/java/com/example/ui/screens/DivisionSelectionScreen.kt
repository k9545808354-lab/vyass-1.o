package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.DivisionId
import com.example.data.model.DivisionInfo
import com.example.data.repository.TimetableData
import com.example.ui.theme.LocalVyasColors
import com.example.ui.theme.VyasBackground

@Composable
fun DivisionSelectionScreen(
    onDivisionSelected: (DivisionId) -> Unit,
    modifier: Modifier = Modifier
) {
    val vyas = LocalVyasColors.current
    val divisions = TimetableData.divisions

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VyasBackground)
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.header_fe_computer),
                color = vyas.textDim,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.title_select_division),
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(22.dp))

            divisions.forEach { div ->
                val accent = when (div.id) {
                    DivisionId.E -> vyas.cyan
                    DivisionId.F -> vyas.purple
                    DivisionId.G -> vyas.green
                }
                DivisionCard(
                    division = div,
                    accentColor = accent,
                    onClick = { onDivisionSelected(div.id) }
                )
                Spacer(modifier = Modifier.height(14.dp))
            }
        }
    }
}

@Composable
private fun DivisionCard(
    division: DivisionInfo,
    accentColor: Color,
    onClick: () -> Unit
) {
    val vyas = LocalVyasColors.current
    val badgeColor = if (division.id == DivisionId.F) vyas.purple else vyas.cyan

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("division_card_${division.id.code.lowercase()}"),
        shape = RoundedCornerShape(14.dp),
        color = vyas.cardBg,
        border = BorderStroke(1.dp, vyas.border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(badgeColor.copy(alpha = 0.10f))
                    .border(1.5.dp, badgeColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = division.id.code,
                    color = badgeColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = division.title,
                    color = accentColor,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = division.subtitle,
                    color = vyas.textDim,
                    fontSize = 12.sp
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = division.title,
                tint = vyas.cyan
            )
        }
    }
}
