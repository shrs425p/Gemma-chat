package com.gemma.chat.ui.chat.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A simple Markdown renderer supporting:
 * - **bold**
 * - *italic*
 * - `inline code`
 * - ### headings
 * - Plain text paragraphs
 *
 * For full Markdown (tables, lists, etc.), a library like Markwon would be needed,
 * but this covers the most common LLM output patterns.
 */
@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val paragraphs = text.split("\n\n").filter { it.isNotBlank() }

    Column(modifier = modifier) {
        paragraphs.forEachIndexed { index, paragraph ->
            if (paragraph.startsWith("```")) {
                // Code block
                val codeContent = paragraph
                    .removePrefix("```")
                    .removeSuffix("```")
                    .trim()
                // Remove language hint if present
                val firstLine = codeContent.lines().first()
                val code = if (firstLine.matches(Regex("[a-zA-Z]+"))) {
                    codeContent.lines().drop(1).joinToString("\n")
                } else {
                    codeContent
                }
                CodeBlock(code = code)
            } else if (paragraph.startsWith("#")) {
                // Heading
                val level = paragraph.takeWhile { it == '#' }.length
                val headingText = paragraph.dropWhile { it == '#' }.trim()
                val style = when (level) {
                    1 -> MaterialTheme.typography.headlineSmall
                    2 -> MaterialTheme.typography.titleLarge
                    else -> MaterialTheme.typography.titleMedium
                }
                Text(
                    text = headingText,
                    style = style,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            } else if (paragraph.startsWith("- ") || paragraph.startsWith("* ")) {
                // Bullet list
                paragraph.lines().forEach { line ->
                    val bullet = if (line.startsWith("- ") || line.startsWith("* ")) "• " + line.drop(2) else line
                    Text(
                        text = parseInlineMarkdown(bullet, textColor),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            } else {
                // Regular paragraph with inline markdown
                Text(
                    text = parseInlineMarkdown(paragraph, textColor),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            if (index < paragraphs.size - 1) {
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun CodeBlock(code: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Text(
            text = code,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(12.dp)
        )
    }
}

private fun parseInlineMarkdown(text: String, textColor: Color): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        while (i < text.length) {
            when {
                // Bold+italic ***
                text.startsWith("***", i) -> {
                    val end = text.indexOf("***", i + 3)
                    if (end != -1) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)) {
                            append(text.substring(i + 3, end))
                        }
                        i = end + 3
                    } else {
                        append(text[i])
                        i++
                    }
                }
                // Bold **
                text.startsWith("**", i) -> {
                    val end = text.indexOf("**", i + 2)
                    if (end != -1) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(text.substring(i + 2, end))
                        }
                        i = end + 2
                    } else {
                        append(text[i])
                        i++
                    }
                }
                // Italic *
                text.startsWith("*", i) && !text.startsWith("**", i) -> {
                    val end = text.indexOf("*", i + 1)
                    if (end != -1 && !text.startsWith("**", end)) {
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(text.substring(i + 1, end))
                        }
                        i = end + 1
                    } else {
                        append(text[i])
                        i++
                    }
                }
                // Inline code `
                text.startsWith("`", i) -> {
                    val end = text.indexOf("`", i + 1)
                    if (end != -1) {
                        withStyle(
                            SpanStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                background = textColor.copy(alpha = 0.1f)
                            )
                        ) {
                            append(" ${text.substring(i + 1, end)} ")
                        }
                        i = end + 1
                    } else {
                        append(text[i])
                        i++
                    }
                }
                else -> {
                    append(text[i])
                    i++
                }
            }
        }
    }
}
