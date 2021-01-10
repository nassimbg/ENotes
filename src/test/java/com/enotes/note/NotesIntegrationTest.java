package com.enotes.note;

import com.enotes.note.application.PathBuilder;
import com.enotes.note.application.Utils;
import com.enotes.note.application.authentication.AuthenticationConfig;
import com.enotes.note.application.authentication.AuthenticationController;
import com.enotes.note.application.notes.NotesConfig;
import com.enotes.note.service.authentication.AuthenticationToken;
import com.enotes.note.service.authentication.User;
import com.enotes.note.service.notes.Note;
import com.enotes.note.service.notes.NoteId;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static com.enotes.note.application.Utils.asJsonString;
import static com.enotes.note.application.Utils.fromJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = NoteApplication.class)
@SpringJUnitConfig(classes = {AuthenticationConfig.class, NotesConfig.class})
@AutoConfigureMockMvc
@TestPropertySource(
    locations = "classpath:application-integrationtest.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class NotesIntegrationTest {

  @Autowired
  private MockMvc mvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  public void testCreateNote() throws Exception {
    final AuthenticationToken authenticationToken = getAuthenticationToken();

    final Note note = new Note("title", "body");
    createNoteAndAssert(note, authenticationToken);
  }

  @Test
  public void testGetNote() throws Exception {
    final AuthenticationToken authenticationToken = getAuthenticationToken();

    final Note note = new Note("title", "body");
    final NoteId noteId = createNoteAndAssert(note, authenticationToken);

    final ResultActions resultActions = mvc.perform(get(PathBuilder.buildPath(PathBuilder.NOTES, noteId.getId()))
        .header("Authorization", "Bearer " + authenticationToken.getAccessToken())
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON));

    final MvcResult mvcResult = resultActions
        .andDo(print())
        .andExpect(status().isOk())
        .andReturn();

    final Note actualNote = fromJson(objectMapper, mvcResult.getResponse().getContentAsString(), Note.class);

    assertEquals(note.getTitle(), actualNote.getTitle());
    assertEquals(note.getBody(), actualNote.getBody());
  }

  @Test
  public void testGetNoteShouldFailDueToInvalidity() throws Exception {
    final AuthenticationToken authenticationToken = getAuthenticationToken();

    final String noteId = UUID.randomUUID().toString();
    final ResultActions resultActions = mvc.perform(get(PathBuilder.buildPath(PathBuilder.NOTES,
        noteId))
        .header("Authorization", "Bearer " + authenticationToken.getAccessToken())
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON));

    final MvcResult mvcResult = resultActions
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andReturn();

    assertEquals("Note with id: {" + noteId + "} is not found", mvcResult.getResponse().getContentAsString());
  }

  private NoteId createNoteAndAssert(final Note note,
      final AuthenticationToken authenticationToken) throws Exception {

    final ResultActions resultActions = postRequest(mvc, note, "",
        authenticationToken.getAccessToken(), objectMapper);

    final MvcResult notesResult = resultActions
        .andDo(print())
        .andExpect(status().isOk())
        .andReturn();

    final NoteId noteId = fromJson(objectMapper, notesResult.getResponse().getContentAsString(), NoteId.class);

    assertNotNull(noteId.getId());

    return noteId;
  }

  private AuthenticationToken getAuthenticationToken() throws Exception {
    final User user = User.builder("John").withPassword("122334").build();

    final ResultActions postResponse = Utils.postRequest(mvc, user, PathBuilder.AUTHENTICATION,
        AuthenticationController.SIGNUP, objectMapper);
    final MvcResult mvcResult = postResponse
        .andDo(print())
        .andExpect(status().isOk())
        .andReturn();

    final AuthenticationToken authenticationToken = fromJson(objectMapper, mvcResult.getResponse().getContentAsString(), AuthenticationToken.class);
    return authenticationToken;
  }


  public static ResultActions postRequest(MockMvc mvc, final Object ob, String path, String accessToken, ObjectMapper objectMapper) throws Exception {
    return mvc.perform(post(PathBuilder.buildPath('/', PathBuilder.NOTES, path))
        .header("Authorization", "Bearer " + accessToken)
        .content(asJsonString(objectMapper, ob))
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON));
  }




}
