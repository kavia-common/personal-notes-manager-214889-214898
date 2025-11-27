package org.example.app

import org.example.app.model.Note
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class NoteModelTest {
    @Test
    fun newGeneratesId() {
        val n = Note.new()
        assertTrue(n.id.isNotBlank())
    }
}
