/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.json.JsonValue$ValueType
 *  jakarta.json.stream.JsonGenerator
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package org.dcm4che3.json;

import jakarta.json.JsonValue;
import jakarta.json.stream.JsonGenerator;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumMap;
import java.util.function.LongFunction;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.Fragments;
import org.dcm4che3.data.PersonName;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.SpecificCharacterSet;
import org.dcm4che3.data.VR;
import org.dcm4che3.data.Value;
import org.dcm4che3.io.DicomInputHandler;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.util.Base64;
import org.dcm4che3.util.StringUtils;
import org.dcm4che3.util.TagUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSONWriter
implements DicomInputHandler {
    private static final Logger LOG = LoggerFactory.getLogger(JSONWriter.class);
    private static final int DOUBLE_MAX_BITS = 53;
    private final JsonGenerator gen;
    private final Deque<Boolean> hasItems = new ArrayDeque<Boolean>();
    private String replaceBulkDataURI;
    private EnumMap<VR, JsonValue.ValueType> jsonTypeByVR = new EnumMap(VR.class);

    public void setJsonType(VR vr, JsonValue.ValueType valueType) {
        this.jsonTypeByVR.put(JSONWriter.requireIS_DS_SV_UV(vr), JSONWriter.requireNumberOrString(valueType));
    }

    private static VR requireIS_DS_SV_UV(VR vr) {
        if (vr != VR.DS && vr != VR.IS && vr != VR.SV && vr != VR.UV) {
            throw new IllegalArgumentException("vr:" + (Object)((Object)vr));
        }
        return vr;
    }

    private static JsonValue.ValueType requireNumberOrString(JsonValue.ValueType jsonType) {
        if (jsonType != JsonValue.ValueType.NUMBER && jsonType != JsonValue.ValueType.STRING) {
            throw new IllegalArgumentException("jsonType:" + jsonType);
        }
        return jsonType;
    }

    public JSONWriter(JsonGenerator gen) {
        this.gen = gen;
    }

    public String getReplaceBulkDataURI() {
        return this.replaceBulkDataURI;
    }

    public void setReplaceBulkDataURI(String replaceBulkDataURI) {
        this.replaceBulkDataURI = replaceBulkDataURI;
    }

    public void write(Attributes attrs) {
        this.gen.writeStartObject();
        this.writeAttributes(attrs);
        this.gen.writeEnd();
    }

    public void writeAttributes(Attributes attrs) {
        final SpecificCharacterSet cs = attrs.getSpecificCharacterSet();
        try {
            attrs.accept(new Attributes.Visitor(){

                @Override
                public boolean visit(Attributes attrs, int tag, VR vr, Object value) throws Exception {
                    JSONWriter.this.writeAttribute(tag, vr, value, cs, attrs);
                    return true;
                }
            }, false);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void writeAttribute(int tag, VR vr, Object value, SpecificCharacterSet cs, Attributes attrs) {
        if (TagUtils.isGroupLength(tag)) {
            return;
        }
        this.gen.writeStartObject(TagUtils.toHexString(tag));
        this.gen.write("vr", vr.name());
        if (value instanceof Value) {
            this.writeValue((Value)value, attrs.bigEndian());
        } else {
            this.writeValue(vr, value, attrs.bigEndian(), attrs.getSpecificCharacterSet(vr), true);
        }
        this.gen.writeEnd();
    }

    private void writeValue(Value value, boolean bigEndian) {
        if (value.isEmpty()) {
            return;
        }
        if (value instanceof Sequence) {
            this.gen.writeStartArray("Value");
            for (Attributes item : (Sequence)value) {
                this.write(item);
            }
            this.gen.writeEnd();
        } else if (value instanceof Fragments) {
            this.gen.writeStartArray("DataFragment");
            Fragments frags = (Fragments)value;
            for (Object frag : frags) {
                if (frag instanceof Value && ((Value)frag).isEmpty()) {
                    this.gen.writeNull();
                    continue;
                }
                this.gen.writeStartObject();
                if (frag instanceof BulkData) {
                    this.writeBulkData((BulkData)frag);
                } else {
                    this.writeInlineBinary(frags.vr(), (byte[])frag, bigEndian, true);
                }
                this.gen.writeEnd();
            }
            this.gen.writeEnd();
        } else if (value instanceof BulkData) {
            this.writeBulkData((BulkData)value);
        }
    }

    @Override
    public void readValue(DicomInputStream dis, Attributes attrs) throws IOException {
        int tag = dis.tag();
        VR vr = dis.vr();
        long len = dis.unsignedLength();
        if (TagUtils.isGroupLength(tag)) {
            dis.readValue(dis, attrs);
        } else if (dis.isExcludeBulkData()) {
            dis.readValue(dis, attrs);
        } else {
            this.gen.writeStartObject(TagUtils.toHexString(tag));
            this.gen.write("vr", vr.name());
            if (vr == VR.SQ || len == -1L) {
                this.hasItems.addLast(false);
                dis.readValue(dis, attrs);
                if (this.hasItems.removeLast().booleanValue()) {
                    this.gen.writeEnd();
                }
            } else if (len > 0L) {
                if (dis.isIncludeBulkDataURI()) {
                    this.writeBulkData(dis.createBulkData(dis));
                } else {
                    byte[] b = dis.readValue();
                    if (tag == 131088 || tag == 524293 || tag == 2621699 || TagUtils.isPrivateCreator(tag)) {
                        attrs.setBytes(tag, vr, b);
                    }
                    this.writeValue(vr, b, dis.bigEndian(), attrs.getSpecificCharacterSet(vr), false);
                }
            }
            this.gen.writeEnd();
        }
    }

    private void writeValue(VR vr, Object val, boolean bigEndian, SpecificCharacterSet cs, boolean preserve) {
        switch (vr) {
            case AE: 
            case AS: 
            case AT: 
            case CS: 
            case DA: 
            case DS: 
            case DT: 
            case IS: 
            case LO: 
            case LT: 
            case PN: 
            case SH: 
            case ST: 
            case TM: 
            case UC: 
            case UI: 
            case UR: 
            case UT: {
                this.writeStringValues(vr, val, bigEndian, cs);
                break;
            }
            case FL: 
            case FD: {
                this.writeDoubleValues(vr, val, bigEndian);
                break;
            }
            case SL: 
            case SS: 
            case US: {
                this.writeIntValues(vr, val, bigEndian);
                break;
            }
            case SV: {
                this.writeLongValues(Long::toString, vr, val, bigEndian);
                break;
            }
            case UV: {
                this.writeLongValues(Long::toUnsignedString, vr, val, bigEndian);
                break;
            }
            case UL: {
                this.writeUIntValues(vr, val, bigEndian);
                break;
            }
            case OB: 
            case OD: 
            case OF: 
            case OL: 
            case OV: 
            case OW: 
            case UN: {
                this.writeInlineBinary(vr, (byte[])val, bigEndian, preserve);
                break;
            }
        }
    }

    private void writeStringValues(VR vr, Object val, boolean bigEndian, SpecificCharacterSet cs) {
        String[] ss;
        String[] stringArray;
        this.gen.writeStartArray("Value");
        Object o = vr.toStrings(val, bigEndian, cs);
        if (o instanceof String[]) {
            stringArray = (String[])o;
        } else {
            String[] stringArray2 = new String[1];
            stringArray = stringArray2;
            stringArray2[0] = (String)o;
        }
        block7: for (String s : ss = stringArray) {
            if (s == null || s.isEmpty()) {
                this.gen.writeNull();
                continue;
            }
            switch (vr) {
                case DS: {
                    if (this.jsonTypeByVR.get((Object)VR.DS) == JsonValue.ValueType.NUMBER) {
                        try {
                            this.gen.write(StringUtils.parseDS(s));
                        }
                        catch (NumberFormatException e) {
                            LOG.info("illegal DS value: {} - encoded as string", (Object)s);
                            this.gen.write(s);
                        }
                        continue block7;
                    }
                    this.gen.write(s);
                    continue block7;
                }
                case IS: {
                    if (this.jsonTypeByVR.get((Object)VR.IS) == JsonValue.ValueType.NUMBER) {
                        this.writeNumber(s);
                        continue block7;
                    }
                    this.gen.write(s);
                    continue block7;
                }
                case PN: {
                    this.writePersonName(s);
                    continue block7;
                }
                default: {
                    this.gen.write(s);
                }
            }
        }
        this.gen.writeEnd();
    }

    private void writeNumber(String s) {
        try {
            long l = StringUtils.parseIS(s);
            if ((l < 0L ? -l : l) >> 53 == 0L) {
                this.gen.write(l);
                return;
            }
        }
        catch (NumberFormatException e) {
            LOG.info("illegal IS value: {} - encoded as string", (Object)s);
        }
        this.gen.write(s);
    }

    private void writeDoubleValues(VR vr, Object val, boolean bigEndian) {
        this.gen.writeStartArray("Value");
        int vm = vr.vmOf(val);
        for (int i = 0; i < vm; ++i) {
            double d = vr.toDouble(val, bigEndian, i, 0.0);
            if (Double.isNaN(d)) {
                LOG.info("encode {} NaN as null", (Object)vr);
                this.gen.writeNull();
                continue;
            }
            if (d == Double.POSITIVE_INFINITY) {
                d = Double.MAX_VALUE;
                LOG.info("encode {} Infinity as {}", (Object)vr, (Object)d);
            } else if (d == Double.NEGATIVE_INFINITY) {
                d = -1.7976931348623157E308;
                LOG.info("encode {} -Infinity as {}", (Object)vr, (Object)d);
            }
            this.gen.write(d);
        }
        this.gen.writeEnd();
    }

    private void writeIntValues(VR vr, Object val, boolean bigEndian) {
        this.gen.writeStartArray("Value");
        int vm = vr.vmOf(val);
        for (int i = 0; i < vm; ++i) {
            this.gen.write(vr.toInt(val, bigEndian, i, 0));
        }
        this.gen.writeEnd();
    }

    private void writeUIntValues(VR vr, Object val, boolean bigEndian) {
        this.gen.writeStartArray("Value");
        int vm = vr.vmOf(val);
        for (int i = 0; i < vm; ++i) {
            this.gen.write((long)vr.toInt(val, bigEndian, i, 0) & 0xFFFFFFFFL);
        }
        this.gen.writeEnd();
    }

    private void writeLongValues(LongFunction<String> toString, VR vr, Object val, boolean bigEndian) {
        this.gen.writeStartArray("Value");
        boolean asString = this.jsonTypeByVR.get((Object)vr) != JsonValue.ValueType.NUMBER;
        int vm = vr.vmOf(val);
        for (int i = 0; i < vm; ++i) {
            long l = vr.toLong(val, bigEndian, i, 0L);
            if (asString || (l < 0L ? vr == VR.UV || -l >> 53 > 0L : l >> 53 > 0L)) {
                this.gen.write(toString.apply(l));
                continue;
            }
            this.gen.write(l);
        }
        this.gen.writeEnd();
    }

    private void writePersonName(String s) {
        PersonName pn = new PersonName(s, true);
        this.gen.writeStartObject();
        this.writePNGroup("Alphabetic", pn, PersonName.Group.Alphabetic);
        this.writePNGroup("Ideographic", pn, PersonName.Group.Ideographic);
        this.writePNGroup("Phonetic", pn, PersonName.Group.Phonetic);
        this.gen.writeEnd();
    }

    private void writePNGroup(String name, PersonName pn, PersonName.Group group) {
        if (pn.contains(group)) {
            this.gen.write(name, pn.toString(group, true));
        }
    }

    private void writeInlineBinary(VR vr, byte[] b, boolean bigEndian, boolean preserve) {
        if (bigEndian) {
            b = vr.toggleEndian(b, preserve);
        }
        this.gen.write("InlineBinary", this.encodeBase64(b));
    }

    private String encodeBase64(byte[] b) {
        int len = b.length * 4 / 3 + 3 & 0xFFFFFFFC;
        char[] ch = new char[len];
        Base64.encode(b, 0, b.length, ch, 0);
        return new String(ch);
    }

    private void writeBulkData(BulkData blkdata) {
        this.gen.write("BulkDataURI", this.replaceBulkDataURI != null ? this.replaceBulkDataURI : blkdata.getURI());
    }

    @Override
    public void readValue(DicomInputStream dis, Sequence seq) throws IOException {
        if (!this.hasItems.getLast().booleanValue()) {
            this.gen.writeStartArray("Value");
            this.hasItems.removeLast();
            this.hasItems.addLast(true);
        }
        this.gen.writeStartObject();
        dis.readValue(dis, seq);
        this.gen.writeEnd();
    }

    @Override
    public void readValue(DicomInputStream dis, Fragments frags) throws IOException {
        int len = dis.length();
        if (dis.isExcludeBulkData()) {
            dis.skipFully(len);
            return;
        }
        if (!this.hasItems.getLast().booleanValue()) {
            this.gen.writeStartArray("DataFragment");
            this.hasItems.removeLast();
            this.hasItems.add(true);
        }
        if (len == 0) {
            this.gen.writeNull();
        } else {
            this.gen.writeStartObject();
            if (dis.isIncludeBulkDataURI()) {
                this.writeBulkData(dis.createBulkData(dis));
            } else {
                this.writeInlineBinary(frags.vr(), dis.readValue(), dis.bigEndian(), false);
            }
            this.gen.writeEnd();
        }
    }

    @Override
    public void startDataset(DicomInputStream dis) throws IOException {
        this.gen.writeStartObject();
    }

    @Override
    public void endDataset(DicomInputStream dis) throws IOException {
        this.gen.writeEnd();
    }
}

