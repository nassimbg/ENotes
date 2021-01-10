package com.enotes.note.repository.notes;

import com.enotes.note.repository.Repository;

import java.util.Collection;

public interface NotesRepository extends Repository<String, NotesDetails> {

  /**
   * Returns all notes corresponding to the user with id {@code userId}
   * @param userId the user id
   * @return all notes corresponding to the user with id {@code userId}
   */
  Collection<NotesDetails> findAll(UserId userId);
}
