package com.carwise.android.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carwise.android.ui.theme.appRed
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RichTextEditorComponent(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "Yazmaya başlayın...",
    isError: Boolean = false,
    errorMessage: String? = null,
    minHeight: Int = 200,
    maxHeight: Int = 400,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val richTextState = rememberRichTextState()
    var isFocused by remember { mutableStateOf(false) }
    var isToolbarExpanded by remember { mutableStateOf(false) }
    var isInitialized by remember { mutableStateOf(false) }

    // Initialize the editor with the current value only once
    LaunchedEffect(value) {
        if (!isInitialized && value.isNotEmpty()) {
            richTextState.setHtml(value)
            isInitialized = true
        }
    }

    // Update parent when content changes
    LaunchedEffect(richTextState.annotatedString.text) {
        if (isInitialized) {
            val currentHtml = richTextState.toHtml()
            if (currentHtml != value) {
                onValueChange(currentHtml)
            }
        }
    }

    // Mark as initialized when user starts typing
    LaunchedEffect(richTextState.annotatedString.text) {
        if (!isInitialized && richTextState.annotatedString.text.isNotEmpty()) {
            isInitialized = true
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Enhanced Label with character count
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = when {
                    isError -> MaterialTheme.colorScheme.error
                    isFocused -> appRed
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )

            Text(
                text = "${richTextState.annotatedString.length} karakter",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        // Enhanced Editor Container
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = if (isFocused) 8.dp else 2.dp,
                    shape = RoundedCornerShape(16.dp)
                )
                .animateContentSize(),
            colors = CardDefaults.cardColors(
                containerColor = if (enabled) Color.White else Color.Gray.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(
                width = if (isFocused) 2.dp else 1.dp,
                color = when {
                    isError -> MaterialTheme.colorScheme.error
                    isFocused -> appRed
                    else -> Color.LightGray
                }
            )
        ) {
            Column {
                // Enhanced Formatting Toolbar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Gray.copy(alpha = 0.05f)
                ) {
                    Column {
                        // Toolbar header with expand/collapse
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Formatla",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )

                            IconButton(
                                onClick = { isToolbarExpanded = !isToolbarExpanded },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (isToolbarExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = if (isToolbarExpanded) "Daralt" else "Genişlet",
                                    tint = Color.Gray
                                )
                            }
                        }

                        // Main toolbar buttons (always visible)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Quick format buttons
                            FormatButton(
                                icon = Icons.Default.FormatBold,
                                contentDescription = "Kalın",
                                isActive = richTextState.currentSpanStyle.fontWeight == FontWeight.Bold,
                                onClick = {
                                    richTextState.toggleSpanStyle(
                                        SpanStyle(fontWeight = FontWeight.Bold)
                                    )
                                }
                            )

                            FormatButton(
                                icon = Icons.Default.FormatItalic,
                                contentDescription = "İtalik",
                                isActive = richTextState.currentSpanStyle.fontStyle == FontStyle.Italic,
                                onClick = {
                                    richTextState.toggleSpanStyle(
                                        SpanStyle(fontStyle = FontStyle.Italic)
                                    )
                                }
                            )

                            FormatButton(
                                icon = Icons.Default.FormatUnderlined,
                                contentDescription = "Altı Çizili",
                                isActive = richTextState.currentSpanStyle.textDecoration?.contains(TextDecoration.Underline) == true,
                                onClick = {
                                    richTextState.toggleSpanStyle(
                                        SpanStyle(textDecoration = TextDecoration.Underline)
                                    )
                                }
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            // Quick alignment buttons
                            FormatButton(
                                icon = Icons.Default.FormatAlignLeft,
                                contentDescription = "Sola Hizala",
                                isActive = richTextState.currentParagraphStyle.textAlign == TextAlign.Start,
                                onClick = {
                                    richTextState.toggleParagraphStyle(
                                        ParagraphStyle(textAlign = TextAlign.Start)
                                    )
                                }
                            )

                            FormatButton(
                                icon = Icons.Default.FormatAlignCenter,
                                contentDescription = "Ortala",
                                isActive = richTextState.currentParagraphStyle.textAlign == TextAlign.Center,
                                onClick = {
                                    richTextState.toggleParagraphStyle(
                                        ParagraphStyle(textAlign = TextAlign.Center)
                                    )
                                }
                            )
                        }

                        // Expandable toolbar section
                        AnimatedVisibility(
                            visible = isToolbarExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column {
                                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

                                // Advanced formatting options
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Text decoration row
                                    FormattingSection(
                                        title = "Metin Süslemeleri",
                                        buttons = listOf(
                                            FormatButtonData(
                                                icon = Icons.Default.StrikethroughS,
                                                contentDescription = "Üstü Çizili",
                                                isActive = richTextState.currentSpanStyle.textDecoration?.contains(TextDecoration.LineThrough) == true,
                                                onClick = {
                                                    richTextState.toggleSpanStyle(
                                                        SpanStyle(textDecoration = TextDecoration.LineThrough)
                                                    )
                                                }
                                            )
                                        )
                                    )

                                    // Text alignment row
                                    FormattingSection(
                                        title = "Metin Hizalama",
                                        buttons = listOf(
                                            FormatButtonData(
                                                icon = Icons.Default.FormatAlignRight,
                                                contentDescription = "Sağa Hizala",
                                                isActive = richTextState.currentParagraphStyle.textAlign == TextAlign.End,
                                                onClick = {
                                                    richTextState.toggleParagraphStyle(
                                                        ParagraphStyle(textAlign = TextAlign.End)
                                                    )
                                                }
                                            ),
                                            FormatButtonData(
                                                icon = Icons.Default.FormatAlignJustify,
                                                contentDescription = "İki Yana Yasla",
                                                isActive = richTextState.currentParagraphStyle.textAlign == TextAlign.Justify,
                                                onClick = {
                                                    richTextState.toggleParagraphStyle(
                                                        ParagraphStyle(textAlign = TextAlign.Justify)
                                                    )
                                                }
                                            )
                                        )
                                    )

                                    // Color section
                                    FormattingSection(
                                        title = "Metin Rengi",
                                        buttons = listOf(
                                            ColorButtonData(Color.Red, "Kırmızı"),
                                            ColorButtonData(Color.Blue, "Mavi"),
                                            ColorButtonData(Color.Green, "Yeşil"),
                                            ColorButtonData(Color(0xFF9C27B0), "Mor"),
                                            ColorButtonData(Color(0xFFFF9800), "Turuncu"),
                                            ColorButtonData(Color.Black, "Siyah")
                                        ).map { colorData ->
                                            FormatButtonData(
                                                icon = Icons.Default.FormatColorText,
                                                contentDescription = colorData.name,
                                                isActive = richTextState.currentSpanStyle.color == colorData.color,
                                                onClick = {
                                                    richTextState.toggleSpanStyle(
                                                        SpanStyle(color = colorData.color)
                                                    )
                                                },
                                                tint = colorData.color
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color.LightGray)

                // Enhanced Rich Text Editor
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = minHeight.dp, max = maxHeight.dp)
                ) {
                    RichTextEditor(
                        state = richTextState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .onFocusChanged { focusState ->
                                isFocused = focusState.isFocused
                            },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            lineHeight = 24.sp
                        ),
                        placeholder = {
                            Text(
                                text = placeholder,
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                    )
                }
            }
        }

        // Enhanced Error Message
        AnimatedVisibility(
            visible = isError && errorMessage != null,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Hata",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun FormatButton(
    icon: ImageVector,
    contentDescription: String,
    isActive: Boolean,
    onClick: () -> Unit,
    tint: Color? = null
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(36.dp),
        shape = RoundedCornerShape(8.dp),
        color = if (isActive) appRed.copy(alpha = 0.1f) else Color.Transparent,
        border = if (isActive) BorderStroke(1.dp, appRed.copy(alpha = 0.3f)) else null
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.padding(8.dp),
            tint = tint ?: if (isActive) appRed else Color.Gray
        )
    }
}

@Composable
private fun FormattingSection(
    title: String,
    buttons: List<FormatButtonData>
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(buttons) { button ->
                FormatButton(
                    icon = button.icon,
                    contentDescription = button.contentDescription,
                    isActive = button.isActive,
                    onClick = button.onClick,
                    tint = button.tint
                )
            }
        }
    }
}

private data class FormatButtonData(
    val icon: ImageVector,
    val contentDescription: String,
    val isActive: Boolean,
    val onClick: () -> Unit,
    val tint: Color? = null
)

private data class ColorButtonData(
    val color: Color,
    val name: String
)