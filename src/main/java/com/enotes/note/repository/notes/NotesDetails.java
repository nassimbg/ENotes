package com.enotes.note.repository.notes;

import java.util.Objects;

public class NotesDetails {

  private final String id;
  private final UserId userId;
  private final String title;
  private final String body;

  private NotesDetails(final Builder builder) {
    this.id = builder.id;
    this.userId = builder.userId;
    this.title = builder.title;
    this.body = builder.body;
  }

  public String getId() {
    return id;
  }

  public UserId getUserId() {
    return userId;
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
    final NotesDetails that = (NotesDetails) o;
    return Objects.equals(id, that.id) && Objects.equals(
        userId, that.userId) && Objects.equals(title, that.title) && Objects.equals(body, that.body);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, userId, title, body);
  }

  @Override
  public String toString() {
    return "NotesDetails{" +
        "id='" + id + '\'' +
        ", userId=" + userId +
        ", title='" + title + '\'' +
        ", body='" + body + '\'' +
        '}';
  }

  public static final class Builder {
    private final String id;
    private final UserId userId;
    private String title;
    private String body;

    public Builder(final String id, final UserId userId) {
      this.id = id;
      this.userId = userId;
    }

    public Builder withTitle(String title) {
      this.title = title;

      return this;
    }

    public Builder withBody(String body) {
      this.body = body;

      return this;
    }

    public NotesDetails build() {
      return new NotesDetails(this);
    }
  }
}
