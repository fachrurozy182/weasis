/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.xml.bind.annotation.XmlAttribute
 *  jakarta.xml.bind.annotation.XmlList
 */
package org.weasis.core.ui.model;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlList;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.weasis.core.ui.model.utils.imp.DefaultUUID;

public class ReferencedImage
extends DefaultUUID {
    private List<Integer> frames;

    public ReferencedImage() {
        this(null, null);
    }

    public ReferencedImage(String uuid) {
        this(uuid, null);
    }

    public ReferencedImage(String uuid, List<Integer> frames) {
        super(uuid);
        this.setFrames(frames);
    }

    @XmlList
    @XmlAttribute(name="frames")
    public List<Integer> getFrames() {
        return this.frames;
    }

    public void setFrames(List<Integer> frames) {
        this.frames = Optional.ofNullable(frames).orElseGet(ArrayList::new);
    }
}

