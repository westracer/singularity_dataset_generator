package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import model.App;
import model.BoxLabel;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

enum MainState {
    Select, Box, Tetragon
}

public class MainController implements Initializable {
    private App app = new App();
    public Stage window;

    @FXML public MenuItem openDirItem;
    @FXML public VBox vbox;
    @FXML public Canvas canvas;
    @FXML public Canvas imageCanvas;
    @FXML public ScrollPane canvasScroll;
    @FXML public RadioButton selectRadio;
    @FXML public RadioButton boxRadio;
    @FXML public RadioButton tetraRadio;

    private MainState _state = MainState.Box;
    private BoxLabel _draggableLabel = null;
    private ArrayList<BoxLabel> _selectedLabels = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {}

    private void _drawPoint(GraphicsContext gc, Point2D point) {
        final double POINT_SIZE = 8.;

        gc.setStroke(null);
        gc.setFill(Color.RED);
        gc.fillOval(point.getX() - POINT_SIZE/2, point.getY() - POINT_SIZE/2, POINT_SIZE, POINT_SIZE);
    }

    private void _drawBoxLabel(GraphicsContext gc, BoxLabel label) {
        if (label == _draggableLabel) {
            gc.setStroke(Color.GREEN);
        } else if (_selectedLabels.contains(label)) {
            gc.setStroke(Color.CYAN);
        } else {
            gc.setStroke(Color.YELLOW);
        }

        gc.setLineWidth(1);

        label = label.fixNegativeSize();

        gc.strokeRect(label.x, label.y, label.w, label.h);

        for (Point2D p : label.getPoints()) {
            _drawPoint(gc, p);
        }
    }

    private void repaintImageCanvas() {
        GraphicsContext gc = imageCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, imageCanvas.getWidth(), imageCanvas.getHeight());

        if (app.currentImage == null) return;

        canvas.setWidth(app.currentImage.getWidth());
        canvas.setHeight(app.currentImage.getHeight());
        imageCanvas.setWidth(app.currentImage.getWidth());
        imageCanvas.setHeight(app.currentImage.getHeight());
        gc.drawImage(app.currentImage, 0, 0);
    }

    private void repaintCanvas() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        for (BoxLabel label : app.boxLabels) {
            _drawBoxLabel(gc, label);
        }

        if (_draggableLabel != null) {
            _drawBoxLabel(gc, _draggableLabel);
        }
    }

    public void openDirectory() {
        DirectoryChooser dc = new DirectoryChooser();

        File dir = dc.showDialog(this.window);
        if (dir == null) return;

        this.app.openDirectory(dir);
        repaintImageCanvas();
    }

    public void onMouseDown(MouseEvent event) {
        _selectedLabels.clear();

        switch (_state) {
            case Select:
                break;
            case Box:
                _draggableLabel = new BoxLabel(0, event.getX(), event.getY(), 0, 0);
                break;
            case Tetragon:
                break;
        }

        repaintCanvas();
    }

    public void onDrag(MouseEvent event) {
        switch (_state) {
            case Select:
                break;
            case Box:
                if (_draggableLabel == null) return;

                _draggableLabel.w = event.getX() - _draggableLabel.x;
                _draggableLabel.h = event.getY() - _draggableLabel.y;
                break;
            case Tetragon:
                break;
        }

        repaintCanvas();
    }

    public void onMouseUp() {
        switch (_state) {
            case Select:
                break;
            case Box:
                if (_draggableLabel != null) {
                    _draggableLabel = _draggableLabel.fixNegativeSize();

                    double xEnd = _draggableLabel.x + _draggableLabel.w;
                    double yEnd = _draggableLabel.y + _draggableLabel.h;
                    double cw = canvas.getWidth();
                    double ch = canvas.getHeight();

                    if (_draggableLabel.x > 0
                            && _draggableLabel.x < cw
                            && _draggableLabel.y > 0
                            && _draggableLabel.y < ch
                            && xEnd > 0
                            && xEnd < cw
                            && yEnd > 0
                            && yEnd < ch
                    ) {
                        app.boxLabels.add(_draggableLabel);
                        _selectedLabels.add(_draggableLabel);
                    }

                    _draggableLabel = null;
                }
                break;
            case Tetragon:
                break;
        }

        repaintCanvas();
    }

    public void onStateChange(ActionEvent ev) {
        Object source = ev.getSource();

        if (source == selectRadio) {
            _state = MainState.Select;
        } else if (source == boxRadio) {
            _state = MainState.Box;
        } else if (source == tetraRadio) {
            _state = MainState.Tetragon;
        }
    }
}
