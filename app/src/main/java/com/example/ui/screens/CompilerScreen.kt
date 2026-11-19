package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import com.example.data.model.CodeSnippet
import com.example.data.repository.TimetableData
import com.example.ui.components.VyasNavHeader
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalVyasColors
import com.example.ui.theme.VyasBackground

private const val URL_ONECOMPILER_EMBED =
    "https://onecompiler.com/embed/c?listenToEvents=true&hideNew=true&hideTitle=true&theme=dark"
private const val URL_JDOODLE_EMBED =
    "https://www.jdoodle.com/embed/v0/2p9"
private const val URL_ONECOMPILER_FULL = "https://onecompiler.com/c"
private const val URL_ONLINEGDB_FULL = "https://www.onlinegdb.com/online_c_compiler"

@Composable
fun CompilerScreen(
    activeTab: Int,
    onSelectTab: (Int) -> Unit,
    onBackToUnits: () -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val vyas = LocalVyasColors.current
    val context = LocalContext.current
    @Suppress("DEPRECATION")
    val clipboard = LocalClipboardManager.current

    var previewSnippet by remember { mutableStateOf<CodeSnippet?>(null) }
    var tab1Progress by remember { mutableIntStateOf(0) }
    var tab2Progress by remember { mutableIntStateOf(0) }
    var tab2Initialized by remember { mutableStateOf(activeTab == 2) }

    if (activeTab == 2 && !tab2Initialized) {
        tab2Initialized = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VyasBackground)
            .padding(14.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 960.dp)
                .fillMaxSize()
        ) {
            VyasNavHeader(
                backLabel = stringResource(R.string.btn_back_pps_units),
                onBack = onBackToUnits,
                rightContent = {
                    Text(
                        text = stringResource(R.string.title_pps_lab),
                        color = vyas.green,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            )

            // Quick Snippets Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = stringResource(R.string.label_lab_snippets),
                    color = vyas.textDim,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                TimetableData.codeSnippets.forEach { snippet ->
                    Surface(
                        modifier = Modifier
                            .heightIn(min = 38.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable {
                                clipboard.setText(AnnotatedString(snippet.code))
                                onShowMessage("📋 ${snippet.label.uppercase()} copied! Paste in the editor & run.")
                                previewSnippet = snippet
                            }
                            .testTag("snippet_btn_${snippet.id}"),
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF162033),
                        border = BorderStroke(1.dp, Color(0xFF233350))
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = snippet.label,
                                color = Color(0xFFCBD5E1),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Dual Dark Compiler Container
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF101623),
                border = BorderStroke(1.5.dp, vyas.border)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Compiler Tab Header Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF090E17))
                            .border(0.5.dp, vyas.border)
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF060A12))
                                .border(1.dp, vyas.border, RoundedCornerShape(8.dp))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CompilerTabButton(
                                label = stringResource(R.string.tab_dark_compiler_1),
                                selected = activeTab == 1,
                                onClick = { onSelectTab(1) },
                                modifier = Modifier.testTag("tab_compiler_1")
                            )
                            CompilerTabButton(
                                label = stringResource(R.string.tab_dark_compiler_2),
                                selected = activeTab == 2,
                                onClick = { onSelectTab(2) },
                                modifier = Modifier.testTag("tab_compiler_2")
                            )
                        }

                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    val url = if (activeTab == 1) URL_ONECOMPILER_FULL else URL_ONLINEGDB_FULL
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                }
                                .testTag("fullscreen_compiler_button"),
                            shape = RoundedCornerShape(8.dp),
                            color = vyas.cyan.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, vyas.cyan)
                        ) {
                            Text(
                                text = stringResource(R.string.btn_fullscreen_tab),
                                color = vyas.cyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp)
                            )
                        }
                    }

                    val activeProgress = if (activeTab == 1) tab1Progress else tab2Progress
                    if (activeProgress in 1..99) {
                        LinearProgressIndicator(
                            progress = { activeProgress / 100f },
                            modifier = Modifier.fillMaxWidth().height(2.dp),
                            color = vyas.cyan,
                            trackColor = Color(0xFF090E17)
                        )
                    }

                    // WebView Host
                    Box(modifier = Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            CompilerWebView(
                                url = URL_ONECOMPILER_EMBED,
                                backgroundHex = 0xFF141B26.toInt(),
                                onProgressChanged = { tab1Progress = it },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        if (tab2Initialized && activeTab == 2) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFF111622))
                            ) {
                                CompilerWebView(
                                    url = URL_JDOODLE_EMBED,
                                    backgroundHex = 0xFF111622.toInt(),
                                    onProgressChanged = { tab2Progress = it },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    previewSnippet?.let { snippet ->
        AlertDialog(
            onDismissRequest = { previewSnippet = null },
            containerColor = vyas.cardBg,
            titleContentColor = vyas.cyan,
            textContentColor = Color.White,
            title = {
                Text(
                    text = "${snippet.label} (Copied to Clipboard)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF080C14))
                        .border(1.dp, vyas.border, RoundedCornerShape(8.dp))
                        .verticalScroll(rememberScrollState())
                        .padding(12.dp)
                ) {
                    Text(
                        text = snippet.code,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 12.sp,
                        color = Color(0xFF7EE787)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { previewSnippet = null }) {
                    Text("Got it", color = vyas.cyan, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun CompilerTabButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val vyas = LocalVyasColors.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) vyas.cyan else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) Color.Black else vyas.textDim,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun CompilerWebView(
    url: String,
    backgroundHex: Int,
    onProgressChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val webView = remember(url) {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(backgroundHex)
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
            }
            webViewClient = WebViewClient()
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    onProgressChanged(newProgress)
                }
            }
            loadUrl(url)
        }
    }

    DisposableEffect(webView) {
        onDispose {
            webView.stopLoading()
            webView.destroy()
        }
    }

    AndroidView(
        factory = { webView },
        modifier = modifier
    )
}
