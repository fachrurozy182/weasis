/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.formdev.flatlaf.extras.FlatSVGIcon
 *  com.formdev.flatlaf.extras.FlatSVGIcon$ColorFilter
 */
package org.weasis.core.ui.editor.image;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.ListDataEvent;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.layout.MigLayoutModel;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.gui.util.ComboItemListener;
import org.weasis.core.api.gui.util.DropButtonIcon;
import org.weasis.core.api.gui.util.DropDownButton;
import org.weasis.core.api.gui.util.Feature;
import org.weasis.core.api.gui.util.GroupRadioMenu;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.service.WProperties;
import org.weasis.core.api.util.ResourceUtil;
import org.weasis.core.ui.editor.image.FrameOfReferenceColor;
import org.weasis.core.ui.editor.image.ImageViewerEventManager;
import org.weasis.core.ui.editor.image.ImageViewerPlugin;
import org.weasis.core.ui.editor.image.MouseActions;
import org.weasis.core.ui.editor.image.SynchOptionsCheckBoxGroup;
import org.weasis.core.ui.editor.image.SynchView;
import org.weasis.core.ui.editor.image.ViewCanvas;
import org.weasis.core.ui.editor.image.ViewSynchData;
import org.weasis.core.ui.editor.image.ViewerPlugin;
import org.weasis.core.ui.util.WtoolBar;

