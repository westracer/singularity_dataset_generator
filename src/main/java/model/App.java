package model;

import helper.FastNoise;
import helper.FileHelper;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class App {
    public App() {}

    private final static String[] IMAGE_EXTENSIONS = new String[] {"jpg", "jpeg", "png"};
    public final static String TEXT_EXTENSION = "txt";
    public final static String PROCESS_FOLDER_NAME = "process";

    private final float NUMBER_OF_RANDOM_IMAGES = 7;

    private final int[] scaleSizes = new int[]{20, 100, 200, 400};
    private final float[] noiseStrengths = new float[] {0f, .25f, .5f, 1f};
    private final float[] brightnesses = new float[]{.4f, .5f, .75f, 1f, 1.5f, 1.75f};
    private final double[] rotationAngles = new double[]{-10, 0, 10};

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

    private void setCurrentImage(File file) {
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

    private void loadLabels() {
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

        String pPath = _openedDirectory + "/" + PROCESS_FOLDER_NAME;

        File dir = new File(pPath);
        dir.delete();
        dir = new File(pPath);
        dir.mkdir();

        int i = 0;

        try {
            File labelsText = new File(_openedDirectory + "/labels" + "." + TEXT_EXTENSION);
            FileWriter labelsFW = new FileWriter(labelsText, false);

            for (File f : _pics) {
                setCurrentImage(f);

                BufferedImage img;

                FastNoise noise = new FastNoise();
                noise.SetFractalOctaves(5);
                try {
                    img = ImageIO.read(f);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }

                for (BoxLabel l : boxLabels) {
                    // cropped label
                    BufferedImage bi = img.getSubimage((int) l.x, (int) l.y, (int) l.w, (int) l.h);

                    /* pre-processing */

                    // scaling to different sizes
                    for (int size : scaleSizes) {
                        BufferedImage scaledImage = Scalr.resize(bi, size);
                        FileHelper.writeCroppedLabel(pPath, i++, scaledImage, l.classNumber, labelsFW);

                        Random random = new Random();
                        for (int n = 0; n < NUMBER_OF_RANDOM_IMAGES; n++) {
                            float noiseStr = noiseStrengths[random.nextInt(noiseStrengths.length)];
                            float brightness = brightnesses[random.nextInt(brightnesses.length)];
                            double angle = rotationAngles[random.nextInt(rotationAngles.length)];

                            BufferedImage editedImage = scaledImage.getSubimage(0, 0, scaledImage.getWidth(), scaledImage.getHeight());

                            // rotation
                            editedImage = FileHelper.rotateImageByDegrees(editedImage, angle);

                            // noise
                            for (int x = 0; x < editedImage.getWidth(); x++) {
                                for (int y = 0; y < editedImage.getHeight(); y++) {
                                    Color pixel = new Color(editedImage.getRGB(x, y));
                                    float noiseVal = noise.GetSimplexFractal(x, y) * noiseStr;

                                    int r = (int) (pixel.getRed() + noiseVal * 255);
                                    int g = (int) (pixel.getGreen() + noiseVal * 255);
                                    int b = (int) (pixel.getBlue() + noiseVal * 255);

                                    Color newPixel = new Color(
                                            Math.max(0, Math.min(r, 255)),
                                            Math.max(0, Math.min(g, 255)),
                                            Math.max(0, Math.min(b, 255))
                                    );

                                    editedImage.setRGB(x, y, newPixel.getRGB());
                                }
                            }

                            // brightness
                            editedImage = Scalr.apply(editedImage, new RescaleOp(brightness, 0, null));

                            FileHelper.writeCroppedLabel(pPath, i++, editedImage, l.classNumber, labelsFW);
                        }
                    }
                }
            }

            labelsFW.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        setCurrentImage(prevFile);
    }
}
