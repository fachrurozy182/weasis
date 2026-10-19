/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.explorer;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;
import javax.swing.Action;
import org.weasis.core.api.explorer.model.DataExplorerModel;
import org.weasis.core.api.gui.util.GUIEntry;

public interface DataExplorerView
extends PropertyChangeListener,
GUIEntry {
    public void dispose();

    public DataExplorerModel getDataExplorerModel();

    public List<Action> getOpenImportDialogAction();

    public List<Action> getOpenExportDialogAction();

    public void importFiles(File[] var1, boolean var2);

    public boolean canImportFiles();
}

