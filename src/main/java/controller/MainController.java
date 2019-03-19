package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import model.App;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    private App app = new App();
    public Stage window;

    @FXML public MenuItem openDirItem;
    @FXML public VBox vbox;
    @FXML public Canvas canvas;
    @FXML public ScrollPane canvasScroll;

    @Override
    public void initialize(URL location, ResourceBundle resources) {}

    public void repaintCanvas() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        if (app.currentImage != null) {
            canvas.setWidth(app.currentImage.getWidth());
            canvas.setHeight(app.currentImage.getHeight());
            gc.drawImage(app.currentImage, 0, 0);
        }
    }

    public void openDirectory() {
        DirectoryChooser dc = new DirectoryChooser();
        this.app.openDirectory(dc.showDialog(this.window));

        repaintCanvas();
    }
}
