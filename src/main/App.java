/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import util.ReadWriteUtil;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;
import util.Util;

/**
 *
 * @author regesober
 */
public class App {

    private JPanel panel1;
    private JPanel panel2;
    private JFrame frame1;
    private JFrame frame2;
    private MainFrame mainFrame;
    private FilterFrame filterFrame;
    private BufferedImage image1;
    private BufferedImage image2;

    private static final int MAIN_FRAME_H = 700;
    private static final int MAIN_FRAME_W = 250;

    private static final int FILTER_FRAME_H = 380;
    private static final int FILTER_FRAME_W = 250;

    private static final int HEIGHT_INCREMENT = 35;

    private static App a = null;

    private App() {
    }

    public static App getInstance() {
        if (a == null) {
            a = new App();
        }
        return a;
    }

    /**
     * @param image1 the image1 to set
     */
    public void setImage1(BufferedImage image1) {
        panel1 = new JPanel(new BorderLayout());
        panel1.setVisible(true);
        frame1 = new JFrame();
        frame1.add(panel1);
        frame1.setTitle("ORIGINAL IMAGE");
        frame1.setVisible(true);
        frame1.setBounds(MAIN_FRAME_W, 0, image1.getWidth(), image1.getHeight() + HEIGHT_INCREMENT);
        frame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        panel2 = new JPanel(new BorderLayout());
        panel2.setVisible(true);
        frame2 = new JFrame();
        frame2.add(panel2);
        frame2.setTitle("WORKBENCH IMAGE");
        frame2.setVisible(true);
        frame2.setBounds(MAIN_FRAME_W, image1.getHeight() + HEIGHT_INCREMENT, image1.getWidth(), image1.getHeight() + HEIGHT_INCREMENT);
        frame2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        mainFrame = new MainFrame();
        mainFrame.setVisible(true);
        mainFrame.setBounds(0, 0, MAIN_FRAME_W, MAIN_FRAME_H);

        filterFrame = new FilterFrame();
        filterFrame.setVisible(true);
        filterFrame.setBounds(MAIN_FRAME_W + image1.getWidth(), 0, FILTER_FRAME_W, FILTER_FRAME_H);
        filterFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.image1 = image1;
        panel1.add(new JLabel(new ImageIcon(image1)));
        frame1.revalidate();
    }

    public MainFrame getMainFrame() {
        return mainFrame;
    }

    public void copyImage1() {
        this.image2 = ReadWriteUtil.copyImage(image1);
        updateImage2();
    }

    private void updateImage1() {
        panel1.removeAll();
        panel1.add(new JLabel(new ImageIcon(image1)));
        frame1.revalidate();
    }

    private void updateImage2() {
        panel2.removeAll();
        panel2.add(new JLabel(new ImageIcon(image2)));
        frame2.setBounds(MAIN_FRAME_W, image1.getHeight() + HEIGHT_INCREMENT, image2.getWidth(), image2.getHeight() + HEIGHT_INCREMENT);
        frame2.revalidate();
    }

    private void updateImage2(byte[] pixelArray, int h, int w) {
        try {
            BufferedImage temp = new BufferedImage(w / 3, h, BufferedImage.TYPE_3BYTE_BGR);
            for (int i = 0; i < h; i++) {
                for (int j = 0; j < w && j / 3 < w / 3; j = j + 3) {
                    int rgb = Util.toInt(pixelArray[i * w + j + 2]);
                    rgb = (rgb << 8) + Util.toInt(pixelArray[i * w + j + 1]);
                    rgb = (rgb << 8) + Util.toInt(pixelArray[i * w + j]);
                    temp.setRGB(j / 3, i, rgb);
                }
            }
            image2 = temp;
            updateImage2();
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void mirrrorVertical() {
        int h = image1.getHeight();
        int w = 3 * image1.getWidth();
        byte[] pixelArray = Util.getPixelArray(image1);
        byte[] buffer = new byte[w];
        for (int i = 0; i < h / 2; i++) {
            System.arraycopy(pixelArray, i * w, buffer, 0, w);
            System.arraycopy(pixelArray, (h - i - 1) * w, pixelArray, i * w, w);
            System.arraycopy(buffer, 0, pixelArray, (h - 1 - i) * w, w);
        }
        updateImage1();
    }

    public void mirrorHorizontal() {
        int h = image1.getHeight();
        int w = 3 * image1.getWidth();
        byte[] pixelArray = Util.getPixelArray(image1);
        byte[] buffer = new byte[3];
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w / 6; j++) {
                System.arraycopy(pixelArray, i * w + 3 * j, buffer, 0, 3);
                System.arraycopy(pixelArray, (i + 1) * w - (j + 1) * 3, pixelArray, i * w + 3 * j, 3);
                System.arraycopy(buffer, 0, pixelArray, (i + 1) * w - (j + 1) * 3, 3);
            }
        }
        updateImage1();
    }

