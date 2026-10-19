/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package org.dcm4che3.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Objects;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.Fragments;
import org.dcm4che3.data.PersonName;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.VR;
import org.dcm4che3.util.Base64;
import org.dcm4che3.util.ByteUtils;
import org.dcm4che3.util.StringUtils;
import org.dcm4che3.util.TagUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ContentHandlerAdapter
extends DefaultHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ContentHandlerAdapter.class);
    private BulkData.Creator bulkDataCreator = BulkData::new;
    private Attributes fmi;
    private boolean bigEndian;
    private final boolean lenient;
    private final LinkedList<Attributes> items = new LinkedList();
    private final LinkedList<Sequence> seqs = new LinkedList();
    private final ByteArrayOutputStream bout = new ByteArrayOutputStream(64);
    private final char[] carry = new char[4];
    private int carryLen;
    private final StringBuilder sb = new StringBuilder(64);
    private final ArrayList<String> values = new ArrayList();
    private PersonName pn;
    private PersonName.Group pnGroup;
    private int tag;
    private String privateCreator;
    private VR vr;
    private BulkData bulkData;
    private Fragments dataFragments;
    private boolean processCharacters;
    private boolean inlineBinary;

    public ContentHandlerAdapter(Attributes attrs) {
        this(attrs, false);
    }

    public ContentHandlerAdapter(Attributes attrs, boolean lenient) {
        if (attrs != null) {
            this.items.add(attrs);
            this.bigEndian = attrs.bigEndian();
        }
        this.lenient = lenient;
    }

    public void setBulkDataCreator(BulkData.Creator bulkDataCreator) {
        this.bulkDataCreator = Objects.requireNonNull(bulkDataCreator);
    }

    public Attributes getFileMetaInformation() {
        return this.fmi;
    }

    public Attributes getDataset() {
        return this.items.getFirst();
    }

    @Override
    public void startElement(String uri, String localName, String qName, org.xml.sax.Attributes atts) throws SAXException {
        switch (qName) {
            case "DicomAttribute": {
                this.startDicomAttribute((int)Long.parseLong(atts.getValue("tag"), 16), atts.getValue("privateCreator"), atts.getValue("vr"));
                break;
            }
            case "Item": {
                this.startItem(Integer.parseInt(atts.getValue("number")));
                break;
            }
            case "DataFragment": {
                this.startDataFragment(Integer.parseInt(atts.getValue("number")));
                break;
            }
            case "InlineBinary": {
                this.startInlineBinary();
                break;
            }
            case "PersonName": {
                this.startPersonName(Integer.parseInt(atts.getValue("number")));
                break;
            }
            case "Alphabetic": {
                this.startPNGroup(PersonName.Group.Alphabetic);
                break;
            }
            case "Ideographic": {
                this.startPNGroup(PersonName.Group.Ideographic);
                break;
            }
            case "Phonetic": {
                this.startPNGroup(PersonName.Group.Phonetic);
                break;
            }
            case "Value": {
                this.startValue(Integer.parseInt(atts.getValue("number")));
                this.startText();
                break;
            }
            case "FamilyName": 
            case "GivenName": 
            case "Length": 
            case "MiddleName": 
            case "NamePrefix": 
            case "NameSuffix": 
            case "Offset": 
            case "TransferSyntax": 
            case "URI": {
                this.startText();
                break;
            }
            case "BulkData": {
                this.bulkData(atts.getValue("uuid"), atts.getValue("uri"));
            }
        }
    }

    private void bulkData(String uuid, String uri) {
        this.bulkData = this.bulkDataCreator.create(uuid, uri, this.items.getLast().bigEndian());
    }

    private void startInlineBinary() {
        this.processCharacters = true;
        this.inlineBinary = true;
        this.carryLen = 0;
        this.bout.reset();
    }

    private void startText() {
        this.processCharacters = true;
        this.inlineBinary = false;
        this.sb.setLength(0);
    }

    private void startDicomAttribute(int tag, String privateCreator, String vr) {
        this.tag = tag;
        this.privateCreator = privateCreator;
        VR vR = this.vr = vr != null ? VR.valueOf(vr) : ElementDictionary.vrOf(tag, privateCreator);
        if (this.vr == VR.SQ) {
            this.seqs.add(this.items.getLast().newSequence(privateCreator, tag, 10));
        }
    }

    private void startDataFragment(int number) {
        if (this.dataFragments == null) {
            this.dataFragments = this.items.getLast().newFragments(this.privateCreator, this.tag, this.vr, 10);
        }
        while (this.dataFragments.size() < number - 1) {
            this.dataFragments.add(ByteUtils.EMPTY_BYTES);
        }
    }

    private void startItem(int number) {
        Sequence seq = this.seqs.getLast();
        while (seq.size() < number - 1) {
            seq.add(new Attributes(this.bigEndian, 0));
        }
        Attributes item = new Attributes(this.bigEndian);
        seq.add(item);
        this.items.add(item);
    }

    private void startValue(int number) {
        while (this.values.size() < number - 1) {
            this.values.add(null);
        }
    }

    private void startPersonName(int number) {
        this.startValue(number);
        this.pn = new PersonName();
    }

    private void startPNGroup(PersonName.Group pnGroup) {
        this.pnGroup = pnGroup;
    }

    @Override
    public void characters(char[] ch, int offset, int len) throws SAXException {
        if (this.processCharacters) {
            if (this.inlineBinary) {
                try {
                    if (this.carryLen != 0) {
                        int copy = Math.min(4 - this.carryLen, len);
                        System.arraycopy(ch, offset, this.carry, this.carryLen, copy);
                        this.carryLen += copy;
                        offset += copy;
                        len -= copy;
                        if (this.carryLen == 4) {
                            Base64.decode(this.carry, 0, 4, this.bout);
                        } else {
                            return;
                        }
                    }
                    if ((this.carryLen = len & 3) != 0) {
                        System.arraycopy(ch, offset + (len -= this.carryLen), this.carry, 0, this.carryLen);
                    }
                    Base64.decode(ch, offset, len, this.bout);
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                this.sb.append(ch, offset, len);
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        switch (qName) {
            case "DicomAttribute": {
                this.endDicomAttribute();
                break;
            }
            case "Item": {
                this.endItem();
                break;
            }
            case "DataFragment": {
                this.endDataFragment();
                break;
            }
            case "PersonName": {
                this.endPersonName();
                break;
            }
            case "Value": {
                this.endValue();
                break;
            }
            case "FamilyName": {
                this.endPNComponent(PersonName.Component.FamilyName);
                break;
            }
            case "GivenName": {
                this.endPNComponent(PersonName.Component.GivenName);
                break;
            }
            case "MiddleName": {
                this.endPNComponent(PersonName.Component.MiddleName);
                break;
            }
            case "NamePrefix": {
                this.endPNComponent(PersonName.Component.NamePrefix);
                break;
            }
            case "NameSuffix": {
                this.endPNComponent(PersonName.Component.NameSuffix);
            }
        }
        this.processCharacters = false;
    }

    @Override
    public void endDocument() throws SAXException {
        if (this.fmi != null) {
            this.fmi.trimToSize();
        }
        this.items.getFirst().trimToSize();
    }

    private void endDataFragment() {
        if (this.bulkData != null) {
            this.dataFragments.add(this.bulkData);
            this.bulkData = null;
        } else {
            this.dataFragments.add(this.getBytes());
        }
    }

    private void endDicomAttribute() throws SAXException {
        if (this.vr == VR.SQ) {
            this.seqs.removeLast().trimToSize();
            return;
        }
        if (this.dataFragments != null) {
            this.dataFragments.trimToSize();
            this.dataFragments = null;
            return;
        }
        Attributes attrs = this.attrs();
        if (this.bulkData != null) {
            attrs.setValue(this.privateCreator, this.tag, this.vr, this.bulkData);
            this.bulkData = null;
        } else if (this.inlineBinary) {
            attrs.setBytes(this.privateCreator, this.tag, this.vr, this.getBytes());
            this.inlineBinary = false;
        } else {
            Object[] value = this.getStrings();
            try {
                attrs.setString(this.privateCreator, this.tag, this.vr, (String[])value);
            }
            catch (RuntimeException e) {
                String message = String.format("Invalid %s(%04X,%04X) %s %s", new Object[]{ContentHandlerAdapter.prefix(this.privateCreator, this.items.size() - 1), TagUtils.groupNumber(this.tag), TagUtils.elementNumber(this.tag), this.vr, Arrays.toString(value)});
                if (this.lenient) {
                    LOG.info("{} - ignored", (Object)message);
                }
                throw new SAXException(message, e);
            }
        }
    }

    private Attributes attrs() {
        if (TagUtils.isFileMetaInformation(this.tag)) {
            if (this.fmi == null) {
                this.fmi = new Attributes();
            }
            return this.fmi;
        }
        if (this.items.isEmpty()) {
            this.bigEndian = ContentHandlerAdapter.bigEndian(this.fmi);
            this.items.add(new Attributes(this.bigEndian));
        }
        return this.items.getLast();
    }

    private static boolean bigEndian(Attributes fmi) {
        return fmi != null && "1.2.840.10008.1.2.2".equals(fmi.getString(131088));
    }

    private static String prefix(String privateCreator, int level) {
        if (privateCreator == null && level == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        if (privateCreator != null) {
            sb.append(privateCreator).append(':');
        }
        for (int i = 0; i < level; ++i) {
            sb.append('>');
        }
        return sb.toString();
    }

    private void endItem() {
        this.items.removeLast().trimToSize();
        this.vr = VR.SQ;
    }

    private void endPersonName() {
        this.values.add(this.pn.toString());
        this.pn = null;
    }

    private void endValue() {
        this.values.add(this.getString());
    }

    private void endPNComponent(PersonName.Component pnComp) {
        this.pn.set(this.pnGroup, pnComp, this.getString());
    }

    private String getString() {
        return this.sb.toString();
    }

    private byte[] getBytes() {
        byte[] b = this.bout.toByteArray();
        return this.bigEndian ? this.vr.toggleEndian(b, false) : b;
    }

    private String[] getStrings() {
        try {
            String[] stringArray = this.values.toArray(StringUtils.EMPTY_STRING);
            return stringArray;
        }
        finally {
            this.values.clear();
        }
    }
}

