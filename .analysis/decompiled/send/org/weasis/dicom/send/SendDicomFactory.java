/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.osgi.framework.BundleContext
 *  org.osgi.service.component.ComponentContext
 *  org.osgi.service.component.annotations.Activate
 *  org.osgi.service.component.annotations.Component
 *  org.osgi.service.component.annotations.Deactivate
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.gui.util.GuiUtils
 *  org.weasis.core.api.service.BundlePreferences
 *  org.weasis.core.util.PropertiesUtil
 *  org.weasis.dicom.explorer.DicomModel
 *  org.weasis.dicom.explorer.exp.CheckTreeModel
 *  org.weasis.dicom.explorer.exp.DicomExportFactory
 *  org.weasis.dicom.explorer.exp.ExportDicom
 */
package org.weasis.dicom.send;

import java.nio.file.Path;
import java.util.Hashtable;
import java.util.Properties;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.service.BundlePreferences;
import org.weasis.core.util.PropertiesUtil;
import org.weasis.dicom.explorer.DicomModel;
import org.weasis.dicom.explorer.exp.CheckTreeModel;
import org.weasis.dicom.explorer.exp.DicomExportFactory;
import org.weasis.dicom.explorer.exp.ExportDicom;
import org.weasis.dicom.send.SendDicomView;

@Component(service={DicomExportFactory.class})
public class SendDicomFactory
implements DicomExportFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(SendDicomFactory.class);
    static final Properties EXPORT_PERSISTENCE = new Properties();

    public ExportDicom createDicomExportPage(Hashtable<String, Object> properties) {
        if (properties != null && GuiUtils.getUICore().getSystemPreferences().getBooleanProperty("weasis.export.dicom.send", true)) {
            DicomModel dicomModel = (DicomModel)properties.get(DicomModel.class.getName());
            CheckTreeModel treeModel = (CheckTreeModel)properties.get(CheckTreeModel.class.getName());
            if (dicomModel != null && treeModel != null) {
                return new SendDicomView(dicomModel, treeModel).initGUI();
            }
        }
        return null;
    }

    @Activate
    protected void activate(ComponentContext context) {
        LOGGER.info("DICOM Send is activated");
        PropertiesUtil.loadProperties((Path)BundlePreferences.getFileInDataFolder((BundleContext)context.getBundleContext(), (String)"export.properties"), (Properties)EXPORT_PERSISTENCE);
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        LOGGER.info("DICOM Send is deactivated");
        PropertiesUtil.storeProperties((Path)BundlePreferences.getFileInDataFolder((BundleContext)context.getBundleContext(), (String)"export.properties"), (Properties)EXPORT_PERSISTENCE, null);
    }
}

