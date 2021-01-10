package com.enotes.note;

import com.enotes.note.application.authentication.StoreConfigProperties;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({StoreConfigProperties.class})
public class NoteApplication {
  public static void main(String[] args) {
    SpringApplication.run(NoteApplication.class, args);
  }
}
