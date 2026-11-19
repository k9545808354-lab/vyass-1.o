package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.CyberBadge
import com.example.ui.components.SkipGateButton
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalVyasColors
import com.example.ui.theme.VyasBackground
import kotlinx.coroutines.delay

@Composable
fun BiometricGateScreen(
    onSequenceComplete: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val vyas = LocalVyasColors.current
    var stageStep by remember { mutableIntStateOf(1) }
    var isWaddling by remember { mutableStateOf(true) }
    var showKeyInHand by remember { mutableStateOf(false) }
    var keyInserted by remember { mutableStateOf(false) }
    var doorUnlocked by remember { mutableStateOf(false) }
    var hudText by remember {
        mutableStateOf("[1/4] Teddy Bear is waddling toward biometric vault door...")
    }

    val teddyProgress = remember { Animatable(0f) }

    val doorOpenFraction by animateFloatAsState(
        targetValue = if (doorUnlocked) 1f else 0f,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "door_open"
    )

    val keyInsertFraction by animateFloatAsState(
        targetValue = if (keyInserted) 1f else 0f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "key_insert"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "gate_loop")
    val walkPhase by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(380, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "walk_phase"
    )
    val scanBeamPhase by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scan_beam"
    )

    LaunchedEffect(Unit) {
        teddyProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 3300, easing = FastOutSlowInEasing)
        )
        isWaddling = false
        stageStep = 2
        showKeyInHand = true
        hudText = "[2/4] Teddy reaches into front pocket & pulls out the Master Key..."
        delay(1400)

        stageStep = 3
        keyInserted = true
        hudText = "[3/4] Key inserted into biometric lock! Scanning credentials..."
        delay(1400)

        stageStep = 4
        doorUnlocked = true
        hudText = "⚡ [4/4] ACCESS GRANTED! Vault Doors Opened."
        delay(1600)

        onSequenceComplete()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VyasBackground)
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 860.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CyberBadge(text = stringResource(R.string.badge_biometric_gate))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.title_authenticating),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                SkipGateButton(
                    text = stringResource(R.string.btn_skip),
                    onClick = onSkip,
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Vault Stage
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
                    .testTag("vault_stage"),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, vyas.border),
                color = Color(0xFF060910)
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(18.dp))
                ) {
                    val stageWidthPx = constraints.maxWidth.toFloat()
                    val stageHeightPx = constraints.maxHeight.toFloat()

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val floorHeight = size.height * 0.24f
                        val floorTop = size.height - floorHeight

                        // Radial backdrop glow
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF141C30), Color(0xFF060910)),
                                center = Offset(size.width * 0.5f, size.height * 0.35f),
                                radius = size.width * 0.65f
                            )
                        )

                        // Floor gradient
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF16243D), Color(0xFF090E1A)),
                                startY = floorTop,
                                endY = size.height
                            ),
                            topLeft = Offset(0f, floorTop),
                            size = Size(size.width, floorHeight)
                        )

                        // Cyber grid lines on floor
                        val gridStep = 24.dp.toPx()
                        var gx = 0f
                        while (gx < size.width) {
                            drawLine(
                                color = vyas.cyan.copy(alpha = 0.08f),
                                start = Offset(gx, floorTop),
                                end = Offset(gx, size.height),
                                strokeWidth = 1f
                            )
                            gx += gridStep
                        }
                        var gy = floorTop
                        while (gy < size.height) {
                            drawLine(
                                color = vyas.cyan.copy(alpha = 0.08f),
                                start = Offset(0f, gy),
                                end = Offset(size.width, gy),
                                strokeWidth = 1f
                            )
                            gy += gridStep
                        }

                        // Glowing cyan horizon line
                        drawLine(
                            color = vyas.cyan,
                            start = Offset(0f, floorTop),
                            end = Offset(size.width, floorTop),
                            strokeWidth = 2.5.dp.toPx()
                        )

                        // Vault Door dimensions
                        val doorW = (size.width * 0.28f).coerceIn(115.dp.toPx(), 165.dp.toPx())
                        val doorH = size.height * 0.60f
                        val doorLeft = size.width - doorW - 24.dp.toPx()
                        val doorTop = floorTop - doorH + 4.dp.toPx()

                        // Door inner core glow
                        drawRoundRect(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    vyas.green.copy(alpha = 0.55f),
                                    vyas.cyan.copy(alpha = 0.28f),
                                    Color.Black
                                ),
                                center = Offset(doorLeft + doorW / 2f, doorTop + doorH / 2f),
                                radius = doorH * 0.6f
                            ),
                            topLeft = Offset(doorLeft, doorTop),
                            size = Size(doorW, doorH),
                            cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                        )

                        // Sliding Left & Right Vault Door Panels
                        val halfW = doorW / 2f
                        val slideOffset = halfW * doorOpenFraction
                        // Left panel
                        drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF1B263B), Color(0xFF0D1522))
                            ),
                            topLeft = Offset(doorLeft - slideOffset, doorTop),
                            size = Size(halfW, doorH)
                        )
                        // Right panel
                        drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF1B263B), Color(0xFF0D1522))
                            ),
                            topLeft = Offset(doorLeft + halfW + slideOffset, doorTop),
                            size = Size(halfW, doorH)
                        )

                        // Door Frame border
                        drawRoundRect(
                            color = Color(0xFF23344E),
                            topLeft = Offset(doorLeft, doorTop),
                            size = Size(doorW, doorH),
                            cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx()),
                            style = Stroke(width = 4.dp.toPx())
                        )

                        // Biometric Scanner Ring (center of vault door)
                        val scannerCenter = Offset(doorLeft + doorW / 2f, doorTop + doorH / 2f)
                        val scannerRadius = 21.dp.toPx()
                        val scannerColor = if (doorUnlocked) vyas.green else vyas.red

                        if (doorOpenFraction < 0.7f) {
                            drawCircle(
                                color = Color(0xFF090D16),
                                radius = scannerRadius,
                                center = scannerCenter
                            )
                            drawCircle(
                                color = scannerColor,
                                radius = scannerRadius,
                                center = scannerCenter,
                                style = Stroke(width = 2.5.dp.toPx())
                            )
                            // Scan beam
                            val beamY = scannerCenter.y + scanBeamPhase * (scannerRadius * 0.55f)
                            drawLine(
                                color = scannerColor,
                                start = Offset(scannerCenter.x - scannerRadius * 0.7f, beamY),
                                end = Offset(scannerCenter.x + scannerRadius * 0.7f, beamY),
                                strokeWidth = 2.dp.toPx()
                            )
                        }

                        // Teddy Bear Actor
                        val teddyScale = (size.height / 360f).coerceIn(0.85f, 1.3f) * density
                        val teddyWidth = 104f * teddyScale
                        val startX = 16.dp.toPx()
                        val endX = (doorLeft - teddyWidth - 10.dp.toPx()).coerceAtLeast(startX + 40f)
                        val currentTeddyX = startX + (endX - startX) * teddyProgress.value
                        val teddyTop = floorTop - (146f * teddyScale)

                        drawTeddyBear(
                            origin = Offset(currentTeddyX, teddyTop),
                            scale = teddyScale,
                            waddling = isWaddling,
                            walkPhase = walkPhase
                        )

                        // Golden Key animation
                        if (showKeyInHand) {
                            val handStart = Offset(
                                currentTeddyX + 68f * teddyScale,
                                teddyTop + 86f * teddyScale
                            )
                            val keyPos = Offset(
                                handStart.x + (scannerCenter.x - handStart.x) * keyInsertFraction,
                                handStart.y + (scannerCenter.y - handStart.y) * keyInsertFraction
                            )
                            val keyRot = -20f + 110f * keyInsertFraction
                            drawGoldenKey(center = keyPos, rotationDeg = keyRot, scale = teddyScale)
                        }
                    }

                    // Overlay label inside opened vault core
                    if (doorUnlocked) {
                        val doorWidthDp = (maxWidth * 0.28f).coerceIn(115.dp, 165.dp)
                        val doorHeightDp = maxHeight * 0.60f
                        val floorHeightDp = maxHeight * 0.24f
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 24.dp, bottom = (floorHeightDp - 4.dp).coerceAtLeast(0.dp))
                                .size(width = doorWidthDp, height = doorHeightDp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "⚡ VYAS CORE",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                letterSpacing = 1.5.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status HUD
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("biometric_status_hud"),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0B101A),
                border = BorderStroke(1.dp, vyas.border)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (stageStep < 4) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = vyas.cyan,
                            strokeWidth = 2.dp
                        )
                    }
                    Text(
                        text = hudText,
                        fontFamily = JetBrainsMonoFontFamily,
                        color = if (stageStep == 4) vyas.green else vyas.cyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawTeddyBear(
    origin: Offset,
    scale: Float,
    waddling: Boolean,
    walkPhase: Float
) {
    val furDark = Color(0xFF8C471C)
    val furMain = Color(0xFFA65823)
    val furInner = Color(0xFFE0A36E)
    val outline = Color(0xFF5A2B0C)
    val gold = Color(0xFFFFD700)
    val eyeDark = Color(0xFF150A04)

    val bounceY = if (waddling) kotlin.math.abs(walkPhase) * -4f * scale else 0f
    val earRot = if (waddling) walkPhase * 3f else 0f
    val legSwing = if (waddling) walkPhase * 22f else 0f
    val armSwing = if (waddling) -walkPhase * 18f else 0f

    translate(left = origin.x, top = origin.y + bounceY) {
        // Ears
        rotate(degrees = earRot, pivot = Offset(52f * scale, 30f * scale)) {
            drawCircle(furDark, 14f * scale, Offset(28f * scale, 28f * scale))
            drawCircle(outline, 14f * scale, Offset(28f * scale, 28f * scale), style = Stroke(2f * scale))
            drawCircle(furInner, 7f * scale, Offset(28f * scale, 28f * scale))

            drawCircle(furDark, 14f * scale, Offset(76f * scale, 28f * scale))
            drawCircle(outline, 14f * scale, Offset(76f * scale, 28f * scale), style = Stroke(2f * scale))
            drawCircle(furInner, 7f * scale, Offset(76f * scale, 28f * scale))
        }

        // Legs (drawn behind body)
        rotate(degrees = legSwing, pivot = Offset(38f * scale, 122f * scale)) {
            drawOval(furDark, Offset(29f * scale, 108f * scale), Size(18f * scale, 36f * scale))
            drawOval(outline, Offset(29f * scale, 108f * scale), Size(18f * scale, 36f * scale), style = Stroke(2f * scale))
            drawCircle(furInner, 6f * scale, Offset(38f * scale, 138f * scale))
        }
        rotate(degrees = -legSwing, pivot = Offset(66f * scale, 122f * scale)) {
            drawOval(furDark, Offset(57f * scale, 108f * scale), Size(18f * scale, 36f * scale))
            drawOval(outline, Offset(57f * scale, 108f * scale), Size(18f * scale, 36f * scale), style = Stroke(2f * scale))
            drawCircle(furInner, 6f * scale, Offset(66f * scale, 138f * scale))
        }

        // Torso & Pocket
        drawOval(furMain, Offset(26f * scale, 64f * scale), Size(52f * scale, 60f * scale))
        drawOval(outline, Offset(26f * scale, 64f * scale), Size(52f * scale, 60f * scale), style = Stroke(2f * scale))
        drawOval(furInner, Offset(36f * scale, 75f * scale), Size(32f * scale, 40f * scale))

        // Front pocket with gold trim
        drawRoundRect(
            color = furDark,
            topLeft = Offset(42f * scale, 94f * scale),
            size = Size(20f * scale, 15f * scale),
            cornerRadius = CornerRadius(4f * scale, 4f * scale)
        )
        drawRoundRect(
            color = gold,
            topLeft = Offset(42f * scale, 94f * scale),
            size = Size(20f * scale, 15f * scale),
            cornerRadius = CornerRadius(4f * scale, 4f * scale),
            style = Stroke(1.5f * scale)
        )
        drawLine(
            color = gold,
            start = Offset(45f * scale, 98f * scale),
            end = Offset(59f * scale, 98f * scale),
            strokeWidth = 1.2f * scale
        )

        // Left & Right Arms
        rotate(degrees = armSwing + 15f, pivot = Offset(23f * scale, 75f * scale)) {
            drawOval(furDark, Offset(15f * scale, 67f * scale), Size(16f * scale, 36f * scale))
            drawOval(outline, Offset(15f * scale, 67f * scale), Size(16f * scale, 36f * scale), style = Stroke(2f * scale))
            drawCircle(furInner, 6f * scale, Offset(23f * scale, 99f * scale))
        }
        rotate(degrees = -15f, pivot = Offset(80f * scale, 75f * scale)) {
            drawOval(furDark, Offset(72f * scale, 67f * scale), Size(16f * scale, 36f * scale))
            drawOval(outline, Offset(72f * scale, 67f * scale), Size(16f * scale, 36f * scale), style = Stroke(2f * scale))
            drawCircle(furInner, 6f * scale, Offset(80f * scale, 99f * scale))
        }

        // Head
        drawOval(furMain, Offset(22f * scale, 20f * scale), Size(60f * scale, 52f * scale))
        drawOval(outline, Offset(22f * scale, 20f * scale), Size(60f * scale, 52f * scale), style = Stroke(2f * scale))

        // Snout
        drawOval(furInner, Offset(38f * scale, 42f * scale), Size(28f * scale, 22f * scale))

        // Nose
        val nosePath = Path().apply {
            moveTo(48f * scale, 48f * scale)
            lineTo(56f * scale, 48f * scale)
            lineTo(52f * scale, 53f * scale)
            close()
        }
        drawPath(nosePath, color = Color(0xFF2D1607))

        // Smile
        val smilePath = Path().apply {
            moveTo(48f * scale, 55f * scale)
            quadraticTo(52f * scale, 59f * scale, 56f * scale, 55f * scale)
        }
        drawPath(smilePath, color = Color(0xFF2D1607), style = Stroke(width = 1.8f * scale, cap = StrokeCap.Round))

        // Eyes
        drawCircle(eyeDark, 4f * scale, Offset(42f * scale, 42f * scale))
        drawCircle(Color.White, 1.5f * scale, Offset(43f * scale, 40f * scale))
        drawCircle(eyeDark, 4f * scale, Offset(62f * scale, 42f * scale))
        drawCircle(Color.White, 1.5f * scale, Offset(63f * scale, 40f * scale))
    }
}

private fun DrawScope.drawGoldenKey(
    center: Offset,
    rotationDeg: Float,
    scale: Float
) {
    val gold = Color(0xFFFFD700)
    val goldDark = Color(0xFFB8860B)
    rotate(degrees = rotationDeg, pivot = center) {
        // Key ring
        drawCircle(
            color = gold,
            radius = 7f * scale,
            center = Offset(center.x - 8f * scale, center.y),
            style = Stroke(width = 2.6f * scale)
        )
        // Shaft
        drawLine(
            color = gold,
            start = Offset(center.x - 1f * scale, center.y),
            end = Offset(center.x + 16f * scale, center.y),
            strokeWidth = 3f * scale,
            cap = StrokeCap.Round
        )
        // Teeth
        drawLine(
            color = goldDark,
            start = Offset(center.x + 10f * scale, center.y),
            end = Offset(center.x + 10f * scale, center.y + 5f * scale),
            strokeWidth = 2.5f * scale
        )
        drawLine(
            color = gold,
            start = Offset(center.x + 14f * scale, center.y),
            end = Offset(center.x + 14f * scale, center.y + 6f * scale),
            strokeWidth = 2.5f * scale
        )
    }
}
