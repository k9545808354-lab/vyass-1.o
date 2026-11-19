package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.PdfNoteItem
import com.example.ui.components.VyasNavHeader
import com.example.ui.theme.LocalVyasColors
import com.example.ui.theme.VyasBackground
import com.example.ui.viewmodel.PdfHubUiState

@Composable
fun PdfHubScreen(
    uiState: PdfHubUiState,
    onBackToUnits: () -> Unit,
    onImportPdf: (Uri) -> Unit,
    onOpenPdf: (PdfNoteItem) -> Unit,
    onClosePdfViewer: () -> Unit,
    onRequestSavePdf: (PdfNoteItem) -> Unit,
    onCompleteExport: (Uri?) -> Unit,
    onShowMessage: (String) -> Unit,
    onClearStatusMessage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val vyas = LocalVyasColors.current

    val openPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            onImportPdf(uri)
        }
    }

    val createPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        onCompleteExport(uri)
    }

    LaunchedEffect(uiState.statusMessage) {
        val msg = uiState.statusMessage
        if (msg != null) {
            onShowMessage(msg)
            onClearStatusMessage()
        }
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
                .widthIn(max = 760.dp)
                .fillMaxWidth()
        ) {
            VyasNavHeader(
                backLabel = stringResource(R.string.btn_back_units),
                onBack = onBackToUnits,
                rightContent = {
                    Text(
                        text = "${uiState.subject} • ${uiState.unit}",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            )

            // Upload Real PDF Notes CTA (Dashed Green Border)
            val greenColor = vyas.green
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(greenColor.copy(alpha = 0.08f))
                    .drawBehind {
                        drawRoundRect(
                            color = greenColor,
                            cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx()),
                            style = Stroke(
                                width = 1.5.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 10f), 0f)
                            )
                        )
                    }
                    .clickable { openPdfLauncher.launch(arrayOf("application/pdf")) }
                    .padding(18.dp)
                    .testTag("upload_pdf_cta"),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "📤", fontSize = 26.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.upload_cta_title),
                        color = vyas.green,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = stringResource(R.string.upload_cta_sub),
                        color = vyas.textDim,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.available_pdfs_header),
                color = vyas.cyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.items.isEmpty()) {
                Text(
                    text = "No PDFs uploaded for ${uiState.subject} ${uiState.unit} yet. Use the upload box above!",
                    color = vyas.textDim,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp)
                )
            } else {
                uiState.items.forEachIndexed { idx, file ->
                    PdfNoteCard(
                        item = file,
                        index = idx,
                        onOpen = { onOpenPdf(file) },
                        onSave = {
                            onRequestSavePdf(file)
                            createPdfLauncher.launch(file.name)
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }

    val viewerItem = uiState.activePdfForViewer
    val viewerFile = uiState.activePdfFile
    if (viewerItem != null && viewerFile != null) {
        PdfReaderDialog(
            item = viewerItem,
            pdfFile = viewerFile,
            onDismiss = onClosePdfViewer
        )
    }
}

@Composable
private fun PdfNoteCard(
    item: PdfNoteItem,
    index: Int,
    onOpen: () -> Unit,
    onSave: () -> Unit
) {
    val vyas = LocalVyasColors.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onOpen)
            .testTag("pdf_card_$index"),
        shape = RoundedCornerShape(12.dp),
        color = vyas.cardBg,
        border = BorderStroke(1.dp, vyas.border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = "📄", fontSize = 24.sp)

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "👁️ Tap to Open & Read",
                    color = vyas.green,
                    fontSize = 11.sp
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(
                    modifier = Modifier
                        .heightIn(min = 40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onOpen)
                        .testTag("pdf_open_btn_$index"),
                    shape = RoundedCornerShape(8.dp),
                    color = vyas.cyan.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, vyas.cyan)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = "👁️ ${stringResource(R.string.btn_open)}",
                            color = vyas.cyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .heightIn(min = 40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onSave)
                        .testTag("pdf_save_btn_$index"),
                    shape = RoundedCornerShape(8.dp),
                    color = vyas.cyan.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, Color(0xFF3E4C63))
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = "📥 ${stringResource(R.string.btn_save)}",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
