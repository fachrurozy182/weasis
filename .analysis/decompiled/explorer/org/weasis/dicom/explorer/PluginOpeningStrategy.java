/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.api.explorer.model.DataExplorerModel
 *  org.weasis.core.api.gui.util.GuiUtils
 *  org.weasis.core.api.media.data.MediaSeriesGroup
 *  org.weasis.core.api.media.data.Series
 *  org.weasis.core.ui.editor.SeriesViewerFactory
 *  org.weasis.core.ui.editor.TabFocusPolicy
 *  org.weasis.core.ui.editor.TabFocusPolicy$Foreground
 *  org.weasis.core.ui.editor.ViewerOpenOptions
 *  org.weasis.core.ui.editor.ViewerPluginBuilder
 */
package org.weasis.dicom.explorer;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.weasis.core.api.explorer.model.DataExplorerModel;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.api.media.data.Series;
import org.weasis.core.ui.editor.SeriesViewerFactory;
import org.weasis.core.ui.editor.TabFocusPolicy;
import org.weasis.core.ui.editor.ViewerOpenOptions;
import org.weasis.core.ui.editor.ViewerPluginBuilder;
import org.weasis.dicom.explorer.DicomModel;
import org.weasis.dicom.explorer.HangingProtocols;
import org.weasis.dicom.explorer.MimeSystemAppFactory;

public class PluginOpeningStrategy {
    private final HangingProtocols.OpeningViewer openingMode;
    private final boolean closePreviousViewers;
    private boolean fullImportSession;
    private final Instant loadStartedAt;
    private final Set<MediaSeriesGroup> openPatients = Collections.synchronizedSet(new HashSet());

    public PluginOpeningStrategy(HangingProtocols.OpeningViewer openingMode) {
        this(openingMode, HangingProtocols.isClosePreviousFromPreferences());
    }

    public PluginOpeningStrategy(HangingProtocols.OpeningViewer openingMode, boolean closePreviousViewers) {
        this.openingMode = Objects.requireNonNullElse(openingMode, HangingProtocols.OpeningViewer.ALL_PATIENTS);
        this.closePreviousViewers = closePreviousViewers;
        this.fullImportSession = true;
        this.loadStartedAt = Instant.now();
    }

    public boolean containsPatient(MediaSeriesGroup patient) {
        return this.openPatients.contains(Objects.requireNonNull(patient));
    }

    public boolean isFullImportSession() {
        return this.fullImportSession;
    }

    public void setFullImportSession(boolean fullImportSession) {
        this.fullImportSession = fullImportSession;
    }

    public void reset() {
        if (this.fullImportSession) {
            this.openPatients.clear();
        }
    }

    public void addPatient(MediaSeriesGroup patient) {
        this.openPatients.add(Objects.requireNonNull(patient));
    }

    public void removePatient(MediaSeriesGroup patient) {
        this.openPatients.remove(patient);
    }

    public void prepareImport() {
        if (this.closePreviousViewers && (this.fullImportSession || this.openPatients.isEmpty())) {
            GuiUtils.getUICore().closeSeriesViewer(new ArrayList(GuiUtils.getUICore().getViewerPlugins()));
        }
    }

    public void openViewerPlugin(MediaSeriesGroup patient, DicomModel dicomModel, Series<?> dicomSeries) {
        Objects.requireNonNull(dicomModel);
        Objects.requireNonNull(dicomSeries);
        if (DicomModel.isHiddenModality(dicomSeries)) {
            return;
        }
        if (HangingProtocols.OpeningViewer.NONE.equals((Object)this.openingMode)) {
            return;
        }
        boolean isPatientOpen = this.containsPatient(patient);
        if (!isPatientOpen) {
            String mime = dicomSeries.getMimeType();
            SeriesViewerFactory plugin = GuiUtils.getUICore().getViewerFactory(mime);
            if (plugin != null && !"sr/dicom".equals(mime) && !(plugin instanceof MimeSystemAppFactory)) {
                boolean hasExistingViewers = !GuiUtils.getUICore().getViewerPlugins().isEmpty();
                TabFocusPolicy.Foreground focusPolicy = hasExistingViewers ? TabFocusPolicy.autoByDuration((Duration)TabFocusPolicy.DEFAULT_AUTO_THRESHOLD, (Instant)this.loadStartedAt) : TabFocusPolicy.foreground();
                ViewerOpenOptions opts = ViewerOpenOptions.defaults().withTabFocusPolicy((TabFocusPolicy)focusPolicy);
                this.addPatient(patient);
                new ViewerPluginBuilder(plugin, List.of(dicomSeries), (DataExplorerModel)dicomModel, opts).open();
            }
        }
    }
}

