/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.gui.util.GuiExecutor
 *  org.weasis.core.api.gui.util.GuiUtils
 *  org.weasis.core.api.net.auth.AuthMethod
 *  org.weasis.core.api.net.auth.AuthProvider
 *  org.weasis.core.api.net.auth.AuthRegistration
 *  org.weasis.core.api.net.auth.DefaultAuthMethod
 *  org.weasis.core.util.LangUtil
 *  org.weasis.core.util.StringUtil
 *  org.weasis.core.util.StringUtil$Suffix
 *  org.weasis.dicom.mf.WadoParameters
 */
package org.weasis.dicom.explorer.rs;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;
import javax.swing.JOptionPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.gui.util.GuiExecutor;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.net.auth.AuthMethod;
import org.weasis.core.api.net.auth.AuthProvider;
import org.weasis.core.api.net.auth.AuthRegistration;
import org.weasis.core.api.net.auth.DefaultAuthMethod;
import org.weasis.core.util.LangUtil;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.explorer.DicomModel;
import org.weasis.dicom.explorer.Messages;
import org.weasis.dicom.explorer.PluginOpeningStrategy;
import org.weasis.dicom.explorer.exp.ExplorerTask;
import org.weasis.dicom.explorer.pref.node.AuthenticationPersistence;
import org.weasis.dicom.explorer.rs.RsQueryResult;
import org.weasis.dicom.explorer.wado.DownloadManager;
import org.weasis.dicom.explorer.wado.LoadSeries;
import org.weasis.dicom.mf.WadoParameters;

