/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.lingala.zip4j.ZipFile
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.explorer.model.DataExplorerModel
 *  org.weasis.core.api.gui.util.AppProperties
 *  org.weasis.core.api.gui.util.GuiUtils
 *  org.weasis.core.api.media.data.Codec
 *  org.weasis.core.api.media.data.FileCache
 *  org.weasis.core.api.media.data.MediaElement
 *  org.weasis.core.api.media.data.MediaReader
 *  org.weasis.core.api.media.data.MediaSeries
 *  org.weasis.core.api.media.data.TagW
 *  org.weasis.core.api.net.ClosableURLConnection
 *  org.weasis.core.api.net.NetworkUtil
 *  org.weasis.core.api.net.URIUtils
 *  org.weasis.core.api.net.URLParameters
 *  org.weasis.core.util.FileUtil
 *  org.weasis.core.util.StringUtil
 *  org.weasis.opencv.data.PlanarImage
 */
package org.weasis.dicom.explorer.imp;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.Timer;
import net.lingala.zip4j.ZipFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.explorer.model.DataExplorerModel;
import org.weasis.core.api.gui.util.AppProperties;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.media.data.Codec;
import org.weasis.core.api.media.data.FileCache;
import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.api.media.data.MediaReader;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.api.net.ClosableURLConnection;
import org.weasis.core.api.net.NetworkUtil;
import org.weasis.core.api.net.URIUtils;
import org.weasis.core.api.net.URLParameters;
import org.weasis.core.util.FileUtil;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.explorer.DicomModel;
import org.weasis.dicom.explorer.HangingProtocols;
import org.weasis.dicom.explorer.LoadDicomDir;
import org.weasis.dicom.explorer.LoadLocalDicom;
import org.weasis.dicom.explorer.Messages;
import org.weasis.dicom.explorer.imp.DicomDirLoader;
import org.weasis.dicom.explorer.imp.DicomZipCodec;
import org.weasis.dicom.explorer.wado.LoadSeries;
import org.weasis.opencv.data.PlanarImage;

public class DicomZipMediaIO
implements MediaReader<MediaElement> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DicomZipMediaIO.class);
    public static final String MIME_TYPE = "application/x-zip";
    private final URI uri;
    private final FileCache fileCache;
    private final DicomZipCodec codec;

    public DicomZipMediaIO(URI uri, DicomZipCodec codec) {
        this.uri = Objects.requireNonNull(uri);
        this.fileCache = new FileCache((MediaReader)this);
        this.codec = codec;
    }

    public URI getUri() {
        return this.uri;
    }

    public FileCache getFileCache() {
        return this.fileCache;
    }

    public boolean isOnlyDelegate() {
        return true;
    }

    public boolean delegate(DataExplorerModel explorerModel) {
        File zipFile = URIUtils.toFile((URI)this.uri);
        if (zipFile.canRead() && explorerModel instanceof DicomModel) {
            DicomModel dicomModel = (DicomModel)explorerModel;
            DicomZipMediaIO.loadDicomZip(zipFile, dicomModel, HangingProtocols.OpeningViewer.ALL_PATIENTS, GuiUtils.getUICore().getApplicationWindow());
            return true;
        }
        return false;
    }

    public MediaElement getPreview() {
        return null;
    }

    public PlanarImage getImageFragment(MediaElement media) throws Exception {
        return null;
    }

    public MediaElement[] getMediaElement() {
        return new MediaElement[0];
    }

    public MediaSeries<MediaElement> getMediaSeries() {
        return null;
    }

    public int getMediaElementNumber() {
        return 0;
    }

    public String getMediaFragmentMimeType() {
        return MIME_TYPE;
    }

    public Map<TagW, Object> getMediaFragmentTags(Object key) {
        return Collections.emptyMap();
    }

    public void close() {
    }

    public Codec<MediaElement> getCodec() {
        return this.codec;
    }

    public String[] getReaderDescription() {
        return new String[]{"DICOM ZIP reader"};
    }

    public Object getTagValue(TagW tag) {
        return null;
    }

    public void setTag(TagW tag, Object value) {
    }

    public boolean containTagKey(TagW tag) {
        return false;
    }

    public void setTagNoNull(TagW tag, Object value) {
    }

    public Iterator<Map.Entry<TagW, Object>> getTagEntrySetIterator() {
        return Collections.emptyIterator();
    }

    public void replaceURI(URI uri) {
    }

    public boolean buildFile(File output) {
        return false;
    }

    public static void loadDicomZip(File file, DicomModel dicomModel, HangingProtocols.OpeningViewer openingViewer, Component parent) {
        if (file != null && file.canRead()) {
            Path dir = FileUtil.createTempDir((Path)AppProperties.buildAccessibleTempDirectory((String[])new String[]{"tmp", "zip"}));
            try (ZipFile zipFile = new ZipFile(file);){
                if (zipFile.isEncrypted()) {
                    JPanel panel = new JPanel();
                    JPasswordField pass = new JPasswordField(16);
                    panel.add(new JLabel(Messages.getString("enter.password")));
                    panel.add(pass);
                    ActionListener al = ae -> pass.requestFocusInWindow();
                    Timer timer = new Timer(250, al);
                    timer.setRepeats(false);
                    timer.start();
                    int response = JOptionPane.showOptionDialog(parent, panel, Messages.getString("DicomZipImport.title"), 2, -1, null, null, pass);
                    if (response == 0) {
                        zipFile.setPassword(pass.getPassword());
                    }
                }
                zipFile.extractAll(dir.toString());
            }
            catch (IOException e) {
                LOGGER.error("unzipping", (Throwable)e);
            }
            File dicomdir = new File(dir.toFile(), "DICOMDIR");
            if (dicomdir.canRead()) {
                DicomDirLoader dirImport = new DicomDirLoader(dicomdir, dicomModel, false);
                List<LoadSeries> loadSeries = dirImport.readDicomDir();
                if (loadSeries != null && !loadSeries.isEmpty()) {
                    DicomModel.LOADING_EXECUTOR.execute(new LoadDicomDir(loadSeries, dicomModel, openingViewer));
                } else {
                    LOGGER.error("Cannot import DICOM from {}", (Object)file);
                }
            } else {
                LoadLocalDicom dicom = new LoadLocalDicom(new File[]{dir.toFile()}, true, (DataExplorerModel)dicomModel, openingViewer);
                DicomModel.LOADING_EXECUTOR.execute(dicom);
            }
        }
    }

    public static void loadDicomZip(String uri, DicomModel dicomModel) {
        if (StringUtil.hasText((CharSequence)uri)) {
            File zipFile = null;
            try {
                URI u = new URI(uri);
                if ("file".equals(u.getScheme())) {
                    zipFile = URIUtils.toFile((URI)u);
                } else {
                    zipFile = Files.createTempFile(AppProperties.APP_TEMP_DIR, "dicom_", ".zip", new FileAttribute[0]).toFile();
                    ClosableURLConnection urlConnection = NetworkUtil.getUrlConnection((URL)u.toURL(), (URLParameters)URLParameters.DEFAULT);
                    FileUtil.writeStreamWithIOException((InputStream)urlConnection.getInputStream(), (Path)zipFile.toPath());
                }
            }
            catch (Exception e) {
                LOGGER.error("Loading DICOM Zip", (Throwable)e);
            }
            HangingProtocols.OpeningViewer openingViewer = HangingProtocols.OpeningViewer.ALL_PATIENTS;
            DicomZipMediaIO.loadDicomZip(zipFile, dicomModel, openingViewer, GuiUtils.getUICore().getApplicationWindow());
        }
    }
}