public class ViewerToolBar<E extends ImageElement>
extends WtoolBar
implements ActionListener {
    protected static final List<Feature<?>> actionsButtons = Collections.synchronizedList(new ArrayList<Feature>(Arrays.asList(ActionW.PAN, ActionW.WINLEVEL, ActionW.SCROLL_SERIES, ActionW.ZOOM, ActionW.ROTATION, ActionW.MEASURE, ActionW.DRAW, ActionW.CONTEXTMENU, ActionW.CROSSHAIR, ActionW.NO_ACTION)));
    protected static final Feature<?>[] actionsScroll = new Feature[]{ActionW.SCROLL_SERIES, ActionW.ZOOM, ActionW.ROTATION, ActionW.NO_ACTION};
    public static final FlatSVGIcon MouseLeftIcon = ResourceUtil.getToolBarIcon(ResourceUtil.ActionIcon.MOUSE_LEFT);
    public static final FlatSVGIcon MouseRightIcon = ResourceUtil.getToolBarIcon(ResourceUtil.ActionIcon.MOUSE_RIGHT);
    public static final FlatSVGIcon MouseMiddleIcon = ResourceUtil.getToolBarIcon(ResourceUtil.ActionIcon.MOUSE_MIDDLE);
    public static final FlatSVGIcon MouseWheelIcon = ResourceUtil.getToolBarIcon(ResourceUtil.ActionIcon.MOUSE_WHEEL);
    protected final ImageViewerEventManager<E> eventManager;
    private final DropDownButton mouseLeft;
    private final DropDownButton mouseMiddle;
    private final DropDownButton mouseRight;
    private final DropDownButton mouseWheel;
    private final DropDownButton synchButton;
    private final boolean showSynchModeItems;

    public ViewerToolBar(ImageViewerEventManager<E> eventManager, int activeMouse, WProperties props, int index) {
        this(eventManager, activeMouse, props, index, true);
    }

    public ViewerToolBar(ImageViewerEventManager<E> eventManager, int activeMouse, WProperties props, int index, boolean showSynchModeItems) {
        super(Messages.getString("ViewerToolBar.title"), index);
        if (eventManager == null) {
            throw new IllegalArgumentException("EventManager cannot be null");
        }
        this.eventManager = eventManager;
        this.showSynchModeItems = showSynchModeItems;
        MouseActions actions = eventManager.getMouseActions();
        if ((activeMouse & 0x400) == 1024) {
            this.mouseLeft = this.buildMouseButton(actions, "left");
            this.mouseLeft.setToolTipText(Messages.getString("ViewerToolBar.change") + " " + Messages.getString("ViewerToolBar.m_action"));
            this.add(this.mouseLeft);
        } else {
            this.mouseLeft = null;
        }
        if ((activeMouse & 0x1000) == 4096) {
            this.mouseRight = this.buildMouseButton(actions, "right");
            this.add(this.mouseRight);
        } else {
            this.mouseRight = null;
        }
        if ((activeMouse & 2) == 2) {
            this.mouseWheel = new DropDownButton(this, "wheel", this.buildMouseIcon("wheel", actions.getAction("wheel"))){
                final /* synthetic */ ViewerToolBar this$0;
                {
                    ViewerToolBar viewerToolBar = this$0;
                    Objects.requireNonNull(viewerToolBar);
                    this.this$0 = viewerToolBar;
                    super(type, icon);
                }

                @Override
                protected JPopupMenu getPopupMenu() {
                    return this.this$0.getPopupMenuScroll(this);
                }
            };
            this.mouseWheel.setToolTipText(Messages.getString("ViewerToolBar.change"));
            this.add(this.mouseWheel);
        } else {
            this.mouseWheel = null;
        }
        if ((activeMouse & 0x800) == 2048) {
            this.mouseMiddle = this.buildMouseButton(actions, "middle");
            this.add(this.mouseMiddle);
        } else {
            this.mouseMiddle = null;
        }
        if (activeMouse > 1) {
            this.addSeparator();
        }
        if (props.getBooleanProperty("weasis.toolbar.layout.button", true)) {
            DropDownButton layout = new DropDownButton(this, "layout", new DropButtonIcon((Icon)ResourceUtil.getToolBarIcon(ResourceUtil.ActionIcon.LAYOUT))){
                final /* synthetic */ ViewerToolBar this$0;
                {
                    ViewerToolBar viewerToolBar = this$0;
                    Objects.requireNonNull(viewerToolBar);
                    this.this$0 = viewerToolBar;
                    super(type, icon);
                }

                @Override
                protected JPopupMenu getPopupMenu() {
                    return this.this$0.getLayoutPopupMenuButton(this);
                }
            };
            layout.setToolTipText(Messages.getString("ViewerToolBar.layout"));
            this.add(layout);
        }
        if (props.getBooleanProperty("weasis.toolbar.synch.button", true)) {
            this.synchButton = this.buildSynchButton();
            this.add(this.synchButton);
        } else {
            this.synchButton = null;
        }
        if (props.getBooleanProperty("weasis.toolbar.reset", true)) {
            JButton resetButton = new JButton();
            resetButton.setToolTipText(Messages.getString("ViewerToolBar.disReset"));
            resetButton.setIcon((Icon)ResourceUtil.getToolBarIcon(ResourceUtil.ActionIcon.RESET));
            resetButton.addActionListener(e -> eventManager.resetDisplay());
            eventManager.getAction(ActionW.RESET).ifPresent(r -> r.registerActionState(resetButton));
            this.add(resetButton);
        }
    }

    private DropDownButton buildMouseButton(MouseActions actions, String actionLabel) {
        String action = actions.getAction(actionLabel);
        DropDownButton button = new DropDownButton(this, actionLabel, this.buildMouseIcon(actionLabel, action)){
            final /* synthetic */ ViewerToolBar this$0;
            {
                ViewerToolBar viewerToolBar = this$0;
                Objects.requireNonNull(viewerToolBar);
                this.this$0 = viewerToolBar;
                super(type, icon);
            }

            @Override
            protected JPopupMenu getPopupMenu() {
                return this.this$0.getPopupMenuButton(this);
            }
        };
        button.setActionCommand(action);
        button.setToolTipText(Messages.getString("ViewerToolBar.change"));
        return button;
    }

    public DropDownButton getMouseLeft() {
        return this.mouseLeft;
    }

    private JPopupMenu getLayoutPopupMenuButton(DropDownButton dropDownButton) {
        Optional<MigLayoutModel> layout = this.eventManager.getAction(ActionW.LAYOUT);
        JPopupMenu popupMouseButtons = new JPopupMenu();
        if (layout.isPresent()) {
            Component[] cps;
            JMenu menu = ((ComboItemListener)((Object)layout.get())).createUnregisteredRadioMenu("layout");
            popupMouseButtons.setInvoker(dropDownButton);
            for (Component cp : cps = menu.getMenuComponents()) {
                popupMouseButtons.add(cp);
            }
        }
        return popupMouseButtons;
    }

    public void removeMouseAction(Feature<?> action) {
        if (action != null) {
            actionsButtons.remove(action);
            String cmd = action.cmd();
            MouseActions actions = this.eventManager.getMouseActions();
            if (cmd.equals(this.mouseLeft.getActionCommand())) {
                Feature<?> last = actionsButtons.get(0);
                actions.setAction("left", last.cmd());
                this.changeButtonState("left", last.cmd());
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private JPopupMenu getPopupMenuButton(DropDownButton dropButton) {
        String type = dropButton.getType();
        String action = this.eventManager.getMouseActions().getAction(type);
        JPopupMenu popupMouseButtons = new JPopupMenu(type);
        popupMouseButtons.setInvoker(dropButton);
        ButtonGroup groupButtons = new ButtonGroup();
        List<Feature<?>> list = actionsButtons;
        synchronized (list) {
            for (Feature<?> b : actionsButtons) {
                if (!this.eventManager.isActionEnabled(b)) continue;
                JRadioButtonMenuItem radio = new JRadioButtonMenuItem(b.getTitle(), (Icon)b.getIcon(), b.cmd().equals(action));
                GuiUtils.applySelectedIconEffect(radio);
                radio.setActionCommand(b.cmd());
                radio.addActionListener(this);
                if ("left".equals(type)) {
                    radio.setAccelerator(KeyStroke.getKeyStroke(b.getKeyCode(), b.getModifier()));
                }
                popupMouseButtons.add(radio);
                groupButtons.add(radio);
            }
        }
        return popupMouseButtons;
    }

    private JPopupMenu getPopupMenuScroll(DropDownButton dropButton) {
        String type = dropButton.getType();
        String action = this.eventManager.getMouseActions().getAction(type);
        JPopupMenu popupMouseScroll = new JPopupMenu(type);
        popupMouseScroll.setInvoker(dropButton);
        ButtonGroup groupButtons = new ButtonGroup();
        for (Feature<?> actionW : actionsScroll) {
            if (!this.eventManager.isActionEnabled(actionW)) continue;
            JRadioButtonMenuItem radio = new JRadioButtonMenuItem(actionW.getTitle(), (Icon)actionW.getIcon(), actionW.cmd().equals(action));
            GuiUtils.applySelectedIconEffect(radio);
            radio.setActionCommand(actionW.cmd());
            radio.addActionListener(this);
            popupMouseScroll.add(radio);
            groupButtons.add(radio);
        }
        return popupMouseScroll;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JRadioButtonMenuItem item;
        Object object = e.getSource();
        if (object instanceof JRadioButtonMenuItem && (object = (item = (JRadioButtonMenuItem)object).getParent()) instanceof JPopupMenu) {
            JPopupMenu popupMenu = (JPopupMenu)object;
            MouseActions mouseActions = this.eventManager.getMouseActions();
            mouseActions.setAction(popupMenu.getLabel(), item.getActionCommand());
            ImageViewerPlugin<E> view = this.eventManager.getSelectedView2dContainer();
            if (view != null) {
                view.setMouseActions(mouseActions);
            }
            if (popupMenu.getInvoker() instanceof DropDownButton) {
                this.changeButtonState(popupMenu.getLabel(), item.getActionCommand());
            }
        }
    }

    public boolean isCommandActive(String cmd) {
        int active = this.eventManager.getMouseActions().getActiveButtons();
        return cmd != null && ViewerToolBar.checkButtonCommand(cmd, this.mouseLeft) || (active & 0x800) == 2048 && ViewerToolBar.checkButtonCommand(cmd, this.mouseMiddle) || (active & 0x1000) == 4096 && ViewerToolBar.checkButtonCommand(cmd, this.mouseRight);
    }

    private static boolean checkButtonCommand(String cmd, JButton button) {
        return button != null && cmd.equals(button.getActionCommand());
    }

    public void changeButtonState(String type, String cmd) {
        DropDownButton button = this.getDropDownButton(type);
        if (button != null) {
            Icon icon = this.buildMouseIcon(type, cmd);
            button.setIcon(icon);
            button.setActionCommand(cmd);
        }
    }

    private Icon buildMouseIcon(String type, String cmd) {
        FlatSVGIcon mouseIcon = ViewerToolBar.getMouseIcon(type);
        Feature<?> action = this.getAction(actionsButtons, cmd);
        FlatSVGIcon smallIcon = action == null ? ActionW.NO_ACTION.getIcon() : action.getIcon();
        return ViewerToolBar.getDopButtonIcon((Icon)mouseIcon, (Icon)smallIcon);
    }

    static Icon getDopButtonIcon(final Icon bckIcon, final Icon smallIcon) {
        return new DropButtonIcon(new Icon(){

            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                if (c instanceof AbstractButton) {
                    AbstractButton model = (AbstractButton)c;
                    Icon icon = null;
                    if (!model.isEnabled()) {
                        icon = UIManager.getLookAndFeel().getDisabledIcon(model, bckIcon);
                    }
                    if (icon == null) {
                        icon = bckIcon;
                    }
                    icon.paintIcon(c, g, x, y);
                    if (smallIcon != null) {
                        Icon sIcon = null;
                        if (!model.isEnabled()) {
                            sIcon = UIManager.getLookAndFeel().getDisabledIcon(model, smallIcon);
                        }
                        if (sIcon == null) {
                            sIcon = smallIcon;
                        }
                        int sx = x + bckIcon.getIconWidth() - sIcon.getIconWidth();
                        int sy = y + bckIcon.getIconHeight() - sIcon.getIconHeight();
                        sIcon.paintIcon(c, g, sx, sy);
                    }
                }
            }

            @Override
            public int getIconWidth() {
                return bckIcon.getIconWidth();
            }

            @Override
            public int getIconHeight() {
                return bckIcon.getIconHeight();
            }
        });
    }

    private DropDownButton buildSynchButton() {
        ViewGroupMenu menuLut = null;
        ComboItemListener synch = this.eventManager.getAction(ActionW.SYNCH).orElse(null);
        SynchView synchView = SynchView.DEFAULT_STACK;
        synchView.resetSynchData();
        if (synch != null) {
            Object object = synch.getSelectedItem();
            if (object instanceof SynchView) {
                SynchView sel;
                synchView = sel = (SynchView)object;
            }
            if (this.showSynchModeItems) {
                menuLut = new ViewGroupMenu(this);
                synch.registerActionState(menuLut);
            }
        }
        final JCheckBoxMenuItem jButtonSynch = new JCheckBoxMenuItem(Messages.getString("ViewerToolBar.synch_all_views"), (Icon)ResourceUtil.getToolBarIcon(ResourceUtil.ActionIcon.SYNCH).derive(20, 20), false);
        jButtonSynch.setToolTipText(Messages.getString("ViewerToolBar.synch_all_views_tooltip"));
        this.eventManager.getAction(ActionW.SYNCH_MODE).ifPresent(b -> b.registerActionState(jButtonSynch));
        DropDownButton button = new DropDownButton(this, ActionW.SYNCH.cmd(), ViewerToolBar.buildSynchIcon(synchView), menuLut){
            final /* synthetic */ ViewerToolBar this$0;
            {
                ViewerToolBar viewerToolBar = this$0;
                Objects.requireNonNull(viewerToolBar);
                this.this$0 = viewerToolBar;
                super(type, icon, model);
            }

            @Override
            protected JPopupMenu getPopupMenu() {
                MediaSeries series;
                ViewSynchData vsd;
                Object object;
                SynchView sv;
                Object object2;
                JPopupMenu menu = new JPopupMenu();
                if (this.getMenuModel() != null) {
                    JPopupMenu modelMenu = this.getMenuModel().createJPopupMenu();
                    for (Component item : modelMenu.getComponents()) {
                        menu.add(item);
                    }
                    menu.addSeparator();
                }
                menu.add(jButtonSynch);
                menu.addSeparator();
                ImageViewerPlugin optionContainer = this.this$0.eventManager.getSelectedView2dContainer();
                List<SynchOptionsCheckBoxGroup.SyncOption> opts = optionContainer != null ? optionContainer.getSyncOptions() : SynchOptionsCheckBoxGroup.getSyncOptions();
                SynchOptionsCheckBoxGroup synchOptions = new SynchOptionsCheckBoxGroup(opts);
                ComboItemListener synchAction = this.this$0.eventManager.getAction(ActionW.SYNCH).orElse(null);
                SynchView currentSynchView = synchAction != null && (object2 = synchAction.getSelectedItem()) instanceof SynchView ? (sv = (SynchView)object2) : null;
                ImageViewerPlugin popupContainer = this.this$0.eventManager.getSelectedView2dContainer();
                ViewCanvas selectedView = popupContainer != null ? popupContainer.getSelectedViewCanvas() : null;
                ViewSynchData selectedViewSynch = selectedView != null && (object = selectedView.getActionValue(ActionW.SYNCH_LINK.cmd())) instanceof ViewSynchData ? (vsd = (ViewSynchData)object) : null;
                MediaSeries mediaSeries = series = selectedView != null ? selectedView.getSeries() : null;
                if (series != null) {
                    menu.add(ViewerToolBar.createSeriesTitle(series.toString()));
                }
                HashMap<String, Boolean> effective = new HashMap<String, Boolean>();
                if (selectedViewSynch != null) {
                    effective.putAll(selectedViewSynch.getActions());
                    effective.putAll(selectedViewSynch.getUserActionOverrides());
                } else if (currentSynchView != null) {
                    effective.putAll(currentSynchView.getSynchData().getActions());
                }
                HashMap<String, List<String>> cmdMapping = new HashMap<String, List<String>>();
                for (SynchOptionsCheckBoxGroup.SyncOption opt : synchOptions.getOptions()) {
                    cmdMapping.put(opt.primary().cmd(), opt.commands());
                }
                for (JCheckBoxMenuItem item : synchOptions.createSyncOptionItems(effective)) {
                    menu.add(item);
                    if (selectedViewSynch != null && selectedViewSynch.isManualSynchActivated() && ActionW.SCROLL_SERIES.cmd().equals(item.getActionCommand())) {
                        item.setSelected(true);
                        item.setEnabled(false);
                    }
                    item.addActionListener(e -> {
                        Object patt0$temp = e.getSource();
                        if (patt0$temp instanceof JCheckBoxMenuItem) {
                            ViewSynchData v;
                            Object patt1$temp;
                            JCheckBoxMenuItem btn = (JCheckBoxMenuItem)patt0$temp;
                            boolean selected = btn.isSelected();
                            List<String> targets = cmdMapping.getOrDefault(btn.getActionCommand(), List.of(btn.getActionCommand()));
                            ImageViewerPlugin c = this.this$0.eventManager.getSelectedView2dContainer();
                            ViewCanvas sel = c != null ? c.getSelectedViewCanvas() : null;
                            ViewSynchData sd = sel != null && (patt1$temp = sel.getActionValue(ActionW.SYNCH_LINK.cmd())) instanceof ViewSynchData ? (v = (ViewSynchData)patt1$temp) : null;
                            for (String cmd : targets) {
                                Object patt1$temp2;
                                if (sd != null) {
                                    if (sd.isManualSynchActivated() && ActionW.SCROLL_SERIES.cmd().equals(cmd) && !selected) continue;
                                    sd.setUserActionOverride(cmd, selected);
                                    sd.setOriginal(false);
                                    continue;
                                }
                                ComboItemListener a = this.this$0.eventManager.getAction(ActionW.SYNCH).orElse(null);
                                if (a == null || !((patt1$temp2 = a.getSelectedItem()) instanceof SynchView)) continue;
                                SynchView sv2 = (SynchView)patt1$temp2;
                                sv2.setActionEnabled(cmd, selected);
                            }
                            if (sel != null) {
                                sel.getJComponent().repaint();
                            }
                        }
                    });
                }
                menu.setInvoker(this);
                if (selectedViewSynch != null) {
                    menu.addSeparator();
                    JMenuItem applyToAll = new JMenuItem(Messages.getString("ViewerToolBar.synch_apply_to_all"));
                    applyToAll.setIcon(this.this$0.resolveChipSwatch(selectedViewSynch.getFrameOfReferenceUID(), popupContainer));
                    applyToAll.addActionListener(e -> this.this$0.applySyncOptionsToOtherViews(selectedViewSynch));
                    menu.add(applyToAll);
                }
                return menu;
            }
        };
        button.setToolTipText(Messages.getString("ViewerToolBar.synch"));
        if (synch != null) {
            synch.registerActionState(button);
        }
        return button;
    }

    private static Component createSeriesTitle(String text) {
        JLabel label = new JLabel(text);
        Font base = label.getFont();
        label.setFont(base.deriveFont(1, base.getSize2D() + 1.0f));
        label.setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 8));
        return label;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private Icon resolveChipSwatch(String fruid, ImageViewerPlugin<E> container) {
        Object vsd;
        if (fruid == null || fruid.isBlank() || container == null) {
            return null;
        }
        LinkedHashSet<String> localDistinct = new LinkedHashSet<String>();
        for (ViewCanvas<E> v : container.getImagePanels()) {
            String uid;
            Iterator<ViewerPlugin<?>> iterator = v.getActionValue(ActionW.SYNCH_LINK.cmd());
            if (!(iterator instanceof ViewSynchData) || (uid = ((ViewSynchData)(vsd = (ViewSynchData)((Object)iterator))).getFrameOfReferenceUID()) == null || uid.isBlank()) continue;
            localDistinct.add(uid);
        }
        if (localDistinct.size() < 2) {
            return null;
        }
        LinkedHashSet<String> globalDistinct = new LinkedHashSet<String>();
        List<ViewerPlugin<?>> plugins = GuiUtils.getUICore().getViewerPlugins();
        vsd = plugins;
        synchronized (vsd) {
            for (ViewerPlugin<?> p : plugins) {
                if (!(p instanceof ImageViewerPlugin)) continue;
                ImageViewerPlugin ivp = (ImageViewerPlugin)p;
                for (ViewCanvas v : ivp.getImagePanels()) {
                    ViewSynchData vsd2;
                    String uid;
                    Object object = v.getActionValue(ActionW.SYNCH_LINK.cmd());
                    if (!(object instanceof ViewSynchData) || (uid = (vsd2 = (ViewSynchData)object).getFrameOfReferenceUID()) == null || uid.isBlank()) continue;
                    globalDistinct.add(uid);
                }
            }
        }
        int size = GuiUtils.getScaleLength(12);
        return FrameOfReferenceColor.swatch(FrameOfReferenceColor.colorFor(fruid, globalDistinct), size);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void applySyncOptionsToOtherViews(ViewSynchData source) {
        List<ViewerPlugin<?>> plugins;
        if (source == null) {
            return;
        }
        ImageViewerPlugin<E> sourceContainer = this.eventManager.getSelectedView2dContainer();
        if (sourceContainer == null) {
            return;
        }
        ViewCanvas<E> selectedView = sourceContainer.getSelectedViewCanvas();
        HashMap<String, Boolean> effective = new HashMap<String, Boolean>(source.getActions());
        effective.putAll(source.getUserActionOverrides());
        List<String> managedCommands = SynchOptionsCheckBoxGroup.getAllManagedCommands(sourceContainer.getSyncOptions());
        if (sourceContainer.isAutoSyncContainerScoped()) {
            ViewerToolBar.applyToContainer(sourceContainer, selectedView, effective, managedCommands);
            return;
        }
        List<ViewerPlugin<?>> list = plugins = GuiUtils.getUICore().getViewerPlugins();
        synchronized (list) {
            for (ViewerPlugin<?> p : plugins) {
                if (!(p instanceof ImageViewerPlugin)) continue;
                ImageViewerPlugin ivp = (ImageViewerPlugin)p;
                ViewerToolBar.applyToContainer(ivp, selectedView, effective, managedCommands);
            }
        }
    }

    private static void applyToContainer(ImageViewerPlugin<?> container, ViewCanvas<?> selectedView, Map<String, Boolean> effective, List<String> managedCommands) {
        for (ViewCanvas<?> v : container.getImagePanels()) {
            Iterator<String> iterator;
            if (v == selectedView || !((iterator = v.getActionValue(ActionW.SYNCH_LINK.cmd())) instanceof ViewSynchData)) continue;
            ViewSynchData target = (ViewSynchData)((Object)iterator);
            target.setOriginal(false);
            iterator = managedCommands.iterator();
            while (iterator.hasNext()) {
                String cmd = iterator.next();
                if (target.isManualSynchActivated() && ActionW.SCROLL_SERIES.cmd().equals(cmd)) {
                    target.setUserActionOverride(cmd, true);
                    continue;
                }
                target.setUserActionOverride(cmd, Boolean.TRUE.equals(effective.get(cmd)));
            }
            v.updateSynchState();
        }
    }

    private static Icon buildSynchIcon(SynchView synch) {
        FlatSVGIcon mouseIcon = ResourceUtil.getToolBarIcon(ResourceUtil.ActionIcon.SYNCH_LARGE);
        FlatSVGIcon smallIcon = GuiUtils.getDerivedIcon(synch.getIcon(), new FlatSVGIcon.ColorFilter().add(new Color(0x6E6E6E), new Color(3710934)));
        return new DropButtonIcon(new Icon(){
            final /* synthetic */ Icon val$mouseIcon;
            final /* synthetic */ FlatSVGIcon val$smallIcon;
            {
                this.val$mouseIcon = icon;
                this.val$smallIcon = flatSVGIcon;
            }

            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                if (c instanceof AbstractButton) {
                    AbstractButton model = (AbstractButton)c;
                    Icon icon = null;
                    if (!model.isEnabled()) {
                        icon = UIManager.getLookAndFeel().getDisabledIcon(model, this.val$mouseIcon);
                    }
                    if (icon == null) {
                        icon = this.val$mouseIcon;
                    }
                    Object sIcon = null;
                    if (!model.isEnabled()) {
                        sIcon = UIManager.getLookAndFeel().getDisabledIcon(model, (Icon)this.val$smallIcon);
                    }
                    if (sIcon == null) {
                        sIcon = this.val$smallIcon;
                    }
                    int x2 = x + this.val$mouseIcon.getIconWidth() / 2 - sIcon.getIconWidth() / 2;
                    int y2 = y + this.val$mouseIcon.getIconHeight() / 2 - sIcon.getIconHeight() / 2;
                    sIcon.paintIcon(c, g, x2, y2);
                    icon.paintIcon(c, g, x, y);
                }
            }

            @Override
            public int getIconWidth() {
                return this.val$mouseIcon.getIconWidth();
            }

            @Override
            public int getIconHeight() {
                return this.val$mouseIcon.getIconHeight();
            }
        });
    }

    public DropDownButton getDropDownButton(String type) {
        if ("left".equals(type)) {
            return this.mouseLeft;
        }
        if ("right".equals(type)) {
            return this.mouseRight;
        }
        if ("middle".equals(type)) {
            return this.mouseMiddle;
        }
        if ("wheel".equals(type)) {
            return this.mouseWheel;
        }
        return null;
    }

    public Feature<?> getToolBarAction(String command) {
        return this.getAction(actionsButtons, command);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Feature<?> getAction(List<Feature<?>> buttons, String command) {
        if (buttons != null) {
            List<Feature<?>> list = buttons;
            synchronized (list) {
                for (Feature<?> a : buttons) {
                    if (!a.cmd().equals(command)) continue;
                    return a;
                }
            }
        }
        return null;
    }

    private static FlatSVGIcon getMouseIcon(String type) {
        if ("left".equals(type)) {
            return MouseLeftIcon;
        }
        if ("right".equals(type)) {
            return MouseRightIcon;
        }
        if ("middle".equals(type)) {
            return MouseMiddleIcon;
        }
        if ("wheel".equals(type)) {
            return MouseWheelIcon;
        }
        return MouseLeftIcon;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Feature<?> getNextCommand(List<Feature<?>> buttons, String command) {
        if (buttons != null && !buttons.isEmpty()) {
            int index = 0;
            List<Feature<?>> list = buttons;
            synchronized (list) {
                for (int i = 0; i < buttons.size(); ++i) {
                    Feature<?> b = buttons.get(i);
                    if (!b.cmd().equals(command)) continue;
                    index = i == buttons.size() - 1 ? 0 : i + 1;
                    break;
                }
                return buttons.get(index);
            }
        }
        return null;
    }

    class ViewGroupMenu
    extends GroupRadioMenu<SynchView> {
        final /* synthetic */ ViewerToolBar this$0;

        ViewGroupMenu(ViewerToolBar this$0) {
            ViewerToolBar viewerToolBar = this$0;
            Objects.requireNonNull(viewerToolBar);
            this.this$0 = viewerToolBar;
        }

        @Override
        public void contentsChanged(ListDataEvent e) {
            super.contentsChanged(e);
            this.changeButtonState();
        }

        public void changeButtonState() {
            Object object;
            if (this.this$0.synchButton != null && (object = this.dataModel.getSelectedItem()) instanceof SynchView) {
                SynchView sel = (SynchView)object;
                Icon icon = ViewerToolBar.buildSynchIcon(sel);
                this.this$0.synchButton.setIcon(icon);
                this.this$0.synchButton.setActionCommand(sel.toString());
            }
        }
    }
}

