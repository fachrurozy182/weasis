/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.io;

import java.io.IOException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.Fragments;
import org.dcm4che3.data.PersonName;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.SpecificCharacterSet;
import org.dcm4che3.data.VR;
import org.dcm4che3.data.Value;
import org.dcm4che3.io.DicomInputHandler;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.util.Base64;
import org.dcm4che3.util.ByteUtils;
import org.dcm4che3.util.TagUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class SAXWriter
implements DicomInputHandler {
    private static final String NAMESPACE = "http://dicom.nema.org/PS3.19/models/NativeDICOM";
    private static final int BASE64_CHUNK_LENGTH = 768;
    private static final int BUFFER_LENGTH = 1024;
    private boolean includeKeyword = true;
    private String namespace = "";
    private final ContentHandler ch;
    private final AttributesImpl atts = new AttributesImpl();
    private final char[] buffer = new char[1024];

    public SAXWriter(ContentHandler ch) {
        this.ch = ch;
    }

    public final boolean isIncludeKeyword() {
        return this.includeKeyword;
    }

    public final void setIncludeKeyword(boolean includeKeyword) {
        this.includeKeyword = includeKeyword;
    }

    public final boolean isIncludeNamespaceDeclaration() {
        return this.namespace == NAMESPACE;
    }

    public final void setIncludeNamespaceDeclaration(boolean includeNameSpaceDeclaration) {
        this.namespace = includeNameSpaceDeclaration ? NAMESPACE : "";
    }

    public void write(Attributes attrs) throws SAXException {
        this.startDocument();
        this.writeItem(attrs);
        this.endDocument();
    }

    private void writeItem(final Attributes item) throws SAXException {
        final SpecificCharacterSet cs = item.getSpecificCharacterSet();
        try {
            item.accept(new Attributes.Visitor(){

                @Override
                public boolean visit(Attributes attrs, int tag, VR vr, Object value) throws Exception {
                    SAXWriter.this.writeAttribute(tag, vr, value, cs, item);
                    return true;
                }
            }, false);
        }
        catch (SAXException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void startDataset(DicomInputStream dis) throws IOException {
        try {
            this.startDocument();
        }
        catch (SAXException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void endDataset(DicomInputStream dis) throws IOException {
        try {
            this.endDocument();
        }
        catch (SAXException e) {
            throw new IOException(e);
        }
    }

    private void startDocument() throws SAXException {
        this.ch.startDocument();
        this.atts.addAttribute("", "space", "xml:space", "NMTOKEN", "preserve");
        this.startElement("NativeDicomModel");
    }

    private void endDocument() throws SAXException {
        this.endElement("NativeDicomModel");
        this.ch.endDocument();
    }

    private void startElement(String name, String attrName, int attrValue) throws SAXException {
        this.startElement(name, attrName, Integer.toString(attrValue));
    }

    private void startElement(String name, String attrName, String attrValue) throws SAXException {
        this.addAttribute(attrName, attrValue);
        this.startElement(name);
    }

    private void startElement(String name) throws SAXException {
        this.ch.startElement(this.namespace, name, name, this.atts);
        this.atts.clear();
    }

    private void endElement(String name) throws SAXException {
        this.ch.endElement(this.namespace, name, name);
    }

    private void addAttribute(String name, String value) {
        this.atts.addAttribute(this.namespace, name, name, "NMTOKEN", value);
    }

    private void writeAttribute(int tag, VR vr, Object value, SpecificCharacterSet cs, Attributes attrs) throws SAXException {
        if (TagUtils.isGroupLength(tag) || TagUtils.isPrivateCreator(tag)) {
            return;
        }
        String privateCreator = attrs.getPrivateCreator(tag);
        this.addAttributes(tag, vr, privateCreator);
        this.startElement("DicomAttribute");
        if (value instanceof Value) {
            this.writeAttribute((Value)value, attrs.bigEndian());
        } else if (!vr.isInlineBinary()) {
            this.writeValues(vr, value, attrs.bigEndian(), attrs.getSpecificCharacterSet(vr));
        } else if (value instanceof byte[]) {
            this.writeInlineBinary(attrs.bigEndian() ? vr.toggleEndian((byte[])value, true) : (byte[])value);
        } else {
            throw new IllegalArgumentException("vr: " + (Object)((Object)vr) + ", value class: " + value.getClass());
        }
        this.endElement("DicomAttribute");
    }

    private void writeAttribute(Value value, boolean bigEndian) throws SAXException {
        if (value.isEmpty()) {
            return;
        }
        if (value instanceof Sequence) {
            Sequence seq = (Sequence)value;
            int number = 0;
            for (Attributes item : seq) {
                this.startElement("Item", "number", ++number);
                this.writeItem(item);
                this.endElement("Item");
            }
        } else if (value instanceof Fragments) {
            Fragments frags = (Fragments)value;
            int number = 0;
            for (Object frag : frags) {
                ++number;
                if (frag instanceof Value && ((Value)frag).isEmpty()) continue;
                this.startElement("DataFragment", "number", number);
                if (frag instanceof BulkData) {
                    this.writeBulkData((BulkData)frag);
                } else {
                    byte[] b = (byte[])frag;
                    if (bigEndian) {
                        frags.vr().toggleEndian(b, true);
                    }
                    this.writeInlineBinary(b);
                }
                this.endElement("DataFragment");
            }
        } else if (value instanceof BulkData) {
            this.writeBulkData((BulkData)value);
        }
    }

    @Override
    public void readValue(DicomInputStream dis, Attributes attrs) throws IOException {
        int tag = dis.tag();
        VR vr = dis.vr();
        long len = dis.unsignedLength();
        if (TagUtils.isGroupLength(tag) || TagUtils.isPrivateCreator(tag)) {
            dis.readValue(dis, attrs);
        } else if (dis.isExcludeBulkData()) {
            if (len == -1L) {
                dis.readValue(dis, attrs);
            } else {
                dis.skipFully(len);
            }
        } else {
            try {
                String privateCreator = attrs.getPrivateCreator(tag);
                this.addAttributes(tag, vr, privateCreator);
                this.startElement("DicomAttribute");
                if (vr == VR.SQ || len == -1L) {
                    dis.readValue(dis, attrs);
                } else if (len > 0L) {
                    if (dis.isIncludeBulkDataURI()) {
                        this.writeBulkData(dis.createBulkData(dis));
                    } else {
                        byte[] b = dis.readValue();
                        if (tag == 131088 || tag == 524293 || tag == 2621699) {
                            attrs.setBytes(tag, vr, b);
                        }
                        if (vr.isInlineBinary()) {
                            this.writeInlineBinary(dis.bigEndian() ? vr.toggleEndian(b, false) : b);
                        } else {
                            this.writeValues(vr, b, dis.bigEndian(), attrs.getSpecificCharacterSet(vr));
                        }
                    }
                }
                this.endElement("DicomAttribute");
            }
            catch (SAXException e) {
                throw new IOException(e);
            }
        }
    }

    private void addAttributes(int tag, VR vr, String privateCreator) {
        String keyword;
        if (this.includeKeyword && (keyword = ElementDictionary.keywordOf(tag, privateCreator)) != null && !keyword.isEmpty()) {
            this.addAttribute("keyword", keyword);
        }
        if (privateCreator != null) {
            tag &= 0xFFFF00FF;
        }
        this.addAttribute("tag", TagUtils.toHexString(tag));
        if (privateCreator != null) {
            this.addAttribute("privateCreator", privateCreator);
        }
        this.addAttribute("vr", vr.name());
    }

    @Override
    public void readValue(DicomInputStream dis, Sequence seq) throws IOException {
        try {
            this.startElement("Item", "number", seq.size() + 1);
            dis.readValue(dis, seq);
            this.endElement("Item");
        }
        catch (SAXException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void readValue(DicomInputStream dis, Fragments frags) throws IOException {
        long len = dis.unsignedLength();
        if (dis.isExcludeBulkData()) {
            dis.skipFully(len);
        } else {
            try {
                frags.add(ByteUtils.EMPTY_BYTES);
                if (len > 0L) {
                    this.startElement("DataFragment", "number", frags.size());
                    if (dis.isIncludeBulkDataURI()) {
                        this.writeBulkData(dis.createBulkData(dis));
                    } else {
                        byte[] b = dis.readValue();
                        if (dis.bigEndian()) {
                            frags.vr().toggleEndian(b, false);
                        }
                        this.writeInlineBinary(b);
                    }
                    this.endElement("DataFragment");
                }
            }
            catch (SAXException e) {
                throw new IOException(e);
            }
        }
    }

    private void writeValues(VR vr, Object val, boolean bigEndian, SpecificCharacterSet cs) throws SAXException {
        if (vr.isStringType()) {
            val = vr.toStrings(val, bigEndian, cs);
        }
        int vm = vr.vmOf(val);
        for (int i = 0; i < vm; ++i) {
            String s = vr.toString(val, bigEndian, i, null);
            this.addAttribute("number", Integer.toString(i + 1));
            if (vr == VR.PN) {
                PersonName pn = new PersonName(s, true);
                this.startElement("PersonName");
                this.writePNGroup("Alphabetic", pn, PersonName.Group.Alphabetic);
                this.writePNGroup("Ideographic", pn, PersonName.Group.Ideographic);
                this.writePNGroup("Phonetic", pn, PersonName.Group.Phonetic);
                this.endElement("PersonName");
                continue;
            }
            this.startElement("Value");
            if (s != null) {
                this.writeText(s);
            }
            this.endElement("Value");
        }
    }

    private void writeInlineBinary(byte[] b) throws SAXException {
        int len;
        this.startElement("InlineBinary");
        char[] buf = this.buffer;
        for (int off = 0; off < b.length; off += len) {
            len = Math.min(b.length - off, 768);
            Base64.encode(b, off, len, buf, 0);
            this.ch.characters(buf, 0, len * 4 / 3 + 3 & 0xFFFFFFFC);
        }
        this.endElement("InlineBinary");
    }

    private void writeBulkData(BulkData bulkData) throws SAXException {
        if (bulkData.getUUID() != null) {
            this.addAttribute("uuid", bulkData.getUUID());
        }
        if (bulkData.getURI() != null) {
            this.addAttribute("uri", bulkData.getURI());
        }
        this.startElement("BulkData");
        this.endElement("BulkData");
    }

    private void writeElement(String qname, String s) throws SAXException {
        if (s != null) {
            this.startElement(qname);
            this.writeText(s);
            this.endElement(qname);
        }
    }

    private void writeText(String s) throws SAXException {
        char[] buf = this.buffer;
        int off = 0;
        int totlen = s.length();
        while (off < totlen) {
            int len = Math.min(totlen - off, buf.length);
            s.getChars(off, off += len, buf, 0);
            this.ch.characters(buf, 0, len);
        }
    }

    private void writePNGroup(String qname, PersonName pn, PersonName.Group group) throws SAXException {
        if (pn.contains(group)) {
            this.startElement(qname);
            this.writeElement("FamilyName", pn.get(group, PersonName.Component.FamilyName));
            this.writeElement("GivenName", pn.get(group, PersonName.Component.GivenName));
            this.writeElement("MiddleName", pn.get(group, PersonName.Component.MiddleName));
            this.writeElement("NamePrefix", pn.get(group, PersonName.Component.NamePrefix));
            this.writeElement("NameSuffix", pn.get(group, PersonName.Component.NameSuffix));
            this.endElement(qname);
        }
    }
}

