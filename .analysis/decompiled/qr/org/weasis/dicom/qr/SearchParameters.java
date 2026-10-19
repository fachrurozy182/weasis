/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.osgi.framework.BundleContext
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.gui.util.AppProperties
 *  org.weasis.core.api.service.BundlePreferences
 *  org.weasis.core.util.StreamUtil
 *  org.weasis.core.util.StringUtil
 *  org.weasis.dicom.param.DicomParam
 */
package org.weasis.dicom.qr;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JComboBox;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.gui.util.AppProperties;
import org.weasis.core.api.service.BundlePreferences;
import org.weasis.core.util.StreamUtil;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.param.DicomParam;
import org.weasis.dicom.qr.DicomQrView;
import org.weasis.dicom.qr.Messages;

public class SearchParameters {
    private static final Logger LOGGER = LoggerFactory.getLogger(SearchParameters.class);
    static final String FILENAME = "searchParameters.xml";
    static final String T_NODES = "searchParametersList";
    static final String T_NODE = "searchParameters";
    static final String T_NAME = "name";
    static final String T_PERIOD = "period";
    static final String T_PARAMS = "dicomParams";
    static final String T_PARAM = "dicomParam";
    static final String T_TAG = "tag";
    static final String T_VALUES = "values";
    static final String T_VALUE = "value";
    static final String T_PARENT_SEQ = "parentSeqTags";
    private String name;
    private DicomQrView.Period period;
    private final ArrayList<DicomParam> parameters = new ArrayList();

    public SearchParameters(String name) {
        this.setName(name);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        if (StringUtil.hasText((CharSequence)name)) {
            this.name = name;
        }
    }

    public List<DicomParam> getParameters() {
        return this.parameters;
    }

    public DicomQrView.Period getPeriod() {
        return this.period;
    }

    public void setPeriod(DicomQrView.Period period) {
        this.period = period;
    }

    public String toString() {
        return this.name;
    }

