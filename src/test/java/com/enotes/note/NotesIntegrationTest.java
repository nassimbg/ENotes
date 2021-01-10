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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
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

import java.util.Collection;
import java.util.List;
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

    final Note note = new Note(null, "title", "body");
    createNoteAndAssert(note, authenticationToken);
  }

  @Test
  public void testGetNote() throws Exception {
    final AuthenticationToken authenticationToken = getAuthenticationToken();

    final Note note = new Note(null,"title", "body");
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

  @Test
  public void testGetNoteAllNotesForUserId() throws Exception {
    final AuthenticationToken authenticationToken1 = getAuthenticationToken();

    final Note note = new Note(null,"title", "body");
    final NoteId noteId = createNoteAndAssert(note, authenticationToken1);

    final Note note2 = new Note(null,"title2", "body2");
    final NoteId noteId2 = createNoteAndAssert(note2, authenticationToken1);

    final AuthenticationToken authenticationTokenUser2 = getAuthenticationToken(
        User.builder("user 2").withPassword("pass 2").build());

    final Note note3 = new Note(null,"title3", "body3");
    final NoteId noteId3 = createNoteAndAssert(note3, authenticationTokenUser2);

    final Note note4 = new Note(null,"title4", "body4");
    final NoteId noteId4 = createNoteAndAssert(note4, authenticationTokenUser2);

    final ResultActions resultActions = mvc.perform(get(PathBuilder.buildPath(PathBuilder.NOTES, ""))
        .header("Authorization", "Bearer " + authenticationToken1.getAccessToken())
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
    assertEquals(noteId.getId(), notes.get(0).getId());
    assertEquals(noteId2.getId(), notes.get(1).getId());
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

    return getAuthenticationToken(user);
  }

  private AuthenticationToken getAuthenticationToken(final User user) throws Exception {
    final ResultActions postResponse = Utils.postRequest(mvc, user, PathBuilder.AUTHENTICATION,
        AuthenticationController.SIGNUP, objectMapper);
    final MvcResult mvcResult = postResponse
        .andDo(print())
        .andExpect(status().isOk())
        .andReturn();

    return fromJson(objectMapper, mvcResult.getResponse().getContentAsString(), AuthenticationToken.class);
  }


  public static ResultActions postRequest(MockMvc mvc, final Object ob, String path, String accessToken, ObjectMapper objectMapper) throws Exception {
    return mvc.perform(post(PathBuilder.buildPath('/', PathBuilder.NOTES, path))
        .header("Authorization", "Bearer " + accessToken)
        .content(asJsonString(objectMapper, ob))
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON));
  }




}
