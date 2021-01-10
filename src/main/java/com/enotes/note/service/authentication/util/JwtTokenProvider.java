package com.enotes.note.service.authentication.util;

import com.enotes.note.application.authentication.StoreConfigProperties;
import com.enotes.note.repository.authentication.UserDetails;
import com.enotes.note.service.InternalServerErrorException;
import com.enotes.note.service.authentication.TokenInfo;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import javax.crypto.SecretKey;

import static com.enotes.note.service.authentication.util.P12Stores.createKeyStore;
import static com.enotes.note.service.authentication.util.P12Stores.readKeyStore;
import static com.enotes.note.service.authentication.util.P12Stores.storeKeyPair;
import static com.enotes.note.service.authentication.util.P12Stores.storeSecretKey;
import static io.jsonwebtoken.SignatureAlgorithm.RS256;

public final class JwtTokenProvider implements TokenProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenProvider.class);

  private static final long ACCESS_TOKEN_EXPIRY_TIME = 1 * 60 * 60 * 1000;
  private static final long REFRESH_TOKEN_EXPIRY_TIME =10 *  24 * 60 * 60 * 1000;

  public static final String SECRET_KEY = "secretKey";
  public static final String PRIVATE_KEY = "privateKey";

  private final Keys keys;

  private final JwtParser secretJwtParser;
  private final JwtParser privateJwtParser;

  public JwtTokenProvider(StoreConfigProperties storeConfigProperties) {
    this(readKey(storeConfigProperties));
  }

  public JwtTokenProvider(Keys keys) {
    this.keys = keys;
    secretJwtParser = Jwts.parserBuilder().setSigningKey(this.keys.secretKey).build();
    privateJwtParser = Jwts.parserBuilder().setSigningKey(this.keys.publicKey).build();
  }

  static Keys readKey(StoreConfigProperties properties) {
    final String storeLocation = properties.getLocation();
    final String storePwd = properties.getPwd();
    final String keyPwd = properties.getKeyPwd();

    Objects.requireNonNull(storeLocation, "store location should be configured");
    Objects.requireNonNull(storePwd, "store password should be configured");
    Objects.requireNonNull(keyPwd, "secret key password should be configured");

    try {
      KeyStore keyStore;
      Key secretKey = null;
      Key privateKey = null;
      Key publicKey = null;
      try {
        keyStore = readKeyStore(storeLocation, storePwd);
        secretKey = P12Stores.readKey(keyStore, SECRET_KEY, keyPwd);
        privateKey = P12Stores.readKey(keyStore, PRIVATE_KEY, keyPwd);
        publicKey = P12Stores.readCertificate(keyStore, PRIVATE_KEY).getPublicKey();
      } catch (IOException e) {
        keyStore = createKeyStore();
        keyStore.load(null, null);
      }

      if (secretKey == null) {
        secretKey = createAndStoreSecretKey(keyStore, SECRET_KEY, keyPwd);
        final KeyPair keyPair = createAndStoreKeyPair(keyStore, PRIVATE_KEY, keyPwd);
        privateKey = keyPair.getPrivate();
        publicKey = keyPair.getPublic();
        P12Stores.saveKeyStore(keyStore, storeLocation, storePwd);
      }
      return new Keys(secretKey, publicKey, privateKey);
    } catch (IOException | GeneralSecurityException e) {
      throw new InternalServerErrorException(e);
    }
  }

  private static Key createAndStoreSecretKey(final KeyStore keyStore, final String keyAlias,
      final String keyPwd) throws KeyStoreException {
    SecretKey secretKey = io.jsonwebtoken.security.Keys.secretKeyFor(SignatureAlgorithm.HS512);
    storeSecretKey(keyStore, keyAlias, keyPwd, secretKey);
    return secretKey;
  }

  private static KeyPair createAndStoreKeyPair(final KeyStore keyStore, final String keyAlias,
      final String keyPwd) throws IOException, GeneralSecurityException {
    KeyPair keyPair = io.jsonwebtoken.security.Keys.keyPairFor(RS256);
    storeKeyPair(keyStore, keyPwd, keyPair, keyAlias);

    try(FileOutputStream fos = new FileOutputStream("publicKey")) {
      fos.write(keyPair.getPublic().getEncoded());
    }
    return keyPair;
  }

  /**
   * Generates a JWT token containing username as subject, and userId and role as additional claims. These properties are taken from the specified
   * User object. Tokens validity is infinite.
   *
   * @param user the user for which the token will be generated
   * @return the JWT token
   */
  @Override
  public TokenInfo generateToken(UserDetails user, TokenType type) {
    String id = UUID.randomUUID().toString().replace("-", "");

    long expiryTime;
    Key key;
    switch (type) {
      case ACCESS:
        expiryTime = ACCESS_TOKEN_EXPIRY_TIME;
        key = keys.privateKey;
        break;
      case REFRESH:
        expiryTime = REFRESH_TOKEN_EXPIRY_TIME;
        key = keys.secretKey;
        break;
    default:
      throw new UnsupportedOperationException("Token of type " + type + "is not yet supported");
    }

    final String userName = user.getUserName();
    Claims claims = Jwts.claims()
        .setIssuer("auth-backend")
        .setSubject(userName);

    final Date issuedDate = new Date(System.currentTimeMillis());
    final Date expiryDate = new Date(issuedDate.getTime() + expiryTime);
    final String token = Jwts.builder()
        .addClaims(claims)
        .setId(id)
        .signWith(key)
        .setExpiration(expiryDate)
        .setIssuedAt(issuedDate)
        .compact();
    return new TokenInfo.Builder(token)
        .id(id)
        .isValid(true)
        .userName(userName)
        .build();
  }

  @Override
  public boolean validateToken(String authToken, TokenType type) {
    return extractTokenInfo(authToken, type).isValid();
  }

  @Override
  public TokenInfo extractTokenInfo(final String authToken, final TokenType type) {
    TokenInfo.Builder tokenBuilder = new TokenInfo.Builder(authToken)
        .isValid(false);
    try {
      final Claims claims = getAllClaimsFromToken(authToken, type);

      tokenBuilder
          .userName(claims.getSubject())
          .id(claims.getId())
          .isValid(true);
    } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
      LOGGER.error("Invalid JWT signature error", e);
    } catch (ExpiredJwtException e) {
      LOGGER.error("Expired JWT token error", e);
    } catch (UnsupportedJwtException e) {
      LOGGER.error("Unsupported JWT token error", e);
    } catch (IllegalArgumentException e) {
      LOGGER.error("JWT token compact of handler are invalid error", e);
    }
    return tokenBuilder.build();
  }

  Claims getAllClaimsFromToken(String token, final TokenType type) {
    return getJwtParser(type).parseClaimsJws(token).getBody();
  }

  private JwtParser getJwtParser(final TokenType type) {
    switch (type) {
    case ACCESS:
      return privateJwtParser;
    case REFRESH:
      return secretJwtParser;
    default:
      throw new UnsupportedOperationException("Token of type " + type + "is not yet supported");
    }
  }

  static final class Keys {
    private final Key secretKey;
    private final Key publicKey;
    private final Key privateKey;

    Keys(final Key secretKey, Key publicKey, final Key privateKey) {
      this.secretKey = secretKey;
      this.publicKey = publicKey;
      this.privateKey = privateKey;
    }

    public Key getSecretKey() {
      return secretKey;
    }

    public Key getPublicKey() {
      return publicKey;
    }

    public Key getPrivateKey() {
      return privateKey;
    }
  }
}
