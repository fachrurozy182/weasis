/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.json.stream.JsonParser
 *  jakarta.json.stream.JsonParser$Event
 *  jakarta.json.stream.JsonParsingException
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package org.dcm4che3.json;

import jakarta.json.stream.JsonParser;
import jakarta.json.stream.JsonParsingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Objects;
import java.util.function.ToLongFunction;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.Fragments;
import org.dcm4che3.data.PersonName;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.VR;
import org.dcm4che3.util.Base64;
import org.dcm4che3.util.TagUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSONReader {
    private static final Logger LOG = LoggerFactory.getLogger(JSONReader.class);
    private final JsonParser parser;
    private boolean skipBulkDataURI;
    private BulkData.Creator bulkDataCreator = BulkData::new;
    private Attributes fmi;
    private JsonParser.Event event;
    private String s;
    private int level = -1;
    private final ByteArrayOutputStream bout = new ByteArrayOutputStream(64);
    private final EnumMap<PersonName.Group, String> pnGroups = new EnumMap(PersonName.Group.class);

    public JSONReader(JsonParser parser) {
        this.parser = Objects.requireNonNull(parser);
    }

    public boolean isSkipBulkDataURI() {
        return this.skipBulkDataURI;
    }

    public void setSkipBulkDataURI(boolean skipBulkDataURI) {
        this.skipBulkDataURI = skipBulkDataURI;
    }

    public void setBulkDataCreator(BulkData.Creator bulkDataCreator) {
        this.bulkDataCreator = Objects.requireNonNull(bulkDataCreator);
    }

    public Attributes getFileMetaInformation() {
        return this.fmi;
    }

    private JsonParser.Event next() {
        this.s = null;
        this.event = this.parser.next();
        return this.event;
    }

    private String getString() {
        if (this.s == null) {
            this.s = this.parser.getString();
        }
        return this.s;
    }

    private void expect(JsonParser.Event expected) {
        if (this.event != expected) {
            throw new JsonParsingException("Unexpected " + this.event + ", expected " + expected, this.parser.getLocation());
        }
    }

    private String valueString() {
        this.next();
        this.expect(JsonParser.Event.VALUE_STRING);
        return this.getString();
    }

    public Attributes readDataset(Attributes attrs) {
        boolean wrappedInArray;
        boolean bl = wrappedInArray = this.next() == JsonParser.Event.START_ARRAY;
        if (wrappedInArray) {
            this.next();
        }
        this.expect(JsonParser.Event.START_OBJECT);
        if (attrs == null) {
            attrs = new Attributes();
        }
        this.fmi = null;
        this.next();
        this.doReadDataset(attrs);
        if (wrappedInArray) {
            this.next();
        }
        return attrs;
    }

    public void readDatasets(Callback callback) {
        this.next();
        this.expect(JsonParser.Event.START_ARRAY);
        while (this.next() == JsonParser.Event.START_OBJECT) {
            this.fmi = null;
            Attributes attrs = new Attributes();
            this.next();
            this.doReadDataset(attrs);
            callback.onDataset(this.fmi, attrs);
        }
        this.expect(JsonParser.Event.END_ARRAY);
    }

    private Attributes doReadDataset(Attributes attrs) {
        ++this.level;
        while (this.event == JsonParser.Event.KEY_NAME) {
            this.readAttribute(attrs);
            this.next();
        }
        this.expect(JsonParser.Event.END_OBJECT);
        attrs.trimToSize();
        --this.level;
        return attrs;
    }

    private void readAttribute(Attributes attrs) {
        int tag = (int)Long.parseLong(this.getString(), 16);
        if (this.level == 0 && TagUtils.isFileMetaInformation(tag)) {
            if (this.fmi == null) {
                this.fmi = new Attributes();
            }
            attrs = this.fmi;
        }
        this.next();
        this.expect(JsonParser.Event.START_OBJECT);
        Element el = new Element();
        block25: while (this.next() == JsonParser.Event.KEY_NAME) {
            switch (this.getString()) {
                case "vr": {
                    try {
                        el.vr = VR.valueOf(this.valueString());
                    }
                    catch (IllegalArgumentException e) {
                        el.vr = ElementDictionary.getStandardElementDictionary().vrOf(tag);
                        LOG.info("Invalid vr: '{}' at {} - treat as '{}'", new Object[]{this.getString(), this.parser.getLocation(), el.vr});
                    }
                    continue block25;
                }
                case "Value": {
                    el.values = this.readValues();
                    continue block25;
                }
                case "InlineBinary": {
                    el.bytes = this.readInlineBinary();
                    continue block25;
                }
                case "BulkDataURI": {
                    el.bulkDataURI = this.valueString();
                    continue block25;
                }
                case "DataFragment": {
                    el.values = this.readDataFragments();
                    continue block25;
                }
            }
            throw new JsonParsingException("Unexpected \"" + this.getString() + "\", expected \"Value\" or \"InlineBinary\" or \"BulkDataURI\" or  \"DataFragment\"", this.parser.getLocation());
        }
        this.expect(JsonParser.Event.END_OBJECT);
        if (el.vr == null) {
            el.vr = ElementDictionary.getStandardElementDictionary().vrOf(tag);
            if (el.vr == null) {
                el.vr = VR.UN;
            }
            LOG.info("Missing property: vr at {} - treat as '{}'", (Object)this.parser.getLocation(), (Object)el.vr);
        }
        if (el.isEmpty()) {
            attrs.setNull(tag, el.vr);
        } else if (el.bulkDataURI != null) {
            if (!this.skipBulkDataURI) {
                attrs.setValue(tag, el.vr, this.bulkDataCreator.create(null, el.bulkDataURI, false));
            }
        } else {
            switch (el.vr) {
                case AE: 
                case AS: 
                case AT: 
                case CS: 
                case DA: 
                case DS: 
                case DT: 
                case LO: 
                case LT: 
                case PN: 
                case IS: 
                case SH: 
                case ST: 
                case TM: 
                case UC: 
                case UI: 
                case UR: 
                case UT: {
                    attrs.setString(tag, el.vr, el.toStrings());
                    break;
                }
                case FL: 
                case FD: {
                    attrs.setDouble(tag, el.vr, el.toDoubles());
                    break;
                }
                case SL: 
                case SS: 
                case UL: 
                case US: {
                    attrs.setInt(tag, el.vr, el.toInts());
                    break;
                }
                case SV: {
                    attrs.setLong(tag, el.vr, el.toLongs(Long::parseLong));
                    break;
                }
                case UV: {
                    attrs.setLong(tag, el.vr, el.toLongs(Long::parseUnsignedLong));
                    break;
                }
                case SQ: {
                    el.toItems(attrs.newSequence(tag, el.values.size()));
                    break;
                }
                case OB: 
                case OD: 
                case OF: 
                case OL: 
                case OV: 
                case OW: 
                case UN: {
                    if (el.bytes != null) {
                        attrs.setBytes(tag, el.vr, el.bytes);
                        break;
                    }
                    el.toFragments(attrs.newFragments(tag, el.vr, el.values.size()));
                }
            }
        }
    }

    private List<Object> readValues() {
        ArrayList<Object> list = new ArrayList<Object>();
        this.next();
        if (this.event == JsonParser.Event.VALUE_STRING) {
            LOG.info("Missing value array at {} - treat as single value", (Object)this.parser.getLocation());
            list.add(this.getString());
            return list;
        }
        this.expect(JsonParser.Event.START_ARRAY);
        block6: while (this.next() != JsonParser.Event.END_ARRAY) {
            switch (this.event) {
                case START_OBJECT: {
                    list.add(this.readItemOrPersonName());
                    continue block6;
                }
                case VALUE_STRING: {
                    list.add(this.parser.getString());
                    continue block6;
                }
                case VALUE_NUMBER: {
                    list.add(this.parser.getBigDecimal());
                    continue block6;
                }
                case VALUE_NULL: {
                    list.add(null);
                    continue block6;
                }
            }
            throw new JsonParsingException("Unexpected " + this.event, this.parser.getLocation());
        }
        return list;
    }

    private List<Object> readDataFragments() {
        ArrayList<Object> list = new ArrayList<Object>();
        this.next();
        this.expect(JsonParser.Event.START_ARRAY);
        block4: while (this.next() != JsonParser.Event.END_ARRAY) {
            switch (this.event) {
                case START_OBJECT: {
                    list.add(this.readDataFragment());
                    continue block4;
                }
                case VALUE_NULL: {
                    list.add(null);
                    continue block4;
                }
            }
            throw new JsonParsingException("Unexpected " + this.event, this.parser.getLocation());
        }
        return list;
    }

    private Object readItemOrPersonName() {
        if (this.next() != JsonParser.Event.KEY_NAME) {
            return null;
        }
        return this.getString().length() == 8 ? this.doReadDataset(new Attributes()) : this.readPersonName();
    }

    private String readPersonName() {
        this.pnGroups.clear();
        while (this.event == JsonParser.Event.KEY_NAME) {
            PersonName.Group key;
            try {
                key = PersonName.Group.valueOf(this.getString());
            }
            catch (IllegalArgumentException e) {
                throw new JsonParsingException("Unexpected \"" + this.getString() + "\", expected \"Alphabetic\" or \"Ideographic\" or \"Phonetic\"", this.parser.getLocation());
            }
            this.pnGroups.put(key, this.valueString());
            this.next();
        }
        this.expect(JsonParser.Event.END_OBJECT);
        String s = this.pnGroups.get((Object)PersonName.Group.Alphabetic);
        if (s != null && this.pnGroups.size() == 1) {
            return s;
        }
        StringBuilder sb = new StringBuilder(64);
        if (s != null) {
            sb.append(s);
        }
        sb.append('=');
        s = this.pnGroups.get((Object)PersonName.Group.Ideographic);
        if (s != null) {
            sb.append(s);
        }
        if ((s = this.pnGroups.get((Object)PersonName.Group.Phonetic)) != null) {
            sb.append('=').append(s);
        }
        return sb.toString();
    }

    private byte[] readInlineBinary() {
        char[] base64 = this.valueString().toCharArray();
        this.bout.reset();
        try {
            Base64.decode(base64, 0, base64.length, this.bout);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this.bout.toByteArray();
    }

    private Object readDataFragment() {
        Object bytes = null;
        String bulkDataURI = null;
        block8: while (this.next() == JsonParser.Event.KEY_NAME) {
            switch (this.getString()) {
                case "BulkDataURI": {
                    bulkDataURI = this.valueString();
                    continue block8;
                }
                case "InlineBinary": {
                    bytes = this.readInlineBinary();
                    continue block8;
                }
            }
            throw new JsonParsingException("Unexpected \"" + this.getString() + "\", expected \"InlineBinary\" or \"BulkDataURI\"", this.parser.getLocation());
        }
        this.expect(JsonParser.Event.END_OBJECT);
        return bulkDataURI != null && !this.skipBulkDataURI ? new BulkData(null, bulkDataURI, false) : bytes;
    }

    public static interface Callback {
        public void onDataset(Attributes var1, Attributes var2);
    }

    private static class Element {
        VR vr;
        List<Object> values;
        byte[] bytes;
        String bulkDataURI;

        private Element() {
        }

        boolean isEmpty() {
            return !(this.values != null && !this.values.isEmpty() || this.bytes != null && this.bytes.length != 0 || this.bulkDataURI != null);
        }

        String[] toStrings() {
            String[] ss = new String[this.values.size()];
            for (int i = 0; i < ss.length; ++i) {
                Object value = this.values.get(i);
                ss[i] = value != null ? value.toString() : null;
            }
            return ss;
        }

        double[] toDoubles() {
            double[] ds = new double[this.values.size()];
            for (int i = 0; i < ds.length; ++i) {
                double d;
                Number number = (Number)this.values.get(i);
                if (number == null) {
                    LOG.info("decode {} null as NaN", (Object)this.vr);
                    d = Double.NaN;
                } else {
                    d = number.doubleValue();
                    if (d == -1.7976931348623157E308) {
                        LOG.info("decode {} {} as -Infinity", (Object)this.vr, (Object)d);
                        d = Double.NEGATIVE_INFINITY;
                    } else if (d == Double.MAX_VALUE) {
                        LOG.info("decode {} {} as Infinity", (Object)this.vr, (Object)d);
                        d = Double.POSITIVE_INFINITY;
                    }
                }
                ds[i] = d;
            }
            return ds;
        }

        int[] toInts() {
            int[] is = new int[this.values.size()];
            for (int i = 0; i < is.length; ++i) {
                is[i] = ((Number)this.values.get(i)).intValue();
            }
            return is;
        }

        long[] toLongs(ToLongFunction<String> parse) {
            long[] ls = new long[this.values.size()];
            for (int i = 0; i < ls.length; ++i) {
                ls[i] = this.longValueOf(parse, this.values.get(i));
            }
            return ls;
        }

        private long longValueOf(ToLongFunction<String> string2long, Object o) {
            return o instanceof Number ? ((Number)o).longValue() : string2long.applyAsLong((String)o);
        }

        void toItems(Sequence seq) {
            for (Object value : this.values) {
                seq.add(value != null ? (Attributes)value : new Attributes(0));
            }
        }

        void toFragments(Fragments fragments) {
            for (Object value : this.values) {
                fragments.add(value);
            }
        }
    }
}

