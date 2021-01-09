package com.enotes.note.service.authentication.util;

import com.enotes.note.application.StoreConfigProperties;
import com.enotes.note.repository.authentication.UserDetails;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.util.Date;
import java.util.Properties;

import static com.enotes.note.service.authentication.util.JwtTokenProvider.KEY_PWD;
import static com.enotes.note.service.authentication.util.JwtTokenProvider.STORE_LOCATION;
import static com.enotes.note.service.authentication.util.JwtTokenProvider.STORE_PWD;
import static org.junit.jupiter.api.Assertions.*;

public class JwtTokenProviderTest {


  @ParameterizedTest
  @EnumSource(TokenProvider.TokenType.class)
  public void testCreatingRefreshToken(TokenProvider.TokenType type) {
    final KeyPair keyPair = Keys.keyPairFor(SignatureAlgorithm.RS256);
    final JwtTokenProvider.Keys keys = new JwtTokenProvider.Keys(Keys.secretKeyFor(SignatureAlgorithm.HS512), keyPair.getPublic(), keyPair.getPrivate());
    final JwtTokenProvider tokenProvider = new JwtTokenProvider(keys);

    final UserDetails user = new UserDetails.Builder("John", "12344").build();

    final String token = tokenProvider.generateToken(user, type).getToken();

    final Claims claims = tokenProvider.getAllClaimsFromToken(token, type);

    assertEquals(user.getUserName(), claims.getSubject());
  }

  @ParameterizedTest
  @EnumSource(TokenProvider.TokenType.class)
  public void testExpiredToken(TokenProvider.TokenType type) {
    final KeyPair keyPair = Keys.keyPairFor(SignatureAlgorithm.RS256);
    final JwtTokenProvider.Keys keys = new JwtTokenProvider.Keys(Keys.secretKeyFor(SignatureAlgorithm.HS512), keyPair.getPublic(), keyPair.getPrivate());

    final JwtTokenProvider tokenProvider = new JwtTokenProvider(keys);

    long minusOneDay = 1 * 24 * 60 * 60 * 1000;
    final Date expiryDate = new Date(System.currentTimeMillis() - minusOneDay );

    String token = Jwts.builder()
        .signWith(keys.getSecretKey())
        .setExpiration(expiryDate)
        .compact();

    assertFalse(tokenProvider.validateToken(token, type));
  }

  @ParameterizedTest
  @EnumSource(TokenProvider.TokenType.class)
  public void testTokenSignedWithDifferentSecretKey(TokenProvider.TokenType type) {
    final KeyPair keyPair = Keys.keyPairFor(SignatureAlgorithm.RS256);
    final JwtTokenProvider.Keys keys = new JwtTokenProvider.Keys(Keys.secretKeyFor(SignatureAlgorithm.HS512), keyPair.getPublic(), keyPair.getPrivate());

    final JwtTokenProvider tokenProvider = new JwtTokenProvider(keys);

    long minusOneDay = 1 * 24 * 60 * 60 * 1000;
    final Date expiryDate = new Date(System.currentTimeMillis() - minusOneDay );

    String token = Jwts.builder()
        .signWith(Keys.secretKeyFor(SignatureAlgorithm.HS512))
        .setExpiration(expiryDate)
        .compact();

    assertFalse(tokenProvider.validateToken(token, type));
  }

  @ParameterizedTest
  @EnumSource(TokenProvider.TokenType.class)
  public void testStoringKeyIsStillTheSame(TokenProvider.TokenType type) throws IOException {

    final StoreConfigProperties configProperties = new StoreConfigProperties.Builder()
        .withLocation("sec.jks")
        .withPwd("123455")
        .withKeyPwd("222222")
        .build();
    final JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(configProperties);

    final UserDetails user = new UserDetails.Builder("John", "").build();
    final String token = jwtTokenProvider.generateToken(user, type).getToken();

    final JwtTokenProvider jwtTokenProvider1 = new JwtTokenProvider(configProperties);

    assertTrue(jwtTokenProvider1.validateToken(token, type));

    Files.deleteIfExists(Paths.get(configProperties.getLocation()));
  }
}
