/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.scribejava.core.httpclient.multipart.BodyPartPayload
 *  com.github.scribejava.core.httpclient.multipart.MultipartPayload
 *  com.github.scribejava.core.model.OAuthRequest
 *  com.github.scribejava.core.model.Verb
 *  org.dcm4che3.data.Attributes
 *  org.dcm4che3.data.Sequence
 *  org.dcm4che3.data.VR
 *  org.dcm4che3.io.SAXReader
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.net.HttpStream
 *  org.weasis.core.api.net.HttpUtils
 *  org.weasis.core.api.net.URLParameters
 *  org.weasis.core.api.net.auth.AuthMethod
 *  org.weasis.core.api.net.auth.BodySupplier
 *  org.weasis.core.api.net.auth.FileBodyPartPayload
 *  org.weasis.core.util.FileUtil
 *  org.weasis.dicom.param.DicomProgress
 *  org.weasis.dicom.param.DicomState
 *  org.weasis.dicom.web.ContentType
 *  org.weasis.dicom.web.DicomStowRS
 *  org.weasis.dicom.web.MultipartConstants$DicomContentType
 */
package org.weasis.dicom.send;

import com.github.scribejava.core.httpclient.multipart.BodyPartPayload;
import com.github.scribejava.core.httpclient.multipart.MultipartPayload;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.xml.parsers.ParserConfigurationException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.net.HttpStream;
import org.weasis.core.api.net.HttpUtils;
import org.weasis.core.api.net.URLParameters;
import org.weasis.core.api.net.auth.AuthMethod;
import org.weasis.core.api.net.auth.BodySupplier;
import org.weasis.core.api.net.auth.FileBodyPartPayload;
import org.weasis.core.util.FileUtil;
import org.weasis.dicom.param.DicomProgress;
import org.weasis.dicom.param.DicomState;
import org.weasis.dicom.send.HttpServerErrorException;
import org.weasis.dicom.web.ContentType;
import org.weasis.dicom.web.DicomStowRS;
import org.weasis.dicom.web.MultipartConstants;
import org.xml.sax.SAXException;

