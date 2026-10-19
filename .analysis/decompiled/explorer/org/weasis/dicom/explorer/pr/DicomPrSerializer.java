/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.xml.bind.JAXBContext
 *  jakarta.xml.bind.Marshaller
 *  java.lang.runtime.SwitchBootstraps
 *  org.dcm4che3.data.Attributes
 *  org.dcm4che3.data.Sequence
 *  org.dcm4che3.data.VR
 *  org.dcm4che3.image.ICCProfile
 *  org.dcm4che3.img.data.CIELab
 *  org.dcm4che3.img.util.DicomUtils
 *  org.dcm4che3.io.DicomOutputStream
 *  org.dcm4che3.util.UIDUtils
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.gui.util.AppProperties
 *  org.weasis.core.api.gui.util.GeomUtil
 *  org.weasis.core.api.image.util.Unit
 *  org.weasis.core.api.util.GzipManager
 *  org.weasis.core.ui.editor.image.dockable.MeasureTool
 *  org.weasis.core.ui.model.GraphicModel
 *  org.weasis.core.ui.model.ReferencedImage
 *  org.weasis.core.ui.model.ReferencedSeries
 *  org.weasis.core.ui.model.graphic.Graphic
 *  org.weasis.core.ui.model.graphic.GraphicLabel
 *  org.weasis.core.ui.model.graphic.imp.AnnotationGraphic
 *  org.weasis.core.ui.model.graphic.imp.PointGraphic
 *  org.weasis.core.ui.model.graphic.imp.area.EllipseGraphic
 *  org.weasis.core.ui.model.graphic.imp.area.ObliqueRectangleGraphic
 *  org.weasis.core.ui.model.graphic.imp.area.PolygonGraphic
 *  org.weasis.core.ui.model.graphic.imp.area.ThreePointsCircleGraphic
 *  org.weasis.core.ui.model.graphic.imp.line.PolylineGraphic
 *  org.weasis.core.ui.model.imp.XmlGraphicModel
 *  org.weasis.core.ui.model.layer.GraphicLayer
 *  org.weasis.core.ui.serialize.XmlSerializer
 *  org.weasis.core.util.StringUtil
 *  org.weasis.dicom.codec.DcmMediaReader
 *  org.weasis.dicom.codec.DicomImageElement
 *  org.weasis.dicom.codec.display.CharsetEncoding
 */
package org.weasis.dicom.explorer.pr;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.runtime.SwitchBootstraps;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.VR;
import org.dcm4che3.image.ICCProfile;
import org.dcm4che3.img.data.CIELab;
import org.dcm4che3.img.util.DicomUtils;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.util.UIDUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.gui.util.AppProperties;
import org.weasis.core.api.gui.util.GeomUtil;
import org.weasis.core.api.image.util.Unit;
import org.weasis.core.api.util.GzipManager;
import org.weasis.core.ui.editor.image.dockable.MeasureTool;
import org.weasis.core.ui.model.GraphicModel;
import org.weasis.core.ui.model.ReferencedImage;
import org.weasis.core.ui.model.ReferencedSeries;
import org.weasis.core.ui.model.graphic.Graphic;
import org.weasis.core.ui.model.graphic.GraphicLabel;
import org.weasis.core.ui.model.graphic.imp.AnnotationGraphic;
import org.weasis.core.ui.model.graphic.imp.PointGraphic;
import org.weasis.core.ui.model.graphic.imp.area.EllipseGraphic;
import org.weasis.core.ui.model.graphic.imp.area.ObliqueRectangleGraphic;
import org.weasis.core.ui.model.graphic.imp.area.PolygonGraphic;
import org.weasis.core.ui.model.graphic.imp.area.ThreePointsCircleGraphic;
import org.weasis.core.ui.model.graphic.imp.line.PolylineGraphic;
import org.weasis.core.ui.model.imp.XmlGraphicModel;
import org.weasis.core.ui.model.layer.GraphicLayer;
import org.weasis.core.ui.serialize.XmlSerializer;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.codec.DcmMediaReader;
import org.weasis.dicom.codec.DicomImageElement;
import org.weasis.dicom.codec.display.CharsetEncoding;

