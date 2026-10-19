/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.osgi.framework.BundleContext
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.gui.util.AppProperties
 *  org.weasis.core.api.gui.util.GuiUtils
 *  org.weasis.core.api.service.BundlePreferences
 *  org.weasis.core.api.service.WProperties
 *  org.weasis.core.api.util.ResourceUtil
 *  org.weasis.core.util.StreamUtil
 *  org.weasis.core.util.StringUtil
 *  org.weasis.dicom.codec.TransferSyntax
 *  org.weasis.dicom.codec.utils.DicomResource
 */
package org.weasis.dicom.explorer.pref.node;

import java.awt.Window;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.accessibility.Accessible;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.gui.util.AppProperties;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.service.BundlePreferences;
import org.weasis.core.api.service.WProperties;
import org.weasis.core.api.util.ResourceUtil;
import org.weasis.core.util.StreamUtil;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.codec.TransferSyntax;
import org.weasis.dicom.codec.utils.DicomResource;
import org.weasis.dicom.explorer.Messages;
import org.weasis.dicom.explorer.pref.node.DefaultDicomNode;
import org.weasis.dicom.explorer.pref.node.DicomNodeDialog;
import org.weasis.dicom.explorer.pref.node.DicomPrintNode;
import org.weasis.dicom.explorer.pref.node.DicomWebNode;
import org.weasis.dicom.explorer.pref.node.DicomWebNodeDialog;

