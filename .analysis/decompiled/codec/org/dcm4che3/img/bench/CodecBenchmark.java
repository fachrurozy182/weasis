/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.opencv.data.PlanarImage
 */
package org.dcm4che3.img.bench;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.Raster;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.imageio.codec.TransferSyntaxType;
import org.dcm4che3.img.DicomImageReadParam;
import org.dcm4che3.img.DicomImageReader;
import org.dcm4che3.img.DicomJpegWriteParam;
import org.dcm4che3.img.DicomMetaData;
import org.dcm4che3.img.DicomOutputData;
import org.dcm4che3.img.DicomTranscodeParam;
import org.dcm4che3.img.Transcoder;
import org.dcm4che3.img.stream.DicomFileInputStream;
import org.dcm4che3.img.stream.ImageDescriptor;
import org.dcm4che3.img.util.SupplierEx;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.weasis.opencv.data.PlanarImage;

public class CodecBenchmark {
    private static final int SSIM_WINDOW = 8;
    private static final int STRIP_HEIGHT = 32;
    private static final String CSV_HEADER = "file,frames,rows,cols,mp_per_frame,src_ts,decoder,decode_med_ms,decode_p95_ms,decode_mpps,decode_sha1,dst_ts,transcode_med_ms,transcode_p95_ms,transcode_mpps,out_kb,enc_sha1,psnr_db,ssim,encoder,encode_med_ms,encode_p95_ms,encode_mpps";

    public static void main(String[] args) throws IOException {
        Path root;
        if (args.length < 1) {
            System.err.println("Usage: CodecBenchmark <sample-dir> [warmup] [iterations]");
            System.exit(2);
        }
        Path dir = Path.of(args[0], new String[0]);
        int warmup = args.length > 1 ? Integer.parseInt(args[1]) : 2;
        int iters = args.length > 2 ? Integer.parseInt(args[2]) : 5;
        List<Object> candidates = new ArrayList();
        if (Files.isDirectory(dir, new LinkOption[0])) {
            root = dir;
            try (Stream<Path> walk = Files.walk(root, new FileVisitOption[0]);){
                walk.filter(x$0 -> Files.isRegularFile(x$0, new LinkOption[0])).sorted().forEach(candidates::add);
            }
        } else {
            Path parent = dir.getParent();
            root = parent != null ? parent : dir;
            candidates = Collections.singletonList(dir);
        }
        ArrayList<Path> files = new ArrayList<Path>();
        int skipped = 0;
        for (Path path : candidates) {
            if (CodecBenchmark.isDecodableImage(path)) {
                files.add(path);
                continue;
            }
            ++skipped;
            System.err.println("# skip (no decodable image): " + String.valueOf(root.relativize(path)));
        }
        if (files.isEmpty()) {
            System.err.println("No DICOM image files in " + String.valueOf(dir));
            System.exit(1);
        }
        System.err.printf("# weasis-core-img=%s  java=%s  os=%s/%s  cores=%d%n", System.getProperty("weasis.core.img.version", "?"), System.getProperty("java.version"), System.getProperty("os.name"), System.getProperty("os.arch"), Runtime.getRuntime().availableProcessors());
        System.err.printf("# transcode=same-codec round-trip  warmup=%d  iterations=%d  files=%d  skipped=%d%n", warmup, iters, files.size(), skipped);
        System.out.println(CSV_HEADER);
        for (Path path : files) {
            String label = root.relativize(path).toString();
            try {
                CodecBenchmark.benchOne(path, label, warmup, iters);
            }
            catch (Exception e) {
                System.out.printf("%s,ERROR:%s%n", CodecBenchmark.csv(label), CodecBenchmark.sanitize(e.toString()));
                System.err.println("FAILED " + label + System.lineSeparator() + CodecBenchmark.stackTrace(e));
            }
        }
    }

