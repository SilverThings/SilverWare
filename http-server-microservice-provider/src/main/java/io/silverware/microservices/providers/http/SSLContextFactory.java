/*
 * -----------------------------------------------------------------------\
 * SilverWare
 *  
 * Copyright (C) 2010 - 2013 the original author or authors.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * -----------------------------------------------------------------------/
 */
package io.silverware.microservices.providers.http;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 * Class for creating {@link SSLContext}.
 *
 * @author Radek Koubsky (radekkoubsky@gmail.com)
 */
public class SSLContextFactory {
   private final String keystore;
   private final char[] keystorePwd;
   private final String truststore;
   private final char[] truststorePwd;

   /**
    * Ctor.
    *
    * @param keystore
    *       a string representing path to a keystore on filesystem or on classpath
    * @param keystorePwd
    *       password to the keystore
    * @param truststore
    *       a string representing path to a truststore on filesystem or on classpath
    * @param truststorePwd
    *       truststore password
    */
   public SSLContextFactory(final String keystore, final String keystorePwd, final String truststore,
         final String truststorePwd) {
      this.keystore = keystore;
      this.keystorePwd = keystore != null ? keystorePwd.toCharArray() : "".toCharArray();
      this.truststore = truststore;
      this.truststorePwd = truststorePwd != null ? truststorePwd.toCharArray() : "".toCharArray();
   }

   /**
    * Creates and initializes {@link SSLContext}.
    *
    * @return initialized instance of ssl context
    */
   public SSLContext createSSLContext() throws IOException {
      final SSLContext sslContext;
      try {
         sslContext = SSLContext.getInstance("TLS");
         sslContext.init(keyManagers(), trustManagers(), null);
      } catch (final NoSuchAlgorithmException | KeyManagementException e) {
         throw new IOException("Unable to initialize SSLContext", e);
      }
      return sslContext;
   }

   private KeyManager[] keyManagers() throws IOException {
      if (this.keystore == null || this.keystore.isEmpty()) {
         return null;
      }
      final KeyManagerFactory keyManagerFactory;
      try {
         keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
         keyManagerFactory.init(keyStore(this.keystore, this.keystorePwd), this.keystorePwd);
         return keyManagerFactory.getKeyManagers();
      } catch (UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException | IOException e) {
         throw new IOException("Unable to initialize KeyManagerFactory", e);
      }
   }

   private TrustManager[] trustManagers() throws IOException {
      if (this.truststore == null || this.truststore.isEmpty()) {
         return null;
      }
      final TrustManagerFactory trustManagerFactory;
      try {
         trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
         trustManagerFactory.init(keyStore(this.truststore, this.truststorePwd));
         return trustManagerFactory.getTrustManagers();
      } catch (final KeyStoreException | NoSuchAlgorithmException e) {
         throw new IOException("Unable to initialize TrustManagerFactory", e);
      }
   }

   /**
    * Tries to load keystore from local filesystem or classpath.
    *
    * @param location
    *       keystore location
    * @param password
    *       password to the keystore
    * @return keystore
    * @throws IOException
    *       if the keystore cannot be loaded
    */
   private KeyStore keyStore(final String location, final char[] password) throws IOException {
      InputStream ksStream = null;
      try {
         if (Paths.get(location).toFile().exists()) {
            ksStream = new FileInputStream(Paths.get(location).toFile());
         } else {
            ksStream = getClass().getClassLoader().getResourceAsStream(location);
         }
         if (ksStream != null) {
            final KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(ksStream, password);
            return keyStore;
         } else {
            throw new IOException(String.format("Cannot find KeyStore %s", location));
         }
      } catch (NoSuchAlgorithmException | CertificateException | IOException | KeyStoreException e) {
         throw new IOException(String.format("Unable to load KeyStore %s", location), e);
      } finally {
         if (ksStream != null) {
            ksStream.close();
         }
      }
   }
}
