/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.LangUtil
 */
package org.weasis.core.ui.editor.image;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.weasis.core.ui.editor.image.SynchData;
import org.weasis.core.ui.editor.image.ViewCanvas;
import org.weasis.core.util.LangUtil;

public class ViewSynchData
extends SynchData {
    protected final Set<ManualSyncData> manualSyncDataSet = ConcurrentHashMap.newKeySet();
    private final Map<String, Boolean> userActionOverrides = new HashMap<String, Boolean>();
    private boolean orphan;
    private boolean canBeManuallySynced;
    private boolean canBeAutoSynced;
    private String frameOfReferenceUID;

    public ViewSynchData(SynchData.Mode mode, Map<String, Boolean> actions, boolean synch) {
        super(mode, actions, synch);
        this.orphan = false;
        this.canBeManuallySynced = false;
        this.canBeAutoSynced = true;
    }

    public ViewSynchData(ViewSynchData synchData) {
        super(synchData);
        this.orphan = synchData.orphan;
        this.canBeManuallySynced = synchData.canBeManuallySynced;
        this.canBeAutoSynced = synchData.canBeAutoSynced;
        this.frameOfReferenceUID = synchData.frameOfReferenceUID;
        this.userActionOverrides.putAll(synchData.userActionOverrides);
    }

    public Map<String, Boolean> getUserActionOverrides() {
        return this.userActionOverrides;
    }

    public void setUserActionOverride(String cmd, boolean enabled) {
        if (cmd != null) {
            this.userActionOverrides.put(cmd, enabled);
        }
    }

    public void clearUserActionOverrides() {
        this.userActionOverrides.clear();
    }

    @Override
    public boolean isActionEnable(String action) {
        Boolean override = this.userActionOverrides.get(action);
        if (override != null) {
            return override;
        }
        return LangUtil.nullToFalse((Boolean)this.getActions().get(action));
    }

    public boolean isOrphan() {
        return this.orphan;
    }

    public void setOrphan(boolean orphan) {
        this.orphan = orphan;
    }

    @Override
    public ViewSynchData copy() {
        ViewSynchData synchData = new ViewSynchData(this);
        return synchData;
    }

    public void addManualSyncData(double sourceLocation, double targetLocation, ViewCanvas<?> targetPane) {
        this.manualSyncDataSet.add(new ManualSyncData(sourceLocation, targetLocation, targetPane));
    }

    public void removeManualSyncData(ManualSyncData manualSyncData) {
        this.manualSyncDataSet.remove(manualSyncData);
    }

    public void clearManualSyncData() {
        this.manualSyncDataSet.clear();
    }

    public Set<ManualSyncData> getManualSyncDataSet() {
        return this.manualSyncDataSet;
    }

    public ManualSyncData getManualSyncDataByPane(ViewCanvas<?> targetPane) {
        for (ManualSyncData data : this.manualSyncDataSet) {
            if (data.getTargetPane() != targetPane) continue;
            return data;
        }
        return null;
    }

    public boolean canBeManuallySynced() {
        return this.canBeManuallySynced;
    }

    public void setCanBeManuallySynced(boolean canBeManuallySynced) {
        this.canBeManuallySynced = canBeManuallySynced;
    }

    public boolean canBeAutoSynced() {
        return this.canBeAutoSynced;
    }

    public void setCanBeAutoSynced(boolean canBeAutoSynced) {
        this.canBeAutoSynced = canBeAutoSynced;
    }

    public String getFrameOfReferenceUID() {
        return this.frameOfReferenceUID;
    }

    public void setFrameOfReferenceUID(String frameOfReferenceUID) {
        this.frameOfReferenceUID = frameOfReferenceUID;
    }

    public static class ManualSyncData {
        protected double sourceLocation;
        protected double targetLocation;
        protected ViewCanvas<?> targetPane;

        public ManualSyncData(double sourceLocation, double targetLocation, ViewCanvas<?> targetPane) {
            this.sourceLocation = sourceLocation;
            this.targetLocation = targetLocation;
            this.targetPane = targetPane;
        }

        public double getSourceLocation() {
            return this.sourceLocation;
        }

        public void setSourceLocation(double sourceLocation) {
            this.sourceLocation = sourceLocation;
        }

        public double getTargetLocation() {
            return this.targetLocation;
        }

        public void setTargetLocation(double targetLocation) {
            this.targetLocation = targetLocation;
        }

        public ViewCanvas<?> getTargetPane() {
            return this.targetPane;
        }

        public void setTargetPane(ViewCanvas<?> targetPane) {
            this.targetPane = targetPane;
        }
    }
}

