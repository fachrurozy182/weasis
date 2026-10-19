/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.api.gui.util.PageItem
 */
package org.weasis.dicom.explorer.exp;

import java.io.IOException;
import javax.swing.JProgressBar;
import org.weasis.core.api.gui.util.PageItem;
import org.weasis.dicom.explorer.exp.CheckTreeModel;

public interface ExportDicom
extends PageItem {
    public void exportDICOM(CheckTreeModel var1, JProgressBar var2) throws IOException;
}

