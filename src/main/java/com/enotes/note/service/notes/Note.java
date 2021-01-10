package com.enotes.note.service.notes;

import java.util.Objects;

public class Note {

  private final String title;
  private final String body;

  public Note(final String title, final String body) {
    this.title = title;
    this.body = body;
  }

  private Note() {
    title = null;
    body = null;
  }

  public String getTitle() {
    return title;
  }

  public String getBody() {
    return body;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final Note note = (Note) o;
    return Objects.equals(title, note.title)
        && Objects.equals(body, note.body);
  }

  @Override
  public int hashCode() {
    return Objects.hash(title, body);
  }

  @Override
  public String toString() {
    return "Note{" +
        "title='" + title + '\'' +
        ", body='" + body + '\'' +
        '}';
  }
}
