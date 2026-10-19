/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.mf;

import java.util.Collection;
import java.util.Objects;
import org.weasis.dicom.mf.AbstractQueryResult;
import org.weasis.dicom.mf.Patient;
import org.weasis.dicom.mf.WadoParameters;

public class DefaultQueryResult
extends AbstractQueryResult {
    protected final WadoParameters wadoParameters;

    public DefaultQueryResult(Collection<Patient> patients, WadoParameters wadoParameters) {
        super(patients);
        this.wadoParameters = Objects.requireNonNull(wadoParameters, "WADO parameters cannot be null");
    }

    public DefaultQueryResult(WadoParameters wadoParameters) {
        this(null, wadoParameters);
    }

    @Override
    public WadoParameters getWadoParameters() {
        return this.wadoParameters;
    }

    public String toString() {
        return "DefaultQueryResult{patientCount=" + this.getPatientCount() + ", wadoParameters=" + String.valueOf(this.wadoParameters) + ", viewerMessageCount=" + this.getViewerMessages().size() + "}";
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DefaultQueryResult)) {
            return false;
        }
        DefaultQueryResult that = (DefaultQueryResult)obj;
        return Objects.equals(this.getPatients(), that.getPatients()) && Objects.equals(this.wadoParameters, that.wadoParameters) && Objects.equals(this.getViewerMessages(), that.getViewerMessages());
    }

    public int hashCode() {
        return Objects.hash(this.getPatients(), this.wadoParameters, this.getViewerMessages());
    }
}

