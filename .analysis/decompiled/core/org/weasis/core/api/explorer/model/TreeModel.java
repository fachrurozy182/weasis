/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.explorer.model;

import java.util.Collection;
import java.util.List;
import org.weasis.core.api.explorer.model.TreeModelNode;
import org.weasis.core.api.media.data.MediaSeriesGroup;

public interface TreeModel {
    public List<TreeModelNode> getModelStructure();

    public Collection<MediaSeriesGroup> getChildren(MediaSeriesGroup var1);

    public MediaSeriesGroup getHierarchyNode(MediaSeriesGroup var1, Object var2);

    public void addHierarchyNode(MediaSeriesGroup var1, MediaSeriesGroup var2);

    public void removeHierarchyNode(MediaSeriesGroup var1, MediaSeriesGroup var2);

    public MediaSeriesGroup getParent(MediaSeriesGroup var1, TreeModelNode var2);
}

