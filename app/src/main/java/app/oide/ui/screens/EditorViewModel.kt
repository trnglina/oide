package app.oide.ui.screens

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.delete
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.oide.data.FileSystemDocumentRepository
import app.oide.data.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class EditorState {
    class Unsaved(
    ) : EditorState()

    data class Saved(
        override val path: Uri,
        override val fileName: String,
        override val savedContentHashCode: Int,
    ) : EditorState()

    open val path: Uri? = null
    open val fileName: String? = null
    open val savedContentHashCode: Int? = null
}

class EditorViewModel(
    private val documentRepository: FileSystemDocumentRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    companion object {
        const val TAG = "EditorViewModel"
    }

    private var _state = MutableStateFlow<EditorState>(EditorState.Unsaved())
    val state = _state.asStateFlow()

    val textFieldState = TextFieldState()

    fun clear() {
        viewModelScope.launch {
            _state.update {
                textFieldState.edit { delete(0, length) }
                settingsRepository.storeLastFile(null)
                Log.i(TAG, "Cleared editor")

                EditorState.Unsaved()
            }
        }
    }

    fun saveCurrent() {
        viewModelScope.launch {
            _state.update { current ->
                val current = _state.value

                // No-op if there's no currently opened file.
                if (current !is EditorState.Saved) return@update current

                // No-op if the content is unchanged from last save.
                if (current.savedContentHashCode == textFieldState.text.hashCode()) return@update current

                try {
                    val content = textFieldState.text.toString()
                    documentRepository.writeFileContent(current.path, content).getOrThrow()
                    Log.i(TAG, "Saved file: $current.path")

                    current.copy(savedContentHashCode = content.hashCode())
                } catch (e: Exception) {
                    Log.e(TAG, "Could not save successfully: $e")
                    // TODO: Handle failure

                    current
                }
            }
        }
    }

    fun saveAs(uri: Uri) {
        viewModelScope.launch {
            _state.update { current ->
                try {
                    val fileName = documentRepository.getFileName(uri).getOrDefault("")
                    val content = textFieldState.text.toString()

                    documentRepository.writeFileContent(uri, content).getOrThrow()
                    settingsRepository.storeLastFile(uri)
                    Log.i(TAG, "Saved file: $uri")

                    EditorState.Saved(
                        path = uri,
                        fileName = fileName,
                        savedContentHashCode = content.hashCode()
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Could not save successfully: $e")
                    // TODO: Handle failure

                    current
                }
            }
        }
    }

    fun loadFromFile(uri: Uri) {
        viewModelScope.launch {
            _state.update {
                loadFile(uri)
            }
        }
    }

    fun loadFromLastFile() {
        viewModelScope.launch {
            _state.update { current ->
                val uri = settingsRepository.getLastFile() ?: return@update current
                loadFile(uri)
            }
        }
    }

    private suspend fun loadFile(uri: Uri): EditorState {
        return try {
            val fileName = documentRepository.getFileName(uri).getOrDefault("")
            val content = documentRepository.readFileContent(uri).getOrThrow()

            textFieldState.edit { replace(0, length, content) }
            settingsRepository.storeLastFile(uri)
            Log.i(TAG, "Loaded file: $uri")

            EditorState.Saved(
                path = uri,
                fileName = fileName,
                savedContentHashCode = content.hashCode()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Could not load successfully: $e")
            // TODO: Handle failure

            EditorState.Unsaved()
        }
    }

    class Factory(
        private val documentRepository: FileSystemDocumentRepository,
        private val settingsRepository: SettingsRepository,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EditorViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return EditorViewModel(documentRepository, settingsRepository) as T
            }

            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
