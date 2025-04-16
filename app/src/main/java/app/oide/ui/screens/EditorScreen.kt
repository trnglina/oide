package app.oide.ui.screens

import android.content.Context
import android.content.Intent
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.oide.R
import app.oide.data.FileSystemDocumentRepository
import app.oide.data.UserDataRepository
import app.oide.plus
import app.oide.ui.Editor
import app.oide.ui.theme.AppTheme
import app.oide.userDataStore
import kotlinx.coroutines.delay

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
fun EditorScreen(viewModel: EditorViewModel) {
    val state = viewModel.state.collectAsState().value
    val textFieldState = viewModel.textFieldState

    val editorFocusRequester = remember { FocusRequester() }

    val saveFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/*")
    ) { result ->
        result?.let { uri -> viewModel.saveAs(uri) }
    }

    val openFileLauncher = rememberLauncherForActivityResult(
        contract = object : ActivityResultContracts.OpenDocument() {
            override fun createIntent(context: Context, input: Array<String>): Intent {
                return super.createIntent(context, input).addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            }
        }
    ) { result ->
        result?.let { uri -> viewModel.loadFromFile(uri) }
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
                                    viewModel.saveCurrent()
                                    viewModel.clear()
                                }
                            ) {
                                Icon(
                                    Icons.Outlined.Add,
                                    stringResource(R.string.editor_toolbar_new),
                                )
                            }

                            when (state) {
                                is EditorState.Unsaved ->
                                    ToolbarButton(
                                        onClick = {
                                            saveFileLauncher.launch("")
                                        }
                                    ) {
                                        Icon(
                                            Icons.Outlined.Save,
                                            stringResource(R.string.editor_toolbar_save),
                                        )
                                    }

                                is EditorState.Saved ->
                                    ToolbarButton(
                                        onClick = {
                                            saveFileLauncher.launch("")
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
                                    viewModel.saveCurrent()
                                    openFileLauncher.launch(arrayOf("text/*"))
                                }
                            ) {
                                Icon(
                                    Icons.Outlined.FileOpen,
                                    stringResource(R.string.editor_toolbar_open),
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
            Editor(
                contentPadding = PaddingValues(16.dp) + innerPadding,
                state = textFieldState,
                focusRequester = editorFocusRequester,
            )
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.loadFromLastFile()
        editorFocusRequester.requestFocus()
    }

    LaunchedEffect(textFieldState.text) {
        delay(500)
        viewModel.saveCurrent()
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun EditorScreenPreview() {
    val context = LocalContext.current
    val viewModel: EditorViewModel = viewModel(
        factory = EditorViewModel.Factory(
            FileSystemDocumentRepository(context.contentResolver),
            UserDataRepository(context.userDataStore),
        )
    )

    AppTheme {
        EditorScreen(viewModel)
    }
}
