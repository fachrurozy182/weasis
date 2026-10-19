/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.testUtil;

import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.encoder.EncoderBase;
import java.nio.charset.Charset;

public class DummyEncoder<E>
extends EncoderBase<E> {
    public static final String DUMMY = "dummy" + CoreConstants.LINE_SEPARATOR;
    String val = DUMMY;
    String fileHeader;
    String fileFooter;
    Charset charset;

    public Charset getCharset() {
        return this.charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public DummyEncoder() {
    }

    public DummyEncoder(String val) {
        this.val = val;
    }

    @Override
    public byte[] encode(E event) {
        return this.encodeString(this.val);
    }

    byte[] encodeString(String s) {
        if (this.charset == null) {
            return s.getBytes();
        }
        return s.getBytes(this.charset);
    }

    private void appendIfNotNull(StringBuilder sb, String s) {
        if (s != null) {
            sb.append(s);
        }
    }

    byte[] header() {
        StringBuilder sb = new StringBuilder();
        this.appendIfNotNull(sb, this.fileHeader);
        if (sb.length() > 0) {
            sb.append(CoreConstants.LINE_SEPARATOR);
        }
        return this.encodeString(sb.toString());
    }

    @Override
    public byte[] headerBytes() {
        return this.header();
    }

    @Override
    public byte[] footerBytes() {
        if (this.fileFooter == null) {
            return null;
        }
        return this.encodeString(this.fileFooter);
    }

    public String getFileHeader() {
        return this.fileHeader;
    }

    public void setFileHeader(String fileHeader) {
        this.fileHeader = fileHeader;
    }

    public String getFileFooter() {
        return this.fileFooter;
    }

    public void setFileFooter(String fileFooter) {
        this.fileFooter = fileFooter;
    }
}

