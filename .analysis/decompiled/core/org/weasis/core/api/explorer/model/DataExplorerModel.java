/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.explorer.model;

import java.beans.PropertyChangeListener;
import java.util.List;
import org.weasis.core.api.explorer.ObservableEvent;
import org.weasis.core.api.explorer.model.TreeModelNode;
import org.weasis.core.api.media.data.Codec;
import org.weasis.core.api.media.data.MediaElement;

public interface DataExplorerModel {
    public List<Codec<MediaElement>> getCodecPlugins();

    public void addPropertyChangeListener(PropertyChangeListener var1);

    public void removePropertyChangeListener(PropertyChangeListener var1);

    public void firePropertyChange(ObservableEvent var1);

    public TreeModelNode getTreeModelNodeForNewPlugin();
}

