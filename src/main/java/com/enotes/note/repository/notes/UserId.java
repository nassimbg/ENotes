package com.enotes.note.repository.notes;

import java.util.Objects;

public class UserId {

  private final String id;

  public UserId(final String id) {
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
    final UserId userId = (UserId) o;
    return Objects.equals(id, userId.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "UserId{" +
        "id='" + id + '\'' +
        '}';
  }
}
