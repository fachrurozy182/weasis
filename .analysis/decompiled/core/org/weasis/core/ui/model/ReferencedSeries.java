/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.xml.bind.annotation.XmlElement
 */
package org.weasis.core.ui.model;

import jakarta.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.weasis.core.ui.model.ReferencedImage;
import org.weasis.core.ui.model.utils.imp.DefaultUUID;

public class ReferencedSeries
extends DefaultUUID {
    private List<ReferencedImage> images;

    public ReferencedSeries() {
        this(null, null);
    }

    public ReferencedSeries(String uuid) {
        this(uuid, null);
    }

    public ReferencedSeries(String uuid, List<ReferencedImage> images) {
        super(uuid);
        this.setImages(images);
    }

    @XmlElement(name="image")
    public List<ReferencedImage> getImages() {
        return this.images;
    }

    public void setImages(List<ReferencedImage> images) {
        this.images = Optional.ofNullable(images).orElseGet(ArrayList::new);
    }
}

