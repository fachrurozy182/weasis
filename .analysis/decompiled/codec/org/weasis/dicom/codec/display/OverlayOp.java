/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.api.gui.util.ActionW
 *  org.weasis.core.api.gui.util.GuiUtils
 *  org.weasis.core.api.image.AbstractOp
 *  org.weasis.core.api.image.ImageOpEvent
 *  org.weasis.core.api.image.ImageOpEvent$OpEvent
 *  org.weasis.core.api.media.data.ImageElement
 *  org.weasis.core.api.media.data.MediaElement
 *  org.weasis.core.api.media.data.MediaReader
 *  org.weasis.opencv.data.PlanarImage
 */
package org.weasis.dicom.codec.display;

import java.awt.Color;
import java.util.Map;
import java.util.Optional;
import org.dcm4che3.img.DicomImageReadParam;
import org.dcm4che3.img.DicomMetaData;
import org.dcm4che3.img.data.OverlayData;
import org.dcm4che3.img.data.PrDicomObject;
import org.dcm4che3.img.stream.ImageDescriptor;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.image.AbstractOp;
import org.weasis.core.api.image.ImageOpEvent;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.api.media.data.MediaReader;
import org.weasis.dicom.codec.DicomMediaIO;
import org.weasis.dicom.codec.PRSpecialElement;
import org.weasis.opencv.data.PlanarImage;

public class OverlayOp
extends AbstractOp {
    public static final String OP_NAME = ActionW.IMAGE_OVERLAY.getTitle();
    public static final String P_SHOW = "overlay";
    public static final String P_IMAGE_ELEMENT = "img.element";
    public static final String OVERLAY_COLOR_KEY = "overlay.color";

    public OverlayOp() {
        this.setName(OP_NAME);
    }

    public OverlayOp(OverlayOp op) {
        super((AbstractOp)op);
    }

    public OverlayOp copy() {
        return new OverlayOp(this);
    }

    public void handleImageOpEvent(ImageOpEvent event) {
        Map p;
        ImageOpEvent.OpEvent type = event.eventType();
        if (ImageOpEvent.OpEvent.IMAGE_CHANGE.equals((Object)type) || ImageOpEvent.OpEvent.RESET_DISPLAY.equals((Object)type)) {
            this.setParam("pr.element", null);
            this.setParam(P_IMAGE_ELEMENT, event.image());
        } else if (ImageOpEvent.OpEvent.APPLY_PR.equals((Object)type) && (p = event.params()) != null) {
            PRSpecialElement pr = Optional.ofNullable(p.get(ActionW.PR_STATE.cmd())).filter(PRSpecialElement.class::isInstance).map(PRSpecialElement.class::cast).orElse(null);
            this.setParam("pr.element", pr == null ? null : pr.getPrDicomObject());
            this.setParam(P_IMAGE_ELEMENT, event.image());
        }
    }

    public void process() throws Exception {
        DicomMediaIO reader;
        DicomMetaData md;
        MediaReader mediaReader;
        ImageElement image;
        PlanarImage source;
        PlanarImage result = source = this.getSourceImage();
        Boolean overlay = (Boolean)this.params.get(P_SHOW);
        if (overlay != null && overlay.booleanValue() && (image = (ImageElement)this.params.get(P_IMAGE_ELEMENT)) != null && (mediaReader = image.getMediaReader()) instanceof DicomMediaIO && (md = (reader = (DicomMediaIO)mediaReader).getDicomMetaData()) != null) {
            ImageDescriptor desc = md.getImageDescriptor();
            Object object = image.getKey();
            if (object instanceof Integer) {
                Integer frame = (Integer)object;
                DicomImageReadParam p = new DicomImageReadParam();
                p.setPresentationState((PrDicomObject)this.params.get("pr.element"));
                PlanarImage original = source;
                if (!desc.getEmbeddedOverlay().isEmpty()) {
                    original = reader.getImageFragment((MediaElement)image, (Integer)image.getKey(), false);
                }
                p.setOverlayColor(GuiUtils.getUICore().getSystemPreferences().getColorProperty(OVERLAY_COLOR_KEY, Color.WHITE));
                result = OverlayData.getOverlayImage(original, source, desc, p, frame);
            }
        }
        this.params.put("op.output.img", result);
    }
}

