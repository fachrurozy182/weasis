/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.annotation.JsonIgnore
 *  com.fasterxml.jackson.annotation.JsonSubTypes
 *  com.fasterxml.jackson.annotation.JsonSubTypes$Type
 *  com.fasterxml.jackson.annotation.JsonTypeInfo
 *  com.fasterxml.jackson.annotation.JsonTypeInfo$Id
 *  com.fasterxml.jackson.databind.JavaType
 *  com.fasterxml.jackson.databind.ObjectMapper
 *  com.formdev.flatlaf.extras.FlatSVGIcon
 *  com.formdev.flatlaf.util.SystemInfo
 *  java.lang.MatchException
 *  org.osgi.framework.BundleContext
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.core.ui.launcher;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.util.SystemInfo;
import java.awt.Component;
import java.awt.Window;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.accessibility.Accessible;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicComboPopup;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.Messages;
import org.weasis.core.api.explorer.ObservableEvent;
import org.weasis.core.api.gui.util.AppProperties;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.service.BundlePreferences;
import org.weasis.core.api.service.BundleTools;
import org.weasis.core.api.util.ResourceUtil;
import org.weasis.core.ui.editor.SeriesViewer;
import org.weasis.core.ui.editor.SeriesViewerUI;
import org.weasis.core.ui.editor.image.ImageViewerEventManager;
import org.weasis.core.ui.editor.image.ViewerPlugin;
import org.weasis.core.ui.launcher.LauncherDialog;
import org.weasis.core.ui.launcher.Placeholder;
import org.weasis.core.ui.util.DefaultAction;
import org.weasis.core.ui.util.UriListFlavor;
import org.weasis.core.util.StringUtil;

