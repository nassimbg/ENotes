package com.enotes.note.application.notes;

import com.enotes.note.application.PathBuilder;
import com.enotes.note.service.notes.Note;
import com.enotes.note.service.notes.NoteId;
import com.enotes.note.service.notes.NotesService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
  public Note getNote(@PathVariable String id) {
    return this.notesService.getNote(id);
  }
}
