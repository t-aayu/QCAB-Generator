package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ThemePreferences
import com.example.model.AppStep
import com.example.model.ClearedType
import com.example.model.QcabUiState
import com.example.model.QuestionItem
import com.example.model.ThemeMode
import com.example.pdf.QcabPdfGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QcabViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val MIN_QUESTIONS = 1
        const val MAX_QUESTIONS = 20
    }

    private val themePreferences = ThemePreferences(application.applicationContext)

    val themeMode: StateFlow<ThemeMode> = themePreferences.themeModeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ThemeMode.SYSTEM
    )

    private val _uiState = MutableStateFlow(QcabUiState())
    val uiState: StateFlow<QcabUiState> = _uiState.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            themePreferences.setThemeMode(mode)
        }
    }

    fun onTestNameChange(newName: String) {
        _uiState.update { it.copy(testName = newName) }
    }

    fun onNumQuestionsChange(rawInput: String) {
        val filtered = rawInput.filter { it.isDigit() }.take(2)
        _uiState.update { it.copy(numQuestionsText = filtered, setupError = null) }
    }

    fun adjustNumQuestions(delta: Int) {
        val current = _uiState.value.numQuestionsText.toIntOrNull() ?: MIN_QUESTIONS
        val next = (current + delta).coerceIn(MIN_QUESTIONS, MAX_QUESTIONS)
        _uiState.update { it.copy(numQuestionsText = next.toString(), setupError = null) }
    }

    fun selectPresetQuestions(count: Int) {
        val clamped = count.coerceIn(MIN_QUESTIONS, MAX_QUESTIONS)
        _uiState.update { it.copy(numQuestionsText = clamped.toString(), setupError = null) }
    }

    fun continueToQuestions(minError: String, maxError: String) {
        val targetCount = _uiState.value.numQuestionsText.toIntOrNull()
        if (targetCount == null || targetCount < MIN_QUESTIONS) {
            _uiState.update { it.copy(setupError = minError) }
            return
        }
        if (targetCount > MAX_QUESTIONS) {
            _uiState.update { it.copy(setupError = maxError) }
            return
        }

        _uiState.update { state ->
            val currentQuestions = state.questions
            val updatedQuestions = when {
                currentQuestions.isEmpty() -> List(targetCount) { QuestionItem() }
                targetCount > currentQuestions.size -> {
                    currentQuestions + List(targetCount - currentQuestions.size) { QuestionItem() }
                }
                targetCount < currentQuestions.size -> currentQuestions.take(targetCount)
                else -> currentQuestions
            }
            state.copy(
                step = AppStep.QUESTIONS,
                setupError = null,
                pdfError = null,
                questions = updatedQuestions
            )
        }
    }

    fun navigateBackToSetup() {
        _uiState.update {
            it.copy(
                step = AppStep.SETUP,
                numQuestionsText = it.questions.size.coerceIn(MIN_QUESTIONS, MAX_QUESTIONS).toString(),
                setupError = null,
                pdfError = null
            )
        }
    }

    fun navigateBackToQuestions() {
        _uiState.update { it.copy(step = AppStep.QUESTIONS) }
    }

    fun addQuestion() {
        _uiState.update { state ->
            if (state.questions.size >= MAX_QUESTIONS) return@update state
            val updated = state.questions + QuestionItem()
            state.copy(
                questions = updated,
                numQuestionsText = updated.size.toString()
            )
        }
    }

    fun removeQuestion(index: Int) {
        _uiState.update { state ->
            if (state.questions.size <= MIN_QUESTIONS || index !in state.questions.indices) {
                return@update state
            }
            val updated = state.questions.toMutableList().apply { removeAt(index) }
            state.copy(
                questions = updated,
                numQuestionsText = updated.size.toString()
            )
        }
    }

    fun updateQuestionText(index: Int, newText: String) {
        updateQuestionAt(index) { q ->
            q.copy(text = newText, lastClearedType = null)
        }
    }

    fun attachImage(index: Int, uri: Uri) {
        updateQuestionAt(index) { q ->
            q.copy(imageUri = uri, lastClearedType = null)
        }
    }

    fun clearText(index: Int) {
        updateQuestionAt(index) { q ->
            q.copy(
                savedText = q.text,
                text = "",
                lastClearedType = ClearedType.TEXT
            )
        }
    }

    fun clearImage(index: Int) {
        updateQuestionAt(index) { q ->
            q.copy(
                savedImageUri = q.imageUri,
                imageUri = null,
                lastClearedType = ClearedType.IMAGE
            )
        }
    }

    fun undoClear(index: Int) {
        updateQuestionAt(index) { q ->
            when (q.lastClearedType) {
                ClearedType.TEXT -> q.copy(text = q.savedText, lastClearedType = null)
                ClearedType.IMAGE -> q.copy(imageUri = q.savedImageUri, lastClearedType = null)
                null -> q
            }
        }
    }

    fun updateMarks(index: Int, marks: String) {
        updateQuestionAt(index) { q -> q.copy(marks = marks) }
    }

    fun updatePagesText(index: Int, rawInput: String) {
        val filtered = rawInput.filter { it.isDigit() }.take(2)
        updateQuestionAt(index) { q -> q.copy(pagesText = filtered) }
    }

    fun adjustPages(index: Int, delta: Int) {
        updateQuestionAt(index) { q ->
            val next = (q.pagesCount + delta).coerceIn(1, 20)
            q.copy(pagesText = next.toString())
        }
    }

    fun requestGeneratePdf(context: Context) {
        val state = _uiState.value
        if (state.questions.isEmpty() || state.isGenerating) return

        val hasEmpty = state.questions.any { it.isEmpty }
        if (hasEmpty) {
            _uiState.update { it.copy(showEmptyWarningDialog = true) }
        } else {
            executeGeneratePdf(context)
        }
    }

    fun dismissEmptyWarning() {
        _uiState.update { it.copy(showEmptyWarningDialog = false) }
    }

    fun proceedGeneratePdf(context: Context) {
        _uiState.update { it.copy(showEmptyWarningDialog = false) }
        executeGeneratePdf(context)
    }

    private fun executeGeneratePdf(context: Context) {
        val appContext = context.applicationContext
        val currentTestName = _uiState.value.testName
        val currentQuestions = _uiState.value.questions

        _uiState.update { it.copy(isGenerating = true, pdfError = null) }

        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    QcabPdfGenerator.generate(
                        context = appContext,
                        testName = currentTestName,
                        questions = currentQuestions
                    )
                }
            }

            result.onSuccess { generated ->
                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        pdfError = null,
                        generatedPdf = generated,
                        step = AppStep.PREVIEW
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        pdfError = "An error occurred while generating the PDF: ${throwable.message ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    private fun updateQuestionAt(index: Int, transform: (QuestionItem) -> QuestionItem) {
        _uiState.update { state ->
            if (index !in state.questions.indices) return@update state
            val updated = state.questions.toMutableList()
            updated[index] = transform(updated[index])
            state.copy(questions = updated, pdfError = null)
        }
    }
}
