/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.param;

import java.util.List;
import java.util.Objects;

public record TlsOptions(List<String> cipherSuites, List<String> tlsProtocols, boolean tlsNeedClientAuth, String keystoreURL, String keystoreType, String keystorePass, String keyPass, String truststoreURL, String truststoreType, String truststorePass) {
    private final List<String> cipherSuites;
    private final List<String> tlsProtocols;
    public static final List<String> TLS = List.of("SSL_RSA_WITH_NULL_SHA", "TLS_RSA_WITH_AES_128_CBC_SHA", "SSL_RSA_WITH_3DES_EDE_CBC_SHA");
    public static final List<String> TLS_NULL = List.of("SSL_RSA_WITH_NULL_SHA");
    public static final List<String> TLS_3DES = List.of("SSL_RSA_WITH_3DES_EDE_CBC_SHA");
    public static final List<String> TLS_AES = List.of("TLS_RSA_WITH_AES_128_CBC_SHA", "SSL_RSA_WITH_3DES_EDE_CBC_SHA");
    @Deprecated(since="Java 17", forRemoval=false)
    public static final List<String> DEFAULT_PROTOCOLS = List.of("TLSv1", "SSLv3");
    public static final List<String> TLS_1_0 = List.of("TLSv1");
    public static final List<String> TLS_1_1 = List.of("TLSv1.1");
    public static final List<String> TLS_1_2 = List.of("TLSv1.2");
    public static final List<String> TLS_1_3 = List.of("TLSv1.3");
    public static final List<String> SSL2_HELLO = List.of("SSLv2Hello", "SSLv3", "TLSv1", "TLSv1.1", "TLSv1.2");
    public static final List<String> MODERN_TLS = List.of("TLSv1.2", "TLSv1.3");

    public TlsOptions(boolean tlsNeedClientAuth, String keystoreURL, String keystoreType, String keystorePass, String keyPass, String truststoreURL, String truststoreType, String truststorePass) {
        this(TLS, MODERN_TLS, tlsNeedClientAuth, keystoreURL, keystoreType, keystorePass, keyPass, truststoreURL, truststoreType, truststorePass);
    }

    public TlsOptions(List<String> cipherSuites, List<String> tlsProtocols, boolean tlsNeedClientAuth, String keystoreURL, String keystoreType, String keystorePass, String keyPass, String truststoreURL, String truststoreType, String truststorePass) {
        this.cipherSuites = Objects.requireNonNull(cipherSuites, "cipherSuites cannot be null");
        this.tlsProtocols = tlsProtocols;
        this.tlsNeedClientAuth = tlsNeedClientAuth;
        this.keystoreURL = keystoreURL;
        this.keystoreType = keystoreType;
        this.keystorePass = keystorePass;
        this.keyPass = keyPass;
        this.truststoreURL = truststoreURL;
        this.truststoreType = truststoreType;
        this.truststorePass = truststorePass;
    }

    public List<String> cipherSuites() {
        return List.copyOf(this.cipherSuites);
    }

    public List<String> tlsProtocols() {
        return this.tlsProtocols != null ? List.copyOf(this.tlsProtocols) : null;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof TlsOptions)) return false;
        TlsOptions other = (TlsOptions)((Object)obj);
        if (!Objects.equals(this.cipherSuites, other.cipherSuites)) return false;
        if (!Objects.equals(this.tlsProtocols, other.tlsProtocols)) return false;
        if (this.tlsNeedClientAuth != other.tlsNeedClientAuth) return false;
        if (!Objects.equals(this.keystoreURL, other.keystoreURL)) return false;
        if (!Objects.equals(this.keystoreType, other.keystoreType)) return false;
        if (!Objects.equals(this.keystorePass, other.keystorePass)) return false;
        if (!Objects.equals(this.keyPass, other.keyPass)) return false;
        if (!Objects.equals(this.truststoreURL, other.truststoreURL)) return false;
        if (!Objects.equals(this.truststoreType, other.truststoreType)) return false;
        if (!Objects.equals(this.truststorePass, other.truststorePass)) return false;
        return true;
    }

    public String toString() {
        return "TlsOptions{\n  cipherSuites=%s,\n  tlsProtocols=%s,\n  tlsNeedClientAuth=%s,\n  keystoreURL='%s',\n  keystoreType='%s',\n  truststoreURL='%s',\n  truststoreType='%s'\n}".formatted(new Object[]{this.cipherSuites, this.tlsProtocols, this.tlsNeedClientAuth, this.keystoreURL, this.keystoreType, this.truststoreURL, this.truststoreType});
    }
}

