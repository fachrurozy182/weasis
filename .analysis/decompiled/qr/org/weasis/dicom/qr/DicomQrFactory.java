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
 *  org.weasis.dicom.explorer.imp.DicomImportFactory
 *  org.weasis.dicom.explorer.imp.ImportDicom
 */
package org.weasis.dicom.qr;

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
import org.weasis.dicom.explorer.imp.DicomImportFactory;
import org.weasis.dicom.explorer.imp.ImportDicom;
import org.weasis.dicom.qr.DicomQrView;

@Component(service={DicomImportFactory.class})
public class DicomQrFactory
implements DicomImportFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(DicomQrFactory.class);

    public ImportDicom createDicomImportPage(Hashtable<String, Object> properties) {
        if (GuiUtils.getUICore().getSystemPreferences().getBooleanProperty("weasis.import.dicom.qr", true)) {
            return new DicomQrView();
        }
        return null;
    }

    @Activate
    protected void activate(ComponentContext context) {
        LOGGER.info("DICOM Q/R is activated");
        PropertiesUtil.loadProperties((Path)BundlePreferences.getFileInDataFolder((BundleContext)context.getBundleContext(), (String)"import.properties"), (Properties)DicomQrView.getPersistence());
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        LOGGER.info("DICOM Q/R is deactivated");
        PropertiesUtil.storeProperties((Path)BundlePreferences.getFileInDataFolder((BundleContext)context.getBundleContext(), (String)"import.properties"), (Properties)DicomQrView.getPersistence(), null);
    }
}

