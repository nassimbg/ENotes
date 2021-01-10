package com.enotes.note.service.notes;

import java.util.Objects;

public class Note {

  private final String id;
  private final String title;
  private final String body;

  public Note(String id, final String title, final String body) {
    this.title = title;
    this.body = body;
    this.id = id;
  }

  private Note() {
    title = null;
    body = null;
    id = null;
  }

  public String getTitle() {
    return title;
  }

  public String getBody() {
    return body;
  }

  public String getId() {
    return id;
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
    return Objects.equals(id, note.id)
        && Objects.equals(title, note.title)
        && Objects.equals(body, note.body);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, title, body);
  }

  @Override
  public String toString() {
    return "Note{" +
        "id='" + id + '\'' +
        ", title='" + title + '\'' +
        ", body='" + body + '\'' +
        '}';
  }
}
