package helper;

import model.BoxLabel;

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
}
