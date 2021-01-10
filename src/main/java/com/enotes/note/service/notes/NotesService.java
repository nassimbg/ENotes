package com.enotes.note.service.notes;

import com.enotes.note.repository.notes.NotesDetails;
import com.enotes.note.repository.notes.NotesRepository;
import com.enotes.note.repository.notes.UserId;

import java.util.UUID;

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
   * @param id that is needed to fetch the corresponding note
   * @return the note corresponding to this id
   */
  public Note getNote(final String id) {
    if (id == null || id.isEmpty()) {
      throw new NotesException("Note id: {" + id + "} is invalid");
    }

    return notesRepository.findById(id)
        .map(note -> new Note(note.getTitle(), note.getBody()))
        .orElseThrow(() -> new NotesException("Note with id: {" + id + "} is not found"));
  }

  private NotesDetails createNoteDetails(final Object userId, final Note note) {
    final UserId uId = new UserId((String) userId);
    final String noteId = UUID.randomUUID().toString();
    return new NotesDetails
        .Builder(noteId, uId)
        .withTitle(note.getTitle())
        .withBody(note.getBody())
        .build();
  }
}
