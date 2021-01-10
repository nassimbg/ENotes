package com.enotes.note.service.notes;

import com.enotes.note.repository.notes.NotesDetails;
import com.enotes.note.repository.notes.NotesRepository;
import com.enotes.note.repository.notes.UserId;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class NotesServiceTest {

  @Test
  public void testCreateNote() {
    final NotesRepository notesRepository = Mockito.mock(NotesRepository.class);
    final NotesService notesService = new NotesService(notesRepository);

    final NoteId noteId = notesService.createNote("note 1", new Note("note title", "note body"));

    assertNotNull(noteId);
  }

  @Test
  public void testGetNote() {
    final NotesRepository notesRepository = Mockito.mock(NotesRepository.class);
    final String id = "note 1";
    final String noteTitle = "note title";
    final String noteBody = "note body";
    final NotesDetails expectedNote = new NotesDetails.Builder(id, new UserId("user 1"))
        .withTitle(noteTitle)
        .withBody(noteBody)
        .build();

    Mockito.when(notesRepository.findById(id)).thenReturn(Optional.of(expectedNote));
    final NotesService notesService = new NotesService(notesRepository);

    final Note note = notesService.getNote(id);

    assertNotNull(noteTitle, note.getTitle());
    assertNotNull(noteBody, note.getBody());
  }

  @Test
  public void testInvalidNoteId() {
    final NotesRepository notesRepository = Mockito.mock(NotesRepository.class);
    final NotesService notesService = new NotesService(notesRepository);


    Exception exception = assertThrows(NotesException.class, () -> {
      notesService.getNote("");
    });

    String expectedMessage = "Note id: {" + "" + "} is invalid";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void testInvalidNotsdeId() {
    final NotesRepository notesRepository = Mockito.mock(NotesRepository.class);
    final String id = "note 1";
    final NotesService notesService = new NotesService(notesRepository);


    Exception exception = assertThrows(NotesException.class, () -> {
      notesService.getNote(id);
    });

    String expectedMessage = "Note with id: {" + id + "} is not found";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }
}
