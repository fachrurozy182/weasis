/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.img.stream;

import java.util.Optional;
import org.dcm4che3.img.stream.ImageDescriptor;

public interface ImageReaderDescriptor {
    public ImageDescriptor getImageDescriptor();

    default public Optional<ImageDescriptor> getImageDescriptorOptional() {
        return Optional.ofNullable(this.getImageDescriptor());
    }

    default public boolean hasImageDescriptor() {
        return this.getImageDescriptor() != null;
    }
}

