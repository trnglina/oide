package app.oide.data

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FileRepository(private val contentResolver: ContentResolver) {
    companion object {
        const val TAG = "FileRepository"
    }

    suspend fun readContent(filePath: Uri): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                contentResolver.takePersistableUriPermission(
                    filePath,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )

                val content = contentResolver.openInputStream(filePath)?.use { inputStream ->
                    inputStream.reader().readText()
                }

                if (content == null) {
                    throw RuntimeException("Failed to read file content")
                }

                content
            }
        }

    suspend fun writeContent(filePath: Uri, content: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                contentResolver.openOutputStream(filePath, "wt")?.use { outputStream ->
                    outputStream.writer().use { writer -> writer.write(content) }
                }

                Unit
            }
        }
}
