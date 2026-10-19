/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.api.media.data.AttributeSource
 *  org.weasis.core.api.media.data.TagUtil
 *  org.weasis.core.api.media.data.XmlAttributeSource
 *  org.weasis.core.ui.util.PrintOptions$DotPerInches
 *  org.weasis.dicom.codec.TransferSyntax
 */
package org.weasis.dicom.explorer.pref.node;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.weasis.core.api.media.data.AttributeSource;
import org.weasis.core.api.media.data.TagUtil;
import org.weasis.core.api.media.data.XmlAttributeSource;
import org.weasis.core.ui.util.PrintOptions;
import org.weasis.dicom.codec.TransferSyntax;
import org.weasis.dicom.explorer.pref.node.AbstractDicomNode;
import org.weasis.dicom.explorer.pref.node.DefaultDicomNode;
import org.weasis.dicom.explorer.print.DicomPrintDialog;
import org.weasis.dicom.explorer.print.DicomPrintOptions;

public class DicomPrintNode
extends DefaultDicomNode {
    private static final String T_MEDIUM_TYPE = "mediumType";
    private static final String T_PRIORITY = "priority";
    private static final String T_FILM_DEST = "filmDestination";
    private static final String T_NUM_COPIES = "numberOfCopies";
    private static final String T_COLOR = "colorPrint";
    private static final String T_FILM_ORIENTATION = "filmOrientation";
    private static final String T_FILM_SIZE = "filmSizeId";
    private static final String T_IMG_DISP_FORMAT = "imageDisplayFormat";
    private static final String T_MAGNIFICATION_TYPE = "magnificationType";
    private static final String T_SMOOTHING_TYPE = "smoothingType";
    private static final String T_BORDER_DENSITY = "borderDensity";
    private static final String T_TRIM = "trim";
    private static final String T_EMPTY_DENSITY = "emptyDensity";
    private static final String T_SHOW_ANNOTATIONS = "showingAnnotations";
    private static final String T_PRINT_SEL_VIEW = "printOnlySelectedView";
    private static final String T_DPI = "dpi";
    private final DicomPrintOptions printOptions;

    public DicomPrintNode(String description, String aeTitle, String hostname, Integer port) {
        this(description, aeTitle, hostname, port, (DicomPrintOptions)null);
    }

    public DicomPrintNode(String description, String aeTitle, String hostname, Integer port, DicomPrintOptions printOptions) {
        super(description, aeTitle, hostname, port, AbstractDicomNode.UsageType.STORAGE);
        this.printOptions = printOptions == null ? new DicomPrintOptions() : printOptions;
    }

    public DicomPrintOptions getPrintOptions() {
        return this.printOptions;
    }

    @Override
    public void saveDicomNode(XMLStreamWriter writer) throws XMLStreamException {
        super.saveDicomNode(writer);
        writer.writeAttribute(T_MEDIUM_TYPE, this.printOptions.getMediumType());
        writer.writeAttribute(T_PRIORITY, this.printOptions.getPriority());
        writer.writeAttribute(T_FILM_DEST, this.printOptions.getFilmDestination());
        writer.writeAttribute(T_NUM_COPIES, String.valueOf(this.printOptions.getNumOfCopies()));
        writer.writeAttribute(T_COLOR, Boolean.toString(this.printOptions.isColorPrint()));
        writer.writeAttribute(T_FILM_ORIENTATION, this.printOptions.getFilmOrientation());
        writer.writeAttribute(T_FILM_SIZE, this.printOptions.getFilmSizeId().name());
        writer.writeAttribute(T_IMG_DISP_FORMAT, this.printOptions.getImageDisplayFormat());
        writer.writeAttribute(T_MAGNIFICATION_TYPE, this.printOptions.getMagnificationType());
        writer.writeAttribute(T_SMOOTHING_TYPE, this.printOptions.getSmoothingType());
        writer.writeAttribute(T_BORDER_DENSITY, this.printOptions.getBorderDensity());
        writer.writeAttribute(T_TRIM, this.printOptions.getTrim());
        writer.writeAttribute(T_EMPTY_DENSITY, this.printOptions.getEmptyDensity());
        writer.writeAttribute(T_SHOW_ANNOTATIONS, Boolean.toString(this.printOptions.isShowingAnnotations()));
        writer.writeAttribute(T_PRINT_SEL_VIEW, Boolean.toString(this.printOptions.isPrintOnlySelectedView()));
        writer.writeAttribute(T_DPI, this.printOptions.getDpi().name());
    }

    public static DicomPrintNode buildDicomPrintNode(XMLStreamReader xmler) {
        XmlAttributeSource source = new XmlAttributeSource(xmler);
        DicomPrintNode node = new DicomPrintNode(xmler.getAttributeValue(null, "description"), xmler.getAttributeValue(null, "aeTitle"), xmler.getAttributeValue(null, "hostname"), TagUtil.getIntegerTagAttribute((AttributeSource)source, (String)"port", (Integer)104));
        node.setTsuid(TransferSyntax.getTransferSyntax((String)xmler.getAttributeValue(null, "tsuid")));
        node.printOptions.setMediumType(TagUtil.getTagAttribute((AttributeSource)source, (String)T_MEDIUM_TYPE, (String)"BLUE FILM"));
        node.printOptions.setPriority(TagUtil.getTagAttribute((AttributeSource)source, (String)T_PRIORITY, (String)"LOW"));
        node.printOptions.setFilmDestination(TagUtil.getTagAttribute((AttributeSource)source, (String)T_FILM_DEST, (String)"MAGAZINE"));
        node.printOptions.setNumOfCopies(TagUtil.getIntegerTagAttribute((AttributeSource)source, (String)T_NUM_COPIES, (Integer)1));
        node.printOptions.setColorPrint(TagUtil.getBooleanTagAttribute((AttributeSource)source, (String)T_COLOR, (Boolean)false));
        node.printOptions.setFilmOrientation(TagUtil.getTagAttribute((AttributeSource)source, (String)T_FILM_ORIENTATION, (String)"PORTRAIT"));
        node.printOptions.setFilmSizeId(DicomPrintDialog.FilmSize.getInstance(xmler.getAttributeValue(null, T_FILM_SIZE), DicomPrintOptions.DEF_FILM_SIZE));
        node.printOptions.setImageDisplayFormat(TagUtil.getTagAttribute((AttributeSource)source, (String)T_IMG_DISP_FORMAT, (String)"STANDARD\\1,1"));
        node.printOptions.setMagnificationType(TagUtil.getTagAttribute((AttributeSource)source, (String)T_MAGNIFICATION_TYPE, (String)"CUBIC"));
        node.printOptions.setSmoothingType(TagUtil.getTagAttribute((AttributeSource)source, (String)T_SMOOTHING_TYPE, (String)"MEDIUM"));
        node.printOptions.setBorderDensity(TagUtil.getTagAttribute((AttributeSource)source, (String)T_BORDER_DENSITY, (String)"WHITE"));
        node.printOptions.setTrim(TagUtil.getTagAttribute((AttributeSource)source, (String)T_TRIM, (String)"NO"));
        node.printOptions.setEmptyDensity(TagUtil.getTagAttribute((AttributeSource)source, (String)T_EMPTY_DENSITY, (String)"BLACK"));
        node.printOptions.setShowingAnnotations(TagUtil.getBooleanTagAttribute((AttributeSource)source, (String)T_SHOW_ANNOTATIONS, (Boolean)true));
        node.printOptions.setPrintOnlySelectedView(TagUtil.getBooleanTagAttribute((AttributeSource)source, (String)T_PRINT_SEL_VIEW, (Boolean)false));
        node.printOptions.setDpi(PrintOptions.DotPerInches.getInstance((String)xmler.getAttributeValue(null, T_DPI), (PrintOptions.DotPerInches)DicomPrintOptions.DEF_DPI));
        return node;
    }
}

