/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.json.Json
 *  jakarta.json.JsonObject
 *  jakarta.json.JsonReader
 *  jakarta.json.JsonValue
 *  jakarta.json.JsonValue$ValueType
 *  org.weasis.core.api.media.data.MediaSeriesGroup
 *  org.weasis.dicom.codec.DicomSeries
 *  org.weasis.dicom.codec.TagD$Level
 *  org.weasis.dicom.codec.utils.SeriesInstanceList
 *  org.weasis.dicom.mf.WadoParameters
 */
package org.weasis.dicom.explorer.wado;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.dicom.codec.DicomSeries;
import org.weasis.dicom.codec.TagD;
import org.weasis.dicom.codec.utils.SeriesInstanceList;
import org.weasis.dicom.explorer.wado.JsonAttributeSource;
import org.weasis.dicom.explorer.wado.ManifestCompletion;
import org.weasis.dicom.explorer.wado.ManifestModelBuilder;
import org.weasis.dicom.explorer.wado.ReaderParams;
import org.weasis.dicom.mf.WadoParameters;

final class JsonManifestParser {
    private JsonManifestParser() {
    }

    static void parse(Path jsonFile, ReaderParams params) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(jsonFile, StandardCharsets.UTF_8);
             JsonReader jsonReader = Json.createReader((Reader)reader);){
            JsonObject manifest = jsonReader.readObject().getJsonObject("manifest");
            if (manifest != null) {
                for (JsonObject arcQuery : JsonManifestParser.children(manifest, "arcQuery")) {
                    JsonManifestParser.readArcQuery(arcQuery, params);
                }
            }
        }
    }

    private static void readArcQuery(JsonObject arcQuery, ReaderParams params) {
        WadoParameters wadoParameters = ManifestModelBuilder.buildArcQueryParameters(new JsonAttributeSource(arcQuery), params);
        for (JsonObject tag : JsonManifestParser.children(arcQuery, "httpTag")) {
            ManifestModelBuilder.addHttpTag(new JsonAttributeSource(tag), wadoParameters);
        }
        for (JsonObject message : JsonManifestParser.children(arcQuery, "Message")) {
            ManifestModelBuilder.showViewerMessage(new JsonAttributeSource(message));
        }
        LinkedHashSet<MediaSeriesGroup> patients = new LinkedHashSet<MediaSeriesGroup>();
        for (JsonObject patient : JsonManifestParser.children(arcQuery, TagD.Level.PATIENT.getTagName())) {
            patients.add(JsonManifestParser.readPatient(patient, params, wadoParameters));
        }
        ManifestModelBuilder.focusUniquePatient(patients);
        ManifestModelBuilder.startDownloads(params, wadoParameters);
    }

    private static MediaSeriesGroup readPatient(JsonObject patient, ReaderParams params, WadoParameters wadoParameters) {
        MediaSeriesGroup patientNode = ManifestModelBuilder.buildPatient(new JsonAttributeSource(patient), params, wadoParameters);
        List<JsonObject> studies = JsonManifestParser.children(patient, TagD.Level.STUDY.getTagName());
        if (ManifestCompletion.isPartialLevel(wadoParameters, !studies.isEmpty())) {
            ManifestCompletion.completePatient(params, wadoParameters, patientNode);
        } else {
            for (JsonObject study : studies) {
                JsonManifestParser.readStudy(study, params, patientNode, wadoParameters);
            }
        }
        return patientNode;
    }

    private static void readStudy(JsonObject study, ReaderParams params, MediaSeriesGroup patient, WadoParameters wadoParameters) {
        MediaSeriesGroup studyNode = ManifestModelBuilder.buildStudy(new JsonAttributeSource(study), params, patient);
        List<JsonObject> series = JsonManifestParser.children(study, TagD.Level.SERIES.getTagName());
        if (ManifestCompletion.isPartialLevel(wadoParameters, !series.isEmpty())) {
            ManifestCompletion.completeStudy(params, wadoParameters, patient, studyNode);
        } else {
            for (JsonObject s : series) {
                JsonManifestParser.readSeries(s, params, patient, studyNode, wadoParameters);
            }
        }
    }

    private static void readSeries(JsonObject series, ReaderParams params, MediaSeriesGroup patient, MediaSeriesGroup study, WadoParameters wadoParameters) {
        DicomSeries dicomSeries = ManifestModelBuilder.buildSeries(new JsonAttributeSource(series), params, study, wadoParameters);
        if (dicomSeries == null) {
            return;
        }
        SeriesInstanceList seriesInstanceList = ManifestModelBuilder.getSeriesInstanceList(dicomSeries);
        for (JsonObject instance : JsonManifestParser.children(series, TagD.Level.INSTANCE.getTagName())) {
            ManifestModelBuilder.addSopInstance(new JsonAttributeSource(instance), seriesInstanceList);
        }
        ManifestModelBuilder.completeSeries(params, patient, study, dicomSeries, seriesInstanceList);
    }

    static List<JsonObject> children(JsonObject parent, String key) {
        JsonValue value = (JsonValue)parent.get((Object)key);
        if (value == null) {
            return List.of();
        }
        return switch (value.getValueType()) {
            case JsonValue.ValueType.OBJECT -> List.of(value.asJsonObject());
            case JsonValue.ValueType.ARRAY -> {
                ArrayList<JsonObject> result = new ArrayList<JsonObject>();
                for (JsonValue item : value.asJsonArray()) {
                    if (item.getValueType() != JsonValue.ValueType.OBJECT) continue;
                    result.add(item.asJsonObject());
                }
                yield result;
            }
            default -> List.of();
        };
    }
}

