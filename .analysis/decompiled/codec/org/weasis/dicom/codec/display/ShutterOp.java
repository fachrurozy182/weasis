/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.opencv.core.Mat
 *  org.weasis.core.api.gui.util.ActionW
 *  org.weasis.core.api.image.AbstractOp
 *  org.weasis.core.api.image.ImageOpEvent
 *  org.weasis.core.api.image.ImageOpEvent$OpEvent
 *  org.weasis.core.api.media.data.ImageElement
 *  org.weasis.core.api.media.data.TagReadable
 *  org.weasis.core.api.media.data.TagW
 *  org.weasis.core.util.LangUtil
 *  org.weasis.opencv.data.ImageCV
 *  org.weasis.opencv.data.PlanarImage
 *  org.weasis.opencv.op.ImageTransformer
 */
package org.weasis.dicom.codec.display;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.img.data.OverlayData;
import org.dcm4che3.img.data.PrDicomObject;
import org.dcm4che3.img.util.DicomUtils;
import org.opencv.core.Mat;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.image.AbstractOp;
import org.weasis.core.api.image.ImageOpEvent;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.api.media.data.TagReadable;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.util.LangUtil;
import org.weasis.dicom.codec.PRSpecialElement;
import org.weasis.dicom.codec.TagD;
import org.weasis.opencv.data.ImageCV;
import org.weasis.opencv.data.PlanarImage;
import org.weasis.opencv.op.ImageTransformer;

public class ShutterOp
extends AbstractOp {
    public static final String OP_NAME = ActionW.IMAGE_SHUTTER.getTitle();
    public static final String P_SHOW = "show";
    public static final String P_SHAPE = "shape";
    public static final String P_RGB_COLOR = "rgb.color";
    public static final String P_PS_VALUE = "ps.value";
    public static final String P_PR_ELEMENT = "pr.element";
    public static final String P_IMAGE_ELEMENT = "img.element";

    public ShutterOp() {
        this.setName(OP_NAME);
    }

    public ShutterOp(ShutterOp op) {
        super((AbstractOp)op);
    }

    public ShutterOp copy() {
        return new ShutterOp(this);
    }

    public void handleImageOpEvent(ImageOpEvent event) {
        Map p;
        ImageOpEvent.OpEvent type = event.eventType();
        if (ImageOpEvent.OpEvent.IMAGE_CHANGE.equals((Object)type) || ImageOpEvent.OpEvent.RESET_DISPLAY.equals((Object)type)) {
            ImageElement img = event.image();
            boolean noMedia = img == null;
            this.setParam(P_SHAPE, noMedia ? null : img.getTagValue(TagW.ShutterFinalShape));
            this.setParam(P_PS_VALUE, noMedia ? null : img.getTagValue(TagW.ShutterPSValue));
            this.setParam(P_RGB_COLOR, noMedia ? null : img.getTagValue(TagW.ShutterRGBColor));
            this.setParam(P_PR_ELEMENT, null);
            this.setParam(P_IMAGE_ELEMENT, noMedia ? null : img);
        } else if (ImageOpEvent.OpEvent.APPLY_PR.equals((Object)type) && (p = event.params()) != null) {
            Rectangle area;
            PRSpecialElement pr = Optional.ofNullable(p.get(ActionW.PR_STATE.cmd())).filter(PRSpecialElement.class::isInstance).map(PRSpecialElement.class::cast).orElse(null);
            this.setParam(P_SHAPE, pr == null ? null : pr.getTagValue(TagW.ShutterFinalShape));
            this.setParam(P_PS_VALUE, pr == null ? null : pr.getTagValue(TagW.ShutterPSValue));
            this.setParam(P_RGB_COLOR, pr == null ? null : pr.getTagValue(TagW.ShutterRGBColor));
            this.setParam(P_PR_ELEMENT, pr == null ? null : pr.getPrDicomObject());
            this.setParam(P_IMAGE_ELEMENT, event.image());
            Area shape = (Area)this.params.get(P_SHAPE);
            if (shape != null && (area = (Rectangle)p.get(ActionW.CROP.cmd())) != null) {
                Area trArea = new Area(shape);
                trArea.transform(AffineTransform.getTranslateInstance(-area.getX(), -area.getY()));
                this.setParam(P_SHAPE, trArea);
            }
        }
    }

    public void process() throws Exception {
        PlanarImage source;
        PlanarImage result = source = this.getSourceImage();
        boolean shutter = LangUtil.nullToFalse((Boolean)((Boolean)this.params.get(P_SHOW)));
        Area area = (Area)this.params.get(P_SHAPE);
        PrDicomObject pr = (PrDicomObject)this.params.get(P_PR_ELEMENT);
        if (shutter && area != null) {
            result = ImageTransformer.applyShutter((Mat)source.toMat(), (Shape)area, (Color)this.getShutterColor());
        }
        if (shutter && pr != null) {
            Object object;
            ImageCV imgOverlay = null;
            ImageElement image = (ImageElement)this.params.get(P_IMAGE_ELEMENT);
            List<OverlayData> overlays = pr.getShutterOverlays();
            if (!overlays.isEmpty() && image != null && (object = image.getKey()) instanceof Integer) {
                Attributes attributes;
                Integer shutterOverlayGroup;
                Integer frame = (Integer)object;
                Integer height = TagD.getTagValue((TagReadable)image, 2621456, Integer.class);
                Integer width = TagD.getTagValue((TagReadable)image, 2621457, Integer.class);
                if (height != null && width != null && (shutterOverlayGroup = DicomUtils.getIntegerFromDicomElement(attributes = pr.getDicomObject(), 1578531, null)) != null) {
                    PlanarImage overlayImg = OverlayData.getOverlayImage(result, overlays, frame);
                    imgOverlay = ImageTransformer.overlay((Mat)result.toMat(), (Mat)overlayImg.toMat(), (Color)this.getShutterColor());
                }
            }
            result = imgOverlay == null ? result : imgOverlay;
        }
        this.params.put("op.output.img", result);
    }

    private Color getShutterColor() {
        Color color = (Color)this.params.get(P_RGB_COLOR);
        if (color == null) {
            Integer val = (Integer)this.params.get(P_PS_VALUE);
            return val == null ? Color.BLACK : new Color(val >> 8, val >> 8, val >> 8);
        }
        return color;
    }
}

