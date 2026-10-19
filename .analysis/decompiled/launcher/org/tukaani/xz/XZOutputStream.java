/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz;

import java.io.IOException;
import java.io.OutputStream;
import org.tukaani.xz.ArrayCache;
import org.tukaani.xz.BlockOutputStream;
import org.tukaani.xz.FilterCoder;
import org.tukaani.xz.FilterEncoder;
import org.tukaani.xz.FilterOptions;
import org.tukaani.xz.FinishableOutputStream;
import org.tukaani.xz.RawCoder;
import org.tukaani.xz.UnsupportedOptionsException;
import org.tukaani.xz.XZ;
import org.tukaani.xz.XZIOException;
import org.tukaani.xz.check.Check;
import org.tukaani.xz.common.EncoderUtil;
import org.tukaani.xz.common.StreamFlags;
import org.tukaani.xz.index.IndexEncoder;

public class XZOutputStream
extends FinishableOutputStream {
    private final ArrayCache arrayCache;
    private OutputStream out;
    private final StreamFlags streamFlags = new StreamFlags();
    private final Check check;
    private final IndexEncoder index = new IndexEncoder();
    private BlockOutputStream blockEncoder = null;
    private FilterEncoder[] filters;
    private boolean filtersSupportFlushing;
    private IOException exception = null;
    private boolean finished = false;
    private final byte[] tempBuf = new byte[1];

    public XZOutputStream(OutputStream out, FilterOptions filterOptions) throws IOException {
        this(out, filterOptions, 4);
    }

    public XZOutputStream(OutputStream out, FilterOptions filterOptions, ArrayCache arrayCache) throws IOException {
        this(out, filterOptions, 4, arrayCache);
    }

    public XZOutputStream(OutputStream out, FilterOptions filterOptions, int checkType) throws IOException {
        this(out, new FilterOptions[]{filterOptions}, checkType);
    }

    public XZOutputStream(OutputStream out, FilterOptions filterOptions, int checkType, ArrayCache arrayCache) throws IOException {
        this(out, new FilterOptions[]{filterOptions}, checkType, arrayCache);
    }

    public XZOutputStream(OutputStream out, FilterOptions[] filterOptions) throws IOException {
        this(out, filterOptions, 4);
    }

    public XZOutputStream(OutputStream out, FilterOptions[] filterOptions, ArrayCache arrayCache) throws IOException {
        this(out, filterOptions, 4, arrayCache);
    }

    public XZOutputStream(OutputStream out, FilterOptions[] filterOptions, int checkType) throws IOException {
        this(out, filterOptions, checkType, ArrayCache.getDefaultCache());
    }

    public XZOutputStream(OutputStream out, FilterOptions[] filterOptions, int checkType, ArrayCache arrayCache) throws IOException {
        this.arrayCache = arrayCache;
        this.out = out;
        this.setFiltersForNextBlock(filterOptions);
        this.streamFlags.checkType = checkType;
        this.check = Check.getInstance(checkType);
        this.encodeStreamHeader();
    }

    public void updateFilters(FilterOptions filterOptions) throws XZIOException {
        FilterOptions[] opts = new FilterOptions[]{filterOptions};
        this.updateFilters(opts);
    }

    public void updateFilters(FilterOptions[] filterOptions) throws XZIOException {
        if (this.blockEncoder != null) {
            throw new UnsupportedOptionsException("Changing filter options in the middle of a XZ Block not implemented");
        }
        this.setFiltersForNextBlock(filterOptions);
    }

    private void setFiltersForNextBlock(FilterOptions[] filterOptions) throws XZIOException {
        if (filterOptions.length < 1 || filterOptions.length > 4) {
            throw new UnsupportedOptionsException("XZ filter chain must be 1-4 filters");
        }
        this.filtersSupportFlushing = true;
        FilterCoder[] newFilters = new FilterEncoder[filterOptions.length];
        for (int i = 0; i < filterOptions.length; ++i) {
            newFilters[i] = filterOptions[i].getFilterEncoder();
            this.filtersSupportFlushing &= newFilters[i].supportsFlushing();
        }
        RawCoder.validate(newFilters);
        this.filters = newFilters;
    }

    @Override
    public void write(int b) throws IOException {
        this.tempBuf[0] = (byte)b;
        this.write(this.tempBuf, 0, 1);
    }

    @Override
    public void write(byte[] buf, int off, int len) throws IOException {
        if (off < 0 || len < 0 || off + len < 0 || off + len > buf.length) {
            throw new IndexOutOfBoundsException();
        }
        if (this.exception != null) {
            throw this.exception;
        }
        if (this.finished) {
            throw new XZIOException("Stream finished or closed");
        }
        try {
            if (this.blockEncoder == null) {
                this.blockEncoder = new BlockOutputStream(this.out, this.filters, this.check, this.arrayCache);
            }
            this.blockEncoder.write(buf, off, len);
        }
        catch (IOException e) {
            this.exception = e;
            throw e;
        }
    }

    public void endBlock() throws IOException {
        if (this.exception != null) {
            throw this.exception;
        }
        if (this.finished) {
            throw new XZIOException("Stream finished or closed");
        }
        if (this.blockEncoder != null) {
            try {
                this.blockEncoder.finish();
                this.index.add(this.blockEncoder.getUnpaddedSize(), this.blockEncoder.getUncompressedSize());
                this.blockEncoder = null;
            }
            catch (IOException e) {
                this.exception = e;
                throw e;
            }
        }
    }

    @Override
    public void flush() throws IOException {
        if (this.exception != null) {
            throw this.exception;
        }
        if (this.finished) {
            throw new XZIOException("Stream finished or closed");
        }
        try {
            if (this.blockEncoder != null) {
                if (this.filtersSupportFlushing) {
                    this.blockEncoder.flush();
                } else {
                    this.endBlock();
                    this.out.flush();
                }
            } else {
                this.out.flush();
            }
        }
        catch (IOException e) {
            this.exception = e;
            throw e;
        }
    }

    @Override
    public void finish() throws IOException {
        if (!this.finished) {
            this.endBlock();
            try {
                this.index.encode(this.out);
                this.encodeStreamFooter();
            }
            catch (IOException e) {
                this.exception = e;
                throw e;
            }
            this.finished = true;
        }
    }

    @Override
    public void close() throws IOException {
        if (this.out != null) {
            block6: {
                try {
                    this.finish();
                }
                catch (IOException iOException) {
                    // empty catch block
                }
                try {
                    this.out.close();
                }
                catch (IOException e) {
                    if (this.exception != null) break block6;
                    this.exception = e;
                }
            }
            this.out = null;
        }
        if (this.exception != null) {
            throw this.exception;
        }
    }

    private void encodeStreamFlags(byte[] buf, int off) {
        buf[off] = 0;
        buf[off + 1] = (byte)this.streamFlags.checkType;
    }

    private void encodeStreamHeader() throws IOException {
        this.out.write(XZ.HEADER_MAGIC);
        byte[] buf = new byte[2];
        this.encodeStreamFlags(buf, 0);
        this.out.write(buf);
        EncoderUtil.writeCRC32(this.out, buf);
    }

    private void encodeStreamFooter() throws IOException {
        byte[] buf = new byte[6];
        long backwardSize = this.index.getIndexSize() / 4L - 1L;
        for (int i = 0; i < 4; ++i) {
            buf[i] = (byte)(backwardSize >>> i * 8);
        }
        this.encodeStreamFlags(buf, 4);
        EncoderUtil.writeCRC32(this.out, buf);
        this.out.write(buf);
        this.out.write(XZ.FOOTER_MAGIC);
    }
}

