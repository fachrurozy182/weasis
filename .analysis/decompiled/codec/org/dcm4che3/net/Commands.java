/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.VR;
import org.dcm4che3.net.Dimse;
import org.dcm4che3.util.UIDUtils;

public class Commands {
    public static final int NO_DATASET = 257;
    private static int withDatasetType = 0;

    public static Attributes mkCStoreRQ(int msgId, String cuid, String iuid, int priority) {
        Attributes rq = Commands.mkRQ(msgId, 1, withDatasetType);
        rq.setString(2, VR.UI, cuid);
        rq.setString(4096, VR.UI, iuid);
        rq.setInt(1792, VR.US, priority);
        return rq;
    }

    public static Attributes mkCStoreRQ(int msgId, String cuid, String iuid, int priority, String moveOriginatorAET, int moveOriginatorMsgId) {
        Attributes rq = Commands.mkCStoreRQ(msgId, cuid, iuid, priority);
        rq.setString(4144, VR.AE, moveOriginatorAET);
        rq.setInt(4145, VR.US, moveOriginatorMsgId);
        return rq;
    }

    public static Attributes mkCStoreRSP(Attributes cmd, int status) {
        return Commands.mkRSP(cmd, status, Dimse.C_STORE_RQ);
    }

    public static Attributes mkCFindRQ(int msgId, String cuid, int priority) {
        Attributes rq = Commands.mkRQ(msgId, 32, withDatasetType);
        rq.setString(2, VR.UI, cuid);
        rq.setInt(1792, VR.US, priority);
        return rq;
    }

    public static Attributes mkCFindRSP(Attributes cmd, int status) {
        return Commands.mkRSP(cmd, status, Dimse.C_FIND_RQ);
    }

    public static Attributes mkCGetRQ(int msgId, String cuid, int priority) {
        Attributes rq = Commands.mkRQ(msgId, 16, withDatasetType);
        rq.setString(2, VR.UI, cuid);
        rq.setInt(1792, VR.US, priority);
        return rq;
    }

    public static Attributes mkCGetRSP(Attributes cmd, int status) {
        return Commands.mkRSP(cmd, status, Dimse.C_GET_RQ);
    }

    public static Attributes mkCMoveRQ(int msgId, String cuid, int priority, String destination) {
        Attributes rq = Commands.mkRQ(msgId, 33, withDatasetType);
        rq.setString(2, VR.UI, cuid);
        rq.setInt(1792, VR.US, priority);
        rq.setString(1536, VR.AE, destination);
        return rq;
    }

    public static Attributes mkCMoveRSP(Attributes cmd, int status) {
        return Commands.mkRSP(cmd, status, Dimse.C_MOVE_RQ);
    }

    public static Attributes mkCCancelRQ(int msgId) {
        Attributes rq = new Attributes();
        rq.setInt(256, VR.US, Dimse.C_CANCEL_RQ.commandField());
        rq.setInt(2048, VR.US, 257);
        rq.setInt(288, VR.US, msgId);
        return rq;
    }

    public static Attributes mkCEchoRQ(int msgId, String cuid) {
        Attributes rq = Commands.mkRQ(msgId, 48, 257);
        rq.setString(2, VR.UI, cuid);
        return rq;
    }

    public static Attributes mkEchoRSP(Attributes cmd, int status) {
        return Commands.mkRSP(cmd, status, Dimse.C_ECHO_RQ);
    }

    public static Attributes mkNEventReportRQ(int msgId, String cuid, String iuid, int eventTypeID, Attributes data) {
        Attributes rq = Commands.mkRQ(msgId, 256, data == null ? 257 : withDatasetType);
        rq.setString(2, VR.UI, cuid);
        rq.setString(4096, VR.UI, iuid);
        rq.setInt(4098, VR.US, eventTypeID);
        return rq;
    }

    public static Attributes mkNEventReportRSP(Attributes cmd, int status) {
        return Commands.mkRSP(cmd, status, Dimse.N_EVENT_REPORT_RQ);
    }

    public static Attributes mkNGetRQ(int msgId, String cuid, String iuid, int[] tags) {
        Attributes rq = Commands.mkRQ(msgId, 272, 257);
        rq.setString(3, VR.UI, cuid);
        rq.setString(4097, VR.UI, iuid);
        if (tags != null) {
            rq.setInt(4101, VR.AT, tags);
        }
        return rq;
    }

