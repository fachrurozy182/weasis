/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.param;

import java.net.URL;
import org.weasis.dicom.param.AbstractListenerParams;
import org.weasis.dicom.param.AdvancedParams;

public class ListenerParams
extends AbstractListenerParams {
    private final String storagePattern;

    public ListenerParams(boolean bindCallingAet) {
        this((AdvancedParams)null, bindCallingAet, (String)null, (URL)null, new String[0]);
    }

    public ListenerParams(AdvancedParams params, boolean bindCallingAet) {
        this(params, bindCallingAet, (String)null, (URL)null, new String[0]);
    }

    public ListenerParams(AdvancedParams params, boolean bindCallingAet, String storagePattern, URL transferCapabilityFile, String ... acceptedCallingAETitles) {
        super(params, bindCallingAet, transferCapabilityFile, acceptedCallingAETitles);
        this.storagePattern = storagePattern;
    }

    public String getStoragePattern() {
        return this.storagePattern;
    }
}

