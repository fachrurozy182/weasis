/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.osgi.service.component.annotations.Component
 *  org.weasis.core.api.gui.Insertable
 *  org.weasis.core.api.gui.PreferencesPageFactory
 *  org.weasis.core.api.gui.util.AbstractItemDialogPage
 */
package org.weasis.dicom.explorer.pref.download;

import java.util.Hashtable;
import org.osgi.service.component.annotations.Component;
import org.weasis.core.api.gui.Insertable;
import org.weasis.core.api.gui.PreferencesPageFactory;
import org.weasis.core.api.gui.util.AbstractItemDialogPage;
import org.weasis.dicom.explorer.pref.download.DicomExplorerPrefView;

@Component(service={PreferencesPageFactory.class})
public class DicomExplorerPrefFactory
implements PreferencesPageFactory {
    public AbstractItemDialogPage createInstance(Hashtable<String, Object> properties) {
        return new DicomExplorerPrefView();
    }

    public boolean isComponentCreatedByThisFactory(Insertable component) {
        return component instanceof DicomExplorerPrefView;
    }
}

