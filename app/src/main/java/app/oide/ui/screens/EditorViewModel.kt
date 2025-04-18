package app.oide.ui.screens

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import app.oide.MainApplication
import app.oide.data.EditorStateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class Editor(
    val id: String,
)

data class EditorUiState(
    val editorState: TextFieldState,
    val filePath: Uri?,
)

class EditorViewModel(
    savedStateHandle: SavedStateHandle,
    private val editorStateRepository: EditorStateRepository
) : ViewModel() {
    companion object {
        const val TAG = "EditorViewModel"

        val Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as MainApplication
                EditorViewModel(this.createSavedStateHandle(), application.editorStateRepository)
            }
        }
    }

    private val parameters = savedStateHandle.toRoute<Editor>()

    private val uiState = MutableStateFlow(EditorUiState(TextFieldState(), null))
    val state = this.uiState.asStateFlow()

    fun replace(ctx: Context, uri: Uri) {
        viewModelScope.launch {
            uiState.update { current ->
                val textFieldState = editorStateRepository.get(ctx, uri)
                    .getOrNull() ?: return@update current

                EditorUiState(textFieldState, uri)
            }
        }
    }

    fun save(ctx: Context) {
        viewModelScope.launch {
            editorStateRepository.save(ctx, uiState.value.editorState)
        }
    }

    fun saveAs(ctx: Context, uri: Uri) {
        viewModelScope.launch {
            uiState.update { current ->
                val newTextFieldState =
                    editorStateRepository.saveAs(ctx, uiState.value.editorState, uri)
                        .getOrNull() ?: return@update current

                EditorUiState(newTextFieldState, uri)
            }
        }
    }
}
