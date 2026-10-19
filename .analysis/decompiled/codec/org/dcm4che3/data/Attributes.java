/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package org.dcm4che3.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.regex.Pattern;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.Code;
import org.dcm4che3.data.DatePrecision;
import org.dcm4che3.data.DatePrecisions;
import org.dcm4che3.data.DateRange;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.Fragments;
import org.dcm4che3.data.IOD;
import org.dcm4che3.data.Implementation;
import org.dcm4che3.data.IncompatibleSpecificCharacterSetException;
import org.dcm4che3.data.ItemPointer;
import org.dcm4che3.data.PersonName;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.SpecificCharacterSet;
import org.dcm4che3.data.VR;
import org.dcm4che3.data.ValidationResult;
import org.dcm4che3.data.Value;
import org.dcm4che3.io.DicomEncodingOptions;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.util.ByteUtils;
import org.dcm4che3.util.DateUtils;
import org.dcm4che3.util.StringUtils;
import org.dcm4che3.util.TagUtils;
import org.dcm4che3.util.UIDUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Attributes
implements Serializable {
    public static final String COERCE = "COERCE";
    public static final String CORRECT = "CORRECT";
    private static final Logger LOG = LoggerFactory.getLogger(Attributes.class);
    private static final int INIT_CAPACITY = 16;
    private static final int TO_STRING_LIMIT = Integer.getInteger("org.dcm4che3.Attributes.toString.limit", 50);
    private static final int TO_STRING_WIDTH = Integer.getInteger("org.dcm4che3.Attributes.toString.width", 78);
    private transient Attributes parent;
    private transient String parentSequencePrivateCreator;
    private transient int parentSequenceTag;
    private transient int[] tags;
    private transient VR[] vrs;
    private transient Object[] values;
    private transient int size;
    private transient SpecificCharacterSet cs;
    private transient TimeZone tz;
    private transient int length = -1;
    private transient int[] groupLengths;
    private transient int groupLengthIndex0;
    private volatile boolean bigEndian;
    private long itemPosition = -1L;
    private boolean containsSpecificCharacterSet;
    private boolean containsTimezoneOffsetFromUTC;
    private Map<String, Object> properties;
    private TimeZone defaultTimeZone;
    private volatile boolean readOnly;
    private static final long serialVersionUID = 7868714416968825241L;

    public Attributes() {
        this(false, 16);
    }

    public Attributes(boolean bigEndian) {
        this(bigEndian, 16);
    }

    public Attributes(int initialCapacity) {
        this(false, initialCapacity);
    }

    public Attributes(boolean bigEndian, int initialCapacity) {
        this.bigEndian = bigEndian;
        this.init(initialCapacity);
    }

    public void clear() {
        this.size = 0;
        Arrays.fill(this.tags, 0);
        Arrays.fill((Object[])this.vrs, null);
        Arrays.fill(this.values, null);
    }

    private void init(int initialCapacity) {
        this.tags = new int[initialCapacity];
        this.vrs = new VR[initialCapacity];
        this.values = new Object[initialCapacity];
    }

    public Attributes(Attributes other) {
        this(other, other.bigEndian);
    }

    public Attributes(Attributes other, boolean bigEndian) {
        this(bigEndian, other.size);
        if (other.properties != null) {
            this.properties = new HashMap<String, Object>(other.properties);
        }
        this.addAll(other);
    }

    public Attributes(Attributes other, int ... selection) {
        this(other, other.bigEndian, selection);
    }

    public Attributes(Attributes other, boolean bigEndian, int ... selection) {
        this(bigEndian, selection.length);
        if (other.properties != null) {
            this.properties = new HashMap<String, Object>(other.properties);
        }
        this.addSelected(other, selection);
    }

    public Attributes(Attributes other, boolean bigEndian, Attributes selection) {
        this(bigEndian, selection.size());
        if (other.properties != null) {
            this.properties = new HashMap<String, Object>(other.properties);
        }
        this.addSelected(other, selection);
    }

    public boolean isReadOnly() {
        return this.readOnly;
    }

    public void setReadOnly() {
        this.readOnly = true;
        int n = this.size;
        for (int i = 0; i < n; ++i) {
            Object value = this.values[i];
            if (value instanceof Sequence) {
                ((Sequence)value).setReadOnly();
                continue;
            }
            if (!(value instanceof Fragments)) continue;
            ((Fragments)value).setReadOnly();
        }
    }

    private void ensureModifiable() {
        if (this.readOnly) {
            throw new UnsupportedOperationException("read-only");
        }
    }

    public Map<String, Object> getProperties() {
        return this.properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.ensureModifiable();
        this.properties = properties;
    }

    public Object getProperty(String key, Object defVal) {
        if (this.properties == null) {
            return defVal;
        }
        Object val = this.properties.get(key);
        return val != null ? val : defVal;
    }

    public Object setProperty(String key, Object value) {
        this.ensureModifiable();
        if (this.properties == null) {
            this.properties = new HashMap<String, Object>();
        }
        return this.properties.put(key, value);
    }

    public Object clearProperty(String key) {
        this.ensureModifiable();
        return this.properties != null ? this.properties.remove(key) : null;
    }

    public final boolean isRoot() {
        return this.parent == null;
    }

    public final Attributes getRoot() {
        return this.isRoot() ? this : this.parent.getRoot();
    }

    public final int getLevel() {
        return this.isRoot() ? 0 : 1 + this.parent.getLevel();
    }

    public final boolean bigEndian() {
        return this.bigEndian;
    }

    public final Attributes getParent() {
        return this.parent;
    }

    public String getParentSequencePrivateCreator() {
        return this.parentSequencePrivateCreator;
    }

    public int getParentSequenceTag() {
        return this.parentSequenceTag;
    }

    public final int getLength() {
        return this.length;
    }

    Attributes setParent(Attributes parent, String parentSequencePrivateCreator, int parentSequenceTag) {
        if (parent != null) {
            if (this.parent != null) {
                throw new IllegalArgumentException("Item already contained by Sequence");
            }
            if (!this.containsSpecificCharacterSet) {
                this.cs = null;
            }
            if (!this.containsTimezoneOffsetFromUTC) {
                this.tz = null;
            }
            if (parent.bigEndian != this.bigEndian) {
                this.toggleEndian();
            }
        }
        this.parent = parent;
        this.parentSequencePrivateCreator = parentSequencePrivateCreator;
        this.parentSequenceTag = parentSequenceTag;
        return this;
    }

    private void toggleEndian() {
        for (int i = 0; i < this.size; ++i) {
            Object value = this.values[i];
            if (value instanceof byte[]) {
                this.vrs[i].toggleEndian((byte[])value, false);
            } else if (value instanceof Sequence) {
                for (Attributes item : (Sequence)value) {
                    item.toggleEndian();
                }
            }
            this.bigEndian = !this.bigEndian;
        }
    }

    public final long getItemPosition() {
        return this.itemPosition;
    }

    public final void setItemPosition(long itemPosition) {
        this.itemPosition = itemPosition;
    }

    public final boolean isEmpty() {
        return this.size == 0;
    }

    public final int size() {
        return this.size;
    }

    public ItemPointer[] itemPointers() {
        return this.itemPointers(0);
    }

    private ItemPointer[] itemPointers(int n) {
        if (this.parent == null) {
            return new ItemPointer[n];
        }
        ItemPointer[] itemPointers = this.parent.itemPointers(n + 1);
        itemPointers[itemPointers.length - n - 1] = new ItemPointer(this.parentSequencePrivateCreator, this.parentSequenceTag, this.itemIndex());
        return itemPointers;
    }

    public int itemIndex() {
        if (this.parent == null) {
            return -1;
        }
        Sequence seq = this.parent.getSequence(this.parentSequencePrivateCreator, this.parentSequenceTag);
        if (seq == null) {
            return -1;
        }
        return seq.indexOf(this);
    }

    public int[] tags() {
        return Arrays.copyOf(this.tags, this.size);
    }

    public void trimToSize() {
        this.trimToSize(false);
    }

    public void trimToSize(boolean recursive) {
        this.ensureModifiable();
        int oldCapacity = this.tags.length;
        if (this.size < oldCapacity) {
            this.tags = Arrays.copyOf(this.tags, this.size);
            this.vrs = Arrays.copyOf(this.vrs, this.size);
            this.values = Arrays.copyOf(this.values, this.size);
        }
        if (recursive) {
            for (Object value : this.values) {
                if (value instanceof Sequence) {
                    ((Sequence)value).trimToSize(recursive);
                    continue;
                }
                if (!(value instanceof Fragments)) continue;
                ((Fragments)value).trimToSize();
            }
        }
    }

    public void internalizeStringValues(boolean decode) {
        this.ensureModifiable();
        SpecificCharacterSet cs = this.getSpecificCharacterSet();
        for (int i = 0; i < this.values.length; ++i) {
            VR vr = this.vrs[i];
            Object value = this.values[i];
            if (vr.isStringType()) {
                if (value instanceof byte[]) {
                    if (!decode) continue;
                    value = vr.toStrings((byte[])value, this.bigEndian, cs);
                }
                if (value instanceof String) {
                    this.values[i] = ((String)value).intern();
                    continue;
                }
                if (!(value instanceof String[])) continue;
                String[] ss = (String[])value;
                for (int j = 0; j < ss.length; ++j) {
                    ss[j] = ss[j].intern();
                }
                continue;
            }
            if (!(value instanceof Sequence)) continue;
            for (Attributes item : (Sequence)value) {
                item.internalizeStringValues(decode);
            }
        }
    }

    private void decodeStringValuesUsingSpecificCharacterSet() {
        SpecificCharacterSet cs = this.getSpecificCharacterSet();
        for (int i = 0; i < this.size; ++i) {
            Object value = this.values[i];
            if (value instanceof Sequence) {
                for (Attributes item : (Sequence)value) {
                    item.decodeStringValuesUsingSpecificCharacterSet();
                }
                continue;
            }
            VR vr = this.vrs[i];
            if (!vr.useSpecificCharacterSet() || !(value instanceof byte[])) continue;
            this.values[i] = vr.toStrings((byte[])value, this.bigEndian, cs);
        }
    }

    private void ensureCapacity(int minCapacity) {
        int oldCapacity = this.tags.length;
        if (minCapacity > oldCapacity) {
            int newCapacity = Math.max(minCapacity, oldCapacity << 1);
            this.tags = Arrays.copyOf(this.tags, newCapacity);
            this.vrs = Arrays.copyOf(this.vrs, newCapacity);
            this.values = Arrays.copyOf(this.values, newCapacity);
        }
    }

    public Attributes getNestedDataset(int sequenceTag) {
        return this.getNestedDataset(null, sequenceTag, 0);
    }

    public Attributes getNestedDataset(int sequenceTag, int itemIndex) {
        return this.getNestedDataset(null, sequenceTag, itemIndex);
    }

    public Attributes getNestedDataset(String privateCreator, int sequenceTag) {
        return this.getNestedDataset(privateCreator, sequenceTag, 0);
    }

    public Attributes getNestedDataset(String privateCreator, int sequenceTag, int itemIndex) {
        Sequence value = this.getSequence(privateCreator, sequenceTag);
        if (value == null) {
            return null;
        }
        Sequence sq = value;
        if (itemIndex >= sq.size()) {
            return null;
        }
        return (Attributes)sq.get(itemIndex);
    }

    public Attributes getNestedDataset(ItemPointer ... itemPointers) {
        return this.getNestedDataset(Arrays.asList(itemPointers));
    }

    public Attributes getNestedDataset(List<ItemPointer> itemPointers) {
        Attributes item = this;
        for (ItemPointer ip : itemPointers) {
            Object value = item.getValue(ip.privateCreator, ip.sequenceTag);
            if (!(value instanceof Sequence)) {
                return null;
            }
            Sequence sq = (Sequence)value;
            if (ip.itemIndex >= sq.size()) {
                return null;
            }
            item = (Attributes)sq.get(ip.itemIndex);
        }
        return item;
    }

    public Attributes getFunctionGroup(int sequenceTag, int frameIndex) {
        Attributes sfgs = this.getNestedDataset(1375769129);
        if (sfgs == null) {
            return null;
        }
        Attributes item = sfgs.getNestedDataset(sequenceTag);
        if (item != null) {
            return item;
        }
        Attributes fgs = this.getNestedDataset(1375769136, frameIndex);
        if (fgs == null) {
            return null;
        }
        return fgs.getNestedDataset(sequenceTag);
    }

    private int indexForInsertOf(int tag) {
        return this.size == 0 ? -1 : (this.tags[this.size - 1] < tag ? -(this.size + 1) : this.indexOf(tag));
    }

    private int indexOf(int tag) {
        return Arrays.binarySearch(this.tags, 0, this.size, tag);
    }

    private int indexOf(String privateCreator, int tag) {
        if (privateCreator != null) {
            int creatorTag = this.creatorTagOf(privateCreator, tag, false);
            if (creatorTag == -1) {
                return -1;
            }
            tag = TagUtils.toPrivateTag(creatorTag, tag);
        }
        return this.indexOf(tag);
    }

    public int tagOf(String privateCreator, int tag) {
        if (privateCreator != null) {
            int creatorTag = this.creatorTagOf(privateCreator, tag, false);
            if (creatorTag == -1) {
                return -1;
            }
            tag = TagUtils.toPrivateTag(creatorTag, tag);
        }
        return tag;
    }

    private int creatorTagOf(String privateCreator, int tag, boolean reserve) {
        if (!TagUtils.isPrivateGroup(tag)) {
            throw new IllegalArgumentException(TagUtils.toString(tag) + " is not a private Data Element");
        }
        int group = tag & 0xFFFF0000;
        int creatorTag = group | 0x10;
        int index = this.indexOf(creatorTag);
        if (index < 0) {
            index = -index - 1;
        }
        while (index < this.size && (this.tags[index] & 0xFFFFFF00) == group) {
            Object creatorID;
            creatorTag = this.tags[index];
            if (this.vrs[index].isStringType() && privateCreator.equals(creatorID = this.decodeStringValue(index))) {
                return creatorTag;
            }
            ++index;
            ++creatorTag;
        }
        if (!reserve) {
            return -1;
        }
        if ((creatorTag & 0xFF00) != 0) {
            throw new IllegalStateException("No free block for Private Element " + TagUtils.toString(tag));
        }
        this.setString(creatorTag, VR.LO, privateCreator);
        return creatorTag;
    }

    private Object decodeStringValue(int index) {
        Object value = this.loadBulkData(this.vrs[index], this.values[index]);
        return this.decodeStringValue(index, value);
    }

    private Object decodeStringValue(int index, Object value) {
        if (value instanceof byte[]) {
            if ((value = this.vrs[index].toStrings((byte[])value, this.bigEndian, this.getSpecificCharacterSet(this.vrs[index]))) instanceof String && ((String)value).isEmpty()) {
                value = Value.NULL;
            }
            this.values[index] = value;
        }
        return value;
    }

    private Object loadAndStoreBulkData(int index) {
        this.values[index] = this.loadBulkData(this.vrs[index], this.values[index]);
        return this.values[index];
    }

    private Object loadBulkData(VR vr, Object value) {
        try {
            return value instanceof BulkData ? (Object)((BulkData)value).toBytes(vr, this.bigEndian) : value;
        }
        catch (Exception e) {
            LOG.info("Failed to load {}", value);
            return Value.NULL;
        }
    }

    public SpecificCharacterSet getSpecificCharacterSet(VR vr) {
        return vr.useSpecificCharacterSet() ? this.getSpecificCharacterSet() : SpecificCharacterSet.ASCII;
    }

    private double[] decodeDSValue(int index) {
        double[] ds;
        Object value;
        Object object = value = index < 0 ? Value.NULL : this.values[index];
        if (value == Value.NULL) {
            return ByteUtils.EMPTY_DOUBLES;
        }
        if (value instanceof double[]) {
            return (double[])value;
        }
        if (value instanceof byte[]) {
            value = this.vrs[index].toStrings((byte[])value, this.bigEndian, SpecificCharacterSet.ASCII);
        }
        if (value instanceof String) {
            String s = (String)value;
            if (s.isEmpty()) {
                this.values[index] = Value.NULL;
                return ByteUtils.EMPTY_DOUBLES;
            }
            ds = new double[]{StringUtils.parseDS(s)};
        } else {
            String[] ss = (String[])value;
            ds = new double[ss.length];
            for (int i = 0; i < ds.length; ++i) {
                String s = ss[i];
                ds[i] = s != null && !s.isEmpty() ? StringUtils.parseDS(s) : Double.NaN;
            }
        }
        this.values[index] = ds;
        return ds;
    }

    private long[] decodeISValue(int index) {
        long[] ls;
        Object value;
        Object object = value = index < 0 ? Value.NULL : this.values[index];
        if (value == Value.NULL) {
            return ByteUtils.EMPTY_LONGS;
        }
        if (value instanceof long[]) {
            return (long[])value;
        }
        if (value instanceof byte[]) {
            value = this.vrs[index].toStrings((byte[])value, this.bigEndian, SpecificCharacterSet.ASCII);
        }
        if (value instanceof String) {
            String s = (String)value;
            if (s.isEmpty()) {
                this.values[index] = Value.NULL;
                return ByteUtils.EMPTY_LONGS;
            }
            ls = new long[]{StringUtils.parseIS(s)};
        } else {
            String[] ss = (String[])value;
            ls = new long[ss.length];
            for (int i = 0; i < ls.length; ++i) {
                String s = ss[i];
                ls[i] = s != null && !s.isEmpty() ? StringUtils.parseIS(s) : Long.MIN_VALUE;
            }
        }
        this.values[index] = ls;
        return ls;
    }

    private VR updateVR(int index, VR vr) {
        VR prev = this.vrs[index];
        if (vr == null || vr == prev) {
            return prev;
        }
        Object value = this.values[index];
        if (!(value == Value.NULL || value instanceof byte[] || vr.isStringType() && (value instanceof String || value instanceof String[]))) {
            throw new IllegalStateException("value instanceof " + value.getClass());
        }
        this.vrs[index] = vr;
        return this.vrs[index];
    }

    private static boolean isEmpty(Object value) {
        return value instanceof Value && ((Value)value).isEmpty();
    }

    public boolean contains(int tag) {
        return this.indexOf(tag) >= 0;
    }

    public boolean contains(String privateCreator, int tag) {
        return this.indexOf(privateCreator, tag) >= 0;
    }

    public boolean containsValue(int tag) {
        return this.containsValue(null, tag);
    }

    public boolean containsValue(String privateCreator, int tag) {
        int index = this.indexOf(privateCreator, tag);
        return index >= 0 && !Attributes.isEmpty(this.vrs[index].isStringType() ? this.decodeStringValue(index) : this.values[index]);
    }

    public boolean containsTagInRange(int firstTag, int lastTag) {
        int indexFirstTag = this.indexForInsertOf(firstTag);
        if (indexFirstTag >= 0) {
            return true;
        }
        int insertIndex = -indexFirstTag - 1;
        return insertIndex < this.size && this.tags[insertIndex] <= lastTag;
    }

    public String privateCreatorOf(int tag) {
        return TagUtils.isPrivateTag(tag) ? this.privateCreatorAt(this.indexOf(TagUtils.creatorTagOf(tag))) : null;
    }

    private String privateCreatorAt(int index) {
        Object value;
        return index < 0 || !this.vrs[index].isStringType() || (value = this.decodeStringValue(index)) == Value.NULL ? null : VR.LO.toString(value, false, 0, null);
    }

    public Object getValue(int tag) {
        return this.getValue(null, tag, null);
    }

    public Object getValue(int tag, VR.Holder vr) {
        return this.getValue(null, tag, vr);
    }

    public Object getValue(String privateCreator, int tag) {
        return this.getValue(privateCreator, tag, null);
    }

    public Object getValue(String privateCreator, int tag, VR.Holder vr) {
        int index = this.indexOf(privateCreator, tag);
        if (index < 0) {
            return null;
        }
        if (vr != null) {
            vr.vr = this.vrs[index];
        }
        return this.values[index];
    }

    public VR getVR(int tag) {
        return this.getVR(null, tag);
    }

    public VR getVR(String privateCreator, int tag) {
        int index = this.indexOf(privateCreator, tag);
        if (index < 0) {
            return null;
        }
        return this.vrs[index];
    }

    public Sequence getSequence(int tag) {
        return this.getSequence(null, tag);
    }

    public Sequence getSequence(String privateCreator, int tag) {
        int index;
        int sqtag = tag;
        if (privateCreator != null) {
            int creatorTag = this.creatorTagOf(privateCreator, tag, false);
            if (creatorTag == -1) {
                return null;
            }
            sqtag = TagUtils.toPrivateTag(creatorTag, tag);
        }
        if ((index = this.indexOf(sqtag)) < 0) {
            return null;
        }
        VR vr = this.vrs[index];
        if (vr != VR.SQ && vr != VR.UN) {
            return null;
        }
        Object value = this.values[index];
        if (value instanceof Sequence) {
            return (Sequence)value;
        }
        if (value == Value.NULL) {
            this.vrs[index] = VR.SQ;
            this.values[index] = new Sequence(this, privateCreator, tag, 0);
        } else {
            try {
                DicomInputStream.parseUNSequence((byte[])value, this, sqtag);
            }
            catch (IOException e) {
                return null;
            }
        }
        return (Sequence)this.values[index];
    }

    public byte[] getBytes(int tag) throws IOException {
        return this.getBytes(null, tag);
    }

    public byte[] getBytes(String privateCreator, int tag) throws IOException {
        int index = this.indexOf(privateCreator, tag);
        if (index < 0) {
            return null;
        }
        Object value = this.values[index];
        VR vr = this.vrs[index];
        try {
            if (value instanceof Value) {
                return ((Value)value).toBytes(vr, this.bigEndian);
            }
            return vr.toBytes(value, this.getSpecificCharacterSet(vr));
        }
        catch (UnsupportedOperationException e) {
            LOG.info("Attempt to access {} {} as bytes", (Object)TagUtils.toString(tag), (Object)vr);
            return null;
        }
    }

    public byte[] getSafeBytes(int tag) {
        return this.getSafeBytes(null, tag);
    }

    public byte[] getSafeBytes(String privateCreator, int tag) {
        try {
            return this.getBytes(privateCreator, tag);
        }
        catch (IOException e) {
            LOG.info("Access " + TagUtils.toString(tag) + " throws i/o exception", (Throwable)e);
            return null;
        }
    }

    public String getString(int tag) {
        return this.getString(null, tag, null, 0, null);
    }

    public String getString(int tag, String defVal) {
        return this.getString(null, tag, null, 0, defVal);
    }

    public String getString(int tag, int valueIndex) {
        return this.getString(null, tag, null, valueIndex, null);
    }

    public String getString(int tag, int valueIndex, String defVal) {
        return this.getString(null, tag, null, valueIndex, defVal);
    }

    public String getString(String privateCreator, int tag) {
        return this.getString(privateCreator, tag, null, 0, null);
    }

    public String getString(String privateCreator, int tag, String defVal) {
        return this.getString(privateCreator, tag, null, 0, defVal);
    }

    public String getString(String privateCreator, int tag, VR vr) {
        return this.getString(privateCreator, tag, vr, 0, null);
    }

    public String getString(String privateCreator, int tag, VR vr, String defVal) {
        return this.getString(privateCreator, tag, vr, 0, defVal);
    }

    public String getString(String privateCreator, int tag, int valueIndex) {
        return this.getString(privateCreator, tag, null, valueIndex, null);
    }

    public String getString(String privateCreator, int tag, int valueIndex, String defVal) {
        return this.getString(privateCreator, tag, null, valueIndex, defVal);
    }

    public String getString(String privateCreator, int tag, VR vr, int valueIndex) {
        return this.getString(privateCreator, tag, vr, valueIndex, null);
    }

    public String getString(String privateCreator, int tag, VR vr, int valueIndex, String defVal) {
        int index = this.indexOf(privateCreator, tag);
        if (index < 0) {
            return defVal;
        }
        Object value = this.values[index];
        if (value == Value.NULL) {
            return defVal;
        }
        vr = this.updateVR(index, vr);
        value = this.loadBulkData(vr, value);
        if (vr.isStringType()) {
            value = this.decodeStringValue(index, value);
        }
        if (value == Value.NULL) {
            return defVal;
        }
        try {
            return vr.toString(value, this.bigEndian, valueIndex, defVal);
        }
        catch (UnsupportedOperationException e) {
            LOG.info("Attempt to access {} {} as string", (Object)TagUtils.toString(tag), (Object)vr);
            return defVal;
        }
    }

    public String[] getStrings(int tag) {
        return this.getStrings(null, tag, null);
    }

    public String[] getStrings(String privateCreator, int tag) {
        return this.getStrings(privateCreator, tag, null);
    }

    public String[] getStrings(String privateCreator, int tag, VR vr) {
        int index = this.indexOf(privateCreator, tag);
        if (index < 0) {
            return null;
        }
        Object value = this.values[index];
        if (value == Value.NULL) {
            return StringUtils.EMPTY_STRING;
        }
        vr = this.updateVR(index, vr);
        value = this.loadBulkData(vr, value);
        if (vr.isStringType()) {
            value = this.decodeStringValue(index, value);
        }
        if (value == Value.NULL) {
            return StringUtils.EMPTY_STRING;
        }
        try {
            return Attributes.toStrings(vr.toStrings(value, this.bigEndian, this.getSpecificCharacterSet(vr)));
        }
        catch (UnsupportedOperationException e) {
            LOG.info("Attempt to access {} {} as string", (Object)TagUtils.toString(tag), (Object)vr);
            return null;
        }
    }

    private static String[] toStrings(Object val) {
        String[] stringArray;
        if (val instanceof String) {
            String[] stringArray2 = new String[1];
            stringArray = stringArray2;
            stringArray2[0] = (String)val;
        } else {
            stringArray = (String[])val;
        }
        return stringArray;
    }

    public int getInt(int tag, int defVal) {
        return this.getInt(null, tag, null, 0, defVal);
    }

    public int getInt(int tag, int valueIndex, int defVal) {
        return this.getInt(null, tag, null, valueIndex, defVal);
    }

    public int getInt(String privateCreator, int tag, int defVal) {
        return this.getInt(privateCreator, tag, null, 0, defVal);
    }

    public int getInt(String privateCreator, int tag, VR vr, int defVal) {
        return this.getInt(privateCreator, tag, vr, 0, defVal);
    }

    public int getInt(String privateCreator, int tag, int valueIndex, int defVal) {
        return this.getInt(privateCreator, tag, null, valueIndex, defVal);
    }

    public int getInt(String privateCreator, int tag, VR vr, int valueIndex, int defVal) {
        int index = this.indexOf(privateCreator, tag);
        if (index < 0) {
            return defVal;
        }
        Object value = this.values[index];
        if (value == Value.NULL) {
            return defVal;
        }
        vr = this.updateVR(index, vr);
        try {
            value = this.loadAndStoreBulkData(index);
            if (vr == VR.IS) {
                value = this.decodeISValue(index);
            }
            return vr.toInt(value, this.bigEndian, valueIndex, defVal);
        }
        catch (UnsupportedOperationException e) {
            LOG.info("Attempt to access {} {} as int", (Object)TagUtils.toString(tag), (Object)vr);
            return defVal;
        }
        catch (IllegalArgumentException e) {
            LOG.info("Invalid value of {} {}", (Object)TagUtils.toString(tag), (Object)vr);
            return defVal;
        }
    }

    public int[] getInts(int tag) {
        return this.getInts(null, tag, null);
    }

    public int[] getInts(String privateCreator, int tag) {
        return this.getInts(privateCreator, tag, null);
    }

    public int[] getInts(String privateCreator, int tag, VR vr) {
        int index = this.indexOf(privateCreator, tag);
        if (index < 0) {
            return null;
        }
        Object value = this.values[index];
        if (value == Value.NULL) {
            return ByteUtils.EMPTY_INTS;
        }
        vr = this.updateVR(index, vr);
        try {
            value = this.loadAndStoreBulkData(index);
            if (vr == VR.IS) {
                value = this.decodeISValue(index);
            }
            return vr.toInts(value, this.bigEndian);
        }
        catch (UnsupportedOperationException e) {
            LOG.info("Attempt to access {} {} as int", (Object)TagUtils.toString(tag), (Object)vr);
            return null;
        }
        catch (IllegalArgumentException e) {
            LOG.info("Invalid value of {} {}", (Object)TagUtils.toString(tag), (Object)vr);
            return null;
        }
    }

    public long getLong(int tag, long defVal) {
        return this.getLong(null, tag, null, 0, defVal);
    }

    public long getLong(int tag, int valueIndex, long defVal) {
        return this.getLong(null, tag, null, valueIndex, defVal);
    }

    public long getLong(String privateCreator, int tag, long defVal) {
        return this.getLong(privateCreator, tag, null, 0, defVal);
    }

    public long getLong(String privateCreator, int tag, VR vr, long defVal) {
        return this.getLong(privateCreator, tag, vr, 0, defVal);
    }

    public long getLong(String privateCreator, int tag, int valueIndex, long defVal) {
        return this.getLong(privateCreator, tag, null, valueIndex, defVal);
    }

    public long getLong(String privateCreator, int tag, VR vr, int valueIndex, long defVal) {
        int index = this.indexOf(privateCreator, tag);
        if (index < 0) {
            return defVal;
        }
        Object value = this.values[index];
        if (value == Value.NULL) {
            return defVal;
        }
        vr = this.updateVR(index, vr);
        try {
            value = this.loadAndStoreBulkData(index);
            if (vr == VR.IS) {
                value = this.decodeISValue(index);
            }
            return vr.toLong(value, this.bigEndian, valueIndex, defVal);
        }
        catch (UnsupportedOperationException e) {
            LOG.info("Attempt to access {} {} as int", (Object)TagUtils.toString(tag), (Object)vr);
            return defVal;
        }
        catch (IllegalArgumentException e) {
            LOG.info("Invalid value of {} {}", (Object)TagUtils.toString(tag), (Object)vr);
            return defVal;
        }
    }

    public long[] getLongs(int tag) {
        return this.getLongs(null, tag, null);
    }

    public long[] getLongs(String privateCreator, int tag) {
        return this.getLongs(privateCreator, tag, null);
    }

    public long[] getLongs(String privateCreator, int tag, VR vr) {
        int index = this.indexOf(privateCreator, tag);
        if (index < 0) {
            return null;
        }
        Object value = this.values[index];
        if (value == Value.NULL) {
            return ByteUtils.EMPTY_LONGS;
        }
        vr = this.updateVR(index, vr);
        try {
            value = this.loadAndStoreBulkData(index);
            if (vr == VR.IS) {
                value = this.decodeISValue(index);
            }
            return vr.toLongs(value, this.bigEndian);
        }
        catch (UnsupportedOperationException e) {
            LOG.info("Attempt to access {} {} as long", (Object)TagUtils.toString(tag), (Object)vr);
            return null;
        }
        catch (IllegalArgumentException e) {
            LOG.info("Invalid value of {} {}", (Object)TagUtils.toString(tag), (Object)vr);
            return null;
        }
    }

    public float getFloat(int tag, float defVal) {
        return this.getFloat(null, tag, null, 0, defVal);
    }

    public float getFloat(int tag, int valueIndex, float defVal) {
        return this.getFloat(null, tag, null, valueIndex, defVal);
    }

    public float getFloat(String privateCreator, int tag, float defVal) {
        return this.getFloat(privateCreator, tag, null, 0, defVal);
    }

    public float getFloat(String privateCreator, int tag, VR vr, float defVal) {
        return this.getFloat(privateCreator, tag, vr, 0, defVal);
    }

    public float getFloat(String privateCreator, int tag, int valueIndex, float defVal) {
        return this.getFloat(privateCreator, tag, null, valueIndex, defVal);
    }

    public float getFloat(String privateCreator, int tag, VR vr, int valueIndex, float defVal) {
        int index = this.indexOf(privateCreator, tag);
        if (index < 0) {
            return defVal;
        }
        Object value = this.values[index];
        if (value == Value.NULL) {
            return defVal;
        }
        vr = this.updateVR(index, vr);
        try {
            value = this.loadAndStoreBulkData(index);
            if (vr == VR.DS) {
                value = this.decodeDSValue(index);
            }
            return vr.toFloat(value, this.bigEndian, valueIndex, defVal);
        }
        catch (UnsupportedOperationException e) {
            LOG.info("Attempt to access {} {} as float", (Object)TagUtils.toString(tag), (Object)vr);
            return defVal;
        }
        catch (IllegalArgumentException e) {
            LOG.info("Invalid value of {} {}", (Object)TagUtils.toString(tag), (Object)vr);
            return defVal;
        }
    }

    public float[] getFloats(int tag) {
        return this.getFloats(null, tag, null);
    }

    public float[] getFloats(String privateCreator, int tag) {
        return this.getFloats(privateCreator, tag, null);
    }

    public float[] getFloats(String privateCreator, int tag, VR vr) {
        int index = this.indexOf(privateCreator, tag);
        if (index < 0) {
            return null;
        }
        Object value = this.values[index];
        if (value == Value.NULL) {
            return ByteUtils.EMPTY_FLOATS;
        }
        vr = this.updateVR(index, vr);
        try {
            value = this.loadAndStoreBulkData(index);
            if (vr == VR.DS) {
                value = this.decodeDSValue(index);
            }
            return vr.toFloats(value, this.bigEndian);
        }
        catch (UnsupportedOperationException e) {
            LOG.info("Attempt to access {} {} as float", (Object)TagUtils.toString(tag), (Object)vr);
            return null;
        }
        catch (IllegalArgumentException e) {
            LOG.info("Invalid value of {} {}", (Object)TagUtils.toString(tag), (Object)vr);
            return null;
        }
    }

    public double getDouble(int tag, double defVal) {
        return this.getDouble(null, tag, null, 0, defVal);
    }

    public double getDouble(int tag, int valueIndex, double defVal) {
        return this.getDouble(null, tag, null, valueIndex, defVal);
    }

    public double getDouble(String privateCreator, int tag, double defVal) {
        return this.getDouble(privateCreator, tag, null, 0, defVal);
    }

    public double getDouble(String privateCreator, int tag, VR vr, double defVal) {
        return this.getDouble(privateCreator, tag, vr, 0, defVal);
    }

    public double getDouble(String privateCreator, int tag, int valueIndex, double defVal) {
        return this.getDouble(privateCreator, tag, null, valueIndex, defVal);
    }

    public double getDouble(String privateCreator, int tag, VR vr, int valueIndex, double defVal) {
        int index = this.indexOf(privateCreator, tag);
        if (index < 0) {
            return defVal;
        }
        Object value = this.values[index];
        if (value == Value.NULL) {
            return defVal;
        }
        vr = this.updateVR(index, vr);
        try {
            value = this.loadAndStoreBulkData(index);
            if (vr == VR.DS) {
                value = this.decodeDSValue(index);
            }
            return vr.toDouble(value, this.bigEndian, valueIndex, defVal);
        }
        catch (UnsupportedOperationException e) {
            LOG.info("Attempt to access {} {} as double", (Object)TagUtils.toString(tag), (Object)vr);
            return defVal;
        }
        catch (IllegalArgumentException e) {
            LOG.info("Invalid value of {} {}", (Object)TagUtils.toString(tag), (Object)vr);
            return defVal;
        }
    }

    public double[] getDoubles(int tag) {
        return this.getDoubles(null, tag, null);
    }

    public double[] getDoubles(String privateCreator, int tag) {
        return this.getDoubles(privateCreator, tag, null);
    }

    public double[] getDoubles(String privateCreator, int tag, VR vr) {
        int index = this.indexOf(privateCreator, tag);
        if (index < 0) {
            return null;
        }
        Object value = this.values[index];
        if (value == Value.NULL) {
            return ByteUtils.EMPTY_DOUBLES;
        }
        vr = this.updateVR(index, vr);
        try {
            value = this.loadAndStoreBulkData(index);
            if (vr == VR.DS) {
                value = this.decodeDSValue(index);
            }
            return vr.toDoubles(value, this.bigEndian);
        }
        catch (UnsupportedOperationException e) {
            LOG.info("Attempt to access {} {} as double", (Object)TagUtils.toString(tag), (Object)vr);
            return null;
        }
        catch (IllegalArgumentException e) {
            LOG.info("Invalid value of {} {}", (Object)TagUtils.toString(tag), (Object)vr);
            return null;
        }
    }

    public Temporal getTemporal(int tag) {
        return this.getTemporal(null, tag, null, 0, null, new DatePrecision());
    }

    public Temporal getTemporal(String privateCreator, int tag, VR vr, int valueIndex, Temporal defVal, DatePrecision precision) {
        ZoneId zoneId;
        Temporal t;
        int index = this.indexOf(privateCreator, tag);
        if (index < 0) {
            return defVal;
        }
        Object value = this.values[index];
        if (value == Value.NULL) {
            return defVal;
        }
        if (!(vr = this.updateVR(index, vr)).isTemporalType()) {
            LOG.info("Attempt to access {} {} as date/time", (Object)TagUtils.toString(tag), (Object)vr);
            return defVal;
        }
        value = this.decodeStringValue(index);
        if (value == Value.NULL) {
            return defVal;
        }
        try {
            t = vr.toTemporal(value, valueIndex, precision);
        }
        catch (IllegalArgumentException e) {
            LOG.info("Invalid value of {} {}", (Object)TagUtils.toString(tag), (Object)vr);
            return defVal;
        }
        if (t == null) {
            return defVal;
        }
        if (t instanceof OffsetDateTime) {
            return ((OffsetDateTime)t).toZonedDateTime();
        }
        if (t instanceof LocalDateTime && (zoneId = this.getZoneId()) != null) {
            return ((LocalDateTime)t).atZone(zoneId);
        }
        return t;
    }

    public Date getDate(int tag) {
        return this.getDate(null, tag, null, 0, null, new DatePrecision());
    }

    public Date getDate(int tag, DatePrecision precision) {
        return this.getDate(null, tag, null, 0, null, precision);
    }

    public Date getDate(int tag, Date defVal) {
        return this.getDate(null, tag, null, 0, defVal, new DatePrecision());
    }

    public Date getDate(int tag, Date defVal, DatePrecision precision) {
        return this.getDate(null, tag, null, 0, defVal, precision);
    }

    public Date getDate(int tag, int valueIndex) {
        return this.getDate(null, tag, null, valueIndex, null, new DatePrecision());
    }

    public Date getDate(int tag, int valueIndex, DatePrecision precision) {
        return this.getDate(null, tag, null, valueIndex, null, precision);
    }

    public Date getDate(int tag, int valueIndex, Date defVal) {
        return this.getDate(null, tag, null, valueIndex, defVal, new DatePrecision());
    }

    public Date getDate(int tag, int valueIndex, Date defVal, DatePrecision precision) {
        return this.getDate(null, tag, null, valueIndex, defVal, precision);
    }

    public Date getDate(String privateCreator, int tag) {
        return this.getDate(privateCreator, tag, null, 0, null, new DatePrecision());
    }

    public Date getDate(String privateCreator, int tag, DatePrecision precision) {
        return this.getDate(privateCreator, tag, null, 0, null, precision);
    }

    public Date getDate(String privateCreator, int tag, Date defVal, DatePrecision precision) {
        return this.getDate(privateCreator, tag, null, 0, defVal, precision);
    }

    public Date getDate(String privateCreator, int tag, VR vr) {
        return this.getDate(privateCreator, tag, vr, 0, null, new DatePrecision());
    }

    public Date getDate(String privateCreator, int tag, VR vr, DatePrecision precision) {
        return this.getDate(privateCreator, tag, vr, 0, null, precision);
    }

    public Date getDate(String privateCreator, int tag, VR vr, Date defVal) {
        return this.getDate(privateCreator, tag, vr, 0, defVal, new DatePrecision());
    }

    public Date getDate(String privateCreator, int tag, VR vr, Date defVal, DatePrecision precision) {
        return this.getDate(privateCreator, tag, vr, 0, defVal, precision);
    }

    public Date getDate(String privateCreator, int tag, int valueIndex) {
        return this.getDate(privateCreator, tag, null, valueIndex, null, new DatePrecision());
    }

    public Date getDate(String privateCreator, int tag, int valueIndex, DatePrecision precision) {
        return this.getDate(privateCreator, tag, null, valueIndex, null, precision);
    }

    public Date getDate(String privateCreator, int tag, int valueIndex, Date defVal) {
        return this.getDate(privateCreator, tag, null, valueIndex, defVal, new DatePrecision());
    }

    public Date getDate(String privateCreator, int tag, int valueIndex, Date defVal, DatePrecision precision) {
        return this.getDate(privateCreator, tag, null, valueIndex, defVal, precision);
    }

    public Date getDate(String privateCreator, int tag, VR vr, int valueIndex) {
        return this.getDate(privateCreator, tag, vr, valueIndex, null, new DatePrecision());
    }

    public Date getDate(String privateCreator, int tag, VR vr, int valueIndex, DatePrecision precision) {
        return this.getDate(privateCreator, tag, vr, valueIndex, null, precision);
    }

    public Date getDate(String privateCreator, int tag, VR vr, int valueIndex, Date defVal) {
        return this.getDate(privateCreator, tag, vr, valueIndex, defVal, new DatePrecision());
    }

    public Date getDate(String privateCreator, int tag, VR vr, int valueIndex, Date defVal, DatePrecision precision) {
        int index = this.indexOf(privateCreator, tag);
        if (index < 0) {
            return defVal;
        }
        Object value = this.values[index];
        if (value == Value.NULL) {
            return defVal;
        }
        if (!(vr = this.updateVR(index, vr)).isTemporalType()) {
            LOG.info("Attempt to access {} {} as date", (Object)TagUtils.toString(tag), (Object)vr);
            return defVal;
        }
        value = this.decodeStringValue(index);
        if (value == Value.NULL) {
            return defVal;
        }
        try {
            return vr.toDate(value, this.getTimeZone(), valueIndex, false, defVal, precision);
        }
        catch (IllegalArgumentException e) {
            LOG.info("Invalid value of {} {}", (Object)TagUtils.toString(tag), (Object)vr);
            return defVal;
        }
    }

    public Date getDate(long tag) {
        return this.getDate(null, tag, null, new DatePrecision());
    }

    public Date getDate(long tag, DatePrecision precision) {
        return this.getDate(null, tag, null, precision);
    }

    public Date getDate(long tag, Date defVal) {
        return this.getDate(null, tag, defVal, new DatePrecision());
    }

    public Date getDate(long tag, Date defVal, DatePrecision precision) {
        return this.getDate(null, tag, defVal, precision);
    }

    public Date getDate(String privateCreator, long tag) {
        return this.getDate(privateCreator, tag, null, new DatePrecision());
    }

    public Date getDate(String privateCreator, long tag, DatePrecision precision) {
        return this.getDate(privateCreator, tag, null, precision);
    }

    public Date getDate(String privateCreator, long tag, Date defVal) {
        return this.getDate(privateCreator, tag, defVal, new DatePrecision());
    }

    public Date getDate(String privateCreator, long tag, Date defVal, DatePrecision precision) {
        int daTag = (int)(tag >>> 32);
        int tmTag = (int)tag;
        String tm = this.getString(privateCreator, tmTag, VR.TM, null);
        if (tm == null) {
            return this.getDate(daTag, defVal, precision);
        }
        String da = this.getString(privateCreator, daTag, VR.DA, null);
        if (da == null) {
            return defVal;
        }
        try {
            return VR.DT.toDate(da + tm, this.getTimeZone(), 0, false, null, precision);
        }
        catch (IllegalArgumentException e) {
            LOG.info("Invalid value of {} DA or {} TM", (Object)TagUtils.toString(daTag), (Object)TagUtils.toString(tmTag));
            return defVal;
        }
    }

    public Temporal getTemporal(long tag) {
        return this.getTemporal(null, tag, null, new DatePrecision());
    }

    public Temporal getTemporal(String privateCreator, long tag, Temporal defVal, DatePrecision precision) {
        int daTag = (int)(tag >>> 32);
        int tmTag = (int)tag;
        LocalDate date = (LocalDate)this.getTemporal(privateCreator, daTag, VR.DA, 0, null, precision);
        LocalTime time = (LocalTime)this.getTemporal(privateCreator, tmTag, VR.TM, 0, null, precision);
        if (date != null && time != null) {
            LocalDateTime localDateTime = LocalDateTime.of(date, time);
            ZoneId zoneId = this.getZoneId();
            if (zoneId != null) {
                return localDateTime.atZone(zoneId);
            }
            return localDateTime;
        }
        if (date != null) {
            return date;
        }
        if (time != null) {
            return time;
        }
        return defVal;
    }

    public Date[] getDates(int tag) {
        return this.getDates(null, tag, null, new DatePrecisions());
    }

    public Date[] getDates(int tag, DatePrecisions precisions) {
        return this.getDates(null, tag, null, precisions);
    }

    public Date[] getDates(String privateCreator, int tag) {
        return this.getDates(privateCreator, tag, null, new DatePrecisions());
    }

    public Date[] getDates(String privateCreator, int tag, DatePrecisions precisions) {
        return this.getDates(privateCreator, tag, null, precisions);
    }

    public Date[] getDates(String privateCreator, int tag, VR vr) {
        return this.getDates(privateCreator, tag, vr, new DatePrecisions());
    }

    public Date[] getDates(String privateCreator, int tag, VR vr, DatePrecisions precisions) {
        int index = this.indexOf(privateCreator, tag);
        if (index < 0) {
            return null;
        }
        Object value = this.values[index];
        if (value == Value.NULL) {
            return DateUtils.EMPTY_DATES;
        }
        if (!(vr = this.updateVR(index, vr)).isTemporalType()) {
            LOG.info("Attempt to access {} {} as date", (Object)TagUtils.toString(tag), (Object)vr);
            return DateUtils.EMPTY_DATES;
        }
        value = this.decodeStringValue(index);
        if (value == Value.NULL) {
            return DateUtils.EMPTY_DATES;
        }
        try {
            return vr.toDates(value, this.getTimeZone(), false, precisions);
        }
        catch (IllegalArgumentException e) {
            LOG.info("Invalid value of {} {}", (Object)TagUtils.toString(tag), (Object)vr);
            return DateUtils.EMPTY_DATES;
        }
    }

    public Date[] getDates(long tag) {
        return this.getDates(null, tag, new DatePrecisions());
    }

    public Date[] getDates(long tag, DatePrecisions precisions) {
        return this.getDates(null, tag, precisions);
    }

    public Date[] getDates(String privateCreator, long tag) {
        return this.getDates(privateCreator, tag, new DatePrecisions());
    }

    public Date[] getDates(String privateCreator, long tag, DatePrecisions precisions) {
        int daTag = (int)(tag >>> 32);
        int tmTag = (int)tag;
        String[] tm = this.getStrings(privateCreator, tmTag);
        if (tm == null || tm.length == 0) {
            return this.getDates(daTag, precisions);
        }
        String[] da = this.getStrings(privateCreator, daTag);
        if (da == null || da.length == 0) {
            return DateUtils.EMPTY_DATES;
        }
        Date[] dates = new Date[da.length];
        precisions.precisions = new DatePrecision[da.length];
        int i = 0;
        TimeZone tz = this.getTimeZone();
        try {
            while (i < tm.length) {
                int n = i++;
                precisions.precisions[i] = new DatePrecision();
                dates[n] = VR.DT.toDate(da[i] + tm[i], tz, 0, false, null, precisions.precisions[i]);
            }
            while (i < da.length) {
                int n = i++;
                precisions.precisions[i] = new DatePrecision();
                dates[n] = VR.DA.toDate(da[i], tz, 0, false, null, precisions.precisions[i]);
            }
        }
        catch (IllegalArgumentException e) {
            LOG.info("Invalid value of {} DA or {} TM", (Object)TagUtils.toString(daTag), (Object)TagUtils.toString(tmTag));
            dates = Arrays.copyOf(dates, i);
        }
        return dates;
    }

    public DateRange getDateRange(int tag) {
        return this.getDateRange(null, tag, null, null);
    }

    public DateRange getDateRange(int tag, DateRange defVal) {
        return this.getDateRange(null, tag, null, defVal);
    }

    public DateRange getDateRange(String privateCreator, int tag) {
        return this.getDateRange(privateCreator, tag, null, null);
    }

    public DateRange getDateRange(String privateCreator, int tag, DateRange defVal) {
        return this.getDateRange(privateCreator, tag, null, defVal);
    }

    public DateRange getDateRange(String privateCreator, int tag, VR vr) {
        return this.getDateRange(privateCreator, tag, vr, null);
    }

    public DateRange getDateRange(String privateCreator, int tag, VR vr, DateRange defVal) {
        int index = this.indexOf(privateCreator, tag);
        if (index < 0) {
            return defVal;
        }
        Object value = this.values[index];
        if (value == Value.NULL) {
            return defVal;
        }
        if (!(vr = this.updateVR(index, vr)).isTemporalType()) {
            LOG.info("Attempt to access {} {} as date", (Object)TagUtils.toString(tag), (Object)vr);
            return defVal;
        }
        value = this.decodeStringValue(index);
        if (value == Value.NULL) {
            return defVal;
        }
        try {
            return this.toDateRange(value instanceof String ? (String)value : ((String[])value)[0], vr);
        }
        catch (IllegalArgumentException e) {
            LOG.info("Invalid value of {} {}", (Object)TagUtils.toString(tag), (Object)vr);
            return defVal;
        }
    }

    private DateRange toDateRange(String s, VR vr) {
        String[] range = Attributes.splitRange(s);
        TimeZone tz = this.getTimeZone();
        DatePrecision precision = new DatePrecision();
        Date start = range[0] == null ? null : vr.toDate(range[0], tz, 0, false, null, precision);
        Date end = range[1] == null ? null : vr.toDate(range[1], tz, 0, true, null, precision);
        return new DateRange(start, end);
    }

    private static String[] splitRange(String s) {
        String[] range = new String[2];
        int delim = s.indexOf(45);
        if (delim == -1) {
            range[0] = range[1] = s;
        } else {
            if (delim > 0) {
                range[0] = s.substring(0, delim);
            }
            if (delim < s.length() - 1) {
                range[1] = s.substring(delim + 1);
            }
        }
        return range;
    }

    public DateRange getDateRange(long tag) {
        return this.getDateRange(null, tag, null);
    }

    public DateRange getDateRange(long tag, DateRange defVal) {
        return this.getDateRange(null, tag, defVal);
    }

    public DateRange getDateRange(String privateCreator, long tag) {
        return this.getDateRange(privateCreator, tag, null);
    }

    public DateRange getDateRange(String privateCreator, long tag, DateRange defVal) {
        int daTag = (int)(tag >>> 32);
        int tmTag = (int)tag;
        String tm = this.getString(privateCreator, tmTag, VR.TM, null);
        if (tm == null) {
            return this.getDateRange(daTag, defVal);
        }
        String da = this.getString(privateCreator, daTag, VR.DA, null);
        if (da == null) {
            return defVal;
        }
        try {
            return this.toDateRange(da, tm);
        }
        catch (IllegalArgumentException e) {
            LOG.info("Invalid value of {} TM", (Object)TagUtils.toString((int)tag));
            return defVal;
        }
    }

    private DateRange toDateRange(String da, String tm) {
        String[] darange = Attributes.splitRange(da);
        String[] tmrange = Attributes.splitRange(tm);
        TimeZone tz = this.getTimeZone();
        DatePrecision precision = new DatePrecision();
        return new DateRange(darange[0] == null ? null : VR.DT.toDate(tmrange[0] == null ? darange[0] : darange[0] + tmrange[0], tz, 0, false, null, precision), darange[1] == null ? null : VR.DT.toDate(tmrange[1] == null ? darange[1] : darange[1] + tmrange[1], tz, 0, true, null, precision));
    }

    public void setSpecificCharacterSet(String ... codes) {
        this.ensureModifiable();
        this.decodeStringValuesUsingSpecificCharacterSet();
        this.setString(524293, VR.CS, codes);
    }

    public SpecificCharacterSet getSpecificCharacterSet() {
        if (this.cs != null) {
            return this.cs;
        }
        if (this.containsSpecificCharacterSet) {
            this.cs = SpecificCharacterSet.valueOf(this.getStrings(null, 524293, VR.CS));
        } else {
            if (this.parent != null) {
                return this.parent.getSpecificCharacterSet();
            }
            this.cs = SpecificCharacterSet.getDefaultCharacterSet();
        }
        return this.cs;
    }

    public boolean containsTimezoneOffsetFromUTC() {
        return this.containsTimezoneOffsetFromUTC;
    }

    public void setDefaultTimeZone(TimeZone tz) {
        this.ensureModifiable();
        this.defaultTimeZone = tz;
    }

    public ZoneId getDefaultZoneId() {
        if (this.defaultTimeZone != null) {
            return this.defaultTimeZone.toZoneId();
        }
        if (this.parent != null) {
            return this.parent.getDefaultZoneId();
        }
        return null;
    }

    public TimeZone getDefaultTimeZone() {
        if (this.defaultTimeZone != null) {
            return this.defaultTimeZone;
        }
        if (this.parent != null) {
            return this.parent.getDefaultTimeZone();
        }
        return TimeZone.getDefault();
    }

    public TimeZone getTimeZone() {
        if (this.tz != null) {
            return this.tz;
        }
        if (this.containsTimezoneOffsetFromUTC) {
            String s = this.getString(524801);
            if (s != null) {
                try {
                    this.tz = DateUtils.timeZone(s);
                }
                catch (IllegalArgumentException e) {
                    LOG.info(e.getMessage());
                }
            }
        } else {
            if (this.parent != null) {
                return this.parent.getTimeZone();
            }
            this.tz = this.getDefaultTimeZone();
        }
        return this.tz;
    }

    public ZoneId getZoneId() {
        if (this.tz != null) {
            return this.tz.toZoneId();
        }
        if (this.containsTimezoneOffsetFromUTC) {
            String s = this.getString(524801);
            if (s == null) {
                return null;
            }
            try {
                this.tz = DateUtils.timeZone(s);
            }
            catch (IllegalArgumentException e) {
                LOG.info(e.getMessage());
                return null;
            }
            return this.tz.toZoneId();
        }
        if (this.parent != null) {
            return this.parent.getZoneId();
        }
        return this.getDefaultZoneId();
    }

    public void setTimezoneOffsetFromUTC(String utcOffset) {
        this.ensureModifiable();
        TimeZone tz = DateUtils.timeZone(utcOffset);
        this.updateTimezone(this.getTimeZone(), tz);
        this.setString(524801, VR.SH, utcOffset);
        this.tz = tz;
    }

    public void setTimezone(TimeZone tz) {
        this.ensureModifiable();
        this.updateTimezone(this.getTimeZone(), tz);
        if (tz.useDaylightTime()) {
            this.remove(524801);
            this.setDefaultTimeZone(tz);
        } else {
            this.setString(524801, VR.SH, DateUtils.formatTimezoneOffsetFromUTC(tz));
        }
        this.tz = tz;
    }

    private void updateTimezone(TimeZone from, TimeZone to) {
        if (from.hasSameRules(to)) {
            return;
        }
        for (int i = 0; i < this.size; ++i) {
            Object val = this.values[i];
            if (val instanceof Sequence) {
                Sequence new_name = (Sequence)val;
                for (Attributes item : new_name) {
                    item.updateTimezone(item.getTimeZone(), to);
                    item.remove(524801);
                }
                continue;
            }
            if ((this.vrs[i] != VR.TM || this.tags[i] == 1048626) && (this.vrs[i] != VR.DT || this.tags[i] == 524550 || this.tags[i] == 524551)) continue;
            this.updateTimezone(from, to, i);
        }
    }

    private void updateTimezone(TimeZone from, TimeZone to, int tmIndex) {
        Object tm = this.decodeStringValue(tmIndex);
        if (tm == Value.NULL) {
            return;
        }
        int tmTag = this.tags[tmIndex];
        if (this.vrs[tmIndex] == VR.DT) {
            if (tm instanceof String[]) {
                String[] tms = (String[])tm;
                for (int i = 0; i < tms.length; ++i) {
                    tms[i] = this.updateTimeZoneDT(from, to, tms[i]);
                }
            } else {
                this.values[tmIndex] = this.updateTimeZoneDT(from, to, (String)tm);
            }
        } else {
            Value da;
            int daTag = ElementDictionary.getElementDictionary(this.privateCreatorOf(tmTag)).daTagOf(tmTag);
            int daIndex = daTag != 0 ? this.indexOf(daTag) : -1;
            Value value = da = daIndex >= 0 ? this.decodeStringValue(daIndex) : Value.NULL;
            if (tm instanceof String[]) {
                String[] tms = (String[])tm;
                if (da instanceof String[]) {
                    String[] das = (String[])da;
                    for (int i = 0; i < tms.length; ++i) {
                        if (i < das.length) {
                            String dt = this.updateTimeZoneDT(from, to, das[i] + tms[i]);
                            das[i] = dt.substring(0, 8);
                            tms[i] = dt.substring(8);
                            continue;
                        }
                        tms[i] = this.updateTimeZoneTM(from, to, tms[i]);
                    }
                } else {
                    if (da == Value.NULL) {
                        tms[0] = this.updateTimeZoneTM(from, to, tms[0]);
                    } else {
                        String dt = this.updateTimeZoneDT(from, to, (String)((Object)da) + tms[0]);
                        this.values[daIndex] = dt.substring(0, 8);
                        tms[0] = dt.substring(8);
                    }
                    for (int i = 1; i < tms.length; ++i) {
                        tms[i] = this.updateTimeZoneTM(from, to, tms[i]);
                    }
                }
            } else if (da instanceof String[]) {
                String[] das = (String[])da;
                String dt = this.updateTimeZoneDT(from, to, das[0] + (String)tm);
                das[0] = dt.substring(0, 8);
                this.values[tmIndex] = dt.substring(8);
            } else {
                String[] tmRange = null;
                if (Attributes.isRange((String)tm)) {
                    tmRange = Attributes.splitRange((String)tm);
                    if (tmRange[0] == null) {
                        tmRange[0] = "000000.000";
                    }
                    if (tmRange[1] == null) {
                        tmRange[1] = "235959.999";
                    }
                }
                if (da == Value.NULL) {
                    if (tmRange != null) {
                        tmRange[0] = this.updateTimeZoneTM(from, to, tmRange[0]);
                        tmRange[1] = this.updateTimeZoneTM(from, to, tmRange[1]);
                        this.values[tmIndex] = Attributes.toDateRangeString(tmRange[0], tmRange[1]);
                    } else {
                        this.values[tmIndex] = this.updateTimeZoneTM(from, to, (String)tm);
                    }
                } else if (tmRange != null) {
                    String dt;
                    String[] daRange = Attributes.splitRange((String)((Object)da));
                    if (daRange[0] == null) {
                        daRange[0] = "";
                        tmRange[0] = this.updateTimeZoneTM(from, to, tmRange[0]);
                    } else {
                        dt = this.updateTimeZoneDT(from, to, daRange[0] + tmRange[0]);
                        daRange[0] = dt.substring(0, 8);
                        tmRange[0] = dt.substring(8);
                    }
                    if (daRange[1] == null) {
                        daRange[1] = "";
                        tmRange[1] = this.updateTimeZoneTM(from, to, tmRange[1]);
                    } else {
                        dt = this.updateTimeZoneDT(from, to, daRange[1] + tmRange[1]);
                        daRange[1] = dt.substring(0, 8);
                        tmRange[1] = dt.substring(8);
                    }
                    this.values[daIndex] = Attributes.toDateRangeString(daRange[0], daRange[1]);
                    this.values[tmIndex] = Attributes.toDateRangeString(tmRange[0], tmRange[1]);
                } else {
                    String dt = this.updateTimeZoneDT(from, to, (String)((Object)da) + (String)tm);
                    this.values[daIndex] = dt.substring(0, 8);
                    this.values[tmIndex] = dt.substring(8);
                }
            }
        }
    }

    private static boolean isRange(String s) {
        return s.indexOf(45) >= 0;
    }

    private String updateTimeZoneDT(TimeZone from, TimeZone to, String dt) {
        char ch;
        int dtlen = dt.length();
        if (dtlen > 8 && ((ch = dt.charAt(dtlen - 5)) == '+' || ch == '-')) {
            return dt;
        }
        try {
            DatePrecision precision = new DatePrecision();
            Date date = DateUtils.parseDT(from, dt, false, precision);
            dt = DateUtils.formatDT(to, date, precision);
        }
        catch (IllegalArgumentException illegalArgumentException) {
            // empty catch block
        }
        return dt;
    }

    private String updateTimeZoneTM(TimeZone from, TimeZone to, String tm) {
        try {
            DatePrecision precision = new DatePrecision();
            Date date = DateUtils.parseTM(from, tm, false, precision);
            tm = DateUtils.formatTM(to, date, precision);
        }
        catch (IllegalArgumentException illegalArgumentException) {
            // empty catch block
        }
        return tm;
    }

    public String getPrivateCreator(int tag) {
        return TagUtils.isPrivateTag(tag) ? this.getString(TagUtils.creatorTagOf(tag), null) : null;
    }

    public Object remove(int tag) {
        return this.remove(null, tag);
    }

    public Object remove(String privateCreator, int tag) {
        int numMoved;
        this.ensureModifiable();
        int index = this.indexOf(privateCreator, tag);
        if (index < 0) {
            return null;
        }
        Object value = this.values[index];
        if (value instanceof Sequence) {
            for (Attributes attrs : (Sequence)value) {
                attrs.setParent(null, null, 0);
            }
        }
        if ((numMoved = this.size - index - 1) > 0) {
            System.arraycopy(this.tags, index + 1, this.tags, index, numMoved);
            System.arraycopy(this.vrs, index + 1, this.vrs, index, numMoved);
            System.arraycopy(this.values, index + 1, this.values, index, numMoved);
        }
        this.values[--this.size] = null;
        if (tag == 524293) {
            this.containsSpecificCharacterSet = false;
            this.cs = null;
        } else if (tag == 524801) {
            this.containsTimezoneOffsetFromUTC = false;
            this.tz = null;
        }
        return value;
    }

    public Object setNull(int tag, VR vr) {
        return this.setNull(null, tag, vr);
    }

    public Object setNull(String privateCreator, int tag, VR vr) {
        this.ensureModifiable();
        return this.set(privateCreator, tag, vr, Value.NULL);
    }

    public Object setBytes(int tag, VR vr, byte[] b) {
        return this.setBytes(null, tag, vr, b);
    }

    public Object setBytes(String privateCreator, int tag, VR vr, byte[] b) {
        this.ensureModifiable();
        return this.set(privateCreator, tag, vr, vr.toValue(b));
    }

    public Object setString(int tag, VR vr, String s) {
        return this.setString((String)null, tag, vr, s);
    }

    public Object setString(String privateCreator, int tag, VR vr, String s) {
        this.ensureModifiable();
        return this.set(privateCreator, tag, vr, vr.toValue(s, this.bigEndian));
    }

    public Object setString(int tag, VR vr, String ... ss) {
        return this.setString(null, tag, vr, ss);
    }

    public Object setString(String privateCreator, int tag, VR vr, String ... ss) {
        this.ensureModifiable();
        return this.set(privateCreator, tag, vr, vr.toValue(ss, this.bigEndian));
    }

    public Object setInt(int tag, VR vr, int ... is) {
        return this.setInt(null, tag, vr, is);
    }

    public Object setInt(String privateCreator, int tag, VR vr, int ... is) {
        this.ensureModifiable();
        return this.set(privateCreator, tag, vr, vr.toValue(is, this.bigEndian));
    }

    public Object setLong(int tag, VR vr, long ... ls) {
        return this.setLong(null, tag, vr, ls);
    }

    public Object setLong(String privateCreator, int tag, VR vr, long ... ls) {
        this.ensureModifiable();
        return this.set(privateCreator, tag, vr, vr.toValue(ls, this.bigEndian));
    }

    public Object setFloat(int tag, VR vr, float ... fs) {
        return this.setFloat(null, tag, vr, fs);
    }

    public Object setFloat(String privateCreator, int tag, VR vr, float ... fs) {
        this.ensureModifiable();
        return this.set(privateCreator, tag, vr, vr.toValue(fs, this.bigEndian));
    }

    public Object setDouble(int tag, VR vr, double ... ds) {
        return this.setDouble(null, tag, vr, ds);
    }

    public Object setDouble(String privateCreator, int tag, VR vr, double ... ds) {
        this.ensureModifiable();
        return this.set(privateCreator, tag, vr, vr.toValue(ds, this.bigEndian));
    }

    public Object setDate(int tag, VR vr, Date ... ds) {
        return this.setDate(null, tag, vr, ds);
    }

    public Object setDate(int tag, VR vr, DatePrecision precision, Date ... ds) {
        return this.setDate(null, tag, vr, precision, ds);
    }

    public Object setDate(String privateCreator, int tag, VR vr, Date ... ds) {
        return this.setDate(privateCreator, tag, vr, new DatePrecision(), ds);
    }

    public Object setDate(String privateCreator, int tag, VR vr, DatePrecision precision, Date ... ds) {
        return this.setDate(privateCreator, tag, vr, vr == VR.DT, precision, ds);
    }

    public Object setDate(int tag, VR vr, boolean applyTimezoneOffset, Date ... ds) {
        return this.setDate(null, tag, vr, applyTimezoneOffset, ds);
    }

    public Object setDate(int tag, VR vr, boolean applyTimezoneOffset, DatePrecision precision, Date ... ds) {
        return this.setDate(null, tag, vr, applyTimezoneOffset, precision, ds);
    }

    public Object setDate(String privateCreator, int tag, VR vr, boolean applyTimezoneOffset, Date ... ds) {
        return this.setDate(privateCreator, tag, vr, applyTimezoneOffset, new DatePrecision(), ds);
    }

    public Object setDate(String privateCreator, int tag, VR vr, boolean applyTimezoneOffset, DatePrecision precision, Date ... ds) {
        this.ensureModifiable();
        return this.set(privateCreator, tag, vr, vr.toValue(ds, applyTimezoneOffset ? this.getTimeZone() : null, precision));
    }

    public void setDate(long tag, Date ... dt) {
        this.setDate(null, tag, dt);
    }

    public void setDate(long tag, DatePrecision precision, Date ... dt) {
        this.setDate(null, tag, precision, dt);
    }

    public void setDate(String privateCreator, long tag, Date ... dt) {
        this.setDate(privateCreator, tag, new DatePrecision(), dt);
    }

    public void setDate(String privateCreator, long tag, DatePrecision precision, Date ... dt) {
        int daTag = (int)(tag >>> 32);
        int tmTag = (int)tag;
        this.setDate(privateCreator, daTag, VR.DA, true, precision, dt);
        this.setDate(privateCreator, tmTag, VR.TM, true, precision, dt);
    }

    public Object setDateRange(int tag, VR vr, DateRange range) {
        return this.setDateRange(null, tag, vr, range);
    }

    public Object setDateRange(int tag, VR vr, DatePrecision precision, DateRange range) {
        return this.setDateRange(null, tag, vr, precision, range);
    }

    public Object setDateRange(String privateCreator, int tag, VR vr, DateRange range) {
        return this.setDateRange(privateCreator, tag, vr, new DatePrecision(), range);
    }

    public Object setDateRange(String privateCreator, int tag, VR vr, DatePrecision precision, DateRange range) {
        this.ensureModifiable();
        return this.set(privateCreator, tag, vr, Attributes.toString(range, vr, this.getTimeZone(), precision));
    }

    private static String toString(DateRange range, VR vr, TimeZone tz, DatePrecision precision) {
        String start = range.getStartDate() != null ? (String)vr.toValue(new Date[]{range.getStartDate()}, tz, precision) : "";
        String end = range.getEndDate() != null ? (String)vr.toValue(new Date[]{range.getEndDate()}, tz, precision) : "";
        return Attributes.toDateRangeString(start, end);
    }

    private static String toDateRangeString(String start, String end) {
        return start.equals(end) ? start : start + '-' + end;
    }

    public void setDateRange(long tag, DateRange dr) {
        this.setDateRange(null, tag, dr);
    }

    public void setDateRange(String privateCreator, long tag, DateRange range) {
        int daTag = (int)(tag >>> 32);
        int tmTag = (int)tag;
        this.setDateRange(privateCreator, daTag, VR.DA, range);
        this.setDateRange(privateCreator, tmTag, VR.TM, range);
    }

    public Object setValue(int tag, VR vr, Object value) {
        return this.setValue(null, tag, vr, value);
    }

    public Object setValue(String privateCreator, int tag, VR vr, Object value) {
        this.ensureModifiable();
        return this.set(privateCreator, tag, vr, value != null ? value : Value.NULL);
    }

    public Sequence newSequence(int tag, int initialCapacity) {
        return this.newSequence(null, tag, initialCapacity);
    }

    public Sequence newSequence(String privateCreator, int tag, int initialCapacity) {
        this.ensureModifiable();
        Sequence seq = new Sequence(this, privateCreator, tag, initialCapacity);
        this.set(privateCreator, tag, VR.SQ, seq);
        return seq;
    }

    public Sequence ensureSequence(int tag, int initialCapacity) {
        return this.ensureSequence(null, tag, initialCapacity);
    }

    public Sequence ensureSequence(String privateCreator, int tag, int initialCapacity) {
        Sequence seq;
        int index;
        this.ensureModifiable();
        if (privateCreator != null) {
            int creatorTag = this.creatorTagOf(privateCreator, tag, true);
            tag = TagUtils.toPrivateTag(creatorTag, tag);
        }
        if ((index = this.indexOf(tag)) >= 0) {
            Object oldValue = this.values[index];
            if (oldValue instanceof Sequence) {
                seq = (Sequence)oldValue;
            } else {
                seq = new Sequence(this, null, 0, initialCapacity);
                this.values[index] = seq;
            }
        } else {
            seq = new Sequence(this, null, 0, initialCapacity);
            this.insert(-index - 1, tag, VR.SQ, seq);
        }
        return seq;
    }

    public Fragments newFragments(int tag, VR vr, int initialCapacity) {
        return this.newFragments(null, tag, vr, initialCapacity);
    }

    public Fragments newFragments(String privateCreator, int tag, VR vr, int initialCapacity) {
        this.ensureModifiable();
        Fragments frags = new Fragments(vr, this.bigEndian, initialCapacity);
        this.set(privateCreator, tag, vr, frags);
        return frags;
    }

    private Object set(String privateCreator, int tag, VR vr, Object value) {
        if (vr == null) {
            throw new NullPointerException("vr");
        }
        if (privateCreator != null) {
            int creatorTag = this.creatorTagOf(privateCreator, tag, true);
            tag = TagUtils.toPrivateTag(creatorTag, tag);
        }
        if (TagUtils.isGroupLength(tag)) {
            return null;
        }
        Object oldValue = this.set(tag, vr, value);
        if (tag == 524293) {
            this.containsSpecificCharacterSet = true;
            this.cs = null;
        } else if (tag == 524801) {
            this.containsTimezoneOffsetFromUTC = value != Value.NULL;
            this.tz = null;
        }
        return oldValue;
    }

    private Object set(int tag, VR vr, Object value) {
        int index = this.indexForInsertOf(tag);
        if (index >= 0) {
            Object oldValue = this.values[index];
            this.vrs[index] = vr;
            this.values[index] = value;
            return oldValue;
        }
        this.insert(-index - 1, tag, vr, value);
        return null;
    }

    private void insert(int index, int tag, VR vr, Object value) {
        this.ensureCapacity(this.size + 1);
        int numMoved = this.size - index;
        if (numMoved > 0) {
            System.arraycopy(this.tags, index, this.tags, index + 1, numMoved);
            System.arraycopy(this.vrs, index, this.vrs, index + 1, numMoved);
            System.arraycopy(this.values, index, this.values, index + 1, numMoved);
        }
        this.tags[index] = tag;
        this.vrs[index] = vr;
        this.values[index] = value;
        ++this.size;
    }

    public boolean addAll(Attributes other) {
        this.ensureModifiable();
        return this.add(other, null, null, 0, 0, null, null, false, false, null);
    }

    public boolean addAll(Attributes other, boolean mergeOriginalAttributesSequence) {
        this.ensureModifiable();
        return this.add(other, null, null, 0, 0, null, null, mergeOriginalAttributesSequence, false, null);
    }

    public boolean addSelected(Attributes other, Attributes selection) {
        this.ensureModifiable();
        return this.add(other, null, null, 0, 0, selection, null, false, false, null);
    }

    public boolean addSelected(Attributes other, String privateCreator, int tag) {
        this.ensureModifiable();
        int index = other.indexOf(privateCreator, tag);
        if (index < 0) {
            return false;
        }
        VR vr = other.vrs[index];
        Object value = other.values[index];
        if (!this.getSpecificCharacterSet().contains(other.getSpecificCharacterSet()) && Attributes.containsNonASCIIStringValues(value, vr)) {
            if (!this.getSpecificCharacterSet().isUTF8()) {
                throw new IncompatibleSpecificCharacterSetException("Specific Character Sets " + Arrays.toString(this.getSpecificCharacterSet().toCodes()) + " and " + Arrays.toString(other.getSpecificCharacterSet().toCodes()) + " not compatible");
            }
            if (vr.useSpecificCharacterSet() && (value = other.loadBulkData(vr, value)) instanceof byte[]) {
                value = vr.toStrings(value, other.bigEndian(), other.getSpecificCharacterSet());
            }
        }
        if (value instanceof Sequence) {
            this.set(privateCreator, tag, (Sequence)value, null);
        } else if (value instanceof Fragments) {
            this.set(privateCreator, tag, (Fragments)value);
        } else {
            this.set(privateCreator, tag, vr, Attributes.toggleEndian(vr, value, this.bigEndian != other.bigEndian));
        }
        return true;
    }

    private static boolean containsNonASCIIStringValues(Object val, VR vr) {
        if (val instanceof Sequence) {
            for (Attributes item : (Sequence)val) {
                if (!item.containsNonASCIIStringValues(null, null, 0, 0, null)) continue;
                return true;
            }
        } else if (val != Value.NULL && vr.useSpecificCharacterSet()) {
            return true;
        }
        return false;
    }

    public boolean addSelected(Attributes other, int ... selection) {
        return this.addSelected(other, selection, 0, selection.length);
    }

    public boolean addSelected(Attributes other, int[] selection, int fromIndex, int toIndex) {
        this.ensureModifiable();
        return this.add(other, selection, null, fromIndex, toIndex, null, null, false, false, null);
    }

    public boolean addNotSelected(Attributes other, int ... selection) {
        return this.addNotSelected(other, selection, 0, selection.length);
    }

    public boolean addNotSelected(Attributes other, int[] selection, int fromIndex, int toIndex) {
        this.ensureModifiable();
        return this.add(other, null, selection, fromIndex, toIndex, null, null, false, false, null);
    }

    public void supplementEmpty(Attributes selection) {
        this.ensureModifiable();
        int[] otherTags = selection.tags;
        VR[] otherVRs = selection.vrs;
        Object[] otherValues = selection.values;
        for (int i = 0; i < selection.size; ++i) {
            Attributes otherItem;
            Sequence otherSeq;
            int index = this.indexOf(otherTags[i]);
            if (index < 0) {
                this.insert(-index - 1, otherTags[i], otherVRs[i], Value.NULL);
                continue;
            }
            if (!(otherValues[i] instanceof Sequence) || !(this.values[index] instanceof Sequence) || (otherSeq = (Sequence)otherValues[i]).isEmpty() || (otherItem = (Attributes)otherSeq.get(0)).isEmpty()) continue;
            for (Attributes item : (Sequence)this.values[index]) {
                item.supplementEmpty(otherItem);
            }
        }
    }

    private boolean add(Attributes other, int[] include, int[] exclude, int fromIndex, int toIndex, Attributes selection, UpdatePolicy updatePolicy, boolean mergeOriginalAttributesSequence, boolean simulate, Attributes modified) {
        boolean updateSpecificCharacterSet;
        if (updatePolicy == UpdatePolicy.REPLACE) {
            throw new IllegalArgumentException("updatePolicy:" + (Object)((Object)updatePolicy));
        }
        boolean decodeStringValue = false;
        if (updatePolicy != UpdatePolicy.PRESERVE && !this.isEmpty() && !(!(updateSpecificCharacterSet = this.isUpdateSpecificCharacterSet(other, include, exclude, fromIndex, toIndex, selection, updatePolicy)) ? this.getSpecificCharacterSet().contains(other.getSpecificCharacterSet()) || !other.containsNonASCIIStringValues(include, exclude, fromIndex, toIndex, selection) : other.getSpecificCharacterSet().contains(this.getSpecificCharacterSet()) || !this.containsNonASCIIStringValues(null, null, 0, 0, null))) {
            if (!(updateSpecificCharacterSet ? other.getSpecificCharacterSet() : this.getSpecificCharacterSet()).isUTF8()) {
                throw new IncompatibleSpecificCharacterSetException("Specific Character Sets " + Arrays.toString(this.getSpecificCharacterSet().toCodes()) + " and " + Arrays.toString(other.getSpecificCharacterSet().toCodes()) + " not compatible");
            }
            if (updateSpecificCharacterSet) {
                this.decodeStringValuesUsingSpecificCharacterSet();
            } else {
                decodeStringValue = true;
            }
        }
        boolean toggleEndian = this.bigEndian != other.bigEndian;
        boolean modifiedToggleEndian = modified != null && this.bigEndian != modified.bigEndian;
        int[] otherTags = other.tags;
        VR[] srcVRs = other.vrs;
        Object[] srcValues = other.values;
        int otherSize = other.size;
        int numAdd = 0;
        String privateCreator = null;
        String privateCreator0 = null;
        int creatorTag = 0;
        for (int i = 0; i < otherSize; ++i) {
            int tag = otherTags[i];
            VR vr = srcVRs[i];
            Object value = srcValues[i];
            if (include != null && Arrays.binarySearch(include, fromIndex, toIndex, tag) < 0 || exclude != null && Arrays.binarySearch(exclude, fromIndex, toIndex, tag) >= 0) continue;
            if (TagUtils.isPrivateCreator(tag) && (privateCreator = other.privateCreatorAt(i)) != null) {
                if (selection != null && selection.creatorTagOf(privateCreator, tag, false) <= 0 || this.contains(tag) || this.creatorTagOf(privateCreator, tag, false) >= 0 && other.creatorTagOf(privateCreator, tag, false) == tag) continue;
                this.setString(tag, VR.LO, privateCreator);
                continue;
            }
            if (TagUtils.isPrivateTag(tag)) {
                int tmp = TagUtils.creatorTagOf(tag);
                if (creatorTag != tmp) {
                    creatorTag = tmp;
                    privateCreator = other.privateCreatorAt(other.indexOf(creatorTag));
                    privateCreator0 = privateCreator == null || privateCreator.equals(this.privateCreatorAt(this.indexOf(creatorTag))) ? null : privateCreator;
                }
            } else {
                creatorTag = 0;
                privateCreator = null;
                privateCreator0 = null;
            }
            if (selection != null && !selection.contains(privateCreator, tag)) continue;
            if (updatePolicy != null) {
                if (updatePolicy != UpdatePolicy.OVERWRITE && Attributes.isEmpty(value)) continue;
                int j = this.indexOf(tag);
                if (j < 0) {
                    if (updatePolicy == UpdatePolicy.PRESERVE) {
                        value = Value.NULL;
                    }
                } else {
                    Object origValue;
                    if (updatePolicy == UpdatePolicy.PRESERVE) continue;
                    Object object = origValue = this.vrs[j].isStringType() ? this.decodeStringValue(j) : this.values[j];
                    if (updatePolicy != UpdatePolicy.SUPPLEMENT ? this.equalValues(other, j, i) : !Attributes.isEmpty(origValue)) continue;
                    if (modified != null && !Attributes.isEmpty(origValue) && !modified.contains(privateCreator, tag)) {
                        if (origValue instanceof Sequence) {
                            modified.set(privateCreator, tag, (Sequence)origValue, null);
                        } else if (origValue instanceof Fragments) {
                            modified.set(privateCreator, tag, (Fragments)origValue);
                        } else {
                            modified.set(privateCreator, tag, this.vrs[j], Attributes.toggleEndian(this.vrs[j], origValue, modifiedToggleEndian));
                        }
                    }
                }
            }
            if (!simulate) {
                if (value instanceof Sequence) {
                    Sequence dest;
                    if (mergeOriginalAttributesSequence && tag == 67110241 && (dest = this.getSequence(tag)) != null) {
                        this.mergeOriginalAttributesSequence((Sequence)value, dest);
                    } else {
                        this.set(privateCreator0, tag, (Sequence)value, selection != null ? selection.getNestedDataset(privateCreator, tag) : null);
                    }
                } else if (value instanceof Fragments) {
                    this.set(privateCreator0, tag, (Fragments)value);
                } else {
                    if (decodeStringValue && vr.useSpecificCharacterSet() && (value = other.loadBulkData(vr, value)) instanceof byte[]) {
                        value = vr.toStrings(value, other.bigEndian(), other.getSpecificCharacterSet());
                    }
                    this.set(privateCreator0, tag, vr, Attributes.toggleEndian(vr, value, toggleEndian));
                }
            }
            ++numAdd;
        }
        return numAdd != 0;
    }

    private boolean containsNonASCIIStringValues(int[] include, int[] exclude, int fromIndex, int toIndex, Attributes selection) {
        for (int i = 0; i < this.size; ++i) {
            int tag = this.tags[i];
            Object val = this.values[i];
            if (include != null && Arrays.binarySearch(include, fromIndex, toIndex, tag) < 0 || exclude != null && Arrays.binarySearch(exclude, fromIndex, toIndex, tag) >= 0 || selection != null && !selection.contains(tag)) continue;
            if (val instanceof Sequence) {
                Attributes nestedSelection = selection != null ? selection.getNestedDataset(tag) : null;
                for (Attributes item : (Sequence)val) {
                    if (!item.containsNonASCIIStringValues(null, null, 0, 0, nestedSelection)) continue;
                    return true;
                }
                continue;
            }
            if (val == Value.NULL || !this.vrs[i].useSpecificCharacterSet()) continue;
            return true;
        }
        return false;
    }

    private boolean isUpdateSpecificCharacterSet(Attributes other, int[] include, int[] exclude, int fromIndex, int toIndex, Attributes selection, UpdatePolicy updatePolicy) {
        String[] oscs = other.getStrings(524293);
        if (oscs == null) {
            return false;
        }
        if (updatePolicy != null) {
            switch (updatePolicy.ordinal()) {
                case 4: {
                    return false;
                }
                case 0: {
                    if (this.containsValue(524293)) {
                        return false;
                    }
                }
                case 1: {
                    if (oscs.length != 0) break;
                    return false;
                }
            }
        }
        return !(include != null && Arrays.binarySearch(include, fromIndex, toIndex, 524293) < 0 || exclude != null && Arrays.binarySearch(exclude, fromIndex, toIndex, 524293) >= 0 || selection != null && !selection.contains(524293));
    }

    private void mergeOriginalAttributesSequence(Sequence src, Sequence dest) {
        TreeMap<String, Attributes> sort = new TreeMap<String, Attributes>();
        for (Attributes destItem : dest) {
            sort.put(destItem.getString(67110242, ""), destItem);
        }
        dest.clear();
        for (Attributes srcItem : src) {
            Attributes destModified;
            String dt = srcItem.getString(67110242, "");
            Attributes destItem = (Attributes)sort.get(dt);
            if (destItem != null && (destModified = destItem.getNestedDataset(0x4000550)) != null) {
                try {
                    Attributes srcModified = srcItem.getNestedDataset(0x4000550);
                    if (srcModified == null) continue;
                    destModified.addAll(srcModified);
                }
                catch (IncompatibleSpecificCharacterSetException e) {
                    LOG.info("Failed to merge original attributes modified at {}: {}", (Object)dt, (Object)e.getMessage());
                }
                continue;
            }
            sort.put(dt, new Attributes(srcItem));
        }
        for (Attributes destItem : sort.values()) {
            dest.add(destItem);
        }
    }

    public boolean update(UpdatePolicy updatePolicy, Attributes newAttrs, Attributes modified) {
        this.ensureModifiable();
        return this.add(newAttrs, null, null, 0, 0, null, updatePolicy, false, false, modified);
    }

    public boolean update(UpdatePolicy updatePolicy, boolean mergeOriginalAttributesSequence, Attributes newAttrs, Attributes modified) {
        this.ensureModifiable();
        return this.add(newAttrs, null, null, 0, 0, null, updatePolicy, mergeOriginalAttributesSequence, false, modified);
    }

    public boolean testUpdate(UpdatePolicy updatePolicy, Attributes newAttrs, Attributes modified) {
        return this.add(newAttrs, null, null, 0, 0, null, updatePolicy, false, true, modified);
    }

    public boolean updateSelected(UpdatePolicy updatePolicy, Attributes newAttrs, Attributes modified, int ... selection) {
        this.ensureModifiable();
        return this.add(newAttrs, selection, null, 0, selection.length, null, updatePolicy, false, false, modified);
    }

    public boolean testUpdateSelected(UpdatePolicy updatePolicy, Attributes newAttrs, Attributes modified, int ... selection) {
        return this.add(newAttrs, selection, null, 0, selection.length, null, updatePolicy, false, true, modified);
    }

    public boolean updateNotSelected(UpdatePolicy updatePolicy, Attributes newAttrs, Attributes modified, int ... selection) {
        this.ensureModifiable();
        return this.add(newAttrs, null, selection, 0, selection.length, null, updatePolicy, false, false, modified);
    }

    public boolean testUpdateNotSelected(UpdatePolicy updatePolicy, Attributes newAttrs, Attributes modified, int ... selection) {
        return this.add(newAttrs, null, selection, 0, selection.length, null, updatePolicy, false, true, modified);
    }

    public Attributes addOriginalAttributes(String sourceOfPreviousValues, Date modificationDateTime, String reasonForModification, String modifyingSystem, Attributes originalAttributes) {
        this.ensureModifiable();
        if (originalAttributes.isEmpty()) {
            return this;
        }
        Attributes item = new Attributes(this.bigEndian, 5);
        item.ensureSequence(0x4000550, 1).add(originalAttributes);
        item.setDate(67110242, VR.DT, modificationDateTime);
        item.setString(67110243, VR.LO, modifyingSystem);
        item.setString(67110244, VR.LO, sourceOfPreviousValues);
        item.setString(67110245, VR.CS, reasonForModification);
        this.ensureSequence(67110241, 1).add(item);
        return this;
    }

    private static Object toggleEndian(VR vr, Object value, boolean toggleEndian) {
        return toggleEndian && value instanceof byte[] ? (Object)vr.toggleEndian((byte[])value, true) : value;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Attributes)) {
            return false;
        }
        Attributes other = (Attributes)o;
        if (this.size != other.size) {
            return false;
        }
        String privateCreator = null;
        int creatorTag = 0;
        int otherCreatorTag = 0;
        block5: for (int i = 0; i < this.size; ++i) {
            int tag = this.tags[i];
            switch (TagUtils.Type.typeOf(tag)) {
                case PRIVATE_CREATOR: {
                    continue block5;
                }
                case PRIVATE: {
                    int tmp = TagUtils.creatorTagOf(tag);
                    if (creatorTag != tmp) {
                        creatorTag = tmp;
                        privateCreator = this.privateCreatorAt(this.indexOf(tmp));
                        if (privateCreator != null ? (otherCreatorTag = other.creatorTagOf(privateCreator, tag, false)) == -1 : other.privateCreatorAt(other.indexOf(tmp)) != null) {
                            return false;
                        }
                    }
                    if (privateCreator != null) {
                        int j = other.indexOf(TagUtils.toPrivateTag(otherCreatorTag, tag));
                        if (j >= 0 && this.equalValues(other, i, j)) continue block5;
                        return false;
                    }
                }
                case STANDARD: {
                    if (tag == other.tags[i] && this.equalValues(other, i, i)) continue block5;
                    return false;
                }
            }
        }
        return true;
    }

    public boolean equalValues(Attributes other, int tag) {
        return this.equalValues(other, null, tag);
    }

    public boolean equalValues(Attributes other, String privateCreator, int tag) {
        return this.equalValues(other, this.indexOf(privateCreator, tag), other.indexOf(privateCreator, tag));
    }

    private boolean equalValues(Attributes other, int index, int otherIndex) {
        Object v2;
        VR vr;
        if (index < 0 && otherIndex < 0) {
            return true;
        }
        VR vR = vr = index < 0 ? other.vrs[otherIndex] : this.vrs[index];
        if (otherIndex >= 0 && vr != other.vrs[otherIndex]) {
            return false;
        }
        if (vr.isStringType()) {
            Value v22;
            try {
                if (vr == VR.IS) {
                    return Arrays.equals(this.decodeISValue(index), other.decodeISValue(otherIndex));
                }
                if (vr == VR.DS) {
                    return Arrays.equals(this.decodeDSValue(index), other.decodeDSValue(otherIndex));
                }
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
            Value v1 = index < 0 ? Value.NULL : this.decodeStringValue(index);
            Value value = v22 = otherIndex < 0 ? Value.NULL : other.decodeStringValue(otherIndex);
            return v1 instanceof String[] ? v22 instanceof String[] && (vr == VR.PN ? Attributes.equalPNValues((String[])v1, (String[])v22) : Arrays.equals((String[])v1, (String[])v22)) : !(v22 instanceof String[]) && (vr == VR.PN ? this.equalPNValues(v1, v22) : v1.equals(v22));
        }
        Value v1 = index < 0 ? Value.NULL : this.values[index];
        Object object = v2 = otherIndex < 0 ? Value.NULL : other.values[otherIndex];
        if (v1 instanceof byte[]) {
            if (v2 instanceof byte[] && ((byte[])v1).length == ((byte[])v2).length) {
                if (this.bigEndian != other.bigEndian) {
                    v2 = vr.toggleEndian((byte[])v2, true);
                }
                return Arrays.equals((byte[])v1, (byte[])v2);
            }
        } else {
            return v1.equals(v2);
        }
        return false;
    }

    private boolean equalPNValues(Object v1, Object v2) {
        return v1 == Value.NULL ? !Attributes.containsPNValue(v2) : (v2 == Value.NULL ? !Attributes.containsPNValue(v1) : Attributes.equalPNValues((String)v1, (String)v2));
    }

    private static boolean containsPNValue(Object v) {
        return v != Value.NULL && !new PersonName((String)v, true).isEmpty();
    }

    private static boolean equalPNValues(String[] v1, String[] v2) {
        if (v1.length != v2.length) {
            return false;
        }
        for (int i = 0; i < v1.length; ++i) {
            if (Attributes.equalPNValues(v1[i], v2[i])) continue;
            return false;
        }
        return true;
    }

    private static boolean equalPNValues(String v1, String v2) {
        return new PersonName(v1, true).equals(new PersonName(v2, true));
    }

    public int hashCode() {
        int h = 0;
        for (int i = 0; i < this.size; ++i) {
            int tag = this.tags[i];
            if (TagUtils.isPrivateGroup(tag)) continue;
            h = 31 * h + tag;
        }
        return h;
    }

    private void set(String privateCreator, int tag, Sequence src, Attributes selection) {
        Sequence dst = this.newSequence(privateCreator, tag, src.size());
        for (Attributes item : src) {
            dst.add(selection != null && !selection.isEmpty() ? new Attributes(item, this.bigEndian, selection) : new Attributes(item, this.bigEndian));
        }
    }

    private void set(String privateCreator, int tag, Fragments src) {
        boolean toogleEndian = src.bigEndian() != this.bigEndian;
        VR vr = src.vr();
        Fragments dst = this.newFragments(privateCreator, tag, vr, src.size());
        for (Object frag : src) {
            dst.add(Attributes.toggleEndian(vr, frag, toogleEndian));
        }
    }

    public String toString() {
        return this.toString(TO_STRING_LIMIT, TO_STRING_WIDTH);
    }

    public String toString(int limit, int maxWidth) {
        return this.toStringBuilder(limit, maxWidth, new StringBuilder(1024)).toString();
    }

    public StringBuilder toStringBuilder(StringBuilder sb) {
        return this.toStringBuilder(TO_STRING_LIMIT, TO_STRING_WIDTH, sb);
    }

    public StringBuilder toStringBuilder(int limit, int maxWidth, StringBuilder sb) {
        if (this.appendAttributes(limit, maxWidth, sb, "") > limit) {
            sb.append("...\n");
        }
        return sb;
    }

    private int appendAttributes(int limit, int maxWidth, StringBuilder sb, String prefix) {
        if (this.size == 0) {
            return 0;
        }
        if (this.tags[0] >= 0) {
            return this.appendAttributes(limit, maxWidth, sb, prefix, 0, this.size);
        }
        int index0 = -(1 + this.indexOf(0));
        int lines = this.appendAttributes(limit, maxWidth, sb, prefix, index0, this.size);
        return lines + this.appendAttributes(limit - lines, maxWidth, sb, prefix, 0, index0);
    }

    private int appendAttributes(int limit, int maxWidth, StringBuilder sb, String prefix, int start, int end) {
        int lines = 0;
        int creatorTag = 0;
        String privateCreator = null;
        for (int i = start; i < end && ++lines <= limit; ++i) {
            int tag = this.tags[i];
            if (TagUtils.isPrivateTag(tag)) {
                int tmp = TagUtils.creatorTagOf(tag);
                if (creatorTag != tmp) {
                    creatorTag = tmp;
                    privateCreator = this.getString(creatorTag, null);
                }
            } else {
                creatorTag = 0;
                privateCreator = null;
            }
            Object value = this.values[i];
            this.appendAttribute(privateCreator, tag, this.vrs[i], value, sb.length() + maxWidth, sb, prefix);
            if (!(value instanceof Sequence)) continue;
            lines += this.appendItems((Sequence)value, limit - lines, maxWidth, sb, prefix + '>');
        }
        return lines;
    }

    private int appendItems(Sequence sq, int limit, int maxWidth, StringBuilder sb, String prefix) {
        int lines = 0;
        int itemNo = 0;
        for (Attributes item : sq) {
            if (++lines > limit) break;
            sb.append(prefix).append("Item #").append(++itemNo).append('\n');
            lines += item.appendAttributes(limit - lines, maxWidth, sb, prefix);
        }
        return lines;
    }

    private StringBuilder appendAttribute(String privateCreator, int tag, VR vr, Object value, int maxLength, StringBuilder sb, String prefix) {
        sb.append(prefix).append(TagUtils.toString(tag)).append(' ').append((Object)vr).append(" [");
        if (vr.prompt(value, this.bigEndian, this.getSpecificCharacterSet(vr), maxLength - sb.length() - 1, sb)) {
            sb.append("] ").append(ElementDictionary.keywordOf(tag, privateCreator));
            if (sb.length() > maxLength) {
                sb.setLength(maxLength);
            }
        }
        sb.append('\n');
        return sb;
    }

    public int calcLength(DicomEncodingOptions encOpts, boolean explicitVR) {
        if (this.isEmpty()) {
            return 0;
        }
        this.groupLengths = encOpts.groupLength ? new int[this.countGroups()] : null;
        this.length = this.calcLength(encOpts, explicitVR, this.getSpecificCharacterSet(), this.groupLengths);
        return this.length;
    }

    private int calcLength(DicomEncodingOptions encOpts, boolean explicitVR, SpecificCharacterSet cs, int[] groupLengths) {
        int totlen = 0;
        int groupLengthTag = -1;
        int groupLengthIndex = -1;
        for (int i = 0; i < this.size; ++i) {
            int len;
            VR vr = this.vrs[i];
            Object val = this.values[i];
            int n = len = explicitVR ? vr.headerLength() : 8;
            if (val instanceof Value) {
                len += ((Value)val).calcLength(encOpts, explicitVR, vr);
            } else {
                if (!(val instanceof byte[])) {
                    byte[] byArray = vr.toBytes(val, cs);
                    val = byArray;
                    this.values[i] = byArray;
                }
                len += ((byte[])val).length + 1 & 0xFFFFFFFE;
            }
            totlen += len;
            if (groupLengths == null) continue;
            int tmp = TagUtils.groupLengthTagOf(this.tags[i]);
            if (groupLengthTag != tmp) {
                groupLengthTag = tmp;
                totlen += 12;
            }
            int n2 = ++groupLengthIndex;
            groupLengths[n2] = groupLengths[n2] + len;
        }
        return totlen;
    }

    private int countGroups() {
        int groupLengthTag = -1;
        int count = 0;
        for (int i = 0; i < this.size; ++i) {
            int tmp = TagUtils.groupLengthTagOf(this.tags[i]);
            if (groupLengthTag == tmp) continue;
            if (groupLengthTag < 0) {
                this.groupLengthIndex0 = count;
            }
            groupLengthTag = tmp;
            ++count;
        }
        return count;
    }

    public void writeTo(DicomOutputStream out) throws IOException {
        if (this.isEmpty()) {
            return;
        }
        if (this.groupLengths == null && out.getEncodingOptions().groupLength) {
            throw new IllegalStateException("groupLengths not initialized by calcLength()");
        }
        SpecificCharacterSet cs = this.getSpecificCharacterSet();
        if (this.tags[0] < 0) {
            int index0 = -(1 + this.indexOf(0));
            this.writeTo(out, cs, index0, this.size, this.groupLengthIndex0);
            this.writeTo(out, cs, 0, index0, 0);
        } else {
            this.writeTo(out, cs, 0, this.size, 0);
        }
    }

    public void writePostPixelDataTo(DicomOutputStream out) throws IOException {
        if (this.isEmpty() || this.tags[0] >= 0 && this.tags[this.size - 1] <= 2145386512) {
            return;
        }
        SpecificCharacterSet cs = this.getSpecificCharacterSet();
        int indexPostPixelData = this.indexOf(2145386512) + 1;
        if (indexPostPixelData < 0) {
            indexPostPixelData = -indexPostPixelData;
        }
        this.writeTo(out, cs, indexPostPixelData, this.size, 0);
        if (this.tags[0] < 0) {
            int index0 = -(1 + this.indexOf(0));
            this.writeTo(out, cs, 0, index0, 0);
        }
    }

    public void writeItemTo(DicomOutputStream out) throws IOException {
        DicomEncodingOptions encOpts = out.getEncodingOptions();
        int len = this.getEncodedItemLength(encOpts, out.isExplicitVR());
        out.writeHeader(-73728, null, len);
        this.writeTo(out);
        if (len == -1) {
            out.writeHeader(-73715, null, 0);
        }
    }

    private int getEncodedItemLength(DicomEncodingOptions encOpts, boolean explicitVR) {
        if (this.isEmpty()) {
            return encOpts.undefEmptyItemLength ? -1 : 0;
        }
        if (encOpts.undefItemLength) {
            return -1;
        }
        if (this.length == -1) {
            this.calcLength(encOpts, explicitVR);
        }
        return this.length;
    }

    private void writeTo(DicomOutputStream out, SpecificCharacterSet cs, int start, int end, int groupLengthIndex) throws IOException {
        boolean groupLength = this.groupLengths != null;
        int groupLengthTag = -1;
        for (int i = start; i < end; ++i) {
            int tmp;
            int tag = this.tags[i];
            if (groupLength && groupLengthTag != (tmp = TagUtils.groupLengthTagOf(tag))) {
                groupLengthTag = tmp;
                out.writeGroupLength(groupLengthTag, this.groupLengths[groupLengthIndex++]);
            }
            out.writeAttribute(tag, this.vrs[i], this.values[i], cs);
        }
    }

    public boolean accept(Visitor visitor, boolean visitNestedDatasets) throws Exception {
        if (this.isEmpty()) {
            return true;
        }
        if (this.tags[0] < 0) {
            int index0 = -(1 + this.indexOf(0));
            return this.accept(visitor, visitNestedDatasets, index0, this.size) && this.accept(visitor, visitNestedDatasets, 0, index0);
        }
        return this.accept(visitor, visitNestedDatasets, 0, this.size);
    }

    private boolean accept(Visitor visitor, boolean visitNestedDatasets, int start, int end) throws Exception {
        for (int i = start; i < end; ++i) {
            if (!visitor.visit(this, this.tags[i], this.vrs[i], this.values[i])) {
                return false;
            }
            if (!visitNestedDatasets || !(this.values[i] instanceof Sequence)) continue;
            if (visitor instanceof SequenceVisitor) {
                ((SequenceVisitor)visitor).startSequence(this.tags[i]);
            }
            int itemIndex = 0;
            for (Attributes item : (Sequence)this.values[i]) {
                if (visitor instanceof SequenceVisitor) {
                    ((SequenceVisitor)visitor).startItem(this.tags[i], itemIndex);
                }
                if (!item.accept(visitor, true)) {
                    return false;
                }
                if (visitor instanceof SequenceVisitor) {
                    ((SequenceVisitor)visitor).endItem();
                }
                ++itemIndex;
            }
            if (!(visitor instanceof SequenceVisitor)) continue;
            ((SequenceVisitor)visitor).endSequence();
        }
        return true;
    }

    public void writeGroupTo(DicomOutputStream out, int groupLengthTag) throws IOException {
        if (this.isEmpty()) {
            throw new IllegalStateException("No attributes");
        }
        this.checkInGroup(0, groupLengthTag);
        this.checkInGroup(this.size - 1, groupLengthTag);
        SpecificCharacterSet cs = this.getSpecificCharacterSet();
        out.writeGroupLength(groupLengthTag, this.calcLength(out.getEncodingOptions(), out.isExplicitVR(), cs, null));
        this.writeTo(out, cs, 0, this.size, 0);
    }

    private void checkInGroup(int i, int groupLengthTag) {
        int tag = this.tags[i];
        if (TagUtils.groupLengthTagOf(tag) != groupLengthTag) {
            throw new IllegalStateException(TagUtils.toString(tag) + " does not belong to group (" + TagUtils.shortToHexString(TagUtils.groupNumber(groupLengthTag)) + ",eeee).");
        }
    }

    public Attributes createFileMetaInformation(String tsuid) {
        return this.createFileMetaInformation(tsuid, true);
    }

    public Attributes createFileMetaInformation(String tsuid, boolean includeImplementationVersionName) {
        return Attributes.createFileMetaInformation(this.getString(524312, null), this.getString(524310, null), tsuid, includeImplementationVersionName);
    }

    public static Attributes createFileMetaInformation(String iuid, String cuid, String tsuid) {
        return Attributes.createFileMetaInformation(iuid, cuid, tsuid, true);
    }

    public static Attributes createFileMetaInformation(String iuid, String cuid, String tsuid, boolean includeImplementationVersionName) {
        if (iuid == null || iuid.isEmpty()) {
            throw new IllegalArgumentException("Missing SOP Instance UID");
        }
        if (cuid == null || cuid.isEmpty()) {
            throw new IllegalArgumentException("Missing SOP Class UID");
        }
        if (tsuid == null || tsuid.isEmpty()) {
            throw new IllegalArgumentException("Missing Transfer Syntax UID");
        }
        Attributes fmi = new Attributes(6);
        fmi.setBytes(131073, VR.OB, new byte[]{0, 1});
        fmi.setString(131074, VR.UI, cuid);
        fmi.setString(131075, VR.UI, iuid);
        fmi.setString(131088, VR.UI, tsuid);
        fmi.setString(131090, VR.UI, Implementation.getClassUID());
        if (includeImplementationVersionName) {
            fmi.setString(131091, VR.SH, Implementation.getVersionName());
        }
        return fmi;
    }

    public boolean matches(Attributes keys, boolean ignorePNCase, boolean matchNoValue) {
        int[] keyTags = keys.tags;
        VR[] keyVrs = keys.vrs;
        Object[] keyValues = keys.values;
        int keysSize = keys.size;
        String privateCreator = null;
        int creatorTag = 0;
        for (int i = 0; i < keysSize; ++i) {
            int tag = keyTags[i];
            if (TagUtils.isPrivateCreator(tag)) continue;
            if (TagUtils.isPrivateGroup(tag)) {
                int tmp = TagUtils.creatorTagOf(tag);
                if (creatorTag != tmp) {
                    creatorTag = tmp;
                    privateCreator = keys.getString(creatorTag, null);
                }
            } else {
                creatorTag = 0;
                privateCreator = null;
            }
            Object keyValue = keyValues[i];
            if (Attributes.isEmpty(keyValue)) continue;
            if (keyVrs[i].isStringType()) {
                if (this.matches(privateCreator, tag, keyVrs[i], ignorePNCase, matchNoValue, keys.getStrings(privateCreator, tag, null))) continue;
                return false;
            }
            if (keyValue instanceof Sequence) {
                if (this.matches(privateCreator, tag, ignorePNCase, matchNoValue, (Sequence)keyValue)) continue;
                return false;
            }
            LOG.info("Matching Key {} with VR: {} not supported", (Object)TagUtils.toString(tag), (Object)keyVrs[i]);
        }
        return true;
    }

    private boolean matches(String privateCreator, int tag, VR vr, boolean ignorePNCase, boolean matchNoValue, String[] keyVals) {
        String[] vals = this.getStrings(privateCreator, tag, null);
        if (vals == null || vals.length == 0) {
            return matchNoValue;
        }
        boolean ignoreCase = ignorePNCase && vr == VR.PN;
        for (String keyVal : keyVals) {
            DateRange dateRange = null;
            switch (vr) {
                case PN: {
                    keyVal = new PersonName(keyVals[0]).toString();
                    break;
                }
                case DA: 
                case DT: 
                case TM: {
                    dateRange = this.toDateRange(keyVal, vr);
                }
            }
            if (StringUtils.containsWildCard(keyVal)) {
                Pattern pattern = StringUtils.compilePattern(keyVal, ignoreCase);
                String[] stringArray = vals;
                int n = stringArray.length;
                for (int i = 0; i < n; ++i) {
                    String val = stringArray[i];
                    if (val == null) {
                        if (!matchNoValue) continue;
                        return true;
                    }
                    if (vr == VR.PN) {
                        val = new PersonName(val).toString();
                    }
                    if (!pattern.matcher(val).matches()) continue;
                    return true;
                }
                continue;
            }
            for (String val : vals) {
                if (val == null) {
                    if (!matchNoValue) continue;
                    return true;
                }
                if (dateRange != null) {
                    if (!dateRange.contains(vr.toDate(val, this.getTimeZone(), 0, false, null, new DatePrecision()))) continue;
                    return true;
                }
                if (vr == VR.PN) {
                    val = new PersonName(val).toString();
                }
                if (!(ignoreCase ? keyVal.equalsIgnoreCase(val) : keyVal.equals(val))) continue;
                return true;
            }
        }
        return false;
    }

    private boolean matches(String privateCreator, int tag, boolean ignorePNCase, boolean matchNoValue, Sequence keySeq) {
        Attributes keys;
        int n = keySeq.size();
        if (n > 1) {
            LOG.info("Matching Key {} with VR: SQ contains {} Items - only consider first Item", (Object)TagUtils.toString(tag), (Object)n);
        }
        if ((keys = (Attributes)keySeq.get(0)).isEmpty()) {
            return true;
        }
        Object value = this.getValue(privateCreator, tag);
        if (value == null || Attributes.isEmpty(value)) {
            return matchNoValue;
        }
        if (value instanceof Sequence) {
            Sequence sq = (Sequence)value;
            for (Attributes item : sq) {
                if (!item.matches(keys, ignorePNCase, matchNoValue)) continue;
                return true;
            }
        }
        return false;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeInt(this.size);
        DicomOutputStream dout = new DicomOutputStream(out, this.bigEndian ? "1.2.840.10008.1.2.2" : "1.2.840.10008.1.2.1");
        dout.writeDataset(null, this);
        dout.writeHeader(-73715, null, 0);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.init(in.readInt());
        DicomInputStream din = new DicomInputStream((InputStream)in, this.bigEndian ? "1.2.840.10008.1.2.2" : "1.2.840.10008.1.2.1");
        din.readItemValue(this, -1L);
    }

    public ValidationResult validate(IOD iod) {
        ValidationResult result = new ValidationResult();
        HashMap<String, Boolean> resolvedConditions = new HashMap<String, Boolean>();
        for (IOD.DataElement el : iod) {
            this.validate(el, result, resolvedConditions);
        }
        return result;
    }

    public void validate(IOD.DataElement el, ValidationResult result) {
        this.validate(el, result, null);
    }

    private void validate(IOD.DataElement el, ValidationResult result, Map<String, Boolean> processedConditions) {
        int index;
        IOD.Condition condition = el.getCondition();
        if (condition != null) {
            Boolean match;
            String id = condition.id();
            Boolean bl = match = id != null ? processedConditions.get(id) : null;
            if (match == null) {
                match = condition.match(this);
                if (id != null) {
                    processedConditions.put(id, match);
                }
            }
            if (!match.booleanValue()) {
                return;
            }
        }
        if ((index = this.indexOf(el.tag)) < 0) {
            if (el.type == IOD.DataElementType.TYPE_1 || el.type == IOD.DataElementType.TYPE_2) {
                result.addMissingAttribute(el);
            }
            return;
        }
        Object value = this.values[index];
        if (Attributes.isEmpty(value)) {
            if (el.type == IOD.DataElementType.TYPE_1) {
                result.addMissingAttributeValue(el);
            }
            return;
        }
        if (el.type == IOD.DataElementType.TYPE_0) {
            result.addNotAllowedAttribute(el);
            return;
        }
        VR vr = this.vrs[index];
        if (vr.isStringType()) {
            value = this.decodeStringValue(index);
        }
        Object validVals = el.getValues();
        if (el.vr == VR.SQ) {
            if (!(value instanceof Sequence)) {
                result.addInvalidAttributeValue(el, ValidationResult.Invalid.VR);
                return;
            }
            Sequence seq = (Sequence)value;
            int seqSize = seq.size();
            if (el.maxVM > 0 && seqSize > el.maxVM) {
                result.addInvalidAttributeValue(el, ValidationResult.Invalid.MultipleItems);
                return;
            }
            if (validVals instanceof Code[]) {
                boolean invalidItem = false;
                ValidationResult[] itemValidationResults = new ValidationResult[seqSize];
                for (int i = 0; i < seqSize; ++i) {
                    ValidationResult itemValidationResult = this.validateCode((Attributes)seq.get(i), (Code[])validVals);
                    invalidItem = invalidItem || !itemValidationResult.isValid();
                    itemValidationResults[i] = itemValidationResult;
                }
                if (invalidItem) {
                    result.addInvalidAttributeValue(el, ValidationResult.Invalid.Code, itemValidationResults, null);
                }
            } else if (validVals instanceof IOD[]) {
                IOD[] itemIODs = (IOD[])validVals;
                int[] matchingItems = new int[itemIODs.length];
                boolean invalidItem = false;
                ValidationResult[] itemValidationResults = new ValidationResult[seqSize];
                for (int i = 0; i < seqSize; ++i) {
                    ValidationResult itemValidationResult = new ValidationResult();
                    HashMap<String, Boolean> resolvedItemConditions = new HashMap<String, Boolean>();
                    Attributes item = (Attributes)seq.get(i);
                    for (int j = 0; j < itemIODs.length; ++j) {
                        IOD itemIOD = itemIODs[j];
                        IOD.Condition itemCondition = itemIOD.getCondition();
                        if (itemCondition != null) {
                            Boolean match;
                            String id = itemCondition.id();
                            Boolean bl = match = id != null ? (Boolean)resolvedItemConditions.get(id) : null;
                            if (match == null) {
                                match = itemCondition.match(item);
                                if (id != null) {
                                    resolvedItemConditions.put(id, match);
                                }
                            }
                            if (!match.booleanValue()) continue;
                        }
                        int n = j;
                        matchingItems[n] = matchingItems[n] + 1;
                        for (IOD.DataElement itemEl : itemIOD) {
                            item.validate(itemEl, itemValidationResult, resolvedItemConditions);
                        }
                    }
                    invalidItem = invalidItem || !itemValidationResult.isValid();
                    itemValidationResults[i] = itemValidationResult;
                }
                IOD[] missingItems = this.checkforMissingItems(matchingItems, itemIODs);
                if (invalidItem || missingItems != null) {
                    result.addInvalidAttributeValue(el, ValidationResult.Invalid.Item, itemValidationResults, missingItems);
                }
            }
            return;
        }
        if (el.maxVM > 0 || el.minVM > 1) {
            int vm = vr.vmOf(value);
            if (el.maxVM > 0 && vm > el.maxVM || el.minVM > 1 && vm < el.minVM) {
                result.addInvalidAttributeValue(el, ValidationResult.Invalid.VM);
                return;
            }
        }
        if (validVals == null) {
            return;
        }
        if (validVals instanceof String[]) {
            if (!vr.isStringType()) {
                result.addInvalidAttributeValue(el, ValidationResult.Invalid.VR);
                return;
            }
            if (!this.isValidValue(Attributes.toStrings(value), el.valueNumber, (String[])validVals)) {
                result.addInvalidAttributeValue(el, ValidationResult.Invalid.Value);
            }
        } else if (validVals instanceof int[]) {
            if (vr == VR.IS) {
                value = this.decodeISValue(index);
            } else if (!vr.isIntType()) {
                result.addInvalidAttributeValue(el, ValidationResult.Invalid.VR);
                return;
            }
            if (!this.isValidValue(vr.toInts(value, this.bigEndian), el.valueNumber, (int[])validVals)) {
                result.addInvalidAttributeValue(el, ValidationResult.Invalid.Value);
            }
        }
    }

    private IOD[] checkforMissingItems(int[] matchingItems, IOD[] itemIODs) {
        IOD[] missingItems = new IOD[matchingItems.length];
        int n = 0;
        for (int i = 0; i < matchingItems.length; ++i) {
            IOD itemIOD = itemIODs[i];
            if (matchingItems[i] != 0 || itemIOD.getType() != IOD.DataElementType.TYPE_1) continue;
            missingItems[n++] = itemIOD;
        }
        return n > 0 ? Arrays.copyOf(missingItems, n) : null;
    }

    private ValidationResult validateCode(Attributes item, Code[] validVals) {
        Code code;
        ValidationResult result = null;
        Code[] codeArray = validVals;
        int n = codeArray.length;
        for (int i = 0; i < n && !(result = item.validate(IOD.valueOf(code = codeArray[i]))).isValid(); ++i) {
        }
        return result;
    }

    private boolean isValidValue(String[] val, int valueNumber, String[] validVals) {
        if (valueNumber != 0) {
            return val.length < valueNumber || this.isOneOf(val[valueNumber - 1], validVals);
        }
        for (int i = 0; i < val.length; ++i) {
            if (this.isOneOf(val[i], validVals)) continue;
            return false;
        }
        return true;
    }

    private <T> boolean isOneOf(Object val, T[] ss) {
        if (ss == null) {
            return true;
        }
        for (T s : ss) {
            if (!val.equals(s)) continue;
            return true;
        }
        return false;
    }

    private boolean isValidValue(int[] val, int valueNumber, int[] validVals) {
        if (valueNumber != 0) {
            return val.length < valueNumber || this.isOneOf(val[valueNumber - 1], validVals);
        }
        for (int i = 0; i < val.length; ++i) {
            if (this.isOneOf(val[i], validVals)) continue;
            return false;
        }
        return true;
    }

    private boolean isOneOf(int val, int[] is) {
        if (is == null) {
            return true;
        }
        for (int i : is) {
            if (val != i) continue;
            return true;
        }
        return false;
    }

    public Attributes getModified(Attributes other, Attributes result) {
        if (result == null) {
            result = new Attributes(other.size);
        }
        int creatorTag = -1;
        int prevOtherCreatorTag = -1;
        int otherCreatorTag = -1;
        String privateCreator = null;
        for (int i = 0; i < other.size; ++i) {
            Object origValue;
            int tag = other.tags[i];
            if ((tag & 0x10000) != 0) {
                if ((tag & 0xFF00) == 0) continue;
                otherCreatorTag = TagUtils.creatorTagOf(tag);
                if (prevOtherCreatorTag != otherCreatorTag) {
                    Object o;
                    prevOtherCreatorTag = otherCreatorTag;
                    creatorTag = -1;
                    int k = other.indexOf(otherCreatorTag);
                    if (k >= 0 && (o = other.decodeStringValue(k)) instanceof String) {
                        privateCreator = (String)o;
                        creatorTag = this.creatorTagOf(privateCreator, tag, false);
                    }
                }
                if (creatorTag == -1) continue;
                tag = TagUtils.toPrivateTag(creatorTag, tag);
            } else {
                privateCreator = null;
            }
            int j = this.indexOf(tag);
            if (j < 0 || (origValue = this.values[j]) instanceof Value && ((Value)origValue).isEmpty() || this.equalValues(other, j, i)) continue;
            if (origValue instanceof Sequence) {
                result.set(privateCreator, tag, (Sequence)origValue, null);
                continue;
            }
            if (origValue instanceof Fragments) {
                result.set(privateCreator, tag, (Fragments)origValue);
                continue;
            }
            result.set(privateCreator, tag, this.vrs[j], origValue);
        }
        return result;
    }

    public Attributes getRemovedOrModified(Attributes other) {
        Attributes modified = new Attributes(this.size);
        int creatorTag = -1;
        int prevCreatorTag = -1;
        int otherCreatorTag = 0;
        String privateCreator = null;
        for (int i = 0; i < this.size; ++i) {
            int j;
            Object origValue;
            int tag = this.tags[i];
            if ((tag & 0x10000) != 0) {
                if ((tag & 0xFF00) == 0) continue;
                creatorTag = TagUtils.creatorTagOf(tag);
                if (prevCreatorTag != creatorTag) {
                    Object o;
                    prevCreatorTag = creatorTag;
                    otherCreatorTag = -1;
                    privateCreator = null;
                    int k = this.indexOf(creatorTag);
                    if (k >= 0 && (o = this.decodeStringValue(k)) instanceof String) {
                        privateCreator = (String)o;
                        otherCreatorTag = other.creatorTagOf(privateCreator, tag, false);
                    }
                }
                if (privateCreator == null) continue;
                if (otherCreatorTag != -1) {
                    tag = TagUtils.toPrivateTag(otherCreatorTag, tag);
                }
            } else {
                otherCreatorTag = 0;
                privateCreator = null;
            }
            if ((origValue = this.values[i]) instanceof Value && ((Value)origValue).isEmpty() || otherCreatorTag >= 0 && (j = other.indexOf(tag)) >= 0 && this.equalValues(other, i, j)) continue;
            if (origValue instanceof Sequence) {
                modified.set(privateCreator, tag, (Sequence)origValue, null);
                continue;
            }
            if (origValue instanceof Fragments) {
                modified.set(privateCreator, tag, (Fragments)origValue);
                continue;
            }
            modified.set(privateCreator, tag, this.vrs[i], origValue);
        }
        return modified;
    }

    public int diff(Attributes other, int[] selection, Attributes diff) {
        return this.diff(other, selection, diff, false);
    }

    public int diff(Attributes other, int[] selection, Attributes diff, boolean onlyModified) {
        int count = 0;
        for (int tag : selection) {
            int otherIndex;
            int index = this.indexOf(tag);
            if (this.equalValues(other, index, otherIndex = other.indexOf(tag))) continue;
            if (diff != null) {
                Value value;
                Value value2 = value = index < 0 ? Value.NULL : this.values[index];
                if (!onlyModified || value != Value.NULL) {
                    if (value instanceof Sequence) {
                        diff.set(null, tag, (Sequence)value, null);
                    } else {
                        diff.set(tag, index < 0 ? other.vrs[otherIndex] : this.vrs[index], value);
                    }
                }
            }
            ++count;
        }
        return count;
    }

    public static void unifyCharacterSets(Attributes ... attrsList) {
        if (attrsList.length == 0) {
            return;
        }
        SpecificCharacterSet utf8 = SpecificCharacterSet.valueOf("ISO_IR 192");
        SpecificCharacterSet commonCS = attrsList[0].getSpecificCharacterSet();
        if (!commonCS.equals(utf8)) {
            for (int i = 1; i < attrsList.length; ++i) {
                SpecificCharacterSet cs = attrsList[i].getSpecificCharacterSet();
                if (cs.equals(commonCS) || cs.isASCII() && commonCS.containsASCII()) continue;
                if (commonCS.isASCII() && cs.containsASCII()) {
                    commonCS = cs;
                    continue;
                }
                commonCS = utf8;
                break;
            }
        }
        for (Attributes attrs : attrsList) {
            SpecificCharacterSet cs = attrs.getSpecificCharacterSet();
            if (cs.equals(commonCS)) continue;
            if (!cs.isASCII() || !commonCS.containsASCII()) {
                attrs.decodeStringValuesUsingSpecificCharacterSet();
            }
            attrs.setString(524293, VR.CS, commonCS.toCodes());
        }
    }

    public int removeAllBulkData() {
        return this.removeAllBulkData(false);
    }

    public int removeAllBulkData(boolean containsBulkData) {
        this.ensureModifiable();
        int removed = 0;
        for (int i = 0; i < this.size; ++i) {
            Object value = this.values[i];
            if (Attributes.isBulkData(value) || containsBulkData && Attributes.containsBulkData(value)) {
                int srcPos = i + 1;
                int len = this.size - srcPos;
                System.arraycopy(this.tags, srcPos, this.tags, i, len);
                System.arraycopy(this.vrs, srcPos, this.vrs, i, len);
                System.arraycopy(this.values, srcPos, this.values, i, len);
                --i;
                --this.size;
                ++removed;
                continue;
            }
            if (containsBulkData || !(value instanceof Sequence)) continue;
            for (Attributes item : (Sequence)value) {
                removed += item.removeAllBulkData();
            }
        }
        return removed;
    }

    private static boolean isBulkData(Object value) {
        return value instanceof BulkData || value instanceof Fragments && ((Fragments)value).size() > 1 && ((Fragments)value).get(1) instanceof BulkData;
    }

    private static boolean containsBulkData(Object value) {
        if (value instanceof Sequence) {
            for (Attributes item : (Sequence)value) {
                if (!item.containsBulkData()) continue;
                return true;
            }
        }
        return false;
    }

    public boolean containsBulkData() {
        for (int i = 0; i < this.size; ++i) {
            Object value = this.values[i];
            if (!Attributes.isBulkData(value) && !Attributes.containsBulkData(value)) continue;
            return true;
        }
        return false;
    }

    private int creatorIndexOf(String privateCreator, int groupNumber) {
        if ((groupNumber & 1) == 0) {
            throw new IllegalArgumentException("(" + TagUtils.shortToHexString(groupNumber) + ",xxxx) is not a private Group");
        }
        int group = groupNumber << 16;
        int creatorTag = group | 0x10;
        int index = this.indexOf(creatorTag);
        if (index < 0) {
            index = -index - 1;
        }
        while (index < this.size && (this.tags[index] & 0xFFFFFF00) == group) {
            Object creatorID;
            if (this.vrs[index].isStringType() && privateCreator.equals(creatorID = this.decodeStringValue(index))) {
                return index;
            }
            ++index;
            ++creatorTag;
        }
        return -1;
    }

    public int removePrivateAttributes(String privateCreator, int groupNumber) {
        int len2;
        int end;
        int srcPos;
        int start;
        this.ensureModifiable();
        int privateCreatorIndex = this.creatorIndexOf(privateCreator, groupNumber);
        if (privateCreatorIndex < 0) {
            return 0;
        }
        int creatorTag = this.tags[privateCreatorIndex];
        int privateTag = creatorTag & 0xFFFF0000 | (creatorTag & 0xFF) << 8;
        for (start = srcPos = privateCreatorIndex + 1; start < this.size && this.tags[start] < privateTag; ++start) {
        }
        for (end = start; end < this.size && (this.tags[end] & 0xFFFFFF00) == privateTag; ++end) {
        }
        int len1 = start - srcPos;
        if (len1 > 0) {
            System.arraycopy(this.tags, srcPos, this.tags, privateCreatorIndex, len1);
            System.arraycopy(this.vrs, srcPos, this.vrs, privateCreatorIndex, len1);
            System.arraycopy(this.values, srcPos, this.values, privateCreatorIndex, len1);
        }
        if ((len2 = this.size - end) > 0) {
            int destPos = start - 1;
            System.arraycopy(this.tags, end, this.tags, destPos, len2);
            System.arraycopy(this.vrs, end, this.vrs, destPos, len2);
            System.arraycopy(this.values, end, this.values, destPos, len2);
        }
        int removed = end - start;
        int size1 = this.size - removed - 1;
        Arrays.fill(this.tags, size1, this.size, 0);
        Arrays.fill((Object[])this.vrs, size1, this.size, null);
        Arrays.fill(this.values, size1, this.size, null);
        this.size = size1;
        return removed;
    }

    public int removePrivateAttributes() {
        this.ensureModifiable();
        int size1 = this.size;
        for (int i = 0; i < size1; ++i) {
            int j;
            for (j = i; j < size1 && TagUtils.isPrivateGroup(this.tags[j]); ++j) {
            }
            if (j <= i) continue;
            int len = size1 - j;
            if (len > 0) {
                System.arraycopy(this.tags, j, this.tags, i, len);
                System.arraycopy(this.vrs, j, this.vrs, i, len);
                System.arraycopy(this.values, j, this.values, i, len);
            }
            size1 -= j - i;
        }
        int removed = this.size - size1;
        if (removed > 0) {
            Arrays.fill(this.tags, size1, this.size, 0);
            Arrays.fill((Object[])this.vrs, size1, this.size, null);
            Arrays.fill(this.values, size1, this.size, null);
            this.size = size1;
        }
        return removed;
    }

    public void removeSelected(int ... selection) {
        this.ensureModifiable();
        for (int i = 0; i < this.size; ++i) {
            if (Arrays.binarySearch(selection, this.tags[i]) < 0) continue;
            int numMoved = this.size - i - 1;
            if (numMoved > 0) {
                System.arraycopy(this.tags, i + 1, this.tags, i, numMoved);
                System.arraycopy(this.vrs, i + 1, this.vrs, i, numMoved);
                System.arraycopy(this.values, i + 1, this.values, i, numMoved);
            }
            this.values[--this.size] = null;
            --i;
        }
    }

    public void replaceSelected(Attributes others, int ... selection) {
        this.ensureModifiable();
        for (int i = 0; i < this.size; ++i) {
            if (Arrays.binarySearch(selection, this.tags[i]) < 0) continue;
            this.values[i] = StringUtils.maskNull(others.getValue(this.tags[i]), Value.NULL);
        }
    }

    public void replaceUIDSelected(int ... selection) {
        this.ensureModifiable();
        for (int i = 0; i < this.size; ++i) {
            if (Arrays.binarySearch(selection, this.tags[i]) < 0 || this.values[i] == Value.NULL) continue;
            this.values[i] = this.replaceUIDs(this.decodeStringValue(i));
        }
    }

    private Object replaceUIDs(Object val) {
        if (val instanceof String) {
            return UIDUtils.remapUID((String)val);
        }
        if (val instanceof String[]) {
            String[] ss = (String[])val;
            for (int i = 0; i < ss.length; ++i) {
                ss[i] = UIDUtils.remapUID(ss[i]);
            }
        }
        return val;
    }

    public int removeCurveData() {
        this.ensureModifiable();
        return this.removeRepeatingGroup(0x50000000);
    }

    public int removeOverlayData() {
        this.ensureModifiable();
        return this.removeRepeatingGroup(0x60000000);
    }

    private int removeRepeatingGroup(int ggxxxxxx) {
        int size1 = this.size;
        int i = this.indexForInsertOf(ggxxxxxx);
        if (i < 0) {
            i = -i - 1;
        }
        while (i < size1 && (this.tags[i] & 0xFFE00000) == ggxxxxxx) {
            int j;
            for (j = i; j < size1 && (this.tags[j] & 0xFFE10000) == ggxxxxxx; ++j) {
            }
            if (j > i) {
                int len = size1 - j;
                if (len > 0) {
                    System.arraycopy(this.tags, j, this.tags, i, len);
                    System.arraycopy(this.vrs, j, this.vrs, i, len);
                    System.arraycopy(this.values, j, this.values, i, len);
                }
                size1 -= j - i;
            }
            ++i;
        }
        int removed = this.size - size1;
        if (removed > 0) {
            Arrays.fill(this.tags, size1, this.size, 0);
            Arrays.fill((Object[])this.vrs, size1, this.size, null);
            Arrays.fill(this.values, size1, this.size, null);
            this.size = size1;
        }
        return removed;
    }

    public static enum UpdatePolicy {
        SUPPLEMENT,
        MERGE,
        OVERWRITE,
        REPLACE,
        PRESERVE;

    }

    public static interface Visitor {
        public boolean visit(Attributes var1, int var2, VR var3, Object var4) throws Exception;
    }

    public static interface SequenceVisitor
    extends Visitor {
        public void startItem(int var1, int var2);

        public void endItem();

        default public void startSequence(int sqTag) {
        }

        default public void endSequence() {
        }
    }

    public static abstract class ItemPointerVisitor
    implements SequenceVisitor {
        protected final List<ItemPointer> itemPointers = new ArrayList<ItemPointer>(4);

        @Override
        public void startItem(int sqTag, int itemIndex) {
            this.itemPointers.add(new ItemPointer(sqTag, itemIndex));
        }

        @Override
        public void endItem() {
            this.itemPointers.remove(this.itemPointers.size() - 1);
        }
    }
}

