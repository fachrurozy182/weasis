/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  bibliothek.gui.dock.DockElementRepresentative
 *  bibliothek.gui.dock.control.focus.DefaultFocusRequest
 *  bibliothek.gui.dock.control.focus.FocusController
 *  bibliothek.gui.dock.control.focus.FocusRequest
 *  org.opencv.core.Point3
 *  org.weasis.core.util.LangUtil
 */
package org.weasis.core.ui.editor.image;

import bibliothek.gui.dock.DockElementRepresentative;
import bibliothek.gui.dock.control.focus.DefaultFocusRequest;
import bibliothek.gui.dock.control.focus.FocusController;
import bibliothek.gui.dock.control.focus.FocusRequest;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeListener;
import java.util.Comparator;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import org.opencv.core.Point3;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.Image2DViewer;
import org.weasis.core.api.gui.util.ActionState;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.gui.util.Feature;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.gui.util.MouseActionAdapter;
import org.weasis.core.api.gui.util.ShortcutManager;
import org.weasis.core.api.gui.util.WinUtil;
import org.weasis.core.api.image.OpManager;
import org.weasis.core.api.image.ZoomOp;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.service.UICore;
import org.weasis.core.ui.editor.SeriesViewerEvent;
import org.weasis.core.ui.editor.image.Canvas;
import org.weasis.core.ui.editor.image.DefaultView2d;
import org.weasis.core.ui.editor.image.GraphicMouseHandler;
import org.weasis.core.ui.editor.image.ImageViewerEventManager;
import org.weasis.core.ui.editor.image.ImageViewerPlugin;
import org.weasis.core.ui.editor.image.MouseActions;
import org.weasis.core.ui.editor.image.Panner;
import org.weasis.core.ui.editor.image.PannerListener;
import org.weasis.core.ui.editor.image.PixelInfo;
import org.weasis.core.ui.editor.image.ViewButton;
import org.weasis.core.ui.editor.image.ViewerPlugin;
import org.weasis.core.ui.editor.image.ViewerToolBar;
import org.weasis.core.ui.editor.image.dockable.MeasureTool;
import org.weasis.core.ui.model.graphic.Graphic;
import org.weasis.core.ui.model.layer.LayerAnnotation;
import org.weasis.core.ui.model.utils.ImageLayerChangeListener;
import org.weasis.core.ui.model.utils.bean.PanPoint;
import org.weasis.core.ui.util.TitleMenuItem;
import org.weasis.core.util.LangUtil;

