/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.LangUtil
 *  org.weasis.opencv.data.PlanarImage
 *  org.weasis.opencv.op.lut.DefaultWlPresentation
 *  org.weasis.opencv.op.lut.PresentationStateLut
 *  org.weasis.opencv.op.lut.WlPresentation
 */
package org.weasis.core.api.image;

import org.weasis.core.Messages;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.image.AbstractOp;
import org.weasis.core.api.image.ImageOpEvent;
import org.weasis.core.api.image.util.WindLevelParameters;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.util.LangUtil;
import org.weasis.opencv.data.PlanarImage;
import org.weasis.opencv.op.lut.DefaultWlPresentation;
import org.weasis.opencv.op.lut.PresentationStateLut;
import org.weasis.opencv.op.lut.WlPresentation;

public class WindowOp
extends AbstractOp {
    public static final String OP_NAME = Messages.getString("WindowLevelOperation.title");
    public static final String P_IMAGE_ELEMENT = "img.element";
    public static final String P_FILL_OUTSIDE_LUT = "fill.outside.lut";
    public static final String P_APPLY_WL_COLOR = "weasis.color.wl.apply";
    public static final String P_INVERSE_LEVEL = "weasis.level.inverse";
    private static final String P_PR_ELEMENT = "pr.element";

    public WindowOp() {
        this.setName(OP_NAME);
    }

    public WindowOp(WindowOp op) {
        super(op);
    }

    @Override
    public WindowOp copy() {
        return new WindowOp(this);
    }

    @Override
    public void handleImageOpEvent(ImageOpEvent event) {
        switch (event.eventType()) {
            case IMAGE_CHANGE: {
                this.setParam(P_IMAGE_ELEMENT, event.image());
                break;
            }
            case RESET_DISPLAY: 
            case SERIES_CHANGE: {
                this.handleDisplayReset(event.image());
                break;
            }
        }
    }

    @Override
    public void process() throws Exception {
        PlanarImage source = this.getSourceImage();
        ImageElement imageElement = this.getImageElement();
        PlanarImage result = imageElement != null ? imageElement.getRenderedImage(source, this.params) : source;
        this.params.put("op.output.img", result);
    }

    private void handleDisplayReset(ImageElement img) {
        this.setParam(P_IMAGE_ELEMENT, img);
        if (img == null) {
            return;
        }
        this.ensureImageLoaded(img);
        WlPresentation wlp = this.getWlPresentation();
        this.setParam(ActionW.WINDOW.cmd(), img.getDefaultWindow(wlp));
        this.setParam(ActionW.LEVEL.cmd(), img.getDefaultLevel(wlp));
        this.setParam(ActionW.LEVEL_MIN.cmd(), img.getMinValue(wlp));
        this.setParam(ActionW.LEVEL_MAX.cmd(), img.getMaxValue(wlp));
    }

    private void ensureImageLoaded(ImageElement img) {
        if (!img.isImageAvailable()) {
            img.getImage();
        }
    }

    private ImageElement getImageElement() {
        return (ImageElement)this.params.get(P_IMAGE_ELEMENT);
    }

    public WlPresentation getWlPresentation() {
        boolean pixelPadding = LangUtil.nullToTrue((Boolean)((Boolean)this.getParam(ActionW.IMAGE_PIX_PADDING.cmd())));
        PresentationStateLut pr = (PresentationStateLut)this.getParam(P_PR_ELEMENT);
        return new DefaultWlPresentation(pr, pixelPadding);
    }

    public WindLevelParameters getWindLevelParameters() {
        ImageElement imageElement = this.getImageElement();
        return imageElement != null ? new WindLevelParameters(imageElement, this.params) : null;
    }
}

