/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.StringUtil
 *  org.weasis.opencv.data.LookupTableCV
 *  org.weasis.opencv.op.lut.LutShape
 *  org.weasis.opencv.op.lut.LutShape$Function
 *  org.weasis.opencv.op.lut.PresentationStateLut
 *  org.weasis.opencv.op.lut.WlPresentation
 */
package org.dcm4che3.img.lut;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.dcm4che3.img.DicomImageAdapter;
import org.dcm4che3.img.data.PrDicomObject;
import org.dcm4che3.img.lut.VoiLutModule;
import org.dcm4che3.img.stream.ImageDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.util.StringUtil;
import org.weasis.opencv.data.LookupTableCV;
import org.weasis.opencv.op.lut.LutShape;
import org.weasis.opencv.op.lut.PresentationStateLut;
import org.weasis.opencv.op.lut.WlPresentation;

public class PresetWindowLevel {
    private static final Logger LOGGER = LoggerFactory.getLogger(PresetWindowLevel.class);
    private static final Map<String, List<PresetWindowLevel>> PRESET_LIST_BY_MODALITY = PresetWindowLevel.loadPresetsByModality();
    private static final int AUTO_LEVEL_KEY = 48;
    private static final int FIRST_PRESET_KEY = 49;
    private static final int SECOND_PRESET_KEY = 50;
    private static final int MIN_BITS_FOR_MODALITY_PRESETS = 8;
    private final String name;
    private final double window;
    private final double level;
    private final LutShape shape;
    private int keyCode = 0;

    public PresetWindowLevel(String name, Double window, Double level, LutShape shape) {
        this.name = Objects.requireNonNull(name);
        this.window = Objects.requireNonNull(window);
        this.level = Objects.requireNonNull(level);
        this.shape = Objects.requireNonNull(shape);
    }

    public String getName() {
        return this.name;
    }

    public double getWindow() {
        return this.window;
    }

    public double getLevel() {
        return this.level;
    }

    public LutShape getLutShape() {
        return this.shape;
    }

    public int getKeyCode() {
        return this.keyCode;
    }

    public double getMinBox() {
        return this.level - this.window / 2.0;
    }

    public double getMaxBox() {
        return this.level + this.window / 2.0;
    }

