package com.enotes.note.service.notes.util;

import com.enotes.note.application.authentication.StoreConfigProperties;
import com.enotes.note.service.InternalServerErrorException;
import com.enotes.note.service.authentication.TokenInfo;
import com.enotes.note.service.authentication.util.P12Stores;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;

import static com.enotes.note.service.authentication.util.JwtTokenProvider.PRIVATE_KEY;
import static com.enotes.note.service.authentication.util.P12Stores.readKeyStore;

public class JwtTokenValidator implements TokenValidator {

  private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenValidator.class);

  private final JwtParser publicJwtParser;

  public JwtTokenValidator(StoreConfigProperties properties) {
    publicJwtParser = createJwtParser(properties);
  }

  private JwtParser createJwtParser(StoreConfigProperties properties) {
    final JwtParser privateJwtParser;
    try {
      final PublicKey publicKey = loadPublicKey(properties);

      privateJwtParser = Jwts.parserBuilder().setSigningKey(publicKey).build();
    } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException e) {
      throw new InternalServerErrorException(e);
    }
    return privateJwtParser;
  }

  private PublicKey loadPublicKey(StoreConfigProperties properties)
      throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {

    final KeyStore keyStore = readKeyStore(properties.getLocation(), properties.getPwd());
    return P12Stores.readCertificate(keyStore, PRIVATE_KEY).getPublicKey();
  }

  @Override
  public TokenInfo extractTokenInfo(final String authToken) {
    TokenInfo.Builder tokenBuilder = new TokenInfo.Builder(authToken)
        .isValid(false);
    try {
      final Claims claims = getAllClaimsFromToken(authToken);

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

  Claims getAllClaimsFromToken(String token) {
    return publicJwtParser.parseClaimsJws(token).getBody();
  }
}