    public static Attributes mkNGetRSP(Attributes cmd, int status) {
        return Commands.mkRSP(cmd, status, Dimse.N_GET_RQ);
    }

    public static Attributes mkNSetRQ(int msgId, String cuid, String iuid) {
        Attributes rq = Commands.mkRQ(msgId, 288, withDatasetType);
        rq.setString(3, VR.UI, cuid);
        rq.setString(4097, VR.UI, iuid);
        return rq;
    }

    public static Attributes mkNSetRSP(Attributes cmd, int status) {
        return Commands.mkRSP(cmd, status, Dimse.N_SET_RQ);
    }

    public static Attributes mkNActionRQ(int msgId, String cuid, String iuid, int actionTypeID, Attributes data) {
        Attributes rq = Commands.mkRQ(msgId, 304, data == null ? 257 : withDatasetType);
        rq.setString(3, VR.UI, cuid);
        rq.setString(4097, VR.UI, iuid);
        rq.setInt(4104, VR.US, actionTypeID);
        return rq;
    }

    public static Attributes mkNActionRSP(Attributes cmd, int status) {
        return Commands.mkRSP(cmd, status, Dimse.N_ACTION_RQ);
    }

    public static Attributes mkNCreateRQ(int msgId, String cuid, String iuid) {
        Attributes rq = Commands.mkRQ(msgId, 320, withDatasetType);
        rq.setString(2, VR.UI, cuid);
        if (iuid != null) {
            rq.setString(4096, VR.UI, iuid);
        }
        return rq;
    }

    public static Attributes mkNCreateRSP(Attributes cmd, int status) {
        String iuid = cmd.getString(4096);
        if (iuid == null) {
            cmd.setString(4096, VR.UI, UIDUtils.createUID());
        }
        return Commands.mkRSP(cmd, status, Dimse.N_CREATE_RQ);
    }

    public static Attributes mkNDeleteRQ(int msgId, String cuid, String iuid) {
        Attributes rq = Commands.mkRQ(msgId, 336, 257);
        rq.setString(3, VR.UI, cuid);
        rq.setString(4097, VR.UI, iuid);
        return rq;
    }

    public static Attributes mkNDeleteRSP(Attributes cmd, int status) {
        return Commands.mkRSP(cmd, status, Dimse.N_DELETE_RQ);
    }

    private static Attributes mkRQ(int msgId, int cmdField, int datasetType) {
        Attributes rsp = new Attributes();
        rsp.setInt(272, VR.US, msgId);
        rsp.setInt(256, VR.US, cmdField);
        rsp.setInt(2048, VR.US, datasetType);
        return rsp;
    }

    public static Attributes mkRSP(Attributes rq, int status, Dimse rqCmd) {
        Attributes rsp = new Attributes();
        rsp.setInt(256, VR.US, rqCmd.commandFieldOfRSP());
        rsp.setInt(2304, VR.US, status);
        rsp.setInt(288, VR.US, rq.getInt(272, 0));
        rsp.setString(2, VR.UI, rq.getString(rqCmd.tagOfSOPClassUID()));
        int tagOfIUID = rqCmd.tagOfSOPInstanceUID();
        if (tagOfIUID != 0) {
            rsp.setString(4096, VR.UI, rq.getString(tagOfIUID));
        }
        return rsp;
    }

    public static void initNumberOfSuboperations(Attributes rsp, int remaining) {
        rsp.setInt(4128, VR.US, remaining);
        rsp.setInt(4129, VR.US, 0);
        rsp.setInt(4130, VR.US, 0);
        rsp.setInt(4131, VR.US, 0);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void incNumberOfSuboperations(int tag, Attributes rsp) {
        Attributes attributes = rsp;
        synchronized (attributes) {
            rsp.setInt(tag, VR.US, rsp.getInt(tag, 0) + 1);
            rsp.setInt(4128, VR.US, rsp.getInt(4128, 1) - 1);
        }
    }

    public static int getWithDatasetType() {
        return withDatasetType;
    }

    public static void setWithDatasetType(int withDatasetType) {
        if (withDatasetType == 257 || (withDatasetType & 0xFFFF0000) != 0) {
            throw new IllegalArgumentException("withDatasetType: " + Integer.toHexString(withDatasetType) + "H");
        }
        Commands.withDatasetType = withDatasetType;
    }

    public static boolean hasDataset(Attributes cmd) {
        return cmd.getInt(2048, 0) != 257;
    }
}

