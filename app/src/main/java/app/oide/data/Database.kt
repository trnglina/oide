package app.oide.data

import androidx.room.Database
import androidx.room.RoomDatabase
import app.oide.data.entity.SheetDao
import app.oide.data.entity.SheetEntity

@Database(version = 1, entities = [SheetEntity::class])
abstract class Database : RoomDatabase() {
    abstract fun sheets(): SheetDao
}
