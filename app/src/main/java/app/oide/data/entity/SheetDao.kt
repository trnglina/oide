package app.oide.data.entity

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SheetDao {
    @Query("SELECT * from sheets WHERE id = :id")
    fun flowById(id: Int): Flow<SheetEntity?>

    @Query("SELECT * from sheets WHERE id = :id")
    suspend fun loadById(id: Int): SheetEntity?

    @Query("SELECT * from sheets WHERE file_path = :filePath")
    suspend fun loadByFilePath(filePath: String): SheetEntity?

    @Update
    suspend fun update(sheetEntity: SheetEntity)

    @Insert
    suspend fun insert(vararg sheetEntities: SheetEntity)
}
