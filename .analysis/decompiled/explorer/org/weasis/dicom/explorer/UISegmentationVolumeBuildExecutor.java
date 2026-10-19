/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.explorer.DataExplorerView
 *  org.weasis.core.api.explorer.ObservableEvent
 *  org.weasis.core.api.explorer.ObservableEvent$BasicAction
 *  org.weasis.core.api.explorer.model.DataExplorerModel
 *  org.weasis.core.api.gui.util.GuiUtils
 *  org.weasis.core.api.media.data.TagW
 *  org.weasis.dicom.codec.DicomSeries
 *  org.weasis.dicom.codec.seg.SegSpecialElement
 *  org.weasis.dicom.codec.seg.SegmentationVolume
 *  org.weasis.dicom.codec.seg.SegmentationVolumeBuildExecutor
 */
package org.weasis.dicom.explorer;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.explorer.DataExplorerView;
import org.weasis.core.api.explorer.ObservableEvent;
import org.weasis.core.api.explorer.model.DataExplorerModel;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.media.data.TagW;
import org.weasis.dicom.codec.DicomSeries;
import org.weasis.dicom.codec.seg.SegSpecialElement;
import org.weasis.dicom.codec.seg.SegmentationVolume;
import org.weasis.dicom.codec.seg.SegmentationVolumeBuildExecutor;
import org.weasis.dicom.explorer.exp.ExplorerTask;
import org.weasis.dicom.explorer.main.DicomExplorer;

public final class UISegmentationVolumeBuildExecutor
implements SegmentationVolumeBuildExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(UISegmentationVolumeBuildExecutor.class);

    public CompletableFuture<SegmentationVolume> schedule(SegSpecialElement seg, final Callable<SegmentationVolume> work) {
        final DataExplorerModel model = UISegmentationVolumeBuildExecutor.resolveModel(seg);
        String message = "Building SEG";
        final CompletableFuture<SegmentationVolume> result = new CompletableFuture<SegmentationVolume>();
        ExplorerTask<SegmentationVolume, Void> task = new ExplorerTask<SegmentationVolume, Void>(this, message, false){
            {
                Objects.requireNonNull(this$0);
                super(message, interruptible);
            }

            @Override
            protected SegmentationVolume doInBackground() throws Exception {
                return (SegmentationVolume)work.call();
            }

            @Override
            protected void done() {
                if (model != null) {
                    model.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.LOADING_STOP, (Object)model, null, (Object)this));
                }
                if (this.isCancelled()) {
                    result.completeExceptionally(new CancellationException());
                    return;
                }
                try {
                    result.complete((SegmentationVolume)this.get());
                }
                catch (CancellationException ce) {
                    result.completeExceptionally(ce);
                }
                catch (ExecutionException ee) {
                    result.completeExceptionally(ee.getCause() == null ? ee : ee.getCause());
                }
                catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    result.completeExceptionally(ie);
                }
            }
        };
        result.whenComplete((segmentationVolume, err) -> {
            if (err instanceof CancellationException && !task.isDone()) {
                task.cancel();
            }
        });
        if (model != null) {
            model.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.LOADING_START, (Object)model, null, (Object)task));
        }
        try {
            task.execute();
        }
        catch (RuntimeException e) {
            LOGGER.error("Failed to schedule segmentation volume build", (Throwable)e);
            if (model != null) {
                model.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.LOADING_STOP, (Object)model, null, (Object)task));
            }
            result.completeExceptionally(e);
        }
        return result;
    }

    private static DataExplorerModel resolveModel(SegSpecialElement seg) {
        DataExplorerModel dataExplorerModel;
        Object tag;
        DicomSeries series = seg == null || seg.getMediaReader() == null ? null : seg.getMediaReader().getMediaSeries();
        Object object = tag = series == null ? null : series.getTagValue(TagW.ExplorerModel);
        if (tag instanceof DataExplorerModel) {
            DataExplorerModel m = (DataExplorerModel)tag;
            return m;
        }
        DataExplorerView view = GuiUtils.getUICore().getExplorerPlugin(DicomExplorer.NAME);
        if (view != null && (dataExplorerModel = view.getDataExplorerModel()) instanceof DataExplorerModel) {
            DataExplorerModel m = dataExplorerModel;
            return m;
        }
        return null;
    }
}

