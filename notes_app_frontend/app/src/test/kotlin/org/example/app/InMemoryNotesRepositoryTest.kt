package org.example.app

import org.example.app.data.InMemoryNotesRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class InMemoryNotesRepositoryTest {
    @Test
    fun addAndGetWorks() {
        val repo = InMemoryNotesRepository(seed = false)
        val n = repo.add("Title", "Body")
        assertEquals("Title", repo.get(n.id)?.title)
    }
}
