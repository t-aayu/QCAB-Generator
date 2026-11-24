package com.example

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.AppStep
import com.example.model.ThemeMode
import com.example.ui.screens.PdfPreviewScreen
import com.example.ui.screens.QuestionsScreen
import com.example.ui.screens.SetupScreen
import com.example.ui.theme.QcabTheme
import com.example.viewmodel.QcabViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: QcabViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()
            val useDarkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> systemDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            // Keep status bar & navigation bar icons legible across Setup (dark hero) and themed surfaces
            val useDarkStatusBar = uiState.step == AppStep.SETUP || useDarkTheme
            DisposableEffect(useDarkStatusBar, useDarkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = if (useDarkStatusBar) {
                        SystemBarStyle.dark(AndroidColor.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
                    },
                    navigationBarStyle = if (useDarkTheme) {
                        SystemBarStyle.dark(AndroidColor.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
                    }
                )
                onDispose {}
            }

            QcabTheme(darkTheme = useDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    QcabApp(
                        viewModel = viewModel,
                        themeMode = themeMode,
                        onThemeModeChange = viewModel::setThemeMode
                    )
                }
            }
        }
    }
}

@Composable
fun QcabApp(
    viewModel: QcabViewModel,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val minQuestionsError = stringResource(R.string.error_min_questions)
    val maxQuestionsError = stringResource(R.string.error_max_questions)

    AnimatedContent(
        targetState = uiState.step,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "qcab_step_transition"
    ) { currentStep ->
        when (currentStep) {
            AppStep.SETUP -> {
                SetupScreen(
                    uiState = uiState,
                    themeMode = themeMode,
                    onThemeModeChange = onThemeModeChange,
                    onTestNameChange = viewModel::onTestNameChange,
                    onNumQuestionsChange = viewModel::onNumQuestionsChange,
                    onAdjustNumQuestions = viewModel::adjustNumQuestions,
                    onSelectPreset = viewModel::selectPresetQuestions,
                    onContinue = {
                        viewModel.continueToQuestions(
                            minError = minQuestionsError,
                            maxError = maxQuestionsError
                        )
                    }
                )
            }

            AppStep.QUESTIONS -> {
                QuestionsScreen(
                    uiState = uiState,
                    themeMode = themeMode,
                    onThemeModeChange = onThemeModeChange,
                    onBackToSetup = viewModel::navigateBackToSetup,
                    onAddQuestion = viewModel::addQuestion,
                    onRemoveQuestion = viewModel::removeQuestion,
                    onTextChange = viewModel::updateQuestionText,
                    onAttachImage = viewModel::attachImage,
                    onClearText = viewModel::clearText,
                    onClearImage = viewModel::clearImage,
                    onUndoClear = viewModel::undoClear,
                    onMarksChange = viewModel::updateMarks,
                    onPagesChange = viewModel::updatePagesText,
                    onAdjustPages = viewModel::adjustPages,
                    onGeneratePdfClick = { viewModel.requestGeneratePdf(context) },
                    onDismissEmptyWarning = viewModel::dismissEmptyWarning,
                    onProceedEmptyWarning = { viewModel.proceedGeneratePdf(context) }
                )
            }

            AppStep.PREVIEW -> {
                val pdfResult = uiState.generatedPdf
                if (pdfResult != null) {
                    PdfPreviewScreen(
                        pdfResult = pdfResult,
                        themeMode = themeMode,
                        onThemeModeChange = onThemeModeChange,
                        onBack = viewModel::navigateBackToQuestions
                    )
                } else {
                    LaunchedEffect(Unit) {
                        viewModel.navigateBackToQuestions()
                    }
                }
            }
        }
    }
}