public interface ViewCanvas<E extends ImageElement>
extends Canvas,
Image2DViewer<E>,
PropertyChangeListener,
FocusListener,
ImageLayerChangeListener<E>,
KeyListener {
    public static final String ZOOM_TYPE_CMD = "zoom.type";
    public static final int CENTER_POINTER = 2;
    public static final int HIGHLIGHTED_POINTER = 4;
    public static final Border focusBorder = BorderFactory.createMatteBorder(1, 1, 1, 1, GuiUtils.IconColor.ACTIONS_YELLOW.getColor());
    public static final Border viewBorder = BorderFactory.createMatteBorder(1, 1, 1, 1, Color.GRAY);
    public static final PanPoint highlightedPosition = new PanPoint(PanPoint.State.CENTER);
    public static final PanPoint startedDragPoint = new PanPoint(PanPoint.State.DRAGSTART);
    public static final Color pointerColor1 = Color.black;
    public static final Color pointerColor2 = Color.white;
    public static final Ellipse2D pointerCircle = new Ellipse2D.Double(-27.0, -27.0, 54.0, 54.0);
    public static final Line2D pointerLeft = new Line2D.Double(-40.0, 0.0, -5.0, 0.0);
    public static final Line2D pointerRight = new Line2D.Double(5.0, 0.0, 40.0, 0.0);
    public static final Line2D pointerUp = new Line2D.Double(0.0, -40.0, 0.0, -5.0);
    public static final Line2D pointerDown = new Line2D.Double(0.0, 5.0, 0.0, 40.0);

    default public MouseActionAdapter getAction(Feature<?> action) {
        Object obj;
        Optional<?> a = this.getEventManager().getAction(action);
        if (a.isPresent() && (obj = a.get()) instanceof MouseActionAdapter) {
            MouseActionAdapter actionAdapter = (MouseActionAdapter)obj;
            return actionAdapter;
        }
        return null;
    }

    default public void addModifierMask(String action, int mask) {
        MouseActionAdapter adapter = this.getMouseAdapter(action);
        if (adapter != null) {
            MouseActionAdapter win;
            adapter.setButtonMaskEx(adapter.getButtonMaskEx() | mask);
            if (ActionW.WINLEVEL.cmd().equals(action) && (win = this.getMouseAdapter(ActionW.WINDOW.cmd())) != null) {
                win.setButtonMaskEx(win.getButtonMaskEx() | mask);
            }
        }
    }

    default public void resetMouseAdapter() {
        for (ActionState adapter : this.getEventManager().getAllActionValues()) {
            if (!(adapter instanceof MouseActionAdapter)) continue;
            MouseActionAdapter mouseActionAdapter = (MouseActionAdapter)((Object)adapter);
            mouseActionAdapter.setButtonMaskEx(0);
        }
    }

    default public void addMouseAdapter(String actionName, int buttonMask) {
        MouseActionAdapter adapter = this.getMouseAdapter(actionName);
        if (adapter == null) {
            return;
        }
        JComponent c = this.getJComponent();
        adapter.setButtonMaskEx(adapter.getButtonMaskEx() | buttonMask);
        if (adapter instanceof GraphicMouseHandler) {
            c.addKeyListener(this.getDrawingsKeyListeners());
        } else if (adapter instanceof PannerListener) {
            PannerListener pannerListener = (PannerListener)adapter;
            pannerListener.reset();
            c.addKeyListener(pannerListener);
        }
        if (actionName.equals(ActionW.WINLEVEL.cmd())) {
            MouseActionAdapter win = this.getAction(ActionW.WINDOW);
            if (win != null) {
                win.setButtonMaskEx(win.getButtonMaskEx() | buttonMask);
                win.setMoveOnX(true);
                c.addMouseListener(win);
                c.addMouseMotionListener(win);
            }
            adapter.setInverse(this.getEventManager().getOptions().getBooleanProperty("weasis.level.inverse", true));
        } else if (actionName.equals(ActionW.WINDOW.cmd())) {
            adapter.setMoveOnX(false);
        } else if (actionName.equals(ActionW.LEVEL.cmd())) {
            adapter.setInverse(this.getEventManager().getOptions().getBooleanProperty("weasis.level.inverse", true));
        }
        c.addMouseListener(adapter);
        c.addMouseMotionListener(adapter);
    }

    default public void disableMouseAndKeyListener(Component c) {
        MouseListener[] listener = c.getMouseListeners();
        MouseMotionListener[] motionListeners = c.getMouseMotionListeners();
        KeyListener[] keyListeners = c.getKeyListeners();
        MouseWheelListener[] wheelListeners = c.getMouseWheelListeners();
        for (MouseListener mouseListener : listener) {
            c.removeMouseListener(mouseListener);
        }
        for (EventListener eventListener : motionListeners) {
            c.removeMouseMotionListener((MouseMotionListener)eventListener);
        }
        for (EventListener eventListener : keyListeners) {
            c.removeKeyListener((KeyListener)eventListener);
        }
        for (EventListener eventListener : wheelListeners) {
            c.removeMouseWheelListener((MouseWheelListener)eventListener);
        }
    }

    default public void enableMouseAndKeyListener(MouseActions actions) {
        this.disableMouseAndKeyListener();
        this.iniDefaultMouseListener();
        this.iniDefaultKeyListener();
        this.resetMouseAdapter();
        this.getJComponent().setCursor(DefaultView2d.DEFAULT_CURSOR);
        this.addMouseAdapter(actions.getLeft(), 1024);
        if (actions.getMiddle().equals(actions.getLeft())) {
            this.addModifierMask(actions.getMiddle(), 2048);
        } else {
            this.addMouseAdapter(actions.getMiddle(), 2048);
        }
        if (actions.getRight().equals(actions.getLeft()) || actions.getRight().equals(actions.getMiddle())) {
            this.addModifierMask(actions.getRight(), 4096);
        } else {
            this.addMouseAdapter(actions.getRight(), 4096);
        }
        this.getJComponent().addMouseWheelListener(this.getMouseAdapter(actions.getWheel()));
    }

    public MouseActionAdapter getMouseAdapter(String var1);

    default public boolean isDrawActionActive() {
        ViewerToolBar toolBar;
        ViewerPlugin container = WinUtil.getParentOfClass(this.getJComponent(), ViewerPlugin.class);
        if (container != null && (toolBar = container.getViewerToolBar()) != null) {
            return toolBar.isCommandActive(ActionW.MEASURE.cmd()) || toolBar.isCommandActive(ActionW.DRAW.cmd());
        }
        return false;
    }

    public void registerDefaultListeners();

    public void copyActionWState(HashMap<String, Object> var1);

    public ImageViewerEventManager<E> getEventManager();

    public void updateSynchState();

    public PixelInfo getPixelInfo(Point var1);

    public Panner<E> getPanner();

    public void setSeries(MediaSeries<E> var1);

    public void setSeries(MediaSeries<E> var1, E var2);

    default public void setFocused(Boolean focused) {
        boolean isFocused = LangUtil.nullToFalse((Boolean)focused);
        MediaSeries series = this.getSeries();
        if (series != null) {
            series.setFocused(isFocused);
        }
        if (isFocused && this.getBorder() == viewBorder) {
            this.setBorder(focusBorder);
        } else if (!isFocused && this.getBorder() == focusBorder) {
            this.setBorder(viewBorder);
        }
        if (isFocused) {
            ViewerPlugin viewerPlugin = WinUtil.getParentOfClass(this.getJComponent(), ViewerPlugin.class);
            if (viewerPlugin instanceof ViewerPlugin) {
                ViewerPlugin viewerPlugin2 = viewerPlugin;
                FocusController focusController = UICore.getInstance().getDockingControl().getController().getFocusController();
                focusController.focus((FocusRequest)new DefaultFocusRequest((DockElementRepresentative)viewerPlugin2.getDockable().intern(), (Component)this.getJComponent(), true));
            } else {
                Timer timer = new Timer();
                timer.schedule(new TimerTask(this){
                    final /* synthetic */ ViewCanvas this$0;
                    {
                        ViewCanvas viewCanvas = this$0;
                        Objects.requireNonNull(viewCanvas);
                        this.this$0 = viewCanvas;
                    }

                    @Override
                    public void run() {
                        SwingUtilities.invokeLater(() -> this.this$0.getJComponent().requestFocus());
                    }
                }, 500L);
            }
        }
    }

    default public Point3 getVolumePoint3FromMouse(int x, int y) {
        return null;
    }

    public void setBorder(Border var1);

    public Border getBorder();

    public LayerAnnotation getInfoLayer();

    public int getTileOffset();

    public void setTileOffset(int var1);

    public void center();

    public void setCenter(Double var1, Double var2);

    public void moveOrigin(PanPoint var1);

    public Comparator<E> getCurrentSortComparator();

    public void setActionsInView(String var1, Object var2);

    public void setActionsInView(String var1, Object var2, Boolean var3);

    public void setSelected(Boolean var1);

    default public Font getFont() {
        return MeasureTool.viewSetting.getFont();
    }

    public Font getLayerFont();

    public void setDrawingsVisibility(Boolean var1);

    public Object getLensActionValue(String var1);

    public void changeZoomInterpolation(ZoomOp.Interpolation var1);

    public OpManager getDisplayOpManager();

    public void disableMouseAndKeyListener();

    public void iniDefaultMouseListener();

    public void iniDefaultKeyListener();

    public int getPointerType();

    public void setPointerType(int var1);

    public void addPointerType(int var1);

    public void resetPointerType(int var1);

    public Point2D getHighlightedPosition();

    default public void drawPointer(Graphics2D g, double x, double y, boolean circle) {
        Object[] oldRenderingHints = GuiUtils.setRenderingHints(g, true, true, false);
        float[] dash = new float[]{5.0f};
        g.translate(x, y);
        g.setStroke(new BasicStroke(3.0f));
        g.setPaint(pointerColor1);
        if (circle) {
            g.draw(pointerCircle);
        }
        g.draw(pointerLeft);
        g.draw(pointerRight);
        g.draw(pointerUp);
        g.draw(pointerDown);
        g.setStroke(new BasicStroke(1.0f, 0, 0, 5.0f, dash, 0.0f));
        g.setPaint(pointerColor2);
        if (circle) {
            g.draw(pointerCircle);
        }
        g.draw(pointerLeft);
        g.draw(pointerRight);
        g.draw(pointerUp);
        g.draw(pointerDown);
        g.translate(-x, -y);
        GuiUtils.resetRenderingHints(g, oldRenderingHints);
    }

    default public void drawProgressBar(Graphics2D g2d, JProgressBar bar) {
        if (bar != null && bar.isVisible()) {
            int centerX = this.getJComponent().getWidth() / 2;
            int centerY = this.getJComponent().getHeight() / 2;
            int shiftX = centerX - bar.getWidth() / 2;
            int shiftY = centerY - bar.getHeight() / 2;
            g2d.translate(shiftX, shiftY);
            bar.paint(g2d);
            g2d.translate(-shiftX, -shiftY);
        }
    }

    public List<Action> getExportActions();

    public void resetZoom();

    public void resetPan();

    public void reset();

    public List<ViewButton> getViewButtons();

    public void closeLens();

    public void updateCanvas(boolean var1);

    public void updateGraphicSelectionListener(ImageViewerPlugin<E> var1);

    public boolean requiredTextAntialiasing();

    public JPopupMenu buildGraphicContextMenu(MouseEvent var1, List<Graphic> var2);

    public JPopupMenu buildContextMenu(MouseEvent var1);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    default public JPopupMenu buildLeftMouseActionMenu() {
        ViewerToolBar toolBar;
        JPopupMenu popupMenu = new JPopupMenu();
        TitleMenuItem itemTitle = new TitleMenuItem(Messages.getString("left.mouse.actions") + ":");
        popupMenu.add(itemTitle);
        popupMenu.setLabel("left");
        ImageViewerEventManager<E> eventManager = this.getEventManager();
        String action = eventManager.getMouseActions().getLeft();
        ButtonGroup groupButtons = new ButtonGroup();
        int count = popupMenu.getComponentCount();
        ImageViewerPlugin<E> view = eventManager.getSelectedView2dContainer();
        if (view != null && (toolBar = view.getViewerToolBar()) != null) {
            List<Feature<?>> actionsButtons;
            ActionListener leftButtonAction = event -> {
                Object patt0$temp = event.getSource();
                if (patt0$temp instanceof JRadioButtonMenuItem) {
                    JRadioButtonMenuItem item = (JRadioButtonMenuItem)patt0$temp;
                    toolBar.changeButtonState("left", item.getActionCommand());
                }
            };
            List<Feature<?>> list = actionsButtons = ViewerToolBar.actionsButtons;
            synchronized (list) {
                for (Feature<?> b : actionsButtons) {
                    if (!eventManager.isActionEnabled(b)) continue;
                    JRadioButtonMenuItem radio = new JRadioButtonMenuItem(b.getTitle(), (Icon)b.getIcon(), b.cmd().equals(action));
                    GuiUtils.applySelectedIconEffect(radio);
                    radio.setActionCommand(b.cmd());
                    radio.setAccelerator(KeyStroke.getKeyStroke(b.getKeyCode(), b.getModifier()));
                    radio.addActionListener(toolBar);
                    radio.addActionListener(leftButtonAction);
                    popupMenu.add(radio);
                    groupButtons.add(radio);
                }
            }
        }
        if (count < popupMenu.getComponentCount()) {
            popupMenu.add(new JSeparator());
        }
        return popupMenu;
    }

    default public int addSeparatorToPopupMenu(JPopupMenu popupMenu, int count) {
        if (count < popupMenu.getComponentCount()) {
            popupMenu.add(new JSeparator());
            return popupMenu.getComponentCount();
        }
        return count;
    }

    public boolean hasValidContent();

    default public void drawPointer(Graphics2D g, int pointerType) {
        if (pointerType < 1) {
            return;
        }
        if ((pointerType & 2) == 2) {
            this.drawPointer(g, (double)(this.getJComponent().getWidth() - 1) * 0.5, (double)(this.getJComponent().getHeight() - 1) * 0.5, false);
        }
        if ((pointerType & 4) == 4 && highlightedPosition.isHighlightedPosition()) {
            double offsetX = this.modelToViewLength(highlightedPosition.getX() + 0.5 - this.getViewModel().getModelOffsetX());
            double offsetY = this.modelToViewLength(highlightedPosition.getY() + 0.5 - this.getViewModel().getModelOffsetY());
            this.drawPointer(g, offsetX, offsetY, true);
        }
    }

    default public void defaultKeyPressed(ImageViewerEventManager<?> eventManager, KeyEvent e) {
        int modifiers;
        int keyCode;
        ShortcutManager sm = ShortcutManager.getInstance();
        if (sm.matches("viewer.nextMouseAction", keyCode = e.getKeyCode(), modifiers = e.getModifiers())) {
            eventManager.nextLeftMouseAction();
        } else if (sm.matches("viewer.toggleInfo", keyCode, modifiers) || sm.matches("viewer.toggleInfoAlt", keyCode, modifiers)) {
            eventManager.fireSeriesViewerListeners(new SeriesViewerEvent(eventManager.getSelectedView2dContainer(), null, null, SeriesViewerEvent.EVENT.TOGGLE_INFO));
        } else if (sm.matches("viewer.fullscreen", keyCode, modifiers)) {
            ImageViewerPlugin<?> c = eventManager.getSelectedView2dContainer();
            if (c != null) {
                c.maximizedSelectedImagePane(c.getSelectedViewCanvas(), null);
            }
        } else if (sm.matches("viewer.rotateLeft", keyCode, modifiers)) {
            eventManager.getAction(ActionW.ROTATION).ifPresent(a -> a.setSliderValue((a.getSliderValue() + 270) % 360));
        } else if (sm.matches("viewer.rotateRight", keyCode, modifiers)) {
            eventManager.getAction(ActionW.ROTATION).ifPresent(a -> a.setSliderValue((a.getSliderValue() + 90) % 360));
        } else if (sm.matches("viewer.flipHorizontal", keyCode, modifiers)) {
            eventManager.getAction(ActionW.FLIP).ifPresent(f -> f.setSelected(!f.isSelected()));
        } else {
            Optional<Feature<ActionState>> feature = eventManager.getLeftMouseActionFromKeyEvent(e.getKeyCode(), e.getModifiers());
            if (feature.isPresent()) {
                eventManager.changeLeftMouseAction(feature.get().cmd());
            } else {
                eventManager.keyPressed(e);
            }
        }
    }
}

