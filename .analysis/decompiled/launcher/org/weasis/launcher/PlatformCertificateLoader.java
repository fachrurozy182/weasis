/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.launcher;

import com.formdev.flatlaf.util.SystemInfo;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Enumeration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlatformCertificateLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlatformCertificateLoader.class);
    private static volatile CompletableFuture<Void> setupTask;

    public static void startAsyncSetup() {
        long startTime = System.currentTimeMillis();
        setupTask = CompletableFuture.runAsync(() -> {
            try {
                PlatformCertificateLoader.setupDefaultSSLContext();
                LOGGER.info("*PERF* Loading system trust store, type:INIT time:{}", (Object)(System.currentTimeMillis() - startTime));
            }
            catch (Exception e) {
                LOGGER.error("Cannot setup default SSL context by merging with system certificates", e);
            }
        }, runnable -> {
            Thread thread = new Thread(runnable, "weasis-ssl-truststore");
            thread.setDaemon(true);
            thread.start();
        });
    }

    public static void awaitReady() {
        CompletableFuture<Void> task = setupTask;
        if (task == null || task.isDone()) {
            return;
        }
        long start = System.currentTimeMillis();
        try {
            task.get(30L, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.warn("Interrupted while waiting for the system trust store setup");
            return;
        }
        catch (ExecutionException | TimeoutException e) {
            LOGGER.warn("System trust store setup did not complete in time, using the default", e);
            return;
        }
        long waited = System.currentTimeMillis() - start;
        if (waited > 5L) {
            LOGGER.info("*PERF* Awaited system trust store, type:INIT time:{}", (Object)waited);
        }
    }

    public static void main(String[] args) throws Exception {
        long startTime = System.nanoTime();
        KeyStore mergedTrustStore = PlatformCertificateLoader.setupDefaultSSLContext();
        System.out.printf("Setup took: %.2f ms%n", (double)(System.nanoTime() - startTime) / 1000000.0);
        PlatformCertificateLoader.printTrustStoreAliases(mergedTrustStore);
    }

    public static KeyStore setupDefaultSSLContext() throws Exception {
        KeyStore platformTrustStore = PlatformCertificateLoader.loadPlatformTrustStore();
        KeyStore javaTrustStore = PlatformCertificateLoader.loadJavaDefaultTrustStore();
        KeyStore mergedTrustStore = PlatformCertificateLoader.mergeTrustStores(platformTrustStore, javaTrustStore);
        SSLContext sslContext = PlatformCertificateLoader.createSSLContext(mergedTrustStore);
        SSLContext.setDefault(sslContext);
        return mergedTrustStore;
    }

    public static KeyStore loadPlatformTrustStore() throws Exception {
        if (SystemInfo.isWindows) {
            return PlatformCertificateLoader.loadKeyStore("Windows-ROOT", null);
        }
        if (SystemInfo.isMacOS) {
            return PlatformCertificateLoader.loadKeyStore("KeychainStore", null);
        }
        return PlatformCertificateLoader.loadLinuxTrustStore();
    }

    private static KeyStore loadLinuxTrustStore() throws Exception {
        String[] TRUSTED_LINUX_PATHS;
        for (String path : TRUSTED_LINUX_PATHS = new String[]{"/etc/ssl/certs/java/cacerts", "/etc/pki/java/cacerts"}) {
            if (!Files.isReadable(Paths.get(path, new String[0]))) continue;
            return PlatformCertificateLoader.loadKeyStore(KeyStore.getDefaultType(), path);
        }
        throw new Exception("No accessible Linux trust store found.");
    }

    public static KeyStore loadJavaDefaultTrustStore() throws Exception {
        String trustStorePath = Paths.get(System.getProperty("java.home"), "lib", "security", "cacerts").toString();
        if (!Files.exists(Paths.get(trustStorePath, new String[0]), new LinkOption[0])) {
            throw new Exception("Default Java trust store not found at: " + trustStorePath);
        }
        return PlatformCertificateLoader.loadKeyStore(KeyStore.getDefaultType(), trustStorePath, "changeit".toCharArray());
    }

    private static KeyStore loadKeyStore(String type, String filePath) throws Exception {
        return PlatformCertificateLoader.loadKeyStore(type, filePath, null);
    }

    private static KeyStore loadKeyStore(String type, String filePath, char[] password) throws Exception {
        KeyStore keyStore = KeyStore.getInstance(type);
        if (filePath == null) {
            keyStore.load(null, null);
        } else {
            try (FileInputStream fis = new FileInputStream(filePath);){
                keyStore.load(fis, password);
            }
        }
        return keyStore;
    }

    public static KeyStore mergeTrustStores(KeyStore store1, KeyStore store2) throws Exception {
        KeyStore mergedTrustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        mergedTrustStore.load(null, null);
        PlatformCertificateLoader.copyCertificates(store1, mergedTrustStore, "");
        PlatformCertificateLoader.copyCertificates(store2, mergedTrustStore, "java-");
        return mergedTrustStore;
    }

    private static void copyCertificates(KeyStore source, KeyStore destination, String aliasPrefix) throws Exception {
        Enumeration<String> aliases = source.aliases();
        while (aliases.hasMoreElements()) {
            Certificate certificate;
            String alias = aliases.nextElement();
            if (!source.isCertificateEntry(alias) || PlatformCertificateLoader.isCertificatePresentInStore(certificate = source.getCertificate(alias), destination)) continue;
            String uniqueAlias = aliasPrefix + alias;
            destination.setCertificateEntry(uniqueAlias, certificate);
        }
    }

    private static boolean isCertificatePresentInStore(Certificate certificate, KeyStore keyStore) throws Exception {
        Enumeration<String> aliases = keyStore.aliases();
        while (aliases.hasMoreElements()) {
            Certificate existingCert;
            String alias = aliases.nextElement();
            if (!keyStore.isCertificateEntry(alias) || !(existingCert = keyStore.getCertificate(alias)).equals(certificate)) continue;
            return true;
        }
        return false;
    }

    public static SSLContext createSSLContext(KeyStore trustStore) throws Exception {
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), null);
        return sslContext;
    }

    private static void printTrustStoreAliases(KeyStore trustStore) throws Exception {
        System.out.println("Merged Trust Store Aliases:");
        Enumeration<String> aliases = trustStore.aliases();
        while (aliases.hasMoreElements()) {
            System.out.println(" - " + aliases.nextElement());
        }
    }
}

