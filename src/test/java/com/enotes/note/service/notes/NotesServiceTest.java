package com.enotes.note.service.notes;

import com.enotes.note.repository.notes.NotesRepository;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

public class NotesServiceTest {

  @Test
  public void testCreateNote() {
    final NotesRepository notesRepository = Mockito.mock(NotesRepository.class);
    final NotesService notesService = new NotesService(notesRepository);

    final NoteId noteId = notesService.createNote("user 1", new Note("note title", "note body"));

    assertNotNull(noteId);
  }
}
