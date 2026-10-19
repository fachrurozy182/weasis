/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.formdev.flatlaf.FlatLaf
 *  com.formdev.flatlaf.ui.FlatUIUtils
 *  com.formdev.flatlaf.util.ColorFunctions
 *  com.formdev.flatlaf.util.UIScale
 */
package org.weasis.core.ui.tp.raven.datetime.component.date;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.ColorFunctions;
import com.formdev.flatlaf.util.UIScale;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import javax.swing.JComponent;
import javax.swing.UIManager;
import org.weasis.core.ui.tp.raven.datetime.DatePicker;
import org.weasis.core.ui.tp.raven.datetime.component.date.ButtonDate;
import org.weasis.core.ui.tp.raven.datetime.component.date.DateSelectionModel;
import org.weasis.core.ui.tp.raven.datetime.component.date.SingleDate;
import org.weasis.core.ui.tp.raven.datetime.util.Utils;

public class DefaultDateCellRenderer {
    public void paint(Graphics2D g2, DatePicker datePicker, ButtonDate component, SingleDate date, float width, float height) {
        float arc = this.getArc(datePicker);
        boolean isSelected = component.isDateSelected();
        this.paintDateSelection(g2, datePicker, component, isSelected, width, height, arc);
        DateSelectionModel dateSelectionModel = datePicker.getDateSelectionModel();
        if (dateSelectionModel.getDateSelectionMode() == DatePicker.DateSelectionMode.BETWEEN_DATE_SELECTED && dateSelectionModel.getDate() != null) {
            this.paintBetweenDateSelection(g2, datePicker, component, date, width, height, arc);
        }
        if (date.same(new SingleDate())) {
            this.paintCurrentDate(g2, datePicker, component, date, isSelected, width, height, arc);
        }
    }

    protected void paintDateSelection(Graphics2D g2, DatePicker datePicker, ButtonDate component, boolean isSelected, float width, float height, float arc) {
        Rectangle2D.Float rec = this.getRectangle(width, height);
        g2.setColor(this.getSelectionColor(datePicker, component, isSelected));
        g2.fill(FlatUIUtils.createComponentRectangle((float)rec.x, (float)rec.y, (float)rec.width, (float)rec.height, (float)arc));
    }

    protected void paintBetweenDateSelection(Graphics2D g2, DatePicker datePicker, ButtonDate component, SingleDate date, float width, float height, float arc) {
        g2.setColor(this.getBetweenDateColor(datePicker, component));
        DateSelectionModel dateSelectionModel = datePicker.getDateSelectionModel();
        Rectangle2D.Float rec = this.getRectangle(width, height);
        int rowIndex = component.getRowIndex();
        if (date.between(dateSelectionModel.getDate(), this.getToDate(datePicker))) {
            if (rowIndex == 0) {
                g2.fill(this.createShape(rec.x, rec.y, width, rec.height, arc, true, true));
            } else if (rowIndex == 6) {
                g2.fill(this.createShape(rec.x, rec.y, width, rec.height, arc, false, true));
            } else {
                g2.fill(new Rectangle2D.Float(0.0f, rec.y, width, rec.height));
            }
        }
        if (!dateSelectionModel.getDate().same(this.getToDate(datePicker))) {
            boolean right = dateSelectionModel.getDate().before(this.getToDate(datePicker));
            if (date.same(dateSelectionModel.getDate()) && (right && rowIndex != 6 || !right && rowIndex != 0)) {
                g2.fill(this.createShape(rec.x, rec.y, width, rec.height, arc, right, false));
            }
            if (date.same(this.getToDate(datePicker)) && (right && rowIndex != 0 || !right && rowIndex != 6)) {
                g2.fill(this.createShape(rec.x, rec.y, width, rec.height, arc, !right, !component.isHover() && dateSelectionModel.getToDate() == null));
            }
        }
    }

