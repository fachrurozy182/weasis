/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.parser.impl;

import com.github.weisj.jsvg.parser.impl.ParserUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Url {
    @NotNull
    private final String rawUrl;
    @Nullable
    private final String url;
    @Nullable
    private final String fragment;

    public Url(@NotNull String rawUrl, @Nullable String url, @Nullable String fragment) {
        this.rawUrl = rawUrl;
        this.url = url;
        this.fragment = fragment;
    }

    @Nullable
    public static Url parse(@Nullable String value, RequireFragment requireFragment) {
        String[] split;
        if (value == null) {
            return null;
        }
        String urlString = value;
        if (urlString.startsWith("url(")) {
            if (!urlString.endsWith(")")) {
                return null;
            }
            urlString = ParserUtil.removeWhiteSpace(urlString.substring(4, urlString.length() - 1));
        }
        if ((split = urlString.split("#", 2)).length == 0) {
            return null;
        }
        if (requireFragment == RequireFragment.YES && split.length != 2) {
            return null;
        }
        String url = Url.nullIfEmpty(split[0]);
        String fragment = Url.nullIfEmpty(split.length == 2 ? split[1] : null);
        if (url == null && fragment == null) {
            return null;
        }
        return new Url(urlString, url, fragment);
    }

    @Nullable
    private static String nullIfEmpty(@Nullable String s) {
        return s == null || s.isEmpty() ? null : s;
    }

    @NotNull
    public String rawUrl() {
        return this.rawUrl;
    }

    @Nullable
    public String url() {
        return this.url;
    }

    @Nullable
    public String fragment() {
        return this.fragment;
    }

    public static enum RequireFragment {
        YES,
        NO;

    }
}

