/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package org.weasis.core.api.explorer.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.swing.SwingUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.command.Option;
import org.weasis.core.api.command.Options;
import org.weasis.core.api.explorer.ObservableEvent;
import org.weasis.core.api.explorer.model.DataExplorerModel;
import org.weasis.core.api.explorer.model.Tree;
import org.weasis.core.api.explorer.model.TreeModel;
import org.weasis.core.api.explorer.model.TreeModelNode;
import org.weasis.core.api.gui.util.GuiExecutor;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.media.data.Codec;
import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.api.media.data.MediaSeriesGroupNode;
import org.weasis.core.api.media.data.TagView;
import org.weasis.core.api.media.data.TagW;

public abstract class AbstractFileModel
implements TreeModel,
DataExplorerModel {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractFileModel.class);
    public static final List<String> functions = List.of("get", "close");
    public static final String NAME = "All Files";
    public static final TreeModelNode group = new TreeModelNode(1, 0, TagW.Group, new TagView(TagW.Group, TagW.FileName));
    public static final TreeModelNode series = new TreeModelNode(2, 0, TagW.SubseriesInstanceUID, new TagView(TagW.FileName));
    private static final List<TreeModelNode> modelStructure = Arrays.asList(TreeModelNode.ROOT, group, series);
    private final Tree<MediaSeriesGroup> model = new Tree<MediaSeriesGroup>(MediaSeriesGroupNode.rootNode);
    private PropertyChangeSupport propertyChange = null;

    protected AbstractFileModel() {
    }

    @Override
    public List<Codec<MediaElement>> getCodecPlugins() {
        return GuiUtils.getUICore().getCodecPlugins();
    }

    @Override
    public Collection<MediaSeriesGroup> getChildren(MediaSeriesGroup node) {
        return this.model.getSuccessors(node);
    }

    @Override
    public MediaSeriesGroup getHierarchyNode(MediaSeriesGroup parent, Object value) {
        if (parent != null || value != null) {
            for (MediaSeriesGroup node : this.model.getSuccessors(parent)) {
                if (!node.matchIdValue(value)) continue;
                return node;
            }
        }
        return null;
    }

    @Override
    public void addHierarchyNode(MediaSeriesGroup root, MediaSeriesGroup leaf) {
        this.model.addLeaf(root, leaf);
    }

    @Override
    public void removeHierarchyNode(MediaSeriesGroup root, MediaSeriesGroup leaf) {
        Tree<MediaSeriesGroup> tree = this.model.getTree(root);
        if (tree != null) {
            tree.removeLeaf(leaf);
        }
    }

    @Override
    public MediaSeriesGroup getParent(MediaSeriesGroup node, TreeModelNode modelNode) {
        if (null != node && modelNode != null) {
            if (node.getTagID().equals(modelNode.tagElement())) {
                return node;
            }
            Tree<MediaSeriesGroup> tree = this.model.getTree(node);
            if (tree != null) {
                Tree<MediaSeriesGroup> parent;
                while ((parent = tree.getParent()) != null) {
                    if (parent.getHead().getTagID().equals(modelNode.tagElement())) {
                        return parent.getHead();
                    }
                    tree = parent;
                }
            }
        }
        return null;
    }

    public void dispose() {
        for (MediaSeriesGroup mediaSeriesGroup : this.getChildren(MediaSeriesGroupNode.rootNode)) {
            mediaSeriesGroup.dispose();
        }
        this.model.clear();
    }

    public String toString() {
        return NAME;
    }

    @Override
    public List<TreeModelNode> getModelStructure() {
        return modelStructure;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener propertychangelistener) {
        if (this.propertyChange == null) {
            this.propertyChange = new PropertyChangeSupport(this);
        }
        this.propertyChange.addPropertyChangeListener(propertychangelistener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener propertychangelistener) {
        if (this.propertyChange != null) {
            this.propertyChange.removePropertyChangeListener(propertychangelistener);
        }
    }

    @Override
    public void firePropertyChange(ObservableEvent event) {
        if (this.propertyChange != null) {
            if (event == null) {
                throw new NullPointerException();
            }
            if (SwingUtilities.isEventDispatchThread()) {
                this.propertyChange.firePropertyChange(event);
            } else {
                SwingUtilities.invokeLater(() -> this.propertyChange.firePropertyChange(event));
            }
        }
    }

    public void removeTopGroup(MediaSeriesGroup topGroup) {
        if (topGroup != null) {
            this.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.REMOVE, this, null, (Object)topGroup));
            Collection<MediaSeriesGroup> seriesList = this.getChildren(topGroup);
            for (MediaSeriesGroup mediaSeriesGroup : seriesList) {
                mediaSeriesGroup.dispose();
            }
            this.removeHierarchyNode(MediaSeriesGroupNode.rootNode, topGroup);
            LOGGER.info("Remove Group: {}", (Object)topGroup);
        }
    }

    public void removeSeries(MediaSeriesGroup seriesGroup) {
        if (seriesGroup != null) {
            this.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.REMOVE, this, null, (Object)seriesGroup));
            MediaSeriesGroup topGroup = this.getParent(seriesGroup, group);
            this.removeHierarchyNode(topGroup, seriesGroup);
            seriesGroup.dispose();
            LOGGER.info("Remove Series/Image: {}", (Object)seriesGroup);
        }
    }

    public abstract void get(String[] var1) throws IOException;

    public void close(String[] argv) throws IOException {
        String[] usage = new String[]{"Close images", "Usage: dicom:close (-a | ([-g UID]... [-s UID]...)) ", "  -a --all         close all series", "  -g --group=UID   close a group from its UID", "  -s --series=UID   close an series/image from its UID", "  -? --help        show help"};
        Option opt = Options.compile(usage).parse(argv);
        List<String> gargs = opt.getList("group");
        List<String> iargs = opt.getList("series");
        if (opt.isSet("help") || gargs.isEmpty() && iargs.isEmpty() && !opt.isSet("all")) {
            opt.usage();
            return;
        }
        GuiExecutor.execute(() -> {
            block6: {
                AbstractFileModel dataModel;
                block5: {
                    dataModel = this;
                    this.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.SELECT, dataModel, null, (Object)dataModel));
                    if (!opt.isSet("all")) break block5;
                    for (MediaSeriesGroup g : this.model.getSuccessors(MediaSeriesGroupNode.rootNode)) {
                        dataModel.removeTopGroup(g);
                    }
                    break block6;
                }
                if (opt.isSet("group")) {
                    for (String gUID : gargs) {
                        dataModel.removeTopGroup(this.getHierarchyNode(MediaSeriesGroupNode.rootNode, gUID));
                    }
                }
                if (!opt.isSet("series")) break block6;
                block2: for (String uid : iargs) {
                    for (MediaSeriesGroup topGroup : this.model.getSuccessors(MediaSeriesGroupNode.rootNode)) {
                        MediaSeriesGroup s = this.getHierarchyNode(topGroup, uid);
                        if (s == null) continue;
                        this.removeSeries(s);
                        continue block2;
                    }
                }
            }
        });
    }

    @Override
    public TreeModelNode getTreeModelNodeForNewPlugin() {
        return group;
    }
}

