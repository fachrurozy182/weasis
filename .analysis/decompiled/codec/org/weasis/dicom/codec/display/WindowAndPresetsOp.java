/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.api.gui.util.ActionW
 *  org.weasis.core.api.image.ImageOpEvent
 *  org.weasis.core.api.image.ImageOpEvent$OpEvent
 *  org.weasis.core.api.image.WindowOp
 *  org.weasis.core.api.media.data.ImageElement
 *  org.weasis.core.util.LangUtil
 *  org.weasis.opencv.data.LookupTableCV
 *  org.weasis.opencv.data.PlanarImage
 *  org.weasis.opencv.op.lut.DefaultWlPresentation
 *  org.weasis.opencv.op.lut.PresentationStateLut
 *  org.weasis.opencv.op.lut.WlPresentation
 */
package org.weasis.dicom.codec.display;

import java.util.Map;
import java.util.Optional;
import org.dcm4che3.img.data.PrDicomObject;
import org.dcm4che3.img.lut.PresetWindowLevel;
import org.dcm4che3.img.util.PaletteColorUtils;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.image.ImageOpEvent;
import org.weasis.core.api.image.WindowOp;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.util.LangUtil;
import org.weasis.dicom.codec.DicomImageElement;
import org.weasis.dicom.codec.PRSpecialElement;
import org.weasis.opencv.data.LookupTableCV;
import org.weasis.opencv.data.PlanarImage;
import org.weasis.opencv.op.lut.DefaultWlPresentation;
import org.weasis.opencv.op.lut.PresentationStateLut;
import org.weasis.opencv.op.lut.WlPresentation;

public class WindowAndPresetsOp
extends WindowOp {
    public static final String P_PR_ELEMENT = "pr.element";

    public void handleImageOpEvent(ImageOpEvent event) {
        ImageOpEvent.OpEvent type = event.eventType();
        if (ImageOpEvent.OpEvent.IMAGE_CHANGE.equals((Object)type)) {
            this.setParam("img.element", event.image());
            this.removeParam(P_PR_ELEMENT);
        } else if (ImageOpEvent.OpEvent.RESET_DISPLAY.equals((Object)type) || ImageOpEvent.OpEvent.SERIES_CHANGE.equals((Object)type)) {
            ImageElement img = event.image();
            this.setParam("img.element", img);
            PrDicomObject pr = (PrDicomObject)this.getParam(P_PR_ELEMENT);
            this.removeParam(P_PR_ELEMENT);
            if (img != null) {
                if (!img.isImageAvailable()) {
                    img.getImage();
                }
                boolean pixelPadding = LangUtil.nullToTrue((Boolean)((Boolean)this.getParam(ActionW.IMAGE_PIX_PADDING.cmd())));
                PresetWindowLevel preset = null;
                if (img instanceof DicomImageElement) {
                    DicomImageElement imageElement = (DicomImageElement)img;
                    DefaultWlPresentation wlp = new DefaultWlPresentation(null, pixelPadding);
                    if (pr != null) {
                        imageElement.getPresetList((WlPresentation)wlp, true);
                    }
                    preset = imageElement.getDefaultPreset((WlPresentation)wlp);
                }
                this.setPreset(preset, img, pixelPadding);
            }
        } else if (ImageOpEvent.OpEvent.APPLY_PR.equals((Object)type)) {
            ImageElement img = event.image();
            this.setParam("img.element", img);
            if (img != null) {
                if (!img.isImageAvailable()) {
                    img.getImage();
                }
                boolean pixelPadding = LangUtil.nullToTrue((Boolean)((Boolean)this.getParam(ActionW.IMAGE_PIX_PADDING.cmd())));
                Map p = event.params();
                if (p != null) {
                    PRSpecialElement pr = Optional.ofNullable(p.get(ActionW.PR_STATE.cmd())).filter(PRSpecialElement.class::isInstance).map(PRSpecialElement.class::cast).orElse(null);
                    this.setParam(P_PR_ELEMENT, pr == null ? null : pr.getPrDicomObject());
                    PresetWindowLevel preset = (PresetWindowLevel)p.get(ActionW.PRESET.cmd());
                    if (preset == null && img instanceof DicomImageElement) {
                        DicomImageElement imageElement = (DicomImageElement)img;
                        DefaultWlPresentation wlp = new DefaultWlPresentation(null, pixelPadding);
                        preset = imageElement.getDefaultPreset((WlPresentation)wlp);
                    }
                    this.setPreset(preset, img, pixelPadding);
                }
            }
        }
    }

    private void setPreset(PresetWindowLevel preset, ImageElement img, boolean pixelPadding) {
        boolean p = preset != null;
        PrDicomObject pr = (PrDicomObject)this.getParam(P_PR_ELEMENT);
        this.setParam(ActionW.PRESET.cmd(), preset);
        this.setParam(ActionW.DEFAULT_PRESET.cmd(), true);
        DefaultWlPresentation wlp = new DefaultWlPresentation((PresentationStateLut)pr, pixelPadding);
        this.setParam(ActionW.WINDOW.cmd(), p ? preset.getWindow() : img.getDefaultWindow((WlPresentation)wlp));
        this.setParam(ActionW.LEVEL.cmd(), p ? preset.getLevel() : img.getDefaultLevel((WlPresentation)wlp));
        this.setParam(ActionW.LEVEL_MIN.cmd(), img.getMinValue((WlPresentation)wlp));
        this.setParam(ActionW.LEVEL_MAX.cmd(), img.getMaxValue((WlPresentation)wlp));
        this.setParam(ActionW.LUT_SHAPE.cmd(), p ? preset.getLutShape() : img.getDefaultShape((WlPresentation)wlp));
    }

    public void process() throws Exception {
        PlanarImage source;
        PlanarImage result = source = this.getSourceImage();
        ImageElement imageElement = (ImageElement)this.params.get("img.element");
        if (imageElement != null) {
            PrDicomObject pr = (PrDicomObject)this.params.get(P_PR_ELEMENT);
            if (pr != null && "1.2.840.10008.5.1.4.1.1.11.3".equals(pr.getDicomObject().getString(524310))) {
                LookupTableCV lookup = PaletteColorUtils.getPaletteColorLookupTable(pr.getDicomObject());
                source = PaletteColorUtils.getRGBImageFromPaletteColorModel(source, lookup);
            }
            result = imageElement.getRenderedImage(source, this.params);
        }
        this.params.put("op.output.img", result);
    }
}

