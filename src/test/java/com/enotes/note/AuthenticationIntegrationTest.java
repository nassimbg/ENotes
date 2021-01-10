package com.enotes.note;

import com.enotes.note.application.Utils;
import com.enotes.note.application.authentication.AuthenticationConfig;
import com.enotes.note.application.PathBuilder;
import com.enotes.note.application.authentication.AuthenticationController;
import com.enotes.note.service.authentication.AuthenticationToken;
import com.enotes.note.service.authentication.User;
import com.enotes.note.service.authentication.UserStatus;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(classes = NoteApplication.class)
@SpringJUnitConfig(AuthenticationConfig.class)
@AutoConfigureMockMvc
@TestPropertySource(
    locations = "classpath:application-integrationtest.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuthenticationIntegrationTest {

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

  @Test
  public void testRefreshToken() throws Exception {
    final User user = User.builder("John").withPassword("122334").build();

    final ResultActions postResponse = signUpUser(user);

    final MvcResult mvcResult = postResponse
        .andDo(print())
        .andExpect(status().isOk())
        .andReturn();
    final AuthenticationToken authenticationToken = fromJson(mvcResult.getResponse().getContentAsString(), AuthenticationToken.class);

    assertNotNull(authenticationToken.getAccessToken());

    final ResultActions response = postRequest(authenticationToken, AuthenticationController.TOKEN);
    final MvcResult refreshMvcResult = response
        .andDo(print())
        .andExpect(status().isOk())
        .andReturn();

    final AuthenticationToken newAuthenticationToken = fromJson(refreshMvcResult.getResponse().getContentAsString(), AuthenticationToken.class);

    assertNotEquals(authenticationToken.getAccessToken(), newAuthenticationToken.getAccessToken());
    assertEquals(authenticationToken.getRefreshToken(), newAuthenticationToken.getRefreshToken());
  }

  @Test
  public void testFailingToRefreshTokenDueUserDoesntExist() throws Exception {
    String refreshToken = "eyJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJhdXRoLWJhY2tlbmQiLCJzdWIiOiJKb2huIiwianRpIjoiMzA3ZmI2ZDg1OWZlNGYzN2I4Mzg5YjMyYjg1OTljZjEiLCJleHAiOjE2MTEwOTcyMTcsImlhdCI6MTYxMDIzMzIxN30.P9HGXRcQV3sniM0uhMM4zwXCF7a5J1G_3zeakd8mBY7JJXctkOv-dezuHdLnTB_KTc3IG02aWfNYtWvB05bReg";
    String accessToken = "eyJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJhdXRoLWJhY2tlbmQiLCJzdWIiOiJKb2huIiwianRpIjoiOGVlMmQ4MmFhNTkwNDc3NmJhZWJkZGI2Zjk2MDk3OTgiLCJleHAiOjE2MTAyMzY4MjEsImlhdCI6MTYxMDIzMzIyMX0.mJt_5XeFXtMYMEgYUWm6CJz8lNguR_5wYyjzuCtr9bSmh3fEqGgmXjVfjOW6PwTkcKegFccs7z80TvKFHUdIufrLWbpGhHF8nL69YhPqZ1hKE9-ZkbwtStfnH0CuCUfzdWvW1XbFVKmCWl3q95Pw7HImpQ8QBX53uf5pBRHLQMZFcCItulztD3q_QK2jWx3WJdlKyIWz150um55wroKAo7gwnnoSN_5RLWiX2ZjGjsxG80NErb0qDXa8CCVuAoiKD4Q_c6nFjKQEYhE21HUBTvtUWIv1wiBzo-4wcsfi0rymoZ1INBT3D4yTbllO5hecuus-rLuhsT_evJPxTuRGGQ";

    final AuthenticationToken authenticationToken = new AuthenticationToken(accessToken, refreshToken);

   final ResultActions response = postRequest(authenticationToken, AuthenticationController.TOKEN);
   final MvcResult refreshMvcResult = response
       .andDo(print())
       .andExpect(status().isBadRequest())
       .andReturn();

    assertEquals("The object you requested does not exist", refreshMvcResult.getResponse().getContentAsString());
  }

  @Test
  public void testFailingToRefreshTokenDueUserInvalidatedToken() throws Exception {
    final User user = User.builder("John").withPassword("122334").build();
    final ResultActions postResponse = signUpUser(user);

    final MvcResult mvcResult = postResponse
        .andDo(print())
        .andExpect(status().isOk())
        .andReturn();
    final AuthenticationToken authenticationToken = fromJson(mvcResult.getResponse().getContentAsString(), AuthenticationToken.class);

    postRequest(authenticationToken, AuthenticationController.SIGNOUT)
        .andDo(print())
        .andExpect(status().isOk())
        .andReturn();

    final ResultActions response = postRequest(authenticationToken, AuthenticationController.TOKEN);
    final MvcResult refreshMvcResult = response
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andReturn();

    assertEquals("The provided Refresh Token is either expired or has been revoked", refreshMvcResult.getResponse().getContentAsString());
  }

  @Test
  public void testSignOut() throws Exception {
    final User user = User.builder("John").withPassword("122334").build();
    final ResultActions postResponse = signUpUser(user);

    final MvcResult mvcResult = postResponse
        .andDo(print())
        .andExpect(status().isOk())
        .andReturn();
    final AuthenticationToken authenticationToken = fromJson(mvcResult.getResponse().getContentAsString(), AuthenticationToken.class);

    final MvcResult signOutMvc = postRequest(authenticationToken, AuthenticationController.SIGNOUT)
        .andDo(print())
        .andExpect(status().isOk())
        .andReturn();
    final UserStatus userStatus = fromJson(signOutMvc.getResponse().getContentAsString(), UserStatus.class);

    assertEquals(user.getUserName(), userStatus.getUserName());
    assertTrue(userStatus.logOut());
  }

  private ResultActions signInUser(final User user) throws Exception {
    return postRequest(user, AuthenticationController.SIGNIN);
  }

  public ResultActions signUpUser(final User user) throws Exception {
    return postRequest(user, AuthenticationController.SIGNUP);
  }

  private ResultActions postRequest(final Object ob, String path) throws Exception {
    return Utils.postRequest(mvc, ob, PathBuilder.AUTHENTICATION, path, objectMapper);
  }

  public <T> T fromJson(String string, Class<T> clazz)
      throws IOException {
    return Utils.fromJson(objectMapper, string, clazz);
  }
}
