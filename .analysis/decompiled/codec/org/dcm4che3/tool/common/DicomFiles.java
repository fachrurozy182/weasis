/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.tool.common;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.List;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.io.ContentHandlerAdapter;
import org.dcm4che3.io.DicomInputStream;
import org.xml.sax.helpers.DefaultHandler;

public abstract class DicomFiles {
    private static SAXParser saxParser;

    public static void scan(List<String> fileNames, Callback scb) {
        DicomFiles.scan(fileNames, true, scb);
    }

    public static void scan(List<String> fileNames, boolean printout, Callback scb) {
        fileNames.forEach(filePath -> DicomFiles.scanPath(Path.of(filePath, new String[0]), printout, scb));
    }

    private static void scanPath(Path path, boolean printout, Callback scb) {
        if (Files.isDirectory(path, new LinkOption[0]) && Files.isReadable(path)) {
            DicomFiles.scanDirectory(path, printout, scb);
        } else if (Files.isRegularFile(path, new LinkOption[0])) {
            DicomFiles.scanFile(path, printout, scb);
        }
    }

    private static void scanDirectory(Path dir, boolean printout, Callback scb) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir);){
            for (Path entry : stream) {
                DicomFiles.scanPath(entry, printout, scb);
            }
        }
        catch (IOException e) {
            DicomFiles.handleError("Failed to scan directory " + String.valueOf(dir), e);
        }
    }

    private static void scanFile(Path path, boolean printout, Callback scb) {
        String fileName = path.getFileName().toString();
        if (fileName.endsWith(".xml")) {
            DicomFiles.processXmlFile(path, printout, scb);
        } else {
            DicomFiles.processBinaryDicomFile(path, printout, scb);
        }
    }

    private static void processXmlFile(Path path, boolean printout, Callback scb) {
        try {
            SAXParser parser = DicomFiles.getOrCreateSaxParser();
            Attributes ds = new Attributes();
            ContentHandlerAdapter ch = new ContentHandlerAdapter(ds);
            parser.parse(Files.newInputStream(path, new OpenOption[0]), (DefaultHandler)ch);
            Attributes fmi = ch.getFileMetaInformation();
            if (fmi == null) {
                fmi = ds.createFileMetaInformation("1.2.840.10008.1.2.1");
            }
            boolean success = scb.dicomFile(path, fmi, -1L, ds);
            DicomFiles.printProgress(printout, success);
        }
        catch (Exception e) {
            DicomFiles.handleError("Failed to parse file " + String.valueOf(path), e);
        }
    }

    private static void processBinaryDicomFile(Path path, boolean printout, Callback scb) {
        try (DicomInputStream in = new DicomInputStream(Files.newInputStream(path, new OpenOption[0]));){
            in.setIncludeBulkData(DicomInputStream.IncludeBulkData.NO);
            Attributes fmi = in.readFileMetaInformation();
            long dsPos = in.getPosition();
            Attributes ds = in.readDatasetUntilPixelData();
            if (DicomFiles.isInvalidFileMetaInformation(fmi)) {
                fmi = ds.createFileMetaInformation(in.getTransferSyntax());
            }
            boolean success = scb.dicomFile(path, fmi, dsPos, ds);
            DicomFiles.printProgress(printout, success);
        }
        catch (Exception e) {
            DicomFiles.handleError("Failed to scan file " + String.valueOf(path), e);
        }
    }

    private static SAXParser getOrCreateSaxParser() throws Exception {
        if (saxParser == null) {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
            saxParser = factory.newSAXParser();
        }
        return saxParser;
    }

    private static boolean isInvalidFileMetaInformation(Attributes fmi) {
        return fmi == null || !fmi.containsValue(131088) || !fmi.containsValue(131074) || !fmi.containsValue(131075);
    }

    private static void printProgress(boolean printout, boolean success) {
        if (printout) {
            System.out.print(success ? (char)'.' : 'I');
        }
    }

    private static void handleError(String message, Exception e) {
        System.out.println();
        System.out.println(message + ": " + e.getMessage());
        e.printStackTrace(System.out);
    }

    public static interface Callback {
        public boolean dicomFile(Path var1, Attributes var2, long var3, Attributes var5) throws Exception;
    }
}

