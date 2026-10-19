/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.param;

import java.net.URL;
import java.util.Objects;
import org.weasis.dicom.param.AdvancedParams;
import org.weasis.dicom.param.ConnectOptions;

public abstract class AbstractListenerParams {
    private static final String[] EMPTY_AET_ARRAY = new String[0];
    private static final int DEFAULT_MAX_OPS = 50;
    protected final AdvancedParams params;
    protected final boolean bindCallingAet;
    protected final URL transferCapabilityFile;
    protected final String[] acceptedCallingAETitles;

    protected AbstractListenerParams(boolean bindCallingAet) {
        this(null, bindCallingAet, null, new String[0]);
    }

    protected AbstractListenerParams(AdvancedParams params, boolean bindCallingAet) {
        this(params, bindCallingAet, null, new String[0]);
    }

    protected AbstractListenerParams(AdvancedParams params, boolean bindCallingAet, URL transferCapabilityFile, String ... acceptedCallingAETitles) {
        this.params = Objects.requireNonNullElseGet(params, AdvancedParams::new);
        this.bindCallingAet = bindCallingAet;
        this.transferCapabilityFile = transferCapabilityFile;
        this.acceptedCallingAETitles = acceptedCallingAETitles != null ? (String[])acceptedCallingAETitles.clone() : EMPTY_AET_ARRAY;
        this.configureDefaultOperationLimits(params);
    }

    private void configureDefaultOperationLimits(AdvancedParams originalParams) {
        if (originalParams == null && this.params.getConnectOptions() != null) {
            ConnectOptions connectOptions = this.params.getConnectOptions();
            connectOptions.setMaxOpsInvoked(50);
            connectOptions.setMaxOpsPerformed(50);
        }
    }

    public boolean isBindCallingAet() {
        return this.bindCallingAet;
    }

    public URL getTransferCapabilityFile() {
        return this.transferCapabilityFile;
    }

    public String[] getAcceptedCallingAETitles() {
        return (String[])this.acceptedCallingAETitles.clone();
    }

    public AdvancedParams getParams() {
        return this.params;
    }
}

