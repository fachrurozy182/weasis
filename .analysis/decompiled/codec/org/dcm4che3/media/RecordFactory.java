/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.media;

import java.io.IOException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.ContentHandlerAdapter;
import org.dcm4che3.io.SAXParserFactoryHolder;
import org.dcm4che3.media.RecordType;
import org.dcm4che3.util.ResourceLocator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class RecordFactory {
    private static final int IN_USE = 65535;
    private EnumMap<RecordType, int[]> recordKeys;
    private HashMap<String, RecordType> recordTypes;
    private HashMap<String, String> privateRecordUIDs;
    private HashMap<String, int[]> privateRecordKeys;

    private void lazyLoadDefaultConfiguration() {
        if (this.recordTypes == null) {
            this.loadDefaultConfiguration();
        }
    }

    public void loadDefaultConfiguration() {
        try {
            this.loadConfiguration(ResourceLocator.getResource("org/dcm4che3/media/RecordFactory.xml", this.getClass()));
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void loadConfiguration(String uri) throws ParserConfigurationException, SAXException, IOException {
        Attributes attrs = this.parseXML(uri);
        Sequence sq = attrs.getSequence(266784);
        if (sq == null) {
            throw new IllegalArgumentException("Missing Directory Record Sequence in " + uri);
        }
        EnumMap<RecordType, int[]> recordKeys = new EnumMap<RecordType, int[]>(RecordType.class);
        HashMap<String, RecordType> recordTypes = new HashMap<String, RecordType>(134);
        HashMap<String, String> privateRecordUIDs = new HashMap<String, String>();
        HashMap<String, int[]> privateRecordKeys = new HashMap<String, int[]>();
        for (Attributes item : sq) {
            RecordType type = RecordType.forCode(item.getString(267312, null));
            String privuid = type == RecordType.PRIVATE ? item.getString(267314, null) : null;
            String[] cuids = item.getStrings(267536);
            if (cuids != null) {
                if (type != RecordType.PRIVATE) {
                    for (String cuid : cuids) {
                        recordTypes.put(cuid, type);
                    }
                } else if (privuid != null) {
                    for (String cuid : cuids) {
                        privateRecordUIDs.put(cuid, privuid);
                    }
                }
            }
            item.remove(267312);
            item.remove(267314);
            item.remove(267536);
            int[] keys = item.tags();
            if (privuid != null) {
                if (privateRecordKeys.put(privuid, keys) == null) continue;
                throw new IllegalArgumentException("Duplicate Private Record UID: " + privuid);
            }
            if (recordKeys.put(type, keys) == null) continue;
            throw new IllegalArgumentException("Duplicate Record Type: " + (Object)((Object)type));
        }
        EnumSet<RecordType> missingTypes = EnumSet.allOf(RecordType.class);
        missingTypes.removeAll(recordKeys.keySet());
        if (!missingTypes.isEmpty()) {
            throw new IllegalArgumentException("Missing Record Types: " + missingTypes);
        }
        this.recordTypes = recordTypes;
        this.recordKeys = recordKeys;
        this.privateRecordUIDs = privateRecordUIDs;
        this.privateRecordKeys = privateRecordKeys;
    }

    private Attributes parseXML(String uri) throws ParserConfigurationException, SAXException, IOException {
        Attributes attrs = new Attributes();
        SAXParser parser = SAXParserFactoryHolder.factory.newSAXParser();
        parser.parse(uri, (DefaultHandler)new ContentHandlerAdapter(attrs));
        return attrs;
    }

    public RecordType getRecordType(String cuid) {
        if (cuid == null) {
            throw new NullPointerException();
        }
        this.lazyLoadDefaultConfiguration();
        RecordType recordType = this.recordTypes.get(cuid);
        return recordType != null ? recordType : RecordType.PRIVATE;
    }

    public RecordType setRecordType(String cuid, RecordType type) {
        if (cuid == null || type == null) {
            throw new NullPointerException();
        }
        this.lazyLoadDefaultConfiguration();
        return this.recordTypes.put(cuid, type);
    }

    public void setRecordKeys(RecordType type, int[] keys) {
        if (type == null) {
            throw new NullPointerException();
        }
        int[] tmp = (int[])keys.clone();
        Arrays.sort(tmp);
        this.lazyLoadDefaultConfiguration();
        this.recordKeys.put(type, keys);
    }

    public int[] getRecordKeys(RecordType type) {
        this.lazyLoadDefaultConfiguration();
        return this.recordKeys.get((Object)type);
    }

    public String getPrivateRecordUID(String cuid) {
        if (cuid == null) {
            throw new NullPointerException();
        }
        this.lazyLoadDefaultConfiguration();
        String uid = this.privateRecordUIDs.get(cuid);
        return uid != null ? uid : cuid;
    }

    public String setPrivateRecordUID(String cuid, String uid) {
        if (cuid == null || uid == null) {
            throw new NullPointerException();
        }
        this.lazyLoadDefaultConfiguration();
        return this.privateRecordUIDs.put(cuid, uid);
    }

    public int[] setPrivateRecordKeys(String uid, int[] keys) {
        if (uid == null) {
            throw new NullPointerException();
        }
        int[] tmp = (int[])keys.clone();
        Arrays.sort(tmp);
        this.lazyLoadDefaultConfiguration();
        return this.privateRecordKeys.put(uid, tmp);
    }

    public Attributes createRecord(Attributes dataset, Attributes fmi, String[] fileIDs) {
        RecordType type;
        String cuid = fmi.getString(131074, null);
        return this.createRecord(type, (type = this.getRecordType(cuid)) == RecordType.PRIVATE ? this.getPrivateRecordUID(cuid) : null, dataset, fmi, fileIDs);
    }

    public Attributes createRecord(RecordType type, String privRecUID, Attributes dataset, Attributes fmi, String[] fileIDs) {
        if (type == null) {
            throw new NullPointerException("type");
        }
        if (dataset == null) {
            throw new NullPointerException("dataset");
        }
        this.lazyLoadDefaultConfiguration();
        int[] keys = null;
        if (type == RecordType.PRIVATE) {
            if (privRecUID == null) {
                throw new NullPointerException("privRecUID must not be null for type = PRIVATE");
            }
            keys = this.privateRecordKeys.get(privRecUID);
        } else if (privRecUID != null) {
            throw new IllegalArgumentException("privRecUID must be null for type != PRIVATE");
        }
        if (keys == null) {
            keys = this.recordKeys.get((Object)type);
        }
        Attributes rec = new Attributes(keys.length + (fileIDs != null ? 9 : 5));
        rec.setInt(267264, VR.UL, 0);
        rec.setInt(267280, VR.US, 65535);
        rec.setInt(267296, VR.UL, 0);
        rec.setString(267312, VR.CS, type.code());
        if (privRecUID != null) {
            rec.setString(267314, VR.UI, privRecUID);
        }
        if (fileIDs != null) {
            rec.setString(267520, VR.CS, fileIDs);
            rec.setString(267536, VR.UI, fmi.getString(131074, null));
            rec.setString(267537, VR.UI, fmi.getString(131075, null));
            rec.setString(267538, VR.UI, fmi.getString(131088, null));
        }
        rec.addSelected(dataset, keys, 0, keys.length);
        Sequence contentSeq = dataset.getSequence(4237104);
        if (contentSeq != null) {
            this.copyConceptMod(contentSeq, rec);
        }
        return rec;
    }

    private void copyConceptMod(Sequence srcSeq, Attributes rec) {
        Sequence dstSeq = null;
        for (Attributes item : srcSeq) {
            if (!"HAS CONCEPT MOD".equals(item.getString(4235280, null))) continue;
            if (dstSeq == null) {
                dstSeq = rec.newSequence(4237104, 1);
            }
            dstSeq.add(new Attributes(item, false));
        }
    }
}

