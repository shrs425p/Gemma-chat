package com.gemma.chat.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gemma.chat.ui.theme.GemmaViolet
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onChangeModel: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
        ) {
            Spacer(Modifier.height(8.dp))

            // Model section
            SettingsSectionHeader(icon = Icons.Default.SmartToy, title = "Model")
            SettingsCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Active Model",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (settings.modelPath.isNotEmpty())
                                settings.modelPath.substringAfterLast("/")
                            else "No model loaded",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OutlinedButton(onClick = onChangeModel) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null)
                        Text("Change", modifier = Modifier.padding(start = 4.dp))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Generation parameters
            SettingsSectionHeader(icon = Icons.Default.Psychology, title = "Generation")
            SettingsCard {
                // Temperature
                Text(
                    text = "Temperature: ${"%.2f".format(settings.temperature)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Higher = more creative, lower = more focused",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = settings.temperature,
                    onValueChange = { viewModel.setTemperature(it) },
                    valueRange = 0f..2f,
                    steps = 39,
                    colors = SliderDefaults.colors(thumbColor = GemmaViolet, activeTrackColor = GemmaViolet)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Top-K
                Text(
                    text = "Top-K: ${settings.topK}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Number of top tokens to consider",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = settings.topK.toFloat(),
                    onValueChange = { viewModel.setTopK(it.roundToInt()) },
                    valueRange = 1f..100f,
                    steps = 98,
                    colors = SliderDefaults.colors(thumbColor = GemmaViolet, activeTrackColor = GemmaViolet)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Top-P
                Text(
                    text = "Top-P: ${"%.2f".format(settings.topP)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Nucleus sampling threshold",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = settings.topP,
                    onValueChange = { viewModel.setTopP(it) },
                    valueRange = 0f..1f,
                    steps = 19,
                    colors = SliderDefaults.colors(thumbColor = GemmaViolet, activeTrackColor = GemmaViolet)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Max tokens
                Text(
                    text = "Max Output Tokens: ${settings.maxTokens}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Slider(
                    value = settings.maxTokens.toFloat(),
                    onValueChange = { viewModel.setMaxTokens(it.roundToInt()) },
                    valueRange = 256f..4096f,
                    steps = 14,
                    colors = SliderDefaults.colors(thumbColor = GemmaViolet, activeTrackColor = GemmaViolet)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Context window
                Text(
                    text = "Context Window: ${settings.contextWindowSize} exchanges",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "How many past messages to include in context",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = settings.contextWindowSize.toFloat(),
                    onValueChange = { viewModel.setContextWindowSize(it.roundToInt()) },
                    valueRange = 1f..20f,
                    steps = 18,
                    colors = SliderDefaults.colors(thumbColor = GemmaViolet, activeTrackColor = GemmaViolet)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Streaming toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Streaming Output",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Show tokens as they're generated",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = settings.streamingEnabled,
                        onCheckedChange = { viewModel.setStreamingEnabled(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = GemmaViolet, checkedTrackColor = GemmaViolet.copy(alpha = 0.4f))
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // System prompt
            SettingsSectionHeader(icon = Icons.Default.Memory, title = "System Prompt")
            SettingsCard {
                var promptText by remember(settings.systemPrompt) {
                    mutableStateOf(settings.systemPrompt)
                }
                OutlinedTextField(
                    value = promptText,
                    onValueChange = {
                        promptText = it
                        viewModel.setSystemPrompt(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "You are Gemma, a helpful AI assistant...",
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    minLines = 4,
                    maxLines = 10,
                    shape = RoundedCornerShape(12.dp),
                    label = { Text("Customize Gemma's behavior") }
                )
            }

            Spacer(Modifier.height(16.dp))

            // Appearance
            SettingsSectionHeader(icon = Icons.Default.Palette, title = "Appearance")
            SettingsCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Dark Mode",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Enforce deep space premium dark theme",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = settings.isDarkTheme,
                        onCheckedChange = { viewModel.setDarkTheme(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = GemmaViolet, checkedTrackColor = GemmaViolet.copy(alpha = 0.4f))
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Dynamic Color",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Use device wallpaper colors (Android 12+)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = settings.useDynamicColor,
                        onCheckedChange = { viewModel.setDynamicColor(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = GemmaViolet, checkedTrackColor = GemmaViolet.copy(alpha = 0.4f))
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // App info
            Text(
                text = "Gemma Chat v2.0 • Powered by Gemma 4 E2B Q4\nRunning 100% offline via LiteRT-LM Engine",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.align(Alignment.CenterHorizontally),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsSectionHeader(icon: ImageVector, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = GemmaViolet,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = GemmaViolet
        )
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}
