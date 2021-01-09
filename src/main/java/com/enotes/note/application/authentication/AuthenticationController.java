package com.enotes.note.application.authentication;

import com.enotes.note.application.PathBuilder;
import com.enotes.note.service.authentication.AuthenticationService;
import com.enotes.note.service.authentication.AuthenticationToken;
import com.enotes.note.service.authentication.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(PathBuilder.AUTHENTICATION)
public class AuthenticationController {

  public static final String SIGNUP = "signup";
  public static final String SIGNIN = "signin";

  private final AuthenticationService authenticationService;

  @Autowired
  public AuthenticationController(AuthenticationService authenticationService) {
    this.authenticationService = authenticationService;
  }

  @PostMapping(SIGNUP)
  public AuthenticationToken signUp(@RequestBody User user) {
    return authenticationService.signUp(user);
  }

  @PostMapping(SIGNIN)
  public AuthenticationToken signIn(@RequestBody User user) {
    return authenticationService.signIn(user);
  }
}
