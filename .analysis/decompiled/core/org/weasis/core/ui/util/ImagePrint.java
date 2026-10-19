/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.MathUtil
 *  org.weasis.opencv.data.PlanarImage
 */
package org.weasis.core.ui.util;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.List;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.layout.MigCell;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.ui.editor.image.ExportImage;
import org.weasis.core.ui.util.ExportLayout;
import org.weasis.core.ui.util.ForcedAcceptPrintService;
import org.weasis.core.ui.util.PrintOptions;
import org.weasis.core.util.MathUtil;
import org.weasis.opencv.data.PlanarImage;

public class ImagePrint
implements Printable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ImagePrint.class);
    private static final double POINTS_PER_INCH = 72.0;
    private Point printLoc;
    private final PrintOptions printOptions;
    private final ExportLayout<? extends ImageElement> layout;

    public ImagePrint(ExportLayout<? extends ImageElement> layout, PrintOptions printOptions) {
        this.layout = layout;
        this.printLoc = new Point(0, 0);
        this.printOptions = printOptions == null ? new PrintOptions() : printOptions;
    }

    public void setPrintLocation(Point d) {
        this.printLoc = d;
    }

    public Point getPrintLocation() {
        return this.printLoc;
    }

    private static OrientationRequested mapOrientation(int orientation) {
        return switch (orientation) {
            case 0 -> OrientationRequested.LANDSCAPE;
            case 2 -> OrientationRequested.REVERSE_LANDSCAPE;
            case 1 -> OrientationRequested.PORTRAIT;
            default -> throw new IllegalArgumentException("The given value is no valid PageFormat orientation.");
        };
    }

    public void print() {
        PrinterJob pj = PrinterJob.getPrinterJob();
        pj.setPrintable(this);
        HashPrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
        PageFormat pf = pj.getPageFormat(aset);
        Paper paper = pf.getPaper();
        MediaSizeName media = MediaSize.findMedia((float)(paper.getWidth() / 72.0), (float)(paper.getHeight() / 72.0), 25400);
        aset.add(media);
        MediaSize mediaSize = MediaSize.getMediaSizeForName(media);
        MediaPrintableArea printableArea = new MediaPrintableArea(0.25f, 0.25f, mediaSize.getX(25400) - 0.5f, mediaSize.getY(25400) - 0.5f, 25400);
        aset.add(printableArea);
        aset.add(ImagePrint.mapOrientation(pf.getOrientation()));
        if (pj.printDialog(aset)) {
            try {
                pj.print(aset);
            }
            catch (Exception e) {
                if (e.getMessage().contains("accepting job")) {
                    int response = JOptionPane.showConfirmDialog(GuiUtils.getUICore().getApplicationWindow(), Messages.getString("ImagePrint.issue_desc"), Messages.getString("ImagePrint.status"), 0, 2);
                    if (response == 0) {
                        try {
                            ForcedAcceptPrintService.setupPrintJob(pj);
                            pj.print(aset);
                            LOGGER.info("Bypass Printer is not accepting job");
                        }
                        catch (PrinterException ex) {
                            LOGGER.error("Printer exception", (Throwable)ex);
                        }
                    }
                }
                LOGGER.error("Print exception", (Throwable)e);
            }
        }
    }

    public static boolean disableDoubleBuffering(JComponent c) {
        if (c == null) {
            return false;
        }
        c.setDoubleBuffered(false);
        return c.isDoubleBuffered();
    }

    public static void restoreDoubleBuffering(JComponent c, boolean wasBuffered) {
        if (c != null) {
            c.setDoubleBuffered(wasBuffered);
        }
    }

    @Override
    public int print(Graphics g, PageFormat f, int pageIndex) {
        if (pageIndex >= 1) {
            return 1;
        }
        this.printImage((Graphics2D)g, f);
        return 0;
    }

    public void printImage(Graphics2D g2d, PageFormat f) {
        if (this.layout == null || g2d == null) {
            return;
        }
        Dimension dimGrid = this.layout.getLayoutModel().getGridSize();
        Point2D.Double placeholder = new Point2D.Double(f.getImageableWidth() - (double)((dimGrid.width - 1) * 5), f.getImageableHeight() - (double)((dimGrid.height - 1) * 5));
        double[] columnWeights = this.calculateColumnWeights(dimGrid);
        double[] rowWeights = this.calculateRowWeights(dimGrid);
        int lastx = 0;
        double lastwx = 0.0;
        double[] lastwy = new double[dimGrid.width];
        double wx = 0.0;
        List<MigCell> cells = this.layout.getLayoutModel().getCells();
        for (MigCell cell : cells) {
            Component component = this.layout.findComponentForCell(cell);
            ExportImage image = null;
            Point2D.Double pad = new Point2D.Double(0.0, 0.0);
            if (component instanceof ExportImage) {
                ExportImage exportImage;
                image = exportImage = (ExportImage)component;
                double weightx = this.calculateCellWeightX(cell, columnWeights);
                double weighty = this.calculateCellWeightY(cell, rowWeights);
                this.formatImage(image, placeholder, pad, weightx, weighty);
            }
            if (cell.x() == 0) {
                wx = 0.0;
            } else if (lastx < cell.x()) {
                wx += lastwx;
            }
            double wy = lastwy[cell.x()];
            double x = (double)this.printLoc.x + f.getImageableX() + placeholder.x * wx + (double)(MathUtil.isEqualToZero((double)wx) ? 0 : cell.x() * 5) + pad.x;
            double y = (double)this.printLoc.y + f.getImageableY() + placeholder.y * wy + (double)(MathUtil.isEqualToZero((double)wy) ? 0 : cell.y() * 5) + pad.y;
            lastx = cell.x();
            lastwx = columnWeights[cell.x()];
            int i = cell.x();
            while (i < cell.x() + cell.spanX()) {
                int n = i++;
                lastwy[n] = lastwy[n] + rowWeights[cell.y()];
            }
            if (image == null) continue;
            g2d.translate(x, y);
            boolean wasBuffered = ImagePrint.disableDoubleBuffering(image);
            g2d.setClip(image.getBounds());
            image.draw(g2d);
            ImagePrint.restoreDoubleBuffering(image, wasBuffered);
            g2d.translate(-x, -y);
        }
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

    private void formatImage(ExportImage<? extends ImageElement> image, Point2D.Double placeholder, Point2D.Double pad, double weightx, double weighty) {
        if (!this.printOptions.isShowingAnnotations() && image.getInfoLayer().getVisible().booleanValue()) {
            image.getInfoLayer().setVisible(false);
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
                canvasWidth = (double)img.width() * ((ImageElement)image.getImage()).getRescaleX();
                canvasHeight = (double)img.height() * ((ImageElement)image.getImage()).getRescaleY();
            } else {
                canvasWidth = originSize.getWidth() / originZoom;
                canvasHeight = originSize.getHeight() / originZoom;
            }
            double scaleCanvas = Math.min(placeholder.x * weightx / canvasWidth, placeholder.y * weighty / canvasHeight);
            double cw = canvasWidth * scaleCanvas;
            double ch = canvasHeight * scaleCanvas;
            image.setSize((int)(cw + 0.5), (int)(ch + 0.5));
            if (this.printOptions.isCenter()) {
                pad.x = (placeholder.x * weightx - cw) * 0.5;
                pad.y = (placeholder.y * weighty - ch) * 0.5;
            } else {
                pad.x = 0.0;
                pad.y = 0.0;
            }
            double scaleFactor = Math.min(cw / canvasWidth, ch / canvasHeight);
            image.zoom(scaleFactor);
            if (bestfit) {
                image.center();
            } else {
                image.setCenter(originCenterOffset.getX(), originCenterOffset.getY());
            }
            int dpi = this.printOptions.getDpi() == null ? 150 : this.printOptions.getDpi().getDpi();
            double ratioFromDPI = (double)dpi * scaleFactor / 72.0;
            image.setImagePrintingResolution(ratioFromDPI);
        }
    }
}

