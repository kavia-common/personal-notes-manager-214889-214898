package org.example.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.example.app.data.InMemoryNotesRepository
import org.example.app.model.Note
import org.example.app.ui.Delete
import org.example.app.ui.Edit
import org.example.app.ui.Add
import org.example.app.ui.ArrowBack

/**
PUBLIC_INTERFACE
MainActivity is the app entry point. It sets up Compose, theme, and hosts the navigation between:
- Notes List
- Create/Edit Note
- View Note
It wires an in-memory repository for basic functionality.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OceanProfessionalNotesApp()
        }
    }
}

private val MonoColorScheme = lightColorScheme(
    // Core grayscale palette (accessible contrast)
    primary = Color(0xFF111111),        // used for key accents (FAB, primary buttons)
    secondary = Color(0xFF2B2B2B),      // secondary accents if needed
    error = Color(0xFF222222),          // keep grayscale; dialogs text uses red tint removed
    background = Color(0xFF0A0A0A),     // app background (near black)
    surface = Color(0xFF1F1F1F),        // cards and surfaces
    onPrimary = Color(0xFFFFFFFF),      // text on primary
    onSecondary = Color(0xFFE5E5E5),    // text/icons on secondary surfaces
    onBackground = Color(0xFFE5E5E5),   // primary text on background
    onSurface = Color(0xFFE5E5E5),      // text on surfaces
)

@Composable
private fun MonoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MonoColorScheme,
        typography = MaterialTheme.typography
    ) {
        // Root surface to ensure background fills the window
        Surface(color = MaterialTheme.colorScheme.background) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OceanProfessionalNotesApp() {
    MonoTheme {
        val repo = remember { InMemoryNotesRepository(seed = true) }
        val screenState = remember { mutableStateOf<Screen>(Screen.List) }
        val selectedNote = remember { mutableStateOf<Note?>(null) }
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()

        // Scaffold hosting top bar and FAB based on screen
        Scaffold(
            topBar = {
                TopBar(
                    screen = screenState.value,
                    onBack = {
                        screenState.value = Screen.List
                        selectedNote.value = null
                    }
                )
            },
            floatingActionButton = {
                AnimatedVisibility(visible = screenState.value == Screen.List) {
                    FloatingActionButton(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        onClick = { screenState.value = Screen.CreateEdit(null) }
                    ) {
                        Icon(
                            imageVector = Add,
                            contentDescription = "Add note",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { padding ->
            when (val screen = screenState.value) {
                is Screen.List -> NotesListScreen(
                    modifier = Modifier.padding(padding),
                    repo = repo,
                    onOpen = { note ->
                        selectedNote.value = note
                        screenState.value = Screen.View
                    }
                )
                is Screen.CreateEdit -> CreateEditScreen(
                    modifier = Modifier.padding(padding),
                    initial = screen.note,
                    onSave = { saved ->
                        if (screen.note == null) {
                            repo.add(saved.title, saved.body)
                            scope.launch { snackbarHostState.showSnackbar("Note created") }
                        } else {
                            repo.update(saved)
                            scope.launch { snackbarHostState.showSnackbar("Note updated") }
                        }
                        screenState.value = Screen.List
                    },
                    onCancel = {
                        screenState.value = if (screen.note == null && selectedNote.value != null) Screen.View else Screen.List
                    }
                )
                is Screen.View -> ViewNoteScreen(
                    modifier = Modifier.padding(padding),
                    note = selectedNote.value,
                    onEdit = {
                        selectedNote.value?.let {
                            screenState.value = Screen.CreateEdit(it)
                        }
                    },
                    onDelete = {
                        selectedNote.value?.let {
                            repo.delete(it.id)
                            scope.launch { snackbarHostState.showSnackbar("Note deleted") }
                        }
                        selectedNote.value = null
                        screenState.value = Screen.List
                    }
                )
            }
        }
    }
}

private sealed class Screen {
    data object List : Screen()
    data object View : Screen()
    data class CreateEdit(val note: Note?) : Screen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(screen: Screen, onBack: () -> Unit) {
    val title = when (screen) {
        is Screen.List -> "My Notes"
        is Screen.View -> "View Note"
        is Screen.CreateEdit -> if (screen.note == null) "New Note" else "Edit Note"
    }
    TopAppBar(
        title = { Text(title, color = MaterialTheme.colorScheme.onSurface) },
        navigationIcon = {
            if (screen != Screen.List) {
                IconButton(onClick = onBack) {
                    Icon(
                        ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    )
}

@Composable
private fun NotesListScreen(
    modifier: Modifier = Modifier,
    repo: InMemoryNotesRepository,
    onOpen: (Note) -> Unit
) {
    val notes = remember { mutableStateListOf<Note>() }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        notes.clear()
        notes.addAll(repo.list())
        loading = false
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Spacer(Modifier.height(4.dp))

        if (loading) {
            LoadingState()
            return@Column
        }

        if (notes.isEmpty()) {
            EmptyState(
                title = "No notes yet",
                subtitle = "Tap the + button to create your first note."
            )
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(notes, key = { it.id }) { note ->
                NoteCard(note = note, onClick = { onOpen(note) })
            }
        }
    }
}

@Composable
private fun NoteCard(note: Note, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, ambientColor = Color.Black.copy(alpha = 0.2f))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = note.title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = note.body,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.80f),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateEditScreen(
    modifier: Modifier = Modifier,
    initial: Note?,
    onSave: (Note) -> Unit,
    onCancel: () -> Unit
) {
    var title by remember { mutableStateOf(initial?.title.orEmpty()) }
    var body by remember { mutableStateOf(initial?.body.orEmpty()) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = {
                title = it
                if (error != null) error = null
            },
            label = { Text("Title", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)) },
            isError = error != null,
            supportingText = { if (error != null) Text(error!!, color = MaterialTheme.colorScheme.onSurface) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = body,
            onValueChange = { body = it },
            label = { Text("Body", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)) },
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f),
            minLines = 6,
        )
        Spacer(Modifier.height(12.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(onClick = onCancel) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.weight(1f))
            TextButton(
                onClick = {
                    if (title.isBlank()) {
                        error = "Title is required"
                        return@TextButton
                    }
                    val toSave = (initial ?: Note.new()).copy(title = title.trim(), body = body.trim())
                    onSave(toSave)
                }
            ) {
                Text("Save", color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ViewNoteScreen(
    modifier: Modifier = Modifier,
    note: Note?,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showConfirm by remember { mutableStateOf(false) }

    if (note == null) {
        EmptyState(title = "Note not found", subtitle = "Return to list and try again.")
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = note.title,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onEdit) {
                Icon(Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurface)
            }
            IconButton(onClick = { showConfirm = true }) {
                Icon(Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f))
            }
        }
        Divider(
            Modifier.padding(vertical = 8.dp),
            color = Color(0xFF2B2B2B)
        )
        Text(
            text = note.body.ifBlank { "No content" },
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyLarge
        )
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Delete note?", color = MaterialTheme.colorScheme.onSurface) },
            text = { Text("This action cannot be undone.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)) },
            confirmButton = {
                TextButton(onClick = {
                    showConfirm = false
                    onDelete()
                }) { Text("Delete", color = MaterialTheme.colorScheme.onSurface) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) { Text("Cancel", color = MaterialTheme.colorScheme.onSurface) }
            }
        )
    }
}

@Composable
private fun LoadingState(height: Dp = 160.dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("Loading...", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
    }
}

@Composable
private fun EmptyState(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(8.dp))
        Text(subtitle, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
    }
}
