package app.oide.data

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.insert

class EditorStateRepository(
    private val fileRepository: FileRepository
) {
    companion object {
        const val TAG = "EditorStateRepository"
    }

    private val savedContentHashes = HashMap<Uri, Int>()
    private val fileBackedStates = HashBiMap<Uri, TextFieldState>()

    suspend fun get(
        ctx: Context,
        filePath: Uri
    ): Result<TextFieldState> {
        return runCatching {
            val existing = fileBackedStates[filePath]
            if (existing != null) {
                Log.i(TAG, "Retrieving TextFieldState from in-memory: $filePath")
                existing
            } else {
                val new = TextFieldState()
                val content = fileRepository.readContent(ctx, filePath).getOrThrow()
                new.edit { insert(0, content) }
                fileBackedStates[filePath] = new
                savedContentHashes[filePath] = content.hashCode()

                Log.i(TAG, "New TextFieldState created from file: $filePath")
                new
            }
        }
    }

    suspend fun save(
        ctx: Context,
        textFieldState: TextFieldState,
    ): Result<Unit> {
        return runCatching {
            val existingFilePath = fileBackedStates.inverse[textFieldState]
            val content = textFieldState.text.toString()

            // No-op if the provided state has no associated file path.
            if (existingFilePath == null) return@runCatching

            // No-op if the state hasn't changed.
            if (savedContentHashes[existingFilePath] == content.hashCode()) return@runCatching

            fileRepository.writeContent(ctx, existingFilePath, content).getOrThrow()
            savedContentHashes[existingFilePath] = content.hashCode()

            Log.i(TAG, "Saved in-place: $existingFilePath")
        }
    }

    suspend fun saveAs(
        ctx: Context,
        textFieldState: TextFieldState,
        filePath: Uri
    ): Result<TextFieldState> {
        return runCatching {
            val content = textFieldState.text.toString()

            val resultingState = if (fileBackedStates.inverse[textFieldState] != null) {
                Log.i(TAG, "Duplicating TextFieldState")
                val new = TextFieldState()
                new.edit {
                    insert(0, content)
                    selection = textFieldState.selection
                }
                new
            } else {
                textFieldState
            }

            if (fileBackedStates[filePath] != null) {
                Log.i(TAG, "Replacing existing TextFieldState at: $filePath")
            }

            fileRepository.writeContent(ctx, filePath, content).getOrThrow()
            fileBackedStates[filePath] = resultingState
            savedContentHashes[filePath] = content.hashCode()

            Log.i(TAG, "Save-as $filePath")
            resultingState
        }
    }
}