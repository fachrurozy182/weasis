/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.util;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.logging.Logger;
import com.github.weisj.jsvg.logging.impl.LogFactory;
import com.github.weisj.jsvg.parser.DomDocument;
import com.github.weisj.jsvg.parser.SVGLoader;
import com.github.weisj.jsvg.parser.resources.RenderableResource;
import com.github.weisj.jsvg.parser.resources.impl.ImageResource;
import com.github.weisj.jsvg.parser.resources.impl.SVGResource;
import com.github.weisj.jsvg.util.DataUri;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ResourceUtil {
    private static final Logger LOGGER = LogFactory.createLogger(ResourceUtil.class);
    private static final Set<String> SUPPORTED_MIME_TYPES = Arrays.stream(ImageIO.getReaderFormatNames()).map(s -> "image/" + s.toLowerCase(Locale.ENGLISH)).collect(Collectors.toSet());

    private ResourceUtil() {
    }

    @Nullable
    public static RenderableResource loadImage(@NotNull DomDocument document, @NotNull URI uri) throws IOException {
        BufferedImage img;
        URI resourceUri = document.loaderContext().externalResourcePolicy().resolveResourceURI(document.rootURI(), uri);
        if (resourceUri == null) {
            return null;
        }
        String path = resourceUri.getPath();
        if (path != null && path.endsWith(".svg")) {
            SVGLoader loader = new SVGLoader();
            try {
                SVGDocument imageDocument = loader.load(uri.toURL(), document.loaderContext());
                if (imageDocument != null) {
                    return new SVGResource(imageDocument);
                }
            }
            catch (Exception e) {
                LOGGER.log(Logger.Level.WARNING, "Could not load svg resource", e);
            }
        }
        if ((img = ResourceUtil.loadToBufferedImage(uri)) == null) {
            return null;
        }
        return new ImageResource(img);
    }

    @Nullable
    private static BufferedImage loadToBufferedImage(@NotNull URI uri) throws IOException {
        String scheme = uri.getScheme();
        if ("data".equals(scheme)) {
            DataUri dataUri = DataUri.parse(uri.toString(), StandardCharsets.UTF_8);
            if (!ResourceUtil.isSupportedMimeType(dataUri.mime())) {
                throw new IOException("Unsupported Mime type " + dataUri.mime());
            }
            try (ByteArrayInputStream in = new ByteArrayInputStream(dataUri.data());){
                BufferedImage bufferedImage = ResourceUtil.readPossiblyCorruptedFile(in);
                return bufferedImage;
            }
        }
        return ImageIO.read(uri.toURL());
    }

    private static boolean isSupportedMimeType(@NotNull String mimeType) {
        return SUPPORTED_MIME_TYPES.contains(mimeType.toLowerCase(Locale.ENGLISH));
    }

    @Nullable
    private static BufferedImage readPossiblyCorruptedFile(@NotNull InputStream inputStream) throws IOException {
        ImageInputStream input = ImageIO.createImageInputStream(inputStream);
        Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
        if (!readers.hasNext()) {
            return null;
        }
        ImageReader reader = readers.next();
        reader.setInput(input);
        ImageReadParam param = reader.getDefaultReadParam();
        BufferedImage image = reader.getImageTypes(0).next().createBufferedImage(reader.getWidth(0), reader.getHeight(0));
        param.setDestination(image);
        try {
            image = reader.read(0, param);
        }
        catch (Exception e) {
            LOGGER.log(Logger.Level.WARNING, e.getMessage(), e);
        }
        return image;
    }
}

