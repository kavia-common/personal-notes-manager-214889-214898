package org.example.app.data

import org.example.app.model.Note
import java.util.concurrent.ConcurrentHashMap

/**
PUBLIC_INTERFACE
InMemoryNotesRepository provides basic CRUD operations for notes stored in-memory.
No external services are used. Optionally seeds sample notes.
 */
class InMemoryNotesRepository(seed: Boolean = false) {

    private val map = ConcurrentHashMap<String, Note>()

    init {
        if (seed) {
            val samples = listOf(
                Note.new().copy(title = "Welcome", body = "This is your notes app. Tap + to create a note."),
                Note.new().copy(title = "Ideas", body = "• Grocery list\n• Project thoughts\n• Books to read"),
                Note.new().copy(title = "Tips", body = "Use the + button to create a note.\nLong notes are collapsed in the list.")
            )
            samples.forEach { map[it.id] = it }
        }
    }

    // PUBLIC_INTERFACE
    fun list(): List<Note> {
        /** Return notes sorted by title then id for stable order. */
        return map.values.sortedWith(compareBy<Note> { it.title.lowercase() }.thenBy { it.id })
    }

    // PUBLIC_INTERFACE
    fun get(id: String): Note? {
        /** Return a note by id or null if not found. */
        return map[id]
    }

    // PUBLIC_INTERFACE
    fun add(title: String, body: String): Note {
        /** Create and store a new note. */
        val n = Note.new().copy(title = title, body = body)
        map[n.id] = n
        return n
    }

    // PUBLIC_INTERFACE
    fun update(note: Note): Boolean {
        /** Update an existing note; returns true if updated. */
        if (!map.containsKey(note.id)) return false
        map[note.id] = note
        return true
    }

    // PUBLIC_INTERFACE
    fun delete(id: String): Boolean {
        /** Delete a note by id; returns true if removed. */
        return map.remove(id) != null
    }

    // PUBLIC_INTERFACE
    fun clear() {
        /** Remove all notes (used in tests). */
        map.clear()
    }
}