    private static boolean isDecodableImage(Path f) {
        boolean bl;
        DicomInputStream dis = new DicomInputStream(Files.newInputStream(f, new OpenOption[0]));
        try {
            dis.setIncludeBulkData(DicomInputStream.IncludeBulkData.URI);
            Attributes attrs = dis.readDataset();
            bl = attrs.getInt(2621456, 0) > 0 && attrs.getInt(2621457, 0) > 0 && attrs.contains(2145386512);
        }
        catch (Throwable throwable) {
            try {
                try {
                    dis.close();
                }
                catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
            catch (Exception e) {
                return false;
            }
        }
        dis.close();
        return bl;
    }

    private static void benchOne(Path f, String label, int warmup, int iters) throws IOException, NoSuchAlgorithmException {
        FrameInfo info = CodecBenchmark.readFrameInfo(f);
        long[] decodeNs = new long[iters];
        String decodeSha1 = CodecBenchmark.timeDecode(f, info.frames(), warmup, iters, decodeNs);
        boolean hasEncoder = DicomOutputData.isSupportedSyntax(info.srcTS());
        boolean compressedSource = CodecBenchmark.isEncapsulated(info.srcTS());
        TranscodeResult transcode = compressedSource && hasEncoder ? CodecBenchmark.timeTranscode(f, info, warmup, iters) : null;
        long[] encodeNs = hasEncoder ? CodecBenchmark.timeEncode(f, info.srcTS(), info.frames(), warmup, iters) : null;
        System.out.println(CodecBenchmark.buildRow(label, info, hasEncoder, decodeNs, decodeSha1, transcode, encodeNs));
    }

    private static FrameInfo readFrameInfo(Path f) throws IOException {
        try (DicomInputStream dis = new DicomInputStream(Files.newInputStream(f, new OpenOption[0]));){
            dis.setIncludeBulkData(DicomInputStream.IncludeBulkData.NO);
            Attributes attrs = dis.readDataset();
            FrameInfo frameInfo = new FrameInfo(attrs.getInt(0x280008, 1), attrs.getInt(2621456, 0), attrs.getInt(2621457, 0), attrs.getInt(2621697, attrs.getInt(2621696, 8)), attrs.getInt(0x280002, 1), dis.getTransferSyntax());
            return frameInfo;
        }
    }

    private static String timeDecode(Path f, int frames, int warmup, int iters, long[] ns) throws IOException, NoSuchAlgorithmException {
        String digest = null;
        for (int i = -warmup; i < iters; ++i) {
            long t0 = System.nanoTime();
            String h = CodecBenchmark.decodeAllFrames(f, frames);
            long dt = System.nanoTime() - t0;
            if (i < 0) continue;
            ns[i] = dt;
            digest = h;
        }
        return digest;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static TranscodeResult timeTranscode(Path f, FrameInfo info, int warmup, int iters) throws IOException, NoSuchAlgorithmException {
        String srcTS = info.srcTS();
        long[] ns = new long[iters];
        long outBytes = -1L;
        String encSha1 = null;
        for (int i = -warmup; i < iters; ++i) {
            CountingDigestStream sink = new CountingDigestStream();
            long t0 = System.nanoTime();
            CodecBenchmark.transcode(f, srcTS, sink);
            long dt = System.nanoTime() - t0;
            if (i < 0) continue;
            ns[i] = dt;
            outBytes = sink.count;
            encSha1 = sink.hex();
        }
        Path tmp = Files.createTempFile(f.toAbsolutePath().getParent(), "codecbench-", ".dcm", new FileAttribute[0]);
        try {
            try (OutputStream out = Files.newOutputStream(tmp, new OpenOption[0]);){
                CodecBenchmark.transcode(f, srcTS, out);
            }
            String actualDstTS = CodecBenchmark.readTransferSyntax(tmp);
            if (!srcTS.equals(actualDstTS) || !CodecBenchmark.isEncapsulated(actualDstTS)) {
                throw new IOException("transcode did not recompress to source codec (" + srcTS + " -> " + actualDstTS + ")");
            }
            double maxVal = (double)(1L << Math.max(1, info.bitsStored())) - 1.0;
            double[] q = CodecBenchmark.quality(f, tmp, info.frames(), maxVal, info.samplesPerPixel() >= 3);
            TranscodeResult transcodeResult = new TranscodeResult(CodecBenchmark.ns2ms(CodecBenchmark.median(ns)), CodecBenchmark.ns2ms(CodecBenchmark.p95(ns)), info.totalMp() / ((double)CodecBenchmark.median(ns) / 1.0E9), outBytes, encSha1, actualDstTS, q[0], q[1]);
            return transcodeResult;
        }
        finally {
            Files.deleteIfExists(tmp);
        }
    }

    private static long[] timeEncode(Path f, String srcTS, int frames, int warmup, int iters) throws IOException, NoSuchAlgorithmException {
        long[] ns = new long[iters];
        for (int i = -warmup; i < iters; ++i) {
            long dt = CodecBenchmark.encodeOnly(f, srcTS, frames);
            if (i < 0) continue;
            ns[i] = dt;
        }
        return ns;
    }

    private static String buildRow(String label, FrameInfo info, boolean hasEncoder, long[] decodeNs, String decodeSha1, TranscodeResult transcode, long[] encodeNs) {
        double totalMp = info.totalMp();
        StringBuilder sb = new StringBuilder();
        sb.append(CodecBenchmark.csv(label)).append(',').append(info.frames()).append(',').append(info.rows()).append(',').append(info.cols()).append(',').append(CodecBenchmark.fmt(info.mpPerFrame())).append(',').append(info.srcTS()).append(',').append(CodecBenchmark.csv(CodecBenchmark.codecName(info.srcTS()))).append(',').append(CodecBenchmark.fmt(CodecBenchmark.ns2ms(CodecBenchmark.median(decodeNs)))).append(',').append(CodecBenchmark.fmt(CodecBenchmark.ns2ms(CodecBenchmark.p95(decodeNs)))).append(',').append(CodecBenchmark.fmt(totalMp / ((double)CodecBenchmark.median(decodeNs) / 1.0E9))).append(',').append(decodeSha1);
        if (transcode != null) {
            sb.append(',').append(transcode.dstTS()).append(',').append(CodecBenchmark.fmt(transcode.medMs())).append(',').append(CodecBenchmark.fmt(transcode.p95Ms())).append(',').append(CodecBenchmark.fmt(transcode.mpps())).append(',').append(CodecBenchmark.fmt((double)transcode.outBytes() / 1024.0)).append(',').append(transcode.encSha1()).append(',').append(CodecBenchmark.fmtPsnr(transcode.psnr())).append(',').append(CodecBenchmark.fmt(transcode.ssim()));
        } else {
            sb.append(",,,,,,,,");
        }
        sb.append(',').append(CodecBenchmark.csv(hasEncoder ? CodecBenchmark.codecName(info.srcTS()) : ""));
        if (encodeNs != null) {
            sb.append(',').append(CodecBenchmark.fmt(CodecBenchmark.ns2ms(CodecBenchmark.median(encodeNs)))).append(',').append(CodecBenchmark.fmt(CodecBenchmark.ns2ms(CodecBenchmark.p95(encodeNs)))).append(',').append(CodecBenchmark.fmt(totalMp / ((double)CodecBenchmark.median(encodeNs) / 1.0E9)));
        } else {
            sb.append(",,,");
        }
        return sb.toString();
    }

    private static DicomImageReader newReader() {
        return new DicomImageReader(Transcoder.dicomImageReaderSpi);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static String decodeAllFrames(Path f, int frames) throws IOException, NoSuchAlgorithmException {
        DicomImageReader reader = CodecBenchmark.newReader();
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        try (DicomFileInputStream iis = new DicomFileInputStream(f);){
            reader.setInput(iis);
            DicomImageReadParam param = (DicomImageReadParam)reader.getDefaultReadParam();
            int n = reader.getNumImages(true);
            if (n <= 0) {
                n = frames;
            }
            for (int i = 0; i < n; ++i) {
                Raster raster = reader.readRaster(i, param);
                CodecBenchmark.digestRaster(md, raster);
            }
        }
        finally {
            reader.dispose();
        }
        return CodecBenchmark.hex(md.digest());
    }

    private static void transcode(Path f, String dstTS, OutputStream sink) throws IOException {
        DicomTranscodeParam params = new DicomTranscodeParam(dstTS);
        params.setOutputFmi(true);
        Transcoder.dcm2dcm(f, sink, params);
    }

    private static String readTransferSyntax(Path f) throws IOException {
        try (DicomInputStream dis = new DicomInputStream(Files.newInputStream(f, new OpenOption[0]));){
            dis.setIncludeBulkData(DicomInputStream.IncludeBulkData.NO);
            dis.readDataset();
            String string = dis.getTransferSyntax();
            return string;
        }
    }

    private static long encodeOnly(Path f, String targetTS, int frames) throws IOException, NoSuchAlgorithmException {
        DicomImageReader reader = CodecBenchmark.newReader();
        try {
            long l;
            try (DicomFileInputStream iis = new DicomFileInputStream(f);){
                reader.setInput(iis);
                DicomMetaData meta = reader.getStreamMetadata();
                ImageDescriptor desc = meta.getImageDescriptor();
                Attributes dataSet = new Attributes(meta.getDicomObject());
                dataSet.remove(2145386512);
                DicomImageReadParam param = (DicomImageReadParam)reader.getDefaultReadParam();
                int n = reader.getNumImages(true);
                if (n <= 0) {
                    n = frames;
                }
                ArrayList<SupplierEx<PlanarImage, IOException>> images = new ArrayList<SupplierEx<PlanarImage, IOException>>(n);
                for (int fr = 0; fr < n; ++fr) {
                    PlanarImage img = reader.getPlanarImage(fr, param);
                    images.add(() -> img);
                }
                DicomOutputData out = new DicomOutputData(images, desc, targetTS);
                String actualTS = out.getTsuid();
                DicomJpegWriteParam writeParams = new DicomTranscodeParam(targetTS).getWriteJpegParam();
                if (!actualTS.equals(targetTS) && !DicomOutputData.isNativeSyntax(actualTS)) {
                    writeParams = DicomJpegWriteParam.buildDicomImageWriteParam(actualTS);
                }
                CountingDigestStream sink = new CountingDigestStream();
                long t0 = System.nanoTime();
                try (DicomOutputStream dos = new DicomOutputStream(sink, actualTS);){
                    dos.writeFileMetaInformation(dataSet.createFileMetaInformation(actualTS));
                    if (DicomOutputData.isNativeSyntax(actualTS)) {
                        out.writeRawImageData(dos, dataSet);
                    } else {
                        int[] jpegParams = out.adaptTagsToCompressedImage(dataSet, out.getFirstImage().get(), desc, writeParams);
                        out.writeCompressedImageData(dos, dataSet, jpegParams);
                    }
                }
                l = System.nanoTime() - t0;
            }
            return l;
        }
        finally {
            reader.dispose();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static double[] quality(Path orig, Path enc, int frames, double maxVal, boolean color) throws IOException {
        double peak = color ? 255.0 : maxVal;
        double c1 = CodecBenchmark.sq(0.01 * peak);
        double c2 = CodecBenchmark.sq(0.03 * peak);
        DicomImageReader r1 = CodecBenchmark.newReader();
        DicomImageReader r2 = CodecBenchmark.newReader();
        double[] acc = new double[4];
        try (DicomFileInputStream i1 = new DicomFileInputStream(orig);
             DicomFileInputStream i2 = new DicomFileInputStream(enc);){
            r1.setInput(i1);
            r2.setInput(i2);
            DicomImageReadParam p1 = (DicomImageReadParam)r1.getDefaultReadParam();
            DicomImageReadParam p2 = (DicomImageReadParam)r2.getDefaultReadParam();
            int n = Math.min(r1.getNumImages(true), r2.getNumImages(true));
            if (n <= 0) {
                n = frames;
            }
            for (int fr = 0; fr < n; ++fr) {
                if (color) {
                    CodecBenchmark.accumulateColorFrame(acc, r1.read(fr, p1), r2.read(fr, p2), c1, c2);
                    continue;
                }
                CodecBenchmark.accumulateGrayFrame(acc, r1.readRaster(fr, p1), r2.readRaster(fr, p2), c1, c2);
            }
        }
        finally {
            r1.dispose();
            r2.dispose();
        }
        double mse = acc[1] == 0.0 ? 0.0 : acc[0] / acc[1];
        double psnr = mse == 0.0 ? Double.POSITIVE_INFINITY : 10.0 * Math.log10(peak * peak / mse);
        double ssim = acc[3] == 0.0 ? 1.0 : acc[2] / acc[3];
        return new double[]{psnr, ssim};
    }

    private static void accumulateGrayFrame(double[] acc, Raster ra, Raster rb, double c1, double c2) {
        int w = Math.min(ra.getWidth(), rb.getWidth());
        int h = Math.min(ra.getHeight(), rb.getHeight());
        int bands = Math.min(ra.getNumBands(), rb.getNumBands());
        for (int b = 0; b < bands; ++b) {
            for (int y = 0; y < h; y += 32) {
                int hh = Math.min(32, h - y);
                double[] a = ra.getSamples(0, y, w, hh, b, (double[])null);
                double[] e = rb.getSamples(0, y, w, hh, b, (double[])null);
                CodecBenchmark.accumulateStrip(acc, a, e, w, hh, c1, c2);
            }
        }
    }

    private static void accumulateColorFrame(double[] acc, BufferedImage ba, BufferedImage be, double c1, double c2) {
        int w = Math.min(ba.getWidth(), be.getWidth());
        int h = Math.min(ba.getHeight(), be.getHeight());
        for (int y = 0; y < h; y += 32) {
            int hh = Math.min(32, h - y);
            int[] ia = ba.getRGB(0, y, w, hh, null, 0, w);
            int[] ie = be.getRGB(0, y, w, hh, null, 0, w);
            double[] a = new double[ia.length];
            double[] e = new double[ie.length];
            for (int shift = 16; shift >= 0; shift -= 8) {
                for (int k = 0; k < ia.length; ++k) {
                    a[k] = ia[k] >> shift & 0xFF;
                    e[k] = ie[k] >> shift & 0xFF;
                }
                CodecBenchmark.accumulateStrip(acc, a, e, w, hh, c1, c2);
            }
        }
    }

    private static void accumulateStrip(double[] acc, double[] a, double[] e, int w, int hh, double c1, double c2) {
        for (int k = 0; k < a.length; ++k) {
            double d = a[k] - e[k];
            acc[0] = acc[0] + d * d;
        }
        acc[1] = acc[1] + (double)a.length;
        int wy = 0;
        while (wy + 8 <= hh) {
            int wx = 0;
            while (wx + 8 <= w) {
                acc[2] = acc[2] + CodecBenchmark.ssimWindow(a, e, w, wx, wy, c1, c2);
                acc[3] = acc[3] + 1.0;
                wx += 8;
            }
            wy += 8;
        }
    }

    private static double ssimWindow(double[] a, double[] e, int w, int wx, int wy, double c1, double c2) {
        int n = 64;
        double sa = 0.0;
        double se = 0.0;
        double saa = 0.0;
        double see = 0.0;
        double sae = 0.0;
        for (int r = 0; r < 8; ++r) {
            int row = (wy + r) * w + wx;
            for (int c = 0; c < 8; ++c) {
                double va = a[row + c];
                double ve = e[row + c];
                sa += va;
                se += ve;
                saa += va * va;
                see += ve * ve;
                sae += va * ve;
            }
        }
        double ma = sa / (double)n;
        double me = se / (double)n;
        double va = saa / (double)n - ma * ma;
        double ve = see / (double)n - me * me;
        double cov = sae / (double)n - ma * me;
        return (2.0 * ma * me + c1) * (2.0 * cov + c2) / ((ma * ma + me * me + c1) * (va + ve + c2));
    }

    private static double sq(double v) {
        return v * v;
    }

    private static void digestRaster(MessageDigest md, Raster raster) {
        DataBuffer db = raster.getDataBuffer();
        for (int b = 0; b < db.getNumBanks(); ++b) {
            if (db instanceof DataBufferByte) {
                DataBufferByte dbb = (DataBufferByte)db;
                md.update(dbb.getData(b));
                continue;
            }
            if (db instanceof DataBufferUShort) {
                DataBufferUShort dbus = (DataBufferUShort)db;
                CodecBenchmark.update(md, dbus.getData(b));
                continue;
            }
            if (db instanceof DataBufferShort) {
                DataBufferShort dbs = (DataBufferShort)db;
                CodecBenchmark.update(md, dbs.getData(b));
                continue;
            }
            if (db instanceof DataBufferInt) {
                DataBufferInt dbi = (DataBufferInt)db;
                CodecBenchmark.update(md, dbi.getData(b));
                continue;
            }
            int size = db.getSize();
            for (int i = 0; i < size; ++i) {
                CodecBenchmark.update(md, db.getElem(b, i));
            }
        }
    }

    private static void update(MessageDigest md, short[] a) {
        byte[] buf = new byte[a.length * 2];
        for (int i = 0; i < a.length; ++i) {
            int j = i * 2;
            buf[j] = (byte)(a[i] >> 8);
            buf[j + 1] = (byte)a[i];
        }
        md.update(buf);
    }

    private static void update(MessageDigest md, int[] a) {
        byte[] buf = new byte[a.length * 4];
        for (int i = 0; i < a.length; ++i) {
            int j = i * 4;
            buf[j] = (byte)(a[i] >> 24);
            buf[j + 1] = (byte)(a[i] >> 16);
            buf[j + 2] = (byte)(a[i] >> 8);
            buf[j + 3] = (byte)a[i];
        }
        md.update(buf);
    }

    private static void update(MessageDigest md, int v) {
        md.update((byte)(v >> 24));
        md.update((byte)(v >> 16));
        md.update((byte)(v >> 8));
        md.update((byte)v);
    }

    private static long median(long[] a) {
        long[] s = (long[])a.clone();
        Arrays.sort(s);
        if (s.length == 0) {
            return 0L;
        }
        return s[s.length / 2];
    }

    private static long p95(long[] a) {
        long[] s = (long[])a.clone();
        Arrays.sort(s);
        if (s.length == 0) {
            return 0L;
        }
        int idx = (int)Math.ceil(0.95 * (double)s.length) - 1;
        if (idx < 0) {
            idx = 0;
        } else if (idx >= s.length) {
            idx = s.length - 1;
        }
        return s[idx];
    }

    private static double ns2ms(long ns) {
        return (double)ns / 1000000.0;
    }

    private static String fmt(double d) {
        return String.format(Locale.ROOT, "%.3f", d);
    }

    private static String fmtPsnr(double d) {
        if (Double.isNaN(d)) {
            return "";
        }
        return Double.isInfinite(d) ? "inf" : CodecBenchmark.fmt(d);
    }

    private static boolean isEncapsulated(String tsuid) {
        TransferSyntaxType t = TransferSyntaxType.forUID(tsuid);
        return t != null && t.isPixeldataEncapsulated();
    }

    private static String codecName(String tsuid) {
        if (tsuid == null || tsuid.isEmpty()) {
            return "";
        }
        return switch (tsuid) {
            case "1.2.840.10008.1.2" -> "Raw (Implicit LE)";
            case "1.2.840.10008.1.2.1" -> "Raw (Explicit LE)";
            case "1.2.840.10008.1.2.1.99" -> "Raw (Deflated)";
            case "1.2.840.10008.1.2.5" -> "RLE";
            case "1.2.840.10008.1.2.4.50" -> "JPEG (libjpeg)";
            case "1.2.840.10008.1.2.4.51" -> "JPEG12 (libjpeg)";
            case "1.2.840.10008.1.2.4.57", "1.2.840.10008.1.2.4.70" -> "JPEG-LL (libjpeg)";
            case "1.2.840.10008.1.2.4.80", "1.2.840.10008.1.2.4.81" -> "JPEG-LS (CharLS)";
            case "1.2.840.10008.1.2.4.90", "1.2.840.10008.1.2.4.91" -> "J2K (OpenJPEG)";
            case "1.2.840.10008.1.2.4.110", "1.2.840.10008.1.2.4.112", "1.2.840.10008.1.2.4.111" -> "JPEG-XL (libjxl)";
            default -> tsuid;
        };
    }

    private static String sanitize(String s) {
        return s.replace(',', ';').replace('\n', ' ');
    }

    private static String stackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    private static String csv(String s) {
        if (s.indexOf(44) < 0 && s.indexOf(34) < 0 && s.indexOf(10) < 0 && s.indexOf(13) < 0) {
            return s;
        }
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }

    private static String hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit(b >> 4 & 0xF, 16)).append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }

    private CodecBenchmark() {
    }

    private record FrameInfo(int frames, int rows, int cols, int bitsStored, int samplesPerPixel, String srcTS) {
        double mpPerFrame() {
            return (double)this.rows * (double)this.cols / 1000000.0;
        }

        double totalMp() {
            return this.mpPerFrame() * (double)this.frames;
        }
    }

    private record TranscodeResult(double medMs, double p95Ms, double mpps, long outBytes, String encSha1, String dstTS, double psnr, double ssim) {
    }

    private static final class CountingDigestStream
    extends OutputStream {
        long count;
        final MessageDigest md = MessageDigest.getInstance("SHA-256");

        CountingDigestStream() throws NoSuchAlgorithmException {
        }

        @Override
        public void write(int b) {
            this.md.update((byte)b);
            ++this.count;
        }

        @Override
        public void write(byte[] b, int off, int len) {
            this.md.update(b, off, len);
            this.count += (long)len;
        }

        String hex() {
            return CodecBenchmark.hex(this.md.digest());
        }
    }
}

