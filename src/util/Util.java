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
                float sr = (float)(r/255.0);
                float sg = (float)(g/255.0);
                float sb = (float)(b/255.0);
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
                rgb[i * w + 3 * j] = (byte) (rgbBuffer[0]*255.0);
                rgb[i * w + 3 * j + 1] = (byte) (rgbBuffer[1]*255.0);
                rgb[i * w + 3 * j + 2] = (byte) (rgbBuffer[2]*255.0);
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

    public static byte[] getPixelArray(BufferedImage image) {
        byte[] pixelArray = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        return pixelArray;
    }

    public static byte negativePixel(byte pixel) {
        int p = (int) pixel & 0xff;
        return (byte) (255 - p);
    }

}