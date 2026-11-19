package com.example.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.example.R
import com.example.data.model.PdfNoteItem
import com.example.ui.theme.LocalVyasColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun PdfReaderDialog(
    item: PdfNoteItem,
    pdfFile: File,
    onDismiss: () -> Unit
) {
    val vyas = LocalVyasColors.current
    val context = LocalContext.current
    var pages by remember(pdfFile) { mutableStateOf<List<Bitmap>>(emptyList()) }
    var isLoading by remember(pdfFile) { mutableStateOf(true) }
    var loadError by remember(pdfFile) { mutableStateOf<String?>(null) }

    LaunchedEffect(pdfFile) {
        isLoading = true
        loadError = null
        val rendered = withContext(Dispatchers.IO) {
            runCatching {
                val bitmaps = mutableListOf<Bitmap>()
                ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
                    PdfRenderer(pfd).use { renderer ->
                        val count = renderer.pageCount
                        for (i in 0 until count) {
                            renderer.openPage(i).use { page ->
                                val targetWidth = 1080
                                val scale = targetWidth.toFloat() / page.width.coerceAtLeast(1)
                                val targetHeight = (page.height * scale).toInt().coerceAtLeast(1)
                                val bmp = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
                                Canvas(bmp).drawColor(android.graphics.Color.WHITE)
                                page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                                bitmaps.add(bmp)
                            }
                        }
                    }
                }
                bitmaps.toList()
            }
        }
        rendered.onSuccess {
            pages = it
            isLoading = false
        }.onFailure { err ->
            loadError = err.message ?: "Unable to render PDF document."
            isLoading = false
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.88f))
                .padding(10.dp)
                .testTag("pdf_viewer_modal")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, vyas.border, RoundedCornerShape(10.dp))
                    .background(Color(0xFF0A0D14))
            ) {
                // Modal Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF101623))
                        .border(0.5.dp, vyas.border)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.name,
                        color = vyas.cyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    try {
                                        val contentUri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            pdfFile
                                        )
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            setDataAndType(contentUri, "application/pdf")
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(intent)
                                    } catch (_: ActivityNotFoundException) {
                                        Toast.makeText(
                                            context,
                                            "No external PDF viewer installed.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                                .testTag("pdf_open_external_button"),
                            shape = RoundedCornerShape(6.dp),
                            color = vyas.cyan.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, vyas.cyan)
                        ) {
                            Text(
                                text = stringResource(R.string.btn_open_external),
                                color = vyas.cyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                            )
                        }

                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable(onClick = onDismiss)
                                .testTag("pdf_close_button"),
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFF4560)
                        ) {
                            Text(
                                text = stringResource(R.string.btn_close),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }

                // PDF Content Body
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0E131D)),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isLoading -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                CircularProgressIndicator(color = vyas.cyan)
                                Text(
                                    text = "Rendering PDF...",
                                    color = vyas.textDim,
                                    fontSize = 13.sp
                                )
                            }
                        }
                        loadError != null -> {
                            Text(
                                text = loadError ?: "Error loading PDF",
                                color = Color(0xFFFF4560),
                                fontSize = 13.sp,
                                modifier = Modifier.padding(20.dp)
                            )
                        }
                        else -> {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                itemsIndexed(pages) { idx, bitmap ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color.White,
                                            shadowElevation = 4.dp,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Image(
                                                bitmap = bitmap.asImageBitmap(),
                                                contentDescription = "${item.name} page ${idx + 1}",
                                                contentScale = ContentScale.FillWidth,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .aspectRatio(bitmap.width.toFloat() / bitmap.height.coerceAtLeast(1))
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Page ${idx + 1} of ${pages.size}",
                                            color = vyas.textDim,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
