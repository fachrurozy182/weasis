/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.tool.common;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

public class FilesetInfo {
    private String uid;
    private String id;
    private Path descriptorFile;
    private String descriptorFileCharset;

    public FilesetInfo(String uid, String id) {
        this.uid = uid;
        this.id = id;
    }

    public FilesetInfo(String uid, String id, Path descriptorFile, String descriptorFileCharset) {
        this.uid = uid;
        this.id = id;
        this.descriptorFile = descriptorFile;
        this.descriptorFileCharset = descriptorFileCharset;
    }

    public String getFilesetUID() {
        return this.uid;
    }

    public FilesetInfo setFilesetUID(String uid) {
        this.uid = uid;
        return this;
    }

    public String getFilesetID() {
        return this.id;
    }

    public FilesetInfo setFilesetID(String id) {
        this.id = id;
        return this;
    }

    public Optional<Path> getDescriptorFile() {
        return Optional.ofNullable(this.descriptorFile);
    }

    public FilesetInfo setDescriptorFile(Path descriptorFile) {
        this.descriptorFile = descriptorFile;
        return this;
    }

    public Optional<String> getDescriptorFileCharset() {
        return Optional.ofNullable(this.descriptorFileCharset);
    }

    public FilesetInfo setDescriptorFileCharset(String descriptorFileCharset) {
        this.descriptorFileCharset = descriptorFileCharset;
        return this;
    }

    public boolean isComplete() {
        return this.uid != null && !this.uid.trim().isEmpty() && this.id != null && !this.id.trim().isEmpty() && this.descriptorFile != null;
    }

    public boolean hasValidIdentifiers() {
        return this.uid != null && !this.uid.trim().isEmpty() && this.id != null && !this.id.trim().isEmpty();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        FilesetInfo other = (FilesetInfo)obj;
        return Objects.equals(this.uid, other.uid) && Objects.equals(this.id, other.id) && Objects.equals(this.descriptorFile, other.descriptorFile) && Objects.equals(this.descriptorFileCharset, other.descriptorFileCharset);
    }

    public int hashCode() {
        return Objects.hash(this.uid, this.id, this.descriptorFile, this.descriptorFileCharset);
    }

    public String toString() {
        return "FilesetInfo{uid='" + this.uid + "', id='" + this.id + "', descriptorFile=" + String.valueOf(this.descriptorFile) + ", descriptorFileCharset='" + this.descriptorFileCharset + "'}";
    }
}

