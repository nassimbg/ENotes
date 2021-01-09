package com.enotes.note.application.authentication;

import com.enotes.note.application.PathBuilder;
import com.enotes.note.service.authentication.AlreadyExistsException;
import com.enotes.note.service.authentication.AuthenticationException;
import com.enotes.note.service.authentication.AuthenticationService;
import com.enotes.note.service.authentication.AuthenticationToken;
import com.enotes.note.service.authentication.User;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = AuthenticationController.class)
class AuthenticationControllerTest {

  @Autowired
  private MockMvc mvc;

  @MockBean
  private AuthenticationService authenticationService;

  private ObjectMapper objectMapper = new ObjectMapper();

  @Test
  public void testSignUp() throws Exception {
    final User user = User.builder("John").withPassword("122334").build();

    Mockito.when(authenticationService.signUp(Mockito.eq(user))).thenReturn(new AuthenticationToken(user.getUserName(),
        user.getUserName()));

    final ResultActions postResponse = signUpUser(user);

    final MvcResult mvcResult = postResponse
        .andDo(print())
        .andExpect(status().isOk())
        .andReturn();

    final AuthenticationToken authenticationToken = fromJson(mvcResult.getResponse().getContentAsString(), AuthenticationToken.class);

    assertEquals(user.getUserName(), authenticationToken.getAccessToken());
  }

  @Test
  public void testSignUpWithUserNameAlreadyExist() throws Exception {
    final User user = User.builder("John").withPassword("122334").build();

    final String errorMessage = "Account with user name " + user.getUserName() + " already exist";

    Mockito.when(authenticationService.signUp(Mockito.eq(user)))
        .thenReturn(new AuthenticationToken(user.getUserName(), user.getUserName()))
        .thenThrow(new AlreadyExistsException(errorMessage));

    final ResultActions postResponse = signUpUser(user);

    final MvcResult mvcResult = postResponse
        .andDo(print())
        .andExpect(status().isOk())
        .andReturn();

    final AuthenticationToken authenticationToken = fromJson(mvcResult.getResponse().getContentAsString(), AuthenticationToken.class);

    assertEquals(user.getUserName(), authenticationToken.getAccessToken());

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

    Mockito.when(authenticationService.signIn(Mockito.eq(user))).thenReturn(new AuthenticationToken(user.getUserName(),
        user.getUserName()));

    final ResultActions postResponse = signInUser(user);

    final MvcResult mvcResult = postResponse
        .andDo(print())
        .andExpect(status().isOk())
        .andReturn();

    final AuthenticationToken authenticationToken = fromJson(mvcResult.getResponse().getContentAsString(), AuthenticationToken.class);

    assertEquals(user.getUserName(), authenticationToken.getAccessToken());
  }

  @Test
  public void testSignInForNotExistingUser() throws Exception {
    final User user = User.builder("John").withPassword("122334").build();

    final String errorMessage = "The username or password is incorrect";
    Mockito.when(authenticationService.signIn(Mockito.eq(user))).thenThrow(new AuthenticationException(errorMessage));

    final ResultActions postResponse = signInUser(user);

    final MvcResult mvcResult = postResponse
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andReturn();

    assertEquals(errorMessage, mvcResult.getResponse().getContentAsString());
  }

  @Test
  public void testRefresh() throws Exception {
    final String refreshToken = "refreshToken";
    final AuthenticationToken authenticationToken = new AuthenticationToken("accessToken", refreshToken);

    final String newAccessToken = "newAccessToken";
    Mockito.when(authenticationService.refresh(Mockito.eq(authenticationToken)))
        .thenReturn(new AuthenticationToken(newAccessToken, refreshToken));

    final ResultActions postResponse = postRequest(authenticationToken, AuthenticationController.TOKEN);
    final MvcResult mvcResult = postResponse
        .andDo(print())
        .andExpect(status().isOk())
        .andReturn();

    final AuthenticationToken newAuthenticationToken = fromJson(mvcResult.getResponse().getContentAsString(), AuthenticationToken.class);
    assertEquals(newAccessToken, newAuthenticationToken.getAccessToken());
    assertEquals(refreshToken, newAuthenticationToken.getRefreshToken());
  }

  @Test
  public void testFailingRefresh() throws Exception {
    final AuthenticationToken authenticationToken = new AuthenticationToken("accessToken", "refreshToken");

    final String errorMessage = "Can not refresh";
    Mockito.when(authenticationService.refresh(Mockito.eq(authenticationToken)))
        .thenThrow(new AuthenticationException(errorMessage));

    final ResultActions postResponse = postRequest(authenticationToken, AuthenticationController.TOKEN);
    final MvcResult mvcResult = postResponse
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals(errorMessage, mvcResult.getResponse().getContentAsString());
  }

  private ResultActions signInUser(final User user) throws Exception {
    return postRequest(user, AuthenticationController.SIGNIN);
  }

  private ResultActions signUpUser(final User user) throws Exception {
    return postRequest(user, AuthenticationController.SIGNUP);
  }

  private ResultActions postRequest(final Object ob, String path) throws Exception {
    return mvc.perform(post(PathBuilder.buildPath('/', PathBuilder.AUTHENTICATION, path))
        .content(asJsonString(ob))
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
