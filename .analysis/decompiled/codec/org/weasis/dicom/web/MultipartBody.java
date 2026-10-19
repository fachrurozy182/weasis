/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package org.weasis.dicom.web;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.net.http.HttpRequest;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.dicom.web.ContentType;
import org.weasis.dicom.web.MultipartPart;
import org.weasis.dicom.web.Payload;

public final class MultipartBody
implements Flow.Subscriber<ByteBuffer> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MultipartBody.class);
    private final String boundary;
    private final ContentType contentType;
    private final List<MultipartPart> parts = new ArrayList<MultipartPart>();
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private Flow.Subscription subscription;

    public MultipartBody(ContentType contentType, String boundary) {
        this.contentType = Objects.requireNonNull(contentType, "Content type cannot be null");
        this.boundary = Objects.requireNonNull(boundary, "Boundary cannot be null");
    }

    public MultipartBody addPart(String contentType, byte[] data, String location) {
        Objects.requireNonNull(data, "Data cannot be null");
        return this.addPart(contentType, Payload.ofBytes(data), location);
    }

    public MultipartBody addPart(String contentType, Path path, String location) {
        Objects.requireNonNull(path, "Path cannot be null");
        return this.addPart(contentType, Payload.ofPath(path), location);
    }

    public MultipartBody addPart(String contentType, Payload payload, String location) {
        Objects.requireNonNull(contentType, "Content type cannot be null");
        Objects.requireNonNull(payload, "Payload cannot be null");
        if (this.closed.get()) {
            throw new IllegalStateException("MultipartBody is closed");
        }
        this.parts.add(new MultipartPart(contentType, location, payload));
        return this;
    }

    public HttpRequest.BodyPublisher createBodyPublisher(Supplier<? extends InputStream> streamSupplier) {
        Objects.requireNonNull(streamSupplier, "Stream supplier cannot be null");
        return HttpRequest.BodyPublishers.ofInputStream(streamSupplier);
    }

    public HttpRequest.BodyPublisher createBodyPublisher() {
        return HttpRequest.BodyPublishers.ofInputStream(() -> new SequenceInputStream(this.createStreamEnumeration()));
    }

    public String getContentTypeHeader() {
        return this.contentType.toMultipartContentType(this.boundary);
    }

    public String getBoundary() {
        return this.boundary;
    }

    public List<MultipartPart> getParts() {
        return Collections.unmodifiableList(this.parts);
    }

    public void reset() {
        this.parts.clear();
        this.closed.set(false);
    }

    public void logDebugInfo() {
        if (LOGGER.isDebugEnabled()) {
            this.parts.forEach(part -> part.logDebugInfo(this.boundary));
            LOGGER.debug("> --{}--", (Object)this.boundary);
            LOGGER.debug(">");
        }
    }

    private Enumeration<InputStream> createStreamEnumeration() {
        return new MultipartStreamEnumeration();
    }

    private String createClosingDelimiter() {
        return "\r\n--" + this.boundary + "--";
    }

    private InputStream createPartHeaderStream(MultipartPart part) {
        if (this.closed.get()) {
            return InputStream.nullInputStream();
        }
        return new ByteArrayInputStream(part.generateHeader(this.boundary).getBytes(StandardCharsets.UTF_8));
    }

    private InputStream createClosingStream() {
        if (this.closed.getAndSet(true)) {
            return InputStream.nullInputStream();
        }
        return new ByteArrayInputStream(this.createClosingDelimiter().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = subscription;
        subscription.request(1L);
    }

    @Override
    public void onNext(ByteBuffer item) {
        if (this.subscription != null) {
            this.subscription.request(1L);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        LOGGER.error("Error in multipart body flow", throwable);
    }

    @Override
    public void onComplete() {
        LOGGER.debug("Multipart body flow completed");
        this.closed.set(true);
    }

    private class MultipartStreamEnumeration
    implements Enumeration<InputStream> {
        private final Iterator<MultipartPart> partIterator;
        private MultipartPart currentPart;
        private boolean streamClosed;
        private boolean needsPartData;

        private MultipartStreamEnumeration() {
            this.partIterator = MultipartBody.this.parts.iterator();
            this.streamClosed = false;
            this.needsPartData = false;
        }

        @Override
        public boolean hasMoreElements() {
            return !this.streamClosed;
        }

        @Override
        public InputStream nextElement() {
            if (this.streamClosed) {
                throw new NoSuchElementException("No more streams available");
            }
            if (this.needsPartData) {
                this.needsPartData = false;
                return this.currentPart.newInputStream();
            }
            if (this.partIterator.hasNext()) {
                this.currentPart = this.partIterator.next();
                this.needsPartData = true;
                return MultipartBody.this.createPartHeaderStream(this.currentPart);
            }
            this.streamClosed = true;
            return MultipartBody.this.createClosingStream();
        }
    }
}

