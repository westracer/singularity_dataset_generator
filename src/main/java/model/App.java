package model;

import helper.FileHelper;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class App {
    public App() {}

    final static String[] IMAGE_EXTENSIONS = new String[] {"jpg", "jpeg", "png"};
    final static String TEXT_EXTENSION = "txt";

    private File _openedDirectory;
    private File[] _pics;
    private File _currentFile;

    public Image currentImage;
    public ArrayList<BoxLabel> boxLabels = new ArrayList<>();
    public Tetragon currentTetragon = new Tetragon();

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

                if (Arrays.asList(IMAGE_EXTENSIONS).contains(ext)) {
                    picList.add(f);
                }
            }
        }

        _pics = picList.toArray(new File[0]);

        if (_pics.length == 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("wooops");
            alert.setContentText("the folder has no images");
            alert.showAndWait();

            return;
        }

        setCurrentImage(_pics[0]);
    }

    public void setCurrentImage(File file) {
        boxLabels.clear();
        _currentFile = file;
        currentImage = new Image(file.toURI().toString());
        loadLabels();
    }

    public void chooseImage(int navigate) {
        if (_currentFile == null) return;

        int index = Arrays.asList(_pics).indexOf(_currentFile) + navigate;
        if (index < 0) {
            index = _pics.length - 1;
        } else if (index > _pics.length - 1) {
            index = 0;
        }

        setCurrentImage(_pics[index]);
    }

    public void loadLabels() {
        if (_currentFile == null) return;

        String fileName = FileHelper.getNameWithoutExtension(_currentFile.getAbsolutePath()) + "." + TEXT_EXTENSION;

        File text = new File(fileName);
        BufferedReader fr;
        try {
            fr = new BufferedReader(new FileReader(text));

            double w = currentImage.getWidth();
            double h = currentImage.getHeight();

            String line;

            while ((line = fr.readLine()) != null) {
                String[] split = line.split(" ");
                if (split.length != 5) continue;

                try {
                    BoxLabel l = FileHelper.readLabel(split, w, h);
                    boxLabels.add(l);
                } catch (NumberFormatException ignored) {}
            }

            fr.close();
        } catch (IOException ignored) {}
    }

    public void saveCurrentLabels() {
        if (_currentFile == null) return;

        String fileName = FileHelper.getNameWithoutExtension(_currentFile.getAbsolutePath()) + "." + TEXT_EXTENSION;

        File text = new File(fileName);
        FileWriter fw;
        try {
            fw = new FileWriter(text, false);
            double w = currentImage.getWidth();
            double h = currentImage.getHeight();

            for (BoxLabel l : boxLabels) {
                FileHelper.writeLabel(fw, l, w, h);
            }

            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void processImages() {
        File prevFile = _currentFile;

        String pPath = _openedDirectory + "/process";

        File dir = new File(pPath);
        dir.delete();
        dir = new File(pPath);
        dir.mkdir();

        int i = 0;

        for (File f : _pics) {
            setCurrentImage(f);

            BufferedImage img;
            try {
                img = ImageIO.read(f);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            for (BoxLabel l : boxLabels) {
                try {
                    // write cropped label
                    BufferedImage bi = img.getSubimage((int) l.x, (int) l.y, (int) l.w, (int) l.h);
                    File output = new File(pPath + "/" + i + ".jpg");
                    File outputText = new File(pPath + "/" + i + "." + TEXT_EXTENSION);
                    ImageIO.write(bi, "jpg", output);

                    FileWriter fw = new FileWriter(outputText, false);
                    BoxLabel nl = l.copy();
                    nl.x = 0;
                    nl.y = 0;

                    fw.write(l.classNumber + " 0.5 0.5 1 1");
                    fw.close();

                    i++;

                    // TODO: pre-processing
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        setCurrentImage(prevFile);
    }
}
