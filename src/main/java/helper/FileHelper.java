package helper;

public class FileHelper {
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
}
