package app.oide.service

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.insert
import app.oide.data.FileRepository
import app.oide.data.Sheet
import app.oide.data.SheetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class EditorState(
    val sheet: Sheet,
    val textFieldState: TextFieldState,
)

class EditorService(
    private val fileRepository: FileRepository,
    private val sheetRepository: SheetRepository,
) {
    companion object {
        const val TAG = "EditorService"
    }

    private val savedContentHashes = HashMap<Int, Int>()
    private val buffers = HashMap<Int, TextFieldState>()

    fun flowById(id: Int, ctx: Context): Flow<EditorState> = flow {
        if (sheetRepository.getById(id) == null) {
            val (sheet, buffer) = getOrCreateById(id, ctx)
            emit(EditorState(sheet, buffer))
        }

        sheetRepository.flowById(id).collect { sheet ->
            val (sheet, buffer) = getOrCreateById(id, ctx)
            emit(EditorState(sheet, buffer))
        }
    }

    suspend fun replaceWithFile(id: Int, filePath: Uri, ctx: Context): Result<Unit> = runCatching {
        val content = fileRepository.readContent(filePath, ctx).getOrThrow()
        val (sheet, buffer) = getOrCreateById(id, ctx, content)

        sheetRepository.update(Sheet.PersistedSheet(id = Sheet.id(sheet), filePath))

        buffer.edit { replace(0, length, content) }
        savedContentHashes[id] = content.hashCode()
    }

    suspend fun save(id: Int, ctx: Context): Result<Unit> = runCatching {
        val (sheet, buffer) = getOrCreateById(id, ctx)
        val content = buffer.text.toString()
        val contentHash = content.hashCode()

        when {
            savedContentHashes[id] == contentHash -> Unit
            sheet is Sheet.TransientSheet -> sheetRepository.update(sheet.copy(contents = content))
            sheet is Sheet.PersistedSheet -> fileRepository.writeContent(sheet.filePath, ctx, content).getOrThrow()
        }

        savedContentHashes[id] = contentHash
    }

    suspend fun saveAs(id: Int, filePath: Uri, ctx: Context): Result<Unit> = runCatching {
        val (sheet, buffer) = getOrCreateById(id, ctx)
        val content = buffer.text.toString()

        val resultingBuffer = if (sheet is Sheet.PersistedSheet) {
            val newBuffer = TextFieldState()
            newBuffer.edit {
                insert(0, content)
                selection = buffer.selection
            }
            newBuffer
        } else {
            buffer
        }

        fileRepository.writeContent(filePath, ctx, content).getOrThrow()
        sheetRepository.update(Sheet.PersistedSheet(id, filePath))

        buffers[id] = resultingBuffer
        savedContentHashes[id] = content.hashCode()
    }

    private suspend fun getOrCreateById(id: Int, ctx: Context, initialContent: String = ""): Pair<Sheet, TextFieldState> {
        val sheet = sheetRepository.getById(id).let { sheet ->
            if (sheet != null) sheet
            else {
                val sheet = Sheet.TransientSheet(id, initialContent)
                sheetRepository.insert(sheet)
                sheet
            }
        }

        val existing = buffers[id]
        if (existing != null) {
            return return Pair(sheet, existing)
        }

        val content = when (sheet) {
            is Sheet.TransientSheet -> sheet.contents
            is Sheet.PersistedSheet -> fileRepository.readContent(sheet.filePath, ctx).getOrDefault(initialContent)
        }

        val buffer = TextFieldState()
        buffers[id] = buffer

        buffer.edit { insert(0, content) }
        savedContentHashes[id] = content.hashCode()

        return Pair(sheet, buffer)
    }
}