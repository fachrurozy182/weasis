/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.mf;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import org.weasis.dicom.mf.ArcParameters;
import org.weasis.dicom.mf.QueryMode;

public class WadoParameters
extends ArcParameters {
    public static final String WADO_ONLY_SOP_UID = "requireOnlySOPInstanceUID";
    private final boolean requireOnlySOPInstanceUID;
    private final boolean wadoRS;
    private final QueryMode queryMode;

    public WadoParameters(String archiveID, String wadoURL, boolean requireOnlySOPInstanceUID, String additionalParameters, String overrideDicomTagsList, String webLogin, boolean wadoRS) {
        this(archiveID, wadoURL, requireOnlySOPInstanceUID, additionalParameters, overrideDicomTagsList, webLogin, wadoRS, QueryMode.DEFAULT);
    }

    public WadoParameters(String archiveID, String wadoURL, boolean requireOnlySOPInstanceUID, String additionalParameters, String overrideDicomTagsList, String webLogin, boolean wadoRS, QueryMode queryMode) {
        super(Objects.requireNonNull(archiveID, "Archive ID cannot be null"), Objects.requireNonNullElse(WadoParameters.validateAndNormalizeUrl(wadoURL), ""), additionalParameters, overrideDicomTagsList, webLogin);
        this.requireOnlySOPInstanceUID = requireOnlySOPInstanceUID;
        this.wadoRS = wadoRS;
        this.queryMode = Objects.requireNonNullElse(queryMode, QueryMode.DEFAULT);
    }

    public WadoParameters(String wadoURL, boolean requireOnlySOPInstanceUID) {
        this("", wadoURL, requireOnlySOPInstanceUID, null, null, null, false);
    }

    public WadoParameters(String wadoURL, boolean requireOnlySOPInstanceUID, boolean wadoRS) {
        this("", wadoURL, requireOnlySOPInstanceUID, null, null, null, wadoRS);
    }

    public WadoParameters(String wadoURL, boolean requireOnlySOPInstanceUID, String additionalParameters, String overrideDicomTagsList, String webLogin) {
        this("", wadoURL, requireOnlySOPInstanceUID, additionalParameters, overrideDicomTagsList, webLogin, false);
    }

    public WadoParameters(String archiveID, String wadoURL, boolean requireOnlySOPInstanceUID, String additionalParameters, String overrideDicomTagsList, String webLogin) {
        this(archiveID, wadoURL, requireOnlySOPInstanceUID, additionalParameters, overrideDicomTagsList, webLogin, false);
    }

    public static WadoParameters wadoUri(String wadoURL, boolean requireOnlySOPInstanceUID) {
        return new WadoParameters(wadoURL, requireOnlySOPInstanceUID, false);
    }

    public static Builder wadoRs(String wadoURL) {
        return new Builder(wadoURL, true);
    }

    public static Builder builder(String wadoURL) {
        return new Builder(wadoURL, false);
    }

    public boolean isRequireOnlySOPInstanceUID() {
        return this.requireOnlySOPInstanceUID;
    }

    public boolean isWadoRS() {
        return this.wadoRS;
    }

    public String getProtocolName() {
        return this.wadoRS ? "WADO-RS" : "WADO-URI";
    }

    public QueryMode getQueryMode() {
        return this.queryMode;
    }

    @Override
    public String toString() {
        return String.format("WadoParameters{archiveID='%s', wadoURL='%s', protocol='%s', queryMode=%s, requireOnlySOPInstanceUID=%b, webLogin='%s', additionalParams='%s', httpTagCount=%d}", new Object[]{this.getArchiveID(), this.getBaseURL(), this.getProtocolName(), this.queryMode, this.requireOnlySOPInstanceUID, this.getWebLogin(), this.getAdditionalParameters(), this.getHttpTaglist().size()});
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof WadoParameters)) {
            return false;
        }
        WadoParameters that = (WadoParameters)obj;
        return super.equals(obj) && this.requireOnlySOPInstanceUID == that.requireOnlySOPInstanceUID && this.wadoRS == that.wadoRS && this.queryMode == that.queryMode;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{super.hashCode(), this.requireOnlySOPInstanceUID, this.wadoRS, this.queryMode});
    }

    private static String validateAndNormalizeUrl(String wadoURL) {
        if (wadoURL.isBlank()) {
            return "";
        }
        try {
            URI uri = new URI(wadoURL.trim());
            return uri.toString();
        }
        catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid WADO URI format: " + wadoURL, e);
        }
    }

    public static class Builder {
        private String archiveID = "";
        private final String wadoURL;
        private boolean requireOnlySOPInstanceUID = false;
        private String additionalParameters;
        private String overrideDicomTagsList;
        private String webLogin;
        private final boolean wadoRS;
        private QueryMode queryMode = QueryMode.DEFAULT;

        private Builder(String wadoURL, boolean wadoRS) {
            this.wadoURL = wadoURL;
            this.wadoRS = wadoRS;
        }

        public Builder withArchiveID(String archiveID) {
            this.archiveID = archiveID != null ? archiveID : "";
            return this;
        }

        public Builder withRequireOnlySOPInstanceUID(boolean requireOnlySOPInstanceUID) {
            this.requireOnlySOPInstanceUID = requireOnlySOPInstanceUID;
            return this;
        }

        public Builder withAdditionalParameters(String additionalParameters) {
            this.additionalParameters = additionalParameters;
            return this;
        }

        public Builder withOverrideDicomTagsList(String overrideDicomTagsList) {
            this.overrideDicomTagsList = overrideDicomTagsList;
            return this;
        }

        public Builder withWebLogin(String webLogin) {
            this.webLogin = webLogin;
            return this;
        }

        public Builder withQueryMode(QueryMode queryMode) {
            this.queryMode = queryMode != null ? queryMode : QueryMode.DEFAULT;
            return this;
        }

        public WadoParameters build() {
            return new WadoParameters(this.archiveID, this.wadoURL, this.requireOnlySOPInstanceUID, this.additionalParameters, this.overrideDicomTagsList, this.webLogin, this.wadoRS, this.queryMode);
        }
    }
}

