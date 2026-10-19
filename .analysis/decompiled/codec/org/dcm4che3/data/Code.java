/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.data;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.VR;

public class Code
implements Serializable {
    private static final String NO_CODE_MEANING = "<none>";
    private static final long serialVersionUID = 8807594793107889446L;
    private String codeValue;
    private String codingSchemeDesignator;
    private String codingSchemeVersion;
    private String codeMeaning;
    private final transient Key key = new Key();

    public Code(String codeValue, String codingSchemeDesignator, String codingSchemeVersion, String codeMeaning) {
        if (codeValue == null) {
            throw new NullPointerException("Missing Code Value");
        }
        if (Code.isURN(codeValue)) {
            if (codingSchemeDesignator != null || codingSchemeVersion != null) {
                throw new IllegalArgumentException("URN Code Value with Coding Scheme Designator");
            }
        } else if (codingSchemeDesignator == null) {
            throw new NullPointerException("Missing Coding Scheme Designator");
        }
        if (codeMeaning == null) {
            throw new NullPointerException("Missing Code Meaning");
        }
        this.codeValue = codeValue;
        this.codingSchemeDesignator = codingSchemeDesignator;
        this.codingSchemeVersion = Code.nullifyDCM01(codingSchemeDesignator, codingSchemeVersion);
        this.codeMeaning = codeMeaning;
    }

    public Code(String s) {
        int len = s.length();
        if (len < 9 || s.charAt(0) != '(' || s.charAt(len - 2) != '\"' || s.charAt(len - 1) != ')') {
            throw new IllegalArgumentException(s);
        }
        int endVal = s.indexOf(44);
        int endScheme = s.indexOf(44, endVal + 1);
        int startMeaning = s.indexOf(34, endScheme + 1) + 1;
        this.codeValue = Code.trimsubstring(s, 1, endVal, false);
        if (Code.isURN(this.codeValue)) {
            Code.trimsubstring(s, endVal + 1, endScheme, true);
        } else {
            this.codingSchemeDesignator = Code.trimsubstring(s, endVal + 1, endScheme, false);
            if (this.codingSchemeDesignator.endsWith("]")) {
                int endVersion = s.lastIndexOf(93, endScheme - 1);
                endScheme = s.lastIndexOf(91, endVersion - 1);
                this.codingSchemeDesignator = Code.trimsubstring(s, endVal + 1, endScheme, false);
                this.codingSchemeVersion = Code.nullifyDCM01(this.codingSchemeDesignator, Code.trimsubstring(s, endScheme + 1, endVersion, false));
            }
        }
        this.codeMeaning = Code.trimsubstring(s, startMeaning, len - 2, false);
    }

    private static String nullifyDCM01(String codingSchemeDesignator, String codingSchemeVersion) {
        return "01".equals(codingSchemeVersion) && "DCM".equals(codingSchemeDesignator) ? null : codingSchemeVersion;
    }

    private static String trimsubstring(String s, int start, int end, boolean empty) {
        try {
            String trim = s.substring(start, end).trim();
            if (trim.isEmpty() == empty) {
                return trim;
            }
        }
        catch (StringIndexOutOfBoundsException stringIndexOutOfBoundsException) {
            // empty catch block
        }
        throw new IllegalArgumentException(s);
    }

    public Code(Attributes item) {
        this(Code.codeValueOf(item), item.getString(524546, null), item.getString(524547, null), item.getString(524548, NO_CODE_MEANING));
    }

    protected Code() {
    }

    public final String getCodeValue() {
        return this.codeValue;
    }

    public final String getCodingSchemeDesignator() {
        return this.codingSchemeDesignator;
    }

    public final String getCodingSchemeVersion() {
        return this.codingSchemeVersion;
    }

    public final String getCodeMeaning() {
        return this.codeMeaning;
    }

    public int hashCode() {
        return this.codeValue.hashCode();
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Code)) {
            return false;
        }
        Code other = (Code)o;
        return this.equalsIgnoreMeaning(other) && Objects.equals(this.codeMeaning, other.getCodeMeaning());
    }

    public boolean equalsIgnoreMeaning(Code other) {
        if (other == this) {
            return true;
        }
        return Objects.equals(this.codeValue, other.getCodeValue()) && Objects.equals(this.codingSchemeDesignator, other.getCodingSchemeDesignator()) && Objects.equals(this.codingSchemeVersion, other.getCodingSchemeVersion());
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('(').append(this.codeValue).append(", ");
        if (this.codingSchemeDesignator != null) {
            sb.append(this.codingSchemeDesignator);
            if (this.codingSchemeVersion != null) {
                sb.append(" [").append(this.codingSchemeVersion).append(']');
            }
        }
        sb.append(", \"").append(this.codeMeaning).append("\")");
        return sb.toString();
    }

    public Attributes toItem() {
        Attributes codeItem = new Attributes(this.codingSchemeVersion != null ? 4 : 3);
        if (this.codingSchemeDesignator == null) {
            codeItem.setString(524576, VR.UR, this.codeValue);
        } else {
            if (this.codeValue.length() > 16) {
                codeItem.setString(524569, VR.UC, this.codeValue);
            } else {
                codeItem.setString(524544, VR.SH, this.codeValue);
            }
            codeItem.setString(524546, VR.SH, this.codingSchemeDesignator);
            if (this.codingSchemeVersion != null) {
                codeItem.setString(524547, VR.SH, this.codingSchemeVersion);
            }
        }
        codeItem.setString(524548, VR.LO, this.codeMeaning);
        return codeItem;
    }

    private static String codeValueOf(Attributes item) {
        String codeValue = item.getString(524544);
        return codeValue != null ? codeValue : ((codeValue = item.getString(524569)) != null ? codeValue : item.getString(524576));
    }

    private static boolean isURN(String codeValue) {
        if (codeValue.indexOf(58) > 0) {
            try {
                if (!codeValue.startsWith("urn:")) {
                    new URL(codeValue);
                }
                return true;
            }
            catch (MalformedURLException malformedURLException) {
                // empty catch block
            }
        }
        return false;
    }

    public final Key key() {
        return this.key;
    }

    public final class Key {
        private Key() {
        }

        public int hashCode() {
            return Code.this.codeValue.hashCode();
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof Key)) {
                return false;
            }
            Key other = (Key)o;
            return Code.this.equalsIgnoreMeaning(other.outer());
        }

        private Code outer() {
            return Code.this;
        }
    }
}

