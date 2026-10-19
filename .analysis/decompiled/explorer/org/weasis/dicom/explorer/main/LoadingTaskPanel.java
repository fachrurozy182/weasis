/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.api.util.FontItem
 *  org.weasis.core.api.util.ResourceUtil
 *  org.weasis.core.api.util.ResourceUtil$ActionIcon
 *  org.weasis.core.api.util.ResourceUtil$ResourceIconPath
 *  org.weasis.core.ui.tp.raven.spinner.SpinnerProgress
 */
package org.weasis.dicom.explorer.main;

import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.weasis.core.api.util.FontItem;
import org.weasis.core.api.util.ResourceUtil;
import org.weasis.core.ui.tp.raven.spinner.SpinnerProgress;
import org.weasis.dicom.explorer.Messages;
import org.weasis.dicom.explorer.exp.ExplorerTask;
import org.weasis.dicom.explorer.wado.DownloadManager;

public class LoadingTaskPanel
extends JPanel {
    private final ExplorerTask<?, ?> task;
    private final boolean globalLoadingManager;
    private final JLabel message = new JLabel();

    public LoadingTaskPanel(ExplorerTask<?, ?> task) {
        this.task = task;
        this.globalLoadingManager = task.isGlobalLoadingManager();
        this.init();
    }

    public LoadingTaskPanel(boolean globalLoadingManager) {
        this.task = null;
        this.globalLoadingManager = globalLoadingManager;
        this.init();
    }

    private void init() {
        this.message.setFont(FontItem.MICRO_SEMIBOLD.getFont());
        if (this.globalLoadingManager) {
            JButton globalResumeButton = new JButton((Icon)ResourceUtil.getIcon((ResourceUtil.ResourceIconPath)ResourceUtil.ActionIcon.EXECUTE));
            globalResumeButton.setToolTipText(Messages.getString("DicomExplorer.resume_all"));
            globalResumeButton.addActionListener(actionEvent -> {
                DownloadManager.resume();
                this.message.setText(Messages.getString("DicomExplorer.loading"));
            });
            this.add(globalResumeButton);
            JButton globalStopButton = new JButton((Icon)ResourceUtil.getIcon((ResourceUtil.ResourceIconPath)ResourceUtil.ActionIcon.SUSPEND));
            globalStopButton.setToolTipText(Messages.getString("DicomExplorer.stop_all"));
            globalStopButton.addActionListener(actionEvent -> {
                DownloadManager.stop();
                this.message.setText(Messages.getString("stopped"));
            });
            this.add(globalStopButton);
        } else {
            JButton cancelButton = new JButton((Icon)ResourceUtil.getIcon((ResourceUtil.ResourceIconPath)ResourceUtil.ActionIcon.SUSPEND));
            cancelButton.setToolTipText(Messages.getString("LoadingTaskPanel.stop_process"));
            cancelButton.addActionListener(e -> {
                this.message.setText(Messages.getString("LoadingTaskPanel.abording"));
                if (this.task != null) {
                    this.task.cancel();
                }
            });
            this.add(cancelButton);
            if (this.task != null) {
                SpinnerProgress globalProgress = this.task.getBar();
                this.add((Component)globalProgress);
                globalProgress.setIndeterminate(true);
            }
        }
        if (this.task != null) {
            this.message.setText(this.task.getMessage());
        }
        this.add(this.message);
        this.revalidate();
        this.repaint();
    }

    public void setMessage(String msg) {
        this.message.setText(msg);
    }

    public ExplorerTask getTask() {
        return this.task;
    }
}

