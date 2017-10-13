/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.FileImageOutputStream;

/**
 *
 * @author regesober
 */
public class Util {

    public static BufferedImage readJpeg(String filename) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(new File(filename));
        return bufferedImage;
    }

    public static void writeJpeg(String filename, BufferedImage image, float compression) throws IOException {
        writeJpeg(new File(filename), image, compression);
    }

    public static void writeJpeg(File file, BufferedImage image, float compression) throws IOException {
        JPEGImageWriteParam jpegParams = new JPEGImageWriteParam(null);
        jpegParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        jpegParams.setCompressionQuality(compression);

        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
        FileImageOutputStream fios = new FileImageOutputStream(file);
        writer.setOutput(fios);
        writer.write(null, new IIOImage(image, null, null), jpegParams);
        fios.flush();
        writer.dispose();
        fios.close();
    }

    public static BufferedImage copyImage(BufferedImage image) {
        ColorModel cm = image.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = image.copyData(image.getRaster().createCompatibleWritableRaster());
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    public static byte[] getPixelArray(BufferedImage image) {
        byte[] pixelArray = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        return pixelArray;
    }

}
