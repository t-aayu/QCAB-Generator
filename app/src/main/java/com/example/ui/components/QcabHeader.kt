package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.rounded.BrightnessAuto
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.ThemeMode
import com.example.ui.theme.LocalQcabColors
import com.example.ui.theme.PtSerifFontFamily

@Composable
fun QcabHeroHeader(
    stepLabel: String,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = LocalQcabColors.current
    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            extendedColors.heroStart,
            extendedColors.heroMid,
            extendedColors.heroEnd
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(gradientBrush)
            .drawBehind {
                // Subtle architectural A4 margin lines behind the header
                val marginColor = Color.White.copy(alpha = 0.06f)
                val leftX = size.width * 0.12f
                val rightX = size.width * 0.88f
                drawLine(
                    color = marginColor,
                    start = Offset(leftX, 0f),
                    end = Offset(leftX, size.height),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = marginColor,
                    start = Offset(rightX, 0f),
                    end = Offset(rightX, size.height),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.12f),
                    start = Offset(0f, size.height - 1f),
                    end = Offset(size.width, size.height - 1f),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar: Theme mode selector (System / Light / Dark)
            ThemeModeSegmentedPill(
                selectedMode = themeMode,
                onSelectMode = onThemeModeChange
            )

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                color = Color.White.copy(alpha = 0.10f),
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Description,
                        contentDescription = null,
                        tint = extendedColors.heroText,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = stepLabel.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = extendedColors.heroText,
                        letterSpacing = 1.2.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.header_title),
                style = MaterialTheme.typography.headlineMedium,
                color = extendedColors.heroText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.header_subtitle).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = extendedColors.heroTextMuted,
                letterSpacing = 1.4.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

