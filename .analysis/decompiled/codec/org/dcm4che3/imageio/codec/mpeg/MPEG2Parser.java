/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.imageio.codec.mpeg;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.VR;
import org.dcm4che3.imageio.codec.XPEGParser;
import org.dcm4che3.imageio.codec.XPEGParserException;
import org.dcm4che3.imageio.codec.mp4.MP4FileType;
import org.dcm4che3.imageio.codec.mpeg.MPEGHeader;
import org.dcm4che3.util.SafeBuffer;

public class MPEG2Parser
implements XPEGParser {
    private static final int BUFFER_SIZE = 8162;
    private static final int SEQUENCE_HEADER_STREAM_ID = -77;
    private static final int GOP_HEADER_STREAM_ID = -72;
    private static final String[] ASPECT_RATIO_1_1 = new String[]{"1", "1"};
    private static final String[] ASPECT_RATIO_4_3 = new String[]{"4", "3"};
    private static final String[] ASPECT_RATIO_16_9 = new String[]{"16", "9"};
    private static final String[] ASPECT_RATIO_221_100 = new String[]{"221", "100"};
    private static final String[][] ASPECT_RATIOS = new String[][]{ASPECT_RATIO_1_1, ASPECT_RATIO_4_3, ASPECT_RATIO_16_9, ASPECT_RATIO_221_100};
    private final byte[] data = new byte[8162];
    private final ByteBuffer buf = ByteBuffer.wrap(this.data);
    private final int columns;
    private final int rows;
    private final int aspectRatio;
    private final int frameRate;
    private final int duration;

    public MPEG2Parser(SeekableByteChannel channel) throws IOException {
        int startCode = this.readStartCode(channel);
        if (!MPEG2Parser.isSequenceHeader(startCode)) {
            while (!MPEG2Parser.isVideoStream(startCode)) {
                this.skip(channel, this.packetLength(channel, startCode));
                startCode = this.readStartCode(channel);
            }
            this.findSequenceHeader(channel, this.packetLength(channel, startCode));
        }
        SafeBuffer.clear(this.buf).limit(7);
        channel.read(this.buf);
        this.columns = (this.data[0] & 0xFF) << 4 | (this.data[1] & 0xF0) >> 4;
        this.rows = (this.data[1] & 0xF) << 8 | this.data[2] & 0xFF;
        this.aspectRatio = this.data[3] >> 4 & 0xF;
        this.frameRate = Math.max(1, Math.min(this.data[3] & 0xF, 8));
        int lastGOP = this.findLastGOP(channel);
        int hh = (this.data[lastGOP] & 0x7C) >> 2;
        int mm = (this.data[lastGOP] & 3) << 4 | (this.data[lastGOP + 1] & 0xF0) >> 4;
        int ss = (this.data[lastGOP + 1] & 7) << 3 | (this.data[lastGOP + 2] & 0xE0) >> 5;
        this.duration = hh * 3600 + mm * 60 + ss;
    }

    @Override
    public long getCodeStreamPosition() {
        return 0L;
    }

    @Override
    public long getPositionAfterAPPSegments() {
        return -1L;
    }

    @Override
    public MP4FileType getMP4FileType() {
        return null;
    }

    @Override
    public Attributes getAttributes(Attributes attrs) {
        if (attrs == null) {
            attrs = new Attributes(15);
        }
        int frameRate2 = this.frameRate - 1 << 1;
        int fps = MPEGHeader.FPS[frameRate2];
        attrs.setInt(1572928, VR.IS, fps);
        attrs.setFloat(1577059, VR.DS, (float)MPEGHeader.FPS[frameRate2 + 1] / (float)fps);
        if (this.aspectRatio > 0 && this.aspectRatio < 5) {
            attrs.setString(2621492, VR.IS, ASPECT_RATIOS[this.aspectRatio - 1]);
        }
        int numFrames = (int)((long)(this.duration * fps) * 1000L / (long)MPEGHeader.FPS[frameRate2 + 1]);
        return MPEGHeader.setImageAttributes(attrs, numFrames, this.rows, this.columns);
    }

    @Override
    public String getTransferSyntaxUID(boolean fragmented) {
        return this.frameRate <= 5 && this.columns <= 720 ? (fragmented ? "1.2.840.10008.1.2.4.100.1" : "1.2.840.10008.1.2.4.100") : (fragmented ? "1.2.840.10008.1.2.4.101.1" : "1.2.840.10008.1.2.4.101");
    }

    private void findSequenceHeader(SeekableByteChannel channel, int length) throws IOException {
        int remaining = length;
        SafeBuffer.clear(this.buf).limit(3);
        while ((remaining -= this.buf.remaining()) > 1) {
            channel.read(this.buf);
            SafeBuffer.rewind(this.buf);
            if ((this.data[0] << 16 | this.data[1] << 8 | this.data[2]) == 1) {
                SafeBuffer.clear(this.buf).limit(1);
                --remaining;
                channel.read(this.buf);
                SafeBuffer.rewind(this.buf);
                if (this.buf.get() == -77) {
                    return;
                }
                SafeBuffer.limit(this.buf, 3);
            }
            SafeBuffer.position(this.buf, this.data[2] == 0 ? (this.data[1] == 0 ? 2 : 1) : 0);
            this.data[0] = 0;
        }
        throw new XPEGParserException("MPEG2 sequence header not found");
    }

    private void skip(SeekableByteChannel channel, long n) throws IOException {
        channel.position(channel.position() + n);
    }

    private int findLastGOP(SeekableByteChannel channel) throws IOException {
        long pos = channel.size() - 8L;
        do {
            pos = Math.max(0L, pos + 8L - 8162L);
            channel.position(pos);
            SafeBuffer.clear(this.buf);
            channel.read(this.buf);
            int i = 0;
            while (i + 8 < 8162) {
                if ((this.data[i] << 16 | this.data[i + 1] << 8 | this.data[i + 2]) == 1 && this.data[i + 3] == -72) {
                    return i + 4;
                }
                i += this.data[i + 2] == 0 ? (this.data[i + 1] == 0 ? 1 : 2) : 3;
            }
        } while (pos > 0L);
        throw new XPEGParserException("last MPEG2 Group of Pictures not found");
    }

    private int readStartCode(SeekableByteChannel channel) throws IOException {
        SafeBuffer.clear(this.buf).limit(4);
        channel.read(this.buf);
        SafeBuffer.rewind(this.buf);
        int startCode = this.buf.getInt();
        if ((startCode & 0xFFFFFE00) != 0) {
            throw new XPEGParserException(String.format("Invalid MPEG2 start code %4XH on position %d", startCode, channel.position() - 4L));
        }
        return startCode;
    }

    private int packetLength(SeekableByteChannel channel, int startCode) throws IOException {
        SafeBuffer.clear(this.buf).limit(2);
        channel.read(this.buf);
        SafeBuffer.rewind(this.buf);
        return MPEG2Parser.isPackHeader(startCode) ? ((this.data[0] & 0xC0) != 0 ? 8 : 6) : this.buf.getShort() & 0xFFFF;
    }

    private static boolean isPackHeader(int startCode) {
        return startCode == 442;
    }

    private static boolean isSequenceHeader(int startCode) {
        return startCode == 435;
    }

    private static boolean isVideoStream(int startCode) {
        return (startCode & 0xFFFFF0) == 480;
    }
}