    protected void paintCurrentDate(Graphics2D g2, DatePicker datePicker, ButtonDate component, SingleDate date, boolean isSelected, float width, float height, float arc) {
        Rectangle2D.Float rec = this.getRectangle(width, height);
        float fw = this.getSelectedFocusWidth();
        float space = UIScale.scale((float)(isSelected ? fw : 0.0f));
        if (space > 0.0f) {
            rec.x -= space;
            rec.y -= space;
            rec.width += space * 2.0f;
            rec.height += space * 2.0f;
        }
        Area area = new Area(FlatUIUtils.createComponentRectangle((float)rec.x, (float)rec.y, (float)rec.width, (float)rec.height, (float)arc));
        if (isSelected) {
            s = UIScale.scale((float)(fw + 1.0f));
            area.subtract(new Area(FlatUIUtils.createComponentRectangle((float)(rec.x + s), (float)(rec.x + s), (float)(rec.width - s * 2.0f), (float)(rec.height - s * 2.0f), (float)(arc - s))));
        } else {
            s = UIScale.scale((float)fw);
            area.subtract(new Area(FlatUIUtils.createComponentRectangle((float)(rec.x + s), (float)(rec.y + s), (float)(rec.width - s * 2.0f), (float)(rec.height - s * 2.0f), (float)(arc - s))));
        }
        Color accentColor = this.getAccentColor(datePicker);
        g2.setColor(isSelected ? this.getBorderColor(component, accentColor) : accentColor);
        g2.fill(area);
    }

    private SingleDate getToDate(DatePicker datePicker) {
        DateSelectionModel dateSelectionModel = datePicker.getDateSelectionModel();
        return dateSelectionModel.getToDate() != null ? dateSelectionModel.getToDate() : dateSelectionModel.getHoverDate();
    }

    protected Shape createShape(float x, float y, float width, float size, float arc, boolean right, boolean add) {
        Area area;
        if (right) {
            area = new Area(new Rectangle2D.Float(width / 2.0f, y, width / 2.0f, size));
            area.subtract(new Area(FlatUIUtils.createComponentRectangle((float)x, (float)y, (float)size, (float)size, (float)arc)));
        } else {
            area = new Area(new Rectangle2D.Float(0.0f, y, width / 2.0f, size));
        }
        if (add) {
            area.add(new Area(FlatUIUtils.createComponentRectangle((float)x, (float)y, (float)size, (float)size, (float)arc)));
        } else {
            area.subtract(new Area(FlatUIUtils.createComponentRectangle((float)x, (float)y, (float)size, (float)size, (float)arc)));
        }
        return area;
    }

    protected Color getBorderColor(ButtonDate component, Color color) {
        return ColorFunctions.mix((Color)color, (Color)component.getParent().getBackground(), (float)0.45f);
    }

    protected Color getBetweenDateColor(DatePicker datePicker, ButtonDate component) {
        Color color = FlatUIUtils.getParentBackground((JComponent)component);
        if (datePicker.getDateSelectionModel().getToDate() != null) {
            return ColorFunctions.mix((Color)color, (Color)this.getAccentColor(datePicker), (float)0.9f);
        }
        return FlatLaf.isLafDark() ? ColorFunctions.lighten((Color)color, (float)0.03f) : ColorFunctions.darken((Color)color, (float)0.03f);
    }

    protected Color getSelectionColor(DatePicker datePicker, ButtonDate component, boolean isSelected) {
        Color color = FlatUIUtils.getParentBackground((JComponent)component);
        if (isSelected) {
            color = this.getAccentColor(datePicker);
        }
        return Utils.getColor(color, component.isPress(), component.isHover());
    }

    protected Color getAccentColor(DatePicker datePicker) {
        if (datePicker.getColor() != null) {
            return datePicker.getColor();
        }
        return UIManager.getColor("Component.accentColor");
    }

    protected float getCellPadding() {
        return 7.0f;
    }

    protected float getSelectedFocusWidth() {
        return 2.0f;
    }

    protected float getArc(DatePicker datePicker) {
        return datePicker.getSelectionArc();
    }

    protected Rectangle2D.Float getRectangle(float width, float height) {
        float size = Math.min(width, height) - UIScale.scale((float)this.getCellPadding());
        float x = (width - size) / 2.0f;
        float y = (height - size) / 2.0f;
        return new Rectangle2D.Float(x, y, size, size);
    }
}

