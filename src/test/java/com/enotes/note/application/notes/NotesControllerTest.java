package com.enotes.note.application.notes;

import com.enotes.note.application.PathBuilder;
import com.enotes.note.application.Utils;
import com.enotes.note.service.notes.Note;
import com.enotes.note.service.notes.NoteId;
import com.enotes.note.service.notes.NotesService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
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

  @WithUserDetails
  @Test
  public void createNote() throws Exception {
    final Note note = new Note("title", "body");

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

  private ResultActions postRequest(final Object ob, String path) throws Exception {
    return Utils.postRequest(mvc, ob, PathBuilder.NOTES, path, objectMapper);
  }

  public <T> T fromJson(String string, Class<T> clazz)
      throws IOException {

    return Utils.fromJson(objectMapper, string, clazz);
  }
}
