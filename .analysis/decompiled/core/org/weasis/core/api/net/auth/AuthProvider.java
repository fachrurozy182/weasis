/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.core.api.net.auth;

import org.weasis.core.util.StringUtil;

public record AuthProvider(String name, String authorizationUri, String tokenUri, String revokeTokenUri, boolean openId) {
    public boolean supportsTokenRevocation() {
        return StringUtil.hasText((CharSequence)this.revokeTokenUri);
    }
}

