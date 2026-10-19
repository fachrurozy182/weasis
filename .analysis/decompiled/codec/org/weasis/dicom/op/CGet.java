/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.StreamUtil
 */
package org.weasis.dicom.op;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.QueryOption;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.tool.getscu.GetSCU;
import org.dcm4che3.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.util.StreamUtil;
import org.weasis.dicom.param.AdvancedParams;
import org.weasis.dicom.param.DeviceOpService;
import org.weasis.dicom.param.DicomNode;
import org.weasis.dicom.param.DicomParam;
import org.weasis.dicom.param.DicomProgress;
import org.weasis.dicom.param.DicomState;
import org.weasis.dicom.util.ServiceUtil;

public final class CGet {
    private static final Logger LOGGER = LoggerFactory.getLogger(CGet.class);
    private static final String DEFAULT_STORE_TCS_PROPERTIES = "store-tcs.properties";

    private CGet() {
    }

    public static DicomState process(DicomNode callingNode, DicomNode calledNode, DicomProgress progress, Path outputDir, DicomParam ... keys) {
        return CGet.process(null, callingNode, calledNode, progress, outputDir, null, keys);
    }

    public static DicomState process(AdvancedParams params, DicomNode callingNode, DicomNode calledNode, DicomProgress progress, Path outputDir, DicomParam ... keys) {
        return CGet.process(params, callingNode, calledNode, progress, outputDir, null, keys);
    }

    public static DicomState process(AdvancedParams params, DicomNode callingNode, DicomNode calledNode, DicomProgress progress, Path outputDir, URL sopClassURL, DicomParam ... keys) {
        CGet.validateRequiredParameters(callingNode, calledNode, outputDir);
        AdvancedParams options = Objects.requireNonNullElse(params, new AdvancedParams());
        try {
            return CGet.executeGetOperation(options, callingNode, calledNode, progress, outputDir, sopClassURL, keys);
        }
        catch (Exception e) {
            LOGGER.error("DICOM C-GET operation failed", (Throwable)e);
            return CGet.createErrorState(e);
        }
    }

    private static void validateRequiredParameters(DicomNode callingNode, DicomNode calledNode, Path outputDir) {
        Objects.requireNonNull(callingNode, "callingNode cannot be null");
        Objects.requireNonNull(calledNode, "calledNode cannot be null");
        Objects.requireNonNull(outputDir, "outputDir cannot be null");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static DicomState executeGetOperation(AdvancedParams options, DicomNode callingNode, DicomNode calledNode, DicomProgress progress, Path outputDir, URL sopClassURL, DicomParam ... keys) throws Exception {
        GetSCU getSCU = new GetSCU(progress);
        DeviceOpService service = new DeviceOpService(getSCU.getDevice());
        try {
            CGet.configureGetSCU(getSCU, options, callingNode, calledNode, outputDir, sopClassURL);
            CGet.addQueryKeys(getSCU, keys);
            service.start();
            DicomState dicomState = CGet.performRetrievalOperation(getSCU);
            return dicomState;
        }
        finally {
            StreamUtil.safeClose((AutoCloseable)getSCU);
            service.stop();
        }
    }

    private static void configureGetSCU(GetSCU getSCU, AdvancedParams options, DicomNode callingNode, DicomNode calledNode, Path outputDir, URL sopClassURL) throws IOException {
        Connection remote = getSCU.getRemoteConnection();
        Connection conn = getSCU.getConnection();
        options.configureConnect(getSCU.getAAssociateRQ(), remote, calledNode);
        options.configureBind(getSCU.getApplicationEntity(), conn, callingNode);
        options.configure(conn);
        options.configureTLS(conn, remote);
        getSCU.setPriority(options.getPriority());
        getSCU.setStorageDirectory(outputDir);
        getSCU.setInformationModel(CGet.getInformationModel(options), options.getTsuidOrder(), options.getQueryOptions().contains((Object)QueryOption.RELATIONAL));
        CGet.configureRelatedSOPClass(getSCU, sopClassURL);
    }

    private static void addQueryKeys(GetSCU getSCU, DicomParam ... keys) {
        DicomState dcmState = getSCU.getState();
        for (DicomParam param : keys) {
            String[] values = param.getValues();
            getSCU.addKey(param.getTag(), values);
            if (values == null || values.length <= 0) continue;
            dcmState.addDicomMatchingKeys(param);
        }
    }

    private static DicomState performRetrievalOperation(GetSCU getSCU) throws Exception {
        long startTime = System.currentTimeMillis();
        getSCU.open();
        long connectTime = System.currentTimeMillis();
        try {
            getSCU.retrieve();
            ServiceUtil.forceGettingAttributes(getSCU.getState(), getSCU);
            long endTime = System.currentTimeMillis();
            return CGet.buildSuccessState(getSCU, startTime, connectTime, endTime);
        }
        catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            LOGGER.error("DICOM C-GET retrieval failed", (Throwable)e);
            ServiceUtil.forceGettingAttributes(getSCU.getState(), getSCU);
            return DicomState.buildMessage(getSCU.getState(), null, e);
        }
    }

    private static DicomState buildSuccessState(GetSCU getSCU, long startTime, long connectTime, long endTime) {
        String timeMsg = MessageFormat.format("DICOM C-GET connected in {2}ms from {0} to {1}. Retrieved files in {3}ms.", getSCU.getAAssociateRQ().getCallingAET(), getSCU.getAAssociateRQ().getCalledAET(), connectTime - startTime, endTime - connectTime);
        DicomState dcmState = DicomState.buildMessage(getSCU.getState(), timeMsg, null);
        dcmState.addProcessTime(startTime, connectTime, endTime);
        dcmState.setBytesSize(getSCU.getTotalSize());
        return dcmState;
    }

    private static DicomState createErrorState(Exception e) {
        return DicomState.buildMessage(new DicomState(49152, "DICOM Get failed: " + e.getMessage(), null), null, e);
    }

    /*
     * Could not resolve type clashes
     * Unable to fully structure code
     */
    private static void configureRelatedSOPClass(GetSCU getSCU, URL url) throws IOException {
        properties = new Properties();
        try {
            if (url == null) {
                inputStream = getSCU.getClass().getResourceAsStream("store-tcs.properties");
                try {
                    if (inputStream == null) ** GOTO lbl21
                    properties.load((InputStream)inputStream);
                }
                finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                }
            } else {
                inputStream = url.openStream();
                try {
                    properties.load((InputStream)inputStream);
                }
                finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                }
            }
            for (Map.Entry entry : properties.entrySet()) {
                CGet.configureStorageSOPClass(getSCU, (String)entry.getKey(), (String)entry.getValue());
            }
        }
        catch (Exception e) {
            CGet.LOGGER.error("Failed to read SOP class configuration", (Throwable)e);
        }
    }

    private static void configureStorageSOPClass(GetSCU getSCU, String cuid, String tsuids) {
        String[] transferSyntaxes = StringUtils.split(tsuids, ';');
        for (int i = 0; i < transferSyntaxes.length; ++i) {
            transferSyntaxes[i] = CLIUtils.toUID(transferSyntaxes[i]);
        }
        getSCU.addOfferedStorageSOPClass(CLIUtils.toUID(cuid), transferSyntaxes);
    }

    private static GetSCU.InformationModel getInformationModel(AdvancedParams options) {
        GetSCU.InformationModel im;
        Object model = options.getInformationModel();
        return model instanceof GetSCU.InformationModel ? (im = (GetSCU.InformationModel)((Object)model)) : GetSCU.InformationModel.StudyRoot;
    }
}

