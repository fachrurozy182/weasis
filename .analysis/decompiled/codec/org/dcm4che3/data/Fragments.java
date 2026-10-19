/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ListIterator;
import org.dcm4che3.data.VR;
import org.dcm4che3.data.Value;
import org.dcm4che3.io.DicomEncodingOptions;
import org.dcm4che3.io.DicomOutputStream;

public class Fragments
extends ArrayList<Object>
implements Value {
    private static final long serialVersionUID = -6667210062541083610L;
    private final VR vr;
    private final boolean bigEndian;
    private volatile boolean readOnly;

    public Fragments(VR vr, boolean bigEndian, int initialCapacity) {
        super(initialCapacity);
        this.vr = vr;
        this.bigEndian = bigEndian;
    }

    public boolean isReadOnly() {
        return this.readOnly;
    }

    public void setReadOnly() {
        this.readOnly = true;
    }

    private void ensureModifiable() {
        if (this.readOnly) {
            throw new UnsupportedOperationException("read-only");
        }
    }

    public final VR vr() {
        return this.vr;
    }

    public final boolean bigEndian() {
        return this.bigEndian;
    }

    @Override
    public String toString() {
        return "" + this.size() + " Fragments";
    }

    @Override
    public boolean add(Object frag) {
        this.add(this.size(), frag);
        return true;
    }

    @Override
    public void add(int index, Object frag) {
        this.ensureModifiable();
        super.add(index, frag == null || frag instanceof byte[] && ((byte[])frag).length == 0 ? Value.NULL : frag);
    }

    @Override
    public boolean addAll(Collection<? extends Object> c) {
        return this.addAll(this.size(), c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends Object> c) {
        this.ensureModifiable();
        for (Object object : c) {
            this.add(index++, object);
        }
        return !c.isEmpty();
    }

    @Override
    public void writeTo(DicomOutputStream out, VR vr) throws IOException {
        for (Object frag : this) {
            out.writeAttribute(-73728, vr, frag, null);
        }
    }

    @Override
    public int calcLength(DicomEncodingOptions encOpts, boolean explicitVR, VR vr) {
        int len = 0;
        for (Object frag : this) {
            len += 8;
            if (frag instanceof Value) {
                len += ((Value)frag).calcLength(encOpts, explicitVR, vr);
                continue;
            }
            len += ((byte[])frag).length + 1 & 0xFFFFFFFE;
        }
        return len;
    }

    @Override
    public int getEncodedLength(DicomEncodingOptions encOpts, boolean explicitVR, VR vr) {
        return -1;
    }

    @Override
    public byte[] toBytes(VR vr, boolean bigEndian) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        Fragments other = (Fragments)obj;
        if (this.bigEndian != other.bigEndian) {
            return false;
        }
        if (this.vr != other.vr) {
            return false;
        }
        ListIterator e1 = this.listIterator();
        ListIterator e2 = other.listIterator();
        while (e1.hasNext() && e2.hasNext()) {
            Object o2;
            Object o1 = e1.next();
            if (this.itemsEqual(o1, o2 = e2.next())) continue;
            return false;
        }
        return !e1.hasNext() && !e2.hasNext();
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int hashCode = 1;
        for (Object e : this) {
            hashCode = 31 * hashCode + this.itemHashCode(e);
        }
        hashCode = 31 * hashCode + (this.bigEndian ? 1231 : 1237);
        hashCode = 31 * hashCode + (this.vr == null ? 0 : this.vr.hashCode());
        return hashCode;
    }

    private boolean itemsEqual(Object o1, Object o2) {
        if (o1 == null) {
            return o2 == null;
        }
        if (o1 instanceof byte[]) {
            if (o2 instanceof byte[] && ((byte[])o1).length == ((byte[])o2).length) {
                return Arrays.equals((byte[])o1, (byte[])o2);
            }
            return false;
        }
        return o1.equals(o2);
    }

    private int itemHashCode(Object e) {
        if (e == null) {
            return 0;
        }
        if (e instanceof byte[]) {
            return Arrays.hashCode((byte[])e);
        }
        return e.hashCode();
    }
}

