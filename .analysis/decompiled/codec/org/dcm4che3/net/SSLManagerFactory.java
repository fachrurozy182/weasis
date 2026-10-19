/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import org.dcm4che3.util.SafeClose;
import org.dcm4che3.util.StreamUtils;

public abstract class SSLManagerFactory {
    public static KeyStore createKeyStore(X509Certificate ... certs) throws KeyStoreException {
        KeyStore ks = KeyStore.getInstance("PKCS12");
        try {
            ks.load(null);
        }
        catch (IOException e) {
            throw new AssertionError((Object)e);
        }
        catch (NoSuchAlgorithmException e) {
            throw new AssertionError((Object)e);
        }
        catch (CertificateException e) {
            throw new AssertionError((Object)e);
        }
        for (X509Certificate cert : certs) {
            ks.setCertificateEntry(cert.getSubjectX500Principal().getName(), cert);
        }
        return ks;
    }

    public static KeyStore loadKeyStore(String type, String url, String password) throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
        return SSLManagerFactory.loadKeyStore(type, url, password.toCharArray());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static KeyStore loadKeyStore(String type, String url, char[] password) throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
        KeyStore ks = KeyStore.getInstance(type);
        InputStream in = StreamUtils.openFileOrURL(url);
        try {
            ks.load(in, password);
        }
        finally {
            SafeClose.close(in);
        }
        return ks;
    }

    public static KeyManager createKeyManager(String type, String url, char[] storePassword, char[] keyPassword) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        return SSLManagerFactory.createKeyManager(SSLManagerFactory.loadKeyStore(type, url, storePassword), keyPassword);
    }

    public static KeyManager createKeyManager(String type, String url, String storePassword, String keyPassword) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        return SSLManagerFactory.createKeyManager(SSLManagerFactory.loadKeyStore(type, url, storePassword), keyPassword);
    }

    public static KeyManager createKeyManager(KeyStore ks, String password) throws UnrecoverableKeyException, KeyStoreException {
        return SSLManagerFactory.createKeyManager(ks, password.toCharArray());
    }

    public static KeyManager createKeyManager(KeyStore ks, char[] password) throws UnrecoverableKeyException, KeyStoreException {
        try {
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, password);
            KeyManager[] kms = kmf.getKeyManagers();
            return kms.length > 0 ? kms[0] : null;
        }
        catch (NoSuchAlgorithmException e) {
            throw new AssertionError((Object)e);
        }
    }

    public static TrustManager createTrustManager(KeyStore ks) throws KeyStoreException {
        try {
            TrustManagerFactory kmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            kmf.init(ks);
            TrustManager[] tms = kmf.getTrustManagers();
            return tms.length > 0 ? tms[0] : null;
        }
        catch (NoSuchAlgorithmException e) {
            throw new AssertionError((Object)e);
        }
    }

    public static TrustManager createTrustManager(X509Certificate ... certs) throws KeyStoreException {
        return SSLManagerFactory.createTrustManager(SSLManagerFactory.createKeyStore(certs));
    }

    public static TrustManager createTrustManager(String type, String url, char[] password) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        return SSLManagerFactory.createTrustManager(SSLManagerFactory.loadKeyStore(type, url, password));
    }

    public static TrustManager createTrustManager(String type, String url, String password) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        return SSLManagerFactory.createTrustManager(SSLManagerFactory.loadKeyStore(type, url, password));
    }
}

