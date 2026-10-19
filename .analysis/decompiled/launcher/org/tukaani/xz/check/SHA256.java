/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz.check;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.tukaani.xz.check.Check;

public class SHA256
extends Check {
    private final MessageDigest sha256;

    public SHA256() throws NoSuchAlgorithmException {
        this.size = 32;
        this.name = "SHA-256";
        this.sha256 = MessageDigest.getInstance("SHA-256");
    }

    @Override
    public void update(byte[] buf, int off, int len) {
        this.sha256.update(buf, off, len);
    }

    @Override
    public byte[] finish() {
        byte[] buf = this.sha256.digest();
        this.sha256.reset();
        return buf;
    }
}

