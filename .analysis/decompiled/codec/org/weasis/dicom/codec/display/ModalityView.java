/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.media.data.AttributeSource
 *  org.weasis.core.api.media.data.TagUtil
 *  org.weasis.core.api.media.data.TagView
 *  org.weasis.core.api.media.data.TagW
 *  org.weasis.core.api.media.data.XmlAttributeSource
 *  org.weasis.core.api.util.ResourceUtil
 *  org.weasis.core.api.util.ResourceUtil$ResourcePath
 *  org.weasis.core.util.StreamUtil
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.dicom.codec.display;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.media.data.AttributeSource;
import org.weasis.core.api.media.data.TagUtil;
import org.weasis.core.api.media.data.TagView;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.api.media.data.XmlAttributeSource;
import org.weasis.core.api.util.ResourceUtil;
import org.weasis.core.util.StreamUtil;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.codec.Messages;
import org.weasis.dicom.codec.TagD;
import org.weasis.dicom.codec.display.CornerDisplay;
import org.weasis.dicom.codec.display.Modality;
import org.weasis.dicom.codec.display.ModalityInfoData;
import org.weasis.dicom.codec.utils.DicomResource;

public class ModalityView {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModalityView.class);
    static final Map<Modality, ModalityInfoData> MODALITY_VIEW_MAP = new EnumMap<Modality, ModalityInfoData>(Modality.class);
    public static final ModalityInfoData DEFAULT_MODALITY_VIEW = new ModalityInfoData(Modality.DEFAULT, null);

    private ModalityView() {
    }

    public static ModalityInfoData getModlatityInfos(Modality mod) {
        ModalityInfoData mdata = MODALITY_VIEW_MAP.get((Object)mod);
        if (mdata == null) {
            mdata = MODALITY_VIEW_MAP.get((Object)Modality.DEFAULT);
        }
        if (mdata == null) {
            mdata = DEFAULT_MODALITY_VIEW;
        }
        return mdata;
    }

    public static Set<Map.Entry<Modality, ModalityInfoData>> getModalityViewEntries() {
        return MODALITY_VIEW_MAP.entrySet();
    }

    private static Modality getModality(String name) {
        try {
            return Modality.valueOf(name);
        }
        catch (Exception e) {
            if ("DS".equals(name)) {
                return Modality.XA;
            }
            if ("CF".equals(name) || "DF".equals(name) || "VF".equals(name)) {
                return Modality.RF;
            }
            if ("MA".equals(name) || "MS".equals(name)) {
                return Modality.MR;
            }
            if ("EC".equals(name) || "CD".equals(name) || "DD".equals(name)) {
                return Modality.US;
            }
            if ("ST".equals(name)) {
                return Modality.NM;
            }
            LOGGER.error("Modality reference of {} is missing", (Object)name, (Object)e);
            return null;
        }
    }

    private static CornerDisplay getCornerDisplay(String name) {
        try {
            return CornerDisplay.valueOf(name);
        }
        catch (Exception e) {
            LOGGER.error("CornerDisplay reference of {} doesn't exist", (Object)name, (Object)e);
            return null;
        }
    }

    private static TagView getTagView(String name, String format) {
        if (name != null) {
            String[] vals = name.split(",");
            ArrayList<TagW> list = new ArrayList<TagW>(vals.length);
            for (String s : vals) {
                TagW t = TagW.get((String)s);
                if (t == null) {
                    LOGGER.warn("Cannot find tag \"{}\"", (Object)s);
                    continue;
                }
                list.add(t);
            }
            if (!list.isEmpty()) {
                return new TagView(format, list.toArray(new TagW[0]));
            }
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void readTagDisplayByModality() {
        XMLStreamReader xmler = null;
        try {
            File file = ResourceUtil.getResource((ResourceUtil.ResourcePath)DicomResource.ATTRIBUTES_VIEW);
            if (!file.canRead()) {
                return;
            }
            XMLInputFactory factory = XMLInputFactory.newInstance();
            factory.setProperty("javax.xml.stream.isSupportingExternalEntities", Boolean.FALSE);
            factory.setProperty("javax.xml.stream.supportDTD", Boolean.FALSE);
            xmler = factory.createXMLStreamReader(new FileInputStream(file));
            while (xmler.hasNext()) {
                String key;
                if (xmler.next() != 1 || !"modalities".equals(key = xmler.getName().getLocalPart())) continue;
                ModalityView.readModalities(xmler);
            }
            StreamUtil.safeClose((XMLStreamReader)xmler);
        }
        catch (Exception e) {
            LOGGER.error("Cannot read attributes-view.xml! ", (Throwable)e);
        }
        finally {
            StreamUtil.safeClose(xmler);
        }
    }

    private static void readModalities(XMLStreamReader xmler) throws XMLStreamException {
        while (xmler.hasNext()) {
            String name;
            Modality m;
            String key;
            if (xmler.next() != 1 || !"modality".equals(key = xmler.getName().getLocalPart()) || xmler.getAttributeCount() < 1 || (m = ModalityView.getModality(name = xmler.getAttributeValue(null, "name"))) == null) continue;
            try {
                String extend = xmler.getAttributeValue(null, "extend");
                ModalityInfoData data = new ModalityInfoData(m, ModalityView.getModality(extend));
                ModalityView.readModality(data, xmler);
                MODALITY_VIEW_MAP.put(m, data);
            }
            catch (Exception e) {
                LOGGER.error("Modality {} cannot be read from attributes-view.xml", (Object)name, (Object)e);
            }
        }
    }

    private static void readModality(ModalityInfoData data, XMLStreamReader xmler) throws XMLStreamException {
        boolean state = true;
        block4: while (xmler.hasNext() && state) {
            switch (xmler.next()) {
                case 1: {
                    String name;
                    CornerDisplay corner;
                    if (!"corner".equals(xmler.getName().getLocalPart()) || xmler.getAttributeCount() < 1 || (corner = ModalityView.getCornerDisplay(name = xmler.getAttributeValue(null, "name"))) == null) continue block4;
                    ModalityView.readCorner(data, corner, xmler);
                    continue block4;
                }
                case 2: {
                    if (!"modality".equals(xmler.getName().getLocalPart())) continue block4;
                    state = false;
                    continue block4;
                }
            }
        }
    }

    private static void readCorner(ModalityInfoData data, CornerDisplay corner, XMLStreamReader xmler) throws XMLStreamException {
        TagView[] disElements = data.getCornerInfo(corner).getInfos();
        boolean state = true;
        int index = -1;
        String format = null;
        block5: while (xmler.hasNext() && state) {
            switch (xmler.next()) {
                case 4: {
                    if (index <= 0 || index > 7) continue block5;
                    disElements[index - 1] = ModalityView.getTag(xmler.getText(), format);
                    index = -1;
                    format = null;
                    continue block5;
                }
                case 1: {
                    if (!"p".equals(xmler.getName().getLocalPart()) || xmler.getAttributeCount() < 1) continue block5;
                    index = TagUtil.getIntegerTagAttribute((AttributeSource)new XmlAttributeSource(xmler), (String)"index", (Integer)-1);
                    format = xmler.getAttributeValue(null, "format");
                    continue block5;
                }
                case 2: {
                    if (!"corner".equals(xmler.getName().getLocalPart())) continue block5;
                    state = false;
                    continue block5;
                }
            }
        }
    }

    private static TagView getTag(String name, String format) {
        if (StringUtil.hasText((CharSequence)name)) {
            return ModalityView.getTagView(name, format);
        }
        return null;
    }

    static {
        TagView[] disElements = DEFAULT_MODALITY_VIEW.getCornerInfo(CornerDisplay.TOP_LEFT).getInfos();
        disElements[0] = new TagView(new TagW[]{TagD.get(0x100010)});
        disElements[1] = new TagView(new TagW[]{TagD.get(0x100030)});
        disElements[2] = new TagView(Messages.getString("ModalityView.id"), new TagW[]{TagD.get(0x100020)});
        disElements[3] = new TagView(Messages.getString("ModalityView.sex"), new TagW[]{TagD.get(0x100040)});
        disElements[4] = new TagView(new TagW[]{TagD.get(0x101010)});
        disElements = DEFAULT_MODALITY_VIEW.getCornerInfo(CornerDisplay.TOP_RIGHT).getInfos();
        disElements[0] = new TagView(new TagW[]{TagD.get(524416)});
        disElements[1] = new TagView(Messages.getString("ModalityView.desc25"), new TagW[]{TagD.get(528432)});
        disElements[2] = new TagView(Messages.getString("ModalityView.study"), new TagW[]{TagD.get(0x200010)});
        disElements[3] = new TagView(Messages.getString("ModalityView.ac_nb"), new TagW[]{TagD.get(524368)});
        disElements[4] = new TagView(Messages.getString("ModalityView.acq"), TagD.getTagFromIDs(524322, 524323, 1576978, 524321, 524320));
        disElements[5] = new TagView(Messages.getString("ModalityView.acq"), TagD.getTagFromIDs(524338, 524339, 1576980, 524337, 524336));
        disElements = DEFAULT_MODALITY_VIEW.getCornerInfo(CornerDisplay.BOTTOM_RIGHT).getInfos();
        disElements[1] = new TagView(Messages.getString("ModalityView.series_nb"), new TagW[]{TagD.get(0x200011)});
        disElements[2] = new TagView(Messages.getString("ModalityView.laterality"), TagD.getTagFromIDs(2134130, 0x200062, 0x200060));
        disElements[3] = new TagView(Messages.getString("ModalityView.desc25"), new TagW[]{TagD.get(0x180010)});
        disElements[4] = new TagView(Messages.getString("ModalityView.desc25"), new TagW[]{TagD.get(528446)});
        disElements[5] = new TagView(Messages.getString("ModalityView.thick"), new TagW[]{TagD.get(1572944)});
        disElements[6] = new TagView(Messages.getString("ModalityView.location"), new TagW[]{TagD.get(2101313)});
        MODALITY_VIEW_MAP.put(Modality.DEFAULT, DEFAULT_MODALITY_VIEW);
        ModalityView.readTagDisplayByModality();
    }
}

