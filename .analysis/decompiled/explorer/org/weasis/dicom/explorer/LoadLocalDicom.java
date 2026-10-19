/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.api.explorer.ObservableEvent
 *  org.weasis.core.api.explorer.ObservableEvent$BasicAction
 *  org.weasis.core.api.explorer.model.DataExplorerModel
 *  org.weasis.core.api.gui.util.AppProperties
 *  org.weasis.core.api.gui.util.Filter
 *  org.weasis.core.api.gui.util.GuiUtils
 *  org.weasis.core.api.media.MimeInspector
 *  org.weasis.core.api.media.data.MediaSeries
 *  org.weasis.core.api.media.data.MediaSeries$MEDIA_POSITION
 *  org.weasis.core.api.media.data.SeriesEvent
 *  org.weasis.core.api.media.data.SeriesEvent$Action
 *  org.weasis.core.api.media.data.SeriesThumbnail
 *  org.weasis.core.api.media.data.TagReadable
 *  org.weasis.core.api.media.data.TagW
 *  org.weasis.core.ui.model.GraphicModel
 *  org.weasis.core.ui.serialize.XmlSerializer
 *  org.weasis.core.util.FileUtil
 *  org.weasis.dicom.codec.DcmMediaReader
 *  org.weasis.dicom.codec.DicomCodec
 *  org.weasis.dicom.codec.DicomImageElement
 *  org.weasis.dicom.codec.DicomMediaIO
 *  org.weasis.dicom.codec.DicomMediaIO$Reading
 *  org.weasis.dicom.codec.DicomSeries
 *  org.weasis.dicom.codec.SortSeriesStack
 *  org.weasis.dicom.codec.utils.DicomMediaUtils
 */
package org.weasis.dicom.explorer;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.swing.JOptionPane;
import org.weasis.core.api.explorer.ObservableEvent;
import org.weasis.core.api.explorer.model.DataExplorerModel;
import org.weasis.core.api.gui.util.AppProperties;
import org.weasis.core.api.gui.util.Filter;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.media.MimeInspector;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.media.data.SeriesEvent;
import org.weasis.core.api.media.data.SeriesThumbnail;
import org.weasis.core.api.media.data.TagReadable;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.ui.model.GraphicModel;
import org.weasis.core.ui.serialize.XmlSerializer;
import org.weasis.core.util.FileUtil;
import org.weasis.dicom.codec.DcmMediaReader;
import org.weasis.dicom.codec.DicomCodec;
import org.weasis.dicom.codec.DicomImageElement;
import org.weasis.dicom.codec.DicomMediaIO;
import org.weasis.dicom.codec.DicomSeries;
import org.weasis.dicom.codec.SortSeriesStack;
import org.weasis.dicom.codec.utils.DicomMediaUtils;
import org.weasis.dicom.explorer.DicomModel;
import org.weasis.dicom.explorer.HangingProtocols;
import org.weasis.dicom.explorer.LoadDicom;
import org.weasis.dicom.explorer.Messages;
import org.weasis.dicom.explorer.PluginOpeningStrategy;
import org.weasis.dicom.explorer.imp.DicomZipCodec;
import org.weasis.dicom.explorer.imp.DicomZipMediaIO;

