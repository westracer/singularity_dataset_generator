package helper;

import javafx.geometry.Point2D;
import model.BoxLabel;
import model.App;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class FileHelper {
    private static final double DOUBLE_ROUND = 10000.;
    private static final int MAX_BLUR = 4;
    private static final int MAX_NOISE = 30;

    public static String getNameWithoutExtension(String str) {
        String[] dotSplit = str.split("\\.");

        if (dotSplit.length > 1) {
            dotSplit[dotSplit.length - 1] = "";

            StringBuilder newStr = new StringBuilder();
            for (String part : dotSplit) {
                newStr.append(part);
            }

            return newStr.toString();
        } else {
            return str;
        }
    }

    public static void writeLabel(FileWriter fw, BoxLabel l, double w, double h) throws IOException {
        double relW = Math.round(l.w / w * DOUBLE_ROUND) / DOUBLE_ROUND;
        double relH = Math.round(l.h / h * DOUBLE_ROUND) / DOUBLE_ROUND;
        double xCenter = Math.round((l.x / w + relW / 2) * DOUBLE_ROUND) / DOUBLE_ROUND;
        double yCenter = Math.round((l.y / h + relH / 2) * DOUBLE_ROUND) / DOUBLE_ROUND;

        String labelText = String.join(
                " ",
                Integer.toString(l.classNumber),
                Double.toString(xCenter),
                Double.toString(yCenter),
                Double.toString(relW),
                Double.toString(relH)
        );

        fw.write(labelText + '\n');
    }

    public static BufferedImage randomBlur(BufferedImage img) {
        Random r = new Random();
        int radius = r.nextInt(MAX_BLUR);
        int blurSize = radius * 2 + 1;
        float weight = 1.0f / (blurSize * blurSize);
        float[] blurData = new float[blurSize * blurSize];

        for (int i = 0; i < blurData.length; i++) {
            blurData[i] = weight;
        }

        Kernel kernel = new Kernel(blurSize, blurSize, blurData);
        ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_ZERO_FILL, null);
        return op.filter(img, null);
    }

    public static BufferedImage randomNoise(BufferedImage img) {
        BufferedImage newImg = img.getSubimage(0, 0, img.getWidth(), img.getHeight());
        Random r = new Random();

        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                Color pixel = new Color(img.getRGB(x, y));
                Color newPixel = new Color(
                        Math.max(0, Math.min(255, pixel.getRed() + ( (r.nextBoolean() ? -1 : 1) * r.nextInt(MAX_NOISE) ))),
                        Math.max(0, Math.min(255, pixel.getGreen() + ( (r.nextBoolean() ? -1 : 1) * r.nextInt(MAX_NOISE) ))),
                        Math.max(0, Math.min(255, pixel.getBlue() + ( (r.nextBoolean() ? -1 : 1) * r.nextInt(MAX_NOISE) )))
                );

                newImg.setRGB(x, y, newPixel.getRGB());
            }
        }

        return newImg;
    }

    public static BoxLabel readLabel(String[] split, double w, double h) throws NumberFormatException {
        int classNumber;
        double xRelCenter, yRelCenter, wRel, hRel;

        classNumber = Integer.parseInt(split[0]);
        xRelCenter = Double.parseDouble(split[1]);
        yRelCenter = Double.parseDouble(split[2]);
        wRel = Double.parseDouble(split[3]);
        hRel = Double.parseDouble(split[4]);

        return new BoxLabel(
                classNumber,
                xRelCenter * w -  wRel * w / 2,
                yRelCenter * h -  hRel * h / 2,
                wRel * w,
                hRel * h
        );
    }

    public static void writeCroppedLabel(String dirPath, int i, BufferedImage img, BoxLabel label) throws IOException {
        String fileName = i + ".jpg";
        File output = new File(dirPath + "/" + fileName);

        ImageIO.write(img.getSubimage((int) label.x, (int) label.y, (int) label.w, (int) label.h), "jpg", output);
    }

    public static void writeCroppedImageLabel(String dirPath, int i, BufferedImage img, ArrayList<BoxLabel> boxLabels, FileWriter labelsFW) throws IOException {
        String fileName = i + ".jpg";
        File output = new File(dirPath + "/" + fileName);

        ImageIO.write(Scalr.resize(img, 832), "jpg", output);
        File outputText = new File(dirPath + "/" + i + "." + App.TEXT_EXTENSION);
        FileWriter fw = new FileWriter(outputText, false);
        double w = img.getWidth();
        double h = img.getHeight();

        for (BoxLabel l : boxLabels) {
            FileHelper.writeLabel(fw, l, w, h);
        }

        labelsFW.write(App.PROCESS_FOLDER_NAME + "/" + fileName + "\n");
        fw.close();
    }
    
    public static AffineTransform createTransform(Point2D[] source,
                                                  Point2D[] dest) {
        double x11 = source[0].getX();
        double x12 = source[0].getY();
        double x21 = source[1].getX();
        double x22 = source[1].getY();
        double x31 = source[2].getX();
        double x32 = source[2].getY();
        double y11 = dest[0].getX();
        double y12 = dest[0].getY();
        double y21 = dest[1].getX();
        double y22 = dest[1].getY();
        double y31 = dest[2].getX();
        double y32 = dest[2].getY();

        final double v = (x11 - x21) * (x12 - x32) - (x11 - x31) * (x12 - x22);
        double a1 = ((y11-y21)*(x12-x32)-(y11-y31)*(x12-x22))/
                v;
        double v1 = (x12 - x22) * (x11 - x31) - (x12 - x32) * (x11 - x21);
        double a2 = ((y11-y21)*(x11-x31)-(y11-y31)*(x11-x21))/
                v1;
        double a3 = y11-a1*x11-a2*x12;
        double a4 = ((y12-y22)*(x12-x32)-(y12-y32)*(x12-x22))/
                v;
        double a5 = ((y12-y22)*(x11-x31)-(y12-y32)*(x11-x21))/
                v1;
        double a6 = y12-a4*x11-a5*x12;
        return new AffineTransform(a1, a4, a2, a5, a3, a6);
    }

    public static double getRandomDouble(double val) {
        Random r = new Random();
        double sign = r.nextBoolean() ? 1 : -1;

        return sign * r.nextDouble() * App.PERSPECTIVE_TRANSFORM_OFFSET * val;
    }

    public static BufferedImage applyAffineTransform(BufferedImage img, AffineTransform at) {
        int w = img.getWidth(), h = img.getHeight();

        BufferedImage warped = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = warped.createGraphics();

        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(0, 0, w + 10, h + 10);

        // TODO: find new size by polygon bounds

        g2d.setTransform(at);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.drawImage(img, 0, 0, null);
        g2d.dispose();

        return warped;
    }

    public static BufferedImage rotateImageByDegrees(BufferedImage img, double angle) {
        double rads = Math.toRadians(angle);
        double sin = Math.abs(Math.sin(rads)), cos = Math.abs(Math.cos(rads));
        int w = img.getWidth();
        int h = img.getHeight();
        int newWidth = (int) Math.floor(w * cos + h * sin);
        int newHeight = (int) Math.floor(h * cos + w * sin);

        BufferedImage rotated = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = rotated.createGraphics();
        AffineTransform at = new AffineTransform();
        at.translate((newWidth - w) / 2., (newHeight - h) / 2.);

        int x = w / 2;
        int y = h / 2;

        at.rotate(rads, x, y);
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(0, 0, newWidth + 10, newHeight + 10);

        g2d.setTransform(at);
        g2d.drawImage(img, 0, 0, null);
        g2d.dispose();

        return rotated;
    }
}
