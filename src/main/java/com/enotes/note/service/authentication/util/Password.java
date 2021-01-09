package com.enotes.note.service.authentication.util;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

public final class Password {

  private static final Logger LOGGER = LoggerFactory.getLogger(Password.class);
  static final Pattern BCRYPT_PATTERN = Pattern.compile("\\A\\$2a?\\$\\d\\d\\$[./0-9A-Za-z]{53}");

  private Password() {
    //do nothing
  }

  // Define the BCrypt workload to use when generating password hashes. 10-31 is a valid value.
  private static int WORKLOAD = 12;

  /**
   * This method can be used to generate a string representing an account password
   * suitable for storing in a database. It will be an OpenBSD-style crypt(3) formatted
   * hash string of length=60
   * The bcrypt workload is specified in the above static variable, a value from 10 to 31.
   * A workload of 12 is a very reasonable safe default as of 2013.
   * This automatically handles secure 128-bit salt generation and storage within the hash.
   * @param passwordPlaintext The account's plaintext password as provided during account creation,
   *			     or when changing an account's password.
   * @return String - a string of length 60 that is the bcrypt hashed password in crypt(3) format.
   */
  public static String hashPassword(String passwordPlaintext) {
    String salt = BCrypt.gensalt(WORKLOAD);

    return BCrypt.hashpw(passwordPlaintext, salt);
  }

  /**
   * This method can be used to verify a computed hash from a plaintext (e.g. during a login
   * request) with that of a stored hash from a database. The password hash from the database
   * must be passed as the second variable.
   * @param passwordPlaintext The account's plaintext password, as provided during a login request
   * @param encodedPassword The account's stored password hash, retrieved from the authorization database
   * @return boolean - true if the password matches the password of the stored hash, false otherwise
   */
  public static boolean checkPassword(String passwordPlaintext, String encodedPassword) {

    if (encodedPassword == null || encodedPassword.length() == 0) {
      LOGGER.warn("Empty encoded password");
      return false;
    }

    if (passwordPlaintext == null || passwordPlaintext.length() == 0) {
      LOGGER.warn("Empty password");
      return false;
    }

    if (!BCRYPT_PATTERN.matcher(encodedPassword).matches()) {
      LOGGER.warn("Encoded password does not look like BCrypt");
      return false;
    }

    return BCrypt.checkpw(passwordPlaintext, encodedPassword);
  }
}
