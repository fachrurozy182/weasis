/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.opencv.op.lut.WlParams
 */
package org.weasis.core.ui.editor.image;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.util.DecFormatter;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.image.util.WindLevelParameters;
import org.weasis.core.api.util.FontItem;
import org.weasis.core.api.util.FontTools;
import org.weasis.core.ui.editor.image.DefaultView2d;
import org.weasis.core.ui.editor.image.HistogramData;
import org.weasis.opencv.op.lut.WlParams;

public class HistogramPanel
extends JPanel {
    private static final Logger LOGGER = LoggerFactory.getLogger(HistogramPanel.class);
    private static final int SLIDER_HEIGHT = 15;
    private static final int SLIDER_WIDTH = 10;
    private static final int SLIDER_X = 10;
    private static final int SLIDER_Y = 20;
    private HistogramData data;
    private boolean showIntensity = true;
    private boolean logarithmic = false;
    private boolean accumulate = false;
    private float zoom = 1.0f;
    private float xAxisHistoRescaleRatio = 1.0f;

    public HistogramPanel() {
        this.init();
        InternalMouseListener listener = new InternalMouseListener(this);
        this.addMouseListener(listener);
        this.addMouseMotionListener(listener);
    }

    private void init() {
        this.setPreferredSize(new Dimension(275, 70));
        this.setBackground(new Color(235, 236, 210));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d;
        block3: {
            block2: {
                super.paintComponent(g);
                if (!(g instanceof Graphics2D)) break block2;
                g2d = (Graphics2D)g;
                if (this.data.getHistValues() != null && this.data.getLut() != null) break block3;
            }
            return;
        }
        Color oldColor = g2d.getColor();
        Stroke oldStroke = g2d.getStroke();
        Font oldFont = g2d.getFont();
        Font font = FontItem.DEFAULT_SEMIBOLD.getFont();
        g2d.setFont(font.deriveFont((float)font.getSize() - 3.0f));
        this.drawHistogramPane(g2d);
        g2d.setFont(oldFont);
        g2d.setColor(oldColor);
        g2d.setStroke(oldStroke);
    }

    private void drawHistogramPane(Graphics2D g2d) {
        boolean hls;
        float sum = 0.0f;
        float maxHistogramCounts = 1.0f;
        float[] histValues = this.data.getHistValues();
        int nbBins = histValues.length;
        for (float histValue : histValues) {
            float val;
            float f = val = this.logarithmic ? (float)Math.log1p(histValue) : histValue;
            if (this.accumulate) {
                sum += val;
                continue;
            }
            maxHistogramCounts = Math.max(maxHistogramCounts, val);
        }
        if (this.accumulate) {
            maxHistogramCounts = sum;
        }
        FontMetrics fontMetrics = g2d.getFontMetrics();
        int fontHeight = fontMetrics.getHeight();
        int midFontHeight = fontHeight + 3;
        float bCanvas = (float)this.getHeight() - 1.0f;
        float tLut = bCanvas - ((float)(20 + fontHeight) + 15.0f);
        float bLut = tLut + 20.0f + 5.0f;
        float fj = (tLut - 20.0f) * this.zoom / maxHistogramCounts;
        float lutLength = (float)this.getWidth() - 20.0f;
        this.xAxisHistoRescaleRatio = lutLength / (float)nbBins;
        WlParams windLevel = this.data.getWindLevel();
        double min = this.data.getPixMin();
        double max = this.data.getPixMax();
        double low = windLevel.getLevel() - windLevel.getWindow() / 2.0;
        double high = windLevel.getLevel() + windLevel.getWindow() / 2.0;
        float firstLevel = (float)min;
        float hRange = (float)(max - min);
        float spaceFactor = (lutLength - this.xAxisHistoRescaleRatio) / hRange;
        float x = 10.0f;
        int piLow = nbBins + 1;
        int piHigh = -1;
        double diffLow = Double.MAX_VALUE;
        double diffHigh = Double.MAX_VALUE;
        g2d.setStroke(new BasicStroke(this.xAxisHistoRescaleRatio + 0.5f, 0, 2));
        double binFactor = (max - min) / (double)(nbBins - 1);
        sum = 0.0f;
        for (int i = 0; i < nbBins; ++i) {
            float val;
            float f = val = this.logarithmic ? (float)Math.log1p(histValues[i]) : histValues[i];
            if (this.accumulate) {
                sum += val;
                val = sum;
            }
            float y1 = tLut - fj * val;
            float xVal = 10.0f + (float)i * this.xAxisHistoRescaleRatio;
            double level = this.data.getLayer().pixelToRealValue((double)i * binFactor + min);
            Color cLut = this.data.getFinalVoiLutColor(level);
            double diff = Math.abs(level - low);
            if (diff < diffLow) {
                diffLow = diff;
                piLow = i;
            }
            if ((diff = Math.abs(level - high)) < diffHigh) {
                diffHigh = diff;
                piHigh = i;
            }
            if (this.showIntensity) {
                g2d.setPaint(cLut);
            } else {
                g2d.setPaint(Color.BLACK);
            }
            g2d.draw(new Line2D.Float(xVal, y1, xVal, tLut));
            g2d.setPaint(cLut);
            g2d.draw(new Line2D.Float(xVal, tLut + 7.0f, xVal, bLut));
        }
        g2d.setStroke(new BasicStroke(1.0f));
        float offsetThick = (this.xAxisHistoRescaleRatio + 0.5f) / 2.0f;
        int separation = (int)(lutLength / (float)(g2d.getFontMetrics().stringWidth(String.valueOf(max)) * 3));
        separation = Math.max(separation, 1);
        float stepWindow = hRange / (float)separation;
        g2d.setPaint(Color.BLACK);
        Rectangle2D.Float rect = new Rectangle2D.Float();
        for (int i = 0; i <= separation; ++i) {
            float val = firstLevel + (float)i * stepWindow;
            float posX = x + (val - firstLevel) * spaceFactor;
            rect.setRect(posX - 2.0f, bLut - 1.0f, 2.0f, 7.0f);
            g2d.draw(rect);
        }
        rect.setRect(x - 2.0f - offsetThick, tLut + 5.0f, lutLength + 3.0f, 20.0f);
        g2d.draw(rect);
        g2d.setPaint(Color.WHITE);
        Object[] oldRenderingHints = GuiUtils.setRenderingHints(g2d, true, false, true);
        Line2D.Float line = new Line2D.Float();
        for (int i = 0; i <= separation; ++i) {
            float val = firstLevel + (float)i * stepWindow;
            float posX = x + (val - firstLevel) * spaceFactor;
            line.setLine(posX - 1.0f, bLut, posX - 1.0f, bLut + 5.0f);
            g2d.draw(line);
            String str = DecFormatter.allNumber(this.data.getLayer().pixelToRealValue(Float.valueOf(firstLevel + (float)i * stepWindow)));
            float offsetLabel = i == separation ? (float)g2d.getFontMetrics().stringWidth(str) - 5.0f : (float)g2d.getFontMetrics().stringWidth(str) / 2.0f;
            float xlabel = i == 0 ? posX / 2.0f : posX - offsetLabel;
            FontTools.paintFontOutline(g2d, str, xlabel, bLut + (float)midFontHeight);
        }
        rect.setRect(x - 1.0f - offsetThick, tLut + 6.0f, lutLength + 1.0f, 18.0f);
        g2d.draw(rect);
        Color yellow = GuiUtils.IconColor.ACTIONS_YELLOW.getColor();
        g2d.setPaint(yellow);
        g2d.setStroke(new BasicStroke(0.5f, 0, 0, 5.0f, new float[]{5.0f}, 0.0f));
        HistogramData.Model m = this.data.getColorModel();
        boolean bl = hls = HistogramData.Model.HLS.equals((Object)m) || HistogramData.Model.HSV.equals((Object)m);
        if (!hls) {
            boolean drawWl = false;
            float plow = x + (float)piLow * (float)binFactor * spaceFactor;
            float phigh = x + (float)piHigh * (float)binFactor * spaceFactor;
            if (low > windLevel.getLevelMin()) {
                drawWl = this.winLevelLine(g2d, midFontHeight, tLut, min, piLow, binFactor, line, plow);
            }
            if (high < windLevel.getLevelMax()) {
                g2d.setPaint(yellow);
                drawWl = this.winLevelLine(g2d, midFontHeight, tLut, min, piHigh, binFactor, line, phigh);
            }
            if (drawWl) {
                g2d.setPaint(yellow);
                line.setLine(plow, 20.0f, phigh, 20.0f);
                g2d.draw(line);
            }
        }
        GuiUtils.resetRenderingHints(g2d, oldRenderingHints);
    }

    private boolean winLevelLine(Graphics2D g2d, float midFontHeight, float tLut, double min, int piLow, double binFactor, Line2D.Float line, float plow) {
        line.setLine(plow, 20.0f, plow, tLut);
        g2d.draw(line);
        String label = DecFormatter.allNumber(this.data.getLayer().pixelToRealValue((double)piLow * binFactor + min));
        FontTools.paintFontOutline(g2d, label, plow - (float)g2d.getFontMetrics().stringWidth(label) / 2.0f, 20.0f + midFontHeight);
        return true;
    }

    public void setHistogram(HistogramData data, boolean accumulate, boolean logarithmic, boolean showIntensity) {
        this.data = data;
        this.accumulate = accumulate;
        this.logarithmic = logarithmic;
        this.showIntensity = showIntensity;
    }

    public void setWindLevelParameters(WindLevelParameters p) {
        this.data.setWindLevel(p);
    }

    public void updateZoom(boolean in) {
        this.zoom = in ? (this.zoom *= 1.4f) : (this.zoom *= 0.71428573f);
        this.repaint();
    }

    public boolean isLogarithmic() {
        return this.logarithmic;
    }

    public void setLogarithmic(boolean logarithmic) {
        boolean update = !Objects.equals(this.logarithmic, logarithmic);
        this.logarithmic = logarithmic;
        if (update) {
            this.zoom = 1.0f;
            this.repaint();
        }
    }

    public boolean isShowIntensity() {
        return this.showIntensity;
    }

    public void setShowIntensity(boolean showIntensity) {
        boolean update = !Objects.equals(this.showIntensity, showIntensity);
        this.showIntensity = showIntensity;
        if (update) {
            this.repaint();
        }
    }

    public boolean isAccumulate() {
        return this.accumulate;
    }

    public void setAccumulate(boolean accumulate) {
        boolean update = !Objects.equals(this.accumulate, accumulate);
        this.accumulate = accumulate;
        if (update) {
            this.zoom = 1.0f;
            this.repaint();
        }
    }

    public void resetDisplay() {
        this.zoom = 1.0f;
        this.accumulate = false;
        this.logarithmic = false;
        this.showIntensity = true;
        this.repaint();
    }

    public float getZoom() {
        return this.zoom;
    }

    public HistogramData getData() {
        return this.data;
    }

    public void saveHistogramInCSV(File csvOutputFile) {
        try (PrintWriter pw = new PrintWriter(csvOutputFile, StandardCharsets.UTF_8);){
            pw.println("Class,Occurrences");
            float[] histValues = this.data.getHistValues();
            WlParams windLevel = this.data.getWindLevel();
            double min = windLevel.getLevelMin();
            double max = windLevel.getLevelMax() + 1.0;
            double factor = (max - min) / (double)histValues.length;
            for (int i = 0; i < histValues.length; ++i) {
                int val = (int)Math.ceil((double)i * factor + min);
                int val2 = (int)Math.floor((double)(i + 1) * factor + min);
                StringBuilder buf = new StringBuilder();
                buf.append(val);
                if (val2 != val && (double)val2 < max) {
                    buf.append("...");
                    buf.append(val2);
                }
                buf.append(",");
                buf.append(histValues[i]);
                pw.println(buf);
            }
        }
        catch (IOException e) {
            LOGGER.error("Cannot save histogram values", (Throwable)e);
        }
    }

    private class InternalMouseListener
    implements MouseListener,
    MouseMotionListener {
        private Popup popup;
        final /* synthetic */ HistogramPanel this$0;

        private InternalMouseListener(HistogramPanel histogramPanel) {
            HistogramPanel histogramPanel2 = histogramPanel;
            Objects.requireNonNull(histogramPanel2);
            this.this$0 = histogramPanel2;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
            this.showPopup(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (this.popup != null) {
                this.popup.hide();
            }
        }

        private void showPopup(MouseEvent e) {
            float[] histValues = this.this$0.data.getHistValues();
            float lpos = (float)(e.getX() - 10) / this.this$0.xAxisHistoRescaleRatio;
            int i = Math.round(lpos);
            if (i >= 0 && i < histValues.length) {
                WlParams windLevel = this.this$0.data.getWindLevel();
                double min = windLevel.getLevelMin();
                double max = windLevel.getLevelMax() + 1.0;
                int val = (int)Math.ceil((double)i * (max - min) / (double)histValues.length + min);
                int val2 = (int)Math.floor((double)(i + 1) * (max - min) / (double)histValues.length + min);
                StringBuilder buf = new StringBuilder();
                buf.append("<html>");
                buf.append(Messages.getString("HistogramPanel.intensity"));
                buf.append(": ");
                buf.append(val);
                if (val2 != val && (double)val2 < max) {
                    buf.append("...");
                    buf.append(val2);
                }
                buf.append("<br>");
                buf.append(Messages.getString("HistogramPanel.pixels"));
                buf.append(": ");
                buf.append((int)histValues[i]);
                JLabel text = new JLabel(buf.toString());
                if (this.popup != null) {
                    this.popup.hide();
                }
                text.setBorder(GuiUtils.getEmptyBorder(3, 5, 3, 5));
                this.popup = PopupFactory.getSharedInstance().getPopup(e.getComponent(), text, e.getXOnScreen() + 15, e.getYOnScreen() - GuiUtils.getScaleLength(40));
                this.popup.show();
            } else if (this.popup != null) {
                this.popup.hide();
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            this.this$0.setCursor(DefaultView2d.CROSS_CURSOR);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            this.this$0.setCursor(DefaultView2d.DEFAULT_CURSOR);
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            this.showPopup(e);
        }

        @Override
        public void mouseMoved(MouseEvent e) {
        }
    }
}

