/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.image;

import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.image.BufferedImageUtils;
import org.dcm4che3.image.ColorModelFactory;
import org.dcm4che3.image.PaletteColorModel;

public final class ICCProfile {
    private ICCProfile() {
    }

    public static boolean isPresentIn(Attributes attrs) {
        return attrs.containsValue(0x282000) || attrs.containsValue(4718853);
    }

    public static ColorSpaceFactory colorSpaceFactoryOf(Attributes attrs) {
        Sequence opticalPathSequence;
        byte[] b = attrs.getSafeBytes(0x282000);
        if (b == null && (opticalPathSequence = attrs.getSequence(4718853)) != null && !opticalPathSequence.isEmpty()) {
            if (opticalPathSequence.size() > 1) {
                return frameIndex -> ICCProfile.getColorSpace(attrs, opticalPathSequence, frameIndex);
            }
            b = ((Attributes)opticalPathSequence.get(0)).getSafeBytes(0x282000);
        }
        if (b == null) {
            return frameIndex -> Optional.empty();
        }
        Optional<ICC_ColorSpace> cs = Optional.of(new ICC_ColorSpace(ICC_Profile.getInstance(b)));
        return frameIndex -> cs;
    }

    private static Optional<ColorSpace> getColorSpace(Attributes attrs, Sequence opticalPathSequence, int frameIndex) {
        byte[] b;
        Optional<Attributes> match;
        String opticalPathID;
        Attributes functionGroup = attrs.getFunctionGroup(4719111, frameIndex);
        if (functionGroup != null && (opticalPathID = functionGroup.getString(4718854)) != null && (match = opticalPathSequence.stream().filter(item -> opticalPathID.equals(item.getString(4718854))).findFirst()).isPresent() && (b = match.get().getSafeBytes(0x282000)) != null) {
            return Optional.of(new ICC_ColorSpace(ICC_Profile.getInstance(b)));
        }
        return Optional.empty();
    }

    @FunctionalInterface
    public static interface ColorSpaceFactory {
        public Optional<ColorSpace> getColorSpace(int var1);
    }

    public static enum Option {
        none{

            @Override
            protected BufferedImage convertColor(BufferedImage bi) {
                return Option.isCS_sRGB(bi) ? bi : BufferedImageUtils.convertColor(bi, CM_sRGB);
            }
        }
        ,
        no{

            @Override
            protected BufferedImage convertColor(BufferedImage bi) {
                return Option.isCS_sRGB(bi) ? bi : BufferedImageUtils.replaceColorModel(bi, CM_sRGB);
            }
        }
        ,
        yes{

            @Override
            protected BufferedImage convertColor(BufferedImage bi) {
                return Option.isCS_sRGB(bi) ? BufferedImageUtils.replaceColorModel(bi, srgb.colorModel) : bi;
            }
        }
        ,
        srgb("sRGB.icc"){

            @Override
            protected BufferedImage convertColor(BufferedImage bi) {
                return Option.isCS_sRGB(bi) ? BufferedImageUtils.replaceColorModel(bi, srgb.colorModel) : BufferedImageUtils.convertColor(bi, srgb.colorModel);
            }
        }
        ,
        adobergb("adobeRGB.icc"),
        rommrgb("rommRGB.icc");

        private static final ColorModel CM_sRGB;
        private final ColorModel colorModel;

        private Option() {
            this.colorModel = null;
        }

        private Option(String fileName) {
            try (InputStream is = ICCProfile.class.getResourceAsStream(fileName);){
                this.colorModel = ColorModelFactory.createRGBColorModel(8, 0, new ICC_ColorSpace(ICC_Profile.getInstance(is)));
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public BufferedImage adjust(BufferedImage bi) {
            ColorModel cm = bi.getColorModel();
            return cm.getNumColorComponents() == 3 ? this.convertColor(Option.toRGB(bi, cm)) : bi;
        }

        protected BufferedImage convertColor(BufferedImage bi) {
            return BufferedImageUtils.convertColor(bi, this.colorModel);
        }

        private static boolean isCS_sRGB(BufferedImage bi) {
            return bi.getColorModel().getColorSpace().isCS_sRGB();
        }

        private static BufferedImage toRGB(BufferedImage bi, ColorModel cm) {
            return cm instanceof PaletteColorModel ? BufferedImageUtils.convertPalettetoRGB(bi, null) : (cm.getColorSpace().getType() == 3 ? BufferedImageUtils.convertYBRtoRGB(bi, null) : bi);
        }

        static {
            CM_sRGB = ColorModelFactory.createRGBColorModel(8, 0, ColorSpace.getInstance(1000));
        }
    }
}

