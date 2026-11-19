package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
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
fun LaptopKernelScreen(
    onSequenceComplete: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val vyas = LocalVyasColors.current
    val codeLines = remember { buildHighlightedCodeLines() }
    var visibleLineCount by remember { mutableIntStateOf(0) }
    var showTerminal by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        for (i in 1..codeLines.size) {
            delay(110)
            visibleLineCount = i
        }
        delay(500)
        showTerminal = true
        delay(2200)
        onSequenceComplete()
    }

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
                .widthIn(max = 720.dp)
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
                    CyberBadge(
                        text = stringResource(R.string.badge_c_kernel),
                        accentColor = vyas.green
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.title_compiling_entry),
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

            Spacer(modifier = Modifier.height(22.dp))

            // Laptop Lid
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("laptop_c_terminal"),
                shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp),
                color = vyas.surfaceElevated,
                border = BorderStroke(2.dp, Color(0xFF28374D))
            ) {
                Column {
                    // Editor top bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF090E17))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(Modifier.size(11.dp).clip(CircleShape).background(Color(0xFFFF5F56)))
                            Box(Modifier.size(11.dp).clip(CircleShape).background(Color(0xFFFFBD2E)))
                            Box(Modifier.size(11.dp).clip(CircleShape).background(Color(0xFF27C93F)))
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(vyas.surfaceElevated)
                                .border(1.dp, vyas.border, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "⚡ main.c",
                                color = vyas.cyan,
                                fontSize = 11.sp,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // C Code Editor Body
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF080C14))
                            .padding(16.dp)
                    ) {
                        codeLines.take(visibleLineCount).forEach { line ->
                            Text(
                                text = line,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 12.sp,
                                lineHeight = 19.sp
                            )
                        }
                        if (visibleLineCount < codeLines.size) {
                            Spacer(modifier = Modifier.height((19 * (codeLines.size - visibleLineCount)).dp.coerceAtMost(140.dp)))
                        }
                    }

                    // Terminal Output
                    AnimatedVisibility(visible = showTerminal) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF04070C))
                                .border(1.dp, Color(0xFF1A2536))
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = "$ gcc main.c -o vyas && ./vyas",
                                color = Color(0xFF60A5FA),
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "====================================",
                                color = vyas.yellow,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "       WELCOME TO VYAS (SSBT)       ",
                                color = vyas.green,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "====================================",
                                color = vyas.yellow,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 12.sp
                            )
                            Text(
                                text = ">> Academic Year : 2026-27 (Term-I)",
                                color = vyas.green,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 12.sp
                            )
                            Text(
                                text = ">> Department    : Computer Engg (Divisions E, F, G)",
                                color = vyas.green,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "✔ Process exited with status 0. Launching timetable...",
                                color = Color(0xFF38BDF8),
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Laptop Base Tray
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF2B394F), Color(0xFF161E2B))
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = Color(0xFF3D4E68),
                        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                    ),
                contentAlignment = Alignment.TopCenter
            ) {
                Box(
                    modifier = Modifier
                        .width(90.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp))
                        .background(Color(0xFF101623))
                )
            }
        }
    }
}

private fun buildHighlightedCodeLines(): List<AnnotatedString> {
    val keyword = SpanStyle(color = Color(0xFFFF7B72), fontWeight = FontWeight.Bold)
    val header = SpanStyle(color = Color(0xFFA5D6FF))
    val func = SpanStyle(color = Color(0xFFD2A8FF), fontWeight = FontWeight.Bold)
    val str = SpanStyle(color = Color(0xFF7EE787))
    val comment = SpanStyle(color = Color(0xFF8B949E), fontStyle = FontStyle.Italic)
    val plain = SpanStyle(color = Color(0xFFCBD5E1))

    return listOf(
        buildAnnotatedString { withStyle(comment) { append("// Loading SSBT First Year Engineering System") } },
        buildAnnotatedString {
            withStyle(keyword) { append("#include ") }
            withStyle(header) { append("<stdio.h>") }
        },
        buildAnnotatedString {
            withStyle(keyword) { append("#include ") }
            withStyle(header) { append("<vyas_kernel.h>") }
        },
        buildAnnotatedString { append("") },
        buildAnnotatedString {
            withStyle(keyword) { append("int ") }
            withStyle(func) { append("main") }
            withStyle(plain) { append("() {") }
        },
        buildAnnotatedString {
            withStyle(comment) { append("    /* Authenticated successfully via Biometric Door */") }
        },
        buildAnnotatedString {
            withStyle(plain) { append("    ") }
            withStyle(func) { append("printf") }
            withStyle(plain) { append("(") }
            withStyle(str) { append("\"\\n====================================\\n\"") }
            withStyle(plain) { append(");") }
        },
        buildAnnotatedString {
            withStyle(plain) { append("    ") }
            withStyle(func) { append("printf") }
            withStyle(plain) { append("(") }
            withStyle(str) { append("\"       WELCOME TO VYAS (SSBT)       \\n\"") }
            withStyle(plain) { append(");") }
        },
        buildAnnotatedString {
            withStyle(plain) { append("    ") }
            withStyle(func) { append("printf") }
            withStyle(plain) { append("(") }
            withStyle(str) { append("\"====================================\\n\"") }
            withStyle(plain) { append(");") }
        },
        buildAnnotatedString {
            withStyle(plain) { append("    ") }
            withStyle(func) { append("printf") }
            withStyle(plain) { append("(") }
            withStyle(str) { append("\">> Academic Year : 2026-27 (Term-I)\\n\"") }
            withStyle(plain) { append(");") }
        },
        buildAnnotatedString {
            withStyle(plain) { append("    ") }
            withStyle(func) { append("printf") }
            withStyle(plain) { append("(") }
            withStyle(str) { append("\">> Department    : Computer Engg (E, F, G)\\n\"") }
            withStyle(plain) { append(");") }
        },
        buildAnnotatedString {
            withStyle(plain) { append("    ") }
            withStyle(keyword) { append("return ") }
            withStyle(header) { append("0") }
            withStyle(plain) { append(";") }
        },
        buildAnnotatedString { withStyle(plain) { append("}") } }
    )
}
