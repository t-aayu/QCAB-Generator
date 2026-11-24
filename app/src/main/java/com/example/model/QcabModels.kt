package com.example.model

import android.net.Uri
import java.io.File
import java.util.UUID

enum class AppStep {
    SETUP,
    QUESTIONS,
    PREVIEW
}

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class ClearedType {
    TEXT,
    IMAGE
}

data class QuestionItem(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "",
    val marks: String = "10 Marks",
    val pagesText: String = "2",
    val imageUri: Uri? = null,
    val lastClearedType: ClearedType? = null,
    val savedText: String = "",
    val savedImageUri: Uri? = null
) {
    val pagesCount: Int
        get() = pagesText.toIntOrNull()?.coerceAtLeast(1) ?: 1

    val hasText: Boolean
        get() = text.trim().isNotEmpty()

    val hasImage: Boolean
        get() = imageUri != null

    val isEmpty: Boolean
        get() = !hasText && !hasImage
}

data class GeneratedPdfResult(
    val file: File,
    val fileName: String,
    val pageCount: Int,
    val questionCount: Int
)

data class QcabUiState(
    val step: AppStep = AppStep.SETUP,
    val testName: String = "",
    val numQuestionsText: String = "1",
    val setupError: String? = null,
    val questions: List<QuestionItem> = emptyList(),
    val isGenerating: Boolean = false,
    val pdfError: String? = null,
    val showEmptyWarningDialog: Boolean = false,
    val generatedPdf: GeneratedPdfResult? = null
) {
    val totalPages: Int
        get() = questions.sumOf { it.pagesCount }
}
