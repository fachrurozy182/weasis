/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package org.dcm4che3.net.service;

import java.util.EnumSet;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.VR;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.util.StringUtils;
import org.dcm4che3.util.TagUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum QueryRetrieveLevel2 {
    PATIENT(0x100020, VR.LO),
    STUDY(0x20000D, VR.UI),
    SERIES(0x20000E, VR.UI),
    IMAGE(524312, VR.UI);

    private static Logger LOG;
    private static ElementDictionary DICT;
    private final int uniqueKey;
    private final VR vrOfUniqueKey;

    private QueryRetrieveLevel2(int uniqueKey, VR vrOfUniqueKey) {
        this.uniqueKey = uniqueKey;
        this.vrOfUniqueKey = vrOfUniqueKey;
    }

    public int uniqueKey() {
        return this.uniqueKey;
    }

    public VR vrOfUniqueKey() {
        return this.vrOfUniqueKey;
    }

    public static QueryRetrieveLevel2 validateQueryIdentifier(Attributes keys, EnumSet<QueryRetrieveLevel2> levels, boolean relational) throws DicomServiceException {
        return QueryRetrieveLevel2.validateIdentifier(keys, levels, relational, false, true);
    }

    public static QueryRetrieveLevel2 validateQueryIdentifier(Attributes keys, EnumSet<QueryRetrieveLevel2> levels, boolean relational, boolean lenient) throws DicomServiceException {
        return QueryRetrieveLevel2.validateIdentifier(keys, levels, relational, lenient, true);
    }

    public static QueryRetrieveLevel2 validateRetrieveIdentifier(Attributes keys, EnumSet<QueryRetrieveLevel2> levels, boolean relational) throws DicomServiceException {
        return QueryRetrieveLevel2.validateIdentifier(keys, levels, relational, false, false);
    }

    public static QueryRetrieveLevel2 validateRetrieveIdentifier(Attributes keys, EnumSet<QueryRetrieveLevel2> levels, boolean relational, boolean lenient) throws DicomServiceException {
        return QueryRetrieveLevel2.validateIdentifier(keys, levels, relational, lenient, false);
    }

    private static QueryRetrieveLevel2 validateIdentifier(Attributes keys, EnumSet<QueryRetrieveLevel2> levels, boolean relational, boolean lenient, boolean query) throws DicomServiceException {
        QueryRetrieveLevel2 level;
        String value = keys.getString(524370);
        if (value == null) {
            throw QueryRetrieveLevel2.missingAttribute(524370);
        }
        try {
            level = QueryRetrieveLevel2.valueOf(value);
        }
        catch (IllegalArgumentException e) {
            throw QueryRetrieveLevel2.invalidAttributeValue(524370, value);
        }
        if (!levels.contains((Object)level)) {
            throw QueryRetrieveLevel2.invalidAttributeValue(524370, value);
        }
        for (QueryRetrieveLevel2 level2 : levels) {
            if (level2 == level) {
                level.checkUniqueKey(keys, query, false, level != PATIENT);
                break;
            }
            level2.checkUniqueKey(keys, relational, lenient, false);
        }
        return level;
    }

    private void checkUniqueKey(Attributes keys, boolean optional, boolean lenient, boolean multiple) throws DicomServiceException {
        String[] ids = keys.getStrings(this.uniqueKey);
        if (!multiple && ids != null && ids.length > 1) {
            throw QueryRetrieveLevel2.invalidAttributeValue(this.uniqueKey, StringUtils.concat(ids, '\\'));
        }
        if (!(ids != null && ids.length != 0 && ids[0].indexOf(42) < 0 && ids[0].indexOf(63) < 0 || optional)) {
            if (lenient) {
                LOG.info("Missing or wildcard " + DICT.keywordOf(this.uniqueKey) + " " + TagUtils.toString(this.uniqueKey) + " in Query/Retrieve Identifier");
            } else {
                throw ids == null || ids.length == 0 ? QueryRetrieveLevel2.missingAttribute(this.uniqueKey) : QueryRetrieveLevel2.invalidAttributeValue(this.uniqueKey, ids[0]);
            }
        }
    }

    private static DicomServiceException missingAttribute(int tag) {
        return QueryRetrieveLevel2.identifierDoesNotMatchSOPClass("Missing " + DICT.keywordOf(tag) + " " + TagUtils.toString(tag), tag);
    }

    private static DicomServiceException invalidAttributeValue(int tag, String value) {
        return QueryRetrieveLevel2.identifierDoesNotMatchSOPClass("Invalid " + DICT.keywordOf(tag) + " " + TagUtils.toString(tag) + " - " + value, tag);
    }

    private static DicomServiceException identifierDoesNotMatchSOPClass(String comment, int tag) {
        return new DicomServiceException(43264, comment).setOffendingElements(tag);
    }

    static {
        LOG = LoggerFactory.getLogger(QueryRetrieveLevel2.class);
        DICT = ElementDictionary.getStandardElementDictionary();
    }
}