public class RsQueryParams
extends ExplorerTask<Boolean, String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RsQueryParams.class);
    public static final String P_DICOMWEB_URL = "dicomweb.url";
    public static final String P_QUERY_EXT = "query.ext";
    public static final String P_RETRIEVE_EXT = "retrieve.ext";
    public static final String P_SHOW_WHOLE_STUDY = "show.whole.study";
    public static final String P_ACCEPT_EXT = "accept.ext";
    public static final String P_AUTH_UID = "auth.uid";
    public static final String P_OIDC_ISSUER = "oidc.issuer";
    public static final String P_OIDC_USER = "oidc.user";
    public static final String P_PAGE_EXT = "page.size";
    private final DicomModel dicomModel;
    private final Map<String, LoadSeries> seriesMap;
    private final Properties properties;
    private final Map<String, String> queryHeaders;
    private final Map<String, String> retrieveHeaders;
    protected final Map<String, List<String>> requestMap;
    protected final RsQueryResult arcConfig;
    protected final PluginOpeningStrategy openingStrategy;

    public RsQueryParams(DicomModel dicomModel, Properties properties, Map<String, List<String>> requestMap, Map<String, String> queryHeaders, Map<String, String> retrieveHeaders) {
        super(Messages.getString("DicomExplorer.loading"), true);
        String issuer;
        this.dicomModel = Objects.requireNonNull(dicomModel);
        this.properties = Objects.requireNonNull(properties);
        this.requestMap = Objects.requireNonNull(requestMap);
        String url = properties.getProperty(P_DICOMWEB_URL);
        if (StringUtil.hasText((CharSequence)url)) {
            if (url.endsWith("/")) {
                properties.setProperty(P_DICOMWEB_URL, url.substring(0, url.length() - 1));
            }
        } else {
            throw new IllegalArgumentException("DICOMweb URL cannot be null");
        }
        this.seriesMap = new HashMap<String, LoadSeries>();
        this.queryHeaders = queryHeaders == null ? Collections.emptyMap() : queryHeaders;
        this.retrieveHeaders = retrieveHeaders == null ? Collections.emptyMap() : retrieveHeaders;
        String uid = properties.getProperty(P_AUTH_UID);
        AuthMethod method = null;
        if (StringUtil.hasText((CharSequence)uid)) {
            method = AuthenticationPersistence.getAuthMethod(uid);
        }
        if (method == null && StringUtil.hasText((CharSequence)(issuer = properties.getProperty(P_OIDC_ISSUER)))) {
            if (issuer.endsWith("/")) {
                issuer = issuer.substring(0, issuer.length() - 1);
            }
            AuthProvider p = new AuthProvider("OIDC", issuer + "/protocol/openid-connect/auth", issuer + "/protocol/openid-connect/token", issuer + "/protocol/openid-connect/revoke", true);
            String user = properties.getProperty(P_OIDC_USER);
            AuthRegistration r = new AuthRegistration(null, null, "openid", null, user, "code");
            method = new DefaultAuthMethod(UUID.randomUUID().toString(), p, r);
        }
        this.arcConfig = new RsQueryResult(this, method);
        this.openingStrategy = new PluginOpeningStrategy(DownloadManager.getOpeningViewer());
    }

    public static Map<String, String> getHeaders(List<String> urlHeaders) {
        HashMap<String, String> headers = new HashMap<String, String>();
        for (String h : LangUtil.emptyIfNull(urlHeaders)) {
            String[] val = h.split(":", 2);
            if (val.length == 1) {
                headers.put(val[0].trim().toLowerCase(), "");
                continue;
            }
            if (val.length != 2) continue;
            String name = val[0].trim().toLowerCase();
            Object value = val[1].trim();
            if ("authorization".equals(name) && ((String)value).length() > 14 && ((String)value).startsWith("&access_token=")) {
                value = "Bearer " + ((String)value).substring(14);
            }
            headers.put(name, (String)value);
        }
        return headers;
    }

    public static Map<String, List<String>> getQueryMap(String query) {
        String[] params = query.split("&");
        HashMap<String, List<String>> map = new HashMap<String, List<String>>();
        for (String param : params) {
            String[] val = param.split("=", 2);
            String name = val[0];
            if (name.isEmpty()) continue;
            ArrayList<String> v = (ArrayList<String>)map.get(name);
            if (v == null) {
                v = new ArrayList<String>();
                map.put(val[0], v);
            }
            if (val.length == 1) {
                v.add("");
                continue;
            }
            if (val.length != 2) continue;
            v.add(val[1]);
        }
        return map;
    }

    @Override
    protected void done() {
        this.openingStrategy.reset();
    }

    @Override
    protected Boolean doInBackground() throws Exception {
        this.fillPatientList();
        if (!this.seriesMap.isEmpty()) {
            this.openingStrategy.prepareImport();
            WadoParameters wp = new WadoParameters("", true, true);
            this.getRetrieveHeaders().forEach((arg_0, arg_1) -> ((WadoParameters)wp).addHttpTag(arg_0, arg_1));
            wp.addHttpTag("Accept", "image/jpeg");
            for (LoadSeries loadSeries : this.seriesMap.values()) {
                if (!DicomModel.isHiddenModality(loadSeries.getDicomSeries())) {
                    loadSeries.startDownloadImageReference(wp);
                }
                loadSeries.setPOpeningStrategy(this.openingStrategy);
                DownloadManager.addLoadSeries(loadSeries, this.dicomModel, loadSeries.isStartDownloading());
            }
            DownloadManager.getTasks().sort(Collections.reverseOrder(new DownloadManager.PriorityTaskComparator()));
            DownloadManager.CONCURRENT_EXECUTOR.prestartAllCoreThreads();
        }
        return true;
    }

    private void fillPatientList() {
        try {
            String requestType = this.getRequestType();
            if ("STUDY".equals(requestType)) {
                String stuID = this.getReqStudyUID();
                String anbID = this.getReqAccessionNumber();
                if (StringUtil.hasText((CharSequence)anbID)) {
                    this.arcConfig.buildFromStudyAccessionNumber(Collections.singletonList(anbID));
                } else if (StringUtil.hasText((CharSequence)stuID)) {
                    this.arcConfig.buildFromStudyInstanceUID(Collections.singletonList(stuID));
                } else {
                    LOGGER.error("Not ID found for STUDY request type: {}", (Object)requestType);
                    RsQueryParams.showErrorMessage(Messages.getString("RsQueryParams.missing_study_uid"), Messages.getString("RsQueryParams.no_sudy_uid"));
                }
            } else if ("PATIENT".equals(requestType)) {
                String patID = this.getReqPatientID();
                if (StringUtil.hasText((CharSequence)patID)) {
                    this.arcConfig.buildFromPatientID(Collections.singletonList(patID));
                }
            } else if (requestType != null) {
                LOGGER.error("Not supported IID request type: {}", (Object)requestType);
                RsQueryParams.showErrorMessage(Messages.getString("RsQueryParams.unexpect_req"), Messages.getString("RsQueryParams.idd_type") + ": " + requestType);
            } else {
                this.arcConfig.buildFromSopInstanceUID(this.getReqObjectUIDs());
                this.arcConfig.buildFromSeriesInstanceUID(this.getReqSeriesUIDs());
                this.arcConfig.buildFromStudyAccessionNumber(this.getReqAccessionNumbers());
                this.arcConfig.buildFromStudyInstanceUID(this.getReqStudyUIDs());
                this.arcConfig.buildFromPatientID(this.getReqPatientIDs());
            }
        }
        catch (Exception e) {
            LOGGER.error("Error when building the patient list", (Throwable)e);
            RsQueryParams.showErrorMessage(Messages.getString("RsQueryParams.unexpect_error"), Messages.getString("RsQueryParams.error_build_mf") + ":\n" + StringUtil.getTruncatedString((String)e.getMessage(), (int)130, (StringUtil.Suffix)StringUtil.Suffix.THREE_PTS));
        }
    }

    private static void showErrorMessage(String title, String msg) {
        GuiExecutor.execute(() -> JOptionPane.showMessageDialog((Component)GuiUtils.getUICore().getBaseArea(), msg, title, 0));
    }

    private static String getFirstParam(List<String> list) {
        if (list != null && !list.isEmpty()) {
            return (String)list.getFirst();
        }
        return null;
    }

    public boolean isAcceptNoImage() {
        return LangUtil.emptyToFalse((String)this.properties.getProperty("accept.noimage"));
    }

    public DicomModel getDicomModel() {
        return this.dicomModel;
    }

    public Map<String, LoadSeries> getSeriesMap() {
        return this.seriesMap;
    }

    public Properties getProperties() {
        return this.properties;
    }

    public Map<String, String> getQueryHeaders() {
        return this.queryHeaders;
    }

    public Map<String, String> getRetrieveHeaders() {
        return this.retrieveHeaders;
    }

    public String getBaseUrl() {
        return this.properties.getProperty(P_DICOMWEB_URL);
    }

    public boolean hasPatients() {
        return !this.arcConfig.getPatients().isEmpty();
    }

    public void clearAllPatients() {
        this.arcConfig.getPatients().clear();
    }

    public void removePatientId(List<String> patientIdList, boolean containsIssuer) {
        this.arcConfig.removePatientId(patientIdList, containsIssuer);
    }

    public void removeStudyUid(List<String> studyUidList) {
        this.arcConfig.removeStudyUid(studyUidList);
    }

    public void removeAccessionNumber(List<String> accessionNumberList) {
        this.arcConfig.removeAccessionNumber(accessionNumberList);
    }

    public void removeSeriesUid(List<String> seriesUidList) {
        this.arcConfig.removeSeriesUid(seriesUidList);
    }

    public String getRequestType() {
        return RsQueryParams.getFirstParam(this.requestMap.get("requestType"));
    }

    public String getReqPatientID() {
        return RsQueryParams.getFirstParam(this.requestMap.get("patientID"));
    }

    public String getPatientName() {
        return RsQueryParams.getFirstParam(this.requestMap.get("patientName"));
    }

    public String getPatientBirthDate() {
        return RsQueryParams.getFirstParam(this.requestMap.get("patientBirthDate"));
    }

    public String getLowerDateTime() {
        return RsQueryParams.getFirstParam(this.requestMap.get("lowerDateTime"));
    }

    public String getUpperDateTime() {
        return RsQueryParams.getFirstParam(this.requestMap.get("upperDateTime"));
    }

    public String getMostRecentResults() {
        return RsQueryParams.getFirstParam(this.requestMap.get("mostRecentResults"));
    }

    public String getKeywords() {
        return RsQueryParams.getFirstParam(this.requestMap.get("containsInDescription"));
    }

    public String getModalitiesInStudy() {
        return RsQueryParams.getFirstParam(this.requestMap.get("modalitiesInStudy"));
    }

    public String getReqStudyUID() {
        return RsQueryParams.getFirstParam(this.requestMap.get("studyUID"));
    }

    public String getReqAccessionNumber() {
        return RsQueryParams.getFirstParam(this.requestMap.get("accessionNumber"));
    }

    public String getReqSeriesUID() {
        return RsQueryParams.getFirstParam(this.requestMap.get("seriesUID"));
    }

    public String getReqObjectUID() {
        return RsQueryParams.getFirstParam(this.requestMap.get("objectUID"));
    }

    public List<String> getReqPatientIDs() {
        return this.requestMap.get("patientID");
    }

    public List<String> getReqStudyUIDs() {
        return this.requestMap.get("studyUID");
    }

    public List<String> getReqAccessionNumbers() {
        return this.requestMap.get("accessionNumber");
    }

    public List<String> getReqSeriesUIDs() {
        return this.requestMap.get("seriesUID");
    }

    public List<String> getReqObjectUIDs() {
        return this.requestMap.get("objectUID");
    }
}