public class LoadLocalDicom
extends LoadDicom {
    private final File[] files;
    private final boolean recursive;

    public LoadLocalDicom(File[] files, boolean recursive, DataExplorerModel explorerModel, HangingProtocols.OpeningViewer openingMode) {
        this(files, recursive, explorerModel, new PluginOpeningStrategy(openingMode));
    }

    public LoadLocalDicom(File[] files, boolean recursive, DataExplorerModel explorerModel, PluginOpeningStrategy openingStrategy) {
        super(explorerModel, false, openingStrategy);
        this.files = Objects.requireNonNull(files);
        this.recursive = recursive;
    }

    @Override
    protected Boolean doInBackground() throws Exception {
        this.startLoadingEvent();
        if (this.files.length > 0) {
            this.openingStrategy.prepareImport();
            this.addSelectionAndNotify(this.files, true);
        }
        return true;
    }

    protected void addSelectionAndNotify(File[] file, boolean firstLevel) {
        if (file == null || file.length < 1) {
            return;
        }
        LinkedHashSet<DicomSeries> uniqueSeriesSet = new LinkedHashSet<DicomSeries>();
        ArrayList<File> folders = new ArrayList<File>();
        for (File value : file) {
            if (this.isCancelled()) {
                return;
            }
            if (value == null || !value.canRead()) continue;
            if (value.isDirectory()) {
                if (!firstLevel && !this.recursive) continue;
                folders.add(value);
                continue;
            }
            if (FileUtil.isFileExtensionMatching((Path)value.toPath(), (String[])DicomCodec.FILE_EXTENSIONS) || MimeInspector.isMatchingMimeTypeFromMagicNumber((File)value, (String)"application/dicom")) {
                DicomMediaIO loader = new DicomMediaIO(value);
                DicomMediaIO.Reading reading = loader.getReadingStatus();
                if (reading == DicomMediaIO.Reading.READABLE) {
                    if (value.getPath().startsWith(AppProperties.APP_TEMP_DIR.toString())) {
                        loader.getFileCache().setOriginalTempFile(value.toPath());
                    }
                    uniqueSeriesSet.add(this.buildDicomStructure(loader));
                    File gpxFile = new File(value.getPath() + ".xml");
                    GraphicModel graphicModel = XmlSerializer.readPresentationModel((File)gpxFile);
                    if (graphicModel == null) continue;
                    loader.setTag(TagW.PresentationModel, (Object)graphicModel);
                    continue;
                }
                if (reading == DicomMediaIO.Reading.ERROR) {
                    this.errors.incrementAndGet();
                    continue;
                }
                if (reading != DicomMediaIO.Reading.UNSUPPORTED) continue;
                this.unsupported.incrementAndGet();
                continue;
            }
            if (!FileUtil.isFileExtensionMatching((Path)value.toPath(), (String[])DicomZipCodec.FILE_EXTENSIONS) && !MimeInspector.isMatchingMimeTypeFromMagicNumber((File)value, (String)"application/x-zip")) continue;
            new DicomZipMediaIO(value.toURI(), null).delegate(this.dicomModel);
        }
        if (this.openingStrategy.isFullImportSession()) {
            LoadLocalDicom.updateSeriesThumbnail(uniqueSeriesSet, this.dicomModel);
        } else {
            for (DicomSeries series : uniqueSeriesSet) {
                this.dicomModel.buildThumbnail(series);
            }
        }
        for (File folder : folders) {
            this.addSelectionAndNotify(folder.listFiles(), false);
        }
    }

    public static void updateSeriesThumbnail(Set<DicomSeries> seriesList, DicomModel dicomModel) {
        if (dicomModel == null || seriesList == null) {
            return;
        }
        for (DicomSeries series : seriesList) {
            if (series == null || DicomModel.isHiddenModality(series)) continue;
            boolean split = LoadLocalDicom.seriesPostProcessing(series, dicomModel);
            if (!split) {
                dicomModel.buildThumbnail(series);
            }
            if (!series.isSuitableFor3d()) continue;
            dicomModel.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.UPDATE, (Object)series, null, (Object)new SeriesEvent(SeriesEvent.Action.UPDATE, (Object)series, null)));
        }
    }

    public static boolean seriesPostProcessing(DicomSeries dicomSeries, DicomModel dicomModel) {
        return LoadLocalDicom.seriesPostProcessing(dicomSeries, dicomModel, false);
    }

    public static boolean seriesPostProcessing(DicomSeries dicomSeries, DicomModel dicomModel, boolean force) {
        Integer step = (Integer)dicomSeries.getTagValue(TagW.stepNDimensions);
        if (step == null || step < 1 || force) {
            int imageCount = dicomSeries.size(null);
            if (imageCount == 0) {
                return false;
            }
            List imageList = dicomSeries.copyOfMedias(null, (Comparator)SortSeriesStack.slicePosition);
            int samplingRate = LoadLocalDicom.calculateSamplingRateFor4d(imageList);
            dicomSeries.setTag(TagW.stepNDimensions, (Object)samplingRate);
            if (samplingRate < 2 || samplingRate > 7 && !force) {
                return false;
            }
            for (int i = 0; i < samplingRate; ++i) {
                SeriesThumbnail thumbnail;
                DicomSeries newSeries;
                DicomImageElement image = (DicomImageElement)imageList.get(i);
                DcmMediaReader dcmMediaReader = image.getMediaReader();
                if (!(dcmMediaReader instanceof DicomMediaIO)) continue;
                DicomMediaIO dicomReader = (DicomMediaIO)dcmMediaReader;
                if (i == 0) {
                    dicomSeries.removeAllMedias();
                    newSeries = dicomSeries;
                } else {
                    newSeries = dicomModel.splitSeries(dicomReader, dicomSeries);
                }
                newSeries.setTag(TagW.stepNDimensions, (Object)1);
                Filter<DicomImageElement> samplingFilter = LoadLocalDicom.getDicomImageElementFilter(i, samplingRate);
                newSeries.addAll((Collection)Filter.makeList((Iterable)samplingFilter.filter((Iterable)imageList)));
                if (i == 0 && (thumbnail = (SeriesThumbnail)dicomSeries.getTagValue(TagW.Thumbnail)) != null) {
                    thumbnail.reBuildThumbnail(null, MediaSeries.MEDIA_POSITION.MIDDLE);
                }
                dicomModel.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.UPDATE, (Object)dicomModel, null, (Object)newSeries));
            }
            return true;
        }
        return false;
    }

    public static boolean isMultiPhaseSeries(MediaSeries<?> series) {
        Integer step;
        Object object = series.getTagValue(TagW.stepNDimensions);
        return object instanceof Integer && (step = (Integer)object) > 1;
    }

    public static MediaSeries<DicomImageElement> confirmSplittingMultiPhaseSeries(MediaSeries<DicomImageElement> series) {
        if (LoadLocalDicom.isMultiPhaseSeries(series) && series instanceof DicomSeries) {
            DicomModel dicomModel;
            DicomSeries dicomSeries = (DicomSeries)series;
            int result = JOptionPane.showConfirmDialog(GuiUtils.getUICore().getApplicationWindow(), Messages.getString("msg.multi.phase"), Messages.getString("multi.phase.title"), 2, 3);
            if (result == 0 && (dicomModel = (DicomModel)dicomSeries.getTagValue(TagW.ExplorerModel)) != null) {
                LoadLocalDicom.seriesPostProcessing(dicomSeries, dicomModel, true);
                return dicomSeries;
            }
            return null;
        }
        return series;
    }

    private static Filter<DicomImageElement> getDicomImageElementFilter(final int index, final int size) {
        return new Filter<DicomImageElement>(){
            private final int samplingRate;
            private int currentIndex;
            {
                this.samplingRate = size;
                this.currentIndex = index;
            }

            public boolean passes(DicomImageElement item) {
                boolean pass = this.currentIndex % this.samplingRate == 0;
                ++this.currentIndex;
                return pass;
            }
        };
    }

    static int calculateSamplingRateFor4d(List<DicomImageElement> imageList) {
        try {
            if (imageList.size() >= 2) {
                double posSum;
                double firstPosSum = DicomMediaUtils.getSlicePositionValue((TagReadable)((TagReadable)imageList.getFirst()));
                int samePositionCount = 1;
                for (int i = 1; i < imageList.size() && Math.abs((posSum = DicomMediaUtils.getSlicePositionValue((TagReadable)((TagReadable)imageList.get(i)))) - firstPosSum) < 0.05; ++i) {
                    ++samePositionCount;
                }
                if (samePositionCount > 1 && samePositionCount < imageList.size() / 2) {
                    return samePositionCount;
                }
            }
        }
        catch (Exception e) {
            return 1;
        }
        return 1;
    }
}

