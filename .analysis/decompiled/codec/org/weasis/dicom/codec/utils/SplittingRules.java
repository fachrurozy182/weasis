/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.media.data.TagW
 *  org.weasis.core.api.util.ResourceUtil
 *  org.weasis.core.api.util.ResourceUtil$ResourcePath
 *  org.weasis.core.util.StreamUtil
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.dicom.codec.utils;

import java.io.File;
import java.io.FileInputStream;
import java.util.EnumMap;
import java.util.Map;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.api.util.ResourceUtil;
import org.weasis.core.util.StreamUtil;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.codec.TagD;
import org.weasis.dicom.codec.display.Modality;
import org.weasis.dicom.codec.utils.DicomResource;
import org.weasis.dicom.codec.utils.SplittingModalityRules;

public class SplittingRules {
    private static final Logger LOGGER = LoggerFactory.getLogger(SplittingRules.class);
    private final Map<Modality, SplittingModalityRules> rules = new EnumMap<Modality, SplittingModalityRules>(Modality.class);

    public SplittingRules() {
        this.initDefault();
        this.readSplittingRulesFromResources();
    }

    private void initDefault() {
        SplittingModalityRules defRules = new SplittingModalityRules(Modality.DEFAULT);
        defRules.addSingleFrameTags(524296, null);
        defRules.addSingleFrameTags(524310, null);
        defRules.addSingleFrameTags(0x180010, null);
        defRules.addMultiFrameTags(524296, null);
        defRules.addMultiFrameTags(524312, null);
        defRules.addMultiFrameTags(561159, null);
        defRules.addMultiFrameTags(2134102, null);
        this.rules.put(defRules.getModality(), defRules);
        SplittingModalityRules ctRules = new SplittingModalityRules(Modality.CT, defRules);
        ctRules.addSingleFrameTags(1577488, null);
        ctRules.addSingleFrameTags(1577248, null);
        SplittingModalityRules.And allOf = new SplittingModalityRules.And();
        allOf.addChild(new SplittingModalityRules.DefaultCondition(TagD.get(524296), SplittingModalityRules.Condition.Type.notContainsIgnoreCase, "PROJECTION"));
        ctRules.addSingleFrameTags(TagW.ImageOrientationPlane, (SplittingModalityRules.Condition)allOf);
        this.rules.put(ctRules.getModality(), ctRules);
        SplittingModalityRules ptRules = new SplittingModalityRules(Modality.PT, defRules);
        ptRules.addSingleFrameTags(1577488, null);
        ptRules.addSingleFrameTags(1577248, null);
        this.rules.put(ptRules.getModality(), ptRules);
        SplittingModalityRules mrRules = new SplittingModalityRules(Modality.MR, defRules);
        mrRules.addSingleFrameTags(1572896, null);
        mrRules.addSingleFrameTags(1572897, null);
        mrRules.addSingleFrameTags(1572898, null);
        mrRules.addSingleFrameTags(0x180080, null);
        mrRules.addSingleFrameTags(0x180081, null);
        mrRules.addSingleFrameTags(1577748, null);
        mrRules.addSingleFrameTags(TagW.ImageOrientationPlane, (SplittingModalityRules.Condition)allOf);
        this.rules.put(mrRules.getModality(), mrRules);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void readSplittingRulesFromResources() {
        block8: {
            XMLStreamReader xmler = null;
            FileInputStream stream = null;
            try {
                File file = ResourceUtil.getResource((ResourceUtil.ResourcePath)DicomResource.SERIES_SPITTING_RULES);
                if (!file.canRead()) {
                    return;
                }
                XMLInputFactory factory = XMLInputFactory.newInstance();
                factory.setProperty("javax.xml.stream.isSupportingExternalEntities", Boolean.FALSE);
                factory.setProperty("javax.xml.stream.supportDTD", Boolean.FALSE);
                stream = new FileInputStream(file);
                xmler = factory.createXMLStreamReader(stream);
                while (xmler.hasNext()) {
                    String key;
                    int eventType = xmler.next();
                    if (eventType != 1 || !"modalities".equals(key = xmler.getName().getLocalPart())) continue;
                    this.readModalities(xmler);
                }
                StreamUtil.safeClose((XMLStreamReader)xmler);
            }
            catch (Exception e) {
                LOGGER.error("Cannot read series-splitting-rules.xml! ", (Throwable)e);
                break block8;
            }
            finally {
                StreamUtil.safeClose(xmler);
                StreamUtil.safeClose(stream);
            }
            StreamUtil.safeClose((AutoCloseable)stream);
        }
    }

    private void readModalities(XMLStreamReader xmler) throws XMLStreamException {
        while (xmler.hasNext()) {
            String name;
            Modality m;
            String key;
            int eventType = xmler.next();
            if (eventType != 1 || !"modality".equals(key = xmler.getName().getLocalPart()) || xmler.getAttributeCount() < 1 || (m = SplittingRules.getModality(name = xmler.getAttributeValue(null, "name"))) == null) continue;
            try {
                String extend = xmler.getAttributeValue(null, "extend");
                SplittingModalityRules splitRules = new SplittingModalityRules(m, this.getSplittingModalityRules(extend));
                SplittingRules.readModality(splitRules, xmler);
                this.rules.put(m, splitRules);
            }
            catch (Exception e) {
                LOGGER.error("Modality {} cannot be read from series-splitting-rules.xml", (Object)name, (Object)e);
            }
        }
    }

    private static void readModality(SplittingModalityRules data, XMLStreamReader xmler) throws XMLStreamException {
        boolean state = true;
        block4: while (xmler.hasNext() && state) {
            int eventType = xmler.next();
            switch (eventType) {
                case 1: {
                    String element = xmler.getName().getLocalPart();
                    if ("splittingTags".equals(element)) {
                        SplittingRules.readTags(data, xmler, false, element);
                        continue block4;
                    }
                    if (!"multiframeSplittingTags".equals(element)) continue block4;
                    SplittingRules.readTags(data, xmler, true, element);
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

    private static void readTags(SplittingModalityRules data, XMLStreamReader xmler, boolean multiframe, String endElement) throws XMLStreamException {
        boolean state = true;
        block4: while (xmler.hasNext() && state) {
            int eventType = xmler.next();
            switch (eventType) {
                case 1: {
                    TagW tag = SplittingRules.getTag(xmler.getName().getLocalPart());
                    if (tag == null) continue block4;
                    SplittingModalityRules.Condition condition = SplittingRules.readCondition(data, xmler, tag.getKeyword());
                    if (multiframe) {
                        data.addMultiFrameTags(tag, condition);
                        continue block4;
                    }
                    data.addSingleFrameTags(tag, condition);
                    continue block4;
                }
                case 2: {
                    if (!endElement.equals(xmler.getName().getLocalPart())) continue block4;
                    state = false;
                    continue block4;
                }
            }
        }
    }

    private static SplittingModalityRules.Condition readCondition(SplittingModalityRules data, XMLStreamReader xmler, String endElement) throws XMLStreamException {
        SplittingModalityRules.CompositeCondition conditions = null;
        SplittingModalityRules.CompositeCondition lastConditions = null;
        TagW tag = null;
        SplittingModalityRules.Condition.Type type = null;
        String value = null;
        boolean state = true;
        block5: while (xmler.hasNext() && state) {
            int eventType = xmler.next();
            switch (eventType) {
                case 4: {
                    value = xmler.getText();
                    continue block5;
                }
                case 1: {
                    if ("condition".equals(xmler.getName().getLocalPart())) {
                        tag = SplittingRules.getTag(xmler.getAttributeValue(null, "tag"));
                        type = SplittingRules.getConditionType(xmler.getAttributeValue(null, "type"));
                        continue block5;
                    }
                    if (!"conditions".equals(xmler.getName().getLocalPart())) continue block5;
                    String t = xmler.getAttributeValue(null, "type");
                    SplittingModalityRules.CompositeCondition compositeCondition = lastConditions = "anyOf".equals(t) ? new SplittingModalityRules.Or() : new SplittingModalityRules.And();
                    if (conditions == null) {
                        conditions = lastConditions;
                        continue block5;
                    }
                    conditions.addChild(lastConditions);
                    continue block5;
                }
                case 2: {
                    if ("condition".equals(xmler.getName().getLocalPart())) {
                        if (tag == null || type == null || value == null) {
                            LOGGER.error("Cannot read condition: {} {} {}", new Object[]{tag, type, value});
                        }
                        if (lastConditions != null) {
                            lastConditions.addChild(new SplittingModalityRules.DefaultCondition(tag, type, value));
                        }
                        tag = null;
                        type = null;
                        value = null;
                        continue block5;
                    }
                    if (!endElement.equals(xmler.getName().getLocalPart())) continue block5;
                    state = false;
                    continue block5;
                }
            }
        }
        return conditions;
    }

    private SplittingModalityRules getSplittingModalityRules(String extend) {
        SplittingModalityRules val;
        if (StringUtil.hasText((CharSequence)extend) && (val = this.rules.get((Object)SplittingRules.getModality(extend))) == null) {
            LOGGER.error("Modality {} doesn't exist! Cannot inherit the rules.", (Object)extend);
        }
        return null;
    }

    public SplittingModalityRules getSplittingModalityRules(Modality key, Modality defaultKey) {
        SplittingModalityRules val = this.rules.get((Object)key);
        if (val == null) {
            val = this.rules.get((Object)defaultKey);
        }
        return val;
    }

    private static SplittingModalityRules.Condition.Type getConditionType(String type) {
        try {
            return SplittingModalityRules.Condition.Type.valueOf(type);
        }
        catch (Exception e) {
            LOGGER.error("{} is not a valid condition type", (Object)type, (Object)e);
            return null;
        }
    }

    private static Modality getModality(String name) {
        try {
            return Modality.valueOf(name);
        }
        catch (Exception e) {
            LOGGER.error("Modality reference of {} is missing", (Object)name, (Object)e);
            return null;
        }
    }

    private static TagW getTag(String tagKey) {
        TagW tag = TagW.get((String)tagKey);
        if (tag == null) {
            LOGGER.error("Cannot find a tag with the keyword {}", (Object)tagKey);
        }
        return tag;
    }
}

