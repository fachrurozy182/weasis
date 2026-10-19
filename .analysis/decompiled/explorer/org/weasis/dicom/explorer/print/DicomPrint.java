/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.dcm4che3.data.Attributes
 *  org.dcm4che3.data.Sequence
 *  org.dcm4che3.data.VR
 *  org.dcm4che3.net.ApplicationEntity
 *  org.dcm4che3.net.Association
 *  org.dcm4che3.net.Connection
 *  org.dcm4che3.net.Device
 *  org.dcm4che3.net.DimseRSP
 *  org.dcm4che3.net.pdu.AAssociateRQ
 *  org.dcm4che3.net.pdu.PresentationContext
 *  org.dcm4che3.util.UIDUtils
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.gui.layout.MigCell
 *  org.weasis.core.api.image.AffineTransformOp
 *  org.weasis.core.api.image.ZoomOp$Interpolation
 *  org.weasis.core.api.media.data.ImageElement
 *  org.weasis.core.ui.editor.image.ExportImage
 *  org.weasis.core.ui.util.ExportLayout
 *  org.weasis.core.ui.util.ImagePrint
 *  org.weasis.core.ui.util.PrintOptions$DotPerInches
 *  org.weasis.core.util.MathUtil
 *  org.weasis.opencv.data.PlanarImage
 */
package org.weasis.dicom.explorer.print;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.swing.JComponent;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.VR;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.DimseRSP;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.util.UIDUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.gui.layout.MigCell;
import org.weasis.core.api.image.AffineTransformOp;
import org.weasis.core.api.image.ZoomOp;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.ui.editor.image.ExportImage;
import org.weasis.core.ui.util.ExportLayout;
import org.weasis.core.ui.util.ImagePrint;
import org.weasis.core.ui.util.PrintOptions;
import org.weasis.core.util.MathUtil;
import org.weasis.dicom.explorer.pref.node.DefaultDicomNode;
import org.weasis.dicom.explorer.pref.node.DicomPrintNode;
import org.weasis.dicom.explorer.print.DicomPrintDialog;
import org.weasis.dicom.explorer.print.DicomPrintOptions;
import org.weasis.opencv.data.PlanarImage;

public class DicomPrint {
    private static final Logger LOGGER = LoggerFactory.getLogger(DicomPrint.class);
    private final DicomPrintNode dcmNode;
    private final DicomPrintOptions printOptions;
    private ZoomOp.Interpolation interpolation;
    private double placeholderX;
    private double placeholderY;
    private int lastx;
    private double lastwx;
    private double[] lastwy;
    private double wx;

    public DicomPrint(DicomPrintNode dicomPrintNode, DicomPrintOptions printOptions) {
        if (dicomPrintNode == null) {
            throw new IllegalArgumentException();
        }
        this.dcmNode = dicomPrintNode;
        this.printOptions = printOptions == null ? dicomPrintNode.getPrintOptions() : printOptions;
    }

