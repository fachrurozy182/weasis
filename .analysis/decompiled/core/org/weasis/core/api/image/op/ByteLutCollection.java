/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.FileUtil
 *  org.weasis.opencv.op.lut.ByteLut
 */
package org.weasis.core.api.image.op;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.util.FileUtil;
import org.weasis.opencv.op.lut.ByteLut;

public final class ByteLutCollection {
    private static final Logger LOGGER = LoggerFactory.getLogger(ByteLutCollection.class);
    private static final int BGR_BANDS = 3;
    private static final int STANDARD_LUT_SIZE = 256;
    private static final int BLUE_CHANNEL = 0;
    private static final int GREEN_CHANNEL = 1;
    private static final int RED_CHANNEL = 2;
    private static final int RGB_COMPONENTS = 3;
    private static final String WHITESPACE_PATTERN = "\\s+";

    private ByteLutCollection() {
    }

    public static byte[][] invert(byte[][] lut) {
        if (lut == null) {
            return null;
        }
        ByteLutCollection.validateLutFormat(lut);
        byte[][] invertedLut = new byte[lut.length][lut[0].length];
        for (int band = 0; band < lut.length; ++band) {
            for (int i = 0; i < lut[band].length; ++i) {
                invertedLut[band][i] = lut[band][lut[band].length - 1 - i];
            }
        }
        return invertedLut;
    }

    public static void readLutFilesFromResourcesDir(List<ByteLut> lutEntries, Path lutFolder) {
        Objects.requireNonNull(lutEntries, "LUT list cannot be null");
        if (lutFolder == null || !Files.isDirectory(lutFolder, new LinkOption[0])) {
            LOGGER.debug("Invalid or non-existent LUT directory: {}", (Object)lutFolder);
            return;
        }
        try (Stream<Path> paths = Files.walk(lutFolder, 1, new FileVisitOption[0]);){
            paths.filter(x$0 -> Files.isRegularFile(x$0, new LinkOption[0])).filter(Files::isReadable).forEach(path -> ByteLutCollection.loadLutFile(lutEntries, path));
            lutEntries.sort(Comparator.comparing(ByteLut::name));
        }
        catch (IOException e) {
            LOGGER.error("Error reading LUT directory: {}", (Object)lutFolder, (Object)e);
        }
    }

    public static byte[][] readLutFile(Scanner scan) {
        Objects.requireNonNull(scan, "Scanner cannot be null");
        byte[][] lut = new byte[3][256];
        int lineIndex = 0;
        while (scan.hasNext() && lineIndex < 256) {
            String line = scan.nextLine().trim();
            if (line.isEmpty() || line.startsWith("#") || !ByteLutCollection.parseLutLine(line, lut, lineIndex)) continue;
            ++lineIndex;
        }
        ByteLutCollection.fillRemainingEntries(lut, lineIndex);
        return lut;
    }

    private static void validateLutFormat(byte[][] lut) {
        if (lut.length == 0) {
            throw new IllegalArgumentException("LUT must have at least one band");
        }
        int expectedLength = lut[0].length;
        for (byte[] band : lut) {
            if (band.length == expectedLength) continue;
            throw new IllegalArgumentException("All LUT bands must have the same length");
        }
    }

    private static void loadLutFile(List<ByteLut> lutEntries, Path filePath) {
        try (Scanner scanner = new Scanner(filePath, StandardCharsets.UTF_8);){
            byte[][] lut = ByteLutCollection.readLutFile(scanner);
            String name = FileUtil.nameWithoutExtension((String)filePath.getFileName().toString());
            lutEntries.add(new ByteLut(name, lut));
        }
        catch (Exception e) {
            LOGGER.error("Error reading LUT file: {}", (Object)filePath, (Object)e);
        }
    }

    private static boolean parseLutLine(String line, byte[][] lut, int lineIndex) {
        String[] components = line.split(WHITESPACE_PATTERN);
        if (components.length != 3) {
            LOGGER.debug("Invalid LUT line format at index {}: {}", (Object)lineIndex, (Object)line);
            return false;
        }
        try {
            lut[2][lineIndex] = ByteLutCollection.parseColorComponent(components[0]);
            lut[1][lineIndex] = ByteLutCollection.parseColorComponent(components[1]);
            lut[0][lineIndex] = ByteLutCollection.parseColorComponent(components[2]);
            return true;
        }
        catch (NumberFormatException e) {
            LOGGER.debug("Invalid number format in LUT line at index {}: {}", (Object)lineIndex, (Object)line);
            return false;
        }
    }

    private static byte parseColorComponent(String component) {
        int value = Integer.parseInt(component.trim());
        return (byte)Math.clamp((long)value, (int)0, (int)255);
    }

    private static void fillRemainingEntries(byte[][] lut, int startIndex) {
        if (startIndex >= 256) {
            return;
        }
        for (int band = 0; band < 3; ++band) {
            byte fillValue = startIndex > 0 ? lut[band][startIndex - 1] : (byte)0;
            for (int i = startIndex; i < 256; ++i) {
                lut[band][i] = startIndex > 0 ? fillValue : (byte)i;
            }
        }
    }
}

