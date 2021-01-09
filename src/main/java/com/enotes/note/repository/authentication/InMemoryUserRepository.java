package com.enotes.note.repository.authentication;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryUserRepository implements UserRepository {

  private final Map<String, UserDetails> cache;

  public InMemoryUserRepository() {
    cache = new ConcurrentHashMap<>();
  }

  @Override
  public boolean putIfAbsent(final String key, final UserDetails value) {
    return cache.putIfAbsent(key, value) == null;
  }

  @Override
  public Optional<UserDetails> findById(final String key) {
    return Optional.ofNullable(cache.get(key));
  }

  @Override
  public void put(final String key, final UserDetails value) {
    cache.put(key, value);
  }
}
