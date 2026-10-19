/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.editor.image;

import java.awt.Container;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.gui.util.CheckBoxModel;
import org.weasis.core.api.gui.util.Feature;
import org.weasis.core.api.gui.util.GroupCheckBoxMenu;

public class SynchOptionsCheckBoxGroup
extends GroupCheckBoxMenu {
    private static final List<SyncOption> DEFAULT_SYNC_OPTIONS = List.of(new SyncOption(ActionW.SCROLL_SERIES), new SyncOption(ActionW.PAN), new SyncOption(ActionW.ZOOM), new SyncOption(ActionW.ROTATION), new SyncOption(ActionW.FLIP), new SyncOption(ActionW.WINLEVEL, List.of(ActionW.WINDOW.cmd(), ActionW.LEVEL.cmd())), new SyncOption(ActionW.SPATIAL_UNIT));
    private final List<SyncOption> syncOptions;

    public static List<SyncOption> getSyncOptions() {
        return DEFAULT_SYNC_OPTIONS;
    }

    public static List<String> getAllManagedCommands() {
        return SynchOptionsCheckBoxGroup.getAllManagedCommands(DEFAULT_SYNC_OPTIONS);
    }

    public static List<String> getAllManagedCommands(List<SyncOption> options) {
        ArrayList<String> commands = new ArrayList<String>();
        for (SyncOption opt : options) {
            commands.addAll(opt.commands());
        }
        return commands;
    }

    public SynchOptionsCheckBoxGroup() {
        this(DEFAULT_SYNC_OPTIONS);
    }

    public SynchOptionsCheckBoxGroup(List<SyncOption> options) {
        this.syncOptions = Objects.requireNonNullElse(options, DEFAULT_SYNC_OPTIONS);
        ArrayList<CheckBoxModel> items = new ArrayList<CheckBoxModel>();
        for (SyncOption option : this.syncOptions) {
            items.add(new CheckBoxModel(option.primary(), true));
        }
        this.setModel(items);
    }

    public List<SyncOption> getOptions() {
        return this.syncOptions;
    }

    public List<JCheckBoxMenuItem> createSyncOptionItems(Map<String, Boolean> currentActions) {
        Map<String, Boolean> source = currentActions != null ? currentActions : Collections.emptyMap();
        ArrayList<JCheckBoxMenuItem> result = new ArrayList<JCheckBoxMenuItem>();
        for (SyncOption option : this.syncOptions) {
            boolean selected = true;
            for (String cmd : option.commands()) {
                if (Boolean.TRUE.equals(source.get(cmd))) continue;
                selected = false;
                break;
            }
            JCheckBoxMenuItem item = new JCheckBoxMenuItem(option.primary().getTitle(), selected);
            item.setActionCommand(option.primary().cmd());
            result.add(item);
        }
        return result;
    }

    public JPopupMenu buildPopupMenu(Map<String, Boolean> currentActions, BiConsumer<String, Boolean> onToggle) {
        JPopupMenu popup = new JPopupMenu();
        this.addStayOpenItemsTo(popup, currentActions, onToggle);
        return popup;
    }

    public void addStayOpenItemsTo(Container target, Map<String, Boolean> currentActions, BiConsumer<String, Boolean> onToggle) {
        for (JCheckBoxMenuItem item : this.createSyncOptionItems(currentActions)) {
            JCheckBoxMenuItem stayOpen = SynchOptionsCheckBoxGroup.wrapAsStayOpen(item);
            target.add(stayOpen);
            String triggerCmd = stayOpen.getActionCommand();
            List<String> targets = this.syncOptions.stream().filter(o -> o.primary().cmd().equals(triggerCmd)).findFirst().map(SyncOption::commands).orElse(List.of(triggerCmd));
            stayOpen.addActionListener(e -> {
                boolean selected = stayOpen.isSelected();
                for (String cmd : targets) {
                    onToggle.accept(cmd, selected);
                }
            });
        }
    }

    private static JCheckBoxMenuItem wrapAsStayOpen(JCheckBoxMenuItem source) {
        JCheckBoxMenuItem stayOpen = new JCheckBoxMenuItem(source.getText(), source.isSelected()){

            @Override
            protected void processMouseEvent(MouseEvent evt) {
                if (evt.getID() == 502 && this.contains(evt.getPoint())) {
                    this.doClick();
                    this.setArmed(true);
                } else {
                    super.processMouseEvent(evt);
                }
            }
        };
        stayOpen.setActionCommand(source.getActionCommand());
        return stayOpen;
    }

    public record SyncOption(Feature<?> primary, List<String> commands) {
        public SyncOption(Feature<?> feature) {
            this(feature, List.of(feature.cmd()));
        }
    }
}

