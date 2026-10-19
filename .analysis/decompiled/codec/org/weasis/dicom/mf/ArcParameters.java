/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.dicom.mf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.mf.HttpTag;

public class ArcParameters {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArcParameters.class);
    public static final String TAG_DOCUMENT_ROOT = "manifest";
    public static final String MANIFEST_UID = "uid";
    public static final String SCHEMA = "xmlns=\"http://www.weasis.org/xsd/2.5\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"";
    public static final String TAG_ARC_QUERY = "arcQuery";
    public static final String ARCHIVE_ID = "arcId";
    public static final String BASE_URL = "baseUrl";
    public static final String QUERY_MODE = "queryMode";
    public static final String TAG_HTTP_TAG = "httpTag";
    public static final String ADDITIONAL_PARAMETERS = "additionalParameters";
    public static final String OVERRIDE_TAGS = "overrideDicomTagsList";
    public static final String WEB_LOGIN = "webLogin";
    public static final String TAG_DELIMITER = ",";
    public static final int DEFAULT_HTTP_TAG_CAPACITY = 2;
    private final String baseURL;
    private final String archiveID;
    private final String additionalParameters;
    private final int[] overrideDicomTagIDList;
    private final String webLogin;
    private final List<HttpTag> httpTaglist;

    public ArcParameters(String archiveID, String baseURL, String additionalParameters, String overrideDicomTagsList, String webLogin) {
        this.archiveID = Objects.requireNonNull(archiveID, "Archive ID cannot be null");
        this.baseURL = Objects.requireNonNull(baseURL, "Base URL cannot be null");
        this.webLogin = this.normalizeWebLogin(webLogin);
        this.additionalParameters = this.normalizeParameters(additionalParameters);
        this.httpTaglist = new ArrayList<HttpTag>(2);
        this.overrideDicomTagIDList = this.parseOverrideTags(overrideDicomTagsList);
    }

    public List<HttpTag> getHttpTaglist() {
        return Collections.unmodifiableList(this.httpTaglist);
    }

    public void addHttpTag(String key, String value) {
        if (key != null && value != null) {
            this.httpTaglist.add(new HttpTag(key, value));
        }
    }

    public String getArchiveID() {
        return this.archiveID;
    }

    public String getBaseURL() {
        return this.baseURL;
    }

    public String getWebLogin() {
        return this.webLogin;
    }

    public String getAdditionalParameters() {
        return this.additionalParameters;
    }

    public int[] getOverrideDicomTagIDList() {
        return this.overrideDicomTagIDList != null ? (int[])this.overrideDicomTagIDList.clone() : null;
    }

    public String getOverrideDicomTagsList() {
        return this.overrideDicomTagIDList != null ? IntStream.of(this.overrideDicomTagIDList).mapToObj(String::valueOf).collect(Collectors.joining(TAG_DELIMITER)) : null;
    }

    public String toString() {
        return "ArcParameters{archiveID='" + this.archiveID + "', baseURL='" + this.baseURL + "', webLogin='" + this.webLogin + "', additionalParameters='" + this.additionalParameters + "', overrideTags=" + Arrays.toString(this.overrideDicomTagIDList) + ", httpTagCount=" + this.httpTaglist.size() + "}";
    }

    private String normalizeWebLogin(String webLogin) {
        return webLogin != null ? webLogin.trim() : null;
    }

    private String normalizeParameters(String parameters) {
        return StringUtil.hasText((CharSequence)parameters) ? parameters : "";
    }

    private int[] parseOverrideTags(String overrideDicomTagsList) {
        String[] tagStrings = StringUtil.getStringArray((String)overrideDicomTagsList, (String)TAG_DELIMITER);
        if (tagStrings.length == 0) {
            return null;
        }
        return Arrays.stream(tagStrings).mapToInt(this::parseTagId).filter(tagId -> tagId != -1).toArray();
    }

    private int parseTagId(String tagString) {
        try {
            return Integer.decode(tagString.trim());
        }
        catch (NumberFormatException e) {
            LOGGER.warn("Invalid DICOM tag ID '{}' - skipping", (Object)tagString.trim());
            return -1;
        }
    }
}

