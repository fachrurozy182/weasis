/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.formdev.flatlaf.FlatIconColors
 *  com.formdev.flatlaf.extras.FlatSVGIcon
 *  com.formdev.flatlaf.extras.FlatSVGIcon$ColorFilter
 *  com.formdev.flatlaf.icons.FlatTreeCollapsedIcon
 *  com.formdev.flatlaf.ui.FlatUIUtils
 *  com.formdev.flatlaf.util.SystemInfo
 *  com.formdev.flatlaf.util.UIScale
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.core.api.gui.util;

import com.formdev.flatlaf.FlatIconColors;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.icons.FlatTreeCollapsedIcon;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.SystemInfo;
import com.formdev.flatlaf.util.UIScale;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.util.WinUtil;
import org.weasis.core.api.service.UICore;
import org.weasis.core.api.util.FontItem;
import org.weasis.core.util.StringUtil;

public class GuiUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(GuiUtils.class);
    public static final String HTML_START = "<html>";
    public static final String HTML_COLOR_START = "<html><font color='";
    public static final String HTML_COLOR_PATTERN = "<html><font color=\"%s\">%s</font></html>\n";
    public static final String HTML_END = "</html>";
    public static final String HTML_BR = "<br>";
    public static final String NEWLINE = "newline";
    private static final List<String> LINUX_BROWSER_FALLBACKS = List.of("x-www-browser", "google-chrome", "google-chrome-stable", "brave-browser", "brave-browser-stable", "vivaldi", "vivaldi-stable", "microsoft-edge", "microsoft-edge-stable", "opera", "chromium", "chromium-browser", "firefox");

    private GuiUtils() {
    }

    public static UICore getUICore() {
        return UICore.getInstance();
    }

    public static Dimension getBigIconButtonSize(JComponent c) {
        Insets insets = c.getInsets();
        int size = c.getFontMetrics(c.getFont()).getHeight() + insets.top + insets.bottom;
        return new Dimension(size, size);
    }

    public static Dimension getComponentSizeFromText(JComponent c, String text) {
        Font font = c.getFont();
        if (font == null) {
            font = FontItem.DEFAULT.getFont();
        }
        Insets insets = c.getInsets();
        int width = c.getFontMetrics(font).stringWidth(text) + insets.left + insets.right;
        int height = c.getFontMetrics(font).getHeight() + insets.top + insets.bottom;
        return new Dimension(width, height);
    }

    public static int getComponentWidthFromText(JComponent c, String text) {
        Font font = c.getFont();
        if (font == null) {
            font = FontItem.DEFAULT.getFont();
        }
        Insets insets = c.getInsets();
        return c.getFontMetrics(font).stringWidth(text) + insets.left + insets.right;
    }

    public static FlatSVGIcon getDerivedIcon(FlatSVGIcon flatSVGIcon, FlatSVGIcon.ColorFilter filter) {
        FlatSVGIcon icon = new FlatSVGIcon(flatSVGIcon);
        icon.setColorFilter(filter);
        return icon;
    }

    public static void applySelectedIconEffect(AbstractButton button) {
        Icon icon = button.getIcon();
        if (icon instanceof FlatSVGIcon) {
            FlatSVGIcon flatSVGIcon = (FlatSVGIcon)icon;
            button.setSelectedIcon((Icon)GuiUtils.getDerivedIcon(flatSVGIcon, GuiUtils.getSelectedColorFilter(button)));
        }
    }

    public static FlatSVGIcon.ColorFilter getSelectedColorFilter(JComponent c) {
        FlatSVGIcon.ColorFilter colorFilter = new FlatSVGIcon.ColorFilter();
        Color color = IconColor.ACTIONS_BLUE.color;
        if (c instanceof JMenuItem) {
            color = FlatUIUtils.getUIColor((String)"MenuItem.selectionForeground", (Color)color);
        }
        colorFilter.add(new Color(0x6E6E6E), color);
        return colorFilter;
    }

    public static Icon getUpArrowIcon() {
        return new FlatTreeCollapsedIcon(){

            protected void paintIcon(Component c, Graphics2D g) {
                g.rotate(Math.toRadians(-90.0), (double)this.width / 2.0, (double)this.height / 2.0);
                super.paintIcon(c, g);
            }
        };
    }

    public static Icon getDownArrowIcon() {
        return UIManager.getIcon("Tree.expandedIcon");
    }

    public static int getScaleLength(int length) {
        return (int)((float)length * UIScale.getUserScaleFactor());
    }

    public static float getScaleLength(float length) {
        return length * UIScale.getUserScaleFactor();
    }

    public static double getScaleLength(double length) {
        return length * (double)UIScale.getUserScaleFactor();
    }

    public static void rightToLeftChanged(Container c, boolean rightToLeft) {
        c.applyComponentOrientation(rightToLeft ? ComponentOrientation.RIGHT_TO_LEFT : ComponentOrientation.LEFT_TO_RIGHT);
        c.revalidate();
        c.repaint();
    }

    public static TitledBorder getTitledBorder(String title) {
        return new TitledBorder(null, title, 0, 0, FontItem.DEFAULT_SEMIBOLD.getFont(), null);
    }

    public static Border getEmptyBorder(int gap) {
        int g = GuiUtils.getScaleLength(gap);
        return new EmptyBorder(g, g, g, g);
    }

    public static Border getEmptyBorder(int horizontal, int vertical) {
        int h = GuiUtils.getScaleLength(horizontal);
        int v = GuiUtils.getScaleLength(vertical);
        return new EmptyBorder(v, h, v, h);
    }

    public static Border getEmptyBorder(int top, int left, int bottom, int right) {
        return new EmptyBorder(GuiUtils.getScaleLength(top), GuiUtils.getScaleLength(left), GuiUtils.getScaleLength(bottom), GuiUtils.getScaleLength(right));
    }

    public static JPanel getFlowLayoutPanel(JComponent ... items) {
        return GuiUtils.getFlowLayoutPanel(3, 5, 5, items);
    }

    public static JPanel getFlowLayoutPanel(int horizontalGap, int verticalGap, JComponent ... items) {
        return GuiUtils.getFlowLayoutPanel(3, horizontalGap, verticalGap, items);
    }

    public static JPanel getFlowLayoutPanel(int align, int horizontalGap, int verticalGap, JComponent ... items) {
        int h = GuiUtils.getScaleLength(horizontalGap);
        int v = GuiUtils.getScaleLength(verticalGap);
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(align, h, v));
        for (JComponent item : items) {
            panel.add(item);
        }
        return panel;
    }

    public static JPanel getHorizontalBoxLayoutPanel(JComponent ... items) {
        return GuiUtils.getBoxLayoutPanel(2, 0, false, items);
    }

    public static JPanel getVerticalBoxLayoutPanel(JComponent ... items) {
        return GuiUtils.getBoxLayoutPanel(3, 0, false, items);
    }

    public static JPanel getHorizontalBoxLayoutPanel(int gap, JComponent ... items) {
        return GuiUtils.getBoxLayoutPanel(2, gap, true, items);
    }

    public static JPanel getVerticalBoxLayoutPanel(int gap, JComponent ... items) {
        return GuiUtils.getBoxLayoutPanel(3, gap, true, items);
    }

    private static JPanel getBoxLayoutPanel(int axis, int gap, boolean externalGap, JComponent ... items) {
        boolean vertical;
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, axis));
        boolean bl = vertical = axis == 3 || axis == 1;
        if (externalGap && gap > 0) {
            panel.add(vertical ? GuiUtils.boxVerticalStrut(gap) : GuiUtils.boxHorizontalStrut(gap));
        }
        for (JComponent item : items) {
            panel.add(item);
            if (gap <= 0) continue;
            panel.add(vertical ? GuiUtils.boxVerticalStrut(gap) : GuiUtils.boxHorizontalStrut(gap));
        }
        if (gap > 0 && !externalGap) {
            panel.remove(panel.getComponentCount() - 1);
        }
        return panel;
    }

    public static JComponent boxVerticalStrut(int height) {
        int v = GuiUtils.getScaleLength(height);
        return new Box.Filler(new Dimension(0, v), new Dimension(0, v), new Dimension(Short.MAX_VALUE, v));
    }

    public static JComponent boxHorizontalStrut(int width) {
        int w = GuiUtils.getScaleLength(width);
        return new Box.Filler(new Dimension(w, 0), new Dimension(w, 0), new Dimension(w, Short.MAX_VALUE));
    }

    public static Box.Filler boxXLastElement(int minimumWidth) {
        int w = GuiUtils.getScaleLength(minimumWidth);
        return new Box.Filler(new Dimension(w, 0), new Dimension(w, 0), new Dimension(Integer.MAX_VALUE, 0));
    }

    public static Box.Filler boxYLastElement(int minimumHeight) {
        int h = GuiUtils.getScaleLength(minimumHeight);
        return new Box.Filler(new Dimension(0, h), new Dimension(0, h), new Dimension(0, Integer.MAX_VALUE));
    }

    public static Dimension getDimension(int width, int height) {
        int w = GuiUtils.getScaleLength(width);
        int h = GuiUtils.getScaleLength(height);
        return new Dimension(w, h);
    }

    public static void setPreferredWidth(Component component, int width, int minWidth) {
        Dimension dim = component.getPreferredSize();
        dim.width = GuiUtils.getScaleLength(width);
        component.setPreferredSize(dim);
        dim = component.getMinimumSize();
        dim.width = GuiUtils.getScaleLength(minWidth);
        component.setMinimumSize(dim);
        component.setMaximumSize(new Dimension(Short.MAX_VALUE, dim.height));
    }

    public static void setWidth(Component component, int width) {
        int sWidth = GuiUtils.getScaleLength(width);
        Dimension dim = component.getMinimumSize();
        dim.width = sWidth;
        component.setMinimumSize(dim);
        dim = component.getPreferredSize();
        dim.width = sWidth;
        component.setPreferredSize(dim);
        component.setMaximumSize(dim);
    }

    public static void setPreferredWidth(Component component, int width) {
        GuiUtils.setPreferredWidth(component, width, 50);
    }

    public static void setPreferredHeight(Component component, int height) {
        GuiUtils.setPreferredHeight(component, height, 50);
    }

    public static void setPreferredHeight(Component component, int height, int minHeight) {
        Dimension dim = component.getPreferredSize();
        dim.height = GuiUtils.getScaleLength(height);
        component.setPreferredSize(dim);
        dim = component.getMinimumSize();
        dim.height = GuiUtils.getScaleLength(minHeight);
        component.setMinimumSize(dim);
    }

    public static void showCenterScreen(Window window) {
        GuiUtils.showCenterScreen(window, null, WindowPosition.CENTER);
    }

    public static void showCenterScreen(Window window, Component parent) {
        GuiUtils.showCenterScreen(window, parent, WindowPosition.CENTER);
    }

    public static void showCenterScreen(Window window, Component parent, WindowPosition direction) {
        try {
            int x;
            Rectangle bound;
            if (parent == null) {
                bound = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getBounds();
            } else {
                Dimension sSize = parent.getSize();
                Point p = parent.getLocationOnScreen();
                bound = new Rectangle(p.x, p.y, sSize.width, sSize.height);
            }
            int windowWidth = window.getWidth();
            int windowHeight = window.getHeight();
            window.setLocation(x, switch (direction.ordinal()) {
                case 1 -> {
                    x = bound.x;
                    yield bound.y;
                }
                case 2 -> {
                    x = bound.x + (bound.width - windowWidth) / 2;
                    yield bound.y;
                }
                case 3 -> {
                    x = bound.x + bound.width - windowWidth;
                    yield bound.y;
                }
                case 4 -> {
                    x = bound.x;
                    yield bound.y + (bound.height - windowHeight) / 2;
                }
                case 5 -> {
                    x = bound.x + bound.width - windowWidth;
                    yield bound.y + (bound.height - windowHeight) / 2;
                }
                case 6 -> {
                    x = bound.x;
                    yield bound.y + bound.height - windowHeight;
                }
                case 7 -> {
                    x = bound.x + (bound.width - windowWidth) / 2;
                    yield bound.y + bound.height - windowHeight;
                }
                case 8 -> {
                    x = bound.x + bound.width - windowWidth;
                    yield bound.y + bound.height - windowHeight;
                }
                default -> {
                    x = bound.x + (bound.width - windowWidth) / 2;
                    yield bound.y + (bound.height - windowHeight) / 2;
                }
            });
        }
        catch (Exception e) {
            LOGGER.error("Cannot position the window", (Throwable)e);
        }
        window.setVisible(true);
    }

    public static void addChangeListener(JSlider slider, ChangeListener listener) {
        ChangeListener[] listeners;
        for (ChangeListener changeListener : listeners = slider.getChangeListeners()) {
            if (listener != changeListener) continue;
            return;
        }
        slider.addChangeListener(listener);
    }

    public static JSlider createSlider(int min, int max, int value, int tickDivisions) {
        JSlider slider = new JSlider(min, max, value);
        slider.setMajorTickSpacing(Math.max(1, (max - min) / tickDivisions));
        slider.setPaintTicks(true);
        return slider;
    }

    private static void addCheckActionToJFormattedTextField(final JFormattedTextField textField) {
        textField.getInputMap().put(KeyStroke.getKeyStroke(10, 0), "check");
        textField.getActionMap().put("check", new AbstractAction(){

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    textField.commitEdit();
                    textField.postActionEvent();
                }
                catch (ParseException pe) {
                    LOGGER.error("Exception when commit value in {}", (Object)textField.getClass().getName(), (Object)pe);
                }
                textField.setValue(textField.getValue());
            }
        });
    }

    public static void addCheckAction(JFormattedTextField textField) {
        textField.setHorizontalAlignment(4);
        GuiUtils.addCheckActionToJFormattedTextField(textField);
    }

    public static void setNumberModel(JSpinner spin, int val, int min, int max, int delta) {
        spin.setModel(new SpinnerNumberModel(val < min ? min : Math.min(val, max), min, max, delta));
        JFormattedTextField ftf = ((JSpinner.DefaultEditor)spin.getEditor()).getTextField();
        GuiUtils.addCheckActionToJFormattedTextField(ftf);
    }

    public static void setSpinnerWidth(JSpinner spin, int valueWidth) {
        JComponent mySpinnerEditor = spin.getEditor();
        JFormattedTextField ftf = ((JSpinner.DefaultEditor)mySpinnerEditor).getTextField();
        ftf.setColumns(valueWidth);
    }

    public static void formatCheckAction(JSpinner spin) {
        GuiUtils.formatCheckAction(spin, null);
    }

    public static void formatCheckAction(JSpinner spin, JFormattedTextField.AbstractFormatter defaultFormat) {
        JFormattedTextField ftf = ((JSpinner.DefaultEditor)spin.getEditor()).getTextField();
        if (defaultFormat != null) {
            ftf.setFormatterFactory(new DefaultFormatterFactory(defaultFormat));
        }
        GuiUtils.addCheckActionToJFormattedTextField(ftf);
    }

    public static Number getFormattedValue(JFormattedTextField textField) {
        JFormattedTextField.AbstractFormatterFactory formatter = textField.getFormatterFactory();
        if (formatter instanceof DefaultFormatterFactory) {
            DefaultFormatterFactory factory = (DefaultFormatterFactory)formatter;
            if (textField.getFormatter().equals(factory.getEditFormatter())) {
                try {
                    textField.commitEdit();
                }
                catch (ParseException pe) {
                    LOGGER.error("Exception when commit value in {}", (Object)textField.getClass().getName(), (Object)pe);
                }
            }
        }
        Number val = null;
        try {
            val = (Number)textField.getValue();
        }
        catch (Exception e) {
            LOGGER.error("Cannot get number form textField", (Throwable)e);
        }
        return val;
    }

    public static void addValidation(final JTextField textField, final Predicate<String> validationLogic) {
        textField.putClientProperty("JComponent.outline", "error");
        textField.getDocument().addDocumentListener(new DocumentListener(){

            @Override
            public void insertUpdate(DocumentEvent e) {
                this.validate();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                this.validate();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                this.validate();
            }

            private void validate() {
                String text = textField.getText().trim();
                if (validationLogic.test(text)) {
                    textField.putClientProperty("JComponent.outline", null);
                } else {
                    textField.putClientProperty("JComponent.outline", "error");
                }
            }
        });
    }

    public static void formatPortTextField(JFormattedTextField port) {
        NumberFormat portFormat = NumberFormat.getNumberInstance();
        portFormat.setMinimumIntegerDigits(0);
        portFormat.setMaximumIntegerDigits(65535);
        portFormat.setMaximumFractionDigits(0);
        port.setFormatterFactory(new DefaultFormatterFactory(new NumberFormatter(portFormat)));
        port.setColumns(5);
        GuiUtils.addCheckAction(port);
    }

    public static JMenuItem menuItem(String label, ActionListener listener, String command, int mnemonic, int acceleratorKey) {
        JMenuItem item = new JMenuItem(label);
        item.addActionListener(listener);
        item.setActionCommand(command);
        if (mnemonic != 0) {
            item.setMnemonic((char)mnemonic);
        }
        if (acceleratorKey != 0) {
            item.setAccelerator(KeyStroke.getKeyStroke(acceleratorKey, 2));
        }
        return item;
    }

    public static JButton createHelpButton(String topic) {
        JButton jButtonHelp = new JButton();
        jButtonHelp.putClientProperty("JButton.buttonType", "help");
        jButtonHelp.setToolTipText(Messages.getString("online.documentation"));
        jButtonHelp.addActionListener(GuiUtils.createHelpActionListener(jButtonHelp, topic));
        return jButtonHelp;
    }

    public static JPanel buildHelpMessagePanel(String message, String helpTopic) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        JTextArea textArea = new JTextArea(message);
        textArea.setEditable(false);
        textArea.setOpaque(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setColumns(40);
        panel.add((Component)textArea, "Center");
        panel.add((Component)GuiUtils.createHelpButton(helpTopic), "East");
        return panel;
    }

    public static ActionListener createHelpActionListener(JButton jButtonHelp, String topic) {
        return actionEvent -> GuiUtils.openInDefaultBrowser((Component)jButtonHelp, URI.create(GuiUtils.getUICore().getSystemPreferences().getProperty("weasis.help.online") + topic));
    }

    public static int getMaxLength(Rectangle bounds) {
        return Math.max(bounds.width, bounds.height);
    }

    public static void openInDefaultBrowser(Component parent, URL url) {
        if (url != null) {
            try {
                GuiUtils.openInDefaultBrowser(parent, url.toURI());
            }
            catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void openInDefaultBrowser(Component parent, URI uri) {
        if (uri != null) {
            if (SystemInfo.isLinux) {
                GuiUtils.openInLinuxBrowser(parent, uri);
            } else if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    try {
                        desktop.browse(uri);
                    }
                    catch (IOException e) {
                        LOGGER.error("Cannot open URL to the desktop browser", (Throwable)e);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(WinUtil.getValidComponent(parent), Messages.getString("JMVUtils.browser") + ": " + String.valueOf(uri), Messages.getString("JMVUtils.error"), 0);
            }
        }
    }

    public static void openSystemExplorer(Component parent, Path path) {
        if (path != null) {
            if (SystemInfo.isLinux) {
                GuiUtils.openCommand(parent, "xdg-open", path.toString());
            } else if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE_FILE_DIR)) {
                Desktop.getDesktop().browseFileDirectory(path.toFile());
            } else if (SystemInfo.isWindows) {
                GuiUtils.openCommand(parent, "explorer", path.toString());
            } else if (SystemInfo.isMacOS) {
                GuiUtils.openCommand(parent, "/usr/bin/open", path.toString());
            } else {
                JOptionPane.showMessageDialog(WinUtil.getValidComponent(parent), Messages.getString("JMVUtils.browser") + ": " + String.valueOf(path), Messages.getString("JMVUtils.error"), 0);
            }
        }
    }

    private static void openInLinuxBrowser(Component parent, URI uri) {
        if ("file".equalsIgnoreCase(uri.getScheme())) {
            String browser = GuiUtils.findLinuxBrowserCommand();
            GuiUtils.openCommand(parent, browser == null ? "xdg-open" : browser, uri.toString());
        } else {
            GuiUtils.openCommand(parent, "xdg-open", uri.toString());
        }
    }

    private static String findLinuxBrowserCommand() {
        String defaultBrowser;
        ArrayList<String> candidates = new ArrayList<String>();
        String browserEnv = System.getenv("BROWSER");
        if (StringUtil.hasText((CharSequence)browserEnv)) {
            for (String entry : browserEnv.split(File.pathSeparator)) {
                String exe = entry.trim().split("\\s+")[0];
                if (exe.isEmpty()) continue;
                candidates.add(exe);
            }
        }
        if ((defaultBrowser = GuiUtils.resolveLinuxDefaultBrowser()) != null) {
            candidates.add(defaultBrowser);
        }
        candidates.addAll(LINUX_BROWSER_FALLBACKS);
        return candidates.stream().filter(GuiUtils::isExecutableOnPath).findFirst().orElse(null);
    }

    private static String resolveLinuxDefaultBrowser() {
        try {
            String desktopEntry;
            Process process = new ProcessBuilder("xdg-settings", "get", "default-web-browser").redirectError(ProcessBuilder.Redirect.DISCARD).start();
            process.getOutputStream().close();
            if (process.waitFor(5L, TimeUnit.SECONDS) && process.exitValue() == 0 && StringUtil.hasText((CharSequence)(desktopEntry = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).strip()))) {
                return GuiUtils.execFromDesktopEntry(desktopEntry);
            }
        }
        catch (IOException e) {
            LOGGER.debug("Cannot query the default web browser", (Throwable)e);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return null;
    }

    private static String execFromDesktopEntry(String desktopEntry) {
        Object fileName = desktopEntry.endsWith(".desktop") ? desktopEntry : desktopEntry + ".desktop";
        for (Path dir : GuiUtils.linuxApplicationDirs()) {
            Path desktopFile = dir.resolve((String)fileName);
            if (!Files.isReadable(desktopFile)) continue;
            try {
                for (String line : Files.readAllLines(desktopFile, StandardCharsets.UTF_8)) {
                    if (!line.startsWith("Exec=")) continue;
                    String exec = line.substring(5).trim();
                    return exec.isEmpty() ? null : exec.split("\\s+")[0];
                }
            }
            catch (IOException e) {
                LOGGER.debug("Cannot read desktop entry {}", (Object)desktopFile, (Object)e);
            }
        }
        return null;
    }

    private static List<Path> linuxApplicationDirs() {
        ArrayList<Path> dirs = new ArrayList<Path>();
        String dataHome = System.getenv("XDG_DATA_HOME");
        if (StringUtil.hasText((CharSequence)dataHome)) {
            dirs.add(Path.of(dataHome, "applications"));
        } else {
            dirs.add(Path.of(System.getProperty("user.home"), ".local", "share", "applications"));
        }
        String dataDirs = System.getenv("XDG_DATA_DIRS");
        if (!StringUtil.hasText((CharSequence)dataDirs)) {
            dataDirs = "/usr/local/share:/usr/share";
        }
        for (String dir : dataDirs.split(File.pathSeparator)) {
            if (dir.isEmpty()) continue;
            dirs.add(Path.of(dir, "applications"));
        }
        dirs.add(Path.of("/var/lib/snapd/desktop/applications", new String[0]));
        return dirs;
    }

    private static boolean isExecutableOnPath(String command) {
        if (command.indexOf(File.separatorChar) >= 0) {
            return Files.isExecutable(Path.of(command, new String[0]));
        }
        String path = System.getenv("PATH");
        if (!StringUtil.hasText((CharSequence)path)) {
            return false;
        }
        for (String dir : path.split(File.pathSeparator)) {
            if (dir.isEmpty() || !Files.isExecutable(Path.of(dir, command))) continue;
            return true;
        }
        return false;
    }

    private static void openCommand(Component parent, String cmd, String target) {
        try {
            Process process = new ProcessBuilder(cmd, target).redirectOutput(ProcessBuilder.Redirect.DISCARD).redirectError(ProcessBuilder.Redirect.DISCARD).start();
            process.getOutputStream().close();
            if (process.waitFor(5L, TimeUnit.SECONDS) && process.exitValue() != 0) {
                LOGGER.error("'{}' failed (exit {}) for {}", new Object[]{cmd, process.exitValue(), target});
                GuiUtils.showOpenCommandError(parent, target);
            }
        }
        catch (IOException e) {
            LOGGER.error("Cannot run '{}' for {}", new Object[]{cmd, target, e});
            GuiUtils.showOpenCommandError(parent, target);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void showOpenCommandError(Component parent, String target) {
        JOptionPane.showMessageDialog(WinUtil.getValidComponent(parent), Messages.getString("JMVUtils.browser") + ": " + target, Messages.getString("JMVUtils.error"), 0);
    }

    public static HyperlinkListener buildHyperlinkListener() {
        return e -> {
            JTextPane pane = (JTextPane)e.getSource();
            if (e.getEventType() == HyperlinkEvent.EventType.ENTERED) {
                pane.setToolTipText(e.getDescription());
            } else if (e.getEventType() == HyperlinkEvent.EventType.EXITED) {
                pane.setToolTipText(null);
            } else if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                Component c;
                Object patt0$temp = e.getSource();
                Component parent = patt0$temp instanceof Component ? (c = (Component)patt0$temp) : null;
                GuiUtils.openInDefaultBrowser(WinUtil.getValidComponent(parent), e.getURL());
            }
        };
    }

    public static JTextPane getPanelWithHyperlink(String html) {
        JTextPane jTextPane1 = new JTextPane();
        jTextPane1.setContentType("text/html");
        jTextPane1.setEditable(false);
        jTextPane1.addHyperlinkListener(GuiUtils.buildHyperlinkListener());
        jTextPane1.setText(html);
        return jTextPane1;
    }

    public static void addItemToMenu(JMenu menu, JMenuItem item) {
        if (menu != null && item != null) {
            menu.add(item);
        }
    }

    public static void addItemToMenu(JPopupMenu menu, Component item) {
        if (menu != null && item != null) {
            menu.add(item);
        }
    }

    public static Object[] setRenderingHints(Graphics g, boolean antialiasing, boolean stroke, boolean text) {
        Graphics2D g2 = (Graphics2D)g;
        Object[] oldRenderingHints = new Object[]{g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING), g2.getRenderingHint(RenderingHints.KEY_STROKE_CONTROL), g2.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING)};
        if (antialiasing) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        if (stroke) {
            boolean useQuartz = Boolean.getBoolean("apple.awt.graphics.UseQuartz");
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, useQuartz ? RenderingHints.VALUE_STROKE_PURE : RenderingHints.VALUE_STROKE_NORMALIZE);
        }
        if (text) {
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
        return oldRenderingHints;
    }

    public static void resetRenderingHints(Graphics g, Object[] oldRenderingHints) {
        if (oldRenderingHints.length == 3) {
            Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldRenderingHints[0]);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, oldRenderingHints[1]);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, oldRenderingHints[2]);
        }
    }

    public static int insetWidth(JPanel panel) {
        Insets insets;
        if (panel != null && (insets = panel.getInsets()) != null) {
            return insets.left + insets.right;
        }
        return 0;
    }

    public static int insetHeight(JPanel panel) {
        Insets insets;
        if (panel != null && (insets = panel.getInsets()) != null) {
            return insets.top + insets.bottom;
        }
        return 0;
    }

    public static enum IconColor {
        ACTIONS_RED(UIManager.getColor(FlatIconColors.ACTIONS_RED.key)),
        ACTIONS_YELLOW(UIManager.getColor(FlatIconColors.ACTIONS_YELLOW.key)),
        ACTIONS_GREEN(UIManager.getColor(FlatIconColors.ACTIONS_GREEN.key)),
        ACTIONS_BLUE(UIManager.getColor(FlatIconColors.ACTIONS_BLUE.key)),
        ACTIONS_GREY(UIManager.getColor(FlatIconColors.ACTIONS_GREY.key)),
        ACTIONS_GREY_INLINE(UIManager.getColor(FlatIconColors.ACTIONS_GREYINLINE.key));

        public final Color color;

        private IconColor(Color color) {
            this.color = color;
        }

        public Color getColor() {
            return this.color;
        }

        public String getHtmlCode() {
            return String.format("rgb(%d,%d,%d)", this.color.getRed(), this.color.getGreen(), this.color.getBlue());
        }
    }

    public static enum WindowPosition {
        CENTER,
        NW,
        N,
        NE,
        W,
        E,
        SW,
        S,
        SE;

    }
}

