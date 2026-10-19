/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package org.dcm4che3.net;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.util.StringUtils;
import org.dcm4che3.util.TagUtils;
import org.dcm4che3.util.UIDUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum Dimse {
    C_STORE_RQ(1, 2, 4096, 272, ":C-STORE-RQ"),
    C_STORE_RSP(32769, 2, 4096, 288, ":C-STORE-RSP"),
    C_GET_RQ(16, 2, 0, 272, ":C-GET-RQ"),
    C_GET_RSP(32784, 2, 0, 288, ":C-GET-RSP"),
    C_FIND_RQ(32, 2, 0, 272, ":C-FIND-RQ"),
    C_FIND_RSP(32800, 2, 0, 288, ":C-FIND-RSP"),
    C_MOVE_RQ(33, 2, 0, 272, ":C-MOVE-RQ"),
    C_MOVE_RSP(32801, 2, 0, 288, ":C-MOVE-RSP"),
    C_ECHO_RQ(48, 2, 0, 272, ":C-ECHO-RQ"),
    C_ECHO_RSP(32816, 2, 0, 288, ":C-ECHO-RSP"),
    N_EVENT_REPORT_RQ(256, 2, 4096, 272, ":N-EVENT-REPORT-RQ"),
    N_EVENT_REPORT_RSP(33024, 2, 4096, 288, ":N-EVENT-REPORT-RSP"),
    N_GET_RQ(272, 3, 4097, 272, ":N-GET-RQ"),
    N_GET_RSP(33040, 2, 4096, 288, ":N-GET-RSP"),
    N_SET_RQ(288, 3, 4097, 272, ":N-SET-RQ"),
    N_SET_RSP(33056, 2, 4096, 288, ":N-SET-RSP"),
    N_ACTION_RQ(304, 3, 4097, 272, ":N-ACTION-RQ"),
    N_ACTION_RSP(33072, 2, 4096, 288, ":N-ACTION-RSP"),
    N_CREATE_RQ(320, 2, 4096, 272, ":N-CREATE-RQ"),
    N_CREATE_RSP(33088, 2, 4096, 288, ":N-CREATE-RSP"),
    N_DELETE_RQ(336, 3, 4097, 272, ":N-DELETE-RQ"),
    N_DELETE_RSP(33104, 2, 4096, 288, ":N-DELETE-RSP"),
    C_CANCEL_RQ(4095, 0, 0, 288, ":C-CANCEL-RQ");

    public static final Logger LOG;
    private final int commandField;
    private final int tagOfSOPClassUID;
    private final int tagOfSOPInstanceUID;
    private final int tagOfMessageID;
    private final String prompt;

    private Dimse(int cmdField, int tagOfSOPClassUID, int tagOfSOPInstanceUID, int tagOfMessageID, String prompt) {
        this.commandField = cmdField;
        this.tagOfSOPClassUID = tagOfSOPClassUID;
        this.tagOfSOPInstanceUID = tagOfSOPInstanceUID;
        this.tagOfMessageID = tagOfMessageID;
        this.prompt = prompt;
    }

    public int commandField() {
        return this.commandField;
    }

    public int tagOfSOPClassUID() {
        return this.tagOfSOPClassUID;
    }

    public int tagOfSOPInstanceUID() {
        return this.tagOfSOPInstanceUID;
    }

    public boolean isRSP() {
        return (this.commandField & 0x8000) != 0;
    }

    public boolean isRetrieveRQ() {
        return this == C_GET_RQ || this == C_MOVE_RQ;
    }

    public boolean isRetrieveRSP() {
        return this == C_GET_RSP || this == C_MOVE_RSP;
    }

    public boolean isCService() {
        return (this.commandField & 0x100) == 0;
    }

    public int commandFieldOfRSP() {
        return this.commandField | 0x8000;
    }

    public static Dimse valueOf(int commandField) {
        switch (commandField) {
            case 1: {
                return C_STORE_RQ;
            }
            case 32769: {
                return C_STORE_RSP;
            }
            case 16: {
                return C_GET_RQ;
            }
            case 32784: {
                return C_GET_RSP;
            }
            case 32: {
                return C_FIND_RQ;
            }
            case 32800: {
                return C_FIND_RSP;
            }
            case 33: {
                return C_MOVE_RQ;
            }
            case 32801: {
                return C_MOVE_RSP;
            }
            case 48: {
                return C_ECHO_RQ;
            }
            case 32816: {
                return C_ECHO_RSP;
            }
            case 256: {
                return N_EVENT_REPORT_RQ;
            }
            case 33024: {
                return N_EVENT_REPORT_RSP;
            }
            case 272: {
                return N_GET_RQ;
            }
            case 33040: {
                return N_GET_RSP;
            }
            case 288: {
                return N_SET_RQ;
            }
            case 33056: {
                return N_SET_RSP;
            }
            case 304: {
                return N_ACTION_RQ;
            }
            case 33072: {
                return N_ACTION_RSP;
            }
            case 320: {
                return N_CREATE_RQ;
            }
            case 33088: {
                return N_CREATE_RSP;
            }
            case 336: {
                return N_DELETE_RQ;
            }
            case 33104: {
                return N_DELETE_RSP;
            }
            case 4095: {
                return C_CANCEL_RQ;
            }
        }
        throw new IllegalArgumentException("commandField: " + commandField);
    }

    public String toString(Attributes cmdAttrs) {
        return cmdAttrs.getInt(this.tagOfMessageID, -1) + this.prompt;
    }

    public String toString(Attributes cmdAttrs, int pcid, String tsuid) {
        StringBuilder sb = new StringBuilder();
        sb.append(cmdAttrs.getInt(this.tagOfMessageID, -1)).append(this.prompt).append("[pcid=").append(pcid);
        switch (this.ordinal()) {
            case 0: {
                Dimse.promptIntTo(cmdAttrs, ", prior=", 1792, sb);
                Dimse.promptMoveOriginatorTo(cmdAttrs, sb);
                break;
            }
            case 2: 
            case 4: {
                Dimse.promptIntTo(cmdAttrs, ", prior=", 1792, sb);
                break;
            }
            case 6: {
                Dimse.promptIntTo(cmdAttrs, ", prior=", 1792, sb);
                Dimse.promptMoveDestination(cmdAttrs, sb);
                break;
            }
            case 3: 
            case 7: {
                Dimse.promptNumberOfSubOpsTo(cmdAttrs, sb);
                break;
            }
            case 10: 
            case 11: {
                Dimse.promptIntTo(cmdAttrs, ", eventID=", 4098, sb);
                break;
            }
            case 12: {
                Dimse.promptAttributeIdentifierListTo(cmdAttrs, sb);
                break;
            }
            case 16: 
            case 17: {
                Dimse.promptIntTo(cmdAttrs, ", actionID=", 4104, sb);
            }
        }
        if (this.isRSP()) {
            sb.append(", status=").append(Integer.toHexString(cmdAttrs.getInt(2304, -1))).append('H');
            Dimse.promptIntTo(cmdAttrs, ", errorID=", 2307, sb);
            Dimse.promptStringTo(cmdAttrs, ", errorComment=", 2306, sb);
            Dimse.promptAttributeIdentifierListTo(cmdAttrs, sb);
        }
        Dimse.promptUIDTo(cmdAttrs, "  cuid=", this.tagOfSOPClassUID, sb);
        Dimse.promptUIDTo(cmdAttrs, "  iuid=", this.tagOfSOPInstanceUID, sb);
        Dimse.promptUIDTo("  tsuid=", tsuid, sb);
        sb.append(']');
        return sb.toString();
    }

    private static void promptIntTo(Attributes cmd, String name, int tag, StringBuilder sb) {
        int val = cmd.getInt(tag, 0);
        if (val != 0 || cmd.containsValue(tag)) {
            sb.append(name).append(val);
        }
    }

    private static void promptStringTo(Attributes cmd, String name, int tag, StringBuilder sb) {
        String s = cmd.getString(tag, null);
        if (s != null) {
            sb.append(name).append(s);
        }
    }

    private static void promptUIDTo(Attributes cmd, String name, int tag, StringBuilder sb) {
        String uid;
        if (tag != 0 && (uid = cmd.getString(tag, null)) != null) {
            Dimse.promptUIDTo(name, uid, sb);
        }
    }

    private static void promptMoveDestination(Attributes cmd, StringBuilder sb) {
        sb.append(", dest=").append(cmd.getString(1536));
    }

    private static void promptUIDTo(String name, String uid, StringBuilder sb) {
        sb.append(StringUtils.LINE_SEPARATOR).append(name);
        UIDUtils.promptTo(uid, sb);
    }

    private static void promptMoveOriginatorTo(Attributes cmd, StringBuilder sb) {
        String aet = cmd.getString(4144, null);
        if (aet != null) {
            sb.append(StringUtils.LINE_SEPARATOR).append("  orig=").append(aet).append(" >> ").append(cmd.getInt(4145, -1)).append(":C-MOVE-RQ");
        }
    }

    private static void promptAttributeIdentifierListTo(Attributes cmd, StringBuilder sb) {
        int[] tags = cmd.getInts(4101);
        if (tags == null) {
            return;
        }
        sb.append(StringUtils.LINE_SEPARATOR).append("  tags=[");
        if (tags.length > 0) {
            for (int tag : tags) {
                sb.append(TagUtils.toString(tag)).append(", ");
            }
            sb.setLength(sb.length() - 2);
        }
        sb.append(']');
    }

    private static void promptNumberOfSubOpsTo(Attributes cmd, StringBuilder sb) {
        Dimse.promptIntTo(cmd, ", remaining=", 4128, sb);
        Dimse.promptIntTo(cmd, ", completed=", 4129, sb);
        Dimse.promptIntTo(cmd, ", failed=", 4130, sb);
        Dimse.promptIntTo(cmd, ", warning=", 4131, sb);
    }

    static {
        LOG = LoggerFactory.getLogger(Dimse.class);
    }
}

