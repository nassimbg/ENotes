package com.enotes.note.repository.notes;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryNotesRepositoryTest {

  @Test
  public void testAddingKeyDoesntAlreadyExist() {
    final InMemoryNotesRepository persister = new InMemoryNotesRepository();

    final String noteId = "12345";
    final NotesDetails notesDetails = new NotesDetails.Builder(noteId, new UserId("john")).build();

    assertTrue(persister.putIfAbsent(noteId, notesDetails));

    assertEquals(notesDetails, persister.findById(noteId).get());
  }

  @Test
  public void testAddingKeyAlreadyExist() {
    final InMemoryNotesRepository persister = new InMemoryNotesRepository();

    final String noteId = "12345";
    final NotesDetails notesDetails = new NotesDetails.Builder(noteId, new UserId("john")).build();
    assertTrue(persister.putIfAbsent(noteId, notesDetails));
    assertFalse(persister.putIfAbsent(noteId, notesDetails));
  }

  @Test
  public void testGetNonExistingKey() {
    final InMemoryNotesRepository persister = new InMemoryNotesRepository();

    final String noteId = "12345";

    assertFalse(persister.findById(noteId).isPresent());
  }
}
