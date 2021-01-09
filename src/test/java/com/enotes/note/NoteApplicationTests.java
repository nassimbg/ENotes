package com.enotes.note;

import com.enotes.note.application.Config;
import com.enotes.note.application.PathBuilder;
import com.enotes.note.application.authentication.AuthenticationController;
import com.enotes.note.service.authentication.AuthenticationToken;
import com.enotes.note.service.authentication.User;
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

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(classes = NoteApplication.class)
@SpringJUnitConfig(Config.class)
@AutoConfigureMockMvc
@TestPropertySource(
    locations = "classpath:application-integrationtest.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class NoteApplicationTests {

  @Autowired
  private MockMvc mvc;

  private final ObjectMapper objectMapper = new ObjectMapper();


  @Test
  public void testSignUp() throws Exception {
    final User user = User.builder("John").withPassword("122334").build();

    final ResultActions postResponse = signUpUser(user);

    final MvcResult mvcResult = postResponse
        .andDo(print())
        .andExpect(status().isOk())
        .andReturn();

    final AuthenticationToken authenticationToken = fromJson(mvcResult.getResponse().getContentAsString(), AuthenticationToken.class);

    assertNotNull(authenticationToken.getAccessToken());
    assertNotNull(authenticationToken.getRefreshToken());
  }

  @Test
  public void testSignUpWithUserNameAlreadyExist() throws Exception {
    final User user = User.builder("John").withPassword("122334").build();

    final String errorMessage = "Account with user name " + user.getUserName() + " already exist";

    final ResultActions postResponse = signUpUser(user);

    final MvcResult mvcResult = postResponse
        .andDo(print())
        .andExpect(status().isOk())
        .andReturn();

    final AuthenticationToken authenticationToken = fromJson(mvcResult.getResponse().getContentAsString(), AuthenticationToken.class);

    assertNotNull(authenticationToken.getAccessToken());

    final ResultActions secondPostResponse = signUpUser(user);

    final MvcResult secondMvcResult = secondPostResponse
        .andDo(print())
        .andExpect(status().isConflict())
        .andReturn();

    assertEquals(errorMessage, secondMvcResult.getResponse().getContentAsString());
  }

  @Test
  public void testSignInForExistingUser() throws Exception {
    final User user = User.builder("John").withPassword("122334").build();

    final ResultActions postResponse = signUpUser(user);

    final MvcResult mvcResult = postResponse
        .andDo(print())
        .andExpect(status().isOk())
        .andReturn();

    final AuthenticationToken authenticationToken = fromJson(mvcResult.getResponse().getContentAsString(), AuthenticationToken.class);

    assertNotNull(authenticationToken.getAccessToken());
    assertNotNull(authenticationToken.getRefreshToken());


    final ResultActions signInPostResponse = signInUser(user);

    final MvcResult signInMvcResult = signInPostResponse
        .andDo(print())
        .andExpect(status().isOk())
        .andReturn();

    final AuthenticationToken signInAuthenticationToken = fromJson(signInMvcResult.getResponse().getContentAsString(), AuthenticationToken.class);

    assertNotEquals(authenticationToken.getAccessToken(), signInAuthenticationToken.getAccessToken());
    assertNotEquals(authenticationToken.getRefreshToken(), signInAuthenticationToken.getRefreshToken());
  }

  @Test
  public void testSignInForNotExistingUser() throws Exception {
    final User user = User.builder("John").withPassword("122334").build();

    final String errorMessage = "The username or password is incorrect";
    final MvcResult signInMvcResult = signInUser(user)
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andReturn();

    assertEquals(errorMessage, signInMvcResult.getResponse().getContentAsString());
  }

  private ResultActions signInUser(final User user) throws Exception {
    return postRequest(user, AuthenticationController.SIGNIN);
  }

  private ResultActions signUpUser(final User user) throws Exception {
    return postRequest(user, AuthenticationController.SIGNUP);
  }

  private ResultActions postRequest(final User user, String path) throws Exception {
    return mvc.perform(post(PathBuilder.buildPath('/', PathBuilder.AUTHENTICATION, path))
        .content(asJsonString(user))
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON));
  }

  public String asJsonString(final Object obj) {
    try {
      return objectMapper.writeValueAsString(obj);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public <T> T fromJson(String string, Class<T> clazz)
      throws IOException {

    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    return objectMapper.readValue(string, clazz);
  }

}
