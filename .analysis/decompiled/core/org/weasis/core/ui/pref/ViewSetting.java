/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.osgi.service.prefs.Preferences
 */
package org.weasis.core.ui.pref;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import org.osgi.service.prefs.Preferences;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.service.BundlePreferences;
import org.weasis.core.api.util.FontItem;
import org.weasis.core.ui.editor.image.MeasureToolBar;
import org.weasis.core.ui.editor.image.dockable.MeasureTool;
import org.weasis.core.ui.model.graphic.Graphic;
import org.weasis.core.ui.model.utils.ImageStatistics;
import org.weasis.core.ui.model.utils.bean.Measurement;
import org.weasis.core.ui.pref.Monitor;

public class ViewSetting {
    public static final String PREFERENCE_NODE = "view2d.default";
    public static final FontItem DEFAULT_FONT = FontItem.SMALL_SEMIBOLD;
    private FontItem fontItem;
    private boolean drawOnlyOnce = true;
    private boolean filled;
    private float fillOpacity;
    private Color lineColor;
    private final SpinnerNumberModel spinnerModel = new SpinnerNumberModel(1, 1, 8, 1);
    private boolean basicStatistics = true;
    private boolean moreStatistics = true;
    private final List<Monitor> monitors = new ArrayList<Monitor>(2);

    public ViewSetting() {
        this.fontItem = DEFAULT_FONT;
        this.filled = Graphic.DEFAULT_FILLED;
        this.fillOpacity = Graphic.DEFAULT_FILL_OPACITY.floatValue();
        this.lineColor = Graphic.DEFAULT_COLOR;
        this.spinnerModel.addChangeListener(changeEvent -> {
            Object patt0$temp = this.spinnerModel.getValue();
            if (patt0$temp instanceof Integer) {
                Integer intVal = (Integer)patt0$temp;
                this.setLineWidth(intVal);
                MeasureTool.updateMeasureProperties();
            }
        });
    }

    public void applyPreferences(Preferences prefs) {
        if (prefs != null) {
            Preferences p = prefs.node(PREFERENCE_NODE);
            Preferences font = p.node("font");
            String fontKey = font.get("type", DEFAULT_FONT.getKey());
            this.fontItem = FontItem.getFontItem(fontKey, DEFAULT_FONT);
            Preferences draw = p.node("drawing");
            this.drawOnlyOnce = draw.getBoolean("once", this.drawOnlyOnce);
            int lineWidth = draw.getInt("width", Graphic.DEFAULT_LINE_THICKNESS.intValue());
            this.setLineWidth(lineWidth);
            int rgb = draw.getInt("color", Graphic.DEFAULT_COLOR.getRGB());
            this.lineColor = new Color(rgb, true);
            this.filled = draw.getBoolean("fill", Graphic.DEFAULT_FILLED.booleanValue());
            this.fillOpacity = draw.getFloat("fillOpacity", Graphic.DEFAULT_FILL_OPACITY.floatValue());
            Preferences stats = p.node("statistics");
            this.basicStatistics = stats.getBoolean("basic", this.basicStatistics);
            this.moreStatistics = stats.getBoolean("more", this.moreStatistics);
            ImageStatistics.IMAGE_PIXELS.setComputed(this.basicStatistics);
            ImageStatistics.IMAGE_MIN.setComputed(this.basicStatistics);
            ImageStatistics.IMAGE_MAX.setComputed(this.basicStatistics);
            ImageStatistics.IMAGE_MEAN.setComputed(this.basicStatistics);
            ImageStatistics.IMAGE_MEDIAN.setComputed(this.moreStatistics);
            ImageStatistics.IMAGE_STD.setComputed(this.moreStatistics);
            ImageStatistics.IMAGE_SKEW.setComputed(this.moreStatistics);
            ImageStatistics.IMAGE_KURTOSIS.setComputed(this.moreStatistics);
            ImageStatistics.IMAGE_ENTROPY.setComputed(this.moreStatistics);
            String labels = stats.get("label", null);
            if (labels != null) {
                String[] items;
                block0: for (String item : items = labels.split(",")) {
                    String[] val = item.split(":");
                    if (val.length != 2) continue;
                    Measurement[] measurementArray = ImageStatistics.ALL_MEASUREMENTS;
                    int n = measurementArray.length;
                    for (int i = 0; i < n; ++i) {
                        Measurement m = measurementArray[i];
                        if (!val[0].equals(String.valueOf(m.getId()))) continue;
                        m.setGraphicLabel(ViewSetting.isTrueValue(val[1]));
                        continue block0;
                    }
                }
            }
            for (int i = 1; i < MeasureToolBar.getMeasureGraphicList().size(); ++i) {
                String[] items;
                Preferences gpref;
                Graphic graph = MeasureToolBar.getMeasureGraphicList().get(i);
                List<Measurement> list = graph.getMeasurementList();
                if (list == null || list.isEmpty() || (labels = (gpref = p.node(graph.getClass().getSimpleName())).get("label", null)) == null) continue;
                block3: for (String item : items = labels.split(",")) {
                    String[] val = item.split(":");
                    if (val.length != 2) continue;
                    for (Measurement m : list) {
                        if (!val[0].equals(String.valueOf(m.getId()))) continue;
                        m.setGraphicLabel(ViewSetting.isTrueValue(val[1]));
                        continue block3;
                    }
                }
            }
        }
    }

    public List<Monitor> getMonitors() {
        return new ArrayList<Monitor>(this.monitors);
    }

