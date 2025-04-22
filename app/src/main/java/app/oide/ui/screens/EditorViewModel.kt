package app.oide.ui.screens

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import app.oide.MainApplication
import app.oide.service.EditorService
import app.oide.service.EditorState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class Editor(
    val id: Int,
)

class EditorViewModel : ViewModel {
    companion object {
        const val TAG = "EditorViewModel"

        val Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as MainApplication
                EditorViewModel(
                    this.createSavedStateHandle(),
                    application.editorService
                )
            }
        }
    }

    private val editorService: EditorService
    private val parameters: Editor

    constructor(
        savedStateHandle: SavedStateHandle,
        editorService: EditorService,
    ) : super() {
        this.editorService = editorService
        this.parameters = savedStateHandle.toRoute<Editor>()
    }

    fun flow(ctx: Context): Flow<EditorState> =
        editorService.flowById(parameters.id, ctx)

    fun replace(filePath: Uri, ctx: Context) = viewModelScope.launch {
        editorService.replaceWithFile(parameters.id, filePath, ctx)
    }

    fun save(ctx: Context) = viewModelScope.launch {
        editorService.save(parameters.id, ctx)
    }

    fun saveAs(filePath: Uri, ctx: Context) = viewModelScope.launch {
        editorService.saveAs(parameters.id, filePath, ctx)
    }
}
