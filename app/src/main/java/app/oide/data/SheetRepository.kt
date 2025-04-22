package app.oide.data

import android.net.Uri
import androidx.core.net.toUri
import app.oide.data.entity.SheetEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull

sealed class Sheet() {
    companion object {
        fun id(sheet: Sheet): Int {
            return when (sheet) {
                is PersistedSheet -> sheet.id
                is TransientSheet -> sheet.id
            }
        }

        internal fun fromEntity(entity: SheetEntity): Sheet {
            val filePath = runCatching { entity.filePath?.toUri() }.getOrNull()

            return when {
                filePath != null -> PersistedSheet(
                    id = entity.id,
                    filePath = filePath
                )

                else -> TransientSheet(
                    id = entity.id,
                    contents = entity.contents ?: "",
                )
            }
        }
    }

    data class TransientSheet(
        val id: Int,
        val contents: String
    ) : Sheet()

    data class PersistedSheet(
        val id: Int,
        val filePath: Uri
    ) : Sheet()

    internal fun toEntity(): SheetEntity {
        return when (this) {
            is TransientSheet -> SheetEntity(
                id = this.id,
                contents = this.contents,
                filePath = null
            )

            is PersistedSheet -> SheetEntity(
                id = this.id,
                contents = null,
                filePath = this.filePath.toString()
            )
        }
    }
}

class SheetRepository(
    private val database: Database,
) {
    companion object {
        const val TAG = "SheetRepository"
    }

    fun flowById(id: Int): Flow<Sheet> {
        return database.sheets().flowById(id).distinctUntilChanged().mapNotNull {
            if (it != null) Sheet.fromEntity(it) else null
        }.distinctUntilChanged()
    }

    suspend fun getById(id: Int): Sheet? {
        return database.sheets().loadById(id).let {
            if (it != null) Sheet.fromEntity(it) else null
        }
    }

    suspend fun getByFilePath(filePath: Uri): Sheet? {
        return database.sheets().loadByFilePath(filePath.toString()).let {
            if (it != null) Sheet.fromEntity(it) else null
        }
    }

    suspend fun update(sheet: Sheet) {
        database.sheets().update(sheet.toEntity())
    }

    suspend fun insert(vararg sheets: Sheet) {
        database.sheets().insert(*sheets.map { it.toEntity() }.toTypedArray())
    }
}