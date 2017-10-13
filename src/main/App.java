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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
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
        panel1.removeAll();
        panel1.add(new JLabel(new ImageIcon(image1)));
        frame1.revalidate();
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
        panel1.removeAll();
        panel1.add(new JLabel(new ImageIcon(image1)));
        frame1.revalidate();
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
        panel2.removeAll();
        panel2.add(new JLabel(new ImageIcon(image2)));
        frame2.revalidate();
    }

    public void quantize(String input) {
        try {
            int quant = Integer.parseInt(input);
            luminance();
            if (quant < 1 || quant > 256) {
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
                    pixelArray[i * w + 3 * j] = quantizePixel(pixelArray[i * w + 3 * j], quant);
                    pixelArray[i * w + 3 * j + 1] = quantizePixel(pixelArray[i * w + 3 * j + 1], quant);
                    pixelArray[i * w + 3 * j + 2] = quantizePixel(pixelArray[i * w + 3 * j + 2], quant);
                }
            }
            panel2.removeAll();
            panel2.add(new JLabel(new ImageIcon(image2)));
            frame2.revalidate();
        } catch (NumberFormatException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private byte quantizePixel(byte pixel, int quant) {
        int p = (int) pixel & 0xff;
        p = p / (256 / quant) * 256 / quant;
        p += 256 / (2*quant);
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
        double[] data = new double[pixelArray.length/3];
        for (int i = 0; i < pixelArray.length; i=i+3) {
            data[i/3] = (double) (pixelArray[i] & 0xff);
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

}
