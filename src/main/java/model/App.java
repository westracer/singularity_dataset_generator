package model;

import helper.FileHelper;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;

import java.io.*;
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

                int classNumber;
                double xRelCenter, yRelCenter, wRel, hRel;

                try {
                    classNumber = Integer.parseInt(split[0]);
                    xRelCenter = Double.parseDouble(split[1]);
                    yRelCenter = Double.parseDouble(split[2]);
                    wRel = Double.parseDouble(split[3]);
                    hRel = Double.parseDouble(split[4]);

                    BoxLabel l = new BoxLabel(
                            classNumber,
                            xRelCenter * w -  wRel * w / 2,
                            yRelCenter * h -  hRel * h / 2,
                            wRel * w,
                            hRel * h
                    );

                    boxLabels.add(l);
                } catch (NumberFormatException ignored) {}
            }

            fr.close();
        } catch (IOException ignored) {}
    }

    public void saveCurrentLabels() {
        if (_currentFile == null) return;

        final double DOUBLE_ROUND = 10000.;

        String fileName = FileHelper.getNameWithoutExtension(_currentFile.getAbsolutePath()) + "." + TEXT_EXTENSION;

        File text = new File(fileName);
        FileWriter fw;
        try {
            fw = new FileWriter(text, false);
            double w = currentImage.getWidth();
            double h = currentImage.getHeight();

            for (BoxLabel l : boxLabels) {
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

            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
