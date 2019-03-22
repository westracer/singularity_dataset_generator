package helper;

import model.BoxLabel;
import model.App;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileHelper {
    private static final double DOUBLE_ROUND = 10000.;

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

    public static void writeCroppedLabel(String dirPath, int i, BufferedImage img, int classNumber, FileWriter labelsFW) throws IOException {
        String fileName = i + ".jpg";
        File output = new File(dirPath + "/" + fileName);
        ImageIO.write(img, "jpg", output);
        File outputText = new File(dirPath + "/" + i + "." + App.TEXT_EXTENSION);
        FileWriter fw = new FileWriter(outputText, false);
        fw.write(classNumber + " 0.5 0.5 1.0 1.0");
        labelsFW.write(App.PROCESS_FOLDER_NAME + "/" + fileName + "\n");
        fw.close();
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
        g2d.fillRect(0, 0, newWidth - 1, newHeight - 1);

        g2d.setTransform(at);
        g2d.drawImage(img, 0, 0, null);
        g2d.dispose();

        return rotated;
    }
}
