/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.opencv.data.PlanarImage
 *  org.weasis.opencv.op.ImageConversion
 */
package org.weasis.core.ui.editor.image;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.Objects;
import javax.swing.JComponent;
import javax.swing.TransferHandler;
import org.weasis.core.api.image.SimpleOpManager;
import org.weasis.opencv.data.PlanarImage;
import org.weasis.opencv.op.ImageConversion;

public class ImageTransferHandler
extends TransferHandler
implements Transferable {
    private static final DataFlavor[] flavors = new DataFlavor[]{DataFlavor.imageFlavor};
    private final SimpleOpManager disOp;

    public ImageTransferHandler(SimpleOpManager disOp) {
        this.disOp = disOp;
    }

    @Override
    public int getSourceActions(JComponent c) {
        return 1;
    }

    @Override
    public boolean canImport(JComponent comp, DataFlavor[] flavor) {
        return false;
    }

    @Override
    public Transferable createTransferable(JComponent comp) {
        return this;
    }

    @Override
    public boolean importData(JComponent comp, Transferable t) {
        return false;
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if (this.isDataFlavorSupported(flavor)) {
            return Objects.requireNonNull(ImageConversion.toBufferedImage((PlanarImage)this.disOp.process().orElse(null)));
        }
        throw new UnsupportedFlavorException(flavor);
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return flavors;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.equals(DataFlavor.imageFlavor);
    }
}

