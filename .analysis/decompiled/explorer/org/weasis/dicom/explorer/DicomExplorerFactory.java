/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.osgi.service.component.ComponentContext
 *  org.osgi.service.component.annotations.Activate
 *  org.osgi.service.component.annotations.Component
 *  org.osgi.service.component.annotations.Deactivate
 *  org.osgi.service.component.annotations.Reference
 *  org.weasis.core.api.explorer.DataExplorerView
 *  org.weasis.core.api.explorer.DataExplorerViewFactory
 *  org.weasis.core.api.explorer.ObservableEvent
 *  org.weasis.core.api.explorer.ObservableEvent$BasicAction
 *  org.weasis.core.api.gui.util.GuiUtils
 *  org.weasis.core.ui.editor.ViewerPluginBuilder
 */
package org.weasis.dicom.explorer;

import java.util.Hashtable;
import java.util.List;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.weasis.core.api.explorer.DataExplorerView;
import org.weasis.core.api.explorer.DataExplorerViewFactory;
import org.weasis.core.api.explorer.ObservableEvent;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.ui.editor.ViewerPluginBuilder;
import org.weasis.dicom.explorer.DicomModel;
import org.weasis.dicom.explorer.exp.ExportToolBar;
import org.weasis.dicom.explorer.imp.ImportToolBar;
import org.weasis.dicom.explorer.main.DicomExplorer;

@Component(service={DataExplorerViewFactory.class})
public class DicomExplorerFactory
implements DataExplorerViewFactory {
    private DicomExplorer explorer = null;
    @Reference
    private DicomModel model;

    public DataExplorerView createDataExplorerView(Hashtable<String, Object> properties) {
        if (this.explorer == null) {
            this.explorer = new DicomExplorer(this.model);
            this.model.addPropertyChangeListener(this.explorer);
            List toolbar = GuiUtils.getUICore().getExplorerPluginToolbars();
            toolbar.add(new ImportToolBar(5, this.explorer));
            toolbar.add(new ExportToolBar(7, this.explorer));
            ViewerPluginBuilder.DefaultDataModel.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.NULL_SELECTION, (Object)this.explorer, null, null));
        }
        return this.explorer;
    }

    @Activate
    protected void activate(ComponentContext context) {
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        if (this.explorer != null) {
            DicomModel dataModel = this.explorer.getDataExplorerModel();
            dataModel.removePropertyChangeListener(this.explorer);
            GuiUtils.getUICore().getExplorerPluginToolbars().removeIf(b -> b.getComponent().getAttachedInsertable() == this.explorer);
        }
    }
}

