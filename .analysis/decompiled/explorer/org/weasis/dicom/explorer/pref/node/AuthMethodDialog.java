/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.miginfocom.swing.MigLayout
 *  org.weasis.core.api.gui.util.GuiUtils
 *  org.weasis.core.api.gui.util.WinUtil
 *  org.weasis.core.api.net.NetworkUtil
 *  org.weasis.core.api.net.auth.AuthMethod
 *  org.weasis.core.api.net.auth.AuthProvider
 *  org.weasis.core.api.net.auth.AuthRegistration
 *  org.weasis.core.api.net.auth.DefaultAuthMethod
 *  org.weasis.core.api.net.auth.OAuth2ServiceFactory
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.dicom.explorer.pref.node;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.LayoutManager;
import java.awt.Window;
import java.util.Objects;
import java.util.UUID;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import net.miginfocom.swing.MigLayout;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.gui.util.WinUtil;
import org.weasis.core.api.net.NetworkUtil;
import org.weasis.core.api.net.auth.AuthMethod;
import org.weasis.core.api.net.auth.AuthProvider;
import org.weasis.core.api.net.auth.AuthRegistration;
import org.weasis.core.api.net.auth.DefaultAuthMethod;
import org.weasis.core.api.net.auth.OAuth2ServiceFactory;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.explorer.Messages;
import org.weasis.dicom.explorer.pref.node.AuthenticationPersistence;

