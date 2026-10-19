/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.explorer.model;

import java.util.Objects;
import org.weasis.core.api.media.data.TagView;
import org.weasis.core.api.media.data.TagW;

public record TreeModelNode(int depth, int nodePosition, TagW tagElement, TagView tagView) {
    public static final TreeModelNode ROOT = new TreeModelNode(0, 0, TagW.RootElement, null);

    public TreeModelNode(int depth, int nodePosition, TagW tagElement, TagView tagView) {
        this.depth = depth;
        this.nodePosition = nodePosition;
        this.tagElement = Objects.requireNonNull(tagElement);
        this.tagView = tagView;
    }
}

