/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.osgi.service.component.ComponentContext
 *  org.osgi.service.component.annotations.Activate
 *  org.osgi.service.component.annotations.Component
 *  org.osgi.service.component.annotations.Deactivate
 *  org.osgi.service.component.annotations.Reference
 *  org.osgi.service.component.annotations.ReferenceCardinality
 *  org.osgi.service.component.annotations.ReferencePolicy
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.gui.util.GuiUtils
 *  org.weasis.core.api.media.data.Codec
 *  org.weasis.core.api.media.data.MediaReader
 *  org.weasis.imageio.codec.ImageioUtil
 */
package org.weasis.dicom.codec;

import java.net.URI;
import java.util.Hashtable;
import javax.imageio.spi.IIOServiceProvider;
import org.dcm4che3.data.SpecificCharacterSet;
import org.dcm4che3.img.DicomImageReaderSpi;
import org.dcm4che3.util.UIDUtils;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.media.data.Codec;
import org.weasis.core.api.media.data.MediaReader;
import org.weasis.dicom.codec.DicomImageElement;
import org.weasis.dicom.codec.DicomMediaIO;
import org.weasis.dicom.codec.DicomSpecialElementFactory;
import org.weasis.imageio.codec.ImageioUtil;

@Component(service={Codec.class})
public class DicomCodec
implements Codec<DicomImageElement> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DicomCodec.class);
    public static final String NAME = "dcm4che";
    public static final String[] FILE_EXTENSIONS = new String[]{"dcm", "dic", "dicm", "dicom"};
    private static final String LOGGER_KEY = "always.info.ItemParser";
    private static final String LOGGER_VAL = "org.dcm4che3.imageio.ItemParser";
    private static final IIOServiceProvider[] dcm4cheCodecs = new IIOServiceProvider[]{new DicomImageReaderSpi()};

    public String[] getReaderMIMETypes() {
        return new String[]{"application/dicom", "xds-i/dicom", "image/dicom", "video/dicom", "encap/dicom"};
    }

    public String[] getReaderExtensions() {
        return FILE_EXTENSIONS;
    }

    public boolean isMimeTypeSupported(String mimeType) {
        if (mimeType != null) {
            for (String mime : this.getReaderMIMETypes()) {
                if (!mimeType.equals(mime)) continue;
                return true;
            }
        }
        return false;
    }

    public MediaReader<DicomImageElement> getMediaIO(URI media, String mimeType, Hashtable<String, Object> properties) {
        if (this.isMimeTypeSupported(mimeType)) {
            return new DicomMediaIO(media);
        }
        return null;
    }

    public String getCodecName() {
        return NAME;
    }

    public String[] getWriterExtensions() {
        return FILE_EXTENSIONS;
    }

    public String[] getWriterMIMETypes() {
        return new String[]{"application/dicom"};
    }

    @Activate
    protected void activate(ComponentContext context) {
        LOGGER.info("Activate DicomCodec");
        String weasisRootUID = GuiUtils.getUICore().getSystemPreferences().getProperty("weasis.dicom.root.uid", UIDUtils.getRoot());
        UIDUtils.setRoot(weasisRootUID);
        SpecificCharacterSet.setDefaultCharacterSet("ISO_IR 192");
        for (IIOServiceProvider p : dcm4cheCodecs) {
            ImageioUtil.registerServiceProvider((IIOServiceProvider)p);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        LOGGER.info("Deactivate DicomCodec");
        for (IIOServiceProvider p : dcm4cheCodecs) {
            ImageioUtil.deregisterServiceProvider((IIOServiceProvider)p);
        }
    }

    @Reference(service=DicomSpecialElementFactory.class, cardinality=ReferenceCardinality.MULTIPLE, policy=ReferencePolicy.DYNAMIC, unbind="removeDicomSpecialElementFactory")
    void addDicomSpecialElementFactory(DicomSpecialElementFactory factory) {
        String name = factory.getClass().getName();
        for (String modality : factory.getModalities()) {
            DicomSpecialElementFactory prev = DicomMediaIO.DCM_ELEMENT_FACTORIES.put(modality, factory);
            if (prev != null) {
                LOGGER.warn("{} factory has been replaced by {}", (Object)prev.getClass().getName(), (Object)name);
            }
            LOGGER.info("Register DicomSpecialElementFactory: {} => {}", (Object)modality, (Object)name);
        }
    }

    void removeDicomSpecialElementFactory(DicomSpecialElementFactory factory) {
        String name = factory.getClass().getName();
        for (String modality : factory.getModalities()) {
            DicomSpecialElementFactory f = DicomMediaIO.DCM_ELEMENT_FACTORIES.get(modality);
            if (factory.equals(f)) {
                DicomMediaIO.DCM_ELEMENT_FACTORIES.remove(modality);
                LOGGER.info("Unregister DicomSpecialElementFactory: {} => {}", (Object)modality, (Object)name);
                continue;
            }
            LOGGER.warn("{}: Unregistering {} has no effect, {} is registered instead", new Object[]{modality, name, f.getClass().getName()});
        }
    }
}

