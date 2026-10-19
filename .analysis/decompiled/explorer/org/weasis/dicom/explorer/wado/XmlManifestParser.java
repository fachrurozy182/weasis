/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.media.data.AttributeSource
 *  org.weasis.core.api.media.data.MediaSeriesGroup
 *  org.weasis.core.api.media.data.XmlAttributeSource
 *  org.weasis.core.api.util.BiConsumerWithException
 *  org.weasis.dicom.codec.DicomSeries
 *  org.weasis.dicom.codec.TagD$Level
 *  org.weasis.dicom.codec.utils.SeriesInstanceList
 *  org.weasis.dicom.mf.WadoParameters
 */
package org.weasis.dicom.explorer.wado;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stax.StAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.media.data.AttributeSource;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.api.media.data.XmlAttributeSource;
import org.weasis.core.api.util.BiConsumerWithException;
import org.weasis.dicom.codec.DicomSeries;
import org.weasis.dicom.codec.TagD;
import org.weasis.dicom.codec.utils.SeriesInstanceList;
import org.weasis.dicom.explorer.wado.ManifestCompletion;
import org.weasis.dicom.explorer.wado.ManifestModelBuilder;
import org.weasis.dicom.explorer.wado.ReaderParams;
import org.weasis.dicom.mf.WadoParameters;
import org.xml.sax.SAXException;

