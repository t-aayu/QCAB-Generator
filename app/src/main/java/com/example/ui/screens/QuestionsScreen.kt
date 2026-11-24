package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.QcabUiState
import com.example.model.ThemeMode
import com.example.ui.components.QuestionCard
import com.example.ui.components.ThemeModeMenuButton
import com.example.ui.theme.LocalQcabColors
import com.example.viewmodel.QcabViewModel

@Composable
fun QuestionsScreen(
    uiState: QcabUiState,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    onBackToSetup: () -> Unit,
    onAddQuestion: () -> Unit,
    onRemoveQuestion: (Int) -> Unit,
    onTextChange: (Int, String) -> Unit,
    onAttachImage: (Int, Uri) -> Unit,
    onClearText: (Int) -> Unit,
    onClearImage: (Int) -> Unit,
    onUndoClear: (Int) -> Unit,
    onMarksChange: (Int, String) -> Unit,
    onPagesChange: (Int, String) -> Unit,
    onAdjustPages: (Int, Int) -> Unit,
    onGeneratePdfClick: () -> Unit,
    onDismissEmptyWarning: () -> Unit,
    onProceedEmptyWarning: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackToSetup)

    val extendedColors = LocalQcabColors.current
    var pendingPhotoQuestionIndex by rememberSaveable { mutableIntStateOf(-1) }

    // Zero-permission Android Photo Picker (Play Policy compliant)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null && pendingPhotoQuestionIndex >= 0) {
            onAttachImage(pendingPhotoQuestionIndex, uri)
        }
        pendingPhotoQuestionIndex = -1
    }

    val isMaxQuestions = uiState.questions.size >= QcabViewModel.MAX_QUESTIONS

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Sticky Top Status Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            shadowElevation = 3.dp,
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 680.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = onBackToSetup,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("back_to_setup_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.btn_back_setup),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = uiState.testName.trim()
                                    .ifEmpty { stringResource(R.string.untitled_test) },
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Text(
                                text = "|",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = "${uiState.questions.size} Qs • ${uiState.totalPages}p",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }

                    ThemeModeMenuButton(
                        selectedMode = themeMode,
                        onSelectMode = onThemeModeChange
                    )
                }
            }
        }

        // Scrollable Question Cards List
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 680.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                itemsIndexed(
                    items = uiState.questions,
                    key = { _, item -> item.id }
                ) { index, question ->
                    QuestionCard(
                        index = index,
                        question = question,
                        canRemove = uiState.questions.size > QcabViewModel.MIN_QUESTIONS,
                        onTextChange = { onTextChange(index, it) },
                        onPickImage = {
                            pendingPhotoQuestionIndex = index
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onClearText = { onClearText(index) },
                        onClearImage = { onClearImage(index) },
                        onUndoClear = { onUndoClear(index) },
                        onMarksChange = { onMarksChange(index, it) },
                        onPagesChange = { onPagesChange(index, it) },
                        onAdjustPages = { delta -> onAdjustPages(index, delta) },
                        onRemove = { onRemoveQuestion(index) }
                    )
                }

                // Dynamic Add Another Question dashed button
                item(key = "add_question_footer") {
                    val dashColor = if (isMaxQuestions) {
                        MaterialTheme.colorScheme.outlineVariant
                    } else {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
                    }

                    Surface(
                        onClick = onAddQuestion,
                        enabled = !isMaxQuestions,
                        shape = MaterialTheme.shapes.large,
                        color = if (isMaxQuestions) {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        } else {
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.large)
                            .drawBehind {
                                drawRoundRect(
                                    color = dashColor,
                                    cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx()),
                                    style = Stroke(
                                        width = 1.8.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 12f), 0f)
                                    )
                                )
                            }
                            .testTag("add_question_button")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp, horizontal = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = null,
                                tint = if (isMaxQuestions) {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                } else {
                                    MaterialTheme.colorScheme.primary
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isMaxQuestions) {
                                    stringResource(R.string.btn_max_questions_reached)
                                } else {
                                    stringResource(R.string.btn_add_question)
                                },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isMaxQuestions) {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                } else {
                                    MaterialTheme.colorScheme.primary
                                }
                            )
                        }
                    }
                }

                item(key = "bottom_spacer") {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        // Sticky Bottom Export Bar
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
                    .imePadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 680.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AnimatedVisibility(visible = uiState.pdfError != null) {
                        uiState.pdfError?.let { errorMsg ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.errorContainer,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("pdf_error_banner")
                            ) {
                                Text(
                                    text = errorMsg,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }

                    Button(
                        onClick = onGeneratePdfClick,
                        enabled = !uiState.isGenerating && uiState.questions.isNotEmpty(),
                        shape = MaterialTheme.shapes.medium,
                        contentPadding = PaddingValues(vertical = 15.dp, horizontal = 24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = extendedColors.exportAction,
                            contentColor = extendedColors.onExportAction
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("generate_pdf_button")
                    ) {
                        if (uiState.isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.5.dp,
                                color = extendedColors.onExportAction
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = stringResource(R.string.btn_building_pdf),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.PictureAsPdf,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = stringResource(R.string.btn_generate_pdf),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // Empty Questions Confirmation Dialog
    if (uiState.showEmptyWarningDialog) {
        AlertDialog(
            onDismissRequest = onDismissEmptyWarning,
            icon = {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = extendedColors.cautionContainer,
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.WarningAmber,
                            contentDescription = null,
                            tint = extendedColors.caution,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            },
            title = {
                Text(
                    text = stringResource(R.string.dialog_empty_title),
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.dialog_empty_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            dismissButton = {
                TextButton(
                    onClick = onDismissEmptyWarning,
                    modifier = Modifier.testTag("dialog_go_back_button")
                ) {
                    Text(stringResource(R.string.btn_go_back))
                }
            },
            confirmButton = {
                Button(
                    onClick = onProceedEmptyWarning,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = extendedColors.caution,
                        contentColor = extendedColors.onCaution
                    ),
                    modifier = Modifier.testTag("dialog_proceed_button")
                ) {
                    Text(stringResource(R.string.btn_proceed_anyway))
                }
            }
        )
    }
}
