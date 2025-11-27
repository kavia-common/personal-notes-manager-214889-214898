package org.example.app.model

import java.util.UUID

/**
PUBLIC_INTERFACE
Represents a Note entity with id, title, and body.
 */
data class Note(
    val id: String,
    val title: String,
    val body: String
) {
    companion object {
        // PUBLIC_INTERFACE
        fun new(): Note {
            /** Create a new empty note with a random id. */
            return Note(
                id = UUID.randomUUID().toString(),
                title = "",
                body = ""
            )
        }
    }
}
