/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

/**
 *
 * @author regesober
 */
public class Util {

    public static float[] srgb2lab(byte[] image, int h, int w) {
        float[] lab = new float[image.length];
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w / 3; j++) {
                int r = toInt(image[i * w + 3 * j]);
                int g = toInt(image[i * w + 3 * j + 1]);
                int b = toInt(image[i * w + 3 * j + 2]);
                float sr = (float) (r / 255.0);
                float sg = (float) (g / 255.0);
                float sb = (float) (b / 255.0);
                float[] labBuffer = CIELab.getInstance().fromRGB(new float[]{sr, sg, sb});
                lab[i * w + 3 * j] = labBuffer[0];
                lab[i * w + 3 * j + 1] = labBuffer[1];
                lab[i * w + 3 * j + 2] = labBuffer[2];
            }
        }
        return lab;
    }

    public static byte[] lab2srgb(float[] lab, int h, int w) {
        byte[] rgb = new byte[lab.length];
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w / 3; j++) {
                float[] labBuffer = new float[]{lab[i * w + 3 * j], lab[i * w + 3 * j + 1], lab[i * w + 3 * j + 2]};
                float[] rgbBuffer = CIELab.getInstance().toRGB(labBuffer);
                rgb[i * w + 3 * j] = (byte) (rgbBuffer[0] * 255.0);
                rgb[i * w + 3 * j + 1] = (byte) (rgbBuffer[1] * 255.0);
                rgb[i * w + 3 * j + 2] = (byte) (rgbBuffer[2] * 255.0);
            }
        }
        return rgb;
    }

    public static int[] histogram(byte[] image, int channel) {
        int[] histogram = new int[256];
        for (int i = channel; i < image.length; i = i + 3) {
            histogram[toInt(image[i])]++;
        }
        return histogram;
    }

    public static int[] histogram(float[] image, int channel) {
        int[] histogram = new int[256];
        for (int i = channel; i < image.length; i = i + 3) {
            histogram[(int) image[i]]++;
        }
        return histogram;
    }

    public static int[] histogram(float[] image, int channel, int size) {
        int[] histogram = new int[size];
        for (int i = channel; i < image.length; i = i + 3) {
            histogram[(int) Math.round(image[i])]++;
        }
        return histogram;
    }

    public static int toInt(byte b) {
        return (int) b & 0xff;
    }

    public static int[] cumulativeHistogram(int[] histogram, int numberOfPixels) {
        int[] cumulativeHistogram = new int[256];
        double[] cumulativeDouble = new double[256];
        double alpha = 255.0 / numberOfPixels;
        cumulativeHistogram[0] = (int) (alpha * histogram[0]);
        cumulativeDouble[0] = alpha * histogram[0];
        for (int i = 1; i < 256; i++) {
            cumulativeDouble[i] = cumulativeDouble[i - 1] + (alpha * histogram[i]);
            cumulativeHistogram[i] = (int) cumulativeDouble[i];
        }
        return cumulativeHistogram;
    }

    public static int[] cumulativeHistogram(int[] histogram, int numberOfPixels, int size) {
        int[] cumulativeHistogram = new int[size];
        double[] cumulativeDouble = new double[size];
        double alpha = ((float) size) / numberOfPixels;
        cumulativeHistogram[0] = (int) (alpha * histogram[0]);
        cumulativeDouble[0] = alpha * histogram[0];
        for (int i = 1; i < size; i++) {
            cumulativeDouble[i] = cumulativeDouble[i - 1] + (alpha * histogram[i]);
            cumulativeHistogram[i] = (int) cumulativeDouble[i];
        }
        return cumulativeHistogram;
    }

    public static byte[] grayscale(byte[] image, int h, int w) {
        byte[] grayscale = new byte[image.length];
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w / 3; j++) {
                int r = toInt(image[i * w + 3 * j]);
                int g = toInt(image[i * w + 3 * j + 1]);
                int b = toInt(image[i * w + 3 * j + 2]);
                double l = (0.299 * r + 0.587 * g + 0.114 * b);
                byte luminance = (byte) l;
                grayscale[i * w + 3 * j] = luminance;
                grayscale[i * w + 3 * j + 1] = luminance;
                grayscale[i * w + 3 * j + 2] = luminance;
            }
        }
        return grayscale;
    }

    public static void grayscaleInPlace(byte[] image, int h, int w) {
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w / 3; j++) {
                int r = toInt(image[i * w + 3 * j]);
                int g = toInt(image[i * w + 3 * j + 1]);
                int b = toInt(image[i * w + 3 * j + 2]);
                double l = (0.299 * r + 0.587 * g + 0.114 * b);
                byte luminance = (byte) l;
                image[i * w + 3 * j] = luminance;
                image[i * w + 3 * j + 1] = luminance;
                image[i * w + 3 * j + 2] = luminance;
            }
        }
    }

    public static byte[] getPixelArray(BufferedImage image) {
        byte[] pixelArray = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        return pixelArray;
    }

    public static byte negativePixel(byte pixel) {
        int p = (int) pixel & 0xff;
        return (byte) (255 - p);
    }

    public static int closestShade(int[] histogram, int shade) {
        int dif = Math.abs(histogram[0] - shade);
        int ind = 0;
        for (int i = 1; i < 256; i++) {
            int newDif = Math.abs(histogram[i] - shade);
            if (newDif < dif) {
                dif = newDif;
                ind = i;
            }
        }
        return histogram[ind];
    }

    public static void copyPixel(byte[] source, int i, byte[] target, int j) {
        target[j] = source[i];
        target[j + 1] = source[i + 1];
        target[j + 2] = source[i + 2];
    }

    public static void interpolatePixel(byte[] pixelArray, int i, int j, int k) {
        pixelArray[k] = (byte) ((toInt(pixelArray[i]) + toInt(pixelArray[j])) / 2);
        pixelArray[k + 1] = (byte) ((toInt(pixelArray[i + 1]) + toInt(pixelArray[j + 1])) / 2);
        pixelArray[k + 2] = (byte) ((toInt(pixelArray[i + 2]) + toInt(pixelArray[j + 2])) / 2);
    }

    public static void switchPixel(byte[] pixelArray, int i, int j) {
        byte temp = pixelArray[i];
        pixelArray[i] = pixelArray[j];
        pixelArray[j] = temp;
    }

    public static void copyImage(byte[] source, byte[] target) {
        System.arraycopy(source, 0, target, 0, source.length);
    }

    public static void convolutePixel(byte[] source, byte[] target, double[] filter, int i, int j, int h, int w) {
        int p22 = i * w + j;
        int p12 = p22 - w;
        int p11 = p12 - 3;
        int p13 = p12 + 3;
        int p21 = p22 - 3;
        int p23 = p22 + 3;
        int p32 = p22 + w;
        int p31 = p32 - 3;
        int p33 = p32 + 3;
        int[] pixelIndexes = new int[]{p11, p12, p13, p21, p22, p23, p31, p32, p33};
        double[] bgr = new double[3];
        double b = 0;
        double g = 0;
        double r = 0;
        for (int k = 0; k < pixelIndexes.length; k++) {
            multiplyPixel(source, bgr, pixelIndexes[k], filter[k]);
            b += bgr[0];
            g += bgr[1];
            r += bgr[2];
        }
        target[p22] = toByte((int) b);
        target[p22 + 1] = toByte((int) g);
        target[p22 + 2] = toByte((int) r);
    }

    public static void multiplyPixel(byte[] source, double[] bgr, int i, double value) {
        bgr[0] = toInt(source[i]) * value;
        bgr[1] = toInt(source[i + 1]) * value;
        bgr[2] = toInt(source[i + 2]) * value;
    }

    public static byte toByte(int a) {
        if (a > 255) {
            return (byte) 255;
        }
        if (a < 0) {
            return (byte) 0;
        }
        return (byte) a;
    }

}
