/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Code;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.SAXParserFactoryHolder;
import org.dcm4che3.util.ByteUtils;
import org.dcm4che3.util.ResourceLocator;
import org.dcm4che3.util.StringUtils;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class IOD
extends ArrayList<DataElement> {
    private static final long serialVersionUID = -5065822488885801576L;
    private DataElementType type;
    private Condition condition;
    private int lineNumber = -1;

    public void setType(DataElementType type) {
        this.type = type;
    }

    public DataElementType getType() {
        return this.type;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public Condition getCondition() {
        return this.condition;
    }

    public int getLineNumber() {
        return this.lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public void parse(String uri) throws IOException {
        try {
            SAXParser parser = SAXParserFactoryHolder.factory.newSAXParser();
            parser.parse(uri, (DefaultHandler)new SAXHandler(this));
        }
        catch (SAXException e) {
            throw new IOException("Failed to parse " + uri, e);
        }
        catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public static IOD load(String uri) throws IOException {
        if (uri.startsWith("resource:")) {
            try {
                uri = ResourceLocator.getResource(uri.substring(9), IOD.class);
            }
            catch (NullPointerException npe) {
                throw new FileNotFoundException(uri);
            }
        } else if (uri.indexOf(58) < 2) {
            uri = new File(uri).toURI().toString();
        }
        IOD iod = new IOD();
        iod.parse(uri);
        iod.trimToSize();
        return iod;
    }

    public static IOD valueOf(Code code) {
        IOD iod = new IOD();
        iod.add(new DataElement(524544, VR.SH, DataElementType.TYPE_1, 1, 1, 0).setValues(code.getCodeValue()));
        iod.add(new DataElement(524546, VR.SH, DataElementType.TYPE_1, 1, 1, 0).setValues(code.getCodingSchemeDesignator()));
        String codingSchemeVersion = code.getCodingSchemeVersion();
        if (codingSchemeVersion == null) {
            iod.add(new DataElement(524547, VR.SH, DataElementType.TYPE_0, -1, -1, 0));
        } else {
            iod.add(new DataElement(524547, VR.SH, DataElementType.TYPE_1, 1, 1, 0));
        }
        return iod;
    }

    public static enum DataElementType {
        TYPE_0,
        TYPE_1,
        TYPE_2,
        TYPE_3;

    }

    public static abstract class Condition {
        protected String id;
        protected boolean not;

        public Condition id(String id) {
            this.id = id;
            return this;
        }

        public final String id() {
            return this.id;
        }

        public final Condition not() {
            this.not = !this.not;
            return this;
        }

        public abstract boolean match(Attributes var1);

        public void addChild(Condition child) {
            throw new UnsupportedOperationException();
        }

        public Condition trim() {
            return this;
        }

        public boolean isEmpty() {
            return false;
        }
    }

    private static class SAXHandler
    extends DefaultHandler {
        private StringBuilder sb = new StringBuilder();
        private boolean processCharacters;
        private boolean elementConditions;
        private boolean itemConditions;
        private String idref;
        private List<String> values = new ArrayList<String>();
        private List<Code> codes = new ArrayList<Code>();
        private LinkedList<IOD> iodStack = new LinkedList();
        private LinkedList<Condition> conditionStack = new LinkedList();
        private Map<String, IOD> id2iod = new HashMap<String, IOD>();
        private Map<String, Condition> id2cond = new HashMap<String, Condition>();
        private Locator locator;

        public SAXHandler(IOD iod) {
            this.iodStack.add(iod);
        }

        @Override
        public void setDocumentLocator(Locator locator) {
            this.locator = locator;
        }

        @Override
        public void startElement(String uri, String localName, String qName, org.xml.sax.Attributes atts) throws SAXException {
            switch (qName) {
                case "And": {
                    this.startCondition(qName, new And());
                    break;
                }
                case "Code": {
                    this.startCode(atts.getValue("codeValue"), atts.getValue("codingSchemeDesignator"), atts.getValue("codingSchemeVersion"), atts.getValue("codeMeaning"));
                    break;
                }
                case "DataElement": {
                    this.startDataElement(atts.getValue("tag"), atts.getValue("vr"), atts.getValue("type"), atts.getValue("vm"), atts.getValue("items"), atts.getValue("valueNumber"));
                    break;
                }
                case "If": {
                    this.startIf(atts.getValue("id"), atts.getValue("idref"));
                    break;
                }
                case "Item": {
                    this.startItem(atts.getValue("id"), atts.getValue("idref"), atts.getValue("type"));
                    break;
                }
                case "MemberOf": {
                    this.startCondition(qName, this.memberOf(atts));
                    break;
                }
                case "NotAnd": {
                    this.startCondition(qName, new And().not());
                    break;
                }
                case "NotMemberOf": {
                    this.startCondition(qName, this.memberOf(atts).not());
                    break;
                }
                case "NotOr": {
                    this.startCondition(qName, new Or().not());
                    break;
                }
                case "NotPresent": {
                    this.startCondition(qName, this.present(atts).not());
                    break;
                }
                case "Or": {
                    this.startCondition(qName, new Or());
                    break;
                }
                case "Present": {
                    this.startCondition(qName, this.present(atts));
                    break;
                }
                case "Value": {
                    this.startValue();
                }
            }
        }

        private Present present(org.xml.sax.Attributes atts) throws SAXException {
            int[] tagPath = this.tagPathOf(atts.getValue("tag"));
            int lastIndex = tagPath.length - 1;
            return new Present(tagPath[lastIndex], lastIndex > 0 ? Arrays.copyOf(tagPath, lastIndex) : ByteUtils.EMPTY_INTS);
        }

        private MemberOf memberOf(org.xml.sax.Attributes atts) throws SAXException {
            int[] tagPath = this.tagPathOf(atts.getValue("tag"));
            int lastIndex = tagPath.length - 1;
            return new MemberOf(tagPath[lastIndex], this.vrOf(atts.getValue("vr")), this.valueNumberOf(atts.getValue("valueNumber"), 1) - 1, this.matchNotPresentOf(atts.getValue("matchNotPresent")), lastIndex > 0 ? Arrays.copyOf(tagPath, lastIndex) : ByteUtils.EMPTY_INTS);
        }

        private void startCode(String codeValue, String codingSchemeDesignator, String codingSchemeVersion, String codeMeaning) throws SAXException {
            if (codeValue == null) {
                throw new SAXException("missing codeValue attribute");
            }
            if (codingSchemeDesignator == null) {
                throw new SAXException("missing codingSchemeDesignator attribute");
            }
            if (codeMeaning == null) {
                throw new SAXException("missing codeMeaning attribute");
            }
            this.codes.add(new Code(codeValue, codingSchemeDesignator, codingSchemeVersion, codeMeaning));
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            switch (qName) {
                case "DataElement": {
                    this.endDataElement();
                    break;
                }
                case "Item": {
                    this.endItem();
                    break;
                }
                case "Value": {
                    this.endValue();
                    break;
                }
                case "And": 
                case "If": 
                case "MemberOf": 
                case "NotAnd": 
                case "NotMemberOf": 
                case "NotOr": 
                case "NotPresent": 
                case "Or": 
                case "Present": {
                    this.endCondition(qName);
                }
            }
            this.processCharacters = false;
            this.idref = null;
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (this.processCharacters) {
                this.sb.append(ch, start, length);
            }
        }

        private void startDataElement(String tagStr, String vrStr, String typeStr, String vmStr, String items, String valueNumberStr) throws SAXException {
            String vm;
            if (this.idref != null) {
                throw new SAXException("<Item> with idref must be empty");
            }
            IOD iod = this.iodStack.getLast();
            int tag = this.tagOf(tagStr);
            VR vr = this.vrOf(vrStr);
            DataElementType type = this.typeOf(typeStr);
            int minVM = -1;
            int maxVM = -1;
            String string = vm = vr == VR.SQ ? items : vmStr;
            if (vm != null) {
                try {
                    String[] ss = StringUtils.split(vm, '-');
                    if (ss[0].charAt(0) != 'n') {
                        minVM = Integer.parseInt(ss[0]);
                        if (ss.length > 1) {
                            if (ss[1].charAt(0) != 'n') {
                                maxVM = Integer.parseInt(ss[1]);
                            }
                        } else {
                            maxVM = minVM;
                        }
                    }
                }
                catch (IllegalArgumentException e) {
                    throw new SAXException((vr == VR.SQ ? "invalid items=\"" : "invalid vm=\"") + vm + '\"');
                }
            }
            DataElement el = new DataElement(tag, vr, type, minVM, maxVM, this.valueNumberOf(valueNumberStr, 0));
            if (this.locator != null) {
                el.setLineNumber(this.locator.getLineNumber());
            }
            iod.add(el);
            this.elementConditions = true;
            this.itemConditions = false;
        }

        private DataElementType typeOf(String s) throws SAXException {
            if (s == null) {
                throw new SAXException("missing type attribute");
            }
            try {
                return DataElementType.valueOf("TYPE_" + s);
            }
            catch (IllegalArgumentException e) {
                throw new SAXException("unrecognized type=\"" + s + '\"');
            }
        }

        private VR vrOf(String s) throws SAXException {
            try {
                return VR.valueOf(s);
            }
            catch (NullPointerException e) {
                throw new SAXException("missing vr attribute");
            }
            catch (IllegalArgumentException e) {
                throw new SAXException("unrecognized vr=\"" + s + '\"');
            }
        }

        private int tagOf(String s) throws SAXException {
            try {
                return (int)Long.parseLong(s, 16);
            }
            catch (NullPointerException e) {
                throw new SAXException("missing tag attribute");
            }
            catch (IllegalArgumentException e) {
                throw new SAXException("invalid tag=\"" + s + '\"');
            }
        }

        private int[] tagPathOf(String s) throws SAXException {
            String[] ss = StringUtils.split(s, '/');
            if (ss.length == 0) {
                throw new SAXException("missing tag attribute");
            }
            try {
                int[] tagPath = new int[ss.length];
                for (int i = 0; i < tagPath.length; ++i) {
                    tagPath[i] = ss[i].equals("..") ? -1 : (int)Long.parseLong(s, 16);
                }
                return tagPath;
            }
            catch (IllegalArgumentException e) {
                throw new SAXException("invalid tag=\"" + s + '\"');
            }
        }

        private int valueNumberOf(String s, int def) throws SAXException {
            try {
                return s != null ? Integer.parseInt(s) : def;
            }
            catch (IllegalArgumentException e) {
                throw new SAXException("invalid valueNumber=\"" + s + '\"');
            }
        }

        private boolean matchNotPresentOf(String s) {
            return s != null && s.equalsIgnoreCase("true");
        }

        private DataElement getLastDataElement() {
            IOD iod = this.iodStack.getLast();
            return (DataElement)iod.get(iod.size() - 1);
        }

        private void endDataElement() throws SAXException {
            DataElement el = this.getLastDataElement();
            if (!this.values.isEmpty()) {
                try {
                    if (el.vr.isIntType()) {
                        el.setValues(this.parseInts(this.values));
                    } else {
                        el.setValues(this.values.toArray(new String[this.values.size()]));
                    }
                }
                catch (IllegalStateException e) {
                    throw new SAXException("unexpected <Value>");
                }
                this.values.clear();
            }
            if (!this.codes.isEmpty()) {
                try {
                    el.setValues(this.codes.toArray(new Code[this.codes.size()]));
                }
                catch (IllegalStateException e) {
                    throw new SAXException("unexpected <Code>");
                }
                this.codes.clear();
            }
            this.elementConditions = false;
        }

        private int[] parseInts(List<String> list) {
            int[] is = new int[list.size()];
            for (int i = 0; i < is.length; ++i) {
                is[i] = Integer.parseInt(list.get(i));
            }
            return is;
        }

        private void startValue() {
            this.sb.setLength(0);
            this.processCharacters = true;
        }

        private void endValue() {
            this.values.add(this.sb.toString());
        }

        private void startItem(String id, String idref, String type) throws SAXException {
            IOD iod;
            if (idref != null) {
                if (type != null) {
                    throw new SAXException("<Item> with idref must not specify type");
                }
                iod = this.id2iod.get(idref);
                if (iod == null) {
                    throw new SAXException("could not resolve <Item idref:\"" + idref + "\"/>");
                }
            } else {
                iod = new IOD();
                if (type != null) {
                    iod.setType(this.typeOf(type));
                }
                if (this.locator != null) {
                    iod.setLineNumber(this.locator.getLineNumber());
                }
            }
            this.getLastDataElement().addItemIOD(iod);
            this.iodStack.add(iod);
            if (id != null) {
                this.id2iod.put(id, iod);
            }
            this.idref = idref;
            this.itemConditions = true;
            this.elementConditions = false;
        }

        private void endItem() {
            this.iodStack.removeLast().trimToSize();
            this.itemConditions = false;
        }

        private void startIf(String id, String idref) throws SAXException {
            Condition cond;
            if (!this.conditionStack.isEmpty()) {
                throw new SAXException("unexpected <If>");
            }
            if (idref != null) {
                cond = this.id2cond.get(idref);
                if (cond == null) {
                    throw new SAXException("could not resolve <If idref:\"" + idref + "\"/>");
                }
            } else {
                cond = new And().id(id);
            }
            this.conditionStack.add(cond);
            if (id != null) {
                this.id2cond.put(id, cond);
            }
            this.idref = idref;
        }

        private void startCondition(String name, Condition cond) throws SAXException {
            if (!this.elementConditions && !this.itemConditions) {
                throw new SAXException("unexpected <" + name + '>');
            }
            this.conditionStack.add(cond);
        }

        private void endCondition(String name) throws SAXException {
            Condition cond = this.conditionStack.removeLast();
            if (cond.isEmpty()) {
                throw new SAXException('<' + name + "> must not be empty");
            }
            if (!this.values.isEmpty()) {
                try {
                    MemberOf memberOf = (MemberOf)cond;
                    if (memberOf.vr.isIntType()) {
                        memberOf.setValues(this.parseInts(this.values));
                    } else {
                        memberOf.setValues(this.values.toArray(new String[this.values.size()]));
                    }
                }
                catch (Exception e) {
                    throw new SAXException("unexpected <Value> contained by <" + name + ">");
                }
                this.values.clear();
            }
            if (!this.codes.isEmpty()) {
                try {
                    ((MemberOf)cond).setValues(this.codes.toArray(new Code[this.codes.size()]));
                }
                catch (Exception e) {
                    throw new SAXException("unexpected <Code> contained by <" + name + ">");
                }
                this.codes.clear();
            }
            if (this.conditionStack.isEmpty()) {
                if (this.elementConditions) {
                    this.getLastDataElement().setCondition(cond.trim());
                } else {
                    this.iodStack.getLast().setCondition(cond.trim());
                }
                this.elementConditions = false;
                this.itemConditions = false;
            } else {
                this.conditionStack.getLast().addChild(cond.trim());
            }
        }
    }

    public static class DataElement
    implements Serializable {
        private static final long serialVersionUID = -7460474415381086525L;
        public final int tag;
        public final VR vr;
        public final DataElementType type;
        public final int minVM;
        public final int maxVM;
        public final int valueNumber;
        private Condition condition;
        private Object values;
        private int lineNumber = -1;

        public DataElement(int tag, VR vr, DataElementType type, int minVM, int maxVM, int valueNumber) {
            this.tag = tag;
            this.vr = vr;
            this.type = type;
            this.minVM = minVM;
            this.maxVM = maxVM;
            this.valueNumber = valueNumber;
        }

        public DataElement setCondition(Condition condition) {
            this.condition = condition;
            return this;
        }

        public Condition getCondition() {
            return this.condition;
        }

        public int getValueNumber() {
            return this.valueNumber;
        }

        public DataElement setValues(String ... values) {
            if (this.vr == VR.SQ) {
                throw new IllegalStateException("vr=SQ");
            }
            this.values = values;
            return this;
        }

        public DataElement setValues(int ... values) {
            if (!this.vr.isIntType()) {
                throw new IllegalStateException("vr=" + (Object)((Object)this.vr));
            }
            this.values = values;
            return this;
        }

        public DataElement setValues(Code ... values) {
            if (this.vr != VR.SQ) {
                throw new IllegalStateException("vr=" + (Object)((Object)this.vr));
            }
            this.values = values;
            return this;
        }

        public DataElement addItemIOD(IOD iod) {
            if (this.values == null) {
                this.values = new IOD[]{iod};
            } else {
                IOD[] iods = (IOD[])this.values;
                iods = Arrays.copyOf(iods, iods.length + 1);
                iods[iods.length - 1] = iod;
                this.values = iods;
            }
            return this;
        }

        public Object getValues() {
            return this.values;
        }

        public int getLineNumber() {
            return this.lineNumber;
        }

        public DataElement setLineNumber(int lineNumber) {
            this.lineNumber = lineNumber;
            return this;
        }
    }

    public static class MemberOf
    extends Present {
        private final VR vr;
        private final int valueIndex;
        private final boolean matchNotPresent;
        private Object values;

        public MemberOf(int tag, VR vr, int valueIndex, boolean matchNotPresent, int ... itemPath) {
            super(tag, itemPath);
            this.vr = vr;
            this.valueIndex = valueIndex;
            this.matchNotPresent = matchNotPresent;
        }

        public VR vr() {
            return this.vr;
        }

        public MemberOf setValues(String ... values) {
            if (this.vr == VR.SQ) {
                throw new IllegalStateException("vr=SQ");
            }
            this.values = values;
            return this;
        }

        public MemberOf setValues(int ... values) {
            if (!this.vr.isIntType()) {
                throw new IllegalStateException("vr=" + (Object)((Object)this.vr));
            }
            this.values = values;
            return this;
        }

        public MemberOf setValues(Code ... values) {
            if (this.vr != VR.SQ) {
                throw new IllegalStateException("vr=" + (Object)((Object)this.vr));
            }
            this.values = values;
            return this;
        }

        @Override
        public boolean match(Attributes attrs) {
            if (this.values == null) {
                throw new IllegalStateException("values not initialized");
            }
            Attributes item = this.item(attrs);
            if (item == null) {
                return this.matchNotPresent;
            }
            if (this.values instanceof int[]) {
                return this.not ? !this.match(item, (int[])this.values) : this.match(item, (int[])this.values);
            }
            if (this.values instanceof Code[]) {
                return this.not ? !this.match(item, (Code[])this.values) : this.match(item, (Code[])this.values);
            }
            return this.not ? !this.match(item, (String[])this.values) : this.match(item, (String[])this.values);
        }

        private boolean match(Attributes item, String[] ss) {
            String val = item.getString(this.tag, this.valueIndex);
            if (val == null) {
                return this.not ? !this.matchNotPresent : this.matchNotPresent;
            }
            for (String s : ss) {
                if (!s.equals(val)) continue;
                return !this.not;
            }
            return this.not;
        }

        private boolean match(Attributes item, Code[] codes) {
            Sequence seq = item.getSequence(this.tag);
            if (seq != null) {
                for (Attributes codeItem : seq) {
                    try {
                        Code val = new Code(codeItem);
                        for (Code code : codes) {
                            if (!code.equals(val)) continue;
                            return !this.not;
                        }
                    }
                    catch (NullPointerException nullPointerException) {
                    }
                }
            }
            return this.not;
        }

        private boolean match(Attributes item, int[] is) {
            int val = item.getInt(this.tag, this.valueIndex, Integer.MIN_VALUE);
            if (val == Integer.MIN_VALUE) {
                return this.matchNotPresent;
            }
            for (int i : is) {
                if (i != val) continue;
                return true;
            }
            return false;
        }
    }

    public static class Present
    extends Condition {
        protected final int tag;
        protected final int[] itemPath;

        public Present(int tag, int ... itemPath) {
            this.tag = tag;
            this.itemPath = itemPath;
        }

        @Override
        public boolean match(Attributes attrs) {
            return this.not ? !this.item(attrs).containsValue(this.tag) : this.item(attrs).containsValue(this.tag);
        }

        protected Attributes item(Attributes attrs) {
            for (int sqtag : this.itemPath) {
                if (sqtag != -1) continue;
                attrs = sqtag == -1 ? attrs.getParent() : attrs.getNestedDataset(sqtag);
            }
            return attrs;
        }
    }

    public static class Or
    extends CompositeCondition {
        @Override
        public boolean match(Attributes attrs) {
            for (Condition child : this.childs) {
                if (!child.match(attrs)) continue;
                return !this.not;
            }
            return this.not;
        }
    }

    public static class And
    extends CompositeCondition {
        @Override
        public boolean match(Attributes attrs) {
            for (Condition child : this.childs) {
                if (child.match(attrs)) continue;
                return this.not;
            }
            return !this.not;
        }
    }

    static abstract class CompositeCondition
    extends Condition {
        protected final ArrayList<Condition> childs = new ArrayList();

        CompositeCondition() {
        }

        @Override
        public abstract boolean match(Attributes var1);

        @Override
        public void addChild(Condition child) {
            this.childs.add(child);
        }

        @Override
        public Condition trim() {
            int size = this.childs.size();
            if (size == 1) {
                Condition child = this.childs.get(0).id(this.id);
                return this.not ? child.not() : child;
            }
            this.childs.trimToSize();
            return this;
        }

        @Override
        public boolean isEmpty() {
            return this.childs.isEmpty();
        }
    }
}

