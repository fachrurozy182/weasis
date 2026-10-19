/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.dicom.codec.DicomSeries
 *  org.weasis.dicom.explorer.wado.LoadSeries
 */
package org.weasis.dicom.qr;

import org.weasis.dicom.codec.DicomSeries;
import org.weasis.dicom.explorer.wado.LoadSeries;
import org.weasis.dicom.qr.RetrieveContext;

public class LoadWadoUriSeries
extends LoadSeries {
    private final RetrieveContext context;

    public LoadWadoUriSeries(DicomSeries dicomSeries, RetrieveContext context, int concurrentDownloads, boolean startDownloading) {
        super(dicomSeries, context.getDicomModel(), null, concurrentDownloads, true, startDownloading);
        this.context = context;
        this.setPOpeningStrategy(context.getOpeningStrategy());
    }

    private LoadWadoUriSeries(LoadWadoUriSeries previous) {
        super(previous.getDicomSeries(), previous.context.getDicomModel(), null, previous.getProgressBar(), previous.getConcurrentDownloads(), true, previous.isStartDownloading());
        this.context = previous.context;
        this.setPOpeningStrategy(previous.getOpeningStrategy());
    }

    protected Boolean doInBackground() {
        this.context.fillInstances(this.getDicomSeries());
        return super.doInBackground();
    }

    protected LoadSeries createResumeTask() {
        return new LoadWadoUriSeries(this);
    }
}

