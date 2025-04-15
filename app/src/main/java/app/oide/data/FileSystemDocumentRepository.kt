package app.oide.data

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FileSystemDocumentRepository(private val contentResolver: ContentResolver) {
    private val TAG: String = "FileSystemDocumentRepository"

    suspend fun getFileName(uri: Uri): Result<String> =
        withContext(Dispatchers.IO) {
            val fileName = contentResolver.query(uri, null, null, null, null)
                ?.use { cursor ->
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    cursor.moveToFirst()
                    cursor.getString(index)
                }

            if (fileName != null) {
                Result.success(fileName)
            } else {
                Result.failure(RuntimeException("Failed to load file name"))
            }
        }

    suspend fun readFileContent(uri: Uri): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )

                val content = contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.reader().readText()
                }

                if (content == null) {
                    throw RuntimeException("Failed to read file content")
                }

                content
            }
        }

    suspend fun writeFileContent(uri: Uri, content: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                contentResolver.openOutputStream(uri, "wt")?.use { outputStream ->
                    outputStream.writer().use { writer -> writer.write(content) }
                }

                Unit
            }
        }
}
