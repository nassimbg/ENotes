package com.enotes.note.service.authentication.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordTest {
  @Test
  public void testPasswordHashing() {
    String originalPass = "12345678";

    final String hashPassword = Password.hashPassword(originalPass);

    assertTrue(Password.BCRYPT_PATTERN.matcher(hashPassword).matches());

    assertTrue(Password.checkPassword(originalPass, hashPassword));
  }

  @Test
  public void testDiffPasswords() {
    String originalPass = "12345678";

    final String hashPassword = Password.hashPassword(originalPass);

    assertTrue(Password.BCRYPT_PATTERN.matcher(hashPassword).matches());

    assertFalse(Password.checkPassword("23567", hashPassword));
  }
}
