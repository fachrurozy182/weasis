/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.gui.task;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.Objects;
import org.weasis.core.api.gui.util.GuiExecutor;
import org.weasis.core.api.media.data.Series;
import org.weasis.core.api.media.data.SeriesImporter;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.api.media.data.Thumbnail;

public class SeriesProgressMonitor
extends FilterInputStream {
    private static final String INTERRUPTION_LABEL = "progress";
    protected final Series<?> series;
    protected int nread = 0;
    protected int size;

    public SeriesProgressMonitor(Series<?> series, InputStream in) {
        super(in);
        this.series = Objects.requireNonNull(series);
        try {
            this.size = in.available();
        }
        catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    protected boolean isLoadingSeriesCanceled() {
        SeriesImporter loader = this.series.getSeriesLoader();
        return loader == null || loader.isStopped();
    }

    protected void updateSeriesProgression(long addSize) {
        this.series.setFileSize(this.series.getFileSize() + addSize);
        GuiExecutor.execute(() -> {
            Thumbnail thumb = (Thumbnail)this.series.getTagValue(TagW.Thumbnail);
            if (thumb != null) {
                thumb.repaint();
            }
        });
    }

    @Override
    public int read() throws IOException {
        if (this.isLoadingSeriesCanceled()) {
            InterruptedIOException exc = new InterruptedIOException(INTERRUPTION_LABEL);
            exc.bytesTransferred = this.nread;
            this.series.setFileSize(this.series.getFileSize() - (long)this.nread);
            this.nread = 0;
            throw exc;
        }
        int c = this.in.read();
        if (c >= 0) {
            ++this.nread;
            this.updateSeriesProgression(1L);
        }
        return c;
    }

    @Override
    public int read(byte[] b) throws IOException {
        if (this.isLoadingSeriesCanceled()) {
            InterruptedIOException exc = new InterruptedIOException(INTERRUPTION_LABEL);
            exc.bytesTransferred = this.nread;
            this.series.setFileSize(this.series.getFileSize() - (long)this.nread);
            this.nread = 0;
            throw exc;
        }
        int nr = this.in.read(b);
        if (nr > 0) {
            this.nread += nr;
            this.updateSeriesProgression(nr);
        }
        return nr;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (this.isLoadingSeriesCanceled()) {
            InterruptedIOException exc = new InterruptedIOException(INTERRUPTION_LABEL);
            exc.bytesTransferred = this.nread;
            this.series.setFileSize(this.series.getFileSize() - (long)this.nread);
            this.nread = 0;
            throw exc;
        }
        int nr = this.in.read(b, off, len);
        if (nr > 0) {
            this.nread += nr;
            this.updateSeriesProgression(nr);
        }
        return nr;
    }

    @Override
    public long skip(long n) throws IOException {
        long nr = this.in.skip(n);
        if (nr > 0L) {
            this.nread += (int)nr;
            this.updateSeriesProgression(nr);
        }
        return nr;
    }

    @Override
    public synchronized void reset() throws IOException {
        this.in.reset();
        this.nread = this.size - this.in.available();
        this.updateSeriesProgression(this.nread);
    }
}

