/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.annotations.Generated
 *  org.weasis.opencv.data.PlanarImage
 */
package org.weasis.dicom.web;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.img.DicomImageReadParam;
import org.dcm4che3.img.DicomImageReader;
import org.dcm4che3.img.DicomJpegWriteParam;
import org.dcm4che3.img.DicomOutputData;
import org.dcm4che3.img.Transcoder;
import org.dcm4che3.img.stream.BytesWithImageDescriptor;
import org.dcm4che3.img.stream.ImageAdapter;
import org.dcm4che3.img.util.Editable;
import org.dcm4che3.img.util.SupplierEx;
import org.dcm4che3.io.DicomOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.util.annotations.Generated;
import org.weasis.dicom.web.ContentType;
import org.weasis.dicom.web.DicomStowConfig;
import org.weasis.dicom.web.HttpException;
import org.weasis.dicom.web.MultipartBody;
import org.weasis.dicom.web.MultipartConstants;
import org.weasis.dicom.web.Payload;
import org.weasis.opencv.data.PlanarImage;

public class DicomStowRS
implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(DicomStowRS.class);
    public static final String DEFAULT_BOUNDARY = "weasisDicomBoundary";
    private final DicomStowConfig config;
    private final HttpClient httpClient;
    private final ExecutorService executorService;

    public DicomStowRS(DicomStowConfig config) {
        this.config = Objects.requireNonNull(config, "Configuration cannot be null");
        this.executorService = this.createExecutorService();
        this.httpClient = this.createHttpClient();
    }

    public DicomStowRS(String requestURL, ContentType contentType, String agentName, Map<String, String> headers) {
        this(requestURL, contentType, agentName, headers, DicomStowConfig.DEFAULT_HTTP_VERSION);
    }

    public DicomStowRS(String requestURL, ContentType contentType, String agentName, Map<String, String> headers, HttpClient.Version httpVersion) {
        this(DicomStowConfig.builder().requestUrl(requestURL).contentType(contentType).userAgent(agentName).headers(headers).httpVersion(httpVersion).build());
    }

    public void uploadDicom(Path path) throws Exception {
        Objects.requireNonNull(path, "Path cannot be null");
        if (!Files.exists(path, new LinkOption[0])) {
            throw new IllegalArgumentException("File does not exist: " + String.valueOf(path));
        }
        this.uploadPayload(Payload.ofPath(path));
    }

    public void uploadDicom(InputStream inputStream, Attributes fileMetaInfo) throws Exception {
        Objects.requireNonNull(inputStream, "Input stream cannot be null");
        Objects.requireNonNull(fileMetaInfo, "File meta information cannot be null");
        Payload payload = this.createStreamPayload(inputStream, fileMetaInfo);
        this.uploadPayload(payload);
    }

    public void uploadDicom(Attributes metadata, String transferSyntaxUid) throws Exception {
        Objects.requireNonNull(metadata, "Metadata cannot be null");
        Objects.requireNonNull(transferSyntaxUid, "Transfer syntax UID cannot be null");
        Payload payload = this.createMetadataPayload(metadata, transferSyntaxUid);
        this.uploadPayload(payload);
    }

    public void uploadPayload(Payload payload) throws Exception {
        Objects.requireNonNull(payload, "Payload cannot be null");
        MultipartBody multipartBody = new MultipartBody(this.config.getContentType(), DEFAULT_BOUNDARY);
        multipartBody.addPart(this.config.getContentType().getType(), payload, null);
        HttpRequest request = this.buildHttpRequest(multipartBody);
        HttpResponse<String> response = this.sendRequest(request);
        this.logResponse(response);
    }

    @Override
    public void close() throws Exception {
        if (this.executorService != null && !this.executorService.isShutdown()) {
            this.executorService.shutdown();
        }
    }

    public ContentType getContentType() {
        return this.config.getContentType();
    }

    public String getRequestURL() {
        return this.config.getRequestUrl();
    }

    public Map<String, String> getHeaders() {
        return this.config.getHeaders();
    }

    public static Payload createCompressedImagePayload(Attributes data, ImageAdapter.AdaptTransferSyntax syntax, BytesWithImageDescriptor desc, Editable<PlanarImage> editable) throws IOException {
        return new CompressedImagePayload(data, syntax, desc, editable);
    }

    private ExecutorService createExecutorService() {
        return Executors.newFixedThreadPool(this.config.getThreadPoolSize(), r -> {
            Thread thread = new Thread(r, "DicomStow-" + System.currentTimeMillis());
            thread.setDaemon(true);
            return thread;
        });
    }

    private HttpClient createHttpClient() {
        return HttpClient.newBuilder().executor(this.executorService).followRedirects(HttpClient.Redirect.NORMAL).version(this.config.getHttpVersion()).connectTimeout(this.config.getConnectTimeout()).build();
    }

    private HttpRequest buildHttpRequest(MultipartBody multipartBody) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(this.config.getRequestUrl())).POST(multipartBody.createBodyPublisher()).header("Content-Type", multipartBody.getContentTypeHeader()).header("Accept", MultipartConstants.DicomContentType.XML.getMimeType()).header("User-Agent", this.config.getUserAgent());
        this.config.getHeaders().forEach(builder::header);
        HttpRequest request = builder.build();
        if (LOGGER.isDebugEnabled()) {
            this.logRequest(request, multipartBody);
        }
        return request;
    }

    HttpResponse<String> sendRequest(HttpRequest request) throws Exception {
        HttpResponse<String> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        int statusCode = response.statusCode();
        if (statusCode >= 400) {
            throw HttpException.fromResponse(response);
        }
        return response;
    }

    @Generated
    private void logRequest(HttpRequest request, MultipartBody multipartBody) {
        URI uri = request.uri();
        LOGGER.debug("> POST {} {}", (Object)uri.getPath(), (Object)request.version().orElse(HttpClient.Version.HTTP_1_1));
        LOGGER.debug("> Host: {}:{}", (Object)uri.getHost(), (Object)uri.getPort());
        DicomStowRS.logHeaders("> ", request.headers());
        multipartBody.logDebugInfo();
    }

    @Generated
    private void logResponse(HttpResponse<String> response) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("< {} response code: {}", (Object)response.version(), (Object)response.statusCode());
            DicomStowRS.logHeaders("< ", response.headers());
        }
        response.body().lines().forEach(arg_0 -> ((Logger)LOGGER).info(arg_0));
    }

    @Generated
    private static void logHeaders(String prefix, HttpHeaders headers) {
        headers.map().forEach((name, values) -> values.forEach(value -> LOGGER.debug("{}{}: {}", new Object[]{prefix, name, value})));
        LOGGER.debug(prefix.trim());
    }

    private Payload createStreamPayload(final InputStream inputStream, final Attributes fileMetaInfo) {
        return new Payload(){

            @Override
            public long size() {
                return -1L;
            }

            @Override
            public InputStream newInputStream() {
                try {
                    ByteArrayOutputStream metaOutput = new ByteArrayOutputStream();
                    try (DicomOutputStream dicomOut = new DicomOutputStream(metaOutput, fileMetaInfo.getString(131088));){
                        dicomOut.writeFileMetaInformation(fileMetaInfo);
                    }
                    ArrayList<InputStream> streams = new ArrayList<InputStream>();
                    streams.add(new ByteArrayInputStream(metaOutput.toByteArray()));
                    streams.add(inputStream);
                    return new SequenceInputStream(Collections.enumeration(streams));
                }
                catch (IOException e) {
                    LOGGER.error("Failed to create stream payload", (Throwable)e);
                    return InputStream.nullInputStream();
                }
            }
        };
    }

    private Payload createMetadataPayload(final Attributes metadata, final String transferSyntaxUid) {
        return new Payload(){

            @Override
            public long size() {
                return -1L;
            }

            @Override
            public InputStream newInputStream() {
                try {
                    Attributes fileMetaInfo = metadata.createFileMetaInformation(transferSyntaxUid);
                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                    try (DicomOutputStream dicomOut = new DicomOutputStream(output, transferSyntaxUid);){
                        dicomOut.writeDataset(fileMetaInfo, metadata);
                    }
                    return new ByteArrayInputStream(output.toByteArray());
                }
                catch (IOException e) {
                    LOGGER.error("Failed to create metadata payload", (Throwable)e);
                    return InputStream.nullInputStream();
                }
            }
        };
    }

    private record CompressedImagePayload(Attributes data, ImageAdapter.AdaptTransferSyntax syntax, BytesWithImageDescriptor descriptor, Editable<PlanarImage> editable) implements Payload
    {
        private CompressedImagePayload(Attributes data, ImageAdapter.AdaptTransferSyntax syntax, BytesWithImageDescriptor descriptor, Editable<PlanarImage> editable) {
            this.data = Objects.requireNonNull(data);
            this.syntax = Objects.requireNonNull(syntax);
            this.descriptor = Objects.requireNonNull(descriptor);
            this.editable = Objects.requireNonNull(editable);
        }

        @Override
        public long size() {
            return -1L;
        }

        @Override
        public InputStream newInputStream() {
            try {
                return this.createCompressedStream();
            }
            catch (IOException e) {
                LOGGER.error("Failed to create compressed image stream", (Throwable)e);
                return InputStream.nullInputStream();
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private InputStream createCompressedStream() throws IOException {
            DicomImageReader reader = new DicomImageReader(Transcoder.dicomImageReaderSpi);
            try {
                reader.setInput(this.descriptor);
                DicomImageReadParam readParam = new DicomImageReadParam();
                readParam.setReleaseImageAfterProcessing(true);
                List<SupplierEx<PlanarImage, IOException>> images = reader.getLazyPlanarImages(readParam, this.editable);
                DicomOutputData outputData = new DicomOutputData(images, this.descriptor.getImageDescriptor(), this.syntax.getRequested());
                if (!this.syntax.getRequested().equals(outputData.getTsuid())) {
                    this.syntax.setSuitable(outputData.getTsuid());
                    LOGGER.warn("Transcoding to {} not possible, using {} instead", (Object)this.syntax.getRequested(), (Object)this.syntax.getSuitable());
                }
                Attributes dataset = new Attributes(this.data);
                dataset.remove(2145386512);
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                try (DicomOutputStream dicomOut = new DicomOutputStream(output, outputData.getTsuid());){
                    dicomOut.writeFileMetaInformation(dataset.createFileMetaInformation(outputData.getTsuid()));
                    if (DicomOutputData.isNativeSyntax(outputData.getTsuid())) {
                        outputData.writeRawImageData(dicomOut, dataset);
                    } else {
                        int[] writeParams = outputData.adaptTagsToCompressedImage(dataset, outputData.getFirstImage().get(), this.descriptor.getImageDescriptor(), DicomJpegWriteParam.buildDicomImageWriteParam(outputData.getTsuid()));
                        outputData.writeCompressedImageData(dicomOut, dataset, writeParams);
                    }
                }
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(output.toByteArray());
                return byteArrayInputStream;
            }
            finally {
                reader.dispose();
            }
        }
    }
}

