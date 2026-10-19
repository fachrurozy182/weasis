/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package org.weasis.core.ui.editor;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.swing.JMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.ui.editor.SeriesViewer;

public abstract class MimeSystemAppViewer
implements SeriesViewer<MediaElement> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MimeSystemAppViewer.class);
    private static final String ERROR_MSG = "Cannot open {} with the default system application";

    @Override
    public void close() {
    }

    @Override
    public List<MediaSeries<MediaElement>> getOpenSeries() {
        return Collections.emptyList();
    }

    public static void startAssociatedProgramFromLinux(Path path) {
        if (path != null && Files.isReadable(path)) {
            try {
                Process process = new ProcessBuilder("xdg-open", path.toString()).redirectOutput(ProcessBuilder.Redirect.DISCARD).redirectError(ProcessBuilder.Redirect.DISCARD).start();
                process.getOutputStream().close();
                if (process.waitFor(5L, TimeUnit.SECONDS) && process.exitValue() != 0) {
                    LOGGER.error("'xdg-open' failed (exit {}) for {}", (Object)process.exitValue(), (Object)path);
                }
            }
            catch (IOException e) {
                LOGGER.error(ERROR_MSG, (Object)path, (Object)e);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void startAssociatedProgramFromDesktop(Desktop desktop, Path path) {
        if (path != null && Files.isReadable(path)) {
            try {
                desktop.open(path.toFile());
            }
            catch (IOException e) {
                LOGGER.error(ERROR_MSG, (Object)path, (Object)e);
            }
        }
    }

    @Override
    public void removeSeries(MediaSeries<MediaElement> sequence) {
    }

    @Override
    public JMenu fillSelectedPluginMenu(JMenu menu) {
        return null;
    }

    @Override
    public void setSelected(boolean selected) {
    }

    @Override
    public MediaSeriesGroup getGroupID() {
        return null;
    }
}