    public void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }

    public boolean isAutoLevel() {
        return this.keyCode == 48;
    }

    public String toString() {
        return this.name;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        PresetWindowLevel that = (PresetWindowLevel)o;
        return Double.compare(that.window, this.window) == 0 && Double.compare(that.level, this.level) == 0 && this.name.equals(that.name) && this.shape.equals((Object)that.shape);
    }

    public int hashCode() {
        return Objects.hash(this.name, this.window, this.level, this.shape);
    }

    public static List<PresetWindowLevel> getPresetCollection(DicomImageAdapter adapter, String type, WlPresentation wl) {
        Objects.requireNonNull(adapter, "adapter cannot be null");
        Objects.requireNonNull(wl, "wl cannot be null");
        PresetCollectionBuilder presetBuilder = new PresetCollectionBuilder(adapter, type, wl);
        return presetBuilder.buildPresets();
    }

    public static PresetWindowLevel buildPresetFromLutData(DicomImageAdapter adapter, LookupTableCV voiLUTsData, WlPresentation wl, String explanation) {
        if (adapter == null || voiLUTsData == null || explanation == null) {
            return null;
        }
        Object lutData = PresetWindowLevel.extractLutData(voiLUTsData);
        if (lutData == null) {
            return null;
        }
        int[] valueRange = PresetWindowLevel.calculateLutValueRange(voiLUTsData, lutData, adapter, wl);
        double[] windowLevel = PresetWindowLevel.calculateWindowLevelFromRange(valueRange);
        LutShape newLutShape = new LutShape(voiLUTsData, explanation);
        return new PresetWindowLevel(newLutShape.toString(), windowLevel[0], windowLevel[1], newLutShape);
    }

    private static Object extractLutData(LookupTableCV voiLUTsData) {
        return switch (voiLUTsData.getDataType()) {
            case 0 -> voiLUTsData.getByteData(0);
            case 1, 2 -> (Object[])voiLUTsData.getShortData(0);
            default -> null;
        };
    }

    private static int[] calculateLutValueRange(LookupTableCV voiLUTsData, Object lutData, DicomImageAdapter adapter, WlPresentation wl) {
        int minValue = voiLUTsData.getOffset();
        int maxValue = voiLUTsData.getOffset() + Array.getLength(lutData) - 1;
        minValue = Math.max(Math.min(minValue, maxValue), adapter.getMinAllocatedValue(wl));
        maxValue = Math.min(Math.max(minValue, maxValue), adapter.getMaxAllocatedValue(wl));
        return new int[]{minValue, maxValue};
    }

    private static double[] calculateWindowLevelFromRange(int[] range) {
        double width = (double)range[1] - (double)range[0];
        double center = (double)range[0] + width / 2.0;
        return new double[]{width, center};
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static Map<String, List<PresetWindowLevel>> loadPresetsByModality() {
        try (InputStream stream = PresetWindowLevel.openPresetFile();){
            if (stream == null) {
                Map<String, List<PresetWindowLevel>> map = Collections.emptyMap();
                return map;
            }
            XMLInputFactory factory = PresetWindowLevel.createSecureXMLFactory();
            XMLStreamReader xmlReader = factory.createXMLStreamReader(stream);
            TreeMap<String, List<PresetWindowLevel>> presets = new TreeMap<String, List<PresetWindowLevel>>();
            PresetWindowLevel.parsePresetsXML(xmlReader, presets);
            TreeMap<String, List<PresetWindowLevel>> treeMap = presets;
            return treeMap;
        }
        catch (Exception e) {
            LOGGER.error("Cannot read presets file!", (Throwable)e);
            return Collections.emptyMap();
        }
    }

    private static InputStream openPresetFile() throws IOException {
        String pathString = System.getProperty("dicom.presets.path");
        if (!StringUtil.hasText((CharSequence)pathString)) {
            return PresetWindowLevel.class.getResourceAsStream("presets.xml");
        }
        Path path = Paths.get(pathString, new String[0]);
        return Files.isReadable(path) ? Files.newInputStream(path, new OpenOption[0]) : null;
    }

    public static XMLInputFactory createSecureXMLFactory() {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty("javax.xml.stream.isSupportingExternalEntities", Boolean.FALSE);
        factory.setProperty("javax.xml.stream.supportDTD", Boolean.FALSE);
        return factory;
    }

    public static void parsePresetsXML(XMLStreamReader xmler, Map<String, List<PresetWindowLevel>> presets) throws XMLStreamException {
        while (xmler.hasNext()) {
            if (xmler.next() != 1 || !"presets".equals(xmler.getName().getLocalPart())) continue;
            while (xmler.hasNext()) {
                PresetWindowLevel.readPresetListByModality(xmler, presets);
            }
        }
    }

    private static void readPresetListByModality(XMLStreamReader xmler, Map<String, List<PresetWindowLevel>> presets) throws XMLStreamException {
        if (xmler.next() != 1) {
            return;
        }
        String elementName = xmler.getName().getLocalPart();
        if (!"preset".equals(elementName) || xmler.getAttributeCount() < 4) {
            return;
        }
        try {
            PresetWindowLevel preset = PresetWindowLevel.parsePresetFromXML(xmler);
            String modality = xmler.getAttributeValue(null, "modality");
            presets.computeIfAbsent(modality, k -> new ArrayList()).add(preset);
        }
        catch (Exception e) {
            String name = xmler.getAttributeValue(null, "name");
            LOGGER.error("Preset {} cannot be read from xml file", (Object)name, (Object)e);
        }
    }

    private static PresetWindowLevel parsePresetFromXML(XMLStreamReader xmler) {
        String name = xmler.getAttributeValue(null, "name");
        double window = Double.parseDouble(xmler.getAttributeValue(null, "window"));
        double level = Double.parseDouble(xmler.getAttributeValue(null, "level"));
        String shape = xmler.getAttributeValue(null, "shape");
        Integer keyCode = PresetWindowLevel.getKeyTagAttribute(xmler);
        LutShape lutShape = LutShape.getLutShape((String)shape);
        PresetWindowLevel preset = new PresetWindowLevel(name, window, level, Objects.requireNonNullElse(lutShape, LutShape.LINEAR));
        if (keyCode != null) {
            preset.setKeyCode(keyCode);
        }
        return preset;
    }

    private static Integer getKeyTagAttribute(XMLStreamReader xmler) {
        String value = xmler.getAttributeValue(null, "key");
        return value != null ? StringUtil.getInteger((String)value) : null;
    }

    private static class PresetCollectionBuilder {
        private final DicomImageAdapter adapter;
        private final String dicomKeyWord;
        private final WlPresentation wl;
        private final List<PresetWindowLevel> presetList;
        private final ImageDescriptor desc;
        private final VoiLutModule vLut;

        PresetCollectionBuilder(DicomImageAdapter adapter, String type, WlPresentation wl) {
            this.adapter = adapter;
            this.dicomKeyWord = " " + type;
            this.wl = wl;
            this.presetList = new ArrayList<PresetWindowLevel>();
            this.desc = adapter.getImageDescriptor();
            this.vLut = this.desc.getVoiLutForFrame(adapter.getFrameIndex());
        }

        List<PresetWindowLevel> buildPresets() {
            this.buildPresetsFromWindowLevel();
            this.buildPresetsFromLutData();
            this.addAutoLevelPreset();
            this.addModalityPresets();
            return this.presetList;
        }

        private void buildPresetsFromWindowLevel() {
            List<Double> levelList = this.getWindowCenter();
            List<Double> windowList = this.getWindowWidth();
            List<String> explanationList = this.getWindowCenterWidthExplanation();
            LutShape defaultLutShape = this.getDefaultLutShape();
            if (levelList.isEmpty() || windowList.isEmpty()) {
                return;
            }
            String defaultExplanation = "Default";
            int presetCounter = 1;
            for (int i = 0; i < levelList.size(); ++i) {
                String explanation = PresetCollectionBuilder.getPresetExplanation(explanationList, i, defaultExplanation + " " + presetCounter);
                PresetWindowLevel preset = new PresetWindowLevel(explanation + this.dicomKeyWord, windowList.get(i), levelList.get(i), defaultLutShape);
                if (this.presetList.contains(preset)) continue;
                PresetCollectionBuilder.setPresetKeyCode(preset, presetCounter - 1);
                this.presetList.add(preset);
                ++presetCounter;
            }
        }

        private void buildPresetsFromLutData() {
            List<LookupTableCV> voiLUTsData = this.getVoiLutData();
            List<String> voiLUTsExplanation = this.getVoiLUTExplanation();
            if (voiLUTsData.isEmpty()) {
                return;
            }
            String defaultExplanation = "VOI LUT";
            for (int i = 0; i < voiLUTsData.size(); ++i) {
                String explanation = PresetCollectionBuilder.getPresetExplanation(voiLUTsExplanation, i, defaultExplanation + " " + i);
                PresetWindowLevel preset = PresetWindowLevel.buildPresetFromLutData(this.adapter, voiLUTsData.get(i), this.wl, explanation + this.dicomKeyWord);
                if (preset == null) continue;
                PresetCollectionBuilder.setPresetKeyCode(preset, this.presetList.size());
                this.presetList.add(preset);
            }
        }

        private void addAutoLevelPreset() {
            PresetWindowLevel autoLevel = new PresetWindowLevel("Auto Level [Image]", this.adapter.getFullDynamicWidth(this.wl), this.adapter.getFullDynamicCenter(this.wl), this.getDefaultLutShape());
            autoLevel.setKeyCode(48);
            this.presetList.add(autoLevel);
        }

        private void addModalityPresets() {
            List<PresetWindowLevel> modPresets;
            String modality;
            if (this.adapter.getBitsStored() > 8 && StringUtil.hasText((CharSequence)(modality = this.desc.getModality())) && (modPresets = PRESET_LIST_BY_MODALITY.get(modality)) != null) {
                this.presetList.addAll(modPresets);
            }
        }

        private List<Double> getWindowCenter() {
            ArrayList<Double> centers = new ArrayList<Double>();
            PresentationStateLut presentationStateLut = this.wl.getPresentationState();
            if (presentationStateLut instanceof PrDicomObject) {
                PrDicomObject pr = (PrDicomObject)presentationStateLut;
                pr.getVoiLUT().ifPresent(voiLutModule -> centers.addAll(voiLutModule.getWindowCenter()));
            }
            centers.addAll(this.vLut.getWindowCenter());
            return centers;
        }

        private List<Double> getWindowWidth() {
            ArrayList<Double> widths = new ArrayList<Double>();
            PresentationStateLut presentationStateLut = this.wl.getPresentationState();
            if (presentationStateLut instanceof PrDicomObject) {
                PrDicomObject pr = (PrDicomObject)presentationStateLut;
                pr.getVoiLUT().ifPresent(voiLutModule -> widths.addAll(voiLutModule.getWindowWidth()));
            }
            widths.addAll(this.vLut.getWindowWidth());
            return widths;
        }

        private List<String> getWindowCenterWidthExplanation() {
            return this.vLut.getWindowCenterWidthExplanation();
        }

        private List<LookupTableCV> getVoiLutData() {
            ArrayList<LookupTableCV> luts = new ArrayList<LookupTableCV>();
            PresentationStateLut presentationStateLut = this.wl.getPresentationState();
            if (presentationStateLut instanceof PrDicomObject) {
                PrDicomObject pr = (PrDicomObject)presentationStateLut;
                pr.getVoiLUT().ifPresent(voiLutModule -> luts.addAll(voiLutModule.getLut()));
            }
            luts.addAll(this.vLut.getLut());
            return luts;
        }

        private List<String> getVoiLUTExplanation() {
            ArrayList<String> explanations = new ArrayList<String>();
            PresentationStateLut presentationStateLut = this.wl.getPresentationState();
            if (presentationStateLut instanceof PrDicomObject) {
                PrDicomObject pr = (PrDicomObject)presentationStateLut;
                pr.getVoiLUT().ifPresent(voiLutModule -> explanations.addAll(voiLutModule.getLutExplanation()));
            }
            explanations.addAll(this.vLut.getLutExplanation());
            return explanations;
        }

        private LutShape getDefaultLutShape() {
            return this.vLut.getVoiLutFunction().map(function -> switch (function.toUpperCase(Locale.ROOT)) {
                case "SIGMOID" -> new LutShape(LutShape.Function.SIGMOID, String.valueOf(LutShape.Function.SIGMOID) + this.dicomKeyWord);
                case "LINEAR" -> new LutShape(LutShape.Function.LINEAR, String.valueOf(LutShape.Function.LINEAR) + this.dicomKeyWord);
                default -> LutShape.LINEAR;
            }).orElse(LutShape.LINEAR);
        }

        private static String getPresetExplanation(List<String> explanationList, int index, String defaultExplanation) {
            String explanation;
            if (index < explanationList.size() && StringUtil.hasText((CharSequence)(explanation = explanationList.get(index)))) {
                return explanation;
            }
            return defaultExplanation;
        }

        private static void setPresetKeyCode(PresetWindowLevel preset, int index) {
            if (index == 0) {
                preset.setKeyCode(49);
            } else if (index == 1) {
                preset.setKeyCode(50);
            }
        }
    }
}

