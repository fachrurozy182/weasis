/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.tool;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.TransferCapability;
import org.dcm4che3.tool.storescp.StoreSCP;
import org.weasis.dicom.param.AdvancedParams;
import org.weasis.dicom.param.DeviceListenerService;
import org.weasis.dicom.param.DicomNode;
import org.weasis.dicom.param.DicomProgress;
import org.weasis.dicom.param.ListenerParams;

public class DicomListener {
    private final StoreSCP storeSCP;
    private final DeviceListenerService deviceService;

    public DicomListener(Path storageDir) {
        this(storageDir, null);
    }

    public DicomListener(Path storageDir, DicomProgress dicomProgress) {
        this.storeSCP = new StoreSCP(storageDir, null, dicomProgress);
        this.deviceService = new DeviceListenerService(this.storeSCP.getDevice());
    }

    public boolean isRunning() {
        return this.storeSCP.getConnection().isListening();
    }

    public StoreSCP getStoreSCP() {
        return this.storeSCP;
    }

    public void start(DicomNode scpNode) throws Exception {
        this.start(scpNode, new ListenerParams(true));
    }

    public synchronized void start(DicomNode scpNode, ListenerParams params) throws Exception {
        this.validateCanStart();
        this.initializeStoreSCP(params);
        this.configureConnection(scpNode, params);
        this.configureTransferCapabilities(params);
        this.deviceService.start();
    }

    public synchronized void stop() {
        this.deviceService.stop();
    }

    private void validateCanStart() throws IOException {
        if (this.isRunning()) {
            throw new IOException("Cannot start a DICOM Listener because it is already running.");
        }
    }

    private void initializeStoreSCP(ListenerParams params) {
        this.storeSCP.setStatus(0);
        this.storeSCP.setStorageFilePathFormat(params.getStoragePattern());
    }

    private void configureConnection(DicomNode scpNode, ListenerParams params) throws Exception {
        AdvancedParams options = Objects.requireNonNull(params, "params cannot be null").getParams();
        Connection connection = this.storeSCP.getConnection();
        this.configureBinding(options, connection, scpNode, params);
        options.configure(connection);
        options.configureTLS(connection, null);
        this.storeSCP.getApplicationEntity().setAcceptedCallingAETitles(params.getAcceptedCallingAETitles());
    }

    private void configureBinding(AdvancedParams options, Connection connection, DicomNode scpNode, ListenerParams params) {
        if (params.isBindCallingAet()) {
            options.configureBind(this.storeSCP.getApplicationEntity(), connection, scpNode);
        } else {
            options.configureBind(connection, scpNode);
        }
    }

    private void configureTransferCapabilities(ListenerParams params) {
        URL transferCapabilityFile = params.getTransferCapabilityFile();
        if (transferCapabilityFile != null) {
            this.storeSCP.loadDefaultTransferCapability(transferCapabilityFile);
        } else {
            this.addDefaultTransferCapability();
        }
    }

    private void addDefaultTransferCapability() {
        this.storeSCP.getApplicationEntity().addTransferCapability(new TransferCapability(null, "*", TransferCapability.Role.SCP, "*"));
    }
}

