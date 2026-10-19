/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.util;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.AbstractMultiResolutionImage;
import java.awt.image.BaseMultiResolutionImage;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.image.MultiResolutionImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.function.Function;
import javax.swing.ImageIcon;

/*
 * Multiple versions of this class in jar - see https://www.benf.org/other/cfr/multi-version-jar.html
 */
public class MultiResolutionImageSupport {
    public static boolean isAvailable() {
        return true;
    }

    public static boolean isMultiResolutionImage(Image image) {
        return image instanceof MultiResolutionImage;
    }

    public static Image create(int baseImageIndex, Image ... resolutionVariants) {
        return new BaseMultiResolutionImage(baseImageIndex, resolutionVariants);
    }

    public static Image create(int baseImageIndex, Dimension[] dimensions, Function<Dimension, Image> producer) {
        return new ProducerMultiResolutionImage(dimensions, producer);
    }

    public static Image map(Image image, Function<Image, Image> mapper) {
        return image instanceof MultiResolutionImage ? new MappedMultiResolutionImage(image, mapper) : mapper.apply(image);
    }

    public static Image getResolutionVariant(Image image, int destImageWidth, int destImageHeight) {
        return image instanceof MultiResolutionImage ? ((MultiResolutionImage)((Object)image)).getResolutionVariant(destImageWidth, destImageHeight) : image;
    }

    public static List<Image> getResolutionVariants(Image image) {
        return image instanceof MultiResolutionImage ? ((MultiResolutionImage)((Object)image)).getResolutionVariants() : Collections.singletonList(image);
    }

    private static class ProducerMultiResolutionImage
    extends AbstractMultiResolutionImage {
        private final Dimension[] dimensions;
        private final Function<Dimension, Image> producer;
        private final HashMap<Dimension, Image> cache = new HashMap();

        ProducerMultiResolutionImage(Dimension[] dimensions, Function<Dimension, Image> producer) {
            this.dimensions = dimensions;
            this.producer = producer;
        }

        @Override
        public Image getResolutionVariant(double destImageWidth, double destImageHeight) {
            return this.produceAndCacheImage(new Dimension((int)destImageWidth, (int)destImageHeight));
        }

        @Override
        public List<Image> getResolutionVariants() {
            ArrayList<Image> mappedVariants = new ArrayList<Image>();
            for (Dimension size : this.dimensions) {
                mappedVariants.add(this.produceAndCacheImage(size));
            }
            return mappedVariants;
        }

        @Override
        protected Image getBaseImage() {
            return this.produceAndCacheImage(this.dimensions[0]);
        }

        @Override
        public int getWidth(ImageObserver observer) {
            return this.dimensions[0].width;
        }

        @Override
        public int getHeight(ImageObserver observer) {
            return this.dimensions[0].height;
        }

        private Image produceAndCacheImage(Dimension size) {
            return this.cache.computeIfAbsent(size, size2 -> new ImageIcon(this.producer.apply((Dimension)size2)).getImage());
        }
    }

    private static class MappedMultiResolutionImage
    extends AbstractMultiResolutionImage {
        private final Image mrImage;
        private final Function<Image, Image> mapper;
        private final IdentityHashMap<Image, Image> cache = new IdentityHashMap();

        MappedMultiResolutionImage(Image mrImage, Function<Image, Image> mapper) {
            assert (mrImage instanceof MultiResolutionImage);
            this.mrImage = mrImage;
            this.mapper = mapper;
        }

        @Override
        public Image getResolutionVariant(double destImageWidth, double destImageHeight) {
            Image variant = ((MultiResolutionImage)((Object)this.mrImage)).getResolutionVariant(destImageWidth, destImageHeight);
            return this.mapAndCacheImage(variant);
        }

        @Override
        public List<Image> getResolutionVariants() {
            List<Image> variants = ((MultiResolutionImage)((Object)this.mrImage)).getResolutionVariants();
            ArrayList<Image> mappedVariants = new ArrayList<Image>();
            for (Image image : variants) {
                mappedVariants.add(this.mapAndCacheImage(image));
            }
            return mappedVariants;
        }

        @Override
        protected Image getBaseImage() {
            return this.mapAndCacheImage(this.mrImage);
        }

        @Override
        public int getWidth(ImageObserver observer) {
            return this.mrImage.getWidth(observer);
        }

        @Override
        public int getHeight(ImageObserver observer) {
            return this.mrImage.getHeight(observer);
        }

        @Override
        public ImageProducer getSource() {
            return this.mrImage.getSource();
        }

        @Override
        public Object getProperty(String name, ImageObserver observer) {
            return this.mrImage.getProperty(name, observer);
        }

        private Image mapAndCacheImage(Image image) {
            return this.cache.computeIfAbsent(image, img -> new ImageIcon(this.mapper.apply((Image)img)).getImage());
        }
    }
}

