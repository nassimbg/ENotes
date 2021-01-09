package com.enotes.note.repository;

import com.enotes.note.repository.authentication.UserDetails;

import java.util.Optional;

public interface Repository<ID, T> {

  /**
   * Persists the specified value with the specified key if the key does not exist
   * @param key key with which the specified value is to be associated
   * @param value value to be associated with the specified key
   * @return true if we were able to add the key and value or false otherwise
   */
  boolean putIfAbsent(ID key, T value);

  /**
   * Returns the value to which the specified key is persisted with
   * @param key the key whose associated value is to be returned
   * @return the value to which the specified key is persisted with or {@link Optional#empty()} if key does not exist
   */
  Optional<T> findById(ID key);

  /**
   * Persists the specified value with the specified key and replaces the key if it exist
   * @param key key with which the specified value is to be associated
   * @param value value to be associated with the specified key
   */
  void put(ID key, T value);
}
