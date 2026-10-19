/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.net;

import java.io.IOException;

public class StallTimeoutException
extends IOException {
    private static final long serialVersionUID = 1L;

    public StallTimeoutException(String message) {
        super(message);
    }

    public StallTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}

