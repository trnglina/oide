package app.oide

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.insert
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.oide.data.FileRepository
import app.oide.utility.HashBiMap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UiState(
    val textFieldState: TextFieldState,
    val filePath: Uri?,
)

class MainViewModel(
    context: Context,
) : ViewModel() {
    companion object {
        const val TAG = "MainViewModel"
    }

    private val fileRepository = FileRepository(context.contentResolver)

    private val savedContentHashes = HashMap<Uri, Int>()
    private val fileBackedStates = HashBiMap<Uri, TextFieldState>()

    private val uiState = MutableStateFlow<UiState>(UiState(TextFieldState(), null))

    val state = uiState.asStateFlow()

    fun saveToFile(uri: Uri) {
        viewModelScope.launch {
            uiState.update { current ->
                val newTextFieldState = writeTextFieldState(uiState.value.textFieldState, uri)
                    .getOrNull() ?: return@update current

                UiState(newTextFieldState, uri)
            }
        }
    }

    fun loadFromFile(uri: Uri) {
        viewModelScope.launch {
            uiState.update { current ->
                val textFieldState = getTextFieldState(uri)
                    .getOrNull() ?: return@update current

                UiState(textFieldState, uri)
            }
        }
    }

    fun save() {
        viewModelScope.launch {
            uiState.update { current ->
                val newTextFieldState =
                    writeTextFieldState(uiState.value.textFieldState, null)
                        .getOrNull() ?: return@update current

                current.copy(textFieldState = newTextFieldState)
            }
        }
    }

    fun new() {
        uiState.update {
            UiState(TextFieldState(), null)
        }
    }

    private suspend fun getTextFieldState(
        filePath: Uri
    ): Result<TextFieldState> {
        return runCatching {
            val existing = fileBackedStates[filePath]
            if (existing != null) {
                Log.i(TAG, "Retrieving TextFieldState from in-memory: $filePath")
                existing
            } else {
                val new = TextFieldState()
                val content = fileRepository.readContent(filePath).getOrThrow()
                new.edit { insert(0, content) }
                fileBackedStates[filePath] = new
                savedContentHashes[filePath] = content.hashCode()

                Log.i(TAG, "New TextFieldState created from file: $filePath")
                new
            }
        }
    }

    private suspend fun writeTextFieldState(
        textFieldState: TextFieldState,
        filePath: Uri?
    ): Result<TextFieldState> {
        return runCatching {
            val existingFilePath = fileBackedStates.inverse[textFieldState]
            val content = textFieldState.text.toString()

            when {
                // If nothing has changed, no-op.
                savedContentHashes[filePath] == content.hashCode() -> {
                    textFieldState
                }

                // In-place save.
                existingFilePath != null && filePath == null -> {
                    fileRepository.writeContent(existingFilePath, content).getOrThrow()
                    savedContentHashes[existingFilePath] = content.hashCode()

                    Log.i(TAG, "Save in-place: $existingFilePath")
                    textFieldState
                }

                // Save-as to a new path.
                existingFilePath != null && filePath != null -> {
                    if (fileBackedStates[filePath] != null) {
                        Log.i(TAG, "Replacing existing TextFieldState at: $filePath")
                    }

                    val new = TextFieldState()
                    new.edit {
                        insert(0, content)
                        selection = textFieldState.selection
                    }

                    fileRepository.writeContent(filePath, content).getOrThrow()
                    fileBackedStates[filePath] = new
                    savedContentHashes[filePath] = content.hashCode()

                    Log.i(TAG, "Save-as: $existingFilePath -> $filePath")
                    new
                }

                // If there's no existing file path, then a file path must be provided.
                filePath == null -> {
                    throw IllegalArgumentException("Attempting to save an unsaved TextFieldState without a file path provided")
                }

                // Otherwise, we're saving a new, unsaved TextFieldState.
                else -> {
                    if (fileBackedStates[filePath] != null) {
                        Log.i(TAG, "Replacing existing TextFieldState at: $filePath")
                    }

                    fileRepository.writeContent(filePath, content).getOrThrow()
                    fileBackedStates[filePath] = textFieldState
                    savedContentHashes[filePath] = content.hashCode()

                    Log.i(TAG, "Save new: $filePath")
                    textFieldState
                }
            }
        }
    }

    class Factory(
        private val context: Context,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(context) as T
            }

            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}