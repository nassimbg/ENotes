package com.enotes.note.service.notes;

import java.util.Objects;

public class NoteId {

  private final String id;

  private NoteId() {
    id = null;
  }

  public NoteId(final String id) {
    this.id = id;
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
    final NoteId noteId = (NoteId) o;
    return Objects.equals(id, noteId.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override public String toString() {
    return "NoteId{" +
        "id='" + id + '\'' +
        '}';
  }
}
