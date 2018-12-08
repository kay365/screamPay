package com.qh.pay.api.utils;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.util.ImageHelper;
import net.sourceforge.tess4j.util.LoadLibs;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Tesseract4JUtils {

    public static final int WX_IMAGE_X = 1080;
    public static final int WX_IMAGE_Y = 1481;

    public static final int ALI_IMAGE_X = 1080;
    public static final int ALI_IMAGE_Y = 1638;


    private static Tesseract instance;

    private Tesseract4JUtils(){

    }

    private static void init(){
        if(instance == null){
            synchronized (Tesseract4JUtils.class){
                instance = Tesseract.getInstance();
                File tessDataFolder = LoadLibs.extractTessResources("tessdata");
                instance.setLanguage("eng");//英文库识别数字比较准确
                instance.setDatapath(tessDataFolder.getAbsolutePath());
            }
        }
    }

    public static  String doOCR(BufferedImage img, Rectangle rectangle) {
        init();
        String ret = "";
        try {
            synchronized(Tesseract4JUtils.class){
                ret = instance.doOCR(img, rectangle).trim().replace("\n","").replace("¥", "").replace(" ","");
            }
        } catch (TesseractException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static String doWxQrMoneyOcr(File imageFile){
        BufferedImage img = null;
        try {
            img = ImageIO.read(imageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return doWxQrMoneyOcr(img);
    }

    public static String doWxQrMoneyOcr(BufferedImage img){
        Rectangle rectangle = new Rectangle(240, 260, 580, 110);
        return doOCR(ImageHelper.getScaledInstance(img,WX_IMAGE_X,WX_IMAGE_Y),rectangle);
    }

    public static String doAliQrMoneyOcr(File imageFile){
        BufferedImage img = null;
        try {
            img = ImageIO.read(imageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return doAliQrMoneyOcr(img);
    }

    public static String doAliQrMoneyOcr(BufferedImage img){
        Rectangle rectangle = new Rectangle(210, 1330, 650, 70);
        return doOCR(ImageHelper.getScaledInstance(img,ALI_IMAGE_X,ALI_IMAGE_Y),rectangle);
    }

}
