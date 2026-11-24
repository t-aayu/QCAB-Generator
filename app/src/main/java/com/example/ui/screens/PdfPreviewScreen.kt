package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.GeneratedPdfResult
import com.example.model.ThemeMode
import com.example.pdf.QcabPdfGenerator
import com.example.ui.components.ThemeModeMenuButton
import com.example.ui.theme.LocalQcabColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private class PdfRenderSession(file: File) {
    private val fileDescriptor: ParcelFileDescriptor =
        ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    private val renderer: PdfRenderer = PdfRenderer(fileDescriptor)
    private val lock = Any()
    private var isClosed = false

    val pageCount: Int
        get() = synchronized(lock) { if (isClosed) 0 else renderer.pageCount }

    suspend fun renderPage(pageIndex: Int, targetWidthPx: Int = 960): Bitmap? {
        return withContext(Dispatchers.IO) {
            synchronized(lock) {
                if (isClosed || pageIndex !in 0 until renderer.pageCount) return@synchronized null
                runCatching {
                    renderer.openPage(pageIndex).use { page ->
                        val scale = targetWidthPx.toFloat() / page.width.coerceAtLeast(1).toFloat()
                        val width = targetWidthPx
                        val height = (page.height * scale).toInt().coerceAtLeast(1)
                        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(bitmap)
                        canvas.drawColor(Color.WHITE)
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        bitmap
                    }
                }.getOrNull()
            }
        }
    }

    fun close() {
        synchronized(lock) {
            if (isClosed) return
            isClosed = true
            runCatching { renderer.close() }
            runCatching { fileDescriptor.close() }
        }
    }
}

@Composable
fun PdfPreviewScreen(
    pdfResult: GeneratedPdfResult,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val extendedColors = LocalQcabColors.current

    val renderSession = remember(pdfResult.file.absolutePath, pdfResult.file.lastModified()) {
        runCatching { PdfRenderSession(pdfResult.file) }.getOrNull()
    }

    DisposableEffect(renderSession) {
        onDispose {
            renderSession?.close()
        }
    }

    val savedLocationMsg = stringResource(R.string.msg_saved_location)
    val chooserTitle = stringResource(R.string.share_chooser_title)

    // Zero-permission SAF CreateDocument launcher for Save As (and Downloads fallback on API 24-28)
    val saveDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val result = withContext(Dispatchers.IO) {
                    QcabPdfGenerator.copyPdfToUri(context, pdfResult.file, uri)
                }
                result.onSuccess {
                    snackbarHostState.showSnackbar(savedLocationMsg)
                }.onFailure { err ->
                    snackbarHostState.showSnackbar(
                        context.getString(R.string.msg_save_error, err.message ?: "Unknown error")
                    )
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                shadowElevation = 3.dp,
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("preview_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp)
                    ) {
                        Text(
                            text = pdfResult.fileName,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${pdfResult.questionCount} ${if (pdfResult.questionCount == 1) "Question" else "Questions"} • ${pdfResult.pageCount} A4 ${if (pdfResult.pageCount == 1) "Page" else "Pages"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    ThemeModeMenuButton(
                        selectedMode = themeMode,
                        onSelectMode = onThemeModeChange
                    )
                }
            }
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
                shadowElevation = 10.dp,
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 680.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    scope.launch {
                                        val result = withContext(Dispatchers.IO) {
                                            QcabPdfGenerator.saveToDownloads(
                                                context = context,
                                                sourceFile = pdfResult.file,
                                                fileName = pdfResult.fileName
                                            )
                                        }
                                        result.onSuccess { savedName ->
                                            snackbarHostState.showSnackbar(
                                                context.getString(R.string.msg_saved_downloads, savedName)
                                            )
                                        }.onFailure { err ->
                                            snackbarHostState.showSnackbar(
                                                context.getString(
                                                    R.string.msg_save_error,
                                                    err.message ?: "Unknown error"
                                                )
                                            )
                                        }
                                    }
                                } else {
                                    saveDocumentLauncher.launch(pdfResult.fileName)
                                }
                            },
                            shape = MaterialTheme.shapes.medium,
                            contentPadding = PaddingValues(vertical = 14.dp, horizontal = 20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = extendedColors.exportAction,
                                contentColor = extendedColors.onExportAction
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("save_downloads_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Download,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.btn_save_downloads),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { saveDocumentLauncher.launch(pdfResult.fileName) },
                                shape = MaterialTheme.shapes.medium,
                                contentPadding = PaddingValues(vertical = 12.dp, horizontal = 14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("save_as_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.FolderOpen,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.btn_save_as),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }

                            FilledTonalButton(
                                onClick = {
                                    runCatching {
                                        val shareIntent = QcabPdfGenerator.createShareIntent(
                                            context = context,
                                            pdfFile = pdfResult.file,
                                            chooserTitle = chooserTitle
                                        )
                                        context.startActivity(shareIntent)
                                    }.onFailure { err ->
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                context.getString(
                                                    R.string.msg_share_error,
                                                    err.message ?: "Unknown error"
                                                )
                                            )
                                        }
                                    }
                                },
                                shape = MaterialTheme.shapes.medium,
                                contentPadding = PaddingValues(vertical = 12.dp, horizontal = 14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("share_pdf_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Share,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.btn_share_pdf),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            val totalPages = renderSession?.pageCount ?: 0
            if (totalPages <= 0) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Unable to preview PDF pages.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 680.dp)
                        .testTag("pdf_pages_list"),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    items(count = totalPages, key = { pageIdx -> pageIdx }) { pageIndex ->
                        PdfPageCard(
                            session = renderSession!!,
                            pageIndex = pageIndex,
                            totalPages = totalPages
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PdfPageCard(
    session: PdfRenderSession,
    pageIndex: Int,
    totalPages: Int
) {
    val pageBitmapState = produceState<Bitmap?>(initialValue = null, session, pageIndex) {
        value = session.renderPage(pageIndex)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = "Page ${pageIndex + 1} of $totalPages",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            Text(
                text = "A4 • 210 × 297 mm",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(210f / 297f),
            shape = RoundedCornerShape(8.dp),
            color = LocalQcabColors.current.paperSheet,
            shadowElevation = 4.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            val bitmap = pageBitmapState.value
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "QCAB Booklet Page ${pageIndex + 1}",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(LocalQcabColors.current.paperSheet),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 2.5.dp
                    )
                }
            }
        }
    }
}
