/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.media.data;

import java.io.File;

public interface Thumbnailable {
    public void registerListeners();

    public File getThumbnailPath();

    public void dispose();

    public void removeMouseAndKeyListener();
}

