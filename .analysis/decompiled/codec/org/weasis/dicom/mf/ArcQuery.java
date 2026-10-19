/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.dicom.mf;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.mf.HttpTag;
import org.weasis.dicom.mf.JsonManifestSerializer;
import org.weasis.dicom.mf.ManifestFormat;
import org.weasis.dicom.mf.ManifestNode;
import org.weasis.dicom.mf.ManifestSerializer;
import org.weasis.dicom.mf.Patient;
import org.weasis.dicom.mf.QueryMode;
import org.weasis.dicom.mf.QueryResult;
import org.weasis.dicom.mf.ViewerMessage;
import org.weasis.dicom.mf.WadoParameters;
import org.weasis.dicom.mf.XmlManifest;
import org.weasis.dicom.mf.XmlManifestSerializer;

public class ArcQuery
implements XmlManifest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArcQuery.class);
    private static final String ATTR_HTTP_TAG_KEY = "key";
    private static final String ATTR_HTTP_TAG_VALUE = "value";
    private final List<QueryResult> queryList;
    private final String manifestUID;

    public ArcQuery(List<QueryResult> resultList) {
        this(resultList, null);
    }

    public ArcQuery(List<QueryResult> resultList, String manifestUID) {
        this.queryList = Objects.requireNonNull(resultList, "Result list cannot be null");
        this.manifestUID = StringUtil.hasText((CharSequence)manifestUID) ? manifestUID : UUID.randomUUID().toString();
    }

    public List<QueryResult> getQueryList() {
        return this.queryList;
    }

    @Override
    public String xmlManifest(String version) {
        String string;
        StringWriter writer = new StringWriter();
        try {
            this.writeManifest(writer, version);
            string = writer.toString();
        }
        catch (Throwable throwable) {
            try {
                try {
                    writer.close();
                }
                catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
            catch (IOException e) {
                LOGGER.error("Cannot write manifest", (Throwable)e);
                return null;
            }
        }
        writer.close();
        return string;
    }

    public String jsonManifest(String version) {
        String string;
        StringWriter writer = new StringWriter();
        try {
            this.writeManifest(writer, version, ManifestFormat.JSON);
            string = writer.toString();
        }
        catch (Throwable throwable) {
            try {
                try {
                    writer.close();
                }
                catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
            catch (IOException e) {
                LOGGER.error("Cannot write manifest", (Throwable)e);
                return null;
            }
        }
        writer.close();
        return string;
    }

    public String manifest(String version, ManifestFormat format) {
        return format == ManifestFormat.JSON ? this.jsonManifest(version) : this.xmlManifest(version);
    }

    @Override
    public void writeManifest(Writer writer, String version) throws IOException {
        this.writeModernManifest(new XmlManifestSerializer(writer, this.getCharsetEncoding()));
    }

    public void writeManifest(Writer writer, String version, ManifestFormat format) throws IOException {
        if (format == ManifestFormat.JSON) {
            this.writeModernManifest(new JsonManifestSerializer(writer));
        } else {
            this.writeManifest(writer, version);
        }
    }

    public static boolean hasContent(QueryResult archive) {
        return !archive.getPatients().isEmpty() || !archive.getViewerMessages().isEmpty();
    }

    private void writeModernManifest(ManifestSerializer serializer) throws IOException {
        serializer.beginDocument();
        serializer.beginObject("manifest");
        serializer.attribute("uid", this.manifestUID);
        serializer.schema("xmlns=\"http://www.weasis.org/xsd/2.5\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
        serializer.beginArray("arcQuery");
        for (QueryResult archive : this.queryList) {
            if (!ArcQuery.hasContent(archive)) continue;
            this.writeArchiveQuery(serializer, archive);
        }
        serializer.endArray();
        serializer.endObject();
        serializer.endDocument();
    }

    private void writeArchiveQuery(ManifestSerializer serializer, QueryResult archive) throws IOException {
        WadoParameters wadoParams = archive.getWadoParameters();
        serializer.beginObject("arcQuery");
        serializer.attribute("arcId", wadoParams.getArchiveID());
        serializer.attribute("baseUrl", wadoParams.getBaseURL());
        QueryMode queryMode = wadoParams.getQueryMode();
        if (queryMode != QueryMode.DEFAULT) {
            serializer.attribute("queryMode", queryMode.name());
        }
        this.writeCommonQueryAttributes(serializer, wadoParams);
        this.writeQueryContent(serializer, archive, wadoParams);
        serializer.endObject();
    }

    private void writeCommonQueryAttributes(ManifestSerializer serializer, WadoParameters wadoParams) throws IOException {
        serializer.attribute("webLogin", wadoParams.getWebLogin());
        serializer.attribute("requireOnlySOPInstanceUID", wadoParams.isRequireOnlySOPInstanceUID());
        serializer.attribute("additionalParameters", wadoParams.getAdditionalParameters());
        serializer.attribute("overrideDicomTagsList", wadoParams.getOverrideDicomTagsList());
    }

    private void writeQueryContent(ManifestSerializer serializer, QueryResult archive, WadoParameters wadoParams) throws IOException {
        ArcQuery.writeHttpTags(serializer, wadoParams.getHttpTaglist());
        ArcQuery.writeViewerMessages(serializer, archive.getViewerMessages());
        ArcQuery.writePatients(serializer, new ArrayList<Patient>(archive.getPatients().values()));
    }

    private static void writePatients(ManifestSerializer serializer, List<Patient> patientList) throws IOException {
        if (patientList.isEmpty()) {
            return;
        }
        Collections.sort(patientList);
        serializer.beginArray(ManifestNode.Level.PATIENT.getTagName());
        for (Patient patient : patientList) {
            patient.write(serializer);
        }
        serializer.endArray();
    }

    private static void writeHttpTags(ManifestSerializer serializer, List<HttpTag> httpTags) throws IOException {
        if (httpTags == null || httpTags.isEmpty()) {
            return;
        }
        serializer.beginArray("httpTag");
        for (HttpTag tag : httpTags) {
            serializer.beginLeaf("httpTag");
            serializer.attribute(ATTR_HTTP_TAG_KEY, tag.getKey());
            serializer.attribute(ATTR_HTTP_TAG_VALUE, tag.getValue());
            serializer.endLeaf();
        }
        serializer.endArray();
    }

    private static void writeViewerMessages(ManifestSerializer serializer, List<ViewerMessage> messages) throws IOException {
        if (messages == null || messages.isEmpty()) {
            return;
        }
        serializer.beginArray("Message");
        for (ViewerMessage message : messages) {
            serializer.beginLeaf("Message");
            serializer.attribute("title", message.title());
            serializer.attribute("description", message.message());
            serializer.attribute("severity", message.level().name());
            serializer.endLeaf();
        }
        serializer.endArray();
    }
}