public class StowRS
extends DicomStowRS {
    private static final Logger LOGGER = LoggerFactory.getLogger(StowRS.class);
    private static final int MAX_BODY_LOG = 2048;
    private static final int MAX_BODY_DIALOG = 200;

    public StowRS(String requestURL, ContentType contentType, String agentName, Map<String, String> headers) {
        super(requestURL, contentType, agentName, headers);
    }

    private OAuthRequest prepareAuthConnection(List<String> filesOrFolders, boolean recursive) {
        OAuthRequest authRequest = new OAuthRequest(Verb.POST, this.getRequestURL());
        MultipartPayload multipart = this.getMultipartPayload(filesOrFolders, recursive);
        authRequest.setMultipartPayload(multipart);
        return authRequest;
    }

    private MultipartPayload getMultipartPayload(List<String> filesOrFolders, boolean recursive) {
        HashMap<String, Object> headers = new HashMap<String, Object>();
        headers.put("Content-Type", "multipart/related;type=\"" + ContentType.APPLICATION_DICOM.getType() + "\";boundary=weasisDicomBoundary");
        headers.put("Accept", MultipartConstants.DicomContentType.XML.toString());
        MultipartPayload multipart = new MultipartPayload("weasisDicomBoundary", headers);
        for (String entry : filesOrFolders) {
            File file = new File(entry);
            if (file.isDirectory()) {
                ArrayList fileList = new ArrayList();
                FileUtil.getAllFilesInDirectory((Path)file.toPath(), fileList, (boolean)recursive);
                for (Path f : fileList) {
                    this.addMultipartFile(multipart, f.toFile());
                }
                continue;
            }
            this.addMultipartFile(multipart, file);
        }
        return multipart;
    }

    private void addMultipartFile(MultipartPayload multipart, final File file) {
        multipart.addBodyPart((BodyPartPayload)new FileBodyPartPayload(this.getContentType().getType(), (BodySupplier)new BodySupplier<InputStream>(this){
            {
                Objects.requireNonNull(this$0);
            }

            public InputStream get() throws IOException {
                return new FileInputStream(file);
            }

            public long length() {
                return file.length();
            }
        }, null));
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public DicomState uploadDicom(List<String> filesOrFolders, boolean recursive, AuthMethod authMethod) {
        DicomState state = new DicomState(new DicomProgress());
        String url = this.getRequestURL();
        OAuthRequest authRequest = this.prepareAuthConnection(filesOrFolders, recursive);
        int nbFile = authRequest.getMultipartPayload().getBodyParts().size();
        try (HttpStream httpCon = HttpUtils.getHttpResponse((String)url, (URLParameters)new URLParameters(this.getHeaders(), true), (AuthMethod)authMethod, (OAuthRequest)authRequest);){
            int code = httpCon.getResponseCode();
            if (code >= 200 && code < 400) {
                DicomState dicomState2 = this.buildErrorMessage(this.getResponseOutput(httpCon), state, nbFile);
                return dicomState2;
            }
            if (code == 401 && authMethod != null) {
                authMethod.resetToken();
            }
            DicomState dicomState = StowRS.buildHttpFailure(state, httpCon, code);
            return dicomState;
        }
        catch (Exception e) {
            LOGGER.error("STOW-RS: error when posting data", (Throwable)e);
            return DicomState.buildMessage((DicomState)state, (String)e.getMessage(), null);
        }
    }

    private static DicomState buildHttpFailure(DicomState state, HttpStream httpCon, int code) {
        String reason = httpCon.getResponseMessage();
        String body = StowRS.readBodyQuietly(httpCon);
        String header = String.format("STOW-RS server response message: HTTP Status-Code %d: %s", code, reason == null ? "" : reason);
        if (body != null && !body.isBlank()) {
            LOGGER.error("{} - body: {}", (Object)header, (Object)body);
        } else {
            LOGGER.error(header);
        }
        state.setStatus(49152);
        String dialogMessage = body != null && !body.isBlank() ? header + " - " + StowRS.truncate(body, 200) : header;
        return DicomState.buildMessage((DicomState)state, null, (Exception)new HttpServerErrorException(dialogMessage));
    }

    private static String truncate(String s, int max) {
        String stripped = s.replaceAll("\\s+", " ").strip();
        return stripped.length() <= max ? stripped : stripped.substring(0, max) + "...";
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static String readBodyQuietly(HttpStream httpCon) {
        try (InputStream in = httpCon.getInputStream();){
            if (in == null) {
                String string2 = null;
                return string2;
            }
            byte[] bytes = in.readNBytes(2048);
            String string = bytes.length == 0 ? null : new String(bytes, StandardCharsets.UTF_8);
            return string;
        }
        catch (IOException e) {
            LOGGER.debug("Cannot read STOW-RS error response body", (Throwable)e);
            return null;
        }
    }

    private DicomState buildErrorMessage(Attributes error, DicomState state, int nbFile) {
        String message;
        if (error == null) {
            state.setStatus(0);
            message = "all the files has been transferred";
        } else {
            Sequence seq;
            message = "one or more files has not been transferred";
            state.setStatus(45056);
            DicomProgress p = state.getProgress();
            if (p != null && (seq = error.getSequence(528792)) != null && !seq.isEmpty()) {
                Attributes cmd = Optional.ofNullable(p.getAttributes()).orElseGet(Attributes::new);
                cmd.setInt(2304, VR.US, new int[]{45056});
                cmd.setInt(4129, VR.US, new int[]{nbFile});
                cmd.setInt(4130, VR.US, new int[]{seq.size()});
                cmd.setInt(4131, VR.US, new int[]{0});
                cmd.setInt(4128, VR.US, new int[]{0});
                p.setAttributes(cmd);
                message = seq.stream().map(s -> s.getString(528725, "Unknown SopUID") + " -> " + s.getString(528791)).collect(Collectors.joining(", "));
                LOGGER.error("STOW-RS error: {}", (Object)message);
                return DicomState.buildMessage((DicomState)state, null, (Exception)new RuntimeException("Failed instances: " + message));
            }
            LOGGER.error("STOW-RS error: {}", (Object)message);
        }
        return DicomState.buildMessage((DicomState)state, (String)message, null);
    }

    private Attributes getResponseOutput(HttpStream httpPost) throws IOException, ParserConfigurationException, SAXException {
        int code = httpPost.getResponseCode();
        if (code != 200) {
            if (code == 202 || code == 409) {
                LOGGER.warn("STOW-RS server response message: HTTP Status-Code {}: {}", (Object)code, (Object)httpPost.getResponseMessage());
                return SAXReader.parse((InputStream)httpPost.getInputStream());
            }
            throw new HttpServerErrorException(String.format("STOW-RS server response message: HTTP Status-Code %d: %s", code, httpPost.getResponseMessage()));
        }
        LOGGER.info("STOW-RS server response message: HTTP Status-Code 200: OK for all the image set");
        return null;
    }
}
