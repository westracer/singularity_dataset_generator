package main.helper;

import java.io.File;

public class FileHelper {
    public static String getFileNameWithoutExtension(File f) {
        String fName = f.getName();
        String[] dotSplit = fName.split("\\.");

        if (dotSplit.length > 1) {
            return dotSplit[dotSplit.length - 1].toLowerCase();
        } else {
            return "";
        }
    }
}
