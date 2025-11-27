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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.example.app.data.InMemoryNotesRepository
import org.example.app.data.SettingsRepository
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
It wires an in-memory repository for basic functionality and persists accessibility preferences.
 */
class MainActivity : ComponentActivity() {

    private lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsRepository = SettingsRepository(applicationContext)

        setContent {
            OceanProfessionalNotesApp(settingsRepository)
        }
    }
}

private val MonoColorScheme = lightColorScheme(
    // Core grayscale palette (accessible contrast baseline)
    primary = Color(0xFF111111),
    secondary = Color(0xFF2B2B2B),
    error = Color(0xFF222222),
    background = Color(0xFF0A0A0A),
    surface = Color(0xFF1F1F1F),
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFE5E5E5),
    onBackground = Color(0xFFE5E5E5),
    onSurface = Color(0xFFE5E5E5),
)

// High-contrast variant: pure black/white for max contrast, thicker borders implied by components,
// higher emphasis on focus via strong dividers and text opacities 1.0.
private val HighContrastScheme = lightColorScheme(
    primary = Color(0xFF000000),
    secondary = Color(0xFF000000),
    error = Color(0xFF000000),
    background = Color(0xFF000000),
    surface = Color(0xFF111111),
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFFFFFFF),
    onBackground = Color(0xFFFFFFFF),
    onSurface = Color(0xFFFFFFFF),
)

@Composable
private fun AppTheme(highContrast: Boolean, content: @Composable () -> Unit) {
    val scheme = if (highContrast) HighContrastScheme else MonoColorScheme
    MaterialTheme(colorScheme = scheme, typography = MaterialTheme.typography) {
        Surface(color = MaterialTheme.colorScheme.background) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OceanProfessionalNotesApp(settingsRepository: SettingsRepository) {
    val repo = remember { InMemoryNotesRepository(seed = true) }
    val screenState = remember { mutableStateOf<Screen>(Screen.List) }
    val selectedNote = remember { mutableStateOf<Note?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Observe high contrast flag. Default false.
    val highContrast by settingsRepository.highContrastEnabled().collectAsState(initial = false)

    AppTheme(highContrast = highContrast) {
        Scaffold(
            topBar = {
                TopBar(
                    screen = screenState.value,
                    highContrast = highContrast,
                    onToggleHighContrast = {
                        scope.launch {
                            settingsRepository.setHighContrastEnabled(!highContrast)
                        }
                    },
                    onBack = {
                        screenState.value = Screen.List
                        selectedNote.value = null
                    }
                )
            },
            floatingActionButton = {
                AnimatedVisibility(visible = screenState.value == Screen.List) {
                    // Flat FAB: remove elevation and shadow; add clear contrasting icon
                    FloatingActionButton(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        onClick = { screenState.value = Screen.CreateEdit(null) },
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp,
                            focusedElevation = 0.dp,
                            hoveredElevation = 0.dp
                        )
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
                    highContrast = highContrast,
                    onOpen = { note ->
                        selectedNote.value = note
                        screenState.value = Screen.View
                    }
                )
                is Screen.CreateEdit -> CreateEditScreen(
                    modifier = Modifier.padding(padding),
                    initial = screen.note,
                    highContrast = highContrast,
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
                    highContrast = highContrast,
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
private fun TopBar(
    screen: Screen,
    highContrast: Boolean,
    onToggleHighContrast: () -> Unit,
    onBack: () -> Unit
) {
    val title = when (screen) {
        is Screen.List -> "My Notes"
        is Screen.View -> "View Note"
        is Screen.CreateEdit -> if (screen.note == null) "New Note" else "Edit Note"
    }

    var overflowOpen by remember { mutableStateOf(false) }

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
        },
        actions = {
            // Simple overflow with a single toggle entry: "High contrast"
            Box {
                IconButton(onClick = { overflowOpen = !overflowOpen }) {
                    // Use a simple 3-dot text as a flat overflow indicator to avoid missing icons
                    Text("⋮", color = MaterialTheme.colorScheme.onSurface)
                }
                DropdownMenu(expanded = overflowOpen, onDismissRequest = { overflowOpen = false }) {
                    DropdownMenuItem(
                        text = {
                            val stateLabel = if (highContrast) "On" else "Off"
                            Text("High contrast: $stateLabel")
                        },
                        onClick = {
                            overflowOpen = false
                            onToggleHighContrast()
                        }
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        ),
        scrollBehavior = null
    )
}

@Composable
private fun NotesListScreen(
    modifier: Modifier = Modifier,
    repo: InMemoryNotesRepository,
    highContrast: Boolean,
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
                NoteCard(note = note, highContrast = highContrast, onClick = { onOpen(note) })
            }
        }
    }
}

@Composable
private fun NoteCard(note: Note, highContrast: Boolean, onClick: () -> Unit) {
    // Flat card: no elevation. In high-contrast: thicker border and full opacity text.
    val borderWidth = if (highContrast) 2.dp else 1.dp
    val borderColor = if (highContrast) Color.White else Color(0xFF2B2B2B)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(borderWidth, borderColor)
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
                color = if (highContrast) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.80f),
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
    highContrast: Boolean,
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
            label = {
                Text(
                    "Title",
                    color = if (highContrast) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                )
            },
            isError = error != null,
            supportingText = {
                if (error != null) Text(
                    error!!,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = body,
            onValueChange = { body = it },
            label = {
                Text(
                    "Body",
                    color = if (highContrast) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                )
            },
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
    highContrast: Boolean,
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
                Icon(
                    Delete,
                    contentDescription = "Delete",
                    tint = if (highContrast) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                )
            }
        }
        Divider(
            Modifier.padding(vertical = 8.dp),
            color = if (highContrast) Color.White else Color(0xFF2B2B2B),
            thickness = if (highContrast) 2.dp else 1.dp
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
            text = {
                Text(
                    "This action cannot be undone.",
                    color = if (highContrast) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showConfirm = false
                    onDelete()
                }) { Text("Delete", color = MaterialTheme.colorScheme.onSurface) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) { Text("Cancel", color = MaterialTheme.colorScheme.onSurface) }
            },
            containerColor = MaterialTheme.colorScheme.surface
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
        Text(
            "Loading...",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
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
        Text(
            subtitle,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
    }
}
