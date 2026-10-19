/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.image;

import java.awt.Graphics;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.IIOImage;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOInvalidTreeException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.VR;
import org.dcm4che3.image.PaletteColorModel;
import org.w3c.dom.Node;

public class BufferedImageUtils {
    private BufferedImageUtils() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static BufferedImage convertToIntRGB(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        if (cm instanceof DirectColorModel) {
            return bi;
        }
        if (cm.getNumComponents() != 3) {
            throw new IllegalArgumentException("ColorModel: " + cm);
        }
        WritableRaster raster = bi.getRaster();
        if (cm instanceof PaletteColorModel) {
            return ((PaletteColorModel)cm).convertToIntDiscrete(raster);
        }
        BufferedImage intRGB = new BufferedImage(bi.getWidth(), bi.getHeight(), 1);
        Graphics graphics = intRGB.getGraphics();
        try {
            graphics.drawImage(bi, 0, 0, null);
        }
        finally {
            graphics.dispose();
        }
        return intRGB;
    }

    public static BufferedImage convertYBRtoRGB(BufferedImage src, BufferedImage dst) {
        if (src.getColorModel().getTransferType() != 0) {
            throw new UnsupportedOperationException("Cannot convert color model to RGB: unsupported transferType" + src.getColorModel().getTransferType());
        }
        if (src.getColorModel().getNumComponents() != 3) {
            throw new IllegalArgumentException("Unsupported colorModel: " + src.getColorModel());
        }
        int width = src.getWidth();
        int height = src.getHeight();
        if (dst == null) {
            ComponentColorModel cmodel = new ComponentColorModel(ColorSpace.getInstance(1000), new int[]{8, 8, 8}, false, false, 1, 0);
            SampleModel sampleModel = ((ColorModel)cmodel).createCompatibleSampleModel(width, height);
            DataBuffer dataBuffer = sampleModel.createDataBuffer();
            WritableRaster rasterDst = Raster.createWritableRaster(sampleModel, dataBuffer, null);
            dst = new BufferedImage(cmodel, rasterDst, false, null);
        }
        WritableRaster rasterDst = dst.getRaster();
        WritableRaster raster = src.getRaster();
        ColorSpace cs = src.getColorModel().getColorSpace();
        ColorSpace dstcs = dst.getColorModel().getColorSpace();
        byte[] ba = new byte[3];
        float[] fba = new float[3];
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                raster.getDataElements(x, y, ba);
                for (int i = 0; i < 3; ++i) {
                    fba[i] = (float)(ba[i] & 0xFF) / 255.0f;
                }
                float[] rgb = cs.toRGB(fba);
                float[] color = dstcs.fromRGB(rgb);
                for (int i = 0; i < 3; ++i) {
                    ba[i] = (byte)(color[i] * 255.0f + 0.5f);
                }
                rasterDst.setDataElements(x, y, ba);
            }
        }
        return dst;
    }

    public static BufferedImage convertPalettetoRGB(BufferedImage src, BufferedImage dst) {
        ColorModel pcm = src.getColorModel();
        if (!(pcm instanceof PaletteColorModel) && !(pcm instanceof IndexColorModel)) {
            throw new UnsupportedOperationException("Cannot convert " + pcm.getClass().getName() + " to RGB");
        }
        int width = src.getWidth();
        int height = src.getHeight();
        if (dst == null) {
            ComponentColorModel cmodel = new ComponentColorModel(pcm.getColorSpace(), new int[]{8, 8, 8}, false, false, 1, 0);
            SampleModel sampleModel = ((ColorModel)cmodel).createCompatibleSampleModel(width, height);
            DataBuffer dataBuffer = sampleModel.createDataBuffer();
            WritableRaster rasterDst = Raster.createWritableRaster(sampleModel, dataBuffer, null);
            dst = new BufferedImage(cmodel, rasterDst, false, null);
        }
        WritableRaster rasterDst = dst.getRaster();
        WritableRaster raster = src.getRaster();
        byte[] b = new byte[3];
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                int rgb = pcm.getRGB(raster.getSample(j, i, 0));
                b[0] = (byte)(rgb >> 16 & 0xFF);
                b[1] = (byte)(rgb >> 8 & 0xFF);
                b[2] = (byte)(rgb & 0xFF);
                rasterDst.setDataElements(j, i, b);
            }
        }
        return dst;
    }

    public static BufferedImage convertShortsToBytes(BufferedImage src, BufferedImage dst) {
        DataBuffer srcBuffer;
        if (dst == null) {
            dst = new BufferedImage(src.getWidth(), src.getHeight(), 10);
        }
        if ((srcBuffer = src.getRaster().getDataBuffer()) instanceof DataBufferByte) {
            byte[] srcData = ((DataBufferByte)srcBuffer).getData();
            byte[] dstData = ((DataBufferByte)dst.getRaster().getDataBuffer()).getData();
            System.arraycopy(srcData, 0, dstData, 0, srcData.length);
            return dst;
        }
        short[] srcData = srcBuffer instanceof DataBufferUShort ? ((DataBufferUShort)srcBuffer).getData() : ((DataBufferShort)srcBuffer).getData();
        byte[] dstData = ((DataBufferByte)dst.getRaster().getDataBuffer()).getData();
        for (int i = 0; i < srcData.length; ++i) {
            dstData[i] = (byte)srcData[i];
        }
        return dst;
    }

    public static BufferedImage replaceColorModel(BufferedImage bi, ColorModel colorModel) {
        return new BufferedImage(colorModel, bi.getRaster(), false, null);
    }

    public static BufferedImage convertColor(BufferedImage bi, ColorModel colorModel) {
        BufferedImage dest = new BufferedImage(colorModel, Raster.createWritableRaster(colorModel.createCompatibleSampleModel(bi.getWidth(), bi.getHeight()), null), false, null);
        new ColorConvertOp(null).filter(bi, dest);
        return dest;
    }

    public static Attributes toImagePixelModule(BufferedImage bi, Attributes attrs) {
        ColorModel cm = bi.getColorModel();
        if (cm instanceof IndexColorModel || cm instanceof PaletteColorModel) {
            bi = BufferedImageUtils.convertPalettetoRGB(bi, null);
            cm = bi.getColorModel();
        }
        ColorSpace cs = cm.getColorSpace();
        WritableRaster raster = bi.getRaster();
        int rows = raster.getHeight();
        int columns = raster.getWidth();
        switch (cs.getType()) {
            case 6: {
                return BufferedImageUtils.toImagePixelModule(1, "MONOCHROME2", rows, columns, BufferedImageUtils.toMonochrome2PixelData(raster), attrs);
            }
            case 5: {
                return BufferedImageUtils.toImagePixelModule(3, "RGB", rows, columns, BufferedImageUtils.toRGBPixelData(raster), attrs);
            }
        }
        throw new UnsupportedOperationException(BufferedImageUtils.toString(cs));
    }

    public static Attributes toImagePixelModule(ImageReader reader, Attributes attrs) throws IOException {
        IIOImage nextFrame;
        IIOImage firstFrame = reader.readAll(0, null);
        BufferedImage bi = (BufferedImage)firstFrame.getRenderedImage();
        try {
            nextFrame = reader.readAll(1, null);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            throw new IOException("JDK-7132728: stringtable overflow in GIFImageReader", e);
        }
        catch (IndexOutOfBoundsException e) {
            return BufferedImageUtils.toImagePixelModule(bi, attrs);
        }
        ArrayList<byte[]> frames = new ArrayList<byte[]>();
        ArrayList<String> delayTimes = new ArrayList<String>();
        BufferedImage rgb = BufferedImageUtils.convertPalettetoRGB(bi, null);
        frames.add(BufferedImageUtils.toRGBPixelData(rgb.getRaster()));
        delayTimes.add(BufferedImageUtils.getDelayTime(BufferedImageUtils.getMetadata(firstFrame)));
        Graphics graphics = bi.getGraphics();
        try {
            while (true) {
                Node metadata = BufferedImageUtils.getMetadata(nextFrame);
                BufferedImageUtils.mergeFrame(graphics, (BufferedImage)nextFrame.getRenderedImage(), metadata);
                frames.add(BufferedImageUtils.toRGBPixelData(BufferedImageUtils.convertPalettetoRGB(bi, rgb).getRaster()));
                delayTimes.add(BufferedImageUtils.getDelayTime(metadata));
                nextFrame = reader.readAll(frames.size(), null);
            }
        }
        catch (ArrayIndexOutOfBoundsException e) {
            throw new IOException("JDK-7132728: stringtable overflow in GIFImageReader", e);
        }
        catch (IndexOutOfBoundsException indexOutOfBoundsException) {
            graphics.dispose();
            attrs = BufferedImageUtils.toImagePixelModule(3, "RGB", bi.getHeight(), bi.getWidth(), BufferedImageUtils.toPixeldata(frames), attrs);
            attrs.setInt(0x280008, VR.IS, frames.size());
            BufferedImageUtils.setFrameTimeVector(attrs, delayTimes);
            return attrs;
        }
    }

    private static void mergeFrame(Graphics graphics, BufferedImage src, Node metadata) throws IIOInvalidTreeException {
        Node imageDescriptor = BufferedImageUtils.getChildNode(metadata, "ImageDescriptor");
        graphics.drawImage(src, BufferedImageUtils.getIntAttribute(imageDescriptor, "imageLeftPosition"), BufferedImageUtils.getIntAttribute(imageDescriptor, "imageTopPosition"), null);
    }

    private static Node getMetadata(IIOImage iioImage) {
        return iioImage.getMetadata().getAsTree("javax_imageio_gif_image_1.0");
    }

    private static String getDelayTime(Node node) throws IIOInvalidTreeException {
        return BufferedImageUtils.getStringAttribute(BufferedImageUtils.getChildNode(node, "GraphicControlExtension"), "delayTime");
    }

    private static String getStringAttribute(Node node, String name) throws IIOInvalidTreeException {
        Node attr = node.getAttributes().getNamedItem(name);
        if (attr == null) {
            throw new IIOInvalidTreeException("Required attribute " + name + " not present!", node);
        }
        return attr.getNodeValue();
    }

    private static int getIntAttribute(Node node, String name) throws IIOInvalidTreeException {
        try {
            return Integer.parseInt(BufferedImageUtils.getStringAttribute(node, name));
        }
        catch (NumberFormatException e) {
            throw new IIOInvalidTreeException("Bad value for " + node.getNodeName() + " attribute " + name + "!", node);
        }
    }

    private static Node getChildNode(Node root, String name) throws IIOInvalidTreeException {
        for (Node child = root.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (!name.equals(child.getLocalName())) continue;
            return child;
        }
        throw new IIOInvalidTreeException("Required child " + name + " not present!", root);
    }

    private static byte[] toPixeldata(List<byte[]> frames) {
        byte[] pixeldata = new byte[frames.get(0).length * frames.size()];
        int pos = 0;
        for (byte[] frame : frames) {
            System.arraycopy(frame, 0, pixeldata, pos, frame.length);
            pos += frame.length;
        }
        return pixeldata;
    }

    private static void setFrameTimeVector(Attributes attrs, List<String> delayTimes) {
        String delayTime0 = delayTimes.get(0);
        for (int i = 1; i < delayTimes.size(); ++i) {
            if (delayTime0.equals(delayTimes.get(i))) continue;
            attrs.setString(1577061, VR.DS, BufferedImageUtils.toFrameTimes(delayTimes));
            attrs.setInt(2621449, VR.AT, 1577061);
        }
        attrs.setString(1577059, VR.DS, BufferedImageUtils.toFrameTime(delayTime0));
        attrs.setInt(2621449, VR.AT, 1577061);
    }

    private static String[] toFrameTimes(List<String> delayTimes) {
        String[] frameTimes = new String[delayTimes.size()];
        for (int i = 0; i < frameTimes.length; ++i) {
            frameTimes[i] = BufferedImageUtils.toFrameTime(delayTimes.get(i));
        }
        return frameTimes;
    }

    private static String toFrameTime(String delayTime) {
        return "0".equals(delayTime) ? "0" : delayTime + "0";
    }

    private static String toString(ColorSpace cs) {
        StringBuilder sb = new StringBuilder();
        int n = cs.getNumComponents();
        for (int i = 0; i < n; ++i) {
            sb.append(i > 0 ? ", 0" : "ColorSpace[").append(cs.getName(i));
        }
        return sb.append(']').toString();
    }

    private static byte[] toMonochrome2PixelData(Raster raster) {
        ComponentSampleModel csb = BufferedImageUtils.getComponentSampleModel(raster);
        int pixelStride = csb.getPixelStride();
        int scanlineStride = csb.getScanlineStride();
        int h = csb.getHeight();
        int w = csb.getWidth();
        int offset = csb.getOffset(0, 0);
        byte[] src = BufferedImageUtils.getData(raster);
        byte[] dest = new byte[h * w];
        int j = 0;
        for (int y = 0; y < h; ++y) {
            int x = 0;
            int i = y * scanlineStride;
            while (x < w) {
                dest[j++] = src[i + offset];
                ++x;
                i += pixelStride;
            }
        }
        return dest;
    }

    private static byte[] toRGBPixelData(Raster raster) {
        byte[] dest = new byte[raster.getHeight() * raster.getWidth() * 3];
        BufferedImageUtils.copyRGBPixelDataTo(raster, dest, 0);
        return dest;
    }

    private static void copyRGBPixelDataTo(Raster raster, byte[] dest, int offset) {
        ComponentSampleModel csb = BufferedImageUtils.getComponentSampleModel(raster);
        int pixelStride = csb.getPixelStride();
        int scanlineStride = csb.getScanlineStride();
        int h = csb.getHeight();
        int w = csb.getWidth();
        int r = csb.getOffset(0, 0, 0);
        int g = csb.getOffset(0, 0, 1);
        int b = csb.getOffset(0, 0, 2);
        byte[] src = BufferedImageUtils.getData(raster);
        int j = offset;
        for (int y = 0; y < h; ++y) {
            int x = 0;
            int i = y * scanlineStride;
            while (x < w) {
                dest[j++] = src[i + r];
                dest[j++] = src[i + g];
                dest[j++] = src[i + b];
                ++x;
                i += pixelStride;
            }
        }
    }

    private static byte[] getData(Raster raster) {
        DataBuffer dataBuffer = raster.getDataBuffer();
        if (!(dataBuffer instanceof DataBufferByte) || dataBuffer.getNumBanks() > 1) {
            throw new UnsupportedOperationException(raster.toString());
        }
        return ((DataBufferByte)dataBuffer).getData();
    }

    private static ComponentSampleModel getComponentSampleModel(Raster raster) {
        SampleModel sb = raster.getSampleModel();
        if (!(sb instanceof ComponentSampleModel)) {
            throw new UnsupportedOperationException(sb.toString());
        }
        return (ComponentSampleModel)sb;
    }

    private static Attributes toImagePixelModule(int samples, String pmi, int rows, int columns, byte[] pixelData, Attributes attrs) {
        if (attrs == null) {
            attrs = new Attributes(13);
        }
        attrs.setInt(0x280002, VR.US, samples);
        attrs.setString(2621444, VR.CS, pmi);
        if (samples > 1) {
            attrs.setInt(2621446, VR.US, 0);
        }
        attrs.setInt(2621456, VR.US, rows);
        attrs.setInt(2621457, VR.US, columns);
        attrs.setInt(2621696, VR.US, 8);
        attrs.setInt(2621697, VR.US, 8);
        attrs.setInt(2621698, VR.US, 7);
        attrs.setInt(2621699, VR.US, 0);
        attrs.setBytes(2145386512, VR.OB, pixelData);
        return attrs;
    }
}