@Composable
fun ThemeModeSegmentedPill(
    selectedMode: ThemeMode,
    onSelectMode: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = LocalQcabColors.current
    val options = listOf(
        Triple(ThemeMode.SYSTEM, R.string.theme_mode_system, Icons.Rounded.BrightnessAuto),
        Triple(ThemeMode.LIGHT, R.string.theme_mode_light, Icons.Rounded.LightMode),
        Triple(ThemeMode.DARK, R.string.theme_mode_dark, Icons.Rounded.DarkMode)
    )

    Surface(
        modifier = modifier.testTag("theme_mode_segmented_bar"),
        shape = RoundedCornerShape(50),
        color = Color.White.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f))
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            options.forEach { (mode, labelRes, icon) ->
                val isSelected = selectedMode == mode
                val chipBg by animateColorAsState(
                    targetValue = if (isSelected) Color.White else Color.Transparent,
                    label = "theme_pill_bg"
                )
                val chipFg by animateColorAsState(
                    targetValue = if (isSelected) extendedColors.heroStart else extendedColors.heroText.copy(alpha = 0.85f),
                    label = "theme_pill_fg"
                )
                val tag = when (mode) {
                    ThemeMode.SYSTEM -> "theme_mode_system"
                    ThemeMode.LIGHT -> "theme_mode_light"
                    ThemeMode.DARK -> "theme_mode_dark"
                }

                Box(
                    modifier = Modifier
                        .heightIn(min = 40.dp)
                        .clip(RoundedCornerShape(50))
                        .background(chipBg)
                        .semantics { selected = isSelected }
                        .clickable(role = Role.Tab) { onSelectMode(mode) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag(tag),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = chipFg,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = stringResource(labelRes),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = chipFg
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeModeMenuButton(
    selectedMode: ThemeMode,
    onSelectMode: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val activeIcon: ImageVector = when (selectedMode) {
        ThemeMode.SYSTEM -> Icons.Rounded.BrightnessAuto
        ThemeMode.LIGHT -> Icons.Rounded.LightMode
        ThemeMode.DARK -> Icons.Rounded.DarkMode
    }

    Box(modifier = modifier) {
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .testTag("theme_mode_menu_button")
        ) {
            Icon(
                imageVector = activeIcon,
                contentDescription = stringResource(R.string.cd_theme_mode_menu),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            val items = listOf(
                Triple(ThemeMode.SYSTEM, R.string.theme_mode_system, Icons.Rounded.BrightnessAuto),
                Triple(ThemeMode.LIGHT, R.string.theme_mode_light, Icons.Rounded.LightMode),
                Triple(ThemeMode.DARK, R.string.theme_mode_dark, Icons.Rounded.DarkMode)
            )
            items.forEach { (mode, labelRes, icon) ->
                val isSelected = selectedMode == mode
                val itemTag = when (mode) {
                    ThemeMode.SYSTEM -> "theme_menu_system"
                    ThemeMode.LIGHT -> "theme_menu_light"
                    ThemeMode.DARK -> "theme_menu_dark"
                }
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(labelRes),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    },
                    trailingIcon = if (isSelected) {
                        {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else {
                        null
                    },
                    onClick = {
                        onSelectMode(mode)
                        expanded = false
                    },
                    modifier = Modifier.testTag(itemTag)
                )
            }
        }
    }
}

@Composable
fun QcabMiniSheetPreview(
    testName: String,
    questionCount: Int,
    modifier: Modifier = Modifier
) {
    val extendedColors = LocalQcabColors.current
    val sheetBg = extendedColors.paperSheet
    val sheetInk = extendedColors.paperInk

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Miniature A4 Sheet rendered to authentic UPSC QCAB proportions (210:297)
            Surface(
                modifier = Modifier
                    .width(96.dp)
                    .aspectRatio(210f / 297f),
                shape = RoundedCornerShape(6.dp),
                color = sheetBg,
                shadowElevation = 4.dp,
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val leftX = size.width * (25f / 210f)
                        val rightX = size.width * (185f / 210f)
                        val topY = size.height * (15f / 297f)
                        val bottomY = size.height * (282f / 297f)

                        // Double vertical official margins
                        drawLine(
                            color = sheetInk.copy(alpha = 0.78f),
                            start = Offset(leftX, topY),
                            end = Offset(leftX, bottomY),
                            strokeWidth = 0.8.dp.toPx()
                        )
                        drawLine(
                            color = sheetInk.copy(alpha = 0.78f),
                            start = Offset(rightX, topY),
                            end = Offset(rightX, bottomY),
                            strokeWidth = 0.8.dp.toPx()
                        )

                        // Simulated question text lines
                        val bodyLeft = leftX + size.width * 0.03f
                        val bodyRight = rightX - size.width * 0.03f
                        val lineStartY = topY + size.height * 0.035f
                        val lineGap = size.height * 0.028f

                        drawLine(
                            color = sheetInk.copy(alpha = 0.7f),
                            start = Offset(bodyLeft, lineStartY),
                            end = Offset(bodyRight, lineStartY),
                            strokeWidth = 1.4.dp.toPx()
                        )
                        drawLine(
                            color = sheetInk.copy(alpha = 0.5f),
                            start = Offset(bodyLeft, lineStartY + lineGap),
                            end = Offset(bodyRight * 0.92f, lineStartY + lineGap),
                            strokeWidth = 1.4.dp.toPx()
                        )
                        drawLine(
                            color = sheetInk.copy(alpha = 0.35f),
                            start = Offset(bodyLeft, lineStartY + lineGap * 2),
                            end = Offset(bodyLeft + (bodyRight - bodyLeft) * 0.62f, lineStartY + lineGap * 2),
                            strokeWidth = 1.4.dp.toPx()
                        )
                        // Marks stamp indicator on the right
                        drawLine(
                            color = sheetInk.copy(alpha = 0.8f),
                            start = Offset(bodyRight - size.width * 0.16f, lineStartY + lineGap * 3.5f),
                            end = Offset(bodyRight, lineStartY + lineGap * 3.5f),
                            strokeWidth = 1.6.dp.toPx()
                        )
                    }

                    Text(
                        text = "Q.1",
                        fontFamily = PtSerifFontFamily,
                        fontSize = 6.sp,
                        color = sheetInk,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 3.dp, top = 8.dp)
                    )

                    if (testName.isNotBlank()) {
                        Text(
                            text = testName.trim(),
                            fontFamily = PtSerifFontFamily,
                            fontStyle = FontStyle.Italic,
                            fontSize = 5.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = sheetInk.copy(alpha = 0.8f),
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(start = 7.dp, end = 12.dp, bottom = 2.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = stringResource(R.string.preview_card_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "A4 (210 × 297 mm) • 25 mm double margins • PT Serif typography • Right-margin exam notice",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "$questionCount ${if (questionCount == 1) "Question" else "Questions"}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    if (testName.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Text(
                                text = testName.trim(),
                                style = MaterialTheme.typography.labelSmall,
                                fontStyle = FontStyle.Italic,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
