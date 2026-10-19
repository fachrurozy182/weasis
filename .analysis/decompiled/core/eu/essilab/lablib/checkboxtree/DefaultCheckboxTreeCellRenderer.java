/*
 * Decompiled with CFR 0.152.
 */
package eu.essilab.lablib.checkboxtree;

import eu.essilab.lablib.checkboxtree.CheckboxTree;
import eu.essilab.lablib.checkboxtree.CheckboxTreeCellRenderer;
import eu.essilab.lablib.checkboxtree.QuadristateButtonModel;
import eu.essilab.lablib.checkboxtree.QuadristateCheckbox;
import eu.essilab.lablib.checkboxtree.TreeCheckingModel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.plaf.ColorUIResource;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

public class DefaultCheckboxTreeCellRenderer
extends JPanel
implements CheckboxTreeCellRenderer {
    protected QuadristateCheckbox checkBox = new QuadristateCheckbox();
    protected DefaultTreeCellRenderer label = new DefaultTreeCellRenderer();

    protected static ImageIcon loadIcon(String iconFile) {
        try {
            return new ImageIcon(DefaultCheckboxTreeCellRenderer.class.getClassLoader().getResource(iconFile));
        }
        catch (NullPointerException npe) {
            return null;
        }
    }

    public DefaultCheckboxTreeCellRenderer() {
        this.setLayout(new FlowLayout(0, 0, 0));
        this.setOpaque(false);
        this.add(this.checkBox);
        this.add(this.label);
        this.label.setBackgroundNonSelectionColor(new Color(0, 0, 0, 0));
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d_check = this.checkBox.getPreferredSize();
        Dimension d_label = this.label.getPreferredSize();
        return new Dimension(d_check.width + d_label.width, d_check.height < d_label.height ? d_label.height : d_check.height);
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object object, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        this.label.getTreeCellRendererComponent(tree, object, selected, expanded, leaf, row, hasFocus);
        if (tree instanceof CheckboxTree) {
            TreePath path = tree.getPathForRow(row);
            TreeCheckingModel checkingModel = ((CheckboxTree)tree).getCheckingModel();
            this.checkBox.setEnabled(checkingModel.isPathEnabled(path) && tree.isEnabled());
            boolean checked = checkingModel.isPathChecked(path);
            boolean greyed = checkingModel.isPathGreyed(path);
            if (checked && !greyed) {
                this.checkBox.setState(QuadristateButtonModel.State.CHECKED);
            }
            if (!checked && greyed) {
                this.checkBox.setState(QuadristateButtonModel.State.GREY_UNCHECKED);
            }
            if (checked && greyed) {
                this.checkBox.setState(QuadristateButtonModel.State.GREY_CHECKED);
            }
            if (!checked && !greyed) {
                this.checkBox.setState(QuadristateButtonModel.State.UNCHECKED);
            }
        }
        return this;
    }

    @Override
    public boolean isOnHotspot(int x, int y) {
        return this.checkBox.contains(x, y);
    }

    @Override
    public void setBackground(Color color) {
        if (color instanceof ColorUIResource) {
            color = null;
        }
        super.setBackground(color);
    }

    public void setClosedIcon(Icon newIcon) {
        this.label.setClosedIcon(newIcon);
    }

    public void setLeafIcon(Icon newIcon) {
        this.label.setLeafIcon(newIcon);
    }

    public void setOpenIcon(Icon newIcon) {
        this.label.setOpenIcon(newIcon);
    }
}

