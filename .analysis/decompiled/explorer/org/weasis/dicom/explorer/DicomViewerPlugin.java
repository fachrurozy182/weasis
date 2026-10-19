/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.api.explorer.DataExplorerView
 *  org.weasis.core.api.explorer.ObservableEvent
 *  org.weasis.core.api.explorer.ObservableEvent$BasicAction
 *  org.weasis.core.api.explorer.model.DataExplorerModel
 *  org.weasis.core.api.gui.layout.MigLayoutModel
 *  org.weasis.core.api.gui.util.GuiUtils
 *  org.weasis.core.ui.editor.image.ImageViewerEventManager
 *  org.weasis.core.ui.editor.image.ImageViewerPlugin
 *  org.weasis.dicom.codec.DicomImageElement
 */
package org.weasis.dicom.explorer;

import javax.swing.Icon;
import org.weasis.core.api.explorer.DataExplorerView;
import org.weasis.core.api.explorer.ObservableEvent;
import org.weasis.core.api.explorer.model.DataExplorerModel;
import org.weasis.core.api.gui.layout.MigLayoutModel;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.ui.editor.image.ImageViewerEventManager;
import org.weasis.core.ui.editor.image.ImageViewerPlugin;
import org.weasis.dicom.codec.DicomImageElement;
import org.weasis.dicom.explorer.DicomModel;
import org.weasis.dicom.explorer.main.DicomExplorer;

public abstract class DicomViewerPlugin
extends ImageViewerPlugin<DicomImageElement> {
    protected DicomViewerPlugin(ImageViewerEventManager<DicomImageElement> eventManager, String pluginName) {
        super(eventManager, pluginName);
    }

    protected DicomViewerPlugin(ImageViewerEventManager<DicomImageElement> eventManager, MigLayoutModel layoutModel, String uid, String pluginName, Icon icon, String tooltips) {
        super(eventManager, layoutModel, uid, pluginName, icon, tooltips);
    }

    public void setSelected(boolean selected) {
        if (selected) {
            DataExplorerModel dataExplorerModel;
            DataExplorerView dicomView;
            if (this.eventManager.getSelectedView2dContainer() != this) {
                this.eventManager.setSelectedView2dContainer((ImageViewerPlugin)this);
            }
            if ((dicomView = GuiUtils.getUICore().getExplorerPlugin(DicomExplorer.NAME)) != null && (dataExplorerModel = dicomView.getDataExplorerModel()) instanceof DicomModel) {
                DicomModel model = (DicomModel)dataExplorerModel;
                model.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.SELECT, (Object)this, null, (Object)this.getGroupID()));
            }
        } else {
            this.eventManager.setSelectedView2dContainer(null);
        }
    }
}

