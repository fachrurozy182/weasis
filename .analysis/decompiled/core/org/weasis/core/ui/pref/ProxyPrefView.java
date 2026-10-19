/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.miginfocom.swing.MigLayout
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.core.ui.pref;

import java.awt.Component;
import java.awt.LayoutManager;
import javax.swing.ButtonGroup;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.util.AbstractItemDialogPage;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.service.WProperties;
import org.weasis.core.util.StringUtil;

public class ProxyPrefView
extends AbstractItemDialogPage {
    private static final String PROXY_MANUAL = "proxy.manual";
    private static final String PROXY_EXCEPTIONS = "proxy.exceptions";
    private static final String PROXY_HTTP_HOST = "proxy.http.host";
    private static final String PROXY_HTTP_PORT = "proxy.http.port";
    private static final String PROXY_HTTPS_HOST = "proxy.https.host";
    private static final String PROXY_HTTPS_PORT = "proxy.https.port";
    private static final String PROXY_FTP_HOST = "proxy.ftp.host";
    private static final String PROXY_FTP_PORT = "proxy.ftp.port";
    private static final String PROXY_SOCKS_HOST = "proxy.socks.host";
    private static final String PROXY_SOCKS_PORT = "proxy.socks.port";
    private final JRadioButton directConnectionRadio = new JRadioButton(Messages.getString("ProxyPrefView.direct"));
    private final JRadioButton proxyConnectionRadio = new JRadioButton(Messages.getString("ProxyPrefView.manual"));
    private final ButtonGroup buttonGroup = new ButtonGroup();
    private final JLabel lblAddress = new JLabel(Messages.getString("ProxyPrefView.host"));
    private final JLabel lblPort = new JLabel(Messages.getString("ProxyPrefView.port"));
    private final JTextField proxyHostHttp = new JTextField(20);
    private final JFormattedTextField proxyPortHttp = new JFormattedTextField();
    private final JTextField proxyHostSecure = new JTextField(20);
    private final JFormattedTextField proxyPortSecure = new JFormattedTextField();
    private final JTextField proxyHostFtp = new JTextField(20);
    private final JFormattedTextField proxyPortFtp = new JFormattedTextField();
    private final JTextField proxyHostSocks = new JTextField(20);
    private final JFormattedTextField proxyPortSocks = new JFormattedTextField();
    private final JTextField proxyExceptions = new JTextField(27);
    private final JLabel proxyLabelHttp = new JLabel("HTTP:");
    private final JLabel proxyLabelSecure = new JLabel(Messages.getString("ProxyPrefView.secure") + ":");
    private final JLabel proxyLabelFtp = new JLabel("FTP:");
    private final JLabel proxyLabelSocks = new JLabel("SOCKS:");
    private final JLabel proxyLabelExceptions = new JLabel(Messages.getString("ProxyPrefView.exceptions") + ":");

    public ProxyPrefView() {
        super(Messages.getString("ProxyPrefView.proxy"), 110);
        this.initialize();
    }

    private void initialize() {
        GuiUtils.formatPortTextField(this.proxyPortHttp);
        GuiUtils.formatPortTextField(this.proxyPortSecure);
        GuiUtils.formatPortTextField(this.proxyPortFtp);
        GuiUtils.formatPortTextField(this.proxyPortSocks);
        this.add(GuiUtils.getFlowLayoutPanel(3, 0, 15, this.directConnectionRadio));
        this.add(GuiUtils.getFlowLayoutPanel(3, 0, 2, this.proxyConnectionRadio));
        this.add(GuiUtils.getHorizontalBoxLayoutPanel(this.buildProxyPanel()));
        this.buttonGroup.add(this.directConnectionRadio);
        this.buttonGroup.add(this.proxyConnectionRadio);
        this.directConnectionRadio.addActionListener(e -> this.proxyConnectionAction(false));
        this.proxyConnectionRadio.addActionListener(e -> this.proxyConnectionAction(true));
        this.initState();
        this.add(GuiUtils.boxYLastElement(5));
        this.getProperties().setProperty("show.restore", Boolean.TRUE.toString());
        this.getProperties().setProperty("help.item", "proxy");
    }

    private void initState() {
        WProperties p = GuiUtils.getUICore().getLocalPersistence();
        this.proxyExceptions.setText(p.getProperty(PROXY_EXCEPTIONS));
        this.proxyHostHttp.setText(p.getProperty(PROXY_HTTP_HOST));
        this.proxyPortHttp.setValue(p.getIntProperty(PROXY_HTTP_PORT, 80));
        this.proxyHostSecure.setText(p.getProperty(PROXY_HTTPS_HOST));
        this.proxyPortSecure.setValue(p.getIntProperty(PROXY_HTTPS_PORT, 443));
        this.proxyHostFtp.setText(p.getProperty(PROXY_FTP_HOST));
        this.proxyPortFtp.setValue(p.getIntProperty(PROXY_FTP_PORT, 80));
        this.proxyHostSocks.setText(p.getProperty(PROXY_SOCKS_HOST));
        this.proxyPortSocks.setValue(p.getIntProperty(PROXY_SOCKS_PORT, 1080));
        if (StringUtil.hasText((CharSequence)p.getProperty("proxy.auth.pwd"))) {
            p.setProperty("proxy.auth.pwd", "");
        }
        if (p.getBooleanProperty(PROXY_MANUAL, false)) {
            this.proxyConnectionRadio.doClick();
        } else {
            this.directConnectionRadio.doClick();
        }
    }

    private JPanel buildProxyPanel() {
        JPanel dataPanel = new JPanel();
        dataPanel.setLayout((LayoutManager)new MigLayout("insets 5lp 15lp 5lp 0, fillx", "[right]rel[grow,fill][grow 0]"));
        dataPanel.add((Component)this.lblAddress, "newline, cell 1 0, growx 0, alignx center");
        dataPanel.add((Component)this.lblPort, "growx 0, alignx center");
        dataPanel.add((Component)this.proxyLabelHttp, "newline");
        dataPanel.add(this.proxyHostHttp);
        dataPanel.add(this.proxyPortHttp);
        dataPanel.add((Component)this.proxyLabelSecure, "newline");
        dataPanel.add(this.proxyHostSecure);
        dataPanel.add(this.proxyPortSecure);
        dataPanel.add((Component)this.proxyLabelFtp, "newline");
        dataPanel.add(this.proxyHostFtp);
        dataPanel.add(this.proxyPortFtp);
        dataPanel.add((Component)this.proxyLabelSocks, "newline");
        dataPanel.add(this.proxyHostSocks);
        dataPanel.add(this.proxyPortSocks);
        dataPanel.add((Component)this.proxyLabelExceptions, "newline");
        dataPanel.add((Component)this.proxyExceptions, "spanx 2");
        return dataPanel;
    }

    public void proxyConnectionAction(boolean enable) {
        this.lblAddress.setEnabled(enable);
        this.lblPort.setEnabled(enable);
        this.proxyLabelHttp.setEnabled(enable);
        this.proxyHostHttp.setEnabled(enable);
        this.proxyPortHttp.setEnabled(enable);
        this.proxyLabelSecure.setEnabled(enable);
        this.proxyHostSecure.setEnabled(enable);
        this.proxyPortSecure.setEnabled(enable);
        this.proxyLabelFtp.setEnabled(enable);
        this.proxyHostFtp.setEnabled(enable);
        this.proxyPortFtp.setEnabled(enable);
        this.proxyLabelSocks.setEnabled(enable);
        this.proxyHostSocks.setEnabled(enable);
        this.proxyPortSocks.setEnabled(enable);
        this.proxyLabelExceptions.setEnabled(enable);
        this.proxyExceptions.setEnabled(enable);
    }

    @Override
    public void resetToDefaultValues() {
        this.directConnectionRadio.doClick();
    }

    @Override
    public void closeAdditionalWindow() {
        WProperties p = GuiUtils.getUICore().getLocalPersistence();
        boolean mproxy = this.proxyConnectionRadio.isSelected();
        p.putBooleanProperty(PROXY_MANUAL, mproxy);
        String exceptions = this.proxyExceptions.getText();
        p.setProperty(PROXY_EXCEPTIONS, exceptions);
        String val = this.proxyHostHttp.getText();
        p.setProperty(PROXY_HTTP_HOST, val);
        ProxyPrefView.applyProxyProperty("http.proxyHost", val, mproxy);
        Number port = GuiUtils.getFormattedValue(this.proxyPortHttp);
        if (port != null) {
            p.putIntProperty(PROXY_HTTP_PORT, port.intValue());
            ProxyPrefView.applyProxyPortProperty("http.proxyPort", port.intValue(), val, mproxy);
        }
        if (mproxy && StringUtil.hasText((CharSequence)val)) {
            ProxyPrefView.applyProxyProperty("http.nonProxyHosts", exceptions, mproxy);
        }
        val = this.proxyHostSecure.getText();
        p.setProperty(PROXY_HTTPS_HOST, val);
        port = GuiUtils.getFormattedValue(this.proxyPortSecure);
        ProxyPrefView.applyProxyProperty("https.proxyHost", val, mproxy);
        if (port != null) {
            p.putIntProperty(PROXY_HTTPS_PORT, port.intValue());
            ProxyPrefView.applyProxyPortProperty("https.proxyPort", port.intValue(), val, mproxy);
        }
        if (mproxy && StringUtil.hasText((CharSequence)val)) {
            ProxyPrefView.applyProxyProperty("http.nonProxyHosts", exceptions, mproxy);
        }
        val = this.proxyHostFtp.getText();
        p.setProperty(PROXY_FTP_HOST, val);
        ProxyPrefView.applyProxyProperty("ftp.proxyHost", val, mproxy);
        port = GuiUtils.getFormattedValue(this.proxyPortFtp);
        if (port != null) {
            p.putIntProperty(PROXY_FTP_PORT, port.intValue());
            ProxyPrefView.applyProxyPortProperty("ftp.proxyPort", port.intValue(), val, mproxy);
        }
        if (mproxy && StringUtil.hasText((CharSequence)val)) {
            ProxyPrefView.applyProxyProperty("ftp.nonProxyHosts", exceptions, mproxy);
        }
        val = this.proxyHostSocks.getText();
        p.setProperty(PROXY_SOCKS_HOST, val);
        ProxyPrefView.applyProxyProperty("socksProxyHost", val, mproxy);
        port = GuiUtils.getFormattedValue(this.proxyPortSocks);
        if (port != null) {
            p.putIntProperty(PROXY_SOCKS_PORT, port.intValue());
            ProxyPrefView.applyProxyPortProperty("socksProxyPort", port.intValue(), val, mproxy);
        }
    }

    private static void applyProxyProperty(String key, String value, boolean manual) {
        if (manual && StringUtil.hasText((CharSequence)value)) {
            System.setProperty(key, value);
        }
    }

    private static void applyProxyPortProperty(String key, int port, String prop, boolean manual) {
        if (manual && StringUtil.hasText((CharSequence)prop)) {
            System.setProperty(key, String.valueOf(port));
        }
    }
}

