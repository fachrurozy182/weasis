/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package org.weasis.core.ui.util;

import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.ServiceUIFactory;
import javax.print.attribute.Attribute;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.PrintServiceAttribute;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.attribute.standard.PrinterIsAcceptingJobs;
import javax.print.event.PrintServiceAttributeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.service.AuditLog;

public class ForcedAcceptPrintService
implements PrintService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ForcedAcceptPrintService.class);
    private final PrinterJob thePrintJob;
    private final PrintService delegate;

    private ForcedAcceptPrintService(PrinterJob printJob) {
        this.thePrintJob = printJob;
        this.delegate = printJob.getPrintService();
        try {
            this.thePrintJob.setPrintService(this);
        }
        catch (PrinterException e) {
            AuditLog.logError(LOGGER, e, "Set Print Service");
        }
    }

    public static void setupPrintJob(PrinterJob printJob) {
        new ForcedAcceptPrintService(printJob);
    }

    private void restoreServiceReference() {
        try {
            this.thePrintJob.setPrintService(this.delegate);
        }
        catch (PrinterException e) {
            AuditLog.logError(LOGGER, e, "Restore Print Service");
        }
    }

    @Override
    public <T extends PrintServiceAttribute> T getAttribute(Class<T> category) {
        if (category.equals(PrinterIsAcceptingJobs.class)) {
            this.restoreServiceReference();
            return (T)PrinterIsAcceptingJobs.ACCEPTING_JOBS;
        }
        return this.delegate.getAttribute(category);
    }

    @Override
    public DocPrintJob createPrintJob() {
        return this.delegate.createPrintJob();
    }

    @Override
    public void addPrintServiceAttributeListener(PrintServiceAttributeListener listener) {
        this.delegate.addPrintServiceAttributeListener(listener);
    }

    @Override
    public PrintServiceAttributeSet getAttributes() {
        return this.delegate.getAttributes();
    }

    @Override
    public Object getDefaultAttributeValue(Class<? extends Attribute> category) {
        return this.delegate.getDefaultAttributeValue(category);
    }

    @Override
    public String getName() {
        return this.delegate.getName();
    }

    @Override
    public ServiceUIFactory getServiceUIFactory() {
        return this.delegate.getServiceUIFactory();
    }

    @Override
    public Class<?>[] getSupportedAttributeCategories() {
        return this.delegate.getSupportedAttributeCategories();
    }

    @Override
    public Object getSupportedAttributeValues(Class<? extends Attribute> category, DocFlavor flavor, AttributeSet attributes) {
        return this.delegate.getSupportedAttributeValues(category, flavor, attributes);
    }

    @Override
    public DocFlavor[] getSupportedDocFlavors() {
        return this.delegate.getSupportedDocFlavors();
    }

    @Override
    public AttributeSet getUnsupportedAttributes(DocFlavor flavor, AttributeSet attributes) {
        return this.delegate.getUnsupportedAttributes(flavor, attributes);
    }

    @Override
    public boolean isAttributeCategorySupported(Class<? extends Attribute> category) {
        return this.delegate.isAttributeCategorySupported(category);
    }

    @Override
    public boolean isAttributeValueSupported(Attribute attrval, DocFlavor flavor, AttributeSet attributes) {
        return this.delegate.isAttributeValueSupported(attrval, flavor, attributes);
    }

    @Override
    public boolean isDocFlavorSupported(DocFlavor flavor) {
        return this.delegate.isDocFlavorSupported(flavor);
    }

    @Override
    public void removePrintServiceAttributeListener(PrintServiceAttributeListener listener) {
        this.delegate.removePrintServiceAttributeListener(listener);
    }
}

