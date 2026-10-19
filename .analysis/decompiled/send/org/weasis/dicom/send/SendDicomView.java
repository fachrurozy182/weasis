/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.gui.util.AppProperties
 *  org.weasis.core.api.gui.util.GuiExecutor
 *  org.weasis.core.api.gui.util.GuiUtils
 *  org.weasis.core.api.gui.util.WinUtil
 *  org.weasis.core.api.net.auth.AuthMethod
 *  org.weasis.core.api.net.auth.OAuth2ServiceFactory
 *  org.weasis.core.util.StringUtil
 *  org.weasis.core.util.StringUtil$Suffix
 *  org.weasis.dicom.explorer.DicomModel
 *  org.weasis.dicom.explorer.exp.CheckTreeModel
 *  org.weasis.dicom.explorer.exp.ExportDicomView
 *  org.weasis.dicom.explorer.pref.node.AbstractDicomNode
 *  org.weasis.dicom.explorer.pref.node.AbstractDicomNode$Type
 *  org.weasis.dicom.explorer.pref.node.AbstractDicomNode$UsageType
 *  org.weasis.dicom.explorer.pref.node.AuthenticationPersistence
 *  org.weasis.dicom.explorer.pref.node.DefaultDicomNode
 *  org.weasis.dicom.explorer.pref.node.DicomWebNode
 *  org.weasis.dicom.op.CStore
 *  org.weasis.dicom.param.AdvancedParams
 *  org.weasis.dicom.param.ConnectOptions
 *  org.weasis.dicom.param.DicomNode
 *  org.weasis.dicom.param.DicomProgress
 *  org.weasis.dicom.param.DicomState
 *  org.weasis.dicom.web.ContentType
 */
package org.weasis.dicom.send;

import java.awt.Component;
import java.util.List;
import java.util.Objects;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.gui.util.AppProperties;
import org.weasis.core.api.gui.util.GuiExecutor;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.gui.util.WinUtil;
import org.weasis.core.api.net.auth.AuthMethod;
import org.weasis.core.api.net.auth.OAuth2ServiceFactory;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.explorer.DicomModel;
import org.weasis.dicom.explorer.exp.CheckTreeModel;
import org.weasis.dicom.explorer.exp.ExportDicomView;
import org.weasis.dicom.explorer.pref.node.AbstractDicomNode;
import org.weasis.dicom.explorer.pref.node.AuthenticationPersistence;
import org.weasis.dicom.explorer.pref.node.DefaultDicomNode;
import org.weasis.dicom.explorer.pref.node.DicomWebNode;
import org.weasis.dicom.op.CStore;
import org.weasis.dicom.param.AdvancedParams;
import org.weasis.dicom.param.ConnectOptions;
import org.weasis.dicom.param.DicomNode;
import org.weasis.dicom.param.DicomProgress;
import org.weasis.dicom.param.DicomState;
import org.weasis.dicom.send.Messages;
import org.weasis.dicom.send.SendDicomFactory;
import org.weasis.dicom.send.StowRS;
import org.weasis.dicom.web.ContentType;

