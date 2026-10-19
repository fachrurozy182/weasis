/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.formdev.flatlaf.FlatLaf
 *  com.formdev.flatlaf.ui.FlatUIUtils
 *  com.formdev.flatlaf.util.ColorFunctions
 *  com.formdev.flatlaf.util.UIScale
 */
package org.weasis.core.ui.tp.raven.datetime.component.time;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.ColorFunctions;
import com.formdev.flatlaf.util.UIScale;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Objects;
import javax.swing.JPanel;
import javax.swing.UIManager;
import org.weasis.core.ui.tp.raven.datetime.TimePicker;
import org.weasis.core.ui.tp.raven.datetime.component.time.AnimationChange;
import org.weasis.core.ui.tp.raven.datetime.component.time.TimeSelectionModel;
import org.weasis.core.ui.tp.raven.datetime.component.time.event.TimeActionListener;

public class PanelClock
extends JPanel {
    private final TimePicker timePicker;
    private final TimeActionListener timeActionListener;
    private boolean use24hour;
    private boolean hourSelectionView = true;
    private AnimationChange animationChange;
    private final int margin12h = 20;
    private final int margin24h = 50;
    private Color color;

    public void setHourSelectionView(boolean hourSelectionView) {
        if (this.hourSelectionView != hourSelectionView) {
            this.hourSelectionView = hourSelectionView;
            this.repaint();
            this.runAnimation(true);
        }
    }

    public void setHourSelectionViewImmediately(boolean hourSelectionView) {
        if (this.hourSelectionView != hourSelectionView) {
            this.hourSelectionView = hourSelectionView;
            this.repaint();
            this.runAnimation(false);
        }
    }

    public boolean isHourSelectionView() {
        return this.hourSelectionView;
    }

    public void setUse24hour(boolean use24hour) {
        if (this.use24hour != use24hour) {
            this.use24hour = use24hour;
        }
    }

    public boolean isUse24hour() {
        return this.use24hour;
    }

    public PanelClock(TimePicker timePicker, TimeActionListener timeActionListener) {
        this.timePicker = timePicker;
        this.timeActionListener = timeActionListener;
        this.init();
    }

    private void init() {
        this.animationChange = new AnimationChange(this);
        this.putClientProperty("FlatLaf.style", "border:5,15,5,15;background:null;foreground:contrast($Component.accentColor,$Panel.background,#fff)");
        MouseAdapter mouseAdapter = new MouseAdapter(this){
            final /* synthetic */ PanelClock this$0;
            {
                PanelClock panelClock = this$0;
                Objects.requireNonNull(panelClock);
                this.this$0 = panelClock;
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (this.this$0.isEnabled()) {
                    this.mouseChanged(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (this.this$0.isEnabled()) {
                    this.this$0.timeActionListener.selectionViewChanged(this.this$0.hourSelectionView);
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (this.this$0.isEnabled()) {
                    this.mouseChanged(e);
                }
            }

            private void mouseChanged(MouseEvent e) {
                int value = this.this$0.getValueOf(e.getPoint(), this.this$0.hourSelectionView);
                if (this.this$0.hourSelectionView) {
                    this.this$0.timePicker.getTimeSelectionModel().setHour(value);
                } else {
                    this.this$0.timePicker.getTimeSelectionModel().setMinute(value);
                }
            }
        };
        this.addMouseListener(mouseAdapter);
        this.addMouseMotionListener(mouseAdapter);
    }

    public void updateClock() {
        TimeSelectionModel timeSelectionModel = this.timePicker.getTimeSelectionModel();
        if (this.hourSelectionView) {
            this.animationChange.set(this.getAngleOf(timeSelectionModel.getHour(), true), this.getTargetMargin());
        } else {
            this.animationChange.set(this.getAngleOf(timeSelectionModel.getMinute(), false), this.getTargetMargin());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g.create();
        FlatUIUtils.setRenderingHints((Graphics)g2);
        Insets insets = this.getInsets();
        int width = this.getWidth() - (insets.left + insets.right);
        int height = this.getHeight() - (insets.top + insets.bottom);
        int size = Math.min(width, height);
        g2.translate(insets.left, insets.top);
        int x = (width - size) / 2;
        int y = (height - size) / 2;
        g2.setColor(this.getClockBackground());
        g2.fill(new Ellipse2D.Double(x, y, size, size));
        this.paintSelection(g2, x, y, size);
        this.paintClockNumber(g2, x, y, size);
        g2.dispose();
    }

    protected void paintSelection(Graphics2D g2, int x, int y, int size) {
        TimeSelectionModel timeSelectionModel = this.timePicker.getTimeSelectionModel();
        AffineTransform tran = g2.getTransform();
        size /= 2;
        float margin = UIScale.scale((float)this.animationChange.getMargin());
        float centerSize = UIScale.scale((float)8.0f);
        float lineSize = UIScale.scale((int)3);
        float selectSize = UIScale.scale((float)25.0f);
        float unselectSize = UIScale.scale((int)4);
        float lineHeight = (float)size - margin;
        Area area = new Area(new Ellipse2D.Float((float)(x + size) - centerSize / 2.0f, (float)(y + size) - centerSize / 2.0f, centerSize, centerSize));
        if (this.hourSelectionView && timeSelectionModel.getHour() != -1 || !this.hourSelectionView && timeSelectionModel.getMinute() != -1) {
            area.add(new Area(new RoundRectangle2D.Float((float)(x + size) - lineSize / 2.0f, (float)y + margin, lineSize, lineHeight, lineSize, lineSize)));
            area.add(new Area(new Ellipse2D.Float((float)(x + size) - selectSize / 2.0f, (float)y + margin - selectSize / 2.0f, selectSize, selectSize)));
            if (!this.hourSelectionView && !this.animationChange.isRunning() && timeSelectionModel.getMinute() % 5 != 0) {
                area.subtract(new Area(new Ellipse2D.Float((float)(x + size) - unselectSize / 2.0f, (float)y + margin - unselectSize / 2.0f, unselectSize, unselectSize)));
            }
        }
        g2.setColor(this.getSelectedColor());
        float angle = this.animationChange.getAngle();
        g2.rotate(Math.toRadians(angle), (double)x + (double)size, (double)y + (double)size);
        g2.fill(area);
        g2.setTransform(tran);
    }

    protected void paintClockNumber(Graphics2D g2, int x, int y, int size) {
        this.paintClockNumber(g2, x, y, size, 20, 0, this.hourSelectionView ? 1 : 5);
        if (this.hourSelectionView && this.use24hour) {
            this.paintClockNumber(g2, x, y, size, 50, 12, 1);
        }
    }

    protected void paintClockNumber(Graphics2D g2, int x, int y, int size, int margin, int start, int add) {
        TimeSelectionModel timeSelectionModel = this.timePicker.getTimeSelectionModel();
        int mg = UIScale.scale((int)margin);
        float center = (float)size / 2.0f;
        float angle = 30.0f;
        for (int i = 1; i <= 12; ++i) {
            int minute;
            int hour;
            float ag = angle * (float)i - 90.0f;
            int value = this.fixHour(start + i * add, this.hourSelectionView);
            float nx = (float)((double)center + Math.cos(Math.toRadians(ag)) * (double)(center - (float)mg));
            float ny = (float)((double)center + Math.sin(Math.toRadians(ag)) * (double)(center - (float)mg));
            if (this.hourSelectionView) {
                hour = this.valueToTime(value, true);
                minute = timeSelectionModel.getMinute();
            } else {
                hour = timeSelectionModel.getHour();
                minute = value;
            }
            boolean isSelectedAble = timeSelectionModel.checkSelection(hour, minute);
            this.paintNumber(g2, (float)x + nx, (float)y + ny, this.fixNumberAndToString(value), this.isSelected(value), isSelectedAble);
        }
    }

    protected void paintNumber(Graphics2D g2, float x, float y, String num, boolean isSelected, boolean isSelectedAble) {
        FontMetrics fm = g2.getFontMetrics();
        Rectangle2D rec = fm.getStringBounds(num, g2);
        float x1 = (float)((double)x - rec.getWidth() / 2.0);
        float y1 = (float)((double)y - rec.getHeight() / 2.0);
        if (!isSelectedAble) {
            g2.setColor(UIManager.getColor("Label.disabledForeground"));
        } else if (isSelected) {
            g2.setColor(this.getSelectedForeground());
        } else {
            g2.setColor(UIManager.getColor("Panel.foreground"));
        }
        g2.drawString(num, x1, y1 + (float)fm.getAscent());
    }

    protected Color getClockBackground() {
        if (FlatLaf.isLafDark()) {
            return ColorFunctions.lighten((Color)this.getBackground(), (float)0.03f);
        }
        return ColorFunctions.darken((Color)this.getBackground(), (float)0.03f);
    }

    protected boolean isSelected(int value) {
        if (this.hourSelectionView) {
            int hour = this.getHourValue(this.timePicker.getTimeSelectionModel().getHour());
            return value == hour;
        }
        return value == this.timePicker.getTimeSelectionModel().getMinute();
    }

    protected Color getSelectedColor() {
        if (this.color != null) {
            return this.color;
        }
        return UIManager.getColor("Component.accentColor");
    }

    protected Color getSelectedForeground() {
        return this.getForeground();
    }

    private int getValueOf(float angle, boolean hourView) {
        float ag = angle / 360.0f;
        int value = (int)(ag * (float)(hourView ? 12 : 60));
        if (hourView) {
            if (this.isUse24hour()) {
                return value == 0 ? 12 : value;
            }
            return value == 12 ? 0 : value;
        }
        return value == 60 ? 0 : value;
    }

    private int getValueOf(Point point, boolean hourView) {
        float angle = this.getAngleOf(point) + (hourView ? 15.0f : 3.0f);
        int value = this.getValueOf(angle, hourView);
        if (hourView) {
            boolean isAdd12Hour = !this.use24hour && !this.timePicker.getHeader().isAm() || this.use24hour && this.is24hourSelect(point);
            return this.fixHour(value + (isAdd12Hour ? 12 : 0), true);
        }
        return value;
    }

    private boolean is24hourSelect(Point point) {
        Insets insets = this.getInsets();
        int width = this.getWidth() - (insets.left + insets.right);
        int height = this.getHeight() - (insets.top + insets.bottom);
        int size = Math.min(width, height) / 2;
        int distanceTarget = size - UIScale.scale((int)35);
        float centerX = (float)insets.left + (float)width / 2.0f;
        float centerY = (float)insets.top + (float)height / 2.0f;
        double distance = Math.sqrt(Math.pow((float)point.x - centerX, 2.0) + Math.pow((float)point.y - centerY, 2.0));
        return distance < (double)distanceTarget;
    }

    private float getAngleOf(int number, boolean hourView) {
        float ag = 360.0f / (hourView ? 12.0f : 60.0f);
        return this.fixAngle(ag * (float)number);
    }

    private float getAngleOf(Point point) {
        float centerX;
        float x;
        Insets insets = this.getInsets();
        int width = this.getWidth() - (insets.left + insets.right);
        int height = this.getHeight() - (insets.top + insets.bottom);
        float centerY = (float)insets.top + (float)height / 2.0f;
        float y = (float)point.y - centerY;
        double angle = Math.toDegrees(Math.atan2(y, x = (float)point.x - (centerX = (float)insets.left + (float)width / 2.0f))) + 90.0;
        if (angle < 0.0) {
            angle += 360.0;
        }
        return (float)angle;
    }

    private float fixAngle(float angle) {
        if (angle > 360.0f) {
            angle -= 360.0f;
        }
        if (angle == 360.0f) {
            return 0.0f;
        }
        return angle;
    }

    private int fixHour(int value, boolean hourView) {
        if (hourView ? this.use24hour && value == 24 : value == 60) {
            return 0;
        }
        return value;
    }

    private String fixNumberAndToString(int num) {
        if (num == 0) {
            return "00";
        }
        return "" + num;
    }

    private int getHourValue(int hour) {
        if (this.isUse24hour()) {
            return hour;
        }
        hour = this.timePicker.getHeader().isAm() ? hour : hour - 12;
        return hour == 0 ? 12 : hour;
    }

    private int valueToTime(int value, boolean hourView) {
        if (!hourView) {
            return value;
        }
        if (!this.isUse24hour()) {
            boolean isAm;
            if (isAm && (value += (isAm = this.timePicker.getHeader().isAm()) ? 0 : 12) == 12) {
                return 0;
            }
            if (!isAm && value == 24) {
                return 12;
            }
        }
        return value;
    }

    private boolean is24hour() {
        int hour = this.timePicker.getTimeSelectionModel().getHour();
        return this.use24hour && (hour == 0 || hour > 12);
    }

    private int getTargetMargin() {
        return this.is24hour() && this.hourSelectionView ? 50 : 20;
    }

    private void runAnimation(boolean animate) {
        int value = this.hourSelectionView ? this.timePicker.getTimeSelectionModel().getHour() : this.timePicker.getTimeSelectionModel().getMinute();
        float angleTarget = this.getAngleOf(value, this.hourSelectionView);
        float marginTarget = this.getTargetMargin();
        this.animationChange.start(angleTarget, marginTarget, animate);
    }

    public void setColor(Color color) {
        this.color = color;
    }
}

