/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.explorer;

import java.util.Hashtable;
import org.weasis.core.api.explorer.DataExplorerView;

@FunctionalInterface
public interface DataExplorerViewFactory {
    public DataExplorerView createDataExplorerView(Hashtable<String, Object> var1);
}

