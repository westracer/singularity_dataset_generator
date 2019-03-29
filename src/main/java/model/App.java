package model;

import helper.FastNoise;
import helper.FileHelper;
import javafx.geometry.Point2D;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class App {
    public App() {}

    private final static String[] IMAGE_EXTENSIONS = new String[] {"jpg", "jpeg", "png"};
    public final static String TEXT_EXTENSION = "txt";
    public final static String PROCESS_FOLDER_NAME = "process";
    public final static double PERSPECTIVE_TRANSFORM_OFFSET = .1;

    private final float NUMBER_OF_RANDOM_IMAGES = 10;

    private final double[] cropSizes = new double[]{832, 1600, 2340};
    private final float[] noiseStrengths = new float[] {.5f};
    private final float[] brightnesses = new float[]{.5f, 1.5f};
    private final double[] rotationAngles = new double[]{-10, 0, 10};

    private File _openedDirectory;
    private File[] _pics;

    public File getCurrentFile() {
        return _currentFile;
    }

    private File _currentFile;

    public Image currentImage;
    public ArrayList<BoxLabel> boxLabels = new ArrayList<>();
    public BoxLabel currentGrid;

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
                noise.SetFrequency(.002f);

                try {
                    img = ImageIO.read(f);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }

                // separate class labels by folders
                /*
                for (BoxLabel l : boxLabels) {
                    dir = new File(pPath + "/" + l.classNumber);
                    dir.mkdir();

                    FileHelper.writeCroppedLabel(pPath + "/" + l.classNumber, i++, img, l);
                }
                */

//                FileHelper.writeCroppedImageLabel(pPath, i++, img, boxLabels, labelsFW);

//                for (float brightness : brightnesses) {
//                    BufferedImage editedImage = Scalr.apply(img, new RescaleOp(brightness, 0, null));
//                    FileHelper.writeCroppedImageLabel(pPath, i++, editedImage, boxLabels, labelsFW);
//                }

//                for (BoxLabel l : boxLabels) {
                    // cropped label
//                    BufferedImage bi = img.getSubimage((int) l.x, (int) l.y, (int) l.w, (int) l.h);

                    /* pre-processing */

                    // scaling to different sizes
//                    for (int size : scaleSizes) {
//                        BufferedImage scaledImage = Scalr.resize(bi, size);
//                        FileHelper.writeCroppedImageLabel(pPath, i++, scaledImage, l.classNumber, labelsFW);



                BufferedImage orig = img;
                ArrayList<BoxLabel> oldLabels = boxLabels;
                int size = oldLabels.size();
                for (int j = 0; j < NUMBER_OF_RANDOM_IMAGES; j++) {
                    int w = img.getWidth(), h = img.getHeight();
                    int min = Math.min(w, h);

                    if (size > 0 && j != 0) {
                        Point2D[] src = new Point2D[] {
                                new Point2D(0, 0),
                                new Point2D(0, h),
                                new Point2D(w, 0),
                        };

                        Point2D[] dst = new Point2D[] {
                                new Point2D(FileHelper.getRandomDouble(min), FileHelper.getRandomDouble(min)),
                                new Point2D(FileHelper.getRandomDouble(min), h + FileHelper.getRandomDouble(min)),
                                new Point2D(w + FileHelper.getRandomDouble(min), FileHelper.getRandomDouble(min)),
                        };

                        AffineTransform at = FileHelper.createTransform(src, dst);
                        img = FileHelper.applyAffineTransform(orig, at);

                        boxLabels = new ArrayList<>();

                        double[] oldCoords = new double[size*8];
                        double[] newCoords = new double[size*8];

                        for (int l = 0; l < size; l++) {
                            BoxLabel label = oldLabels.get(l);
                            oldCoords[l*8] = label.x;
                            oldCoords[l*8 + 1] = label.y;
                            oldCoords[l*8 + 2] = label.x + label.w;
                            oldCoords[l*8 + 3] = label.y;
                            oldCoords[l*8 + 4] = label.x;
                            oldCoords[l*8 + 5] = label.y + label.h;
                            oldCoords[l*8 + 6] = label.x + label.w;
                            oldCoords[l*8 + 7] = label.y + label.h;
                        }

                        at.transform(oldCoords, 0, newCoords, 0, size*4);

                        for (int l = 0; l < size; l++) {
                            BoxLabel label = oldLabels.get(l);
                            BoxLabel newLabel = label.copy();

                            Polygon p = new Polygon();

                            for (int offset = 0; offset < 8; offset += 2) {
                                p.addPoint((int) newCoords[l*8 + offset], (int) newCoords[l*8 + offset + 1]);
                            }

                            Rectangle rect = p.getBounds();
                            newLabel.x = rect.x;
                            newLabel.y = rect.y;
                            newLabel.w = rect.width;
                            newLabel.h = rect.height;

                            boxLabels.add(newLabel);
                        }
                    } else {
                        boxLabels = oldLabels;
                        img = orig;
                    }

                    for (double cropSize : cropSizes) {
                        // with aspectratio
//                    double aspectRatio = (double) w / (double) h;
//                    int wCrop = w < h ? (int) cropSize : (int) (cropSize * aspectRatio);
//                    int hCrop = w > h ? (int) cropSize : (int) (cropSize * aspectRatio);

                        // square
                        int wCrop = (int) cropSize;
                        int hCrop = (int) cropSize;

                        for (int x = 0; x < w; x += wCrop) {
                            for (int y = 0; y < h; y += hCrop) {
                                int newX = x, newY = y;

                                if (x + wCrop > w) newX = w - wCrop;
                                if (y + hCrop > h) newY = h - hCrop;

                                Rectangle imgRect = new Rectangle(0, 0, img.getWidth(), img.getHeight());
                                Rectangle imgNewRect = new Rectangle(newX, newY, wCrop, hCrop);
                                if (!imgRect.contains(imgNewRect)) continue;

                                BufferedImage croppedImage = img.getSubimage(newX, newY, wCrop, hCrop);
                                Rectangle croppedRect = new Rectangle(newX, newY, wCrop, hCrop);

                                ArrayList<BoxLabel> croppedLabels = new ArrayList<>();
                                for (BoxLabel l : boxLabels) {
                                    boolean isInside = true;

                                    for (Point2D p : l.getPoints()) {
                                        Point pointInt = new Point((int) p.getX(), (int) p.getY());
                                        if (!croppedRect.contains(pointInt)) {
                                            isInside = false;
                                            break;
                                        }
                                    }

                                    if (isInside) {
                                        BoxLabel newL = l.copy();
                                        newL.x -= newX;
                                        newL.y -= newY;

                                        croppedLabels.add(newL);
                                    }
                                }

                                if (croppedLabels.size() > 0) {
                                    FileHelper.writeCroppedImageLabel(pPath, i++, croppedImage, croppedLabels, labelsFW);

//                                for (float brightness : brightnesses) {
//                                    BufferedImage editedImage = Scalr.apply(croppedImage, new RescaleOp(brightness, 0, null));
//                                    FileHelper.writeCroppedImageLabel(pPath, i++, editedImage, croppedLabels, labelsFW);
//                                }
                                }
                            }
                        }
                    }
                }

//                        Random random = new Random();
//                        for (int n = 0; n < NUMBER_OF_RANDOM_IMAGES; n++) {
//                            float noiseStr = noiseStrengths[random.nextInt(noiseStrengths.length)];
//                            float brightness = brightnesses[random.nextInt(brightnesses.length)];
//                            double angle = rotationAngles[random.nextInt(rotationAngles.length)];
//
//                            BufferedImage editedImage = img.getSubimage(0, 0, img.getWidth(), img.getHeight());
//
//                            // rotation
//                            editedImage = FileHelper.rotateImageByDegrees(editedImage, angle);
//
//                            // noise
//                            for (int x = 0; x < editedImage.getWidth(); x++) {
//                                for (int y = 0; y < editedImage.getHeight(); y++) {
//                                    Color pixel = new Color(editedImage.getRGB(x, y));
//                                    float noiseVal = noise.GetSimplexFractal(x, y) * noiseStr;
//
//                                    int r = (int) (pixel.getRed() + noiseVal * 255);
//                                    int g = (int) (pixel.getGreen() + noiseVal * 255);
//                                    int b = (int) (pixel.getBlue() + noiseVal * 255);
//
//                                    Color newPixel = new Color(
//                                            Math.max(0, Math.min(r, 255)),
//                                            Math.max(0, Math.min(g, 255)),
//                                            Math.max(0, Math.min(b, 255))
//                                    );
//
//                                    editedImage.setRGB(x, y, newPixel.getRGB());
//                                }
//                            }
//
//                            // brightness
//                            editedImage = Scalr.apply(editedImage, new RescaleOp(brightness, 0, null));
//
//                            FileHelper.writeCroppedImageLabel(pPath, i++, editedImage, boxLabels, labelsFW);
//                        }
//                    }
//                }
            }

            labelsFW.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        setCurrentImage(prevFile);
    }
}
