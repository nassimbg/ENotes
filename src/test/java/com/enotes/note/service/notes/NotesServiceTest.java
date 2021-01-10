package com.enotes.note.service.notes;

import com.enotes.note.repository.notes.NotesDetails;
import com.enotes.note.repository.notes.NotesRepository;
import com.enotes.note.repository.notes.UserId;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class NotesServiceTest {

  @Test
  public void testCreateNote() {
    final NotesRepository notesRepository = Mockito.mock(NotesRepository.class);
    final NotesService notesService = new NotesService(notesRepository);

    final NoteId noteId = notesService.createNote("note 1", new Note(null,"note title", "note body"));

    assertNotNull(noteId);
  }

  @Test
  public void testGetNote() {
    final NotesRepository notesRepository = Mockito.mock(NotesRepository.class);
    final String id = "note 1";
    final String noteTitle = "note title";
    final String noteBody = "note body";
    final UserId userId = new UserId("user 1");
    final NotesDetails expectedNote = new NotesDetails.Builder(id, userId)
        .withTitle(noteTitle)
        .withBody(noteBody)
        .build();

    Mockito.when(notesRepository.findById(id)).thenReturn(Optional.of(expectedNote));
    final NotesService notesService = new NotesService(notesRepository);

    final Note note = notesService.getNote(userId.getId(), id);

    assertNotNull(noteTitle, note.getTitle());
    assertNotNull(noteBody, note.getBody());
  }

  @Test
  public void testInvalidNoteId() {
    final NotesRepository notesRepository = Mockito.mock(NotesRepository.class);
    final NotesService notesService = new NotesService(notesRepository);


    Exception exception = assertThrows(NotesException.class, () -> {
      notesService.getNote("user 1", "");
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
      notesService.getNote("user 1", id);
    });

    String expectedMessage = "Note with id: {" + id + "} is not found";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void testGetAllNotesForUser() {
    final NotesRepository notesRepository = Mockito.mock(NotesRepository.class);
    final String id = "note 1";
    final String noteTitle = "note title";
    final String noteBody = "note body";
    final UserId userId = new UserId("user 1");
    final NotesDetails expectedNote1 = new NotesDetails.Builder(id, userId)
        .withTitle(noteTitle)
        .withBody(noteBody)
        .build();

    final String id2 = "note id 2";
    final NotesDetails expectedNote2 = new NotesDetails.Builder(id2, userId)
        .withTitle(noteTitle)
        .withBody(noteBody)
        .build();

    Mockito.when(notesRepository.findAll(userId)).thenReturn(Arrays.asList(expectedNote1, expectedNote2));
    final NotesService notesService = new NotesService(notesRepository);

    final List<Note> note = (List<Note>) notesService.getAllNotes(userId.getId());
    assertEquals(2, note.size());

    assertNotNull(id, note.get(0).getId());
    assertNotNull(id2, note.get(1).getId());
  }
}
