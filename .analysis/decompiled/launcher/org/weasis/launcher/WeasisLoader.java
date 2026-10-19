/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.osgi.framework.BundleContext
 */
package org.weasis.launcher;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.RootPaneContainer;
import javax.swing.UIManager;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.launcher.AutoProcessor;
import org.weasis.launcher.Messages;
import org.weasis.launcher.WeasisMainFrame;

public class WeasisLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(WeasisLoader.class);
    public static final String LBL_LOADING = Messages.getString("WebStartLoader.load");
    public static final String LBL_DOWNLOADING = Messages.getString("WebStartLoader.download");
    private JButton cancelButton;
    private JLabel loadingLabel;
    private JProgressBar downloadProgress;
    private Container container;
    private final Path resPath;
    private final WeasisMainFrame mainFrame;

    public WeasisLoader(Path resPath, WeasisMainFrame mainFrame) {
        this.resPath = resPath;
        this.mainFrame = mainFrame;
    }

    public void writeLabel(String text) {
        this.loadingLabel.setText(text);
    }

    public void initGUI() {
        this.loadingLabel = new JLabel();
        this.downloadProgress = new JProgressBar();
        this.cancelButton = new JButton();
        Font font = UIManager.getFont("h4.font");
        RootPaneContainer frame = this.mainFrame.getRootPaneContainer();
        Window win = new Window((Frame)((Object)frame));
        win.addWindowListener(new WindowAdapter(this){
            final /* synthetic */ WeasisLoader this$0;
            {
                WeasisLoader weasisLoader = this$0;
                Objects.requireNonNull(weasisLoader);
                this.this$0 = weasisLoader;
            }

            @Override
            public void windowClosing(WindowEvent evt) {
                this.this$0.closing();
            }
        });
        this.container = win;
        this.loadingLabel.setText(LBL_LOADING);
        this.loadingLabel.setFocusable(false);
        this.loadingLabel.setBorder(BorderFactory.createEmptyBorder(5, 3, 5, 3));
        this.downloadProgress.setFocusable(false);
        this.downloadProgress.setStringPainted(true);
        this.downloadProgress.setFont(font);
        this.downloadProgress.setString(LBL_LOADING);
        this.cancelButton.setFont(font);
        this.cancelButton.setText(Messages.getString("WebStartLoader.cancel"));
        this.cancelButton.addActionListener(evt -> this.closing());
        FlatSVGIcon icon = new FlatSVGIcon(this.resPath.resolve("svg/logo/WeasisAbout.svg").toUri());
        String text = String.format(Messages.getString("WebStartLoader.title"), System.getProperty("weasis.name"));
        JLabel imagePane = new JLabel(text, icon, 0);
        imagePane.setFont(UIManager.getFont("h3.font"));
        imagePane.setVerticalTextPosition(1);
        imagePane.setHorizontalTextPosition(0);
        imagePane.setFocusable(false);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add((Component)imagePane, "Center");
        JPanel panelProgress = new JPanel(new BorderLayout());
        panelProgress.add((Component)this.loadingLabel, "North");
        panelProgress.add((Component)this.downloadProgress, "Center");
        panelProgress.add((Component)this.cancelButton, "East");
        panel.add((Component)panelProgress, "South");
        panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        this.container.add((Component)panel, "Center");
        Container container = this.container;
        if (container instanceof Window) {
            Window window = (Window)container;
            window.pack();
        }
    }

    public WeasisMainFrame getMainFrame() {
        return this.mainFrame;
    }

    public void setMax(int max) {
        if (this.isClosed()) {
            return;
        }
        EventQueue.invokeLater(() -> this.downloadProgress.setMaximum(max));
    }

    public void setValue(int val) {
        if (this.isClosed()) {
            return;
        }
        EventQueue.invokeLater(() -> {
            this.downloadProgress.setString(String.format(Messages.getString("WebStartLoader.end"), val, this.downloadProgress.getMaximum()));
            this.downloadProgress.setValue(val);
            this.downloadProgress.repaint();
        });
    }

    private void closing() {
        System.exit(0);
    }

    public boolean isClosed() {
        return this.container == null;
    }

    public void open() {
        try {
            EventQueue.invokeAndWait(() -> {
                if (this.container == null) {
                    this.initGUI();
                }
                this.displayOnScreen();
            });
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        catch (InvocationTargetException e) {
            LOGGER.error("Display splashscreen", e);
        }
    }

    public void close() {
        if (this.isClosed()) {
            return;
        }
        EventQueue.invokeLater(() -> {
            Container patt0$temp;
            this.container.setVisible(false);
            if (this.container.getParent() != null) {
                this.container.getParent().remove(this.container);
            }
            if ((patt0$temp = this.container) instanceof Window) {
                Window window = (Window)patt0$temp;
                window.dispose();
            }
            this.container = null;
            this.cancelButton = null;
            this.downloadProgress = null;
            this.loadingLabel = null;
        });
    }

    private void displayOnScreen() {
        if (this.container instanceof Window) {
            try {
                Rectangle bounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getBounds();
                int x = bounds.x + (bounds.width - this.container.getWidth()) / 2;
                int y = bounds.y + (bounds.height - this.container.getHeight()) / 2;
                this.container.setLocation(x, y);
            }
            catch (Exception e) {
                LOGGER.error("Set splashscreen location", e);
            }
            this.container.setVisible(true);
        }
    }

    public void setFelix(Map<String, String> serverProp, BundleContext bundleContext, Properties modulesI18n) {
        AutoProcessor.process(serverProp, modulesI18n, bundleContext, this);
    }
}

