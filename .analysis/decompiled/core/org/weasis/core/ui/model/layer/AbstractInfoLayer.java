/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.osgi.service.prefs.Preferences
 *  org.weasis.core.util.StringUtil
 *  org.weasis.opencv.op.lut.ColorLut
 *  org.weasis.opencv.op.lut.WlParams
 */
package org.weasis.core.ui.model.layer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.Icon;
import org.osgi.service.prefs.Preferences;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.gui.util.DecFormatter;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.image.ImageOpNode;
import org.weasis.core.api.image.OpManager;
import org.weasis.core.api.image.WindowOp;
import org.weasis.core.api.image.util.Unit;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.api.service.BundlePreferences;
import org.weasis.core.api.util.FontTools;
import org.weasis.core.ui.editor.image.DisplayByteLut;
import org.weasis.core.ui.editor.image.HistogramData;
import org.weasis.core.ui.editor.image.HistogramView;
import org.weasis.core.ui.editor.image.PixelInfo;
import org.weasis.core.ui.editor.image.SynchData;
import org.weasis.core.ui.editor.image.ViewButton;
import org.weasis.core.ui.editor.image.ViewCanvas;
import org.weasis.core.ui.model.layer.LayerAnnotation;
import org.weasis.core.ui.model.layer.LayerItem;
import org.weasis.core.ui.model.layer.LayerType;
import org.weasis.core.ui.model.utils.imp.DefaultUUID;
import org.weasis.core.util.StringUtil;
import org.weasis.opencv.op.lut.ColorLut;
import org.weasis.opencv.op.lut.WlParams;

