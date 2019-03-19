package main;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import main.model.App;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    private App app = new App();
    Stage window;

    @FXML public MenuItem openDirItem;
    @FXML public VBox vbox;
    @FXML public Pane canvasWrap;
    @FXML public Canvas canvas;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        canvas.widthProperty().bind(canvasWrap.widthProperty());
        canvas.heightProperty().bind(canvasWrap.heightProperty());

        canvas.widthProperty().addListener(event -> repaintCanvas());
        canvas.heightProperty().addListener(event -> repaintCanvas());

        repaintCanvas();
    }

    public void repaintCanvas() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    public void openDirectory() {
        DirectoryChooser dc = new DirectoryChooser();
        this.app.openDirectory(dc.showDialog(this.window));
    }
}
