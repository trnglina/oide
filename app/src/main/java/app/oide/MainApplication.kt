package app.oide

import android.app.Application
import androidx.room.Room
import app.oide.data.Database
import app.oide.data.FileRepository
import app.oide.data.SheetRepository
import app.oide.service.EditorService

class MainApplication : Application() {
    lateinit var database: Database

    lateinit var fileRepository: FileRepository
    lateinit var sheetRepository: SheetRepository

    lateinit var editorService: EditorService

    override fun onCreate() {
        super.onCreate()

        database = Room.databaseBuilder(applicationContext, Database::class.java, "database.db").build()

        fileRepository = FileRepository()
        sheetRepository = SheetRepository(database)

        editorService = EditorService(fileRepository, sheetRepository)
    }
}
