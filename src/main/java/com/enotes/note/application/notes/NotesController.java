package com.enotes.note.application.notes;

import com.enotes.note.application.PathBuilder;
import com.enotes.note.service.notes.Note;
import com.enotes.note.service.notes.NoteId;
import com.enotes.note.service.notes.NotesService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping(PathBuilder.NOTES)
public class NotesController {

  private static final String NOTE_ID = "id";

  private final NotesService notesService;

  @Autowired
  public NotesController(NotesService notesService) {
    this.notesService = notesService;
  }

  @PostMapping
  public NoteId createNode(Authentication authentication, @RequestBody Note note) {
    return this.notesService.createNote(authentication.getPrincipal(), note);
  }

  @GetMapping("{" + NOTE_ID + "}")
  public Note getNote(Authentication authentication, @PathVariable String id) {
    return this.notesService.getNote(authentication.getPrincipal(), id);
  }

  @GetMapping
  public Collection<Note> getAllNotes(Authentication authentication) {
    return this.notesService.getAllNotes(authentication.getPrincipal());
  }

  @DeleteMapping("{" + NOTE_ID + "}")
  @ResponseStatus(value = HttpStatus.NO_CONTENT)
  public void deleteNote(Authentication authentication, @PathVariable String id) {
    this.notesService.deleteNote(authentication.getPrincipal(), id);
  }
}
