/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import util.ReadWriteUtil;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author regesober
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //readAndWriteJpeg("Gramado_22k.jpg");
        
        try {
            BufferedImage readJpeg = ReadWriteUtil.readJpeg(args[0]);
            App app = App.getInstance();
            app.setImage1(readJpeg);
            
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void readAndWriteJpeg(String filename) {
        try {
            BufferedImage image = ReadWriteUtil.readJpeg(filename);
            ReadWriteUtil.writeJpeg("out_" + filename, image, 1.0f);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
