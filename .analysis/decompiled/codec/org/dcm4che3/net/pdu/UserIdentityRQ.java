/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net.pdu;

import java.nio.charset.StandardCharsets;
import org.dcm4che3.util.StringUtils;

public class UserIdentityRQ {
    public static final int USERNAME = 1;
    public static final int USERNAME_PASSCODE = 2;
    public static final int KERBEROS = 3;
    public static final int SAML = 4;
    public static final int JWT = 5;
    private static final String[] TYPES = new String[]{"0", "1 - Username", "2 - Username and passcode", "3 - Kerberos Service ticket", "4 - SAML Assertion", "5 - JSON Web Token (JWT)"};
    private final int type;
    private final boolean rspReq;
    private final byte[] primaryField;
    private final byte[] secondaryField;

    public UserIdentityRQ(int type, boolean rspReq, byte[] primaryField, byte[] secondaryField) {
        this.type = type;
        this.rspReq = rspReq;
        this.primaryField = (byte[])primaryField.clone();
        this.secondaryField = secondaryField != null ? (byte[])secondaryField.clone() : new byte[]{};
    }

    public UserIdentityRQ(int type, boolean rspReq, byte[] primaryField) {
        this(type, rspReq, primaryField, null);
    }

    @Deprecated
    public UserIdentityRQ(String username, char[] passcode) {
        this(2, true, UserIdentityRQ.toBytes(username), UserIdentityRQ.toBytes(new String(passcode)));
    }

    @Deprecated
    public UserIdentityRQ(String username, boolean rspReq) {
        this(1, rspReq, UserIdentityRQ.toBytes(username));
    }

    public static UserIdentityRQ username(String username, boolean rspReq) {
        return new UserIdentityRQ(1, rspReq, UserIdentityRQ.toBytes(username));
    }

    public static UserIdentityRQ usernamePasscode(String username, char[] passcode, boolean rspReq) {
        return new UserIdentityRQ(2, rspReq, UserIdentityRQ.toBytes(username), UserIdentityRQ.toBytes(new String(passcode)));
    }

    public static UserIdentityRQ kerberos(byte[] ticket, boolean rspReq) {
        return new UserIdentityRQ(3, rspReq, ticket);
    }

    public static UserIdentityRQ saml(String assertion, boolean rspReq) {
        return new UserIdentityRQ(4, rspReq, UserIdentityRQ.toBytes(assertion));
    }

    public static UserIdentityRQ jwt(String token, boolean rspReq) {
        return new UserIdentityRQ(5, rspReq, UserIdentityRQ.toBytes(token));
    }

    private static String typeAsString(int type) {
        try {
            return TYPES[type];
        }
        catch (IndexOutOfBoundsException e) {
            return Integer.toString(type);
        }
    }

    public final int getType() {
        return this.type;
    }

    public final boolean isPositiveResponseRequested() {
        return this.rspReq;
    }

    public final byte[] getPrimaryField() {
        return (byte[])this.primaryField.clone();
    }

    public final byte[] getSecondaryField() {
        return (byte[])this.secondaryField.clone();
    }

    public final String getUsername() {
        return UserIdentityRQ.toString(this.primaryField);
    }

    public final char[] getPasscode() {
        return UserIdentityRQ.toString(this.secondaryField).toCharArray();
    }

    private static byte[] toBytes(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }

    private static String toString(byte[] b) {
        return new String(b, StandardCharsets.UTF_8);
    }

    public int length() {
        return 6 + this.primaryField.length + this.secondaryField.length;
    }

    public String toString() {
        return this.promptTo(new StringBuilder()).toString();
    }

    StringBuilder promptTo(StringBuilder sb) {
        sb.append("  UserIdentity[").append(StringUtils.LINE_SEPARATOR).append("    type: ").append(UserIdentityRQ.typeAsString(this.type)).append(StringUtils.LINE_SEPARATOR);
        if (this.type == 1 || this.type == 2) {
            sb.append("    username: ").append(this.getUsername());
        } else {
            sb.append("    primaryField: byte[").append(this.primaryField.length).append(']');
        }
        if (this.type == 2) {
            sb.append(StringUtils.LINE_SEPARATOR).append("    passcode: ");
            int i = this.secondaryField.length;
            while (--i >= 0) {
                sb.append('*');
            }
        } else if (this.secondaryField.length > 0) {
            sb.append(StringUtils.LINE_SEPARATOR).append("    secondaryField: byte[").append(this.secondaryField.length).append(']');
        }
        return sb.append(StringUtils.LINE_SEPARATOR).append("  ]");
    }
}