public class AuthMethodDialog
extends JDialog {
    private final AuthMethod authMethod;
    private final JComboBox<AuthMethod> parentCombobox;
    private final JComboBox<AuthMethod> comboBoxAuth = new JComboBox();
    private final Border spaceY = BorderFactory.createEmptyBorder(10, 3, 0, 3);
    private final JTextField name = new JTextField(50);
    private final JTextField authorizationURI = new JTextField(50);
    private final JTextField tokenURI = new JTextField(50);
    private final JTextField revokeTokenURI = new JTextField(50);
    private final JCheckBox oidc = new JCheckBox("OpenID Connect");
    private final JTextField clientID = new JTextField(50);
    private final JTextField clientSecret = new JTextField(50);
    private final JTextField scope = new JTextField(50);
    private final JTextField audience = new JTextField(50);
    private final JComboBox<String> grantType = new JComboBox<String>(new String[]{"code", "client_credentials"});

    public AuthMethodDialog(Window parent, String title, AuthMethod authMethod, JComboBox<AuthMethod> parentCombobox) {
        super(parent, title, Dialog.ModalityType.APPLICATION_MODAL);
        this.parentCombobox = parentCombobox;
        this.comboBoxAuth.addItem((AuthMethod)OAuth2ServiceFactory.GOOGLE_AUTH_TEMPLATE);
        this.comboBoxAuth.addItem((AuthMethod)OAuth2ServiceFactory.KEYCLOAK_TEMPLATE);
        boolean addAuth = false;
        if (authMethod == null) {
            addAuth = true;
            this.authMethod = new DefaultAuthMethod(UUID.randomUUID().toString(), new AuthProvider(null, null, null, null, false), AuthRegistration.empty());
            this.authMethod.setLocal(true);
        } else {
            this.authMethod = authMethod;
        }
        this.initComponents(addAuth);
        this.fill(this.authMethod);
        this.pack();
    }

    private void initComponents(boolean addAuth) {
        JPanel panel = new JPanel();
        panel.setLayout((LayoutManager)new MigLayout("insets 10lp 15lp 10lp 15lp", "[grow ,fill][grow 0]"));
        if (addAuth) {
            this.buildHeader(panel);
        }
        panel.add((Component)new JLabel("ID: " + this.authMethod.getUid()), "newline, spanx");
        panel.add((Component)this.getProvider(), "newline, spanx");
        panel.add((Component)this.getRegistration(), "newline, spanx");
        this.buildFooter(panel);
        this.setContentPane(panel);
    }

    private void buildFooter(JPanel panel) {
        JButton okButton = new JButton(Messages.getString("PrinterDialog.ok"));
        okButton.addActionListener(e -> this.okButtonActionPerformed());
        JButton cancelButton = new JButton(Messages.getString("PrinterDialog.cancel"));
        cancelButton.addActionListener(e -> this.dispose());
        JButton helpButton = GuiUtils.createHelpButton((String)"dicomweb-config");
        panel.add((Component)GuiUtils.getFlowLayoutPanel((int)4, (int)0, (int)0, (JComponent[])new JComponent[]{helpButton, GuiUtils.boxHorizontalStrut((int)15), okButton, GuiUtils.boxHorizontalStrut((int)15), cancelButton}), "newline, spanx, gap 15lp 0lp 10lp 10lp, alignx trailing");
    }

    public void buildHeader(JPanel panel) {
        JLabel headersLabel = new JLabel(Messages.getString("template") + ":");
        JButton buttonFill = new JButton(Messages.getString("fill"));
        buttonFill.addActionListener(e -> {
            AuthMethod m = (AuthMethod)this.comboBoxAuth.getSelectedItem();
            if (OAuth2ServiceFactory.KEYCLOAK_TEMPLATE.equals(m)) {
                JTextField textFieldName = new JTextField();
                JTextField textFieldURL = new JTextField();
                JTextField textFieldRealm = new JTextField();
                Object[] inputFields = new Object[]{Messages.getString("name"), textFieldName, "Base URL", textFieldURL, "Realm", textFieldRealm};
                int option = JOptionPane.showConfirmDialog(WinUtil.getValidComponent((Component)this), inputFields, Messages.getString("enter.keycloak.inf"), 2, -1);
                if (option == 0) {
                    AuthProvider p = OAuth2ServiceFactory.buildKeycloakProvider((String)textFieldName.getText(), (String)textFieldURL.getText(), (String)textFieldRealm.getText());
                    m = new DefaultAuthMethod(UUID.randomUUID().toString(), p, m.getAuthRegistration());
                    m.setLocal(true);
                }
            }
            this.fill(m);
        });
        panel.add((Component)GuiUtils.getFlowLayoutPanel((int)0, (int)0, (JComponent[])new JComponent[]{headersLabel, this.comboBoxAuth, GuiUtils.boxHorizontalStrut((int)15), buttonFill}), "newline");
    }

    public JPanel getProvider() {
        MigLayout layout = new MigLayout("fillx, insets 5lp", "[right]rel[grow,fill]");
        JPanel panel = new JPanel((LayoutManager)layout);
        panel.setBorder(BorderFactory.createCompoundBorder(this.spaceY, GuiUtils.getTitledBorder((String)"Provider")));
        panel.add((Component)new JLabel("Name:"), "newline");
        panel.add((Component)this.name, "");
        GuiUtils.addValidation((JTextField)this.name, StringUtil::hasText);
        panel.add((Component)new JLabel("Authorization URI:"), "newline");
        panel.add((Component)this.authorizationURI, "");
        GuiUtils.addValidation((JTextField)this.authorizationURI, NetworkUtil::isValidUrlLikeUri);
        panel.add((Component)new JLabel("Token URI:"), "newline");
        GuiUtils.addValidation((JTextField)this.tokenURI, NetworkUtil::isValidUrlLikeUri);
        panel.add((Component)this.tokenURI, "");
        panel.add((Component)new JLabel("Revoke URI:"), "newline");
        GuiUtils.addValidation((JTextField)this.revokeTokenURI, NetworkUtil::isValidUrlLikeUri);
        panel.add((Component)this.revokeTokenURI, "");
        return panel;
    }

    public JPanel getRegistration() {
        MigLayout layout = new MigLayout("fillx, ins 5lp", "[right]rel[grow,fill]");
        JPanel panel = new JPanel((LayoutManager)layout);
        panel.setBorder(BorderFactory.createCompoundBorder(this.spaceY, GuiUtils.getTitledBorder((String)"Registration")));
        panel.add((Component)new JLabel("Grant Type:"), "newline");
        panel.add(this.grantType, "");
        panel.add((Component)new JLabel("Client ID:"), "newline");
        panel.add((Component)this.clientID, "");
        panel.add((Component)new JLabel("Client Secret:"), "newline");
        panel.add((Component)this.clientSecret, "");
        panel.add((Component)new JLabel("Scope:"), "newline");
        panel.add((Component)this.scope, "");
        panel.add((Component)new JLabel("Audience:"), "newline");
        panel.add((Component)this.audience, "");
        return panel;
    }

    private void fill(AuthMethod authMethod) {
        if (authMethod != null) {
            AuthProvider provider = authMethod.getAuthProvider();
            this.name.setText(provider.name());
            this.authorizationURI.setText(provider.authorizationUri());
            this.tokenURI.setText(provider.tokenUri());
            this.revokeTokenURI.setText(provider.revokeTokenUri());
            this.oidc.setSelected(provider.openId());
            AuthRegistration reg = authMethod.getAuthRegistration();
            this.clientID.setText(reg.clientId());
            this.clientSecret.setText(reg.clientSecret());
            this.scope.setText(reg.scope());
            this.audience.setText(reg.audience());
            this.grantType.setSelectedItem(reg.getAuthorizationGrantType());
        }
    }

    private void okButtonActionPerformed() {
        String n = this.name.getText();
        String authURI = this.authorizationURI.getText();
        String tURI = this.tokenURI.getText();
        String rURI = this.revokeTokenURI.getText();
        if (!(StringUtil.hasText((CharSequence)n) && StringUtil.hasText((CharSequence)authURI) && NetworkUtil.isValidUrlLikeUri((String)authURI) && NetworkUtil.isValidUrlLikeUri((String)tURI) && NetworkUtil.isValidUrlLikeUri((String)tURI))) {
            JOptionPane.showMessageDialog(WinUtil.getValidComponent((Component)this), Messages.getString("missing.fields"), Messages.getString("error"), 0);
            return;
        }
        AuthProvider currentProvider = this.authMethod.getAuthProvider();
        boolean addMethod = currentProvider.name() == null;
        AuthProvider newProvider = new AuthProvider(n, authURI, tURI, rURI, this.oidc.isSelected());
        DefaultAuthMethod updatedAuth = this.updateAuthMethod(newProvider);
        if (addMethod) {
            this.parentCombobox.addItem((AuthMethod)updatedAuth);
            this.parentCombobox.setSelectedItem(updatedAuth);
        } else {
            int index = -1;
            for (int i = 0; i < this.parentCombobox.getItemCount(); ++i) {
                if (!this.parentCombobox.getItemAt(i).getUid().equals(this.authMethod.getUid())) continue;
                index = i;
                break;
            }
            if (index >= 0) {
                this.parentCombobox.removeItemAt(index);
                this.parentCombobox.insertItemAt((AuthMethod)updatedAuth, index);
                this.parentCombobox.setSelectedIndex(index);
            }
        }
        this.comboBoxAuth.repaint();
        AuthenticationPersistence.addOrUpdateMethod((AuthMethod)updatedAuth);
        this.dispose();
    }

    private DefaultAuthMethod updateAuthMethod(AuthProvider newProvider) {
        AuthRegistration newRegistration = new AuthRegistration(this.clientID.getText(), this.clientSecret.getText(), this.scope.getText(), this.audience.getText(), this.authMethod.getAuthRegistration().user(), (String)this.grantType.getSelectedItem());
        DefaultAuthMethod updatedAuth = new DefaultAuthMethod(this.authMethod.getUid(), newProvider, newRegistration);
        updatedAuth.setLocal(this.authMethod.isLocal());
        if (this.isSameGrantContext(newProvider, newRegistration)) {
            updatedAuth.setCode(this.authMethod.getCode());
        }
        return updatedAuth;
    }

    private boolean isSameGrantContext(AuthProvider provider, AuthRegistration registration) {
        AuthProvider oldProvider = this.authMethod.getAuthProvider();
        AuthRegistration oldRegistration = this.authMethod.getAuthRegistration();
        return Objects.equals(oldProvider.authorizationUri(), provider.authorizationUri()) && Objects.equals(oldProvider.tokenUri(), provider.tokenUri()) && Objects.equals(oldProvider.openId(), provider.openId()) && Objects.equals(oldRegistration.clientId(), registration.clientId()) && Objects.equals(oldRegistration.clientSecret(), registration.clientSecret()) && Objects.equals(oldRegistration.scope(), registration.scope()) && Objects.equals(oldRegistration.getAuthorizationGrantType(), registration.getAuthorizationGrantType());
    }
}

