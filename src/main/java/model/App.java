package model;

import helper.FileHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class App {
    public App() {}

    final static String[] IMAGE_EXTENSIONS = new String[] {"jpg", "jpeg", "png"};
    final static String TEXT_EXTENSION = "txt";

    private File _openedDirectory;
    private File[] _pics;
    private Map<String, File> _textFiles = new HashMap<>();

    public void openDirectory(File dir) {
        this._openedDirectory = dir;

        File[] files = this._openedDirectory.listFiles();
        if (files == null) return;

        ArrayList<File> picList = new ArrayList<>();
        for (File f : files) {
            String fName = f.getName();
            String[] dotSplit = fName.split("\\.");

            if (dotSplit.length > 1) {
                String ext = dotSplit[dotSplit.length - 1].toLowerCase();

                if (Arrays.binarySearch(IMAGE_EXTENSIONS, ext) > -1) {
                    picList.add(f);
                } else if (ext.equals(TEXT_EXTENSION)) {
                    _textFiles.putIfAbsent(FileHelper.getFileNameWithoutExtension(f), f);
                }
            }
        }

        _pics = picList.toArray(new File[0]);
    }
}
