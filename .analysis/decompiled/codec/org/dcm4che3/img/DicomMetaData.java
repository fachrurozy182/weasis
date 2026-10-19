/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.img;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import javax.imageio.metadata.IIOMetadata;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.img.stream.ImageDescriptor;
import org.dcm4che3.io.DicomInputStream;
import org.w3c.dom.Node;

public final class DicomMetaData
extends IIOMetadata {
    private static final String VIDEO_TRANSFER_SYNTAX_PREFIX = "1.2.840.10008.1.2.4.10";
    private final Attributes fileMetaInformation;
    private final Attributes dcm;
    private final ImageDescriptor desc;
    private final String transferSyntaxUID;

    public DicomMetaData(DicomInputStream dcmStream) throws IOException {
        Objects.requireNonNull(dcmStream, "DICOM input stream cannot be null");
        this.fileMetaInformation = dcmStream.readFileMetaInformation();
        this.dcm = dcmStream.readDataset();
        this.desc = new ImageDescriptor(this.dcm);
        this.transferSyntaxUID = this.resolveTransferSyntax(dcmStream);
    }

    public DicomMetaData(Attributes dcm, String transferSyntaxUID) {
        this.fileMetaInformation = null;
        this.dcm = Objects.requireNonNull(dcm, "DICOM attributes cannot be null");
        this.desc = new ImageDescriptor(dcm);
        this.transferSyntaxUID = Objects.requireNonNull(transferSyntaxUID, "Transfer syntax UID cannot be null");
    }

    private String resolveTransferSyntax(DicomInputStream dcmStream) {
        return Optional.ofNullable(this.fileMetaInformation).map(fmi -> fmi.getString(131088, dcmStream.getTransferSyntax())).orElseGet(dcmStream::getTransferSyntax);
    }

    public Attributes getFileMetaInformation() {
        return this.fileMetaInformation;
    }

    public Attributes getDicomObject() {
        return this.dcm;
    }

    public ImageDescriptor getImageDescriptor() {
        return this.desc;
    }

    public String getTransferSyntaxUID() {
        return this.transferSyntaxUID;
    }

    public Optional<String> getMediaStorageSOPClassUID() {
        return Optional.ofNullable(this.fileMetaInformation).map(fmi -> fmi.getString(131074));
    }

    public boolean isVideoTransferSyntaxUID() {
        return this.transferSyntaxUID != null && this.transferSyntaxUID.startsWith(VIDEO_TRANSFER_SYNTAX_PREFIX);
    }

    public boolean isMediaStorageDirectory() {
        return this.getMediaStorageSOPClassUID().filter("1.2.840.10008.1.3.10"::equals).isPresent();
    }

    public boolean isSegmentationStorage() {
        return this.getMediaStorageSOPClassUID().filter("1.2.840.10008.5.1.4.1.1.66.4"::equals).isPresent();
    }

    public boolean hasFileMetaInformation() {
        return this.fileMetaInformation != null;
    }

    public int getNumberOfFrames() {
        return this.desc.getFrames();
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public Node getAsTree(String formatName) {
        throw new UnsupportedOperationException("DICOM metadata does not support tree representation");
    }

    @Override
    public void mergeTree(String formatName, Node root) {
        throw new UnsupportedOperationException("DICOM metadata is read-only");
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException("DICOM metadata is read-only");
    }

    public String toString() {
        return "DicomMetaData{transferSyntax='%s', sopClass='%s', frames=%d, hasFileMetaInfo=%s}".formatted(new Object[]{this.transferSyntaxUID, this.getMediaStorageSOPClassUID().orElse("N/A"), this.getNumberOfFrames(), this.hasFileMetaInformation()});
    }
}

