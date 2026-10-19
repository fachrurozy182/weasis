/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.runtime.SwitchBootstraps
 *  org.dcm4che3.data.Attributes
 *  org.dcm4che3.data.BulkData
 *  org.dcm4che3.data.Fragments
 *  org.dcm4che3.data.VR
 *  org.dcm4che3.data.VR$Holder
 *  org.dcm4che3.img.DicomImageReader
 *  org.dcm4che3.img.Transcoder
 *  org.dcm4che3.img.stream.BytesWithImageDescriptor
 *  org.dcm4che3.img.stream.ImageDescriptor
 *  org.dcm4che3.img.util.DicomUtils
 *  org.dcm4che3.media.DicomDirReader
 *  org.dcm4che3.media.DicomDirWriter
 *  org.dcm4che3.media.RecordFactory
 *  org.dcm4che3.media.RecordType
 *  org.dcm4che3.util.UIDUtils
 *  org.opencv.core.Mat
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.explorer.model.DataExplorerModel
 *  org.weasis.core.api.gui.util.GuiExecutor
 *  org.weasis.core.api.gui.util.GuiUtils
 *  org.weasis.core.api.media.data.MediaSeriesGroup
 *  org.weasis.core.api.media.data.MediaSeriesGroupNode
 *  org.weasis.core.api.media.data.TagW
 *  org.weasis.core.api.media.data.Thumbnail
 *  org.weasis.core.ui.editor.image.ViewerPlugin
 *  org.weasis.dicom.codec.DicomSeries
 *  org.weasis.dicom.codec.TagD
 *  org.weasis.dicom.codec.TagD$Level
 *  org.weasis.dicom.codec.utils.DicomMediaUtils
 *  org.weasis.dicom.codec.utils.PatientComparator
 *  org.weasis.dicom.codec.utils.SeriesInstanceList
 *  org.weasis.dicom.mf.SopInstance
 *  org.weasis.dicom.mf.WadoParameters
 *  org.weasis.opencv.data.PlanarImage
 *  org.weasis.opencv.op.ImageIOHandler
 */
package org.weasis.dicom.explorer.imp;

import java.io.File;
import java.io.IOException;
import java.lang.runtime.SwitchBootstraps;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.imageio.spi.ImageReaderSpi;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.Fragments;
import org.dcm4che3.data.VR;
import org.dcm4che3.img.DicomImageReader;
import org.dcm4che3.img.Transcoder;
import org.dcm4che3.img.stream.BytesWithImageDescriptor;
import org.dcm4che3.img.stream.ImageDescriptor;
import org.dcm4che3.img.util.DicomUtils;
import org.dcm4che3.media.DicomDirReader;
import org.dcm4che3.media.DicomDirWriter;
import org.dcm4che3.media.RecordFactory;
import org.dcm4che3.media.RecordType;
import org.dcm4che3.util.UIDUtils;
import org.opencv.core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.explorer.model.DataExplorerModel;
import org.weasis.core.api.gui.util.GuiExecutor;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.api.media.data.MediaSeriesGroupNode;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.api.media.data.Thumbnail;
import org.weasis.core.ui.editor.image.ViewerPlugin;
import org.weasis.dicom.codec.DicomSeries;
import org.weasis.dicom.codec.TagD;
import org.weasis.dicom.codec.utils.DicomMediaUtils;
import org.weasis.dicom.codec.utils.PatientComparator;
import org.weasis.dicom.codec.utils.SeriesInstanceList;
import org.weasis.dicom.explorer.DicomModel;
import org.weasis.dicom.explorer.wado.DownloadPriority;
import org.weasis.dicom.explorer.wado.LoadSeries;
import org.weasis.dicom.mf.SopInstance;
import org.weasis.dicom.mf.WadoParameters;
import org.weasis.opencv.data.PlanarImage;
import org.weasis.opencv.op.ImageIOHandler;

