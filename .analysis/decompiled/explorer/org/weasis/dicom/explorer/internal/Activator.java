/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.osgi.annotation.bundle.Header
 *  org.osgi.framework.BundleActivator
 *  org.osgi.framework.BundleContext
 *  org.weasis.core.api.explorer.DataExplorerView
 *  org.weasis.core.api.explorer.model.DataExplorerModel
 *  org.weasis.core.api.gui.util.GuiUtils
 *  org.weasis.core.api.service.BundlePreferences
 *  org.weasis.core.util.PropertiesUtil
 *  org.weasis.dicom.codec.seg.SegSpecialElement
 *  org.weasis.dicom.codec.seg.SegmentationVolumeBuildExecutor
 */
package org.weasis.dicom.explorer.internal;

import java.nio.file.Path;
import java.util.Properties;
import org.osgi.annotation.bundle.Header;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.weasis.core.api.explorer.DataExplorerView;
import org.weasis.core.api.explorer.model.DataExplorerModel;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.service.BundlePreferences;
import org.weasis.core.util.PropertiesUtil;
import org.weasis.dicom.codec.seg.SegSpecialElement;
import org.weasis.dicom.codec.seg.SegmentationVolumeBuildExecutor;
import org.weasis.dicom.explorer.DicomModel;
import org.weasis.dicom.explorer.LocalPersistence;
import org.weasis.dicom.explorer.UISegmentationVolumeBuildExecutor;
import org.weasis.dicom.explorer.main.DicomExplorer;
import org.weasis.dicom.explorer.wado.DicomManager;

@Header(name="Bundle-Activator", value="${@class}")
public class Activator
implements BundleActivator {
    public void start(BundleContext context) {
        String cache = context.getProperty("weasis.portable.dicom.cache");
        DicomManager.getInstance().setPortableDirCache(cache == null || !cache.equalsIgnoreCase(Boolean.FALSE.toString()));
        PropertiesUtil.loadProperties((Path)BundlePreferences.getFileInDataFolder((BundleContext)context, (String)"import-export.properties"), (Properties)LocalPersistence.getProperties());
        SegSpecialElement.setVolumeBuildExecutor((SegmentationVolumeBuildExecutor)new UISegmentationVolumeBuildExecutor());
    }

    public void stop(BundleContext context) {
        DataExplorerModel dataExplorerModel;
        SegSpecialElement.setVolumeBuildExecutor(null);
        PropertiesUtil.storeProperties((Path)BundlePreferences.getFileInDataFolder((BundleContext)context, (String)"import-export.properties"), (Properties)LocalPersistence.getProperties(), null);
        DicomModel.LOADING_EXECUTOR.shutdownNow();
        DataExplorerView explorer = GuiUtils.getUICore().getExplorerPlugin(DicomExplorer.NAME);
        if (explorer != null && (dataExplorerModel = explorer.getDataExplorerModel()) instanceof DicomModel) {
            DicomModel dicomModel = (DicomModel)dataExplorerModel;
            dicomModel.dispose();
        }
    }
}

