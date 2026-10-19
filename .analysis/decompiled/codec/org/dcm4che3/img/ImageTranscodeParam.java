/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.LangUtil
 */
package org.dcm4che3.img;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import org.dcm4che3.img.DicomImageReadParam;
import org.dcm4che3.img.Transcoder;
import org.weasis.core.util.LangUtil;

public final class ImageTranscodeParam {
    private static final int MIN_JPEG_QUALITY = 1;
    private static final int MAX_JPEG_QUALITY = 100;
    private static final Transcoder.Format DEFAULT_FORMAT = Transcoder.Format.JPEG;
    private final DicomImageReadParam readParam;
    private final Transcoder.Format format;
    private Integer jpegCompressionQuality;
    private Boolean preserveRawImage;

    public ImageTranscodeParam(Transcoder.Format format) {
        this(null, format);
    }

    public ImageTranscodeParam(DicomImageReadParam readParam, Transcoder.Format format) {
        this.readParam = Objects.requireNonNullElseGet(readParam, DicomImageReadParam::new);
        this.format = Objects.requireNonNullElse(format, DEFAULT_FORMAT);
    }

    public DicomImageReadParam getReadParam() {
        return this.readParam;
    }

    public OptionalInt getJpegCompressionQuality() {
        return LangUtil.toOptional((Integer)this.jpegCompressionQuality);
    }

    public void setJpegCompressionQuality(int jpegCompressionQuality) {
        if (jpegCompressionQuality < 1 || jpegCompressionQuality > 100) {
            throw new IllegalArgumentException("JPEG quality must be between %d and %d, got: %d".formatted(new Object[]{1, 100, jpegCompressionQuality}));
        }
        this.jpegCompressionQuality = jpegCompressionQuality;
    }

    public Optional<Boolean> isPreserveRawImage() {
        return Optional.ofNullable(this.preserveRawImage);
    }

    public void setPreserveRawImage(Boolean preserveRawImage) {
        this.preserveRawImage = preserveRawImage;
    }

    public Transcoder.Format getFormat() {
        return this.format;
    }

    public ImageTranscodeParam copy() {
        ImageTranscodeParam copy = new ImageTranscodeParam(this.readParam, this.format);
        copy.jpegCompressionQuality = this.jpegCompressionQuality;
        copy.preserveRawImage = this.preserveRawImage;
        return copy;
    }

    public String toString() {
        return "ImageTranscodeParam{format=" + String.valueOf((Object)this.format) + ", jpegQuality=" + String.valueOf(this.jpegCompressionQuality != null ? this.jpegCompressionQuality : "default") + ", preserveRaw=" + String.valueOf(this.preserveRawImage != null ? this.preserveRawImage : "default") + "}";
    }
}