public abstract class AbstractDicomNode {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDicomNode.class);
    protected static final String T_NODES = "nodes";
    protected static final String T_NODE = "node";
    protected static final String T_DESCRIPTION = "description";
    protected static final String T_TYPE = "type";
    protected static final String T_USAGE_TYPE = "usageType";
    protected static final String T_TSUID = "tsuid";
    private String description;
    private TransferSyntax tsuid;
    private Type type;
    private UsageType usageType;
    private boolean local;

    protected AbstractDicomNode(String description, Type type, UsageType usageType) {
        if (type == null) {
            throw new IllegalArgumentException("Type cannot be null");
        }
        this.description = description;
        this.tsuid = TransferSyntax.NONE;
        this.type = type;
        this.usageType = usageType;
        this.local = true;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String toString() {
        return this.description;
    }

    public TransferSyntax getTsuid() {
        return this.tsuid;
    }

    public void setTsuid(TransferSyntax tsuid) {
        this.tsuid = tsuid;
    }

    public String getToolTips() {
        return this.description;
    }

    public Type getType() {
        return this.type;
    }

    public void setType(Type type) {
        this.type = type == null ? Type.DICOM : type;
    }

    public UsageType getUsageType() {
        return this.usageType;
    }

    public void setUsageType(UsageType usageType) {
        this.usageType = usageType;
    }

    public boolean isLocal() {
        return this.local;
    }

    public void setLocal(boolean local) {
        this.local = local;
    }

    public void saveDicomNode(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeAttribute(T_DESCRIPTION, this.description);
        writer.writeAttribute(T_TYPE, StringUtil.getEmptyStringIfNullEnum((Enum)this.type));
        writer.writeAttribute(T_USAGE_TYPE, StringUtil.getEmptyStringIfNullEnum((Enum)this.usageType));
        writer.writeAttribute(T_TSUID, StringUtil.getEmptyStringIfNullEnum((Enum)this.tsuid));
    }

    public static void loadDicomNodes(JComboBox<AbstractDicomNode> comboBox, Type type) {
        AbstractDicomNode.loadDicomNodes(comboBox, type, UsageType.BOTH);
    }

    public static void loadDicomNodes(JComboBox<AbstractDicomNode> comboBox, Type type, UsageType usage) {
        AbstractDicomNode.loadDicomNodes(comboBox, type, usage, null);
    }

    public static void loadDicomNodes(JComboBox<AbstractDicomNode> comboBox, Type type, UsageType usage, DicomWebNode.WebType webType) {
        List<AbstractDicomNode> list = AbstractDicomNode.loadDicomNodes(type, usage, webType);
        for (AbstractDicomNode node : list) {
            comboBox.addItem(node);
        }
    }

    public static List<AbstractDicomNode> loadDicomNodes(Type type, UsageType usage) {
        return AbstractDicomNode.loadDicomNodes(type, usage, null);
    }

    public static List<AbstractDicomNode> loadDicomNodes(Type type, UsageType usage, DicomWebNode.WebType webType) {
        ArrayList<AbstractDicomNode> list = new ArrayList<AbstractDicomNode>();
        AbstractDicomNode.loadDicomNodes(list, ResourceUtil.getResource((String)type.getFilename()), type, false, usage, webType);
        BundleContext context = AppProperties.getBundleContext(AbstractDicomNode.class);
        AbstractDicomNode.loadDicomNodes(list, BundlePreferences.getFileInDataFolder((BundleContext)context, (String)type.getFilename()).toFile(), type, true, usage, webType);
        return list;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void saveDicomNodes(JComboBox<? extends AbstractDicomNode> comboBox, Type type) {
        XMLStreamWriter writer = null;
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        BundleContext context = AppProperties.getBundleContext(AbstractDicomNode.class);
        try {
            writer = factory.createXMLStreamWriter(new FileOutputStream(BundlePreferences.getFileInDataFolder((BundleContext)context, (String)type.getFilename()).toString()), "UTF-8");
            writer.writeStartDocument("UTF-8", "1.0");
            writer.writeStartElement(T_NODES);
            for (int i = 0; i < comboBox.getItemCount(); ++i) {
                AbstractDicomNode node = comboBox.getItemAt(i);
                if (!node.isLocal() || type != node.getType()) continue;
                writer.writeStartElement(T_NODE);
                node.saveDicomNode(writer);
                writer.writeEndElement();
            }
            writer.writeEndElement();
            writer.writeEndDocument();
            writer.flush();
            StreamUtil.safeClose((XMLStreamWriter)writer);
        }
        catch (Exception e) {
            LOGGER.error("Error on writing DICOM node file", (Throwable)e);
        }
        finally {
            StreamUtil.safeClose(writer);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void loadDicomNodes(List<AbstractDicomNode> list, File prefs, Type type, boolean local, UsageType usage, DicomWebNode.WebType webType) {
        if (prefs.canRead()) {
            XMLStreamReader xmler = null;
            try {
                XMLInputFactory factory = XMLInputFactory.newInstance();
                factory.setProperty("javax.xml.stream.isSupportingExternalEntities", Boolean.FALSE);
                factory.setProperty("javax.xml.stream.supportDTD", Boolean.FALSE);
                xmler = factory.createXMLStreamReader(new FileInputStream(prefs));
                while (xmler.hasNext()) {
                    int eventType = xmler.next();
                    if (eventType != 1) continue;
                    AbstractDicomNode.readDicomNodes(xmler, list, type, local, usage, webType);
                }
                StreamUtil.safeClose((XMLStreamReader)xmler);
            }
            catch (Exception e) {
                LOGGER.error("Error on reading DICOM node file", (Throwable)e);
            }
            finally {
                StreamUtil.safeClose(xmler);
            }
        }
    }

    private static void readDicomNodes(XMLStreamReader xmler, List<AbstractDicomNode> list, Type type, boolean local, UsageType usage, DicomWebNode.WebType webType) throws XMLStreamException {
        String key = xmler.getName().getLocalPart();
        if (T_NODES.equals(key)) {
            while (xmler.hasNext()) {
                int eventType = xmler.next();
                if (eventType != 1) continue;
                AbstractDicomNode.readDicomNode(xmler, list, type, local, usage, webType);
            }
        }
    }

    private static void readDicomNode(XMLStreamReader xmler, List<AbstractDicomNode> list, Type type, boolean local, UsageType usage, DicomWebNode.WebType webType) {
        String key = xmler.getName().getLocalPart();
        if (T_NODE.equals(key)) {
            try {
                AbstractDicomNode node;
                UsageType u;
                Type t = Type.valueOf(xmler.getAttributeValue(null, T_TYPE));
                if (type != t) {
                    return;
                }
                if (usage != UsageType.BOTH && (u = UsageType.valueOf(xmler.getAttributeValue(null, T_USAGE_TYPE))) != UsageType.BOTH && u != usage) {
                    return;
                }
                if (Type.WEB == type) {
                    DicomWebNode.WebType wt = DicomWebNode.WebType.valueOf(xmler.getAttributeValue(null, "webtype"));
                    if (webType != null && (webType == DicomWebNode.WebType.WADO && webType != wt || webType != wt && wt != DicomWebNode.WebType.DICOMWEB)) {
                        return;
                    }
                    node = DicomWebNode.buildDicomWebNode(xmler);
                } else {
                    node = Type.PRINTER == type ? DicomPrintNode.buildDicomPrintNode(xmler) : DefaultDicomNode.buildDicomNodeEx(xmler);
                }
                node.setLocal(local);
                node.setType(t);
                list.add(node);
            }
            catch (Exception e) {
                LOGGER.error("Cannot read DicomNode", (Throwable)e);
            }
        }
    }

    public static void addNodeActionPerformed(JComboBox<? extends AbstractDicomNode> comboBox, Type type) {
        JDialog dialog = Type.WEB == type ? new DicomWebNodeDialog(SwingUtilities.getWindowAncestor(comboBox), Type.WEB.toString(), null, comboBox) : new DicomNodeDialog(SwingUtilities.getWindowAncestor(comboBox), Type.DICOM.toString(), null, comboBox, type);
        GuiUtils.showCenterScreen((Window)dialog, comboBox);
    }

    public static void editNodeActionPerformed(JComboBox<? extends AbstractDicomNode> comboBox) {
        AbstractDicomNode node = (AbstractDicomNode)comboBox.getSelectedItem();
        if (node != null) {
            if (node.isLocal()) {
                Type type = node.getType();
                JDialog dialog = Type.WEB == type ? new DicomWebNodeDialog(SwingUtilities.getWindowAncestor(comboBox), Type.WEB.toString(), (DicomWebNode)node, comboBox) : new DicomNodeDialog(SwingUtilities.getWindowAncestor(comboBox), Type.DICOM.toString(), (DefaultDicomNode)node, comboBox, type);
                GuiUtils.showCenterScreen((Window)dialog, comboBox);
            } else {
                JOptionPane.showMessageDialog(comboBox, Messages.getString("AbstractDicomNode.only_usr_cr_msg"), Messages.getString("DicomPrintDialog.error"), 0);
            }
        }
    }

    public static void deleteNodeActionPerformed(JComboBox<? extends AbstractDicomNode> comboBox) {
        int index = comboBox.getSelectedIndex();
        if (index >= 0) {
            AbstractDicomNode node = comboBox.getItemAt(index);
            if (node.isLocal()) {
                int response = JOptionPane.showConfirmDialog(comboBox, String.format(Messages.getString("AbstractDicomNode.delete_msg"), node), Type.DICOM.toString(), 0, 2);
                if (response == 0) {
                    comboBox.removeItemAt(index);
                    AbstractDicomNode.saveDicomNodes(comboBox, node.getType());
                }
            } else {
                JOptionPane.showMessageDialog(comboBox, Messages.getString("AbstractDicomNode.only_usr_cr_msg"), Messages.getString("DicomPrintDialog.error"), 0);
            }
        }
    }

    public static void addTooltipToComboList(JComboBox<? extends AbstractDicomNode> combo) {
        Accessible comp = combo.getUI().getAccessibleChild(combo, 0);
        if (comp instanceof BasicComboPopup) {
            BasicComboPopup popup = (BasicComboPopup)comp;
            popup.getList().getSelectionModel().addListSelectionListener(e -> {
                ListSelectionModel model;
                int first;
                if (!e.getValueIsAdjusting() && (first = (model = (ListSelectionModel)e.getSource()).getMinSelectionIndex()) >= 0) {
                    AbstractDicomNode item = (AbstractDicomNode)combo.getItemAt(first);
                    ((JComponent)((Object)combo.getRenderer())).setToolTipText(item.getToolTips());
                }
            });
        }
    }

    public static void selectDicomNode(ComboBoxModel<AbstractDicomNode> model, String desc) {
        if (model != null && StringUtil.hasText((CharSequence)desc)) {
            for (int i = 0; i < model.getSize(); ++i) {
                if (!desc.equals(((AbstractDicomNode)model.getElementAt(i)).getDescription())) continue;
                model.setSelectedItem(model.getElementAt(i));
                break;
            }
        }
    }

    public static void nodeSelectionPersistence(WProperties prefs, AbstractDicomNode node, String key) {
        if (node != null && prefs != null) {
            prefs.setProperty(key, node.getDescription());
        }
    }

    public static void restoreNodeSelection(WProperties prefs, ComboBoxModel<AbstractDicomNode> model, String key) {
        if (prefs != null) {
            String desc = prefs.getProperty(key);
            AbstractDicomNode.selectDicomNode(model, desc);
        }
    }

    public static enum Type {
        DICOM(Messages.getString("AbstractDicomNode.dcm_node"), "dicomNodes.xml"),
        DICOM_CALLING(Messages.getString("AbstractDicomNode.dcm_calling_node"), DicomResource.CALLING_NODES.getPath()),
        PRINTER(Messages.getString("AbstractDicomNode.dcm_printer"), "dicomPrinterNodes.xml"),
        WEB(Messages.getString("AbstractDicomNode.dcm_web_node"), "dicomWebNodes.xml");

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

    public static enum UsageType {
        STORAGE(Messages.getString("AbstractDicomNode.storage")),
        RETRIEVE(Messages.getString("AbstractDicomNode.retrieve")),
        BOTH(Messages.getString("AbstractDicomNode.both"));

        final String title;

        private UsageType(String title) {
            this.title = title;
        }

        public String toString() {
            return this.title;
        }
    }

    public static enum RetrieveType {
        CMOVE("C-MOVE"),
        CGET("C-GET"),
        WADO("WADO-URI");

        final String title;

        private RetrieveType(String title) {
            this.title = title;
        }

        public String toString() {
            return this.title;
        }
    }
}

