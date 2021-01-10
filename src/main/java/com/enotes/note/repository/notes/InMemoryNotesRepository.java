package com.enotes.note.repository.notes;

import com.enotes.note.repository.AbstractInMemoryRepository;
import com.enotes.note.service.notes.Note;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryNotesRepository extends AbstractInMemoryRepository<String, NotesDetails> implements NotesRepository {

  private final Map<UserId, Collection<String>> noteIdsPerUserId;

  public InMemoryNotesRepository() {
    super();
    noteIdsPerUserId = new ConcurrentHashMap<>();
  }

  @Override
  public boolean putIfAbsent(final String key, final NotesDetails value) {
    final boolean added = super.putIfAbsent(key, value);

    if (added) {
      noteIdsPerUserId.computeIfAbsent(value.getUserId(), (k) -> new ArrayList<>()).add(value.getId());
    }
    return added;
  }

  @Override
  public void put(final String key, final NotesDetails value) {
    super.put(key, value);

    noteIdsPerUserId.computeIfAbsent(value.getUserId(), (k) -> new ArrayList<>()).add(value.getId());
  }

  @Override
  public Collection<NotesDetails> findAll(final UserId userId) {
    final Map<String, NotesDetails> cache = getCache();
    return noteIdsPerUserId.get(userId)
        .stream()
        .map(cache::get)
        .collect(Collectors.toList());
  }
}
