package com.enotes.note.repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractInMemoryRepository<ID, T> implements Repository<ID, T>{

  private final Map<ID, T> cache;

  public AbstractInMemoryRepository() {
    cache = new ConcurrentHashMap<>();
  }

  protected Map<ID, T> getCache() {
    return cache;
  }

  @Override
  public boolean putIfAbsent(final ID key, final T value) {
    return cache.putIfAbsent(key, value) == null;
  }

  @Override
  public Optional<T> findById(final ID key) {
    return Optional.ofNullable(cache.get(key));
  }

  @Override
  public void put(final ID key, final T value) {
    cache.put(key, value);
  }
}