public class SendDicomView
extends ExportDicomView {
    private static final Logger LOGGER = LoggerFactory.getLogger(SendDicomView.class);
    private static final String LAST_SEL_NODE = "lastSelNode";
    private static final String LAST_CALLING_NODE = "lastCallingNode";
    private final JComboBox<AbstractDicomNode> comboNode = new JComboBox();
    private final JComboBox<AbstractDicomNode> comboCallingNode = new JComboBox();
    private AuthMethod authMethod;

    public SendDicomView(DicomModel dicomModel, CheckTreeModel treeModel) {
        super(Messages.getString("SendDicomView.title"), 5, dicomModel, treeModel);
        this.initialize();
    }

    public SendDicomView initGUI() {
        JLabel lblDest = new JLabel(Messages.getString("SendDicomView.destination") + ":");
        GuiUtils.setPreferredWidth(this.comboNode, (int)210, (int)185);
        AbstractDicomNode.addTooltipToComboList(this.comboNode);
        JPanel panel = GuiUtils.getFlowLayoutPanel((int)2, (int)0, (JComponent[])new JComponent[]{lblDest, this.comboNode});
        if (this.comboCallingNode.getItemCount() > 0) {
            AbstractDicomNode.addTooltipToComboList(this.comboCallingNode);
            JLabel lblCalling = new JLabel(Messages.getString("calling.node") + ":");
            GuiUtils.setPreferredWidth(this.comboCallingNode, (int)160, (int)120);
            panel.add(GuiUtils.boxHorizontalStrut((int)10));
            panel.add(lblCalling);
            panel.add(this.comboCallingNode);
        }
        this.add(panel);
        this.add(GuiUtils.boxVerticalStrut((int)5));
        super.initGUI();
        return this;
    }

    protected void initialize() {
        AbstractDicomNode.loadDicomNodes(this.comboNode, (AbstractDicomNode.Type)AbstractDicomNode.Type.DICOM, (AbstractDicomNode.UsageType)AbstractDicomNode.UsageType.STORAGE);
        AbstractDicomNode.loadDicomNodes(this.comboNode, (AbstractDicomNode.Type)AbstractDicomNode.Type.WEB, (AbstractDicomNode.UsageType)AbstractDicomNode.UsageType.STORAGE);
        String desc = SendDicomFactory.EXPORT_PERSISTENCE.getProperty(LAST_SEL_NODE);
        AbstractDicomNode.selectDicomNode(this.comboNode.getModel(), (String)desc);
        String weasisAet = GuiUtils.getUICore().getSystemPreferences().getProperty("weasis.aet");
        if (!StringUtil.hasText((CharSequence)weasisAet)) {
            AbstractDicomNode.loadDicomNodes(this.comboCallingNode, (AbstractDicomNode.Type)AbstractDicomNode.Type.DICOM_CALLING, (AbstractDicomNode.UsageType)AbstractDicomNode.UsageType.STORAGE);
            String calling = SendDicomFactory.EXPORT_PERSISTENCE.getProperty(LAST_CALLING_NODE);
            AbstractDicomNode.selectDicomNode(this.comboCallingNode.getModel(), (String)calling);
        }
    }

    public void applyChange() {
        AbstractDicomNode callingNode;
        AbstractDicomNode node = (AbstractDicomNode)this.comboNode.getSelectedItem();
        if (node != null) {
            SendDicomFactory.EXPORT_PERSISTENCE.setProperty(LAST_SEL_NODE, node.getDescription());
        }
        if ((callingNode = (AbstractDicomNode)this.comboCallingNode.getSelectedItem()) != null) {
            SendDicomFactory.EXPORT_PERSISTENCE.setProperty(LAST_CALLING_NODE, callingNode.getDescription());
        }
    }

    public void closeAdditionalWindow() {
        this.applyChange();
        super.closeAdditionalWindow();
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    protected boolean exportAction(List<String> files, DicomProgress dicomProgress) {
        Object selectedItem = this.comboNode.getSelectedItem();
        if (selectedItem instanceof DefaultDicomNode) {
            DefaultDicomNode node = (DefaultDicomNode)selectedItem;
            String weasisAet = GuiUtils.getUICore().getSystemPreferences().getProperty("weasis.aet");
            if (!StringUtil.hasText((CharSequence)weasisAet)) {
                weasisAet = this.comboCallingNode.getSelectedItem() == null ? "WEASIS_AE" : ((DefaultDicomNode)this.comboCallingNode.getSelectedItem()).getAeTitle();
            }
            AdvancedParams params = new AdvancedParams();
            ConnectOptions connectOptions = new ConnectOptions();
            connectOptions.setConnectTimeout(3000);
            connectOptions.setAcceptTimeout(5000);
            params.setConnectOptions(connectOptions);
            DicomState state = CStore.process((AdvancedParams)params, (DicomNode)new DicomNode(weasisAet), (DicomNode)node.getDicomNode(), files, (DicomProgress)dicomProgress);
            if (state.getStatus() != 0 && state.getStatus() != 65024) {
                this.showErrorMessage(null, null, state);
                return false;
            }
            LOGGER.info("Dicom send: {}", (Object)state.getMessage());
            return true;
        }
        if (!(selectedItem instanceof DicomWebNode)) return true;
        DicomWebNode node = (DicomWebNode)selectedItem;
        AuthMethod auth = AuthenticationPersistence.getAuthMethod((String)node.getAuthMethodUid());
        if (!OAuth2ServiceFactory.NO_AUTH.equals(auth)) {
            String oldCode = auth.getCode();
            this.authMethod = auth;
            if (this.authMethod.getToken() == null) {
                return false;
            }
            if (!Objects.equals(oldCode, this.authMethod.getCode())) {
                AuthenticationPersistence.saveMethod();
            }
        }
        try (StowRS stowRS = new StowRS(node.getUrl().toString(), ContentType.APPLICATION_DICOM, AppProperties.WEASIS_NAME, node.getHeaders());){
            DicomState state = stowRS.uploadDicom(files, true, this.authMethod);
            if (state.getStatus() == 0) return true;
            if (state.getStatus() == 65024) return true;
            this.showErrorMessage(null, null, state);
            boolean bl = false;
            return bl;
        }
        catch (Exception e) {
            this.showErrorMessage("StowRS error: {}", e, null);
            return false;
        }
    }

    private void showErrorMessage(String title, Exception e, DicomState state) {
        if (e != null) {
            LOGGER.error(title, (Object)e.getMessage());
        }
        GuiExecutor.execute(() -> JOptionPane.showMessageDialog(WinUtil.getValidComponent((Component)this.exportTree), state == null ? Objects.requireNonNull(e).getMessage() : StringUtil.getTruncatedString((String)state.getMessage(), (int)150, (StringUtil.Suffix)StringUtil.Suffix.THREE_PTS), this.getTitle(), 0));
    }
}

