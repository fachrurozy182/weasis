/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.imageio.codec.mp4;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.Date;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.VR;
import org.dcm4che3.imageio.codec.XPEGParser;
import org.dcm4che3.imageio.codec.XPEGParserException;
import org.dcm4che3.imageio.codec.mp4.MP4FileType;
import org.dcm4che3.imageio.codec.mpeg.MPEGHeader;
import org.dcm4che3.util.SafeBuffer;

public class MP4Parser
implements XPEGParser {
    private static final int FileBoxType = 1718909296;
    private static final int MovieBoxType = 1836019574;
    private static final int TrackBoxType = 1953653099;
    private static final int MediaBoxType = 1835297121;
    private static final int MediaHeaderBoxType = 1835296868;
    private static final int MediaInformationBoxType = 1835626086;
    private static final int SampleTableBoxType = 1937007212;
    private static final int SampleDescriptionBoxType = 1937011556;
    private static final int VisualSampleEntryTypeAVC1 = 1635148593;
    private static final int AvcConfigurationBoxType = 1635148611;
    private static final int VisualSampleEntryTypeHVC1 = 1752589105;
    private static final int HevcConfigurationBoxType = 1752589123;
    private static final int SampleSizeBoxType = 1937011578;
    private final ByteBuffer buf = ByteBuffer.allocate(8);
    private MP4FileType mp4FileType;
    private Date creationTime;
    private Date modificationTime;
    private int timescale;
    private long duration;
    private int fp1000s;
    private int rows;
    private int columns;
    private int numFrames;
    private int visualSampleEntryType;
    private int configurationVersion;
    private int profile_idc;
    private int level_idc;

    public MP4Parser(SeekableByteChannel channel) throws IOException {
        long position = channel.position();
        Box box = this.nextBox(channel, channel.size() - position);
        if (box.type == 1718909296) {
            this.mp4FileType = new MP4FileType(this.readInt(channel), this.readInt(channel), this.readInts(channel, box.end));
        } else {
            channel.position(position);
        }
        this.parseMovieBox(channel, this.findBox(channel, channel.size(), 1836019574));
    }

    public Date getCreationTime() {
        return this.creationTime;
    }

    public Date getModificationTime() {
        return this.modificationTime;
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
        return this.mp4FileType;
    }

    @Override
    public Attributes getAttributes(Attributes attrs) {
        if (attrs == null) {
            attrs = new Attributes(14);
        }
        attrs.setInt(1572928, VR.IS, (this.fp1000s + 500) / 1000);
        attrs.setFloat(1577059, VR.DS, 1000000.0f / (float)this.fp1000s);
        return MPEGHeader.setImageAttributes(attrs, this.numFrames, this.rows, this.columns);
    }

    @Override
    public String getTransferSyntaxUID(boolean fragmented) throws XPEGParserException {
        switch (this.visualSampleEntryType) {
            case 1635148593: {
                switch (this.profile_idc) {
                    case 100: {
                        if (this.level_idc <= 41) {
                            return this.isBDCompatible() ? (fragmented ? "1.2.840.10008.1.2.4.103.1" : "1.2.840.10008.1.2.4.103") : (fragmented ? "1.2.840.10008.1.2.4.102.1" : "1.2.840.10008.1.2.4.102");
                        }
                        if (this.level_idc > 42) break;
                        return fragmented ? "1.2.840.10008.1.2.4.104.1" : "1.2.840.10008.1.2.4.104";
                    }
                    case 128: {
                        if (this.level_idc > 42) break;
                        return "1.2.840.10008.1.2.4.106";
                    }
                }
                throw this.profileLevelNotSupported("MPEG-4 AVC profile_idc/level_idc: %d/%d not supported");
            }
            case 1752589105: {
                if (this.level_idc <= 153) {
                    switch (this.profile_idc) {
                        case 1: {
                            return "1.2.840.10008.1.2.4.107";
                        }
                        case 2: {
                            return "1.2.840.10008.1.2.4.108";
                        }
                    }
                }
                throw this.profileLevelNotSupported("MPEG-4 HEVC profile_idc/level_idc: %d/%d not supported");
            }
        }
        throw new AssertionError((Object)("visualSampleEntryType:" + this.visualSampleEntryType));
    }

    private XPEGParserException profileLevelNotSupported(String format) {
        return new XPEGParserException(String.format(format, this.profile_idc, this.level_idc));
    }

    private boolean isBDCompatible() {
        return this.rows == 1080 ? this.columns == 1920 && (this.fp1000s == 23976 || this.fp1000s == 24000 || this.fp1000s == 25000 || this.fp1000s == 29970) : this.rows == 720 && this.columns == 1280 && (this.fp1000s == 23976 || this.fp1000s == 24000 || this.fp1000s == 50000 || this.fp1000s == 59940);
    }

    private Box nextBox(SeekableByteChannel channel, long remaining) throws IOException {
        long pos = channel.position();
        long type = this.readLong(channel);
        long size = type >>> 32;
        return new Box((int)type, pos + (size == 0L ? remaining : (size == 1L ? this.readLong(channel) : size)));
    }

    private Box findBox(SeekableByteChannel channel, long end, int type) throws IOException {
        long remaining;
        while ((remaining = end - channel.position()) > 0L) {
            Box box = this.nextBox(channel, remaining);
            if (box.type == type) {
                return box;
            }
            channel.position(box.end);
        }
        throw new XPEGParserException(MP4Parser.boxNotFound(type));
    }

    private static String boxNotFound(int type) {
        return String.format("%c%c%c%c box not found", type >> 24 & 0xFF, type >> 16 & 0xFF, type >> 8 & 0xFF, type & 0xFF);
    }

    private int[] readInts(SeekableByteChannel channel, long end) throws IOException {
        int[] values = new int[(int)((end - channel.position()) / 4L)];
        for (int i = 0; i < values.length; ++i) {
            values[i] = this.readInt(channel);
        }
        return values;
    }

    private byte readByte(SeekableByteChannel channel) throws IOException {
        SafeBuffer.clear(this.buf).limit(1);
        channel.read(this.buf);
        SafeBuffer.rewind(this.buf);
        return this.buf.get();
    }

    private short readShort(SeekableByteChannel channel) throws IOException {
        SafeBuffer.clear(this.buf).limit(2);
        channel.read(this.buf);
        SafeBuffer.rewind(this.buf);
        return this.buf.getShort();
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

    private static Date toDate(long val) {
        return val > 0L ? new Date((val - 2082844800L) * 1000L) : null;
    }

    private void parseMovieBox(SeekableByteChannel channel, Box box) throws IOException {
        do {
            this.parseTrackBox(channel, this.findBox(channel, box.end, 1953653099));
        } while (this.visualSampleEntryType == 0);
        channel.position(box.end);
    }

    private void parseTrackBox(SeekableByteChannel channel, Box box) throws IOException {
        this.parseMediaBox(channel, this.findBox(channel, box.end, 1835297121));
        channel.position(box.end);
    }

    private void parseMediaBox(SeekableByteChannel channel, Box box) throws IOException {
        this.parseMediaHeaderBox(channel, this.findBox(channel, box.end, 1835296868));
        this.parseMediaInformationBox(channel, this.findBox(channel, box.end, 1835626086));
        channel.position(box.end);
    }

    private void parseMediaHeaderBox(SeekableByteChannel channel, Box box) throws IOException {
        if (this.readInt(channel) >>> 24 == 1) {
            this.creationTime = MP4Parser.toDate(this.readLong(channel));
            this.modificationTime = MP4Parser.toDate(this.readLong(channel));
            this.timescale = this.readInt(channel);
            this.duration = this.readLong(channel);
        } else {
            this.creationTime = MP4Parser.toDate((long)this.readInt(channel) & 0xFFFFFFFFL);
            this.modificationTime = MP4Parser.toDate((long)this.readInt(channel) & 0xFFFFFFFFL);
            this.timescale = this.readInt(channel);
            this.duration = (long)this.readInt(channel) & 0xFFFFFFFFL;
        }
        channel.position(box.end);
    }

    private void parseMediaInformationBox(SeekableByteChannel channel, Box box) throws IOException {
        this.parseSampleTableBox(channel, this.findBox(channel, box.end, 1937007212));
        channel.position(box.end);
    }

    private void parseSampleTableBox(SeekableByteChannel channel, Box box) throws IOException {
        this.parseSampleDescriptionBox(channel, this.findBox(channel, box.end, 1937011556));
        this.parseSampleSizeBox(channel, this.findBox(channel, box.end, 1937011578));
        channel.position(box.end);
    }

    private void parseSampleDescriptionBox(SeekableByteChannel channel, Box box) throws IOException {
        this.skip(channel, 8L);
        this.parseVisualSampleEntry(channel, this.nextBox(channel, box.end));
        channel.position(box.end);
    }

    private void parseVisualSampleEntry(SeekableByteChannel channel, Box box) throws IOException {
        switch (box.type) {
            case 1635148593: {
                this.parseVisualSampleEntryHeader(channel, box);
                this.parseAvcConfigurationBox(channel, this.findBox(channel, box.end, 1635148611));
                break;
            }
            case 1752589105: {
                this.parseVisualSampleEntryHeader(channel, box);
                this.parseHevcConfigurationBox(channel, this.findBox(channel, box.end, 1752589123));
            }
        }
        channel.position(box.end);
    }

    private void parseVisualSampleEntryHeader(SeekableByteChannel channel, Box box) throws IOException {
        this.visualSampleEntryType = box.type;
        this.skip(channel, 24L);
        int val = this.readInt(channel);
        this.columns = val >>> 16;
        this.rows = val & 0xFFFF;
        this.skip(channel, 50L);
    }

    private void parseAvcConfigurationBox(SeekableByteChannel channel, Box box) throws IOException {
        int val = this.readInt(channel);
        this.configurationVersion = val >>> 24;
        this.profile_idc = val >> 16 & 0xFF;
        this.level_idc = val & 0xFF;
        channel.position(box.end);
    }

    private void parseHevcConfigurationBox(SeekableByteChannel channel, Box box) throws IOException {
        short val = this.readShort(channel);
        this.configurationVersion = val >>> 8;
        this.profile_idc = val & 0x1F;
        this.skip(channel, 10L);
        this.level_idc = this.readByte(channel) & 0xFF;
        channel.position(box.end);
    }

    private void parseSampleSizeBox(SeekableByteChannel channel, Box box) throws IOException {
        this.skip(channel, 8L);
        this.numFrames = this.readInt(channel);
        this.fp1000s = (int)(((long)this.numFrames * 1000L * (long)this.timescale + (this.duration >> 1)) / this.duration);
        channel.position(box.end);
    }

    private static class Box {
        final int type;
        final long end;

        Box(int type, long end) {
            this.type = type;
            this.end = end;
        }
    }
}

