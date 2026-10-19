/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.internal.mime;

import java.util.List;

public class InvalidMagicMimeEntryException
extends Exception {
    public InvalidMagicMimeEntryException(Throwable cause) {
        super("Invalid Magic Mime Entry: Unknown entry", cause);
    }

    public InvalidMagicMimeEntryException(List<String> mimeMagicEntry, Throwable cause) {
        super("Invalid Magic Mime Entry: " + mimeMagicEntry.toString(), cause);
    }
}

