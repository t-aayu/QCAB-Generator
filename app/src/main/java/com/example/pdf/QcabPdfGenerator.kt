package com.example.pdf

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.model.GeneratedPdfResult
import com.example.model.QuestionItem
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

object QcabPdfGenerator {

    private const val MM_TO_PT = 72f / 25.4f
    private const val PAGE_WIDTH_MM = 210f
    private const val PAGE_HEIGHT_MM = 297f
    private const val LEFT_MARGIN_MM = 25f
    private const val TOP_MARGIN_MM = 15f
    private const val RIGHT_MARGIN_MM = PAGE_WIDTH_MM - 25f
    private const val BOTTOM_MARGIN_MM = PAGE_HEIGHT_MM - 15f

    private fun mm(value: Float): Float = value * MM_TO_PT

    fun buildSafeFileName(testName: String): String {
        val base = testName.trim().ifEmpty { "qcab_test" }
        val sanitized = base.replace(Regex("[^a-zA-Z0-9]"), "_").lowercase()
        val normalized = sanitized.replace(Regex("_+"), "_").trim('_').ifEmpty { "qcab_test" }
        return "$normalized.pdf"
    }

    fun generate(
        context: Context,
        testName: String,
        questions: List<QuestionItem>
    ): GeneratedPdfResult {
        val pageWidthPt = mm(PAGE_WIDTH_MM).roundToInt()   // 595 pt (A4)
        val pageHeightPt = mm(PAGE_HEIGHT_MM).roundToInt() // 842 pt (A4)

        val ptSerifTypeface = ResourcesCompat.getFont(context, R.font.pt_serif) ?: Typeface.SERIF
        val serifRegular = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        val serifItalic = Typeface.create(Typeface.SERIF, Typeface.ITALIC)

        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            strokeWidth = mm(0.3f)
            style = Paint.Style.STROKE
        }

        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            typeface = serifItalic
            textSize = 12f
        }

        val marginWarningPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            typeface = serifRegular
            textSize = 9f
        }

        val questionNumberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            typeface = ptSerifTypeface
            textSize = 12f
            textAlign = Paint.Align.CENTER
        }

        val questionTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            typeface = ptSerifTypeface
            textSize = 12f
        }

        val marksPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            typeface = ptSerifTypeface
            textSize = 12f
            textAlign = Paint.Align.RIGHT
        }

        val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        val warningLayout = buildStaticLayout(
            text = "Candidates must not write on this margin",
            paint = marginWarningPaint,
            widthPx = mm(20f).roundToInt(),
            justified = false
        )

        val document = PdfDocument()
        var globalPageNumber = 1

        try {
            questions.forEachIndexed { qIndex, question ->
                val pageCountForQuestion = question.pagesCount

                for (pageOffset in 0 until pageCountForQuestion) {
                    val pageInfo = PdfDocument.PageInfo.Builder(
                        pageWidthPt,
                        pageHeightPt,
                        globalPageNumber
                    ).create()
                    val page = document.startPage(pageInfo)
                    val canvas = page.canvas

                    // Crisp white page background
                    canvas.drawColor(Color.WHITE)

                    // Standard UPSC QCAB left & right margin rules, footer stamp, and right-margin notice
                    drawBookletFrame(
                        canvas = canvas,
                        footerMark = testName,
                        linePaint = linePaint,
                        footerPaint = footerPaint,
                        warningLayout = warningLayout,
                        warningPaint = marginWarningPaint
                    )

                    if (pageOffset == 0) {
                        drawQuestionHeaderAndBody(
                            context = context,
                            canvas = canvas,
                            qIndex = qIndex,
                            question = question,
                            questionNumberPaint = questionNumberPaint,
                            questionTextPaint = questionTextPaint,
                            marksPaint = marksPaint,
                            bitmapPaint = bitmapPaint
                        )
                    }

                    document.finishPage(page)
                    globalPageNumber++
                }
            }

            val outputDir = File(context.cacheDir, "qcab").apply { mkdirs() }
            val fileName = buildSafeFileName(testName)
            val outputFile = File(outputDir, fileName)
            FileOutputStream(outputFile).use { stream ->
                document.writeTo(stream)
            }

            return GeneratedPdfResult(
                file = outputFile,
                fileName = fileName,
                pageCount = globalPageNumber - 1,
                questionCount = questions.size
            )
        } finally {
            document.close()
        }
    }

    private fun drawBookletFrame(
        canvas: Canvas,
        footerMark: String,
        linePaint: Paint,
        footerPaint: Paint,
        warningLayout: StaticLayout,
        warningPaint: TextPaint
    ) {
        val leftX = mm(LEFT_MARGIN_MM)
        val topY = mm(TOP_MARGIN_MM)
        val rightX = mm(RIGHT_MARGIN_MM)
        val bottomY = mm(BOTTOM_MARGIN_MM)

        canvas.drawLine(leftX, topY, leftX, bottomY, linePaint)
        canvas.drawLine(rightX, topY, rightX, bottomY, linePaint)

        val trimmedFooter = footerMark.trim()
        if (trimmedFooter.isNotEmpty()) {
            canvas.drawText(
                trimmedFooter,
                mm(LEFT_MARGIN_MM - 10f),
                mm(BOTTOM_MARGIN_MM + 5f),
                footerPaint
            )
        }

        // Align first line baseline of margin notice with topMargin + 5mm
        val warningTop = mm(TOP_MARGIN_MM + 5f) + warningPaint.fontMetrics.ascent
        canvas.save()
        canvas.translate(mm(RIGHT_MARGIN_MM + 2f), warningTop)
        warningLayout.draw(canvas)
        canvas.restore()
    }

    private fun drawQuestionHeaderAndBody(
        context: Context,
        canvas: Canvas,
        qIndex: Int,
        question: QuestionItem,
        questionNumberPaint: Paint,
        questionTextPaint: TextPaint,
        marksPaint: Paint,
        bitmapPaint: Paint
    ) {
        val baselineStartY = mm(TOP_MARGIN_MM + 5f)
        canvas.drawText(
            "Q. ${qIndex + 1}",
            mm(LEFT_MARGIN_MM - 15f),
            baselineStartY,
            questionNumberPaint
        )

        var currentY = baselineStartY
        val qTextX = mm(LEFT_MARGIN_MM + 2f)
        val qTextWidth = mm(RIGHT_MARGIN_MM - LEFT_MARGIN_MM - 4f)

        val imageUri = question.imageUri
        if (imageUri != null) {
            val bitmap = loadScaledBitmap(context, imageUri, maxDimensionPx = 1600)
            if (bitmap != null) {
                try {
                    val imgRatio = bitmap.width.toFloat() / max(1, bitmap.height).toFloat()
                    var renderWidth = qTextWidth
                    var renderHeight = renderWidth / imgRatio

                    val imageTopY = currentY + questionTextPaint.fontMetrics.ascent
                    val maxAvailableHeight = mm(BOTTOM_MARGIN_MM) - imageTopY - mm(15f)
                    if (renderHeight > maxAvailableHeight && maxAvailableHeight > 0f) {
                        renderHeight = maxAvailableHeight
                        renderWidth = renderHeight * imgRatio
                    }

                    val destRect = RectF(
                        qTextX,
                        imageTopY,
                        qTextX + renderWidth,
                        imageTopY + renderHeight
                    )
                    canvas.drawBitmap(bitmap, null, destRect, bitmapPaint)
                    currentY = imageTopY + renderHeight + mm(5f)
                } finally {
                    bitmap.recycle()
                }
            }
        } else if (question.text.trim().isNotEmpty()) {
            val textLayout = buildStaticLayout(
                text = question.text.trim(),
                paint = questionTextPaint,
                widthPx = qTextWidth.roundToInt(),
                justified = true
            )
            val textTopY = currentY + questionTextPaint.fontMetrics.ascent
            canvas.save()
            canvas.translate(qTextX, textTopY)
            textLayout.draw(canvas)
            canvas.restore()
            currentY = textTopY + textLayout.height + mm(5f)
        }

        val marksTrimmed = question.marks.trim()
        if (marksTrimmed.isNotEmpty()) {
            canvas.drawText(
                "[$marksTrimmed]",
                mm(RIGHT_MARGIN_MM - 2f),
                currentY,
                marksPaint
            )
        }
    }

    private fun buildStaticLayout(
        text: String,
        paint: TextPaint,
        widthPx: Int,
        justified: Boolean
    ): StaticLayout {
        val builder = StaticLayout.Builder.obtain(text, 0, text.length, paint, max(1, widthPx))
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1.15f)
            .setIncludePad(false)

        if (justified && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD)
        }
        return builder.build()
    }

    private fun loadScaledBitmap(
        context: Context,
        uri: Uri,
        maxDimensionPx: Int
    ): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    val longest = max(info.size.width, info.size.height)
                    if (longest > maxDimensionPx) {
                        val scale = maxDimensionPx.toFloat() / longest.toFloat()
                        decoder.setTargetSize(
                            max(1, (info.size.width * scale).roundToInt()),
                            max(1, (info.size.height * scale).roundToInt())
                        )
                    }
                }
            } else {
                val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, boundsOptions)
                }
                val longest = max(boundsOptions.outWidth, boundsOptions.outHeight)
                var sampleSize = 1
                while (longest / sampleSize > maxDimensionPx) {
                    sampleSize *= 2
                }
                val decodeOptions = BitmapFactory.Options().apply {
                    inSampleSize = sampleSize
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, decodeOptions)
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    fun saveToDownloads(context: Context, sourceFile: File, fileName: String): Result<String> {
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    ?: error("Failed to create Downloads entry")
                try {
                    resolver.openOutputStream(uri)?.use { out ->
                        FileInputStream(sourceFile).use { input ->
                            input.copyTo(out)
                        }
                    } ?: error("Could not open Downloads output stream")
                    values.clear()
                    values.put(MediaStore.Downloads.IS_PENDING, 0)
                    resolver.update(uri, values, null, null)
                } catch (e: Exception) {
                    resolver.delete(uri, null, null)
                    throw e
                }
                fileName
            } else {
                error("USE_SAF_PICKER")
            }
        }
    }

    fun copyPdfToUri(context: Context, sourceFile: File, destinationUri: Uri): Result<Unit> {
        return runCatching {
            context.contentResolver.openOutputStream(destinationUri)?.use { out ->
                FileInputStream(sourceFile).use { input ->
                    input.copyTo(out)
                }
            } ?: error("Unable to write to selected destination")
        }
    }

    fun createShareIntent(context: Context, pdfFile: File, chooserTitle: String): Intent {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return Intent.createChooser(sendIntent, chooserTitle)
    }
}