public class DicomDirLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(DicomDirLoader.class);
    public static final RecordFactory RecordFactory = new RecordFactory();
    private final DicomModel dicomModel;
    private final ArrayList<LoadSeries> seriesList;
    private final WadoParameters wadoParameters;
    private final boolean writeInCache;
    private final File dcmDirFile;

    public DicomDirLoader(File dcmDirFile, DataExplorerModel explorerModel, boolean writeInCache) {
        if (dcmDirFile == null || !dcmDirFile.canRead() || !(explorerModel instanceof DicomModel)) {
            throw new IllegalArgumentException("invalid parameters");
        }
        this.dicomModel = (DicomModel)explorerModel;
        this.writeInCache = writeInCache;
        this.dcmDirFile = dcmDirFile;
        this.wadoParameters = new WadoParameters("", true);
        this.seriesList = new ArrayList();
    }

    public List<LoadSeries> readDicomDir() {
        MediaSeriesGroup patient = null;
        try (DicomDirReader reader = new DicomDirReader(this.dcmDirFile);){
            Attributes dcmPatient = this.findFirstRootDirectoryRecordInUse(reader);
            while (dcmPatient != null) {
                if (RecordType.PATIENT.name().equals(dcmPatient.getString(267312))) {
                    patient = this.parsePatient(dcmPatient, reader);
                }
                dcmPatient = this.findNextSiblingRecord(dcmPatient, reader);
            }
        }
        catch (IOException e) {
            LOGGER.error("Cannot read DICOMDIR !", (Throwable)e);
        }
        if (patient != null) {
            MediaSeriesGroup uniquePatient = patient;
            GuiExecutor.execute(() -> {
                List viewerPlugins;
                List list = viewerPlugins = GuiUtils.getUICore().getViewerPlugins();
                synchronized (list) {
                    for (ViewerPlugin p : viewerPlugins) {
                        if (!uniquePatient.equals((Object)p.getGroupID())) continue;
                        p.setSelectedAndGetFocus();
                        break;
                    }
                }
            });
        }
        for (LoadSeries loadSeries : this.seriesList) {
            if (DicomModel.isHiddenModality(loadSeries.getDicomSeries())) continue;
            loadSeries.startDownloadImageReference(this.wadoParameters);
        }
        return this.seriesList;
    }

    private MediaSeriesGroup parsePatient(Attributes dcmPatient, DicomDirReader reader) {
        MediaSeriesGroup patient = null;
        try {
            PatientComparator patientComparator = new PatientComparator(dcmPatient);
            String patientPseudoUID = patientComparator.buildPatientPseudoUID();
            patient = this.dicomModel.getHierarchyNode(MediaSeriesGroupNode.rootNode, patientPseudoUID);
            if (patient == null) {
                patient = new MediaSeriesGroupNode(TagD.getUID((TagD.Level)TagD.Level.PATIENT), (Object)patientPseudoUID, DicomModel.patient.tagView());
                DicomMediaUtils.writeMetaData((MediaSeriesGroup)patient, (Attributes)dcmPatient);
                this.dicomModel.addHierarchyNode(MediaSeriesGroupNode.rootNode, patient);
            }
            this.parseStudy(patient, dcmPatient, reader);
        }
        catch (Exception e) {
            LOGGER.error("Cannot read DICOMDIR !", (Throwable)e);
        }
        return patient;
    }

    private void parseStudy(MediaSeriesGroup patient, Attributes dcmPatient, DicomDirReader reader) {
        Attributes dcmStudy = this.findFirstChildRecord(dcmPatient, reader);
        while (dcmStudy != null) {
            if (RecordType.STUDY.name().equals(dcmStudy.getString(267312))) {
                String studyUID = (String)TagD.getUID((TagD.Level)TagD.Level.STUDY).getValue((Object)dcmStudy);
                MediaSeriesGroup study = this.dicomModel.getHierarchyNode(patient, studyUID);
                if (study == null) {
                    study = new MediaSeriesGroupNode(TagD.getUID((TagD.Level)TagD.Level.STUDY), (Object)studyUID, DicomModel.study.tagView());
                    DicomMediaUtils.writeMetaData((MediaSeriesGroup)study, (Attributes)dcmStudy);
                    this.dicomModel.addHierarchyNode(patient, study);
                }
                this.parseSeries(patient, study, dcmStudy, reader);
            }
            dcmStudy = this.findNextSiblingRecord(dcmStudy, reader);
        }
    }

    private void parseSeries(MediaSeriesGroup patient, MediaSeriesGroup study, Attributes dcmStudy, DicomDirReader reader) {
        Attributes series = this.findFirstChildRecord(dcmStudy, reader);
        while (series != null) {
            if (RecordType.SERIES.name().equals(series.getString(267312))) {
                String seriesUID = series.getString(0x20000E, "UNKNOWN");
                DicomSeries dicomSeries = (DicomSeries)this.dicomModel.getHierarchyNode(study, seriesUID);
                if (dicomSeries == null) {
                    dicomSeries = new DicomSeries(seriesUID);
                    dicomSeries.setTag(TagW.ExplorerModel, (Object)this.dicomModel);
                    dicomSeries.setTag(TagW.WadoParameters, (Object)this.wadoParameters);
                    DicomMediaUtils.writeMetaData((MediaSeriesGroup)dicomSeries, (Attributes)series);
                    this.dicomModel.addHierarchyNode(study, (MediaSeriesGroup)dicomSeries);
                } else {
                    WadoParameters wado = (WadoParameters)dicomSeries.getTagValue(TagW.WadoParameters);
                    if (wado == null) {
                        dicomSeries.setTag(TagW.WadoParameters, (Object)this.wadoParameters);
                    }
                }
                SeriesInstanceList seriesInstanceList = Optional.ofNullable((SeriesInstanceList)dicomSeries.getTagValue(TagW.WadoInstanceReferenceList)).orElseGet(SeriesInstanceList::new);
                dicomSeries.setTag(TagW.WadoInstanceReferenceList, (Object)seriesInstanceList);
                Attributes iconInstance = series.getNestedDataset(0x880200);
                Attributes instance = this.findFirstChildRecord(series, reader);
                while (instance != null) {
                    File file;
                    Integer frame;
                    SopInstance sop;
                    String sopInstanceUID = instance.getString(267537);
                    if (sopInstanceUID != null && (sop = seriesInstanceList.getSopInstance(sopInstanceUID, frame = DicomUtils.getIntegerFromDicomElement((Attributes)instance, (int)2097171, null))) == null && (file = this.toFileName(instance, reader)) != null) {
                        if (file.exists()) {
                            sop = new SopInstance(sopInstanceUID, frame);
                            sop.setDirectDownloadFile(file.toURI().toString());
                            seriesInstanceList.addSopInstance(sop);
                            if (iconInstance == null) {
                                iconInstance = instance.getNestedDataset(0x880200);
                            }
                        } else {
                            LOGGER.error("Missing DICOMDIR entry: {}", (Object)file.getPath());
                        }
                    }
                    instance = this.findNextSiblingRecord(instance, reader);
                }
                if (!seriesInstanceList.isEmpty()) {
                    dicomSeries.setTag(TagW.DirectDownloadThumbnail, (Object)this.readDicomDirIcon(iconInstance, reader.getTransferSyntaxUID()));
                    dicomSeries.setTag(TagW.ReadFromDicomdir, (Object)true);
                    LoadSeries loadSeries = new LoadSeries(dicomSeries, this.dicomModel, 1, this.writeInCache);
                    loadSeries.setPriority(new DownloadPriority(patient, study, (MediaSeriesGroup)dicomSeries, false));
                    this.seriesList.add(loadSeries);
                }
            }
            series = this.findNextSiblingRecord(series, reader);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private String readDicomDirIcon(Attributes iconInstance, final String transferSyntaxUID) {
        if (iconInstance != null) {
            DicomImageReader reader = new DicomImageReader((ImageReaderSpi)Transcoder.dicomImageReaderSpi);
            try {
                final VR.Holder holder = new VR.Holder();
                final Object pixelData = iconInstance.getValue(2145386512, holder);
                if (pixelData != null) {
                    final ImageDescriptor imdDesc = new ImageDescriptor(iconInstance);
                    File thumbnailPath = File.createTempFile("thumb_", ".jpg", Thumbnail.THUMBNAIL_CACHE_DIR.toFile());
                    BytesWithImageDescriptor bytesWithImageDescriptor = new BytesWithImageDescriptor(){
                        {
                            Objects.requireNonNull(this$0);
                        }

                        public ByteBuffer getBytes(int frame) throws IOException {
                            Object object = pixelData;
                            Objects.requireNonNull(object);
                            Object object2 = object;
                            int n = 0;
                            return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{byte[].class, BulkData.class, Fragments.class}, (Object)object2, (int)n)) {
                                case 0 -> {
                                    byte[] data = (byte[])object2;
                                    yield ByteBuffer.wrap(data);
                                }
                                case 1 -> {
                                    BulkData bulkData = (BulkData)object2;
                                    yield ByteBuffer.wrap(bulkData.toBytes(holder.vr, this.isBigEndian()));
                                }
                                case 2 -> {
                                    Fragments fragments = (Fragments)object2;
                                    yield ByteBuffer.wrap(fragments.toBytes(holder.vr, this.isBigEndian()));
                                }
                                default -> null;
                            };
                        }

                        public String getTransferSyntax() {
                            return transferSyntaxUID;
                        }

                        public boolean isBigEndian() {
                            if (pixelData instanceof BulkData) {
                                BulkData bulkData = (BulkData)pixelData;
                                return bulkData.bigEndian();
                            }
                            if (pixelData instanceof Fragments) {
                                Fragments fragments = (Fragments)pixelData;
                                return fragments.bigEndian();
                            }
                            return false;
                        }

                        public VR getPixelDataVR() {
                            return holder.vr;
                        }

                        public ImageDescriptor getImageDescriptor() {
                            return imdDesc;
                        }
                    };
                    reader.setInput((Object)bytesWithImageDescriptor);
                    ImageDescriptor desc = reader.getImageDescriptor();
                    PlanarImage img = reader.getPlanarImage(0, null);
                    if (img.width() != desc.getColumns() || img.height() != desc.getRows()) {
                        LOGGER.error("The native image size ({}x{}) does not match with the DICOM attributes({}x{})", new Object[]{img.width(), img.height(), desc.getColumns(), desc.getRows()});
                    }
                    if (ImageIOHandler.writeImage((Mat)img.toMat(), (Path)thumbnailPath.toPath())) {
                        String string = thumbnailPath.getPath();
                        return string;
                    }
                }
            }
            catch (Exception e) {
                LOGGER.error("Cannot read Icon in DICOMDIR!", (Throwable)e);
            }
            finally {
                reader.dispose();
            }
        }
        return null;
    }

    private Attributes findFirstChildRecord(Attributes dcmObject, DicomDirReader reader) {
        try {
            return reader.findLowerDirectoryRecordInUse(dcmObject, true);
        }
        catch (IOException e) {
            LOGGER.error("Cannot read first DICOMDIR entry!", (Throwable)e);
            return null;
        }
    }

    private Attributes findNextSiblingRecord(Attributes dcmObject, DicomDirReader reader) {
        try {
            return reader.findNextDirectoryRecordInUse(dcmObject, true);
        }
        catch (IOException e) {
            LOGGER.error("Cannot read next DICOMDIR entry!", (Throwable)e);
            return null;
        }
    }

    private Attributes findFirstRootDirectoryRecordInUse(DicomDirReader reader) {
        try {
            return reader.findFirstRootDirectoryRecordInUse(true);
        }
        catch (IOException e) {
            LOGGER.error("Cannot find Patient in DICOMDIR !", (Throwable)e);
            return null;
        }
    }

    private File toFileName(Attributes dcmObject, DicomDirReader reader) {
        String[] fileID = dcmObject.getStrings(267520);
        if (fileID == null || fileID.length == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder(fileID[0]);
        for (int i = 1; i < fileID.length; ++i) {
            sb.append(File.separatorChar).append(fileID[i]);
        }
        File file = new File(reader.getFile().getParent(), sb.toString());
        if (!file.exists()) {
            File fileLowerCase = new File(reader.getFile().getParent(), sb.toString().toLowerCase());
            if (fileLowerCase.exists()) {
                return fileLowerCase;
            }
            String dcmFilename = file.getName();
            File[] dcmFileList = file.getParentFile().listFiles((p, name) -> name.startsWith(dcmFilename + ".") || name.startsWith(dcmFilename.toLowerCase() + "."));
            if (dcmFileList != null && dcmFileList.length == 1) {
                return dcmFileList[0];
            }
        }
        return file;
    }

    public static DicomDirWriter open(File file) throws IOException {
        if (file.createNewFile()) {
            DicomDirWriter.createEmptyDirectory((File)file, (String)UIDUtils.createUID(), null, null, null);
        }
        return DicomDirWriter.open((File)file);
    }
}

