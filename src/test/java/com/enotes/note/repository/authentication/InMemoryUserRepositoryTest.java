package com.enotes.note.repository.authentication;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryUserRepositoryTest {

  @Test
  public void testAddingKeyDoesntAlreadyExist() {
    final InMemoryUserRepository persister = new InMemoryUserRepository();

    final String userName = "John";
    final UserDetails user = new UserDetails.Builder(userName, "123").build();

    assertTrue(persister.putIfAbsent(userName, user));

    assertEquals(user, persister.findById(userName).get());
  }

  @Test
  public void testAddingKeyAlreadyExist() {
    final InMemoryUserRepository persister = new InMemoryUserRepository();

    final String userName = "John";
    final UserDetails user = new UserDetails.Builder(userName, "123").build();
    assertTrue(persister.putIfAbsent(userName, user));
    assertFalse(persister.putIfAbsent(userName, user));
  }

  @Test
  public void testGetNonExistingKey() {
    final InMemoryUserRepository persister = new InMemoryUserRepository();

    final String userName = "John";

    assertFalse(persister.findById(userName).isPresent());
  }
}
