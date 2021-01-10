package com.enotes.note.application.notes;

import com.enotes.note.application.PathBuilder;
import com.enotes.note.application.Utils;
import com.enotes.note.service.notes.Note;
import com.enotes.note.service.notes.NoteId;
import com.enotes.note.service.notes.NotesService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = NotesController.class)
@EnableAutoConfiguration(exclude = { EnableWebSecurity.class })
class NotesControllerTest {

  @Autowired
  private MockMvc mvc;

  @MockBean
  private NotesService notesService;

  private ObjectMapper objectMapper = new ObjectMapper();

  @WithMockUser
  @Test
  public void testCreateNote() throws Exception {
    final Note note = new Note(null, "title", "body");

    final NoteId expectedNoteId = new NoteId("2233");
    Mockito.when(notesService.createNote(Mockito.any(), Mockito.eq(note))).thenReturn(expectedNoteId);

    final ResultActions resultActions = postRequest(note, "");

    final MvcResult mvcResult = resultActions
        .andDo(print())
        .andExpect(status().isOk())
        .andReturn();

    final NoteId noteId = fromJson(mvcResult.getResponse().getContentAsString(), NoteId.class);

    assertEquals(expectedNoteId.getId(), noteId.getId());
  }

  @WithMockUser
  @Test
  public void testGetNote() throws Exception {
    final Note note = new Note(null, "title", "body");

    final String id = "2233";
    Mockito.when(notesService.getNote(Mockito.eq(id))).thenReturn(note);

    final ResultActions resultActions = mvc.perform(get(PathBuilder.buildPath(PathBuilder.NOTES, id))
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON));

    final MvcResult mvcResult = resultActions
        .andDo(print())
        .andExpect(status().isOk())
        .andReturn();

    final Note actualNote = fromJson(mvcResult.getResponse().getContentAsString(), Note.class);

    assertEquals(note.getTitle(), actualNote.getTitle());
    assertEquals(note.getBody(), actualNote.getBody());
  }

  @WithMockUser
  @Test
  public void testGetAllNoteForUser() throws Exception {
    final String id = "note 1";
    final String noteTitle = "note title";
    final String noteBody = "note body";

    final String userId = "user id";
    final String id2 = "note 2";
    Mockito.when(notesService.getAllNotes(Mockito.any()))
        .thenReturn(Arrays.asList(new Note(id, noteTitle, noteBody), new Note(id2, noteTitle, noteBody)));

    final ResultActions resultActions = mvc.perform(get(PathBuilder.buildPath(PathBuilder.NOTES, ""))
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON));

    final MvcResult mvcResult = resultActions
        .andDo(print())
        .andExpect(status().isOk())
        .andReturn();

    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    final List<Note> notes = (List<Note>)objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
        new TypeReference<Collection<Note>>() {
        });

    assertEquals(2, notes.size());
    assertEquals(id, notes.get(0).getId());
    assertEquals(id2, notes.get(1).getId());
  }

  private ResultActions postRequest(final Object ob, String path) throws Exception {
    return Utils.postRequest(mvc, ob, PathBuilder.NOTES, path, objectMapper);
  }

  public <T> T fromJson(String string, Class<T> clazz)
      throws IOException {

    return Utils.fromJson(objectMapper, string, clazz);
  }
}
