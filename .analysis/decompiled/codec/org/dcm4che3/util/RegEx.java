/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.util;

import java.util.regex.Pattern;

public class RegEx {
    private final Pattern pattern;

    public RegEx(String pattern) {
        this.pattern = Pattern.compile(pattern);
    }

    public boolean match(String value) {
        return this.pattern.matcher(value).matches();
    }
}

