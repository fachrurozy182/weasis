/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net.service;

import org.dcm4che3.net.DimseRQHandler;

public interface DicomService
extends DimseRQHandler {
    public String[] getSOPClasses();
}

