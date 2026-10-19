/*
 * Decompiled with CFR 0.152.
 */
package eu.essilab.lablib.checkboxtree;

import eu.essilab.lablib.checkboxtree.TreeCheckingEvent;
import java.util.EventListener;

public interface TreeCheckingListener
extends EventListener {
    public void valueChanged(TreeCheckingEvent var1);
}

