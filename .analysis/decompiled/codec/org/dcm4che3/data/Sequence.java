/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ListIterator;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.VR;
import org.dcm4che3.data.Value;
import org.dcm4che3.io.DicomEncodingOptions;
import org.dcm4che3.io.DicomOutputStream;

public class Sequence
extends ArrayList<Attributes>
implements Value {
    private static final long serialVersionUID = 7062970085409148066L;
    private final Attributes parent;
    private final String privateCreator;
    private final int tag;
    private volatile int length = -1;
    private volatile boolean readOnly;

    Sequence(Attributes parent, String privateCreator, int tag, int initialCapacity) {
        super(initialCapacity);
        this.parent = parent;
        this.privateCreator = privateCreator;
        this.tag = tag;
    }

    public boolean isReadOnly() {
        return this.readOnly;
    }

    public void setReadOnly() {
        this.readOnly = true;
        for (Attributes attrs : this) {
            attrs.setReadOnly();
        }
    }

    private void ensureModifiable() {
        if (this.readOnly) {
            throw new UnsupportedOperationException("read-only");
        }
    }

    public final Attributes getParent() {
        return this.parent;
    }

    public void trimToSize(boolean recursive) {
        this.ensureModifiable();
        super.trimToSize();
        if (recursive) {
            for (Attributes attrs : this) {
                attrs.trimToSize(recursive);
            }
        }
    }

    @Override
    public int indexOf(Object o) {
        ListIterator it = this.listIterator();
        while (it.hasNext()) {
            if (it.next() != o) continue;
            return it.previousIndex();
        }
        return -1;
    }

    @Override
    public boolean add(Attributes attrs) {
        this.ensureModifiable();
        return super.add(attrs.setParent(this.parent, this.privateCreator, this.tag));
    }

    @Override
    public void add(int index, Attributes attrs) {
        this.ensureModifiable();
        super.add(index, attrs.setParent(this.parent, this.privateCreator, this.tag));
    }

    @Override
    public boolean addAll(Collection<? extends Attributes> c) {
        this.ensureModifiable();
        this.setParent(c);
        return super.addAll(c);
    }

    private void setParent(Collection<? extends Attributes> c) {
        boolean bigEndian = this.parent.bigEndian();
        for (Attributes attributes : c) {
            if (attributes.bigEndian() != bigEndian) {
                throw new IllegalArgumentException("Endian of Item must match Endian of parent Data Set");
            }
            if (attributes.isRoot()) continue;
            throw new IllegalArgumentException("Item already contained by Sequence");
        }
        for (Attributes attributes : c) {
            attributes.setParent(this.parent, this.privateCreator, this.tag);
        }
    }

    @Override
    public boolean addAll(int index, Collection<? extends Attributes> c) {
        this.ensureModifiable();
        this.setParent(c);
        return super.addAll(index, c);
    }

    @Override
    public void clear() {
        this.ensureModifiable();
        for (Attributes attrs : this) {
            attrs.setParent(null, null, 0);
        }
        super.clear();
    }

    @Override
    public Attributes remove(int index) {
        this.ensureModifiable();
        return ((Attributes)super.remove(index)).setParent(null, null, 0);
    }

    @Override
    public boolean remove(Object o) {
        this.ensureModifiable();
        if (o instanceof Attributes && super.remove(o)) {
            ((Attributes)o).setParent(null, null, 0);
            return true;
        }
        return false;
    }

    @Override
    public Attributes set(int index, Attributes attrs) {
        this.ensureModifiable();
        return super.set(index, attrs.setParent(this.parent, this.privateCreator, this.tag));
    }

    @Override
    public String toString() {
        return "" + this.size() + " Items";
    }

    @Override
    public int calcLength(DicomEncodingOptions encOpts, boolean explicitVR, VR vr) {
        int len = 0;
        for (Attributes item : this) {
            len += 8 + item.calcLength(encOpts, explicitVR);
            if (!(item.isEmpty() ? encOpts.undefEmptyItemLength : encOpts.undefItemLength)) continue;
            len += 8;
        }
        if (this.isEmpty() ? encOpts.undefEmptySequenceLength : encOpts.undefSequenceLength) {
            len += 8;
        }
        this.length = len;
        return len;
    }

    @Override
    public int getEncodedLength(DicomEncodingOptions encOpts, boolean explicitVR, VR vr) {
        if (this.isEmpty()) {
            return encOpts.undefEmptySequenceLength ? -1 : 0;
        }
        if (encOpts.undefSequenceLength) {
            return -1;
        }
        if (this.length == -1) {
            this.calcLength(encOpts, explicitVR, vr);
        }
        return this.length;
    }

    @Override
    public void writeTo(DicomOutputStream out, VR vr) throws IOException {
        for (Attributes item : this) {
            item.writeItemTo(out);
        }
    }

    @Override
    public byte[] toBytes(VR vr, boolean bigEndian) throws IOException {
        throw new UnsupportedOperationException();
    }
}