    public Monitor getMonitor(GraphicsDevice device) {
        for (Monitor m : this.monitors) {
            if (m.getGraphicsDevice() != device) continue;
            return m;
        }
        return null;
    }

    public void initMonitors() {
        GraphicsDevice[] gd;
        this.monitors.clear();
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (GraphicsDevice graphicsDevice : gd = ge.getScreenDevices()) {
            GraphicsConfiguration config = graphicsDevice.getDefaultConfiguration();
            if (config == null || graphicsDevice.getType() != 0) continue;
            Monitor monitor = new Monitor(graphicsDevice);
            String screen = ViewSetting.buildScreenItem(monitor);
            double pitch = GuiUtils.getUICore().getLocalPersistence().getDoubleProperty(screen, 0.0);
            monitor.setRealScaleFactor(pitch);
            this.monitors.add(monitor);
        }
    }

    static String buildScreenItem(Monitor monitor) {
        StringBuilder buf = new StringBuilder("screen.");
        buf.append(monitor.getMonitorID());
        Rectangle b = monitor.getBounds();
        buf.append(".");
        buf.append(b.width);
        buf.append("x");
        buf.append(b.height);
        buf.append(".pitch");
        return buf.toString();
    }

    private static boolean isTrueValue(String val) {
        return "1".equals(val.trim());
    }

    private static void writeLabels(StringBuilder buffer, Measurement m) {
        buffer.append(m.getId());
        buffer.append(":");
        buffer.append(m.getGraphicLabel() != false ? "1" : "0");
    }

    public void savePreferences(Preferences prefs) {
        if (prefs != null) {
            int i;
            Preferences p = prefs.node(PREFERENCE_NODE);
            Preferences font = p.node("font");
            BundlePreferences.putStringPreferences(font, "type", this.getFontItem().getKey());
            Preferences draw = p.node("drawing");
            BundlePreferences.putBooleanPreferences(draw, "once", this.drawOnlyOnce);
            BundlePreferences.putIntPreferences(draw, "width", this.getLineWidth());
            BundlePreferences.putIntPreferences(draw, "color", this.lineColor.getRGB());
            BundlePreferences.putBooleanPreferences(draw, "fill", this.filled);
            BundlePreferences.putFloatPreferences(draw, "fillOpacity", this.fillOpacity);
            Preferences stats = p.node("statistics");
            BundlePreferences.putBooleanPreferences(stats, "basic", this.basicStatistics);
            BundlePreferences.putBooleanPreferences(stats, "more", this.moreStatistics);
            StringBuilder buffer = new StringBuilder();
            ViewSetting.writeLabels(buffer, ImageStatistics.ALL_MEASUREMENTS[0]);
            for (i = 1; i < ImageStatistics.ALL_MEASUREMENTS.length; ++i) {
                buffer.append(",");
                ViewSetting.writeLabels(buffer, ImageStatistics.ALL_MEASUREMENTS[i]);
            }
            BundlePreferences.putStringPreferences(stats, "label", buffer.toString());
            for (i = 1; i < MeasureToolBar.getMeasureGraphicList().size(); ++i) {
                Graphic graph = MeasureToolBar.getMeasureGraphicList().get(i);
                List<Measurement> list = graph.getMeasurementList();
                if (list == null || list.isEmpty()) continue;
                Preferences gpref = p.node(graph.getClass().getSimpleName());
                buffer = new StringBuilder();
                ViewSetting.writeLabels(buffer, (Measurement)list.getFirst());
                for (int j = 1; j < list.size(); ++j) {
                    buffer.append(",");
                    ViewSetting.writeLabels(buffer, list.get(j));
                }
                BundlePreferences.putStringPreferences(gpref, "label", buffer.toString());
            }
        }
    }

    public void setFontItem(FontItem fontItem) {
        this.fontItem = fontItem == null ? DEFAULT_FONT : fontItem;
    }

    public Font getFont() {
        return this.getFontItem().getFont();
    }

    public FontItem getFontItem() {
        FontItem item = this.fontItem;
        return item == null ? DEFAULT_FONT : this.fontItem;
    }

    public void setDrawOnlyOnce(boolean drawOnlyOnce) {
        this.drawOnlyOnce = drawOnlyOnce;
    }

    public boolean isDrawOnlyOnce() {
        return this.drawOnlyOnce;
    }

    public Color getLineColor() {
        return this.lineColor;
    }

    public void setLineColor(Color lineColor) {
        this.lineColor = lineColor;
    }

    public int getLineWidth() {
        return (Integer)this.spinnerModel.getValue();
    }

    public void setLineWidth(int lineWidth) {
        this.spinnerModel.setValue(Math.max(1, Math.min(8, lineWidth)));
    }

    public boolean isFilled() {
        return this.filled;
    }

    public void setFilled(boolean filled) {
        this.filled = filled;
    }

    public float getFillOpacity() {
        return this.fillOpacity;
    }

    public void setFillOpacity(float fillOpacity) {
        this.fillOpacity = fillOpacity;
    }

    public boolean isBasicStatistics() {
        return this.basicStatistics;
    }

    public void setBasicStatistics(boolean basicStatistics) {
        this.basicStatistics = basicStatistics;
    }

    public boolean isMoreStatistics() {
        return this.moreStatistics;
    }

    public void setMoreStatistics(boolean moreStatistics) {
        this.moreStatistics = moreStatistics;
    }

    public void initLineWidthSpinner(JSpinner spinner) {
        spinner.setModel(this.spinnerModel);
        GuiUtils.formatCheckAction(spinner);
    }
}

