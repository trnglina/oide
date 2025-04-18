package app.oide

import android.app.Application
import app.oide.data.EditorStateRepository
import app.oide.data.FileRepository

class MainApplication : Application() {
    val fileRepository = FileRepository()
    val editorStateRepository = EditorStateRepository(fileRepository)
}
