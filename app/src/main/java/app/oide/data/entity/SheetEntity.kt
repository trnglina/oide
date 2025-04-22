package app.oide.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sheets",
    indices = [
        Index(value = ["file_path"], unique = true)
    ]
)
data class SheetEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "contents") val contents: String?,
    @ColumnInfo(name = "file_path") val filePath: String?,
)
