package com.enotes.note.service.authentication.util;

import io.jsonwebtoken.SignatureAlgorithm;
import sun.security.x509.AlgorithmId;
import sun.security.x509.CertificateAlgorithmId;
import sun.security.x509.CertificateSerialNumber;
import sun.security.x509.CertificateValidity;
import sun.security.x509.CertificateVersion;
import sun.security.x509.CertificateX509Key;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import javax.crypto.SecretKey;

public final class P12Stores {

  public static final String KEY_STORE_TYPE = "PKCS12";

  private P12Stores() {
    //do nothing
  }

  public static Certificate readCertificate(final KeyStore store, final String keyAlias) throws KeyStoreException {
    return store.getCertificate(keyAlias);
  }

  public static Key readKey(final KeyStore store, final String keyAlias, String keyPwd)
      throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException {
    return store.getKey(keyAlias, keyPwd.toCharArray());
  }

  public static KeyStore readKeyStore(final String storeLocation, final String storePwd) throws
      KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
    try (InputStream storeIS = locateKeystore(storeLocation)) {
      return readKeyStore(storeIS, storePwd);
    }
  }

  public static void storeSecretKey(final KeyStore keyStore, final String keyAlias,
      final String keyPwd, SecretKey secretKey) throws KeyStoreException {
    KeyStore.SecretKeyEntry secret = new KeyStore.SecretKeyEntry(secretKey);
    KeyStore.ProtectionParameter password = new KeyStore.PasswordProtection(keyPwd.toCharArray());
    keyStore.setEntry(keyAlias, secret, password);
  }

  public static void storeKeyPair(KeyStore keyStore, final String secretKey, KeyPair keyPair, final String keyAlias) throws GeneralSecurityException, IOException {
    final X509Certificate certificate = generateCertificate("CN=auth", keyPair, 365,
        SignatureAlgorithm.RS256.getJcaName());
    Certificate[] chain = {certificate};

    keyStore.setKeyEntry(keyAlias, keyPair.getPrivate(), secretKey.toCharArray(), chain);
  }

  private static X509Certificate generateCertificate(String dn, KeyPair keyPair, int validity, String sigAlgName) throws
      GeneralSecurityException, IOException {
    PrivateKey privateKey = keyPair.getPrivate();

    X509CertInfo info = new X509CertInfo();

    Date from = new Date();
    Date to = new Date(from.getTime() + validity * 1000L * 24L * 60L * 60L);

    CertificateValidity interval = new CertificateValidity(from, to);
    BigInteger serialNumber = new BigInteger(64, new SecureRandom());
    X500Name owner = new X500Name(dn);
    AlgorithmId sigAlgId = new AlgorithmId(AlgorithmId.md5WithRSAEncryption_oid);

    info.set(X509CertInfo.VALIDITY, interval);
    info.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(serialNumber));
    info.set(X509CertInfo.SUBJECT, owner);
    info.set(X509CertInfo.ISSUER, owner);
    info.set(X509CertInfo.KEY, new CertificateX509Key(keyPair.getPublic()));
    info.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
    info.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(sigAlgId));

    // Sign the cert to identify the algorithm that's used.
    X509CertImpl certificate = new X509CertImpl(info);
    certificate.sign(privateKey, sigAlgName);

    // Update the algorithm, and resign.
    sigAlgId = (AlgorithmId) certificate.get(X509CertImpl.SIG_ALG);
    info.set(CertificateAlgorithmId.NAME + "." + CertificateAlgorithmId.ALGORITHM, sigAlgId);
    certificate = new X509CertImpl(info);
    certificate.sign(privateKey, sigAlgName);

    return certificate;
  }

  public static KeyStore createKeyStore() throws KeyStoreException {
    return KeyStore.getInstance(KEY_STORE_TYPE);
  }

  public static void saveKeyStore(KeyStore keyStore, String storeLocation, String storePwd)
      throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException {
    try (FileOutputStream fos = new FileOutputStream(storeLocation)) {
      keyStore.store(fos, storePwd.toCharArray());
    }
  }

  private static InputStream locateKeystore(final String storeLocation) throws IOException {
    InputStream storeIS = null;
    if (Files.exists(Paths.get(storeLocation))) {
      storeIS = Files.newInputStream(Paths.get(storeLocation));
    }
    if (storeIS == null && Thread.currentThread().getContextClassLoader() != null) {
      storeIS = Thread.currentThread().getContextClassLoader().getResourceAsStream(storeLocation);
    }

    if (storeIS == null) {
      storeIS = ClassLoader.getSystemResourceAsStream(storeLocation);
    }

    if (storeIS == null) {
      throw new IOException("KeyStore not found at " + storeLocation);
    }

    return storeIS;
  }

  private static KeyStore readKeyStore(final InputStream inputStream, final String storePwd)
      throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
    KeyStore store = KeyStore.getInstance("PKCS12");
    store.load(inputStream, storePwd != null ? storePwd.toCharArray() : null);
    return store;
  }
}
