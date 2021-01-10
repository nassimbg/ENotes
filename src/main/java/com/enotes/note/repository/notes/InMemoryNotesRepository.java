package com.enotes.note.repository.notes;

import com.enotes.note.repository.AbstractInMemoryRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class InMemoryNotesRepository extends AbstractInMemoryRepository<String, NotesDetails> implements NotesRepository {

  private final Map<UserId, Collection<String>> noteIdsPerUserId;

  public InMemoryNotesRepository() {
    super();
    noteIdsPerUserId = new HashMap<>();
  }

  @Override
  public synchronized boolean putIfAbsent(final String key, final NotesDetails value) {
    final boolean added = super.putIfAbsent(key, value);

    if (added) {
      noteIdsPerUserId.computeIfAbsent(value.getUserId(), (k) -> new ArrayList<>()).add(value.getId());
    }
    return added;
  }

  @Override
  public synchronized void put(final String key, final NotesDetails value) {
    super.put(key, value);

    noteIdsPerUserId.computeIfAbsent(value.getUserId(), (k) -> new ArrayList<>()).add(value.getId());
  }

  @Override
  public synchronized Collection<NotesDetails> findAll(final UserId userId) {
    final Map<String, NotesDetails> cache = getCache();
    return noteIdsPerUserId.get(userId)
        .stream()
        .map(cache::get)
        .collect(Collectors.toList());
  }

  @Override
  public synchronized void delete(final String id) {
    final NotesDetails note = getCache().remove(id);
    noteIdsPerUserId.get(note.getUserId()).remove(note.getId());
  }
}
