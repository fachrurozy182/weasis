/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.api.gui.util.PageItem
 */
package org.weasis.dicom.explorer.imp;

import javax.swing.JProgressBar;
import org.weasis.core.api.gui.util.PageItem;
import org.weasis.dicom.explorer.DicomModel;

public interface ImportDicom
extends PageItem {
    public void importDICOM(DicomModel var1, JProgressBar var2);
}