    public BufferedImage printImage(ExportLayout<? extends ImageElement> layout) {
        if (layout == null) {
            return null;
        }
        BufferedImage bufferedImage = this.initialize(layout);
        Graphics2D g2d = (Graphics2D)bufferedImage.getGraphics();
        if (g2d != null) {
            Color borderColor = "WHITE".equals(this.printOptions.getBorderDensity()) ? Color.WHITE : Color.BLACK;
            Color background = "WHITE".equals(this.printOptions.getEmptyDensity()) ? Color.WHITE : Color.BLACK;
            g2d.setBackground(background);
            if (!Color.BLACK.equals(background)) {
                g2d.clearRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());
            }
            Dimension dimGrid = layout.getLayoutModel().getGridSize();
            double[] columnWeights = this.calculateColumnWeights(dimGrid);
            double[] rowWeights = this.calculateRowWeights(dimGrid);
            List cells = layout.getLayoutModel().getCells();
            for (MigCell cell : cells) {
                Component component = layout.findComponentForCell(cell);
                ExportImage image = null;
                Point2D.Double pad = new Point2D.Double(0.0, 0.0);
                if (component instanceof ExportImage) {
                    ExportImage exportImage;
                    image = exportImage = (ExportImage)component;
                    double weightx = this.calculateCellWeightX(cell, columnWeights);
                    double weighty = this.calculateCellWeightY(cell, rowWeights);
                    this.formatImage((ExportImage<? extends ImageElement>)image, pad, weightx, weighty);
                }
                if (cell.x() == 0) {
                    this.wx = 0.0;
                } else if (this.lastx < cell.x()) {
                    this.wx += this.lastwx;
                }
                double wy = this.lastwy[cell.x()];
                double x = 5.0 + this.placeholderX * this.wx + (double)(MathUtil.isEqualToZero((double)this.wx) ? 0 : cell.x() * 5) + pad.x;
                double y = 5.0 + this.placeholderY * wy + (double)(MathUtil.isEqualToZero((double)wy) ? 0 : cell.y() * 5) + pad.y;
                this.lastx = cell.x();
                this.lastwx = columnWeights[cell.x()];
                int i = cell.x();
                while (i < cell.x() + cell.spanX()) {
                    int n = i++;
                    this.lastwy[n] = this.lastwy[n] + rowWeights[cell.y()];
                }
                if (image == null) continue;
                boolean wasBuffered = ImagePrint.disableDoubleBuffering((JComponent)image);
                g2d.translate(x, y);
                g2d.setClip(image.getBounds());
                image.draw(g2d);
                ImagePrint.restoreDoubleBuffering((JComponent)image, (boolean)wasBuffered);
                g2d.translate(-x, -y);
                if (borderColor.equals(background)) continue;
                g2d.setClip(null);
                g2d.setColor(borderColor);
                g2d.setStroke(new BasicStroke(2.0f));
                Dimension viewSize = image.getSize();
                g2d.drawRect((int)x - 1, (int)y - 1, viewSize.width + 1, viewSize.height + 1);
            }
        }
        return bufferedImage;
    }

    private BufferedImage initialize(ExportLayout<? extends ImageElement> layout) {
        Dimension dimGrid = layout.getLayoutModel().getGridSize();
        DicomPrintDialog.FilmSize filmSize = this.printOptions.getFilmSizeId();
        PrintOptions.DotPerInches dpi = this.printOptions.getDpi();
        int width = filmSize.getWidth(dpi);
        int height = filmSize.getHeight(dpi);
        if ("LANDSCAPE".equals(this.printOptions.getFilmOrientation())) {
            int tmp = width;
            width = height;
            height = tmp;
        }
        String mType = this.printOptions.getMagnificationType();
        this.interpolation = ZoomOp.Interpolation.BILINEAR;
        if ("REPLICATE".equals(mType)) {
            this.interpolation = ZoomOp.Interpolation.NEAREST_NEIGHBOUR;
        } else if ("CUBIC".equals(mType)) {
            this.interpolation = ZoomOp.Interpolation.BICUBIC;
        }
        this.placeholderX = (double)width - (double)(dimGrid.width + 1) * 5.0;
        this.placeholderY = (double)height - (double)(dimGrid.height + 1) * 5.0;
        this.lastx = 0;
        this.lastwx = 0.0;
        this.lastwy = new double[dimGrid.width];
        this.wx = 0.0;
        if (this.printOptions.isColorPrint()) {
            return DicomPrint.createRGBBufferedImage(width, height);
        }
        return DicomPrint.createGrayBufferedImage(width, height);
    }

    private double[] calculateColumnWeights(Dimension dimGrid) {
        double[] weights = new double[dimGrid.width];
        double uniformWeight = 1.0 / (double)dimGrid.width;
        for (int i = 0; i < dimGrid.width; ++i) {
            weights[i] = uniformWeight;
        }
        return weights;
    }

    private double[] calculateRowWeights(Dimension dimGrid) {
        double[] weights = new double[dimGrid.height];
        double uniformWeight = 1.0 / (double)dimGrid.height;
        for (int i = 0; i < dimGrid.height; ++i) {
            weights[i] = uniformWeight;
        }
        return weights;
    }

    private double calculateCellWeightX(MigCell cell, double[] columnWeights) {
        double totalWeight = 0.0;
        for (int i = cell.x(); i < cell.x() + cell.spanX() && i < columnWeights.length; ++i) {
            totalWeight += columnWeights[i];
        }
        return totalWeight;
    }

    private double calculateCellWeightY(MigCell cell, double[] rowWeights) {
        double totalWeight = 0.0;
        for (int i = cell.y(); i < cell.y() + cell.spanY() && i < rowWeights.length; ++i) {
            totalWeight += rowWeights[i];
        }
        return totalWeight;
    }

    private void formatImage(ExportImage<? extends ImageElement> image, Point2D.Double pad, double weightx, double weighty) {
        if (!this.printOptions.isShowingAnnotations() && image.getInfoLayer().getVisible().booleanValue()) {
            image.getInfoLayer().setVisible(Boolean.valueOf(false));
        }
        Rectangle2D originSize = (Rectangle2D)image.getActionValue("origin.image.bound");
        Point2D originCenterOffset = (Point2D)image.getActionValue("origin.center.offset");
        Double originZoom = (Double)image.getActionValue("origin.zoom");
        PlanarImage img = image.getSourceImage();
        if (img != null && originCenterOffset != null && originZoom != null) {
            double canvasHeight;
            double canvasWidth;
            boolean bestfit;
            boolean bl = bestfit = originZoom <= 0.0;
            if (bestfit || originSize == null) {
                canvasWidth = (double)img.width() * image.getImage().getRescaleX();
                canvasHeight = (double)img.height() * image.getImage().getRescaleY();
            } else {
                canvasWidth = originSize.getWidth() / originZoom;
                canvasHeight = originSize.getHeight() / originZoom;
            }
            double scaleCanvas = Math.min(this.placeholderX * weightx / canvasWidth, this.placeholderY * weighty / canvasHeight);
            double cw = canvasWidth * scaleCanvas;
            double ch = canvasHeight * scaleCanvas;
            image.setSize((int)(cw + 0.5), (int)(ch + 0.5));
            if (this.printOptions.isCenter()) {
                pad.x = (this.placeholderX * weightx - cw) * 0.5;
                pad.y = (this.placeholderY * weighty - ch) * 0.5;
            } else {
                pad.x = 0.0;
                pad.y = 0.0;
            }
            image.getDisplayOpManager().setParamValue(AffineTransformOp.OP_NAME, "interpolation", (Object)this.interpolation);
            double scaleFactor = Math.min(cw / canvasWidth, ch / canvasHeight);
            image.zoom(Double.valueOf(scaleFactor));
            if (bestfit) {
                image.center();
            } else {
                image.setCenter(Double.valueOf(originCenterOffset.getX()), Double.valueOf(originCenterOffset.getY()));
            }
        }
    }

    public static BufferedImage createRGBBufferedImage(int destWidth, int destHeight) {
        ColorSpace cs = ColorSpace.getInstance(1000);
        ComponentColorModel cm = new ComponentColorModel(cs, false, false, 1, 0);
        WritableRaster r = ((ColorModel)cm).createCompatibleWritableRaster(destWidth, destHeight);
        return new BufferedImage(cm, r, false, null);
    }

    public static BufferedImage createGrayBufferedImage(int destWidth, int destHeight) {
        ColorSpace cs = ColorSpace.getInstance(1003);
        ComponentColorModel cm = new ComponentColorModel(cs, false, false, 1, 0);
        WritableRaster r = ((ColorModel)cm).createCompatibleWritableRaster(destWidth, destHeight);
        return new BufferedImage(cm, r, false, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void printImage(BufferedImage image, DefaultDicomNode selectedItem) throws Exception {
        Attributes filmSessionAttrs = new Attributes();
        Attributes filmBoxAttrs = new Attributes();
        Attributes imageBoxAttrs = new Attributes();
        Attributes dicomImage = new Attributes();
        String printManagementSOPClass = this.printOptions.isColorPrint() ? "1.2.840.10008.5.1.1.18" : "1.2.840.10008.5.1.1.9";
        String imageBoxSOPClass = this.printOptions.isColorPrint() ? "1.2.840.10008.5.1.1.4.1" : "1.2.840.10008.5.1.1.4";
        DicomPrint.storeRasterInDicom(image, dicomImage, this.printOptions.isColorPrint());
        String weasisAet = selectedItem == null ? "WEASIS_AE" : selectedItem.getAeTitle();
        Device device = new Device(weasisAet);
        ApplicationEntity ae = new ApplicationEntity(weasisAet);
        Connection conn = new Connection();
        ApplicationEntity remoteAE = new ApplicationEntity(this.dcmNode.getAeTitle());
        Connection remoteConn = new Connection();
        ae.addConnection(conn);
        ae.setAssociationInitiator(true);
        ae.setAETitle(weasisAet);
        remoteConn.setPort(this.dcmNode.getPort());
        remoteConn.setHostname(this.dcmNode.getHostname());
        remoteConn.setSocketCloseDelay(90);
        remoteAE.setAssociationAcceptor(true);
        remoteAE.addConnection(remoteConn);
        device.addConnection(conn);
        device.addApplicationEntity(ae);
        ae.addConnection(conn);
        device.setExecutor((Executor)Executors.newSingleThreadExecutor());
        device.setScheduledExecutor(Executors.newSingleThreadScheduledExecutor());
        filmSessionAttrs.setInt(0x20000010, VR.IS, new int[]{this.printOptions.getNumOfCopies()});
        filmSessionAttrs.setString(0x20000020, VR.CS, this.printOptions.getPriority());
        filmSessionAttrs.setString(0x20000030, VR.CS, this.printOptions.getMediumType());
        filmSessionAttrs.setString(0x20000040, VR.CS, this.printOptions.getFilmDestination());
        filmBoxAttrs.setString(537919568, VR.CS, this.printOptions.getFilmSizeId().toString());
        filmBoxAttrs.setString(537919552, VR.CS, this.printOptions.getFilmOrientation());
        filmBoxAttrs.setString(537919584, VR.CS, this.printOptions.getMagnificationType());
        filmBoxAttrs.setString(537919616, VR.CS, this.printOptions.getSmoothingType());
        filmBoxAttrs.setString(537919808, VR.CS, this.printOptions.getTrim());
        filmBoxAttrs.setString(0x20100100, VR.CS, this.printOptions.getBorderDensity());
        filmBoxAttrs.setInt(0x20100120, VR.US, new int[]{this.printOptions.getMinDensity()});
        filmBoxAttrs.setInt(537919792, VR.US, new int[]{this.printOptions.getMaxDensity()});
        filmBoxAttrs.setString(0x20100010, VR.ST, this.printOptions.getImageDisplayFormat());
        imageBoxAttrs.setInt(0x20200010, VR.US, new int[]{1});
        Sequence seq = imageBoxAttrs.ensureSequence(this.printOptions.isColorPrint() ? 0x20200111 : 0x20200110, 1);
        seq.add(dicomImage);
        String filmSessionUID = UIDUtils.createUID();
        String filmBoxUID = UIDUtils.createUID();
        Attributes filmSessionSequenceObject = new Attributes();
        filmSessionSequenceObject.setString(528720, VR.UI, "1.2.840.10008.5.1.1.1");
        filmSessionSequenceObject.setString(528725, VR.UI, filmSessionUID);
        seq = filmBoxAttrs.ensureSequence(537920768, 1);
        seq.add(filmSessionSequenceObject);
        AAssociateRQ rq = new AAssociateRQ();
        rq.addPresentationContext(new PresentationContext(1, printManagementSOPClass, new String[]{"1.2.840.10008.1.2"}));
        rq.setCallingAET(ae.getAETitle());
        rq.setCalledAET(remoteAE.getAETitle());
        Association as = ae.connect(remoteConn, rq);
        try {
            this.dimseRSPHandler(as.ncreate(printManagementSOPClass, "1.2.840.10008.5.1.1.1", filmSessionUID, filmSessionAttrs, "1.2.840.10008.1.2"));
            DimseRSP ncreateFilmBoxRSP = as.ncreate(printManagementSOPClass, "1.2.840.10008.5.1.1.2", filmBoxUID, filmBoxAttrs, "1.2.840.10008.1.2");
            this.dimseRSPHandler(ncreateFilmBoxRSP);
            ncreateFilmBoxRSP.next();
            Attributes imageBoxSequence = ncreateFilmBoxRSP.getDataset().getNestedDataset(537920784);
            this.dimseRSPHandler(as.nset(printManagementSOPClass, imageBoxSOPClass, imageBoxSequence.getString(528725), imageBoxAttrs, "1.2.840.10008.1.2"));
            this.dimseRSPHandler(as.naction(printManagementSOPClass, "1.2.840.10008.5.1.1.2", filmBoxUID, 1, null, "1.2.840.10008.1.2"));
            as.ndelete(printManagementSOPClass, "1.2.840.10008.5.1.1.2", filmBoxUID);
            as.ndelete(printManagementSOPClass, "1.2.840.10008.5.1.1.1", filmSessionUID);
        }
        finally {
            if (as != null && as.isReadyForDataTransfer()) {
                as.waitForOutstandingRSP();
                as.release();
            }
        }
    }

    private void dimseRSPHandler(DimseRSP response) throws IOException, InterruptedException {
        response.next();
        Attributes command = response.getCommand();
        int status = command.getInt(2304, 0);
        if (status == 278 || status == 263 || status == 46592 || status == 46594 || status == 46596 || status == 46601 || status == 46602) {
            LOGGER.warn("DICOM Print warning status: {}", (Object)Integer.toHexString(status));
        } else if (status != 0) {
            throw new IOException("Unable to print the image. DICOM response status: " + Integer.toHexString(status));
        }
    }

    public static void storeRasterInDicom(BufferedImage image, Attributes dcmObj, Boolean printInColor) {
        byte[] bytesOut = null;
        if (dcmObj != null && image != null) {
            DataBuffer dataBuffer;
            dcmObj.setInt(2621457, VR.US, new int[]{image.getWidth()});
            dcmObj.setInt(2621456, VR.US, new int[]{image.getHeight()});
            dcmObj.setInt(2621699, VR.US, new int[]{0});
            dcmObj.setString(2621444, VR.CS, printInColor != false ? "RGB" : "MONOCHROME2");
            dcmObj.setInt(0x280002, VR.US, new int[]{printInColor != false ? 3 : 1});
            dcmObj.setInt(2621696, VR.US, new int[]{8});
            dcmObj.setInt(2621697, VR.US, new int[]{8});
            dcmObj.setInt(2621698, VR.US, new int[]{7});
            dcmObj.setInt(2621492, VR.IS, new int[]{1, 1});
            if (printInColor.booleanValue()) {
                dcmObj.setInt(2621446, VR.US, new int[]{0});
                dataBuffer = image.getRaster().getDataBuffer();
            } else {
                dataBuffer = DicomPrint.convertRGBImageToMonochrome(image).getRaster().getDataBuffer();
            }
            if (dataBuffer instanceof DataBufferByte) {
                DataBufferByte dataBufferByte = (DataBufferByte)dataBuffer;
                bytesOut = dataBufferByte.getData();
            } else if (dataBuffer instanceof DataBufferShort) {
                DataBufferShort dataBufferShort = (DataBufferShort)dataBuffer;
                bytesOut = DicomPrint.fillShortArray(dataBufferShort.getData());
            } else if (dataBuffer instanceof DataBufferUShort) {
                DataBufferUShort dataBufferUShort = (DataBufferUShort)dataBuffer;
                bytesOut = DicomPrint.fillShortArray(dataBufferUShort.getData());
            }
            dcmObj.setBytes(2145386512, VR.OW, bytesOut);
        }
    }

    private static byte[] fillShortArray(short[] data) {
        byte[] bytesOut = new byte[data.length * 2];
        for (int i = 0; i < data.length; ++i) {
            bytesOut[i * 2] = (byte)(data[i] & 0xFF);
            bytesOut[i * 2 + 1] = (byte)(data[i] >>> 8 & 0xFF);
        }
        return bytesOut;
    }

    private static BufferedImage convertRGBImageToMonochrome(BufferedImage colorImage) {
        if (colorImage.getType() == 10) {
            return colorImage;
        }
        BufferedImage image = new BufferedImage(colorImage.getWidth(), colorImage.getHeight(), 10);
        Graphics g = image.getGraphics();
        g.drawImage(colorImage, 0, 0, null);
        g.dispose();
        return image;
    }
}

