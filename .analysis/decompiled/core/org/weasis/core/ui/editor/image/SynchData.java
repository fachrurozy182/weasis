/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.LangUtil
 */
package org.weasis.core.ui.editor.image;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.weasis.core.api.util.Copyable;
import org.weasis.core.util.LangUtil;

public class SynchData
implements Copyable<SynchData> {
    protected final Map<String, Boolean> actions;
    protected final Mode mode;
    protected SyncState autoSyncState;
    private boolean original;
    protected SyncState manualSyncState;

    public SynchData(Mode mode, Map<String, Boolean> actions, boolean synch) {
        if (actions == null) {
            throw new IllegalArgumentException("A parameter is null!");
        }
        this.actions = actions;
        this.mode = mode;
        this.original = true;
        this.autoSyncState = synch ? SyncState.ON : SyncState.OFF;
        this.manualSyncState = SyncState.OFF;
    }

    public SynchData(SynchData synchData) {
        Objects.requireNonNull(synchData);
        this.actions = new HashMap<String, Boolean>(synchData.actions);
        this.mode = synchData.mode;
        this.original = synchData.original;
        this.autoSyncState = synchData.autoSyncState;
        this.manualSyncState = synchData.manualSyncState;
    }

    public Map<String, Boolean> getActions() {
        return this.actions;
    }

    public boolean isActionEnable(String action) {
        return LangUtil.nullToFalse((Boolean)this.actions.get(action));
    }

    public Mode getMode() {
        return this.mode;
    }

    public SyncState getAutoSyncState() {
        return this.autoSyncState;
    }

    public void setAutoSyncState(SyncState state) {
        this.autoSyncState = state;
    }

    public SyncState getManualSyncState() {
        return this.manualSyncState;
    }

    public void setManualSyncState(SyncState state) {
        this.manualSyncState = state;
    }

    public boolean isSynchActivated() {
        return this.autoSyncState == SyncState.ON || this.manualSyncState == SyncState.ON;
    }

    public boolean isAutoSynchActivated() {
        return this.autoSyncState == SyncState.ON;
    }

    public boolean isManualSynchActivated() {
        return this.manualSyncState == SyncState.ON;
    }

    @Override
    public SynchData copy() {
        return new SynchData(this);
    }

    public boolean isOriginal() {
        return this.original;
    }

    public void setOriginal(boolean original) {
        this.original = original;
    }

    public static enum Mode {
        STACK,
        TILE;

    }

    public static enum SyncState {
        OFF,
        ON;

    }
}

