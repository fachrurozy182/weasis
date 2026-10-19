/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.imageio.codec.jpeg;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.VR;
import org.dcm4che3.imageio.codec.XPEGParser;
import org.dcm4che3.imageio.codec.XPEGParserException;
import org.dcm4che3.imageio.codec.jpeg.JPEG;
import org.dcm4che3.imageio.codec.mp4.MP4FileType;
import org.dcm4che3.util.SafeBuffer;

public class JPEGParser
implements XPEGParser {
    private static final long JPEG2000_SIGNATURE_BOX = 7660658288187049738L;
    private static final int CONTIGUOUS_CODESTREAM_BOX = 1785737827;
    private static final String MONOCHROME2_TYPE = "MONOCHROME2";
    private final ByteBuffer buf = ByteBuffer.allocate(8);
    private final long codeStreamPosition;
    private long positionAfterAPP = -1L;
    private final Params params;

    public JPEGParser(SeekableByteChannel channel) throws IOException {
        this.seekCodeStream(channel);
        this.codeStreamPosition = channel.position();
        switch (this.readUShort(channel)) {
            case 65496: {
                this.params = new JPEGParams(channel);
                break;
            }
            case 65359: {
                this.params = new JPEG2000Params(channel);
                break;
            }
            default: {
                throw new XPEGParserException("JPEG SOI/SOC marker not found");
            }
        }
    }

    public String toString() {
        return "JPEGParser{codeStreamPos=" + this.codeStreamPosition + ", posAfterAPP=" + this.positionAfterAPP + ", " + String.valueOf(this.params) + "}";
    }

    public Params getParams() {
        return this.params;
    }

    @Override
    public long getCodeStreamPosition() {
        return this.codeStreamPosition;
    }

    @Override
    public long getPositionAfterAPPSegments() {
        return this.positionAfterAPP;
    }

    @Override
    public MP4FileType getMP4FileType() {
        return null;
    }

    @Override
    public Attributes getAttributes(Attributes attrs) {
        if (attrs == null) {
            attrs = new Attributes(10);
        }
        int samples = this.params.samplesPerPixel();
        attrs.setInt(0x280002, VR.US, samples);
        if (samples == 3 || samples == 4) {
            attrs.setString(2621444, VR.CS, this.params.colorPhotometricInterpretation());
            attrs.setInt(2621446, VR.US, 0);
        } else {
            attrs.setString(2621444, VR.CS, MONOCHROME2_TYPE);
        }
        attrs.setInt(2621456, VR.US, this.params.rows());
        attrs.setInt(2621457, VR.US, this.params.columns());
        int bitsStored = this.params.bitsStored();
        attrs.setInt(2621696, VR.US, bitsStored > 8 ? 16 : 8);
        attrs.setInt(2621697, VR.US, bitsStored);
        attrs.setInt(2621698, VR.US, bitsStored - 1);
        attrs.setInt(2621699, VR.US, this.params.pixelRepresentation());
        if (this.params.lossyImageCompression()) {
            attrs.setString(2629904, VR.CS, "01");
        }
        return attrs;
    }

    @Override
    public String getTransferSyntaxUID(boolean fragmented) throws XPEGParserException {
        return this.params.transferSyntaxUID();
    }

    private void seekCodeStream(SeekableByteChannel channel) throws IOException {
        long boxLengthType;
        long startPos = channel.position();
        if (this.readInt(channel) != 12 || this.readLong(channel) != 7660658288187049738L) {
            channel.position(startPos);
            return;
        }
        long size = channel.size();
        long boxPos = channel.position();
        while ((int)(boxLengthType = this.readLong(channel)) != 1785737827) {
            long boxLength = boxLengthType >>> 32;
            if (boxLength == 1L) {
                boxLength = this.readLong(channel);
            }
            if (boxLength <= 0L || (boxPos += boxLength) > size) {
                channel.position(startPos);
                return;
            }
            channel.position(boxPos);
        }
    }

    private int readUShort(SeekableByteChannel channel) throws IOException {
        SafeBuffer.clear(this.buf).limit(2);
        channel.read(this.buf);
        SafeBuffer.rewind(this.buf);
        return this.buf.getShort() & 0xFFFF;
    }

    private int readInt(SeekableByteChannel channel) throws IOException {
        SafeBuffer.clear(this.buf).limit(4);
        channel.read(this.buf);
        SafeBuffer.rewind(this.buf);
        return this.buf.getInt();
    }

    private long readLong(SeekableByteChannel channel) throws IOException {
        SafeBuffer.clear(this.buf);
        channel.read(this.buf);
        SafeBuffer.rewind(this.buf);
        return this.buf.getLong();
    }

    private void skip(SeekableByteChannel channel, long n) throws IOException {
        channel.position(channel.position() + n);
    }

    private Segment nextSegment(SeekableByteChannel channel) throws IOException {
        int v = this.readInt(channel);
        this.requiresFF(channel, v >>> 24);
        int marker = v >> 16 & 0xFF;
        while (JPEG.isStandalone(marker)) {
            marker = v & 0xFF;
            v = v << 16 | this.readUShort(channel);
            this.requiresFF(channel, v >>> 24);
        }
        return new Segment(marker, (v & 0xFFFF) - 2);
    }

    private void requiresFF(SeekableByteChannel channel, int v) throws IOException {
        if (v != 255) {
            throw new XPEGParserException(String.format("unexpected %2XH on position %d", v, channel.position() - 4L));
        }
    }

    private static void checkPosition(SeekableByteChannel channel) throws IOException {
        if (channel.position() >= channel.size()) {
            throw new XPEGParserException("Reached end of stream without finding SOT marker in JPEG stream");
        }
    }

    class JPEGParams
    implements Params {
        final int sof;
        final ByteBuffer sofParams;
        final ByteBuffer sosParams;
        boolean rgb = false;
        boolean jfif = false;

        JPEGParams(SeekableByteChannel channel) throws IOException {
            Segment segment;
            while (true) {
                segment = JPEGParser.this.nextSegment(channel);
                if (!JPEG.isAPP(segment.marker)) break;
                if (segment.marker == 224) {
                    this.jfif = true;
                }
                if (segment.marker == 238 && segment.contentSize >= 12) {
                    ByteBuffer buf = ByteBuffer.allocate(segment.contentSize);
                    channel.read(buf);
                    byte[] values = buf.array();
                    if (values[0] == 65 && values[1] == 100 && values[2] == 111 && values[3] == 98 && values[4] == 101 && values[11] == 0) {
                        this.rgb = true;
                    }
                } else {
                    JPEGParser.this.skip(channel, segment.contentSize);
                }
                JPEGParser.this.positionAfterAPP = channel.position();
            }
            while (!JPEG.isSOF(segment.marker)) {
                JPEGParser.checkPosition(channel);
                JPEGParser.this.skip(channel, segment.contentSize);
                segment = JPEGParser.this.nextSegment(channel);
            }
            this.sof = segment.marker;
            this.sofParams = ByteBuffer.allocate(segment.contentSize);
            channel.read(this.sofParams);
            while (true) {
                segment = JPEGParser.this.nextSegment(channel);
                if (segment.marker == 218) break;
                JPEGParser.checkPosition(channel);
                JPEGParser.this.skip(channel, segment.contentSize);
            }
            this.sosParams = ByteBuffer.allocate(segment.contentSize);
            channel.read(this.sosParams);
        }

        @Override
        public int samplesPerPixel() {
            return this.sofParams.get(5) & 0xFF;
        }

        @Override
        public int rows() {
            return this.sofParams.getShort(1) & 0xFFFF;
        }

        @Override
        public int columns() {
            return this.sofParams.getShort(3) & 0xFFFF;
        }

        @Override
        public int bitsStored() {
            return this.sofParams.get(0) & 0xFF;
        }

        @Override
        public int pixelRepresentation() {
            return 0;
        }

        @Override
        public boolean lossyImageCompression() {
            return this.sof != 195 && (this.sof != 247 || this.sosParams.get(3) != 0);
        }

        public boolean isRgb() {
            return !this.jfif && (this.rgb || this.sofParams.limit() > 12 && (this.sofParams.get(6) & 0xFF) == 82 && (this.sofParams.get(9) & 0xFF) == 71 && (this.sofParams.get(12) & 0xFF) == 66);
        }

        @Override
        public String colorPhotometricInterpretation() {
            if (this.samplesPerPixel() < 3) {
                return JPEGParser.MONOCHROME2_TYPE;
            }
            if (this.sof == 195 || this.sof == 247 || this.isRgb()) {
                return "RGB";
            }
            return this.sof == 192 ? "YBR_FULL_422" : "YBR_FULL";
        }

        @Override
        public String transferSyntaxUID() throws XPEGParserException {
            if (this.sof <= 0) {
                throw new XPEGParserException("Invalid or missing SOF marker");
            }
            return switch (this.sof) {
                case 192 -> "1.2.840.10008.1.2.4.50";
                case 193 -> "1.2.840.10008.1.2.4.51";
                case 194 -> "1.2.840.10008.1.2.4.55";
                case 195 -> this.determineJPEGLosslessUID();
                case 247 -> this.determineJPEGLSUID();
                default -> throw new XPEGParserException(String.format("JPEG SOF%d (0x%02X) is not supported or recognized", this.sof & 0xF, this.sof));
            };
        }

        private String determineJPEGLosslessUID() throws XPEGParserException {
            if (this.sosParams == null || this.sosParams.capacity() < 4) {
                throw new XPEGParserException("SOS parameters required for JPEG Lossless (SOF3) but not available");
            }
            try {
                byte predictor = this.sosParams.get(3);
                return predictor == 1 ? "1.2.840.10008.1.2.4.70" : "1.2.840.10008.1.2.4.57";
            }
            catch (IndexOutOfBoundsException e) {
                throw new XPEGParserException("Invalid SOS parameters for JPEG Lossless: predictor value not accessible", e);
            }
        }

        private String determineJPEGLSUID() throws XPEGParserException {
            if (this.sosParams == null || this.sosParams.capacity() < 4) {
                throw new XPEGParserException("SOS parameters required for JPEG-LS (SOF55) but not available");
            }
            try {
                byte nearLossless = this.sosParams.get(3);
                return nearLossless == 0 ? "1.2.840.10008.1.2.4.80" : "1.2.840.10008.1.2.4.81";
            }
            catch (IndexOutOfBoundsException e) {
                throw new XPEGParserException("Invalid SOS parameters for JPEG-LS: near-lossless value not accessible", e);
            }
        }
    }

    public static interface Params {
        public int samplesPerPixel();

        public int rows();

        public int columns();

        public int bitsStored();

        public int pixelRepresentation();

        public boolean lossyImageCompression();

        public String colorPhotometricInterpretation();

        public String transferSyntaxUID() throws XPEGParserException;
    }

    class JPEG2000Params
    implements Params {
        final ByteBuffer sizParams;
        final ByteBuffer codParams;
        final boolean tlm;

        JPEG2000Params(SeekableByteChannel channel) throws IOException {
            Segment segment;
            ByteBuffer sizParams = null;
            ByteBuffer codParams = null;
            boolean tlm = false;
            do {
                JPEGParser.checkPosition(channel);
                segment = JPEGParser.this.nextSegment(channel);
                switch (segment.marker) {
                    case 81: {
                        sizParams = ByteBuffer.allocate(segment.contentSize);
                        channel.read(sizParams);
                        break;
                    }
                    case 82: {
                        codParams = ByteBuffer.allocate(segment.contentSize);
                        channel.read(codParams);
                        break;
                    }
                    case 85: {
                        tlm = true;
                    }
                    default: {
                        JPEGParser.this.skip(channel, segment.contentSize);
                    }
                }
            } while (segment.marker != 144);
            this.sizParams = sizParams;
            this.codParams = codParams;
            this.tlm = tlm;
        }

        @Override
        public int samplesPerPixel() {
            return this.sizParams.getShort(34) & 0xFFFF;
        }

        @Override
        public int rows() {
            return this.sizParams.getInt(6) - this.sizParams.getInt(14);
        }

        @Override
        public int columns() {
            return this.sizParams.getInt(2) - this.sizParams.getInt(10);
        }

        @Override
        public int bitsStored() {
            return (this.sizParams.get(36) & 0x7F) + 1;
        }

        @Override
        public int pixelRepresentation() {
            return this.sizParams.get(36) < 0 ? 1 : 0;
        }

        @Override
        public boolean lossyImageCompression() {
            return this.codParams.get(9) == 0;
        }

        @Override
        public String colorPhotometricInterpretation() {
            if (this.samplesPerPixel() < 3) {
                return JPEGParser.MONOCHROME2_TYPE;
            }
            return this.codParams.get(4) == 0 ? "RGB" : (this.lossyImageCompression() ? "YBR_ICT" : "YBR_RCT");
        }

        @Override
        public String transferSyntaxUID() {
            return (this.sizParams.getShort(0) & 0x4000) != 0 ? (this.lossyImageCompression() ? "1.2.840.10008.1.2.4.203" : (this.isRPCL() ? "1.2.840.10008.1.2.4.202" : "1.2.840.10008.1.2.4.201")) : (this.lossyImageCompression() ? "1.2.840.10008.1.2.4.91" : "1.2.840.10008.1.2.4.90");
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(512);
            sb.append("JPEG2000Params{\n  SIZ{Lsiz=").append(this.sizParams.limit() + 2);
            sb.append(", Rsiz=").append(this.toBinaryString(this.sizParams.getShort(0) & 0xFFFF));
            sb.append(", Xsiz=").append((long)this.sizParams.getInt(2) & 0xFFFFFFFFL);
            sb.append(", Ysiz=").append((long)this.sizParams.getInt(6) & 0xFFFFFFFFL);
            sb.append(", XOsiz=").append((long)this.sizParams.getInt(10) & 0xFFFFFFFFL);
            sb.append(", YOsiz=").append((long)this.sizParams.getInt(14) & 0xFFFFFFFFL);
            sb.append(", XTsiz=").append((long)this.sizParams.getInt(18) & 0xFFFFFFFFL);
            sb.append(", YTsiz=").append((long)this.sizParams.getInt(22) & 0xFFFFFFFFL);
            sb.append(", XTOsiz=").append((long)this.sizParams.getInt(26) & 0xFFFFFFFFL);
            sb.append(", YTOsiz=").append((long)this.sizParams.getInt(30) & 0xFFFFFFFFL);
            sb.append(", numXTiles=").append(this.numXTiles());
            sb.append(", numYTiles=").append(this.numYTiles());
            sb.append(", Csiz=").append(this.sizParams.getShort(34) & 0xFFFF);
            sb.append(", Comps{");
            int i = 36;
            while (i + 2 < this.sizParams.limit()) {
                sb.append('{');
                byte ssiz = this.sizParams.get(i++);
                if (ssiz < 0) {
                    sb.append('\u00b1');
                }
                sb.append((ssiz & 0x7F) + 1).append(':').append(this.sizParams.get(i++) & 0xFF).append(':').append(this.sizParams.get(i++) & 0xFF).append('}').append(',');
            }
            sb.setLength(sb.length() - 1);
            sb.append("}},\n  COD{Lcod=").append(this.codParams.limit() + 2);
            sb.append(", Scod=").append(this.toBinaryString(this.codParams.get(0) & 0xFF));
            sb.append(", SGcod{P=").append(this.toProgressionOrder(this.codParams.get(1) & 0xFF));
            sb.append(", Layers=").append(this.codParams.getShort(2) & 0xFFFF);
            sb.append(", RCT/ICT=").append(this.codParams.get(4));
            sb.append("}, SPcod{NL=").append(this.decompositions());
            sb.append(", cb-width=").append(4 << (this.codParams.get(6) & 0xFF));
            sb.append(", cb-height=").append(4 << (this.codParams.get(7) & 0xFF));
            sb.append(", cb-style=").append(this.toBinaryString(this.codParams.get(8) & 0xFF));
            sb.append(", Wavelet=").append(this.toTransformation(this.codParams.get(9) & 0xFF));
            if (this.codParams.limit() > 10) {
                sb.append(", Precincts{");
                for (i = 10; i < this.codParams.limit(); ++i) {
                    sb.append('{').append(this.codParams.get(i) & 0xF).append(',').append((this.codParams.get(i) & 0xF0) >>> 4).append('}').append(',');
                }
                sb.setLength(sb.length() - 1);
            }
            sb.append(this.tlm ? "}}}\n  TLM}" : "}}}}");
            return sb.toString();
        }

        private String toTransformation(int i) {
            return switch (i) {
                case 0 -> "9-7";
                case 1 -> "5-3";
                default -> Integer.toString(i);
            };
        }

        private String toProgressionOrder(int i) {
            return switch (i) {
                case 0 -> "LRCP";
                case 1 -> "RLCP";
                case 2 -> "RPCL";
                case 3 -> "PCRL";
                case 4 -> "CPRL";
                default -> Integer.toString(i);
            };
        }

        private String toBinaryString(int i) {
            String s = Integer.toBinaryString(i);
            int l = s.length();
            if (l <= 4) {
                return s;
            }
            StringBuilder sb = new StringBuilder(s);
            while (l > 4) {
                sb.insert(l -= 4, '_');
            }
            return sb.toString();
        }

        private boolean isRPCL() {
            return this.tlm && this.isProgressionOrderRPCL() && this.isBlockSize64x64() && this.numXTiles() == 1 && this.numYTiles() == 1 && Math.min(this.rows(), this.columns()) >>> this.decompositions() <= 64;
        }

        private boolean isProgressionOrderRPCL() {
            return this.codParams.get(1) == 2;
        }

        private int decompositions() {
            return this.codParams.get(5) & 0xFF;
        }

        private boolean isBlockSize64x64() {
            return (this.codParams.get(6) & 0xFF) == 4 && (this.codParams.get(7) & 0xFF) == 4;
        }

        private int numXTiles() {
            return this.numTiles(2, 18, 26);
        }

        private int numYTiles() {
            return this.numTiles(6, 22, 30);
        }

        private int numTiles(int iSize, int iTsiz, int iTOsiz) {
            long tSize = (long)this.sizParams.getInt(iTsiz) & 0xFFFFFFFFL;
            return (int)((((long)this.sizParams.getInt(iSize) & 0xFFFFFFFFL) - ((long)this.sizParams.getInt(iTOsiz) & 0xFFFFFFFFL) + tSize - 1L) / tSize);
        }
    }

    private static class Segment {
        final int marker;
        final int contentSize;

        private Segment(int marker, int contentSize) {
            this.marker = marker;
            this.contentSize = contentSize;
        }
    }
}