public abstract class AbstractInfoLayer<E extends ImageElement>
extends DefaultUUID
implements LayerAnnotation<E> {
    protected static final Color highlight = new Color(255, 153, 153);
    public static final AtomicBoolean applyToAllView = new AtomicBoolean(true);
    protected static final Map<LayerItem, Boolean> defaultDisplayPreferences = new EnumMap<LayerItem, Boolean>(LayerItem.class);
    protected static final int P_BORDER = 10;
    protected final Map<LayerItem, Boolean> displayPreferences = new EnumMap<LayerItem, Boolean>(LayerItem.class);
    protected boolean visible = true;
    protected static final Color color;
    protected final ViewCanvas<E> view2DPane;
    protected PixelInfo pixelInfo = null;
    protected final Rectangle pixelInfoBound;
    protected final Rectangle preloadingProgressBound;
    protected int border = 10;
    protected double thickLength = 15.0;
    protected boolean showBottomScale = true;
    protected String name;
    protected boolean useGlobalPreferences;
    private final Map<LayerAnnotation.Position, Point2D> positions = new EnumMap<LayerAnnotation.Position, Point2D>(LayerAnnotation.Position.class);

    protected AbstractInfoLayer(ViewCanvas<E> view2DPane) {
        this(view2DPane, true);
    }

    protected AbstractInfoLayer(ViewCanvas<E> view2DPane, boolean useGlobalPreferences) {
        this.view2DPane = Objects.requireNonNull(view2DPane);
        this.pixelInfoBound = new Rectangle();
        this.preloadingProgressBound = new Rectangle();
        this.useGlobalPreferences = useGlobalPreferences;
        this.positions.put(LayerAnnotation.Position.TopLeft, new Point2D.Double(0.0, 0.0));
        this.positions.put(LayerAnnotation.Position.TopRight, new Point2D.Double(0.0, 0.0));
        this.positions.put(LayerAnnotation.Position.BottomLeft, new Point2D.Double(0.0, 0.0));
        this.positions.put(LayerAnnotation.Position.BottomRight, new Point2D.Double(0.0, 0.0));
    }

    @Override
    public Point2D getPosition(LayerAnnotation.Position position) {
        return this.positions.get((Object)position);
    }

    @Override
    public void setPosition(LayerAnnotation.Position position, double x, double y) {
        Point2D p = this.positions.get((Object)position);
        if (p != null) {
            p.setLocation(x, y);
        }
    }

    protected void setDefaultCornerPositions(Rectangle bound) {
        this.setPosition(LayerAnnotation.Position.TopLeft, this.border, this.border);
        this.setPosition(LayerAnnotation.Position.TopRight, (double)bound.width - (double)this.border, this.border);
        this.setPosition(LayerAnnotation.Position.BottomRight, (double)bound.width - (double)this.border, (double)bound.height - (double)this.border);
    }

    protected void drawExtendedActions(Graphics2D g2d) {
        if (this.view2DPane.getViewButtons().isEmpty()) {
            return;
        }
        int space = GuiUtils.getScaleLength(10);
        Point2D topRight = this.getPosition(LayerAnnotation.Position.TopRight);
        Point2D.Double midy = this.calculateMiddleYPosition(space, topRight);
        SynchData synchData = (SynchData)this.view2DPane.getActionValue(ActionW.SYNCH_LINK.cmd());
        boolean tile = synchData != null && SynchData.Mode.TILE.equals((Object)synchData.getMode());
        for (ViewButton b : this.view2DPane.getViewButtons()) {
            if (!this.shouldDrawButton(b, tile)) continue;
            this.positionAndDrawButton(g2d, b, space, topRight, midy);
        }
    }

    private Point2D.Double calculateMiddleYPosition(int space, Point2D topRight) {
        int height = 0;
        for (ViewButton b : this.view2DPane.getViewButtons()) {
            if (!b.isVisible() || b.getPosition() != 13) continue;
            height += b.getIcon().getIconHeight() + space;
        }
        return new Point2D.Double(topRight.getX(), (double)this.view2DPane.getJComponent().getHeight() * 0.5 - (double)(height - space) * 0.5);
    }

    protected boolean shouldDrawButton(ViewButton b, boolean tile) {
        return b.isVisible() && (!tile || !ActionW.KO_SELECTION.getTitle().equals(b.getName()));
    }

    protected void positionAndDrawButton(Graphics2D g2d, ViewButton b, int space, Point2D topRight, Point2D.Double midy) {
        Icon icon = b.getIcon();
        int position = b.getPosition();
        switch (position) {
            case 13: {
                b.x = midy.x - (double)icon.getIconWidth();
                b.y = midy.y;
                midy.y += (double)(icon.getIconHeight() + space);
                break;
            }
            case 12: {
                b.x = topRight.getX() - (double)icon.getIconWidth();
                b.y = topRight.getY();
                topRight.setLocation(topRight.getX() - (double)icon.getIconWidth() - (double)space, topRight.getY());
                break;
            }
            case 14: {
                Point2D bottomRight = this.getPosition(LayerAnnotation.Position.BottomRight);
                b.x = bottomRight.getX() - (double)icon.getIconWidth();
                b.y = bottomRight.getY() - (double)icon.getIconHeight();
                bottomRight.setLocation(bottomRight.getX() - (double)icon.getIconWidth() - (double)space, bottomRight.getY());
                break;
            }
            case 18: {
                Point2D topLeft = this.getPosition(LayerAnnotation.Position.TopLeft);
                b.x = topLeft.getX();
                b.y = topLeft.getY();
                topLeft.setLocation(topLeft.getX() + (double)icon.getIconWidth() + (double)space, topLeft.getY());
                break;
            }
            case 16: {
                Point2D bottomLeft = this.getPosition(LayerAnnotation.Position.BottomLeft);
                b.x = bottomLeft.getX();
                b.y = bottomLeft.getY() - (double)icon.getIconHeight();
                bottomLeft.setLocation(bottomLeft.getX() + (double)icon.getIconWidth() + (double)space, bottomLeft.getY());
            }
        }
        ViewButton.drawButtonBackground(g2d, this.view2DPane.getJComponent(), b, icon);
    }

    public ViewCanvas<E> getView2DPane() {
        return this.view2DPane;
    }

    protected void setLayerValue(Map<LayerItem, Boolean> prefMap, LayerItem item) {
        prefMap.put(item, this.getDisplayPreferences(item));
    }

    public static Map<LayerItem, Boolean> getDefaultDisplayPreferences() {
        return defaultDisplayPreferences;
    }

    public static void applyPreferences(Preferences prefs) {
        if (prefs != null) {
            Preferences p = prefs.node("view2d.default");
            Preferences pref = p.node("infolayer");
            applyToAllView.set(pref.getBoolean("allViews", true));
            for (Map.Entry<LayerItem, Boolean> v : defaultDisplayPreferences.entrySet()) {
                v.setValue(pref.getBoolean(v.getKey().getKey(), v.getValue().booleanValue()));
            }
        }
    }

    public static void savePreferences(Preferences prefs) {
        if (prefs != null) {
            Preferences p = prefs.node("view2d.default");
            Preferences pref = p.node("infolayer");
            BundlePreferences.putBooleanPreferences(pref, "allViews", applyToAllView.get());
            for (Map.Entry<LayerItem, Boolean> v : defaultDisplayPreferences.entrySet()) {
                BundlePreferences.putBooleanPreferences(pref, v.getKey().getKey(), v.getValue());
            }
        }
    }

    public static Boolean setDefaultDisplayPreferencesValue(LayerItem item, Boolean selected) {
        Boolean selected2 = Optional.ofNullable(defaultDisplayPreferences.get((Object)item)).orElse(Boolean.FALSE);
        defaultDisplayPreferences.put(item, selected);
        return !Objects.equals(selected, selected2);
    }

    @Override
    public void resetToDefault() {
        this.displayPreferences.putAll(defaultDisplayPreferences);
    }

    @Override
    public boolean isShowBottomScale() {
        return this.showBottomScale;
    }

    @Override
    public void setShowBottomScale(Boolean showBottomScale) {
        this.showBottomScale = showBottomScale;
    }

    @Override
    public Boolean getVisible() {
        return this.visible;
    }

    @Override
    public void setVisible(Boolean visible) {
        this.visible = Optional.ofNullable(visible).orElse(this.getType().getVisible());
    }

    @Override
    public Integer getLevel() {
        return this.getType().getLevel();
    }

    @Override
    public void setLevel(Integer i) {
    }

    @Override
    public int getBorder() {
        return this.border;
    }

    @Override
    public void setBorder(int border) {
        this.border = border;
    }

    @Override
    public LayerType getType() {
        return LayerType.IMAGE_ANNOTATION;
    }

    @Override
    public void setType(LayerType type) {
    }

    @Override
    public void setName(String layerName) {
        this.name = layerName;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return Optional.ofNullable(this.getName()).orElse(this.getType().getDefaultName());
    }

    @Override
    public boolean getDisplayPreferences(LayerItem item) {
        if (this.useGlobalPreferences && applyToAllView.get()) {
            return Optional.ofNullable(defaultDisplayPreferences.get((Object)item)).orElse(Boolean.FALSE);
        }
        return Optional.ofNullable(this.displayPreferences.getOrDefault((Object)item, defaultDisplayPreferences.get((Object)item))).orElse(Boolean.FALSE);
    }

    @Override
    public boolean setDisplayPreferencesValue(LayerItem displayItem, boolean selected) {
        Boolean selected2 = this.getDisplayPreferences(displayItem);
        this.displayPreferences.put(displayItem, selected);
        return !Objects.equals(selected, selected2);
    }

    @Override
    public Rectangle getPreloadingProgressBound() {
        return this.preloadingProgressBound;
    }

    @Override
    public Rectangle getPixelInfoBound() {
        return this.pixelInfoBound;
    }

    @Override
    public void setPixelInfo(PixelInfo pixelInfo) {
        this.pixelInfo = pixelInfo;
    }

    @Override
    public PixelInfo getPixelInfo() {
        return this.pixelInfo;
    }

    public Rectangle2D getOutLine(Line2D l) {
        Rectangle2D r = l.getBounds2D();
        r.setFrame(r.getX() - 1.0, r.getY() - 1.0, r.getWidth() + 2.0, r.getHeight() + 2.0);
        return r;
    }

    protected double getPixelMin() {
        Object img = this.view2DPane.getImage();
        if (img != null) {
            return ((ImageElement)img).getPixelMin();
        }
        return 0.0;
    }

    protected double getPixelMax() {
        Object img = this.view2DPane.getImage();
        if (img != null) {
            return ((ImageElement)img).getPixelMax();
        }
        return 0.0;
    }

    public void drawLUT(Graphics2D g2, Rectangle bound, float midFontHeight) {
        WlParams p = this.getWinLeveParameters();
        if (p != null && bound.height > 350) {
            double level;
            DisplayByteLut lut = this.getLut(p);
            byte[][] table = lut.getByteLut().lutTable();
            float length = table[0].length;
            int width = 0;
            for (ViewButton b : this.view2DPane.getViewButtons()) {
                int w;
                if (!b.isVisible() || b.getPosition() != 13 || (w = b.getIcon().getIconWidth() + 5) <= width) continue;
                width = w;
            }
            float x = (float)bound.width - 30.0f - (float)width;
            float y = (float)bound.height / 2.0f - length / 2.0f;
            g2.setPaint(Color.BLACK);
            Rectangle2D.Float rect = new Rectangle2D.Float(x - 11.0f, y - 2.0f, 12.0f, 2.0f);
            g2.draw(rect);
            int separation = 4;
            float step = length / (float)separation;
            for (int i = 1; i < separation; ++i) {
                float posY = y + (float)i * step;
                rect.setRect(x - 6.0f, posY - 1.0f, 7.0f, 2.0f);
                g2.draw(rect);
            }
            rect.setRect(x - 11.0f, y + length, 12.0f, 2.0f);
            g2.draw(rect);
            rect.setRect(x - 2.0f, y - 2.0f, 23.0f, length + 4.0f);
            g2.draw(rect);
            g2.setPaint(Color.WHITE);
            double pixMin = this.getPixelMin();
            double pixMax = this.getPixelMax();
            HistogramData data = new HistogramData(new float[0], lut, 0, null, p, pixMin, pixMax, this.view2DPane.getMeasurableLayer());
            data.updateVoiLut(this.view2DPane);
            double binFactor = (pixMax - pixMin) / (double)(length - 1.0f);
            double stepWindow = (pixMax - pixMin) / (double)separation;
            float shiftY = midFontHeight / 2.0f - (float)g2.getFontMetrics().getDescent();
            Line2D.Float line = new Line2D.Float();
            for (int i = 0; i <= separation; ++i) {
                float posY = y + (float)i * step;
                line.setLine(x - 5.0f, posY, x - 1.0f, posY);
                g2.draw(line);
                level = data.getLayer().pixelToRealValue((double)(separation - i) * stepWindow + pixMin);
                String str = DecFormatter.allNumber(level);
                FontTools.paintFontOutline(g2, str, x - (float)g2.getFontMetrics().stringWidth(str) - 7.0f, posY + shiftY);
            }
            rect.setRect(x - 1.0f, y - 1.0f, 21.0f, length + 2.0f);
            g2.draw(rect);
            int limit = table[0].length - 1;
            for (int k = 0; k <= limit; ++k) {
                level = data.getLayer().pixelToRealValue((double)(limit - k) * binFactor + pixMin);
                Color cLut = data.getFinalVoiLutColor(level);
                g2.setPaint(cLut);
                rect.setRect(x, y + (float)k, 19.0f, 1.0f);
                g2.draw(rect);
            }
        }
    }

    private WlParams getWinLeveParameters() {
        ImageOpNode imageOpNode;
        OpManager dispOp;
        Optional<ImageOpNode> wlOp;
        if (this.view2DPane != null && (wlOp = (dispOp = this.view2DPane.getDisplayOpManager()).getNode(WindowOp.OP_NAME)).isPresent() && (imageOpNode = wlOp.get()) instanceof WindowOp) {
            WindowOp windowOp = (WindowOp)imageOpNode;
            return windowOp.getWindLevelParameters();
        }
        return null;
    }

    private DisplayByteLut getLut(WlParams p) {
        int channels;
        DisplayByteLut lut = null;
        if (this.view2DPane != null && (channels = this.view2DPane.getSourceImage().channels()) == 1) {
            DisplayByteLut disLut = HistogramView.buildDisplayByteLut(this.view2DPane);
            if (disLut == null) {
                disLut = new DisplayByteLut(ColorLut.GRAY.getByteLut());
            }
            disLut.setInvert(p.isInverseLut());
            lut = disLut;
        }
        if (lut == null) {
            lut = new DisplayByteLut(ColorLut.GRAY.getByteLut());
        }
        return lut;
    }

    public void drawScale(Graphics2D g2d, Rectangle bound, float fontHeight, ImageProperties imgProps) {
        double scaleSizeY;
        if (imgProps == null) {
            return;
        }
        double zoomFactor = this.view2DPane.getViewModel().getViewScale();
        double scale = imgProps.pixelSize / zoomFactor;
        double scaleSizex = AbstractInfoLayer.adjustShowScale(scale, (int)Math.min(zoomFactor * (double)imgProps.width * imgProps.rescaleX, (double)bound.width / 2.0));
        if (this.showBottomScale && scaleSizex > 50.0) {
            double secBar;
            int k;
            int i;
            int i2;
            Unit[] unit = new Unit[]{imgProps.pixelUnit};
            Object str = AbstractInfoLayer.adjustLengthDisplay(scaleSizex * scale, unit);
            g2d.setStroke(new BasicStroke(1.0f));
            g2d.setPaint(Color.BLACK);
            double posx = (double)bound.width / 2.0 - scaleSizex / 2.0;
            double posy = (double)(bound.height - this.border) - 1.5;
            Line2D.Double line = new Line2D.Double(posx, posy, posx + scaleSizex, posy);
            g2d.draw(this.getOutLine(line));
            ((Line2D)line).setLine(posx, posy - this.thickLength, posx, posy);
            g2d.draw(this.getOutLine(line));
            ((Line2D)line).setLine(posx + scaleSizex, posy - this.thickLength, posx + scaleSizex, posy);
            g2d.draw(this.getOutLine(line));
            int divisor = !((String)str).contains("5") ? (!((String)str).contains("2") ? 10 : 2) : 5;
            double midThick = this.thickLength * 2.0 / 3.0;
            double smallThick = this.thickLength / 3.0;
            double divSquare = scaleSizex / (double)divisor;
            for (i2 = 1; i2 < divisor; ++i2) {
                ((Line2D)line).setLine(posx + divSquare * (double)i2, posy, posx + divSquare * (double)i2, posy - midThick);
                g2d.draw(this.getOutLine(line));
            }
            if (divSquare > 90.0) {
                double secondSquare = divSquare / 10.0;
                for (i = 0; i < divisor; ++i) {
                    for (k = 1; k < 10; ++k) {
                        secBar = posx + divSquare * (double)i + secondSquare * (double)k;
                        ((Line2D)line).setLine(secBar, posy, secBar, posy - smallThick);
                        g2d.draw(this.getOutLine(line));
                    }
                }
            }
            g2d.setPaint(Color.white);
            ((Line2D)line).setLine(posx, posy, posx + scaleSizex, posy);
            g2d.draw(line);
            ((Line2D)line).setLine(posx, posy - this.thickLength, posx, posy);
            g2d.draw(line);
            ((Line2D)line).setLine(posx + scaleSizex, posy - this.thickLength, posx + scaleSizex, posy);
            g2d.draw(line);
            for (i2 = 0; i2 < divisor; ++i2) {
                ((Line2D)line).setLine(posx + divSquare * (double)i2, posy, posx + divSquare * (double)i2, posy - midThick);
                g2d.draw(line);
            }
            if (divSquare > 90.0) {
                double secondSquare = divSquare / 10.0;
                for (i = 0; i < divisor; ++i) {
                    for (k = 1; k < 10; ++k) {
                        secBar = posx + divSquare * (double)i + secondSquare * (double)k;
                        ((Line2D)line).setLine(secBar, posy, secBar, posy - smallThick);
                        g2d.draw(line);
                    }
                }
            }
            if (StringUtil.hasText((CharSequence)imgProps.pixelDescription)) {
                FontTools.paintFontOutline(g2d, imgProps.pixelDescription, (float)(posx + scaleSizex + 5.0), (float)posy - fontHeight);
            }
            str = (String)str + " " + unit[0].getAbbreviation();
            FontTools.paintFontOutline(g2d, (String)str, (float)(posx + scaleSizex + 5.0), (float)posy);
        }
        if ((scaleSizeY = AbstractInfoLayer.adjustShowScale(scale, (int)Math.min(zoomFactor * (double)imgProps.height * imgProps.rescaleY, (double)bound.height / 2.0))) > 30.0) {
            double secBar;
            int k;
            int i;
            Unit[] unit = new Unit[]{imgProps.pixelUnit};
            String str = AbstractInfoLayer.adjustLengthDisplay(scaleSizeY * scale, unit);
            float strokeWidth = (float)g2d.getFont().getSize() / 15.0f;
            strokeWidth = Math.max(strokeWidth, 1.0f);
            g2d.setStroke(new BasicStroke(strokeWidth));
            g2d.setPaint(Color.black);
            double posx = (float)this.border - 1.5f;
            double posy = (double)bound.height / 2.0 - scaleSizeY / 2.0;
            Line2D.Double line = new Line2D.Double(posx, posy, posx, posy + scaleSizeY);
            g2d.draw(this.getOutLine(line));
            ((Line2D)line).setLine(posx, posy, posx + this.thickLength, posy);
            g2d.draw(this.getOutLine(line));
            ((Line2D)line).setLine(posx, posy + scaleSizeY, posx + this.thickLength, posy + scaleSizeY);
            g2d.draw(this.getOutLine(line));
            int divisor = !str.contains("5") ? (!str.contains("2") ? 10 : 2) : 5;
            double divSquare = scaleSizeY / (double)divisor;
            double midThick = this.thickLength * 2.0 / 3.0;
            double smallThick = this.thickLength / 3.0;
            for (int i3 = 0; i3 < divisor; ++i3) {
                ((Line2D)line).setLine(posx, posy + divSquare * (double)i3, posx + midThick, posy + divSquare * (double)i3);
                g2d.draw(this.getOutLine(line));
            }
            if (divSquare > 90.0) {
                double secondSquare = divSquare / 10.0;
                for (i = 0; i < divisor; ++i) {
                    for (k = 1; k < 10; ++k) {
                        secBar = posy + divSquare * (double)i + secondSquare * (double)k;
                        ((Line2D)line).setLine(posx, secBar, posx + smallThick, secBar);
                        g2d.draw(this.getOutLine(line));
                    }
                }
            }
            g2d.setPaint(Color.WHITE);
            ((Line2D)line).setLine(posx, posy, posx, posy + scaleSizeY);
            g2d.draw(line);
            ((Line2D)line).setLine(posx, posy, posx + this.thickLength, posy);
            g2d.draw(line);
            ((Line2D)line).setLine(posx, posy + scaleSizeY, posx + this.thickLength, posy + scaleSizeY);
            g2d.draw(line);
            for (int i4 = 0; i4 < divisor; ++i4) {
                ((Line2D)line).setLine(posx, posy + divSquare * (double)i4, posx + midThick, posy + divSquare * (double)i4);
                g2d.draw(line);
            }
            if (divSquare > 90.0) {
                double secondSquare = divSquare / 10.0;
                for (i = 0; i < divisor; ++i) {
                    for (k = 1; k < 10; ++k) {
                        secBar = posy + divSquare * (double)i + secondSquare * (double)k;
                        ((Line2D)line).setLine(posx, secBar, posx + smallThick, secBar);
                        g2d.draw(line);
                    }
                }
            }
            FontTools.paintFontOutline(g2d, str + " " + unit[0].getAbbreviation(), (int)posx, (int)(posy - (double)(5.0f * strokeWidth)));
        }
    }

    public static double adjustShowScale(double ratio, int maxLength) {
        int digits = (int)(Math.log((double)maxLength * ratio) / Math.log(10.0) + 1.0);
        double scaleLength = Math.pow(10.0, digits);
        double scaleSize = scaleLength / ratio;
        int loop = 0;
        while ((int)scaleSize > maxLength) {
            scaleLength /= AbstractInfoLayer.findGeometricSuite(scaleLength);
            scaleSize = scaleLength / ratio;
            if (++loop <= 50) continue;
            return 0.0;
        }
        return scaleSize;
    }

    public static double findGeometricSuite(double length) {
        int shift = (int)(Math.log(length) / Math.log(10.0) + 0.1);
        int firstDigit = (int)(length / Math.pow(10.0, shift) + 0.5);
        if (firstDigit == 5) {
            return 2.5;
        }
        return 2.0;
    }

    public static String adjustLengthDisplay(double scaleLength, Unit[] unit) {
        double adjustScaleLength = scaleLength;
        Unit adjustUnit = unit[0];
        if (scaleLength < 1.0) {
            Unit down = adjustUnit;
            while ((down = (Unit)down.getNextSmallerUnit().orElse(null)) != null) {
                double length = scaleLength * down.getConversionRatio(unit[0].getFactorToMeters());
                if (!(length > 1.0)) continue;
                adjustUnit = down;
                adjustScaleLength = length;
                break;
            }
        } else if (scaleLength > 10.0) {
            double length;
            Unit up = adjustUnit;
            while ((up = (Unit)up.getNextLargerUnit().orElse(null)) != null && !((length = scaleLength * up.getConversionRatio(unit[0].getFactorToMeters())) < 1.0)) {
                adjustUnit = up;
                adjustScaleLength = length;
            }
        }
        unit[0] = adjustUnit;
        if (adjustScaleLength < 1.0) {
            return adjustScaleLength < 0.001 ? DecFormatter.scientificFormat(adjustScaleLength) : DecFormatter.fourDecimal(adjustScaleLength);
        }
        return adjustScaleLength > 50000.0 ? DecFormatter.scientificFormat(adjustScaleLength) : DecFormatter.twoDecimal(adjustScaleLength);
    }

    static {
        for (LayerItem item : LayerItem.values()) {
            defaultDisplayPreferences.put(item, item.isVisible());
        }
        color = Color.yellow;
    }

    public record ImageProperties(int width, int height, double pixelSize, double rescaleX, double rescaleY, Unit pixelUnit, String pixelDescription) {
    }
}