final class XmlManifestParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(XmlManifestParser.class);
    private static final boolean XSD_VALIDATION = Boolean.getBoolean("weasis.manifest.xsd.validation");
    private static volatile Schema manifestSchema;

    private XmlManifestParser() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void parse(Path xmlFile, ReaderParams params) throws XMLStreamException {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty("javax.xml.stream.isSupportingExternalEntities", Boolean.FALSE);
        factory.setProperty("javax.xml.stream.supportDTD", Boolean.FALSE);
        if (XSD_VALIDATION) {
            XmlManifestParser.validate(xmlFile, factory);
        }
        try (BufferedInputStream in = new BufferedInputStream(Files.newInputStream(xmlFile, new OpenOption[0]));
             XMLStreamReader xmler = factory.createXMLStreamReader(in);){
            XmlManifestParser.readManifest(xmler, params);
        }
        catch (IOException e) {
            throw new XMLStreamException(e);
        }
    }

    private static void validate(Path xmlFile, XMLInputFactory factory) {
        try (BufferedInputStream in = new BufferedInputStream(Files.newInputStream(xmlFile, new OpenOption[0]));){
            XMLStreamReader xmler = factory.createXMLStreamReader(in);
            Validator validator = XmlManifestParser.manifestSchema().newValidator();
            validator.setProperty("http://javax.xml.XMLConstants/property/accessExternalDTD", "");
            validator.setProperty("http://javax.xml.XMLConstants/property/accessExternalSchema", "");
            validator.validate(new StAXSource(xmler));
            xmler.close();
            LOGGER.info("[Validate with XSD schema] the manifest is valid");
        }
        catch (SAXException e) {
            LOGGER.error("[Validate with XSD schema] the manifest is NOT valid", (Throwable)e);
        }
        catch (Exception e) {
            LOGGER.error("Error when validate XSD schema.", (Throwable)e);
        }
    }

    private static Schema manifestSchema() throws SAXException {
        Schema schema = manifestSchema;
        if (schema == null) {
            SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
            manifestSchema = schema = schemaFactory.newSchema(XmlManifestParser.class.getResource("/config/manifest.xsd"));
        }
        return schema;
    }

    private static void readManifest(XMLStreamReader xmler, ReaderParams params) throws XMLStreamException {
        BiConsumerWithException method = (x, r) -> {
            if ("manifest".equals(x.getName().getLocalPart())) {
                BiConsumerWithException method2 = (x2, r2) -> {
                    if ("arcQuery".equals(x2.getName().getLocalPart())) {
                        XmlManifestParser.readArcQuery(x2, r2);
                    }
                };
                XmlManifestParser.readElement(x, "manifest", (BiConsumerWithException<XMLStreamReader, ReaderParams, XMLStreamException>)method2, r);
            }
        };
        XmlManifestParser.readElement(xmler, "manifest", (BiConsumerWithException<XMLStreamReader, ReaderParams, XMLStreamException>)method, params);
    }

    private static void readArcQuery(XMLStreamReader xmler, ReaderParams params) throws XMLStreamException {
        WadoParameters wadoParameters = ManifestModelBuilder.buildArcQueryParameters((AttributeSource)new XmlAttributeSource(xmler), params);
        LinkedHashSet<MediaSeriesGroup> patients = new LinkedHashSet<MediaSeriesGroup>();
        BiConsumerWithException method = (x, r) -> {
            String key = x.getName().getLocalPart();
            if (TagD.Level.PATIENT.getTagName().equals(key)) {
                patients.add(XmlManifestParser.readPatient(x, params, wadoParameters));
            } else if ("httpTag".equals(key)) {
                ManifestModelBuilder.addHttpTag((AttributeSource)new XmlAttributeSource(x), wadoParameters);
            } else if ("Message".equals(key)) {
                ManifestModelBuilder.showViewerMessage((AttributeSource)new XmlAttributeSource(x));
            }
        };
        XmlManifestParser.readElement(xmler, "arcQuery", (BiConsumerWithException<XMLStreamReader, ReaderParams, XMLStreamException>)method, params);
        ManifestModelBuilder.focusUniquePatient(patients);
        ManifestModelBuilder.startDownloads(params, wadoParameters);
    }

    private static MediaSeriesGroup readPatient(XMLStreamReader xmler, ReaderParams params, WadoParameters wadoParameters) throws XMLStreamException {
        MediaSeriesGroup patient = ManifestModelBuilder.buildPatient((AttributeSource)new XmlAttributeSource(xmler), params, wadoParameters);
        BiConsumerWithException method = (x, r) -> XmlManifestParser.readStudy(x, params, patient, wadoParameters);
        XmlManifestParser.readElement(xmler, TagD.Level.STUDY.getTagName(), TagD.Level.PATIENT.getTagName(), (BiConsumerWithException<XMLStreamReader, ReaderParams, XMLStreamException>)method, params);
        if (ManifestCompletion.isPartialLevel(wadoParameters, !params.getModel().getChildren(patient).isEmpty())) {
            ManifestCompletion.completePatient(params, wadoParameters, patient);
        }
        return patient;
    }

    private static void readStudy(XMLStreamReader xmler, ReaderParams params, MediaSeriesGroup patient, WadoParameters wadoParameters) throws XMLStreamException {
        MediaSeriesGroup study = ManifestModelBuilder.buildStudy((AttributeSource)new XmlAttributeSource(xmler), params, patient);
        BiConsumerWithException method = (x, r) -> XmlManifestParser.readSeries(x, params, patient, study, wadoParameters);
        XmlManifestParser.readElement(xmler, TagD.Level.SERIES.getTagName(), TagD.Level.STUDY.getTagName(), (BiConsumerWithException<XMLStreamReader, ReaderParams, XMLStreamException>)method, params);
        if (ManifestCompletion.isPartialLevel(wadoParameters, !params.getModel().getChildren(study).isEmpty())) {
            ManifestCompletion.completeStudy(params, wadoParameters, patient, study);
        }
    }

    private static void readSeries(XMLStreamReader xmler, ReaderParams params, MediaSeriesGroup patient, MediaSeriesGroup study, WadoParameters wadoParameters) throws XMLStreamException {
        DicomSeries dicomSeries = ManifestModelBuilder.buildSeries((AttributeSource)new XmlAttributeSource(xmler), params, study, wadoParameters);
        if (dicomSeries == null) {
            return;
        }
        SeriesInstanceList seriesInstanceList = ManifestModelBuilder.getSeriesInstanceList(dicomSeries);
        BiConsumerWithException method = (x, r) -> ManifestModelBuilder.addSopInstance((AttributeSource)new XmlAttributeSource(x), seriesInstanceList);
        XmlManifestParser.readElement(xmler, TagD.Level.INSTANCE.getTagName(), TagD.Level.SERIES.getTagName(), (BiConsumerWithException<XMLStreamReader, ReaderParams, XMLStreamException>)method, params);
        ManifestModelBuilder.completeSeries(params, patient, study, dicomSeries, seriesInstanceList);
    }

    private static void readElement(XMLStreamReader xmler, String endElement, BiConsumerWithException<XMLStreamReader, ReaderParams, XMLStreamException> method, ReaderParams params) throws XMLStreamException {
        boolean state = true;
        while (xmler.hasNext() && state) {
            int eventType = xmler.next();
            switch (eventType) {
                case 1: {
                    method.accept((Object)xmler, (Object)params);
                    break;
                }
                case 2: {
                    if (!endElement.equals(xmler.getName().getLocalPart())) break;
                    state = false;
                    break;
                }
            }
        }
    }

    private static void readElement(XMLStreamReader xmler, String startElement, String endElement, BiConsumerWithException<XMLStreamReader, ReaderParams, XMLStreamException> method, ReaderParams params) throws XMLStreamException {
        boolean state = true;
        while (xmler.hasNext() && state) {
            int eventType = xmler.next();
            switch (eventType) {
                case 1: {
                    if (!startElement.equals(xmler.getName().getLocalPart())) break;
                    method.accept((Object)xmler, (Object)params);
                    break;
                }
                case 2: {
                    if (!endElement.equals(xmler.getName().getLocalPart())) break;
                    state = false;
                    break;
                }
            }
        }
    }
}