public class Launcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(Launcher.class);
    private String name;
    private String iconPath;
    private boolean enable;
    private boolean button;
    private boolean local = true;
    @JsonTypeInfo(use=JsonTypeInfo.Id.NAME, property="launchType")
    @JsonSubTypes(value={@JsonSubTypes.Type(value=URIConfiguration.class, name="URI"), @JsonSubTypes.Type(value=ApplicationConfiguration.class, name="Application")})
    private Configuration configuration;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIconPath() {
        return this.iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    @JsonIgnore
    public Icon getIcon() {
        String path = this.iconPath;
        if (!StringUtil.hasText((CharSequence)path)) {
            path = ResourceUtil.ActionIcon.LAUNCH.getPath();
        }
        Object icon = null;
        if (path.endsWith(".svg")) {
            FlatSVGIcon svgIcon = ResourceUtil.getIcon(path);
            if (svgIcon.hasFound()) {
                icon = svgIcon;
            }
        } else {
            File file = ResourceUtil.getResource(path);
            if (file.canRead()) {
                icon = new ImageIcon(file.getAbsolutePath());
            }
        }
        if (icon == null) {
            icon = ResourceUtil.getIcon(ResourceUtil.ActionIcon.LAUNCH);
        }
        return icon;
    }

    @JsonIgnore
    public Icon getResizeIcon(int width, int height) {
        Icon icon = this.getIcon();
        if (icon instanceof FlatSVGIcon) {
            return ((FlatSVGIcon)icon).derive(width, height);
        }
        if (icon instanceof ImageIcon) {
            ImageIcon imageIcon = (ImageIcon)icon;
            return new ImageIcon(imageIcon.getImage().getScaledInstance(GuiUtils.getScaleLength(width), GuiUtils.getScaleLength(height), 4));
        }
        return icon;
    }

    public boolean isEnable() {
        return this.enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public boolean isButton() {
        return this.button;
    }

    public void setButton(boolean button) {
        this.button = button;
    }

    public Configuration getConfiguration() {
        return this.configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public boolean isLocal() {
        return this.local;
    }

    public void setLocal(boolean local) {
        this.local = local;
    }

    public String toString() {
        return this.name;
    }

    private String getToolTips() {
        return Messages.getString("name") + ": " + this.name + "\n" + Messages.getString("icon.path") + ": " + this.iconPath + "\n" + Messages.getString("enable") + ": " + this.enable + "\n" + Messages.getString("button") + ": " + this.button + "\n" + Messages.getString("launcher.typ") + ": " + this.configuration.launchType;
    }

    public static void addNodeActionPerformed(JComboBox<Launcher> comboBox, Type type) {
        LauncherDialog dialog = new LauncherDialog(SwingUtilities.getWindowAncestor(comboBox), type, null, comboBox);
        GuiUtils.showCenterScreen(dialog, comboBox);
    }

    public static void editNodeActionPerformed(JComboBox<Launcher> comboBox, Type type) {
        int index = comboBox.getSelectedIndex();
        if (index >= 0) {
            Launcher node = comboBox.getItemAt(index);
            if (node.isLocal()) {
                LauncherDialog dialog = new LauncherDialog(SwingUtilities.getWindowAncestor(comboBox), type, node, comboBox);
                GuiUtils.showCenterScreen(dialog, comboBox);
            } else {
                JOptionPane.showMessageDialog(comboBox, Messages.getString("only.user.created.item.modified"), Messages.getString("error"), 0);
            }
        }
    }

    public static void deleteNodeActionPerformed(JComboBox<Launcher> comboBox, Type type) {
        int index = comboBox.getSelectedIndex();
        if (index >= 0) {
            Launcher node = comboBox.getItemAt(index);
            if (node.isLocal()) {
                int response = JOptionPane.showConfirmDialog(comboBox, String.format(Messages.getString("really.want.delete"), node), Type.DICOM.toString(), 0, 2);
                if (response == 0) {
                    comboBox.removeItemAt(index);
                    Launcher.getLaunchers(type).remove(node);
                    Launcher.saveLaunchers(type);
                }
            } else {
                JOptionPane.showMessageDialog(comboBox, Messages.getString("only.user.created.item.modified"), Messages.getString("error"), 0);
            }
        }
    }

    static List<Launcher> getLaunchers(Type type) {
        return switch (type.ordinal()) {
            default -> throw new MatchException(null, null);
            case 1 -> GuiUtils.getUICore().getOtherLaunchers();
            case 0 -> GuiUtils.getUICore().getDicomLaunchers();
        };
    }

    static void saveLaunchers(Type type) {
        List<Launcher> list = Launcher.getLaunchers(type);
        BundleContext context = AppProperties.getBundleContext(Launcher.class);
        try {
            File file = BundlePreferences.getFileInDataFolder(context, type.getFilename()).toFile();
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(file, list);
        }
        catch (IOException e) {
            LOGGER.error("Cannot save the launcher configuration", (Throwable)e);
        }
        List<ViewerPlugin<?>> viewerPlugins = GuiUtils.getUICore().getViewerPlugins();
        if (viewerPlugins != null && !viewerPlugins.isEmpty()) {
            Set uiSet = viewerPlugins.stream().map(SeriesViewer::getSeriesViewerUI).collect(Collectors.toSet());
            for (SeriesViewerUI ui : uiSet) {
                if (ui.clazz.getPackageName().contains("dicom")) {
                    BundleTools.notifyDicomModel(ObservableEvent.BasicAction.UPDATE_TOOLBARS, ui);
                    continue;
                }
                BundleTools.notifyDefaultDataModel(ObservableEvent.BasicAction.UPDATE_TOOLBARS, ui);
            }
        }
    }

    public static void addTooltipToComboList(JComboBox<? extends Launcher> combo) {
        Accessible comp = combo.getUI().getAccessibleChild(combo, 0);
        if (comp instanceof BasicComboPopup) {
            BasicComboPopup popup = (BasicComboPopup)comp;
            popup.getList().getSelectionModel().addListSelectionListener(e -> {
                ListSelectionModel model;
                int first;
                if (!e.getValueIsAdjusting() && (first = (model = (ListSelectionModel)e.getSource()).getMinSelectionIndex()) >= 0) {
                    Launcher item = (Launcher)combo.getItemAt(first);
                    ((JComponent)((Object)combo.getRenderer())).setToolTipText(item.getToolTips());
                }
            });
        }
    }

    public static List<Action> getLauncherActions(ImageViewerEventManager<?> eventManager, Type type) {
        ArrayList<Action> actions = new ArrayList<Action>();
        ArrayList<Launcher> launchers = new ArrayList<Launcher>();
        if (type == Type.OTHER) {
            launchers.addAll(GuiUtils.getUICore().getOtherLaunchers());
        } else if (type == Type.DICOM) {
            launchers.addAll(GuiUtils.getUICore().getDicomLaunchers());
        } else {
            launchers.addAll(GuiUtils.getUICore().getOtherLaunchers());
            launchers.addAll(GuiUtils.getUICore().getDicomLaunchers());
        }
        for (Launcher launcher : launchers) {
            if (!launcher.isEnable() || !launcher.getConfiguration().isValid()) continue;
            String name = launcher.getName();
            Icon icon = launcher.getResizeIcon(16, 16);
            actions.add(new DefaultAction(name, icon, actionEvent -> launcher.execute(eventManager)));
        }
        return actions;
    }

    public void execute(ImageViewerEventManager<?> eventManager) {
        if (this.isEnable() && this.configuration != null && this.configuration.isValid()) {
            if (eventManager != null) {
                eventManager.dicomExportAction(this);
            }
            this.configuration.launch(eventManager);
            this.configuration.setSource(null);
        }
    }

    public static void loadLaunchers(JComboBox<Launcher> comboBox, Type type) {
        block3: {
            block2: {
                if (type != Type.DICOM) break block2;
                for (Launcher node : GuiUtils.getUICore().getDicomLaunchers()) {
                    comboBox.addItem(node);
                }
                break block3;
            }
            if (type != Type.OTHER) break block3;
            for (Launcher node : GuiUtils.getUICore().getOtherLaunchers()) {
                comboBox.addItem(node);
            }
        }
    }

    public static List<Launcher> loadLaunchers(Type type) {
        ArrayList<Launcher> list = new ArrayList<Launcher>();
        Launcher.loadLaunchers(list, ResourceUtil.getResource(type.getFilename()), false);
        AppProperties.getBundleContext(Launcher.class);
        BundleContext context = AppProperties.getBundleContext(Launcher.class);
        Launcher.loadLaunchers(list, BundlePreferences.getFileInDataFolder(context, type.getFilename()).toFile(), true);
        return list;
    }

    private static void loadLaunchers(List<Launcher> list, File resource, boolean local) {
        if (resource.canRead()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                List nodes = (List)mapper.readValue(resource, (JavaType)mapper.getTypeFactory().constructCollectionType(List.class, Launcher.class));
                for (Launcher node : nodes) {
                    node.setLocal(local);
                    list.add(node);
                }
            }
            catch (IOException e) {
                LOGGER.error("Cannot load the launcher configuration", (Throwable)e);
            }
        }
    }

    public static abstract class Configuration {
        protected static final String DICOM_SEL = "{dicom:selection.folder}";
        private final String launchType;
        private Path source;

        public Configuration(String launchType) {
            this.launchType = launchType;
        }

        public String getLaunchType() {
            return this.launchType;
        }

        @JsonIgnore
        public boolean isDicomSelectionAction() {
            return this.source != null && this.source.toString().contains(DICOM_SEL);
        }

        @JsonIgnore
        public Path getSource() {
            return this.source;
        }

        @JsonIgnore
        public void setSource(Path source) {
            this.source = source;
        }

        @JsonIgnore
        public abstract void launch(ImageViewerEventManager<?> var1);

        @JsonIgnore
        protected String resolvePlaceholders(String text, ImageViewerEventManager<?> eventManager) {
            String val = Placeholder.PREFERENCES_PLACEHOLDER.resolvePlaceholders(text, eventManager);
            if (this.source != null && StringUtil.hasText((CharSequence)val)) {
                val = Placeholder.DICOM_COPY_FOLDER_PLACEHOLDER.resolvePlaceholders(this.source.toAbsolutePath().toString(), eventManager);
            }
            if (eventManager != null) {
                return eventManager.resolvePlaceholders(val);
            }
            return val;
        }

        @JsonIgnore
        void showInvalidField(Window parent) {
            if (this instanceof URIConfiguration) {
                LauncherDialog.ShowRequiredValue(parent, Messages.getString("uri"));
            } else {
                LauncherDialog.ShowRequiredValue(parent, Messages.getString("application"));
            }
        }

        @JsonIgnore
        public abstract boolean isValid();
    }

    public static enum Type {
        DICOM(Messages.getString("dicom.launcher"), "dicomLaunchers.json"),
        OTHER(Messages.getString("other.launcher"), "otherLaunchers.json");

        final String title;
        final String filename;

        private Type(String title, String filename) {
            this.title = title;
            this.filename = filename;
        }

        public String toString() {
            return this.title;
        }

        public String getFilename() {
            return this.filename;
        }
    }

    public static class ApplicationConfiguration
    extends Configuration {
        private String binaryPath;
        private List<String> parameters;
        private String workingDirectory;
        private Map<String, String> environmentVariables;
        private Compatibility compatibility = SystemInfo.isWindows ? Compatibility.WINDOWS : (SystemInfo.isMacOS ? Compatibility.MAC : Compatibility.LINUX);

        public ApplicationConfiguration() {
            super(Messages.getString("application"));
        }

        public String getBinaryPath() {
            return this.binaryPath;
        }

        public void setBinaryPath(String binaryPath) {
            this.binaryPath = binaryPath;
        }

        public List<String> getParameters() {
            return this.parameters;
        }

        public void setParameters(List<String> parameters) {
            this.parameters = parameters;
        }

        public String getWorkingDirectory() {
            return this.workingDirectory;
        }

        public void setWorkingDirectory(String workingDirectory) {
            this.workingDirectory = workingDirectory;
        }

        public Map<String, String> getEnvironmentVariables() {
            return this.environmentVariables;
        }

        public void setEnvironmentVariables(Map<String, String> environmentVariables) {
            this.environmentVariables = environmentVariables;
        }

        public Compatibility getCompatibility() {
            return this.compatibility;
        }

        public void setCompatibility(Compatibility compatibility) {
            this.compatibility = compatibility;
        }

        @JsonIgnore
        public boolean isCompatibleWithCurrentSystem() {
            if (this.compatibility == null) {
                return true;
            }
            if (SystemInfo.isWindows) {
                return this.compatibility == Compatibility.WINDOWS;
            }
            if (SystemInfo.isMacOS) {
                return this.compatibility == Compatibility.MAC;
            }
            return this.compatibility == Compatibility.LINUX;
        }

        @Override
        @JsonIgnore
        public boolean isDicomSelectionAction() {
            boolean sel = super.isDicomSelectionAction();
            if (sel) {
                return true;
            }
            if (this.parameters != null && !this.parameters.isEmpty()) {
                for (String param : this.parameters) {
                    if (!StringUtil.hasText((CharSequence)param) || !param.contains("{dicom:selection.folder}")) continue;
                    return true;
                }
            }
            if (this.environmentVariables != null && !this.environmentVariables.isEmpty()) {
                for (String val : this.environmentVariables.values()) {
                    if (!StringUtil.hasText((CharSequence)val) || !val.contains("{dicom:selection.folder}")) continue;
                    return true;
                }
            }
            return false;
        }

        @Override
        public void launch(ImageViewerEventManager<?> eventManager) {
            if (!StringUtil.hasText((CharSequence)this.binaryPath)) {
                return;
            }
            boolean isMac = this.compatibility == Compatibility.MAC;
            ArrayList<String> command = new ArrayList<String>(Arrays.asList(this.binaryPath.trim().split("\\s+")));
            if (!isMac && this.parameters != null && !this.parameters.isEmpty()) {
                for (String param : this.parameters) {
                    command.add(this.resolvePlaceholders(param, eventManager));
                }
            }
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            if (StringUtil.hasText((CharSequence)this.workingDirectory)) {
                processBuilder.directory(new File(this.workingDirectory));
            }
            if (this.environmentVariables != null && !this.environmentVariables.isEmpty()) {
                Map<String, String> environment = processBuilder.environment();
                for (Map.Entry<String, String> entry : this.environmentVariables.entrySet()) {
                    environment.put(entry.getKey(), this.resolvePlaceholders(entry.getValue(), eventManager));
                }
            }
            Thread launcherThread = Thread.ofVirtual().unstarted(() -> {
                try {
                    String data;
                    Process p = processBuilder.start();
                    BufferedReader buffer = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    for (int lineCount = 0; lineCount < 5 && (data = buffer.readLine()) != null; ++lineCount) {
                        System.out.println(data);
                    }
                    int val = this.getExitValue(p);
                    if (isMac && this.parameters != null && !this.parameters.isEmpty()) {
                        for (String param : this.parameters) {
                            command.add(this.resolvePlaceholders(param, eventManager));
                        }
                        Thread.sleep(500L);
                        p = processBuilder.start();
                        val = this.getExitValue(p);
                    }
                    if (val != 0) {
                        JOptionPane.showMessageDialog((Component)GuiUtils.getUICore().getBaseArea(), String.format(Messages.getString("error.launching.app"), this.binaryPath), Messages.getString("launcher.error"), 0);
                    }
                }
                catch (IOException e1) {
                    LOGGER.error("Running cmd", (Throwable)e1);
                }
                catch (InterruptedException e2) {
                    LOGGER.error("Cannot get the exit status of {}", (Object)this.binaryPath, (Object)e2);
                    Thread.currentThread().interrupt();
                }
            });
            launcherThread.start();
        }

        private int getExitValue(Process p) throws InterruptedException {
            if (!p.waitFor(15L, TimeUnit.SECONDS)) {
                LOGGER.warn("Process did not exit within the timeout, detaching the process.");
                return 0;
            }
            return p.exitValue();
        }

        @Override
        public boolean isValid() {
            return StringUtil.hasText((CharSequence)this.binaryPath) && this.isCompatibleWithCurrentSystem();
        }
    }

    public static class URIConfiguration
    extends Configuration {
        private String uri;

        public URIConfiguration() {
            super("URI");
        }

        public String getUri() {
            return this.uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        @Override
        @JsonIgnore
        public boolean isDicomSelectionAction() {
            boolean sel = super.isDicomSelectionAction();
            if (sel) {
                return true;
            }
            return this.uri != null && this.uri.contains("{dicom:selection.folder}");
        }

        @Override
        public void launch(ImageViewerEventManager<?> eventManager) {
            try {
                String val = this.resolvePlaceholders(this.uri, eventManager);
                URI ref = new URI(val);
                GuiUtils.openInDefaultBrowser((Component)GuiUtils.getUICore().getBaseArea(), ref);
            }
            catch (Exception e1) {
                LOGGER.error("Opening launcher", (Throwable)e1);
            }
        }

        @Override
        public boolean isValid() {
            if (!StringUtil.hasText((CharSequence)this.uri)) {
                return false;
            }
            return UriListFlavor.isValidURI(this.uri.replaceAll("\\{.*?}", ""));
        }
    }

    public static enum Compatibility {
        LINUX("Linux"),
        MAC("macOS"),
        WINDOWS("Windows");

        final String title;

        private Compatibility(String title) {
            this.title = title;
        }

        public String toString() {
            return this.title;
        }
    }
}

