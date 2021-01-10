package com.enotes.note.service.notes;

import com.enotes.note.repository.notes.NotesDetails;
import com.enotes.note.repository.notes.NotesRepository;
import com.enotes.note.repository.notes.UserId;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

public class NotesService {

  private final NotesRepository notesRepository;

  public NotesService(final NotesRepository notesRepository) {
    this.notesRepository = notesRepository;
  }

  /**
   * Creates a note for the {@code user}
   * @param userId user id for which the note belongs
   * @param note the note data
   * @return the note id of the create note
   */
  public NoteId createNote(final Object userId, final Note note) {
    final NotesDetails noteDetails = createNoteDetails(userId, note);

    final String id = noteDetails.getId();
    notesRepository.put(id, noteDetails);

    return new NoteId(id);
  }

  /**
   * Retrieves the Note that has an id {@code id}
   * @param userId user id for which the note belongs
   * @param id that is needed to fetch the corresponding note
   * @return the note corresponding to this id
   */
  public Note getNote(final Object userId,final String id) {
    if (id == null || id.isEmpty()) {
      throw new NotesException("Note id: {" + id + "} is invalid");
    }
    final UserId uId = createUserId(userId);

    return notesRepository.findById(id)
        .filter(noteDetails -> noteDetails.getUserId().equals(uId))
        .map(note -> new Note(note.getId(), note.getTitle(), note.getBody()))
        .orElseThrow(() -> new NotesException("Note with id: {" + id + "} is not found"));
  }

  /**
   * Retrieves all notes for the user with user id {@code userId}
   * @param userId user id for which the notes belongs
   * @return all notes for the user with user id {@code userId}
   */
  public Collection<Note> getAllNotes(final Object userId) {
    final UserId uId = createUserId(userId);
    if (uId.getId() == null || uId.getId().isEmpty()) {
      throw new NotesException("user id: {" + userId + "} is invalid");
    }

    return notesRepository.findAll(uId)
        .stream()
        .map(notesDetails -> new Note(notesDetails.getId(), notesDetails.getTitle(), notesDetails.getBody()))
        .collect(Collectors.toList());
  }

  /**
   * Deletes a note with id {@code id}
   * @param id for the note to delete
   */
  public void deleteNote(final Object userId, final String id) {
    final UserId uId = createUserId(userId);

    final NotesDetails note = notesRepository.findById(id)
        .filter(noteDetails -> noteDetails.getUserId().equals(uId))
        .orElseThrow(() -> new NotesException("Note with id: {" + id + "} is not found"));

    notesRepository.delete(note.getId());
  }

  private UserId createUserId(final Object userId) {
    return new UserId((String) userId);
  }

  private NotesDetails createNoteDetails(final Object userId, final Note note) {
    final UserId uId = createUserId(userId);
    final String noteId = UUID.randomUUID().toString();
    return new NotesDetails
        .Builder(noteId, uId)
        .withTitle(note.getTitle())
        .withBody(note.getBody())
        .build();
  }

}
