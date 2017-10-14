/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
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

/**
 *
 * @author regesober
 */
public class App {

    private JPanel panel1;
    private JPanel panel2;
    private JFrame frame1;
    private JFrame frame2;
    private BufferedImage image1;
    private BufferedImage image2;

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
        frame1.setVisible(true);
        frame1.setBounds(200, 0, image1.getWidth(), image1.getHeight());
        frame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        panel2 = new JPanel(new BorderLayout());
        panel2.setVisible(true);
        frame2 = new JFrame();
        frame2.add(panel2);
        frame2.setVisible(true);
        frame2.setBounds(200 + image1.getWidth(), 0, image1.getWidth(), image1.getHeight());
        frame2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        MainFrame mainFrame = new MainFrame();
        mainFrame.setVisible(true);
        mainFrame.setBounds(0, 0, 200, 500);

        this.image1 = image1;
        panel1.add(new JLabel(new ImageIcon(image1)));
        frame1.revalidate();
    }

    public void copyImage1() {
        this.image2 = Util.copyImage(image1);
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
        frame2.revalidate();
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
                int r = (int) pixelArray[i * w + 3 * j] & 0xff;
                int g = (int) pixelArray[i * w + 3 * j + 1] & 0xff;
                int b = (int) pixelArray[i * w + 3 * j + 2] & 0xff;
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
            Util.writeJpeg(f, image2, 0.7f);
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
                pixelArray[i * w + 3 * j] = negativePixel(pixelArray[i * w + 3 * j]);
                pixelArray[i * w + 3 * j + 1] = negativePixel(pixelArray[i * w + 3 * j + 1]);
                pixelArray[i * w + 3 * j + 2] = negativePixel(pixelArray[i * w + 3 * j + 2]);
            }
        }
        updateImage2();
    }

    private byte negativePixel(byte pixel) {
        int p = (int) pixel & 0xff;
        return (byte) (255 - p);
    }

    public void histogramEqualization() {
        if (image2 == null) {
            copyImage1();
        }
        byte[] pixelArray = Util.getPixelArray(image2);
        int h = image2.getHeight();
        int w = 3 * image2.getWidth();
        byte[] grayscale = grayscale(pixelArray, h, w);
        int[] grayscaleHistogram = grayscaleHistogram(grayscale);
        int[] cumulativeHistogram = cumulativeHistogram(grayscaleHistogram, pixelArray.length/3);
        System.out.println(Arrays.toString(grayscaleHistogram));
        int sum = 0;
        for(int i = 0; i < grayscaleHistogram.length; i++) sum += grayscaleHistogram[i];
        System.out.println(sum);
        System.out.println(Arrays.toString(cumulativeHistogram));
        byte[] newImage = new byte[pixelArray.length];
        for (int i = 0; i < h * w; i++) {
            newImage[i] = (byte) cumulativeHistogram[toInt(pixelArray[i])];
        }
        System.arraycopy(newImage, 0, pixelArray, 0, pixelArray.length);
        updateImage2();
    }

    private byte[] grayscale(byte[] image, int h, int w) {
        byte[] grayscale = new byte[image.length];
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w / 3; j++) {
                int r = (int) image[i * w + 3 * j] & 0xff;
                int g = (int) image[i * w + 3 * j + 1] & 0xff;
                int b = (int) image[i * w + 3 * j + 2] & 0xff;
                double l = (0.299 * r + 0.587 * g + 0.114 * b);
                byte luminance = (byte) l;
                grayscale[i * w + 3 * j] = luminance;
                grayscale[i * w + 3 * j + 1] = luminance;
                grayscale[i * w + 3 * j + 2] = luminance;
            }
        }
        return grayscale;
    }

    private int[] grayscaleHistogram(byte[] grayscale) {
        int[] histogram = new int[256];
        for (int i = 0; i < grayscale.length; i = i + 3) {
            histogram[toInt(grayscale[i])]++;
        }
        return histogram;
    }

    private int toInt(byte b) {
        return (int) b & 0xff;
    }

    private int[] cumulativeHistogram(int[] histogram, int numberOfPixels) {
        int[] cumulativeHistogram = new int[256];
        double[] cumulativeDouble = new double[256];
        double alpha = 255.0 / numberOfPixels;
        cumulativeHistogram[0] = (int) (alpha * histogram[0]);
        cumulativeDouble[0] = alpha * histogram[0];
        System.out.println(histogram[0]);
        System.out.println(cumulativeHistogram[0]);
        System.out.println(alpha);
        for (int i = 1; i < 256; i++) {
            cumulativeDouble[i] = cumulativeDouble[i - 1] + (alpha * histogram[i]);
            cumulativeHistogram[i] = (int) cumulativeDouble[i];
        }
        return cumulativeHistogram;
    }
}
