/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class DataUri {
    private static final String CHARSET_OPTION_NAME = "charset";
    private static final String FILENAME_OPTION_NAME = "filename";
    private static final String CONTENT_DISPOSITION_OPTION_NAME = "content-disposition";
    @NotNull
    private final String mime;
    @Nullable
    private final Charset charset;
    @Nullable
    private final String filename;
    @Nullable
    private final String contentDisposition;
    private final byte @NotNull [] data;
    private static final Pattern PLUS = Pattern.compile("+", 16);

    public DataUri(String mime, Charset charset, byte[] data) {
        this(mime, charset, null, null, data);
    }

    public DataUri(@NotNull String mime, @Nullable Charset charset, @Nullable String filename, @Nullable String contentDisposition, byte @NotNull [] data) {
        this.mime = mime;
        this.charset = charset;
        this.filename = filename;
        this.contentDisposition = contentDisposition;
        this.data = data;
    }

    @NotNull
    public String mime() {
        return this.mime;
    }

    public byte @NotNull [] data() {
        return this.data;
    }

    @Nullable
    public Charset charset() {
        return this.charset;
    }

    @Nullable
    public String contentDisposition() {
        return this.contentDisposition;
    }

    @Nullable
    public String filename() {
        return this.filename;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DataUri)) {
            return false;
        }
        DataUri dataUri = (DataUri)o;
        return this.mime.equals(dataUri.mime) && Objects.equals(this.charset, dataUri.charset) && Objects.equals(this.filename, dataUri.filename) && Objects.equals(this.contentDisposition, dataUri.contentDisposition) && Arrays.equals(this.data, dataUri.data);
    }

    public int hashCode() {
        int result = Objects.hash(this.mime, this.charset, this.filename, this.contentDisposition);
        result = 31 * result + Arrays.hashCode(this.data);
        return result;
    }

    public static DataUri parse(@NotNull String uri, Charset charset) throws MalformedDataUriException {
        byte[] finalData;
        if (!uri.toLowerCase(Locale.ENGLISH).startsWith("data:")) {
            throw new MalformedDataUriException("URI must start with a case-insensitive `data:'");
        }
        if (-1 == uri.indexOf(44)) {
            throw new MalformedDataUriException("URI must contain a `,'");
        }
        List<String> supportedContentEncodings = Collections.singletonList("base64");
        String mimeType = "text/plain";
        String contentEncoding = "";
        boolean contentEncodingAlreadySet = false;
        HashMap<String, String> supportedValues = new HashMap<String, String>();
        supportedValues.put(CHARSET_OPTION_NAME, "");
        supportedValues.put(FILENAME_OPTION_NAME, "");
        supportedValues.put(CONTENT_DISPOSITION_OPTION_NAME, "");
        HashMap<String, Boolean> supportedValueSetBits = new HashMap<String, Boolean>();
        for (String key : supportedValues.keySet()) {
            supportedValueSetBits.put(key, false);
        }
        int comma = uri.indexOf(44);
        String temp = uri.substring("data:".length(), comma);
        String[] headers = temp.split(";");
        for (int header = 0; header < headers.length; ++header) {
            boolean valueSet;
            String nameCaseInsensitive;
            String name;
            String s = headers[header];
            s = s.toLowerCase(Locale.ENGLISH);
            int eq = s.indexOf(61);
            String value = "";
            if (-1 == eq) {
                name = DataUri.percentDecode(s, charset);
                name = name.trim();
            } else {
                name = s.substring(0, eq);
                name = DataUri.percentDecode(name, charset);
                name = name.trim();
                value = s.substring(eq + 1);
                value = DataUri.percentDecode(value, charset);
                value = value.trim();
            }
            if (0 == header && -1 == eq && !name.isEmpty()) {
                mimeType = name;
                continue;
            }
            if (-1 == eq) {
                nameCaseInsensitive = name.toLowerCase(Locale.ENGLISH);
                if (!supportedContentEncodings.contains(nameCaseInsensitive) || contentEncodingAlreadySet) continue;
                contentEncoding = name;
                contentEncodingAlreadySet = true;
                continue;
            }
            nameCaseInsensitive = name.toLowerCase(Locale.ENGLISH);
            if (value.isEmpty() || !supportedValues.containsKey(nameCaseInsensitive) || (valueSet = ((Boolean)supportedValueSetBits.get(nameCaseInsensitive)).booleanValue())) continue;
            supportedValues.put(nameCaseInsensitive, value);
            supportedValueSetBits.put(nameCaseInsensitive, true);
        }
        String data = uri.substring(comma + 1);
        data = DataUri.percentDecode(data, charset);
        String finalMimeType = mimeType;
        Charset finalCharset = ((String)supportedValues.get(CHARSET_OPTION_NAME)).isEmpty() ? null : Charset.forName((String)supportedValues.get(CHARSET_OPTION_NAME));
        String finalFilename = ((String)supportedValues.get(FILENAME_OPTION_NAME)).isEmpty() ? null : (String)supportedValues.get(FILENAME_OPTION_NAME);
        String finalContentDisposition = ((String)supportedValues.get(CONTENT_DISPOSITION_OPTION_NAME)).isEmpty() ? null : (String)supportedValues.get(CONTENT_DISPOSITION_OPTION_NAME);
        try {
            finalData = "base64".equalsIgnoreCase(contentEncoding) ? Base64.getMimeDecoder().decode(data) : data.getBytes(charset);
        }
        catch (RuntimeException e) {
            throw new MalformedDataUriException(e);
        }
        return new DataUri(finalMimeType, finalCharset, finalFilename, finalContentDisposition, finalData);
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("data:").append(this.mime()).append(";");
        if (this.charset != null) {
            s.append("charset=").append(this.charset.name()).append(";");
        }
        if (this.contentDisposition != null) {
            s.append("content-disposition=").append(this.contentDisposition).append(";");
        }
        if (this.filename != null) {
            s.append("filename=").append(this.filename).append(";");
        }
        s.append("base64,").append(new String(Base64.getEncoder().encode(this.data()), StandardCharsets.UTF_8));
        return s.toString();
    }

    private static String percentDecode(String s, Charset cs) {
        try {
            s = PLUS.matcher(s).replaceAll("%2B");
            return URLDecoder.decode(s, cs.name());
        }
        catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Charset `" + cs.name() + "' not supported", e);
        }
    }

    static final class MalformedDataUriException
    extends IOException {
        MalformedDataUriException(@NotNull String reason) {
            super(reason);
        }

        MalformedDataUriException(@NotNull Exception reason) {
            super(reason);
        }
    }
}

