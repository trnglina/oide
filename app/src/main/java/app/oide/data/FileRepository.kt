package app.oide.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FileRepository() {
    companion object {
        const val TAG = "FileRepository"
    }

    suspend fun readContent(
        filePath: Uri,
        ctx: Context,
    ): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                ctx.contentResolver.takePersistableUriPermission(
                    filePath,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )

                val content =
                    ctx.contentResolver.openInputStream(filePath)?.use { inputStream ->
                        inputStream.reader().readText()
                    }

                if (content == null) {
                    throw RuntimeException("Failed to read file content")
                }

                content
            }
        }

    suspend fun writeContent(
        filePath: Uri,
        ctx: Context,
        content: String,
    ): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                ctx.contentResolver.openOutputStream(filePath, "wt")?.use { outputStream ->
                    outputStream.writer().use { writer -> writer.write(content) }
                }

                Unit
            }
        }
}
