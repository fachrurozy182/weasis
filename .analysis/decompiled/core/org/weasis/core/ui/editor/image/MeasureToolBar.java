/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.formdev.flatlaf.extras.FlatSVGIcon
 */
package org.weasis.core.ui.editor.image;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.event.ListDataEvent;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.util.ActionState;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.gui.util.ComboItemListener;
import org.weasis.core.api.gui.util.DropDownButton;
import org.weasis.core.api.gui.util.Feature;
import org.weasis.core.api.gui.util.GroupPopup;
import org.weasis.core.api.gui.util.GroupRadioMenu;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.gui.util.RadioMenuItem;
import org.weasis.core.api.service.WProperties;
import org.weasis.core.api.util.ResourceUtil;
import org.weasis.core.ui.editor.image.ImageViewerEventManager;
import org.weasis.core.ui.editor.image.ImageViewerPlugin;
import org.weasis.core.ui.editor.image.MouseActions;
import org.weasis.core.ui.editor.image.ViewerToolBar;
import org.weasis.core.ui.editor.image.dockable.MeasureTool;
import org.weasis.core.ui.model.GraphicModel;
import org.weasis.core.ui.model.graphic.DragGraphic;
import org.weasis.core.ui.model.graphic.Graphic;
import org.weasis.core.ui.model.graphic.imp.AnnotationGraphic;
import org.weasis.core.ui.model.graphic.imp.PixelInfoGraphic;
import org.weasis.core.ui.model.graphic.imp.angle.AngleToolGraphic;
import org.weasis.core.ui.model.graphic.imp.angle.CobbAngleToolGraphic;
import org.weasis.core.ui.model.graphic.imp.angle.FourPointsAngleToolGraphic;
import org.weasis.core.ui.model.graphic.imp.angle.OpenAngleToolGraphic;
import org.weasis.core.ui.model.graphic.imp.area.EllipseGraphic;
import org.weasis.core.ui.model.graphic.imp.area.ObliqueRectangleGraphic;
import org.weasis.core.ui.model.graphic.imp.area.PolygonGraphic;
import org.weasis.core.ui.model.graphic.imp.area.SelectGraphic;
import org.weasis.core.ui.model.graphic.imp.area.ThreePointsCircleGraphic;
import org.weasis.core.ui.model.graphic.imp.line.LineGraphic;
import org.weasis.core.ui.model.graphic.imp.line.ParallelLineGraphic;
import org.weasis.core.ui.model.graphic.imp.line.PerpendicularLineGraphic;
import org.weasis.core.ui.model.graphic.imp.line.PolylineGraphic;
import org.weasis.core.ui.model.layer.LayerType;
import org.weasis.core.ui.pref.ViewSetting;
import org.weasis.core.ui.util.WtoolBar;

