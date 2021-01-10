package com.enotes.note.application.notes;

import com.enotes.note.application.PathBuilder;
import com.enotes.note.application.authentication.StoreConfigProperties;
import com.enotes.note.repository.notes.InMemoryNotesRepository;
import com.enotes.note.repository.notes.NotesRepository;
import com.enotes.note.service.notes.NotesService;
import com.enotes.note.service.notes.util.JwtTokenValidator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity
public class NotesConfig extends WebSecurityConfigurerAdapter {

  @Autowired
  private StoreConfigProperties storeConfigProperties;

  @Bean
  public NotesService getNotesService(NotesRepository notesRepository) {
    return new NotesService(notesRepository);
  }

  @Bean
  public NotesRepository getNotesRepository() {
    return new InMemoryNotesRepository();
  }

  @Override
  protected void configure(final HttpSecurity http) throws Exception {
    http.cors().and().authorizeRequests()
        .anyRequest().authenticated()
        .and()
        .addFilter(new JWTAuthorizationFilter(authenticationManager(), new JwtTokenValidator(storeConfigProperties)))
        .csrf().disable();
  }

  @Override
  public void configure(final WebSecurity web) throws Exception {
    web.ignoring().antMatchers(PathBuilder.buildPath(PathBuilder.AUTHENTICATION, "**"));
  }
}