    public void luminance() {
        if (image2 == null) {
            copyImage1();
        }
        int h = image2.getHeight();
        int w = 3 * image2.getWidth();
        byte[] pixelArray = Util.getPixelArray(image2);
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w / 3; j++) {
                int r = Util.toInt(pixelArray[i * w + 3 * j]);
                int g = Util.toInt(pixelArray[i * w + 3 * j + 1]);
                int b = Util.toInt(pixelArray[i * w + 3 * j + 2]);
                double l = (0.299 * r + 0.587 * g + 0.114 * b);
                byte luminance = (byte) l;
                pixelArray[i * w + 3 * j] = luminance;
                pixelArray[i * w + 3 * j + 1] = luminance;
                pixelArray[i * w + 3 * j + 2] = luminance;
            }
        }
        updateImage2();
    }

    public void quantize(String input) {
        try {
            int quant = Integer.parseInt(input);
            if (quant < 1 || quant > 256) {
                JOptionPane.showMessageDialog(null, "The number of tones must be an Integer in [1,256].", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            luminance();
            if (image2 == null) {
                copyImage1();
            }
            int h = image2.getHeight();
            int w = 3 * image2.getWidth();
            byte[] pixelArray = Util.getPixelArray(image2);
            for (int i = 0; i < h; i++) {
                for (int j = 0; j < w / 3; j++) {
                    pixelArray[i * w + 3 * j] = quantizePixel(pixelArray[i * w + 3 * j], quant);
                    pixelArray[i * w + 3 * j + 1] = quantizePixel(pixelArray[i * w + 3 * j + 1], quant);
                    pixelArray[i * w + 3 * j + 2] = quantizePixel(pixelArray[i * w + 3 * j + 2], quant);
                }
            }
            updateImage2();
        } catch (NumberFormatException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private byte quantizePixel(byte pixel, int quant) {
        int p = (int) pixel & 0xff;
        p = p / (256 / quant) * 256 / quant;
        p += 256 / (2 * quant);
        return (byte) p;
    }

    public void saveImage(File f) {
        if (image2 == null) {
            copyImage1();
        }
        try {
            ReadWriteUtil.writeJpeg(f, image2, 0.7f);
        } catch (IOException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void histogram() {
        luminance();
        byte[] pixelArray = Util.getPixelArray(image2);
        double[] data = new double[pixelArray.length / 3];
        for (int i = 0; i < pixelArray.length; i = i + 3) {
            data[i / 3] = (double) (pixelArray[i] & 0xff);
        }
        HistogramDataset hd = new HistogramDataset();
        hd.setType(HistogramType.FREQUENCY);
        hd.addSeries("Frequencies", data, 256, 0, 255);
        JFreeChart chart = ChartFactory.createHistogram("Histogram", "Pixel Value", "Frequency", hd, PlotOrientation.VERTICAL, false, false, false);
        chart.setBackgroundPaint(Color.white);
        ChartFrame cf = new ChartFrame("Frequency Histogram", chart);
        cf.pack();
        cf.setVisible(true);
    }

    public void brightness(String input) {
        try {
            int quant = Integer.parseInt(input);
            if (quant < -255 || quant > 255) {
                JOptionPane.showMessageDialog(null, "Brightness must be an Integer in [-255,255].", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (image2 == null) {
                copyImage1();
            }
            int h = image2.getHeight();
            int w = 3 * image2.getWidth();
            byte[] pixelArray = Util.getPixelArray(image2);
            for (int i = 0; i < h; i++) {
                for (int j = 0; j < w / 3; j++) {
                    pixelArray[i * w + 3 * j] = addBrightness(pixelArray[i * w + 3 * j], quant);
                    pixelArray[i * w + 3 * j + 1] = addBrightness(pixelArray[i * w + 3 * j + 1], quant);
                    pixelArray[i * w + 3 * j + 2] = addBrightness(pixelArray[i * w + 3 * j + 2], quant);
                }
            }
            updateImage2();
        } catch (NumberFormatException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private byte addBrightness(byte pixel, int quant) {
        int p = (int) pixel & 0xff;
        p += quant;
        if (p > 255) {
            p = 255;
        }
        if (p < 0) {
            p = 0;
        }
        return (byte) p;
    }

    public void contrast(String input) {
        try {
            double gain = Double.parseDouble(input);
            if (gain < 0 || gain > 255) {
                JOptionPane.showMessageDialog(null, "Contrast must be a Double in [-255,255].", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (image2 == null) {
                copyImage1();
            }
            int h = image2.getHeight();
            int w = 3 * image2.getWidth();
            byte[] pixelArray = Util.getPixelArray(image2);
            for (int i = 0; i < h; i++) {
                for (int j = 0; j < w / 3; j++) {
                    pixelArray[i * w + 3 * j] = changeContrast(pixelArray[i * w + 3 * j], gain);
                    pixelArray[i * w + 3 * j + 1] = changeContrast(pixelArray[i * w + 3 * j + 1], gain);
                    pixelArray[i * w + 3 * j + 2] = changeContrast(pixelArray[i * w + 3 * j + 2], gain);
                }
            }
            updateImage2();
        } catch (NumberFormatException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private byte changeContrast(byte pixel, double gain) {
        int p = (int) pixel & 0xff;
        p *= gain;
        if (p > 255) {
            p = 255;
        }
        if (p < 0) {
            p = 0;
        }
        return (byte) p;
    }

    public void negative() {
        if (image2 == null) {
            copyImage1();
        }
        int h = image2.getHeight();
        int w = 3 * image2.getWidth();
        byte[] pixelArray = Util.getPixelArray(image2);
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w / 3; j++) {
                pixelArray[i * w + 3 * j] = Util.negativePixel(pixelArray[i * w + 3 * j]);
                pixelArray[i * w + 3 * j + 1] = Util.negativePixel(pixelArray[i * w + 3 * j + 1]);
                pixelArray[i * w + 3 * j + 2] = Util.negativePixel(pixelArray[i * w + 3 * j + 2]);
            }
        }
        updateImage2();
    }

    public void histogramEqualization() {
        if (image2 == null) {
            copyImage1();
        }
        byte[] pixelArray = Util.getPixelArray(image2);
        int h = image2.getHeight();
        int w = 3 * image2.getWidth();
        byte[] grayscale = Util.grayscale(pixelArray, h, w);
        int[] grayscaleHistogram = Util.histogram(grayscale, 0);
        int[] cumulativeHistogram = Util.cumulativeHistogram(grayscaleHistogram, pixelArray.length / 3);
        byte[] newImage = new byte[pixelArray.length];
        for (int i = 0; i < h * w; i++) {
            newImage[i] = (byte) cumulativeHistogram[Util.toInt(pixelArray[i])];
        }
        System.arraycopy(newImage, 0, pixelArray, 0, pixelArray.length);
        updateImage2();
    }

    public void histogramEqualizationLab() {
        if (image2 == null) {
            copyImage1();
        }
        byte[] pixelArray = Util.getPixelArray(image2);
        int h = image2.getHeight();
        int w = 3 * image2.getWidth();
        float[] lab = Util.srgb2lab(pixelArray, h, w);
        int[] histogramL = Util.histogram(lab, 0, 101);
        int[] cumulativeHistogram = Util.cumulativeHistogram(histogramL, pixelArray.length / 3, 101);
        float[] newImageLab = lab.clone();
        for (int i = 0; i < h * w; i = i + 3) {
            newImageLab[i] = (float) (cumulativeHistogram[(int) Math.round(lab[i])]) + 10;
        }
        byte[] newImageSrgb = Util.lab2srgb(newImageLab, h, w);
        System.arraycopy(newImageSrgb, 0, pixelArray, 0, pixelArray.length);
        updateImage2();
    }

    public void histogramMatching(File f) throws IOException {
        if (image2 == null) {
            copyImage1();
        }
        BufferedImage targetBI = ReadWriteUtil.readJpeg(f);
        byte[] source = Util.getPixelArray(image2);
        byte[] target = Util.getPixelArray(targetBI);
        Util.grayscaleInPlace(source, image2.getHeight(), image2.getWidth() * 3);
        Util.grayscaleInPlace(target, targetBI.getHeight(), targetBI.getWidth() * 3);
        int[] sourceHistogram = Util.histogram(source, 0);
        int[] targetHistogram = Util.histogram(target, 0);
        int[] sourceCumulativeHistogram = Util.cumulativeHistogram(sourceHistogram, source.length / 3);
        int[] targetCumulativeHistogram = Util.cumulativeHistogram(targetHistogram, target.length / 3);
        int[] HM = new int[256];
        byte[] sourceHM = new byte[source.length];
        for (int i = 0; i < 256; i++) {
            HM[i] = Util.closestShade(targetCumulativeHistogram, sourceCumulativeHistogram[i]);
        }
        for (int i = 0; i < image2.getHeight() * image2.getWidth() * 3; i = i + 3) {
            sourceHM[i] = (byte) HM[Util.toInt(source[i])];
            sourceHM[i + 1] = sourceHM[i];
            sourceHM[i + 2] = sourceHM[i];
        }
        System.arraycopy(sourceHM, 0, source, 0, source.length);
        updateImage2();
    }

    public void zoomOut(String x, String y) {
        try {
            int sx = Integer.parseInt(x);
            int sy = Integer.parseInt(y);
            if (image2 == null) {
                copyImage1();
            }
            byte[] pixelArray = Util.getPixelArray(image2);
            int h = image2.getHeight();
            int w = 3 * image2.getWidth();
            int newH = h / sx;
            int newW = w / sy / 3 * 3;
            if (newH == 0 || newW == 0) {
                return;
            }
            byte[] newImage = new byte[pixelArray.length];
            for (int i = 0; i < h; i = i + sx) {
                for (int j = 0; j < w / sy / 3 * 3 * sy; j = j + 3 * sy) {
                    int ra = 0;
                    int ga = 0;
                    int ba = 0;
                    int den = 0;
                    for (int ix = i; ix < i + sx && ix < h; ix++) {
                        for (int jy = j; jy < j + sy && jy < w; jy = jy + 3) {
                            den++;
                            ra += Util.toInt(pixelArray[ix * w + jy]);
                            ga += Util.toInt(pixelArray[ix * w + jy + 1]);
                            ba += Util.toInt(pixelArray[ix * w + jy + 2]);
                        }
                    }
                    newImage[(i / sx) * newW + j / sy] = (byte) (ra / den);
                    newImage[(i / sx) * newW + j / sy + 1] = (byte) (ga / den);
                    newImage[(i / sx) * newW + j / sy + 2] = (byte) (ba / den);
                }
            }
            updateImage2(newImage, newH, newW);
        } catch (NumberFormatException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void zoomIn() {
        if (image2 == null) {
            copyImage1();
        }
        byte[] pixelArray = Util.getPixelArray(image2);
        byte[] newImage = new byte[4 * pixelArray.length];
        int h = image2.getHeight();
        int w = 3 * image2.getWidth();
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j = j + 3) {
                Util.copyPixel(pixelArray, i * w + j, newImage, i * w * 4 + j * 2);
            }
        }
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j = j + 3) {
                int p11 = i * w * 4 + j * 2;
                int p12 = p11 + 3;
                int p13 = p12 + 3;
                if (j < w - 3) {
                    Util.interpolatePixel(newImage, p11, p13, p12);
                } else {
                    Util.copyPixel(newImage, p11, newImage, p12);
                }
            }
        }
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j = j + 3) {
                int p11 = i * w * 4 + j * 2;
                int p12 = p11 + 3;
                int p21 = p11 + w * 2;
                int p22 = p21 + 3;
                int p31 = p21 + w * 2;
                int p32 = p31 + 3;
                if (i < h - 1) {
                    Util.interpolatePixel(newImage, p11, p31, p21);
                    Util.interpolatePixel(newImage, p12, p32, p22);
                } else {
                    Util.copyPixel(newImage, p11, newImage, p21);
                    Util.copyPixel(newImage, p12, newImage, p22);
                }
            }
        }
        updateImage2(newImage, h * 2, w * 2);
    }

    public void turn90counterClockwise() {
        if (image2 == null) {
            copyImage1();
        }
        byte[] pixelArray = Util.getPixelArray(image2);
        byte[] newImage = new byte[pixelArray.length];
        int h = image2.getHeight();
        int w = 3 * image2.getWidth();
        for (int i = 0; i < h - 1; i++) {
            for (int j = 0; j < w; j = j + 3) {
                Util.copyPixel(pixelArray, i * w + j, newImage, (w / 3 - 1 - j / 3) * h * 3 + i * 3);
            }
        }
        updateImage2(newImage, w / 3, h * 3);
    }

    public void turn90clockwise() {
        if (image2 == null) {
            copyImage1();
        }
        byte[] pixelArray = Util.getPixelArray(image2);
        byte[] newImage = new byte[pixelArray.length];
        int h = image2.getHeight();
        int w = 3 * image2.getWidth();
        for (int i = 0; i < h - 1; i++) {
            for (int j = 0; j < w; j = j + 3) {
                Util.copyPixel(pixelArray, i * w + j, newImage, (j / 3) * h * 3 + (h - 1 - i) * 3);
            }
        }
        updateImage2(newImage, w / 3, h * 3);
    }

    public void convolute(boolean sum127) {
        if (image2 == null) {
            copyImage1();
        }
        double[] filter = filterFrame.getFilter();
        if (filter == null) {
            return;
        }
        byte[] pixelArray = Util.getPixelArray(image2);
        byte[] newImage = new byte[pixelArray.length];
        Util.copyImage(pixelArray, newImage);
        int h = image2.getHeight();
        int w = 3 * image2.getWidth();
        for (int i = 1; i < h - 1; i++) {
            for (int j = 3; j < w - 3; j = j + 3) {
                Util.convolutePixel(pixelArray, newImage, filter, i, j, h, w);
            }
        }
        Util.copyImage(newImage, pixelArray);
        updateImage2();
    }

}