public class MeasureToolBar
extends WtoolBar {
    public static final SelectGraphic selectionGraphic = new SelectGraphic();
    private static final List<Graphic> measureGraphicList = new ArrayList<Graphic>();
    private static final List<Graphic> drawGraphicList = new ArrayList<Graphic>();
    protected final JButton deleteButton = new JButton();
    protected final ImageViewerEventManager<?> eventManager;

    public MeasureToolBar(ImageViewerEventManager<?> eventManager, int index) {
        super(Messages.getString("MeasureToolBar.drawing_tools"), index);
        if (eventManager == null) {
            throw new IllegalArgumentException("EventManager cannot be null");
        }
        this.eventManager = eventManager;
        MeasureTool.updateMeasureProperties();
        Optional<Graphic> measure = eventManager.getAction(ActionW.DRAW_MEASURE);
        measure.ifPresent(comboItemListener -> this.add(this.buildButton((ComboItemListener<?>)comboItemListener)));
        Optional<Graphic> draw = eventManager.getAction(ActionW.DRAW_GRAPHICS);
        draw.ifPresent(comboItemListener -> this.add(this.buildButton((ComboItemListener<?>)comboItemListener)));
        if (measure.isPresent() || draw.isPresent()) {
            this.deleteButton.setToolTipText(Messages.getString("MeasureToolBar.del"));
            this.deleteButton.setIcon((Icon)ResourceUtil.getToolBarIcon(ResourceUtil.ActionIcon.SELECTION_DELETE));
            this.deleteButton.addActionListener(e -> {
                GraphicModel gm = eventManager.getSelectedViewPane().getGraphicManager();
                if (gm.getSelectedGraphics().isEmpty()) {
                    gm.setSelectedAllGraphics();
                }
                gm.deleteSelectedGraphics(eventManager.getSelectedViewPane(), Boolean.TRUE);
            });
            if (measure.isPresent()) {
                ((ComboItemListener)((Object)measure.get())).registerActionState(this.deleteButton);
            } else {
                draw.ifPresent(comboItemListener -> comboItemListener.registerActionState(this.deleteButton));
            }
            this.add(this.deleteButton);
        }
    }

    public static List<Graphic> getDrawGraphicList() {
        return drawGraphicList;
    }

    public static List<Graphic> getMeasureGraphicList() {
        return measureGraphicList;
    }

    public static void applyDefaultSetting(ViewSetting setting, Graphic graphic) {
        if (graphic instanceof DragGraphic) {
            DragGraphic g = (DragGraphic)graphic;
            g.setLineThickness(Float.valueOf(setting.getLineWidth()));
            g.setPaint(setting.getLineColor());
            g.setFilled(setting.isFilled());
            g.setFillOpacity(Float.valueOf(setting.getFillOpacity()));
        }
    }

    private DropDownButton buildButton(ComboItemListener<?> action) {
        boolean draw = action.getActionW() == ActionW.DRAW_GRAPHICS;
        MeasureGroupMenu menu = new MeasureGroupMenu(action.getActionW());
        action.registerActionState(menu);
        for (RadioMenuItem item : menu.getRadioMenuItemListCopy()) {
            Graphic g;
            Object object = item.getUserObject();
            if (!(object instanceof Graphic) || (g = (Graphic)object).getKeyCode() == 0) continue;
            item.setAccelerator(KeyStroke.getKeyStroke(g.getKeyCode(), g.getModifier()));
        }
        DropDownButton dropDownButton = new DropDownButton(this, action.getActionW().cmd(), MeasureToolBar.buildIcon(selectionGraphic, (Icon)(draw ? ResourceUtil.getToolBarIcon(ResourceUtil.ActionIcon.DRAW_TOP_LEFT) : ResourceUtil.getToolBarIcon(ResourceUtil.ActionIcon.MEASURE_TOP_LEFT))), (GroupPopup)menu){
            final /* synthetic */ MeasureToolBar this$0;
            {
                MeasureToolBar measureToolBar = this$0;
                Objects.requireNonNull(measureToolBar);
                this.this$0 = measureToolBar;
                super(type, icon, model);
            }

            @Override
            protected JPopupMenu getPopupMenu() {
                JPopupMenu m = this.getMenuModel() == null ? new JPopupMenu() : this.getMenuModel().createJPopupMenu();
                m.setInvoker(this);
                return m;
            }
        };
        menu.setButton(dropDownButton);
        action.registerActionState(dropDownButton);
        dropDownButton.setToolTipText(draw ? Messages.getString("MeasureToolBar.drawing_tools") : Messages.getString("MeasureToolBar.tools"));
        dropDownButton.addActionListener(e -> {
            ViewerToolBar toolBar;
            ImageViewerPlugin<?> view = this.eventManager.getSelectedView2dContainer();
            if (view != null && (toolBar = view.getViewerToolBar()) != null) {
                String cmd;
                String string = cmd = draw ? ActionW.DRAW.cmd() : ActionW.MEASURE.cmd();
                if (!toolBar.isCommandActive(cmd)) {
                    MouseActions mouseActions = this.eventManager.getMouseActions();
                    mouseActions.setAction("left", cmd);
                    view.setMouseActions(mouseActions);
                    toolBar.changeButtonState("left", cmd);
                }
            }
        });
        return dropDownButton;
    }

    public static Icon buildIcon(Graphic graphic, Icon bckIcon) {
        Icon smallIcon;
        if (graphic == null) {
            smallIcon = null;
        } else {
            Icon icon = graphic.getIcon();
            if (icon instanceof FlatSVGIcon) {
                FlatSVGIcon flatSVGIcon = (FlatSVGIcon)icon;
                smallIcon = flatSVGIcon.derive(18, 18);
            } else {
                smallIcon = graphic.getIcon();
            }
        }
        return ViewerToolBar.getDopButtonIcon(bckIcon, smallIcon);
    }

    static {
        WProperties p = GuiUtils.getUICore().getSystemPreferences();
        if (p.getBooleanProperty("weasis.measure.selection", true)) {
            measureGraphicList.add(selectionGraphic);
        }
        if (p.getBooleanProperty("weasis.measure.line", true)) {
            measureGraphicList.add(new LineGraphic());
        }
        if (p.getBooleanProperty("weasis.measure.polyline", true)) {
            measureGraphicList.add(new PolylineGraphic());
        }
        if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
            measureGraphicList.add(new ObliqueRectangleGraphic());
        }
        if (p.getBooleanProperty("weasis.measure.ellipse", true)) {
            measureGraphicList.add(new EllipseGraphic());
        }
        if (p.getBooleanProperty("weasis.measure.threeptcircle", true)) {
            measureGraphicList.add(new ThreePointsCircleGraphic());
        }
        if (p.getBooleanProperty("weasis.measure.polygon", true)) {
            measureGraphicList.add(new PolygonGraphic());
        }
        if (p.getBooleanProperty("weasis.measure.perpendicular", true)) {
            measureGraphicList.add(new PerpendicularLineGraphic());
        }
        if (p.getBooleanProperty("weasis.measure.parallele", true)) {
            measureGraphicList.add(new ParallelLineGraphic());
        }
        if (p.getBooleanProperty("weasis.measure.angle", true)) {
            measureGraphicList.add(new AngleToolGraphic());
        }
        if (p.getBooleanProperty("weasis.measure.openangle", true)) {
            measureGraphicList.add(new OpenAngleToolGraphic());
        }
        if (p.getBooleanProperty("weasis.measure.fourptangle", true)) {
            measureGraphicList.add(new FourPointsAngleToolGraphic());
        }
        if (p.getBooleanProperty("weasis.measure.cobbangle", true)) {
            measureGraphicList.add(new CobbAngleToolGraphic());
        }
        if (p.getBooleanProperty("weasis.measure.pixelinfo", true)) {
            measureGraphicList.add(new PixelInfoGraphic());
        }
        measureGraphicList.forEach(g -> g.setLayerType(LayerType.MEASURE));
        if (p.getBooleanProperty("weasis.draw.selection", true)) {
            drawGraphicList.add(selectionGraphic);
        }
        if (p.getBooleanProperty("weasis.draw.line", true)) {
            drawGraphicList.add(new LineGraphic(){

                @Override
                public int getKeyCode() {
                    return 0;
                }
            });
        }
        if (p.getBooleanProperty("weasis.draw.polyline", true)) {
            drawGraphicList.add(new PolylineGraphic());
        }
        if (p.getBooleanProperty("weasis.draw.rectangle", true)) {
            drawGraphicList.add(new ObliqueRectangleGraphic());
        }
        if (p.getBooleanProperty("weasis.draw.ellipse", true)) {
            drawGraphicList.add(new EllipseGraphic());
        }
        if (p.getBooleanProperty("weasis.draw.threeptcircle", true)) {
            drawGraphicList.add(new ThreePointsCircleGraphic());
        }
        if (p.getBooleanProperty("weasis.draw.polygon", true)) {
            drawGraphicList.add(new PolygonGraphic(){

                @Override
                public int getKeyCode() {
                    return 0;
                }
            });
        }
        drawGraphicList.forEach(g -> {
            g.setLayerType(LayerType.DRAW);
            g.setLabelVisible(false);
        });
        if (p.getBooleanProperty("weasis.draw.textGrahic", true)) {
            AnnotationGraphic graphic = new AnnotationGraphic();
            graphic.setLayerType(LayerType.ANNOTATION);
            drawGraphicList.add(graphic);
        }
        selectionGraphic.setFilled(false);
        selectionGraphic.setLabelVisible(false);
        selectionGraphic.setLayerType(LayerType.TEMP_DRAW);
    }

    static class MeasureGroupMenu
    extends GroupRadioMenu<Graphic> {
        private final Feature<? extends ActionState> action;
        private JButton button;

        public MeasureGroupMenu(Feature<? extends ActionState> action) {
            this.action = Objects.requireNonNull(action);
        }

        @Override
        public void contentsChanged(ListDataEvent e) {
            super.contentsChanged(e);
            this.changeButtonState();
        }

        public void changeButtonState() {
            Object sel = this.dataModel.getSelectedItem();
            if (this.button != null && sel instanceof Graphic) {
                Graphic graphic = (Graphic)sel;
                FlatSVGIcon drawIcon = this.action == ActionW.DRAW_GRAPHICS ? ResourceUtil.getToolBarIcon(ResourceUtil.ActionIcon.DRAW_TOP_LEFT) : ResourceUtil.getToolBarIcon(ResourceUtil.ActionIcon.MEASURE_TOP_LEFT);
                Icon icon = MeasureToolBar.buildIcon(graphic, (Icon)drawIcon);
                this.button.setIcon(icon);
                this.button.setActionCommand(sel.toString());
            }
        }

        public JButton getButton() {
            return this.button;
        }

        public void setButton(JButton button) {
            this.button = button;
        }
    }
}

