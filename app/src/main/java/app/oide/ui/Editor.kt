package app.oide.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Redo
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.FileOpen
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.SaveAs
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.oide.R
import app.oide.extension.plus
import kotlinx.coroutines.delay

class CreateDocumentContract() : ActivityResultContracts.CreateDocument("text/*")

class OpenDocumentContract() : ActivityResultContracts.OpenDocument() {
    override fun createIntent(context: Context, input: Array<String>): Intent {
        return super.createIntent(context, input).addFlags(
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
    }
}

@Composable
fun ToolbarButton(
    onClick: () -> Unit,
    content: @Composable (RowScope.() -> Unit),
) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(48.dp, 32.dp),
        colors = ButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        contentPadding = PaddingValues(6.dp),
        shape = RoundedCornerShape(8.dp),
        content = content,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Editor(
    textFieldState: TextFieldState,
    filePath: Uri?,

    onSaveAs: (uri: Uri) -> Unit,
    onOpen: (uri: Uri) -> Unit,
    onSave: () -> Unit,
    onNew: () -> Unit,
) {
    val editorFocusRequester = remember { FocusRequester() }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = CreateDocumentContract()
    ) { result ->
        result?.let { uri -> onSaveAs(uri) }
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = OpenDocumentContract()
    ) { result ->
        result?.let { uri -> onOpen(uri) }
    }

    Surface(color = MaterialTheme.colorScheme.surface) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            bottomBar = {
                Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(BottomAppBarDefaults.windowInsets)
                            .height(IntrinsicSize.Min)
                            .padding(6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(
                                6.dp,
                                Alignment.Start
                            ),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            ToolbarButton(
                                onClick = {
                                    onSave()
                                    openDocumentLauncher.launch(arrayOf("text/*"))
                                }
                            ) {
                                Icon(
                                    Icons.Outlined.FileOpen,
                                    stringResource(R.string.editor_toolbar_open),
                                )
                            }

                            when (filePath) {
                                null ->
                                    ToolbarButton(
                                        onClick = {
                                            createDocumentLauncher.launch("")
                                        }
                                    ) {
                                        Icon(
                                            Icons.Outlined.Save,
                                            stringResource(R.string.editor_toolbar_save),
                                        )
                                    }

                                else ->
                                    ToolbarButton(
                                        onClick = {
                                            createDocumentLauncher.launch("")
                                        }
                                    ) {
                                        Icon(
                                            Icons.Outlined.SaveAs,
                                            stringResource(R.string.editor_toolbar_save_as),
                                        )
                                    }

                            }

                            ToolbarButton(
                                onClick = {
                                    onSave()
                                    onNew()
                                }
                            ) {
                                Icon(
                                    Icons.Outlined.Add,
                                    stringResource(R.string.editor_toolbar_new),
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(
                                6.dp,
                                Alignment.End
                            ),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            ToolbarButton(
                                onClick = { textFieldState.undoState.undo() }
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Outlined.Undo,
                                    stringResource(R.string.editor_toolbar_undo)
                                )
                            }

                            ToolbarButton(
                                onClick = {
                                    textFieldState.undoState.redo()
                                }
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Outlined.Redo,
                                    stringResource(R.string.editor_toolbar_redo)
                                )
                            }
                        }
                    }
                }
            },
        ) { innerPadding ->
            TextEditor(
                contentPadding = PaddingValues(16.dp) + innerPadding,
                state = textFieldState,
                focusRequester = editorFocusRequester,
            )
        }
    }

    LaunchedEffect(textFieldState.text) {
        delay(500)
        onSave()
    }
}
