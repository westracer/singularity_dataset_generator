package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import model.App;
import model.BoxLabel;

import java.io.File;
import java.net.URL;
import java.util.*;

enum MainState {
    Select, Move, Box, Grid
}

enum PointDragState {
    None, TopLeft, TopRight, BottomLeft, BottomRight
}

public class MainController implements Initializable {
    private final double POINT_SIZE = 8.;
    private final double CLICK_POINT_SIZE = POINT_SIZE + 5;

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
    @FXML public RadioButton moveRadio;
    @FXML public TextField columnField;
    @FXML public TextField rowField;
    @FXML public TextField spacingField;
    @FXML public TextField classField;
    @FXML public Button saveButton;
    @FXML public Button classButton;
    @FXML public Button gridBoxesButton;

    private boolean _saved = true;
    private PointDragState _pointDrag = PointDragState.None;
    private Point2D _prevMouse;
    private MainState _state = MainState.Box;
    private BoxLabel _draggableLabel = null;
    private ArrayList<BoxLabel> _selectedLabels = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        openDirectoryByPath("/Users/test/Documents/pics"); // "C:\\Projects\\making_model\\pics"
    }

    private void _drawPoint(GraphicsContext gc, Point2D point) {

        gc.setStroke(null);
        gc.setFill(Color.RED);
        gc.fillOval(point.getX() - POINT_SIZE/2, point.getY() - POINT_SIZE/2, POINT_SIZE, POINT_SIZE);
    }

    private void _setLabelColor(GraphicsContext gc, BoxLabel label) {
        boolean isDraggable = label == _draggableLabel;

        if (isDraggable) {
            if (_state == MainState.Select) {
                gc.setStroke(Color.GRAY);
            } else {
                gc.setStroke(Color.GREEN);
            }
        } else if (_selectedLabels.contains(label)) {
            gc.setStroke(Color.CYAN);
        } else {
            gc.setStroke(Color.VIOLET);
        }
    }

    private void _drawBoxLabel(GraphicsContext gc, BoxLabel label) {
        boolean isDraggable = label == _draggableLabel;

        gc.setLineWidth(1);

        label = label.fixNegativeSize();

        gc.strokeRect(label.x, label.y, label.w, label.h);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(11));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText(Integer.toString(label.classNumber), label.x + 10, label.y + 20);
        gc.strokeText(Integer.toString(label.classNumber), label.x + 10, label.y + 20);

        if (_state != MainState.Select || !isDraggable) {
            for (Point2D p : label.getPoints()) {
                _drawPoint(gc, p);
            }
        }
    }

    private void _drawGrid(GraphicsContext gc) {
        if (_state != MainState.Grid || app.currentGrid == null) return;

        gc.setStroke(Color.DARKCYAN);
        gc.setFill(Color.DARKCYAN);
        gc.setLineWidth(1);

        BoxLabel label = app.currentGrid.fixNegativeSize();

        gc.strokeRect(label.x, label.y, label.w, label.h);

        gc.setFont(Font.font(11));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText(Integer.toString(label.classNumber), label.x + 10, label.y + 20);
        gc.strokeText(Integer.toString(label.classNumber), label.x + 10, label.y + 20);

        for (Point2D p : label.getPoints()) {
            _drawPoint(gc, p);
        }

        gc.setStroke(Color.DARKCYAN);
        for (BoxLabel l : _getGridLabels()) {
            _drawBoxLabel(gc, l);
        }
    }

    private BoxLabel[] _getGridLabels() {
        int rows, columns;
        double spacing;
        BoxLabel grid = app.currentGrid;

        try {
            rows = Integer.parseInt(rowField.getText());
            columns  = Integer.parseInt(columnField.getText());
            spacing = Double.parseDouble(spacingField.getText());
        } catch (NumberFormatException ex) {
            return new BoxLabel[] {};
        }

        if (grid == null || rows < 1 || columns < 1 || spacing < 0) {
            return new BoxLabel[] {};
        }

        return grid.generateGrid(columns, rows, spacing);
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

        gc.setFill(Color.VIOLET);
        gc.setFont(Font.font(11));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText(app.getCurrentFile().getName(), 10, 20);
        gc.strokeText(app.getCurrentFile().getName(), 10, 20);

        for (BoxLabel label : app.boxLabels) {
            _setLabelColor(gc, label);
            _drawBoxLabel(gc, label);
        }

        if (_draggableLabel != null) {
            _setLabelColor(gc, _draggableLabel);
            _drawBoxLabel(gc, _draggableLabel);
        }

        _drawGrid(gc);
    }

    public void openDirectory() {
        DirectoryChooser dc = new DirectoryChooser();

        File dir = dc.showDialog(this.window);
        _openDirectory(dir);
    }

    private void openDirectoryByPath(String dirPath) {
        _openDirectory(new File(dirPath));
    }

    private void _openDirectory(File dir) {
        if (dir == null) return;

        if (!_saved) if (!askSave()) return;

        _saved = true;
        this.app.openDirectory(dir);
        repaintImageCanvas();
        repaintCanvas();
    }

    public void onMouseDown(MouseEvent event) {
        _prevMouse = new Point2D(event.getX(), event.getY());
        MouseButton mb = event.getButton();

        if (mb == MouseButton.PRIMARY) {
            int classNumber = 0;

            try {
                classNumber = Integer.parseInt(classField.getText());
            } catch (NumberFormatException ignored) {}

            switch (_state) {
                case Select:
                case Box:
                    _draggableLabel = new BoxLabel(classNumber, event.getX(), event.getY(), 0, 0);
                    _selectedLabels.clear();
                    break;
                case Move:
                    Point2D clickedPoint = null;
                    Point2D[] points = null;

                    if (app.currentGrid != null) {
                        points = app.currentGrid.getPoints();

                        for (Point2D p : points) {
                            if (Math.abs(p.getX() - CLICK_POINT_SIZE / 2 - event.getX()) < CLICK_POINT_SIZE
                                    && Math.abs(p.getY() - CLICK_POINT_SIZE / 2 - event.getY()) < CLICK_POINT_SIZE) {
                                clickedPoint = p;
                                _draggableLabel = app.currentGrid;
                                break;
                            }
                        }
                    } else {
                        for (BoxLabel l : app.boxLabels) {
                            points = l.getPoints();

                            for (Point2D p : points) {
                                if (Math.abs(p.getX() - CLICK_POINT_SIZE / 2 - event.getX()) < CLICK_POINT_SIZE
                                        && Math.abs(p.getY() - CLICK_POINT_SIZE / 2 - event.getY()) < CLICK_POINT_SIZE) {
                                    clickedPoint = p;
                                    break;
                                }
                            }

                            if (clickedPoint != null) {
                                _draggableLabel = l;
                                break;
                            }
                        }
                    }

                    if (clickedPoint != null) {
                        switch (Arrays.asList(points).indexOf(clickedPoint)) {
                            case 0:
                                _pointDrag = PointDragState.TopLeft;
                                break;
                            case 1:
                                _pointDrag = PointDragState.BottomLeft;
                                break;
                            case 2:
                                _pointDrag = PointDragState.TopRight;
                                break;
                            case 3:
                                _pointDrag = PointDragState.BottomRight;
                                break;
                            default:
                        }
                    }

                    break;
                case Grid:
                    app.currentGrid = new BoxLabel(classNumber, event.getX(), event.getY(), 0, 0);
                    _selectedLabels.clear();
                    break;
            }
        }

        repaintCanvas();
    }

    public void onDrag(MouseEvent event) {
        MouseButton mb = event.getButton();
        double deltaX = event.getX() - _prevMouse.getX();
        double deltaY = event.getY() - _prevMouse.getY();

        if (mb == MouseButton.PRIMARY) {
            switch (_state) {
                case Select:
                case Box:
                    if (_draggableLabel == null) return;

                    _draggableLabel.w = event.getX() - _draggableLabel.x;
                    _draggableLabel.h = event.getY() - _draggableLabel.y;
                    break;
                case Move:

                    if (_selectedLabels.size() > 0 && _pointDrag == PointDragState.None) {
                        for (BoxLabel l : _selectedLabels) {
                            l.x += deltaX;
                            l.y += deltaY;
                        }
                    } else if (_draggableLabel != null && _pointDrag != PointDragState.None) {
                        switch (_pointDrag) {
                            case TopLeft:
                                _draggableLabel.x += deltaX;
                                _draggableLabel.y += deltaY;

                                _draggableLabel.w -= deltaX;
                                _draggableLabel.h -= deltaY;
                                break;
                            case BottomLeft:
                                _draggableLabel.x += deltaX;
                                _draggableLabel.h += deltaY;

                                _draggableLabel.w -= deltaX;
                                break;
                            case TopRight:
                                _draggableLabel.y += deltaY;
                                _draggableLabel.w += deltaX;

                                _draggableLabel.h -= deltaY;
                                break;
                            case BottomRight:
                                _draggableLabel.w += deltaX;
                                _draggableLabel.h += deltaY;
                                break;
                            default:
                        }
                    }
                    break;
                case Grid:
                    if (app.currentGrid == null) return;

                    app.currentGrid.w = event.getX() - app.currentGrid.x;
                    app.currentGrid.h = event.getY() - app.currentGrid.y;
                    break;
            }

            _saved = false;
        }

        _prevMouse = new Point2D(event.getX(), event.getY());
        repaintCanvas();
    }

    public void onMouseUp(MouseEvent event) {
        MouseButton mb = event.getButton();

        if (mb == MouseButton.PRIMARY) {
            switch (_state) {
                case Select:
                    if (_draggableLabel != null) {
                        _selectedLabels.clear();

                        BoxLabel d = _draggableLabel.fixNegativeSize();
                        Rectangle2D rect = new Rectangle2D(d.x, d.y, d.w, d.h);
                        for (BoxLabel l : app.boxLabels) {
                            boolean contains = false;

                            for (Point2D p : l.getPoints()) {
                                if (rect.contains(p)) {
                                    contains = true;
                                    break;
                                }
                            }

                            if (contains) {
                                _selectedLabels.add(l);
                            }
                        }
                    }

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

                    }
                    break;
                case Move:
                    if (_pointDrag != null && _pointDrag != PointDragState.None) {
                        BoxLabel fixed = _draggableLabel.fixNegativeSize();
                        _draggableLabel.copyBoundsFrom(fixed);
                    }

                    _pointDrag = PointDragState.None;
                    break;
                case Grid:
                    break;
            }
            _saved = false;
            _draggableLabel = null;
        }

        _prevMouse = null;
        repaintCanvas();
    }

    public void onStateChange(ActionEvent ev) {
        Object source = ev.getSource();

        if (source == selectRadio) {
            _state = MainState.Select;
        } else if (source == boxRadio) {
            _state = MainState.Box;
        } else if (source == moveRadio) {
            _state = MainState.Move;
        } else if (source == tetraRadio) {
            _state = MainState.Grid;
        }
    }

    public void onKeyPressed(KeyEvent ev) {
        switch (ev.getCode()) {
            case ESCAPE:
                _selectedLabels.clear();
                app.currentGrid = null;
                repaintCanvas();
                break;
            case DELETE:
            case BACK_SPACE:
                _saved = false;
                app.boxLabels.removeAll(_selectedLabels);
                repaintCanvas();
                break;
            default:
        }
    }

    public void onSaveButtonPress() {
        app.saveCurrentLabels();
        _saved = true;
    }

    public void applyClass() {
        _saved = false;
        try {
            int classNumber = Integer.parseInt(classField.getText());

            for (BoxLabel l : _selectedLabels) {
                l.classNumber = classNumber;
            }

            repaintCanvas();
        } catch (NumberFormatException ignored) {}
    }

    public void nextImage() {
        if (!_saved) if (!askSave()) return;

        _saved = true;
        app.chooseImage(1);
        repaintImageCanvas();
        repaintCanvas();
    }

    private boolean askSave() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Save?", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
        alert.showAndWait();

        if (alert.getResult() == ButtonType.YES) {
            app.saveCurrentLabels();
            return true;
        } else if (alert.getResult() == ButtonType.NO) {
            return true;
        } else {
            return false;
        }
    }

    public void prevImage() {
        if (!_saved) if (!askSave()) return;

        _saved = true;
        app.chooseImage(-1);
        repaintImageCanvas();
        repaintCanvas();
    }

    public void duplicate() {
        _saved = false;
        ArrayList<BoxLabel> newLabels = new ArrayList<>();

        for (BoxLabel l : _selectedLabels) {
            BoxLabel nl = l.copy();
            nl.x += 10;
            nl.y += 10;

            app.boxLabels.add(nl);
            newLabels.add(nl);
        }

        _selectedLabels = newLabels;
        repaintCanvas();
    }

    public void process() {
        app.processImages();
    }

    public void onGridFieldChange() {
        repaintCanvas();
    }

    public void createBoxes() {
        app.boxLabels.addAll(Arrays.asList(_getGridLabels()));
        app.currentGrid = null;
        repaintCanvas();
    }
}
