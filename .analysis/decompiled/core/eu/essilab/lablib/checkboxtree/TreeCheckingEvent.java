/*
 * Decompiled with CFR 0.152.
 */
package eu.essilab.lablib.checkboxtree;

import java.util.EventObject;
import javax.swing.tree.TreePath;

public class TreeCheckingEvent
extends EventObject {
    protected TreePath changedPath;
    private boolean checked;

    public TreeCheckingEvent(Object source, TreePath path, boolean checked) {
        super(source);
        this.changedPath = path;
        this.checked = checked;
    }

    public TreePath getPath() {
        return this.changedPath;
    }

    public boolean isCheckedPath() {
        return this.checked;
    }
}

