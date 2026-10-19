/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.api.gui.util.GuiExecutor
 *  org.weasis.core.api.util.FontItem
 */
package org.weasis.dicom.explorer.main;

import java.awt.Component;
import java.util.ArrayList;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import org.weasis.core.api.gui.util.GuiExecutor;
import org.weasis.core.api.util.FontItem;
import org.weasis.dicom.explorer.exp.ExplorerTask;
import org.weasis.dicom.explorer.main.LoadingTaskPanel;

public class LoadingPanel
extends JPanel {
    private final ArrayList<ExplorerTask<?, ?>> tasks = new ArrayList();
    private final LoadingTaskPanel globalDownloadTask = new LoadingTaskPanel(true);

    public LoadingPanel() {
        this.setLayout(new BoxLayout(this, 1));
        this.setAlignmentX(0.0f);
        this.setAlignmentY(0.0f);
        this.globalDownloadTask.setFont(FontItem.MICRO_SEMIBOLD.getFont());
    }

    public void setGlobalMessage(String msg) {
        GuiExecutor.execute(() -> this.globalDownloadTask.setMessage(msg));
    }

    public boolean addTask(ExplorerTask<?, ?> task) {
        boolean update = false;
        if (task != null && !this.tasks.contains(task)) {
            this.tasks.add(task);
            if (task.isSubTask()) {
                if (this.getComponentZOrder(this.globalDownloadTask) == -1) {
                    this.add(this.globalDownloadTask);
                    update = true;
                }
                this.setGlobalMessage(task.getMessage());
            } else {
                LoadingTaskPanel taskPanel = new LoadingTaskPanel(task);
                this.add(taskPanel);
                update = true;
            }
        }
        return update;
    }

    public boolean removeTask(ExplorerTask<?, ?> task) {
        boolean update = false;
        if (task != null) {
            this.tasks.remove(task);
            if (task.isSubTask()) {
                if (this.getDownloadTaskNumber() == 0) {
                    this.remove(this.globalDownloadTask);
                    update = true;
                }
            } else {
                for (Component c : this.getComponents()) {
                    LoadingTaskPanel loadingTaskPanel;
                    if (!(c instanceof LoadingTaskPanel) || !task.equals((loadingTaskPanel = (LoadingTaskPanel)c).getTask())) continue;
                    this.remove(c);
                    task.stopProgress();
                    update = true;
                }
            }
        }
        return update;
    }

    public int getDownloadTaskNumber() {
        int i = 0;
        for (ExplorerTask<?, ?> explorerTask : this.tasks) {
            if (!explorerTask.isSubTask()) continue;
            ++i;
        }
        return i;
    }
}

