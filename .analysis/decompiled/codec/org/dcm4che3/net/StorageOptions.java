/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net;

import java.io.Serializable;
import org.dcm4che3.net.pdu.ExtendedNegotiation;

public class StorageOptions
implements Serializable {
    private static final long serialVersionUID = 6911502883119290413L;
    private LevelOfSupport levelOfSupport;
    private DigitalSignatureSupport digitalSignatureSupport;
    private ElementCoercion elementCoercion;

    public StorageOptions() {
        this(LevelOfSupport.UNSPECIFIED, DigitalSignatureSupport.UNSPECIFIED, ElementCoercion.UNSPECIFIED);
    }

    public StorageOptions(LevelOfSupport levelOfSupport, DigitalSignatureSupport levelOfDigitalSignatureSupport, ElementCoercion getElementCoercion) {
        this.levelOfSupport = levelOfSupport;
        this.digitalSignatureSupport = levelOfDigitalSignatureSupport;
        this.elementCoercion = getElementCoercion;
    }

    public final LevelOfSupport getLevelOfSupport() {
        return this.levelOfSupport;
    }

    public final void setLevelOfSupport(LevelOfSupport levelOfSupport) {
        this.levelOfSupport = levelOfSupport;
    }

    public final DigitalSignatureSupport getDigitalSignatureSupport() {
        return this.digitalSignatureSupport;
    }

    public final void setDigitalSignatureSupport(DigitalSignatureSupport digitalSignatureSupport) {
        this.digitalSignatureSupport = digitalSignatureSupport;
    }

    public final ElementCoercion getElementCoercion() {
        return this.elementCoercion;
    }

    public final void setElementCoercion(ElementCoercion elementCoercion) {
        this.elementCoercion = elementCoercion;
    }

    public byte[] toExtendedNegotiationInformation() {
        return new byte[]{(byte)this.levelOfSupport.ordinal(), 0, (byte)this.digitalSignatureSupport.ordinal(), 0, (byte)this.elementCoercion.ordinal(), 0};
    }

    public static StorageOptions valueOf(ExtendedNegotiation extNeg) {
        return new StorageOptions(LevelOfSupport.valueOf(extNeg.getField(0, (byte)3)), DigitalSignatureSupport.valueOf(extNeg.getField(2, (byte)0)), ElementCoercion.valueOf(extNeg.getField(4, (byte)2)));
    }

    public int hashCode() {
        return this.levelOfSupport.hashCode() + this.digitalSignatureSupport.hashCode() + this.elementCoercion.hashCode();
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof StorageOptions)) {
            return false;
        }
        StorageOptions other = (StorageOptions)o;
        return this.levelOfSupport == other.levelOfSupport && this.digitalSignatureSupport == other.digitalSignatureSupport && this.elementCoercion == other.elementCoercion;
    }

    public String toString() {
        return "StorageOptions[levelOfSupport=" + this.levelOfSupport.ordinal() + ", digitalSignatureSupport=" + this.digitalSignatureSupport.ordinal() + ", elementCoercion=" + this.elementCoercion.ordinal() + "]";
    }

    public static enum LevelOfSupport {
        LEVEL_0,
        LEVEL_1,
        LEVEL_2,
        UNSPECIFIED;


        public static LevelOfSupport valueOf(int level) {
            switch (level) {
                case 0: {
                    return LEVEL_0;
                }
                case 1: {
                    return LEVEL_1;
                }
                case 2: {
                    return LEVEL_2;
                }
            }
            return UNSPECIFIED;
        }
    }

    public static enum DigitalSignatureSupport {
        UNSPECIFIED,
        LEVEL_1,
        LEVEL_2,
        LEVEL_3;


        public static DigitalSignatureSupport valueOf(int level) {
            switch (level) {
                case 1: {
                    return LEVEL_1;
                }
                case 2: {
                    return LEVEL_2;
                }
                case 3: {
                    return LEVEL_3;
                }
            }
            return UNSPECIFIED;
        }
    }

    public static enum ElementCoercion {
        NO,
        YES,
        UNSPECIFIED;


        public static ElementCoercion valueOf(int i) {
            switch (i) {
                case 0: {
                    return NO;
                }
                case 1: {
                    return YES;
                }
            }
            return UNSPECIFIED;
        }
    }
}

