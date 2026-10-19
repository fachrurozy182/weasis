/*
 * Decompiled with CFR 0.152.
 */
package eu.essilab.lablib.checkboxtree;

import java.awt.Component;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;

public interface CheckboxTreeCellRenderer
extends TreeCellRenderer {
    @Override
    public Component getTreeCellRendererComponent(JTree var1, Object var2, boolean var3, boolean var4, boolean var5, int var6, boolean var7);

    public boolean isOnHotspot(int var1, int var2);
}

