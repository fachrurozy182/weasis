/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.xml.bind.JAXBContext
 *  jakarta.xml.bind.JAXBException
 *  jakarta.xml.bind.Marshaller
 *  jakarta.xml.bind.Unmarshaller
 *  org.glassfish.jaxb.runtime.v2.ContextFactory
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package org.weasis.core.ui.serialize;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.util.Map;
import org.glassfish.jaxb.runtime.v2.ContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.api.util.GzipManager;
import org.weasis.core.ui.model.GraphicModel;
import org.weasis.core.ui.model.imp.XmlGraphicModel;

public class XmlSerializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(XmlSerializer.class);

    private XmlSerializer() {
    }

    public static GraphicModel readPresentationModel(File gpxFile) {
        if (gpxFile.canRead()) {
            try {
                JAXBContext jaxbContext = XmlSerializer.getJaxbContext(XmlGraphicModel.class);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                return XmlSerializer.getGraphicModel((GraphicModel)jaxbUnmarshaller.unmarshal(gpxFile));
            }
            catch (Exception e) {
                LOGGER.error("Cannot load xml: ", (Throwable)e);
            }
        }
        return null;
    }

    public static void writePresentation(ImageElement img, File destinationFile) {
        GraphicModel model = (GraphicModel)img.getTagValue(TagW.PresentationModel);
        if (model != null && !model.getModels().isEmpty()) {
            File gpxFile = new File(destinationFile.getParent(), destinationFile.getName() + ".xml");
            try {
                JAXBContext jaxbContext = XmlSerializer.getJaxbContext(model.getClass());
                Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
                jaxbMarshaller.setProperty("jaxb.formatted.output", (Object)true);
                jaxbMarshaller.marshal((Object)model, gpxFile);
            }
            catch (Exception e) {
                LOGGER.error("Cannot save xml: ", (Throwable)e);
            }
        }
    }

    public static <T> T deserialize(Reader reader, Class<T> clazz) throws JAXBException {
        JAXBContext context = XmlSerializer.getJaxbContext(clazz);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return (T)unmarshaller.unmarshal(reader);
    }

    public static GraphicModel buildPresentationModel(byte[] gzipData) {
        try {
            JAXBContext jaxbContext = XmlSerializer.getJaxbContext(XmlGraphicModel.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            ByteArrayInputStream inputStream = new ByteArrayInputStream(GzipManager.gzipUncompressToByte(gzipData));
            return XmlSerializer.getGraphicModel((GraphicModel)jaxbUnmarshaller.unmarshal((InputStream)inputStream));
        }
        catch (Exception e) {
            LOGGER.error("Cannot load xml graphic model: ", (Throwable)e);
            return null;
        }
    }

    private static GraphicModel getGraphicModel(GraphicModel model) {
        int length = model.getModels().size();
        model.getModels().removeIf(g -> g.getLayer() == null);
        if (length > model.getModels().size()) {
            LOGGER.error("Removing {} graphics without a attached layer", (Object)(model.getModels().size() - length));
        }
        return model;
    }

    public static JAXBContext getJaxbContext(Class<?> ... clazz) throws JAXBException {
        return XmlSerializer.getJaxbContext(null, clazz);
    }

    public static JAXBContext getJaxbContext(Map<String, Object> properties, Class<?> ... clazz) throws JAXBException {
        return ContextFactory.createContext((Class[])clazz, properties);
    }
}

