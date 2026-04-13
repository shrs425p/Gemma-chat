package com.gemma.chat.ui.setup

import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemma.chat.data.inference.GemmaInferenceEngine
import com.gemma.chat.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed class SetupState {
    data object Idle : SetupState()
    data class Loading(val progress: String = "Copying model file...") : SetupState()
    data class Success(val modelPath: String) : SetupState()
    data class Error(val message: String) : SetupState()
}

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val inferenceEngine: GemmaInferenceEngine
) : ViewModel() {

    private val _setupState = MutableStateFlow<SetupState>(SetupState.Idle)
    val setupState: StateFlow<SetupState> = _setupState.asStateFlow()

    val isModelReady: StateFlow<Boolean> = _setupState
        .map { it is SetupState.Success }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val savedModelPath: StateFlow<String> = settingsRepository.settingsFlow
        .map { it.modelPath }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    init {
        // Try to load previously saved model on startup
        viewModelScope.launch {
            val appSettings = settingsRepository.settingsFlow.first()
            if (appSettings.modelPath.isNotEmpty() && !inferenceEngine.isReady) {
                val file = java.io.File(appSettings.modelPath)
                if (file.exists()) {
                    _setupState.value = SetupState.Loading("Loading Gemma 4 model...")
                    try {
                        loadModel(
                            path = appSettings.modelPath,
                            maxTokens = appSettings.maxTokens
                        )
                        _setupState.value = SetupState.Success(appSettings.modelPath)
                    } catch (e: Exception) {
                        _setupState.value = SetupState.Idle
                    }
                } else {
                    // Model file was moved/deleted; user needs to re-select
                    _setupState.value = SetupState.Idle
                }
            }
        }
    }

    fun onModelFileSelected(uri: Uri, context: android.content.Context) {
        viewModelScope.launch {
            _setupState.value = SetupState.Loading("Copying model to app storage...")

            try {
                // Copy the model file to internal storage via a background thread.
                // This avoids content URI permission issues and ensures persistent access.
                val internalPath = withContext(Dispatchers.IO) {
                    copyModelToInternalStorage(uri, context)
                }

                _setupState.value = SetupState.Loading("Initializing Gemma 4 engine...")

                val currentSettings = settingsRepository.settingsFlow.first()

                loadModel(
                    path = internalPath,
                    maxTokens = currentSettings.maxTokens
                )

                settingsRepository.saveModelPath(internalPath)
                _setupState.value = SetupState.Success(internalPath)
            } catch (e: Exception) {
                _setupState.value = SetupState.Error("Failed to load model: ${e.message}")
            }
        }
    }

    private suspend fun loadModel(
        path: String,
        maxTokens: Int
    ) {
        inferenceEngine.initialize(
            modelPath = path,
            maxTokens = maxTokens
        )
    }

    /**
     * Copy model file from content URI to internal storage.
     * Uses buffered streaming to avoid OOM on large (2.5 GB+) files.
     */
    private fun copyModelToInternalStorage(uri: Uri, context: android.content.Context): String {
        val contentResolver = context.contentResolver
        val fileName = getFileNameFromUri(uri, context) ?: "gemma_model.litertlm"
        val destFile = java.io.File(context.filesDir, fileName)

        // If file already exists with same name, skip copy
        if (destFile.exists()) {
            return destFile.absolutePath
        }

        contentResolver.openInputStream(uri)?.use { inputStream ->
            destFile.outputStream().buffered(1024 * 1024).use { outputStream ->
                // Use 1 MB buffer for faster large file transfer
                val buffer = ByteArray(1024 * 1024)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }
            }
        } ?: throw Exception("Cannot open model file. Please check permissions.")

        return destFile.absolutePath
    }

    private fun getFileNameFromUri(uri: Uri, context: android.content.Context): String? {
        var fileName: String? = null
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex)
                }
            }
        }
        return fileName
    }

    fun resetState() {
        _setupState.value = SetupState.Idle
    }

    override fun onCleared() {
        super.onCleared()
    }
}