    public void saveSearchParameters(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeAttribute(T_NAME, this.name);
        writer.writeAttribute(T_PERIOD, StringUtil.getEmptyStringIfNullEnum((Enum)this.period));
        writer.writeStartElement(T_PARAMS);
        for (DicomParam p : this.parameters) {
            writer.writeStartElement(T_PARAM);
            writer.writeAttribute(T_TAG, String.valueOf(p.getTag()));
            if (p.getParentSeqTags() != null) {
                writer.writeAttribute(T_PARENT_SEQ, String.join((CharSequence)",", (CharSequence[])Arrays.stream(p.getParentSeqTags()).mapToObj(String::valueOf).toArray(String[]::new)));
            }
            if (p.getValues() != null) {
                writer.writeStartElement(T_VALUES);
                for (String val : p.getValues()) {
                    writer.writeStartElement(T_VALUE);
                    writer.writeCharacters(val);
                    writer.writeEndElement();
                }
                writer.writeEndElement();
            }
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    public static void loadSearchParameters(JComboBox<SearchParameters> comboBox) {
        SearchParameters emptyParameters = new SearchParameters(Messages.getString("empty"));
        comboBox.addItem(emptyParameters);
        SearchParameters todayParameters = new SearchParameters(DicomQrView.Period.TODAY.toString());
        todayParameters.setPeriod(DicomQrView.Period.TODAY);
        comboBox.addItem(todayParameters);
        List<SearchParameters> list = SearchParameters.loadSearchParameters();
        for (SearchParameters node : list) {
            comboBox.addItem(node);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static List<SearchParameters> loadSearchParameters() {
        ArrayList<SearchParameters> list = new ArrayList<SearchParameters>();
        BundleContext context = AppProperties.getBundleContext(DicomQrView.class);
        File file = BundlePreferences.getFileInDataFolder((BundleContext)context, (String)FILENAME).toFile();
        if (file.canRead()) {
            XMLStreamReader xmler = null;
            try {
                XMLInputFactory factory = XMLInputFactory.newInstance();
                factory.setProperty("javax.xml.stream.isSupportingExternalEntities", Boolean.FALSE);
                factory.setProperty("javax.xml.stream.supportDTD", Boolean.FALSE);
                xmler = factory.createXMLStreamReader(new FileInputStream(file));
                while (xmler.hasNext()) {
                    String key;
                    int eventType = xmler.next();
                    if (eventType != 1 || !T_NODES.equals(key = xmler.getName().getLocalPart())) continue;
                    while (xmler.hasNext()) {
                        eventType = xmler.next();
                        if (eventType != 1) continue;
                        SearchParameters.readSearchParameters(xmler, list);
                    }
                }
                StreamUtil.safeClose((XMLStreamReader)xmler);
            }
            catch (Exception e) {
                LOGGER.error("Error on reading DICOM node file", (Throwable)e);
            }
            finally {
                StreamUtil.safeClose(xmler);
            }
        }
        return list;
    }

    private static void readSearchParameters(XMLStreamReader xmler, List<SearchParameters> list) {
        if (T_NODE.equals(xmler.getName().getLocalPart())) {
            try {
                SearchParameters node = new SearchParameters(xmler.getAttributeValue(null, T_NAME));
                node.setPeriod(DicomQrView.Period.getPeriod(xmler.getAttributeValue(null, T_PERIOD)));
                boolean stateNode = true;
                while (xmler.hasNext() && stateNode) {
                    int eventType = xmler.next();
                    if (eventType == 1) {
                        if (!T_PARAMS.equals(xmler.getName().getLocalPart())) continue;
                        boolean stateParams = true;
                        while (xmler.hasNext() && stateParams) {
                            eventType = xmler.next();
                            if (eventType == 1) {
                                SearchParameters.buildDicomParams(xmler, node);
                                continue;
                            }
                            if (eventType != 2 || !T_PARAMS.equals(xmler.getName().getLocalPart())) continue;
                            stateParams = false;
                        }
                        continue;
                    }
                    if (eventType != 2 || !T_NODE.equals(xmler.getName().getLocalPart())) continue;
                    stateNode = false;
                }
                list.add(node);
            }
            catch (Exception e) {
                LOGGER.error("Cannot read SearchParameters", (Throwable)e);
            }
        }
    }

    private static void buildDicomParams(XMLStreamReader xmler, SearchParameters node) throws XMLStreamException {
        String key = xmler.getName().getLocalPart();
        if (T_PARAM.equals(key)) {
            int tag = StringUtil.getInt((String)xmler.getAttributeValue(null, T_TAG));
            int[] parentSeq = StringUtil.getIntegerArray((String)xmler.getAttributeValue(null, T_PARENT_SEQ), (String)",");
            ArrayList<String> values = new ArrayList<String>();
            boolean state = true;
            while (xmler.hasNext() && state) {
                int eventType = xmler.next();
                if (eventType == 1) {
                    if (T_VALUES.equals(xmler.getName().getLocalPart())) {
                        boolean stateValues = true;
                        while (xmler.hasNext() && stateValues) {
                            eventType = xmler.next();
                            if (eventType == 1) {
                                SearchParameters.buildDicomParam(xmler, values);
                                continue;
                            }
                            if (eventType != 2 || !T_VALUES.equals(xmler.getName().getLocalPart())) continue;
                            stateValues = false;
                        }
                    }
                    DicomParam param = new DicomParam(parentSeq, tag, values.toArray(new String[0]));
                    node.getParameters().add(param);
                    continue;
                }
                if (eventType != 2 || !T_PARAM.equals(xmler.getName().getLocalPart())) continue;
                state = false;
            }
        }
    }

    private static void buildDicomParam(XMLStreamReader xmler, List<String> values) throws XMLStreamException {
        String key = xmler.getName().getLocalPart();
        if (T_VALUE.equals(key)) {
            values.add(xmler.getElementText());
        }
    }
}

