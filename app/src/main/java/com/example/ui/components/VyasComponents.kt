package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalVyasColors

@Composable
fun CyberBadge(
    text: String,
    accentColor: Color = LocalVyasColors.current.cyan,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = accentColor.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, accentColor)
    ) {
        Text(
            text = text,
            color = accentColor,
            style = MaterialTheme.typography.labelMedium,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
        )
    }
}

@Composable
fun SkipGateButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val vyas = LocalVyasColors.current
    Surface(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .testTag("skip_animation_button"),
        shape = RoundedCornerShape(50),
        color = Color.White.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, vyas.border)
    ) {
        Text(
            text = text,
            color = vyas.textDim,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun VyasNavHeader(
    backLabel: String,
    onBack: () -> Unit,
    rightContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val vyas = LocalVyasColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, vyas.border, RoundedCornerShape(8.dp))
                .background(vyas.cardBg.copy(alpha = 0.6f))
                .clickable(onClick = onBack)
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .testTag("nav_back_button"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = backLabel,
                tint = vyas.cyan
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = backLabel.removePrefix("‹ "),
                color = vyas.cyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Box(contentAlignment = Alignment.CenterEnd) {
            rightContent()
        }
    }
}

@Composable
fun ResponsiveContentContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(modifier = Modifier.widthIn(max = 960.dp).fillMaxWidth()) {
            content()
        }
    }
}