public class DicomPrSerializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(DicomPrSerializer.class);
    private static final String PIXEL = "PIXEL";
    private static final String GSPS = "GSPS";
    private static final String DESCRIPTION = "Weasis Graphics and Annotations";
    private static final String YES = "Y";
    private static final String NO = "N";
    private static final String CARRIAGE_RETURN_LINE_FEED = "\r\n";
    private static final int DEFAULT_SERIES_NUMBER = 999;
    private static final Color DEFAULT_LINE_COLOR = Color.YELLOW;
    private static final double FLATTENING_TOLERANCE = 1.0;

    private DicomPrSerializer() {
    }

    public static Attributes writePresentation(GraphicModel model, Attributes parentAttributes, File outputFile, String seriesInstanceUID, String sopInstanceUID) {
        return DicomPrSerializer.writePresentation(model, parentAttributes, outputFile, seriesInstanceUID, sopInstanceUID, null);
    }

    public static Attributes writePresentation(GraphicModel model, Attributes parentAttributes, File outputFile, String seriesInstanceUID, String sopInstanceUID, Point2D offset) {
        Objects.requireNonNull(model, "GraphicModel cannot be null");
        Objects.requireNonNull(outputFile, "Output file cannot be null");
        if (parentAttributes == null) {
            LOGGER.warn("Parent attributes are null, cannot create presentation state");
            return null;
        }
        try {
            return DicomPrSerializer.createPresentationState(model, parentAttributes, outputFile, seriesInstanceUID, sopInstanceUID, offset);
        }
        catch (Exception e) {
            LOGGER.error("Cannot write Presentation State to file: {}", (Object)outputFile.getAbsolutePath(), (Object)e);
            return null;
        }
    }

    private static void writeCalibration(DicomImageElement img, Attributes attributes) {
        if (img != null && Unit.PIXEL != img.getPixelSpacingUnit() && attributes != null && img.isPixelSizeModifiedByUser()) {
            double unitRatio = img.getPixelSize() * Unit.MILLIMETER.getConversionRatio(img.getPixelSpacingUnit().getFactorToMeters());
            attributes.setDouble(0x700101, VR.DS, new double[]{unitRatio, unitRatio});
        } else if (attributes != null && attributes.containsValue(0x700101)) {
            attributes.remove(0x700101);
        }
    }

    public static Attributes writePresentation(GraphicModel model, DicomImageElement img, File outputFile, String seriesInstanceUID, String sopInstanceUID) {
        Attributes imgAttributes = DicomPrSerializer.extractImageAttributes(img);
        DicomPrSerializer.writeCalibration(img, imgAttributes);
        return DicomPrSerializer.writePresentation(model, imgAttributes, outputFile, seriesInstanceUID, sopInstanceUID, null);
    }

    private static Attributes createPresentationState(GraphicModel model, Attributes parentAttributes, File outputFile, String seriesInstanceUID, String sopInstanceUID, Point2D offset) throws Exception {
        GraphicModel serializedModel = DicomPrSerializer.getModelForSerialization(model, offset);
        Attributes attributes = DicomPrSerializer.createDicomPR(parentAttributes, seriesInstanceUID, sopInstanceUID);
        DicomPrSerializer.addDisplayedAreaSelectionSequence(attributes, parentAttributes);
        DicomPrSerializer.addGraphicAnnotationSequence(attributes);
        DicomPrSerializer.writeCommonTags(attributes);
        DicomPrSerializer.writeReferences(attributes, serializedModel, parentAttributes.getString(524310));
        DicomPrSerializer.writeGraphics(serializedModel, attributes);
        DicomPrSerializer.writePrivateTags(serializedModel, attributes);
        if (DicomPrSerializer.saveToFile(outputFile, attributes)) {
            return attributes;
        }
        throw new IOException("Failed to save DICOM PR to file: " + outputFile.getAbsolutePath());
    }

    private static Attributes extractImageAttributes(DicomImageElement img) {
        DcmMediaReader dcmMediaReader;
        if (img != null && (dcmMediaReader = img.getMediaReader()) instanceof DcmMediaReader) {
            DcmMediaReader reader = dcmMediaReader;
            return reader.getDicomObject();
        }
        return null;
    }

    public static Attributes createDicomPR(Attributes dicomSourceAttribute, String seriesInstanceUID, String sopInstanceUID) {
        int[] patientStudyAttributes = new int[]{524293, 0x100010, 0x100020, 0x100030, 0x100040, 0x100021, 1048626, 0x101010, 0x101030, 0x101020, 0x104000, 1057200, 0x20000D, 524320, 524336, 524368, 524432, 528432, 0x200010, 524369, 1048612, 524369, 1179746, 1179747, 1179748};
        Arrays.sort(patientStudyAttributes);
        Attributes pr = new Attributes(dicomSourceAttribute, patientStudyAttributes);
        String sopClassUID = DicomPrSerializer.determinePresentationStateSopClass(dicomSourceAttribute);
        pr.setString(524310, VR.UI, sopClassUID);
        pr.setString(524293, VR.CS, CharsetEncoding.ISO_IR_192.getLabel());
        if (!"1.2.840.10008.5.1.4.1.1.11.1".equals(sopClassUID)) {
            DicomPrSerializer.addICCProfile(pr, dicomSourceAttribute);
        }
        pr.setString(524312, VR.UI, StringUtil.hasText((CharSequence)sopInstanceUID) ? sopInstanceUID : UIDUtils.createUID());
        Date now = new Date();
        pr.setDate(31525755744682115L, new Date[]{now});
        pr.setDate(2251950138064947L, new Date[]{now});
        pr.setString(524384, VR.CS, "PR");
        pr.setString(0x20000E, VR.UI, StringUtil.hasText((CharSequence)seriesInstanceUID) ? seriesInstanceUID : UIDUtils.createUID());
        return pr;
    }

    public static String determinePresentationStateSopClass(Attributes parentAttributes) {
        if (parentAttributes == null) {
            return "1.2.840.10008.5.1.4.1.1.11.1";
        }
        String photometricInterpretation = parentAttributes.getString(2621444);
        if ("PALETTE COLOR".equals(photometricInterpretation)) {
            return "1.2.840.10008.5.1.4.1.1.11.3";
        }
        int samplesPerPixel = parentAttributes.getInt(0x280002, 1);
        if (samplesPerPixel >= 3) {
            return "1.2.840.10008.5.1.4.1.1.11.2";
        }
        return "1.2.840.10008.5.1.4.1.1.11.1";
    }

    private static void addICCProfile(Attributes pr, Attributes dicomSourceAttribute) {
        if (dicomSourceAttribute != null) {
            ColorSpace colorSpace = null;
            if (ICCProfile.isPresentIn((Attributes)dicomSourceAttribute)) {
                Optional profile = ICCProfile.colorSpaceFactoryOf((Attributes)dicomSourceAttribute).getColorSpace(0);
                colorSpace = profile.orElse(null);
            }
            try {
                if (colorSpace instanceof ICC_ColorSpace) {
                    ICC_ColorSpace iccColorSpace = (ICC_ColorSpace)colorSpace;
                    byte[] data = iccColorSpace.getProfile().getData();
                    pr.setBytes(0x282000, VR.OB, data);
                } else {
                    pr.setBytes(0x282000, VR.OB, null);
                    pr.setString(0x282002, VR.CS, "sRGB");
                }
            }
            catch (Exception e) {
                LOGGER.warn("Cannot set default sRGB ICC profile", (Throwable)e);
            }
        }
    }

    private static void addDisplayedAreaSelectionSequence(Attributes attributes, Attributes sourceAttributes) {
        Sequence displayedAreaSeq = attributes.newSequence(7340122, 1);
        Attributes displayedAreaItem = new Attributes();
        int imageWidth = sourceAttributes.getInt(2621457, 512);
        int imageHeight = sourceAttributes.getInt(2621456, 512);
        displayedAreaItem.setInt(7340115, VR.SL, new int[]{imageWidth, imageHeight});
        displayedAreaItem.setInt(7340114, VR.SL, new int[]{1, 1});
        displayedAreaItem.setString(0x700100, VR.CS, "SCALE TO FIT");
        double[] pixelSpacing = DicomUtils.getDoubleArrayFromDicomElement((Attributes)sourceAttributes, (int)0x700101, null);
        if (pixelSpacing == null) {
            displayedAreaItem.setInt(7340290, VR.IS, new int[]{1, 1});
        } else {
            displayedAreaItem.setDouble(0x700101, VR.DS, pixelSpacing);
        }
        displayedAreaSeq.add(displayedAreaItem);
    }

    private static void addGraphicAnnotationSequence(Attributes attributes) {
        attributes.newSequence(0x700001, 0);
    }

    public static GraphicModel getModelForSerialization(GraphicModel model, Point2D offset) {
        XmlGraphicModel xmlModel = new XmlGraphicModel();
        xmlModel.setReferencedSeries(model.getReferencedSeries());
        for (Graphic graphic : model.getModels()) {
            if (!DicomPrSerializer.isGraphicSerializable(graphic)) continue;
            Graphic processedGraphic = DicomPrSerializer.processGraphicForSerialization(graphic, offset);
            xmlModel.addGraphic(processedGraphic);
        }
        return xmlModel;
    }

    private static boolean isGraphicSerializable(Graphic graphic) {
        return graphic.getLayer().getSerializable() != false && !graphic.getPts().isEmpty();
    }

    private static Graphic processGraphicForSerialization(Graphic graphic, Point2D offset) {
        if (offset == null) {
            return graphic;
        }
        Graphic processedGraphic = (Graphic)graphic.copy();
        DicomPrSerializer.applyOffsetToGraphic(processedGraphic, offset);
        processedGraphic.buildShape();
        GraphicLabel label = graphic.getGraphicLabel();
        if (label != null) {
            processedGraphic.setLabel(label);
        }
        return processedGraphic;
    }

    private static void applyOffsetToGraphic(Graphic graphic, Point2D offset) {
        for (Point2D point : graphic.getPts()) {
            point.setLocation(point.getX() - offset.getX(), point.getY() - offset.getY());
        }
    }

    private static void writePrivateTags(GraphicModel model, Attributes attributes) {
        try {
            JAXBContext jaxbContext = XmlSerializer.getJaxbContext((Class[])new Class[]{model.getClass()});
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();){
                jaxbMarshaller.marshal((Object)model, (OutputStream)outputStream);
                attributes.setString(0x71070070, VR.LO, "weasis/model/xml/2.5");
                attributes.setBytes(0x71077001, VR.OB, GzipManager.gzipCompressToByte((byte[])outputStream.toByteArray()));
            }
        }
        catch (Exception | NoClassDefFoundError e) {
            LOGGER.error("Cannot save XML model to private tags", e);
        }
    }

    private static void writeCommonTags(Attributes attributes) {
        attributes.setInt(2097171, VR.IS, new int[]{1});
        attributes.setString(7340164, VR.PN, AppProperties.WEASIS_USER);
        attributes.setString(0x700080, VR.CS, GSPS);
        attributes.setString(7340161, VR.LO, DESCRIPTION);
        attributes.setInt(0x200011, VR.IS, new int[]{999});
        attributes.setString(1576992, VR.LO, AppProperties.WEASIS_VERSION);
        attributes.setString(528446, VR.LO, String.join((CharSequence)" ", AppProperties.WEASIS_NAME, GSPS));
        attributes.setString(0x20500020, VR.CS, "IDENTITY");
        DicomPrSerializer.setStationName(attributes);
    }

    private static void setStationName(Attributes attributes) {
        try {
            String hostName = InetAddress.getLocalHost().getHostName();
            String stationName = hostName.length() > 16 ? hostName.substring(0, 16) : hostName;
            attributes.setString(528400, VR.SH, stationName);
        }
        catch (UnknownHostException e) {
            LOGGER.warn("Cannot get host name for station name", (Throwable)e);
        }
    }

    private static void writeReferences(Attributes attributes, GraphicModel model, String sopClassUID) {
        List referencedSeries = model.getReferencedSeries();
        Sequence seriesSeq = attributes.newSequence(528661, referencedSeries.size());
        for (ReferencedSeries seriesRef : referencedSeries) {
            Attributes seriesAttributes = DicomPrSerializer.createReferencedSeriesAttributes(seriesRef, sopClassUID);
            seriesSeq.add(seriesAttributes);
        }
    }

    private static Attributes createReferencedSeriesAttributes(ReferencedSeries seriesRef, String sopClassUID) {
        Attributes seriesAttributes = new Attributes(2);
        seriesAttributes.setString(0x20000E, VR.UI, seriesRef.getUuid());
        List images = seriesRef.getImages();
        Sequence imageSeq = seriesAttributes.newSequence(528704, images.size());
        for (ReferencedImage imageRef : images) {
            Attributes imageAttributes = DicomPrSerializer.createReferencedImageAttributes(imageRef, sopClassUID);
            imageSeq.add(imageAttributes);
        }
        return seriesAttributes;
    }

    private static Attributes createReferencedImageAttributes(ReferencedImage imageRef, String sopClassUID) {
        Attributes imageAttributes = new Attributes(2);
        imageAttributes.setString(528720, VR.UI, sopClassUID);
        imageAttributes.setString(528725, VR.UI, imageRef.getUuid());
        List frames = imageRef.getFrames();
        if (frames != null && !frames.isEmpty()) {
            int[] frameNumbers = frames.stream().mapToInt(i -> i + 1).toArray();
            imageAttributes.setInt(528736, VR.IS, frameNumbers);
        }
        return imageAttributes;
    }

    private static void writeGraphics(GraphicModel model, Attributes attributes) {
        List<GraphicLayer> layers = DicomPrSerializer.getSerializableLayers(model.getLayers());
        Sequence annotationSeq = attributes.newSequence(0x700001, layers.size());
        Sequence layerSeq = attributes.newSequence(0x700060, layers.size());
        for (int i = 0; i < layers.size(); ++i) {
            GraphicLayer layer = layers.get(i);
            DicomPrSerializer.processGraphicLayer(model, layer, i, annotationSeq, layerSeq);
        }
    }

    private static List<GraphicLayer> getSerializableLayers(List<GraphicLayer> layers) {
        return layers.stream().filter(GraphicLayer::getSerializable).collect(Collectors.toList());
    }

    private static void processGraphicLayer(GraphicModel model, GraphicLayer layer, int layerIndex, Sequence annotationSeq, Sequence layerSeq) {
        String layerName = layer.getType().name();
        List<Graphic> graphics = DicomPrSerializer.getGraphicsByLayer(model, layer.getUuid());
        Attributes layerAttributes = DicomPrSerializer.createLayerAttributes(layer, layerName, layerIndex);
        layerSeq.add(layerAttributes);
        Attributes annotationAttributes = DicomPrSerializer.createAnnotationAttributes(layerName, graphics);
        annotationSeq.add(annotationAttributes);
    }

    private static Attributes createLayerAttributes(GraphicLayer layer, String layerName, int layerIndex) {
        Attributes attributes = new Attributes(4);
        attributes.setString(0x700002, VR.CS, layerName);
        attributes.setInt(7340130, VR.IS, new int[]{layerIndex});
        attributes.setString(7340136, VR.LO, layer.toString());
        DicomPrSerializer.setLayerDisplayColor(attributes);
        return attributes;
    }

    private static void setLayerDisplayColor(Attributes attributes) {
        Color lineColor = Optional.ofNullable(MeasureTool.viewSetting.getLineColor()).orElse(DEFAULT_LINE_COLOR);
        int[] lab = CIELab.rgbToDicomLab((Color)lineColor);
        attributes.setInt(7341057, VR.US, lab);
    }

    private static Attributes createAnnotationAttributes(String layerName, List<Graphic> graphics) {
        Attributes annotationAttributes = new Attributes(3);
        annotationAttributes.setString(0x700002, VR.CS, layerName);
        Sequence graphicSeq = annotationAttributes.newSequence(0x700009, graphics.size());
        Sequence textSeq = annotationAttributes.newSequence(0x700008, graphics.size());
        for (Graphic graphic : graphics) {
            DicomPrSerializer.buildDicomGraphic(graphic, graphicSeq, textSeq);
        }
        return annotationAttributes;
    }

    private static List<Graphic> getGraphicsByLayer(GraphicModel model, String layerUid) {
        return model.getModels().stream().filter(g -> layerUid.equals(g.getLayer().getUuid())).collect(Collectors.toList());
    }

    private static void buildDicomGraphic(Graphic graphic, Sequence graphicSeq, Sequence textSeq) {
        if (graphic instanceof AnnotationGraphic) {
            AnnotationGraphic annotationGraphic = (AnnotationGraphic)graphic;
            DicomPrSerializer.buildAnnotationGraphic(annotationGraphic, textSeq);
        } else {
            DicomPrSerializer.buildStandardGraphic(graphic, graphicSeq, textSeq);
        }
    }

    private static void buildAnnotationGraphic(AnnotationGraphic graphic, Sequence textSeq) {
        Attributes attributes = DicomPrSerializer.createAnnotationAttributes(graphic);
        textSeq.add(attributes);
    }

    private static void buildStandardGraphic(Graphic graphic, Sequence graphicSeq, Sequence textSeq) {
        GraphicTypeInfo typeInfo = DicomPrSerializer.getGraphicTypeInfo(graphic);
        if (typeInfo.requiresShapeTransformation()) {
            DicomPrSerializer.transformShapeToContour(graphic, graphicSeq);
        } else {
            Attributes dcm = DicomPrSerializer.createBasicGraphicAttributes(graphic, typeInfo);
            graphicSeq.add(dcm);
        }
        DicomPrSerializer.buildGraphicLabel(graphic, textSeq);
    }

    private static GraphicTypeInfo getGraphicTypeInfo(Graphic graphic) {
        Graphic graphic2 = graphic;
        int n = 0;
        switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{ObliqueRectangleGraphic.class, ThreePointsCircleGraphic.class, PolygonGraphic.class, PolylineGraphic.class, PointGraphic.class}, (Graphic)graphic2, (int)n)) {
            case 0: {
                ObliqueRectangleGraphic rectangleShape = (ObliqueRectangleGraphic)graphic2;
                boolean isEllipse = graphic instanceof EllipseGraphic;
                String graphicType = isEllipse ? "ELLIPSE" : "POLYLINE";
                List points = rectangleShape.getRectanglePointList();
                if (!isEllipse) {
                    points.add((Point2D)points.getFirst());
                }
                return new GraphicTypeInfo(graphicType, points, false);
            }
            case 1: {
                Point2D centerPt = GeomUtil.getCircleCenter((List)graphic.getPts());
                List<Point2D> points = Arrays.asList(centerPt, (Point2D)graphic.getPts().getFirst());
                return new GraphicTypeInfo("CIRCLE", points, false);
            }
            case 2: {
                ArrayList<Point2D> points = new ArrayList<Point2D>(graphic.getPts());
                points.add((Point2D)points.getFirst());
                return new GraphicTypeInfo("POLYLINE", points, false);
            }
            case 3: {
                return new GraphicTypeInfo("POLYLINE", graphic.getPts(), false);
            }
            case 4: {
                List<Point2D> points = Collections.singletonList((Point2D)graphic.getPts().getFirst());
                return new GraphicTypeInfo("POINT", points, false);
            }
        }
        return new GraphicTypeInfo(null, null, true);
    }

    private static Attributes createBasicGraphicAttributes(Graphic graphic, GraphicTypeInfo typeInfo) {
        Attributes dcm = DicomPrSerializer.getBasicGraphicStructure(graphic);
        dcm.setString(7340067, VR.CS, typeInfo.graphicType());
        dcm.setDouble(0x700022, VR.FL, DicomPrSerializer.getGraphicsPoints(typeInfo.points()));
        dcm.setInt(7340065, VR.US, new int[]{typeInfo.points().size()});
        return dcm;
    }

    private static Attributes getBasicGraphicStructure(Graphic graphic) {
        Attributes dcm = new Attributes(6);
        dcm.setString(0x700005, VR.CS, PIXEL);
        dcm.setInt(0x700020, VR.US, new int[]{2});
        dcm.setString(7340068, VR.CS, graphic.getFilled() != false ? YES : NO);
        int[] labColor = DicomPrSerializer.getLabColor(graphic);
        DicomPrSerializer.addLineStyleSequence(dcm, graphic, labColor);
        if (graphic.getFilled().booleanValue()) {
            DicomPrSerializer.addFillStyleSequence(dcm, graphic.getFillOpacity().floatValue(), labColor);
        }
        return dcm;
    }

    private static int[] getLabColor(Graphic graphic) {
        Color color;
        Paint paint = graphic.getColorPaint();
        Color lineColor = paint instanceof Color ? (color = (Color)paint) : MeasureTool.viewSetting.getLineColor();
        return CIELab.rgbToDicomLab((Color)lineColor);
    }

    private static void addLineStyleSequence(Attributes dcm, Graphic graphic, int[] labColor) {
        Sequence styleSeq = dcm.newSequence(7340594, 1);
        Attributes styleAttributes = new Attributes();
        styleAttributes.setFloat(7340627, VR.FL, new float[]{graphic.getLineThickness().floatValue()});
        styleAttributes.setString(7340628, VR.CS, "SOLID");
        styleAttributes.setString(7340612, VR.CS, "OFF");
        styleAttributes.setFloat(7340613, VR.FL, new float[]{0.0f});
        styleAttributes.setFloat(7340614, VR.FL, new float[]{0.0f});
        styleAttributes.setInt(7340615, VR.US, labColor);
        styleAttributes.setFloat(7340632, VR.FL, new float[]{0.0f});
        styleAttributes.setFloat(7340676, VR.FL, new float[]{1.0f});
        styleAttributes.setInt(7340625, VR.US, labColor);
        styleAttributes.setFloat(7340677, VR.FL, new float[]{0.0f});
        styleSeq.add(styleAttributes);
    }

    private static void addFillStyleSequence(Attributes dcm, float opacity, int[] labColor) {
        Sequence fillStyleSeq = dcm.newSequence(7340595, 1);
        Attributes fillAttributes = new Attributes();
        fillAttributes.setString(7340631, VR.CS, "SOLID");
        fillAttributes.setFloat(7340676, VR.FL, new float[]{opacity});
        fillAttributes.setFloat(7340677, VR.FL, new float[]{0.0f});
        fillAttributes.setInt(7340625, VR.US, labColor);
        fillStyleSeq.add(fillAttributes);
    }

    private static double[] getGraphicsPoints(List<Point2D> pts) {
        double[] coordinates = new double[pts.size() * 2];
        for (int i = 0; i < pts.size(); ++i) {
            Point2D point = pts.get(i);
            coordinates[i * 2] = point.getX();
            coordinates[i * 2 + 1] = point.getY();
        }
        return coordinates;
    }

    private static void buildGraphicLabel(Graphic graphic, Sequence textSeq) {
        if (!graphic.getLabelVisible().booleanValue()) {
            return;
        }
        GraphicLabel label = graphic.getGraphicLabel();
        if (label == null) {
            return;
        }
        Rectangle2D bounds = label.getTransformedBounds(null);
        String text = String.join((CharSequence)CARRIAGE_RETURN_LINE_FEED, label.getLabels());
        Attributes textAttributes = DicomPrSerializer.createLabelAttributes(bounds, text);
        textSeq.add(textAttributes);
    }

    private static Attributes createAnnotationAttributes(AnnotationGraphic graphic) {
        Rectangle2D bounds = graphic.getLabelBounds();
        Point2D anchor = graphic.getAnchorPoint();
        String text = String.join((CharSequence)CARRIAGE_RETURN_LINE_FEED, graphic.getLabels());
        Attributes attributes = new Attributes(7);
        attributes.setString(0x700003, VR.CS, PIXEL);
        if (anchor != null) {
            attributes.setFloat(7340052, VR.FL, new float[]{(float)anchor.getX(), (float)anchor.getY()});
            attributes.setString(7340053, VR.CS, YES);
            attributes.setString(0x700004, VR.CS, PIXEL);
        }
        attributes.setDouble(0x700010, VR.FL, new double[]{bounds.getMinX(), bounds.getMinY()});
        attributes.setDouble(0x700011, VR.FL, new double[]{bounds.getMaxX(), bounds.getMaxY()});
        attributes.setString(0x700006, VR.ST, text);
        attributes.setString(7340050, VR.CS, "LEFT");
        int[] labColor = DicomPrSerializer.getLabColor((Graphic)graphic);
        DicomPrSerializer.addLineStyleSequence(attributes, (Graphic)graphic, labColor);
        if (graphic.getFilled().booleanValue()) {
            DicomPrSerializer.addFillStyleSequence(attributes, graphic.getFillOpacity().floatValue(), labColor);
        }
        return attributes;
    }

    private static Attributes createLabelAttributes(Rectangle2D bounds, String text) {
        Attributes attributes = new Attributes(4);
        attributes.setString(0x700003, VR.CS, PIXEL);
        attributes.setDouble(0x700010, VR.FL, new double[]{bounds.getMinX(), bounds.getMinY()});
        attributes.setDouble(0x700011, VR.FL, new double[]{bounds.getMaxX(), bounds.getMaxY()});
        attributes.setString(7340050, VR.CS, "LEFT");
        attributes.setString(0x700006, VR.ST, text);
        return attributes;
    }

    public static void transformShapeToContour(Graphic graphic, Sequence graphicSeq) {
        Shape shape = graphic.getShape();
        FlatteningPathIterator iterator = new FlatteningPathIterator(shape.getPathIterator(null), 1.0);
        Attributes currentGraphic = null;
        ArrayList<Point2D> currentPoints = new ArrayList<Point2D>();
        double[] coords = new double[6];
        while (!iterator.isDone()) {
            int segmentType = iterator.currentSegment(coords);
            switch (segmentType) {
                case 0: {
                    DicomPrSerializer.finalizePreviousGraphic(currentGraphic, graphicSeq, currentPoints);
                    currentGraphic = DicomPrSerializer.getBasicGraphicStructure(graphic);
                    currentPoints.clear();
                    currentPoints.add(new Point2D.Double(coords[0], coords[1]));
                    break;
                }
                case 1: 
                case 4: {
                    currentPoints.add(new Point2D.Double(coords[0], coords[1]));
                    break;
                }
                case 2: 
                case 3: {
                    throw new IllegalStateException("Cubic and quadratic segments should be flattened");
                }
                default: {
                    LOGGER.warn("Unsupported path segment type: {}", (Object)segmentType);
                }
            }
            iterator.next();
        }
        DicomPrSerializer.finalizePreviousGraphic(currentGraphic, graphicSeq, currentPoints);
    }

    private static void finalizePreviousGraphic(Attributes attributes, Sequence graphicSeq, List<Point2D> points) {
        if (attributes != null && !points.isEmpty()) {
            attributes.setString(7340067, VR.CS, "POLYLINE");
            attributes.setDouble(0x700022, VR.FL, DicomPrSerializer.getGraphicsPoints(points));
            attributes.setInt(7340065, VR.US, new int[]{points.size()});
            graphicSeq.add(attributes);
        }
    }

    private static boolean saveToFile(File output, Attributes dataset) {
        boolean bl;
        if (dataset == null) {
            LOGGER.error("Cannot save null DICOM attributes");
            return false;
        }
        DicomOutputStream out = new DicomOutputStream(output);
        try {
            out.writeDataset(dataset.createFileMetaInformation("1.2.840.10008.1.2"), dataset);
            LOGGER.debug("Successfully saved DICOM PR to: {}", (Object)output.getAbsolutePath());
            bl = true;
        }
        catch (Throwable throwable) {
            try {
                try {
                    out.close();
                }
                catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
            catch (IOException e) {
                LOGGER.error("Cannot write DICOM PR to file: {}", (Object)output.getAbsolutePath(), (Object)e);
                return false;
            }
        }
        out.close();
        return bl;
    }

    private record GraphicTypeInfo(String graphicType, List<Point2D> points, boolean requiresShapeTransformation) {
    }
}

