package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
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
import java.util.Arrays;
import java.util.ResourceBundle;

enum MainState {
    Select, Move, Box, Tetragon
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

    private PointDragState _pointDrag = PointDragState.None;
    private Point2D _prevMouse;
    private MainState _state = MainState.Box;
    private BoxLabel _draggableLabel = null;
    private ArrayList<BoxLabel> _selectedLabels = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {}

    private void _drawPoint(GraphicsContext gc, Point2D point) {

        gc.setStroke(null);
        gc.setFill(Color.RED);
        gc.fillOval(point.getX() - POINT_SIZE/2, point.getY() - POINT_SIZE/2, POINT_SIZE, POINT_SIZE);
    }

    private void _drawBoxLabel(GraphicsContext gc, BoxLabel label) {
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
            gc.setStroke(Color.YELLOW);
        }

        gc.setLineWidth(1);

        label = label.fixNegativeSize();

        gc.strokeRect(label.x, label.y, label.w, label.h);

        if (_state != MainState.Select || !isDraggable) {
            for (Point2D p : label.getPoints()) {
                _drawPoint(gc, p);
            }
        }
    }

    private void _drawTetragon(GraphicsContext gc) {
        int tetSize = app.currentTetragon.points.size();

        if (tetSize > 2) {
            double[] xPoints = new double[tetSize];
            double[] yPoints = new double[tetSize];

            for (int i = 0; i < tetSize; i++) {
                Point2D p = app.currentTetragon.points.get(i);
                xPoints[i] = p.getX();
                yPoints[i] = p.getY();
            }

            gc.setFill(Color.rgb(100, 100, 255, .1));
            gc.setStroke(Color.BLUE);
            gc.setLineWidth(1);
            gc.fillPolygon(xPoints, yPoints, tetSize);
            gc.strokePolygon(xPoints, yPoints, tetSize);
        }

        for (Point2D p : app.currentTetragon.points) {
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

        _drawTetragon(gc);
    }

    public void openDirectory() {
        DirectoryChooser dc = new DirectoryChooser();

        File dir = dc.showDialog(this.window);
        if (dir == null) return;

        this.app.openDirectory(dir);
        repaintImageCanvas();
    }

    public void onMouseDown(MouseEvent event) {
        _prevMouse = new Point2D(event.getX(), event.getY());
        MouseButton mb = event.getButton();

        if (mb == MouseButton.PRIMARY) {
            switch (_state) {
                case Select:
                case Box:
                    _draggableLabel = new BoxLabel(0, event.getX(), event.getY(), 0, 0);
                    _selectedLabels.clear();
                    break;
                case Move:
                    for (BoxLabel l : app.boxLabels) {

                        Point2D clickedPoint = null;
                        Point2D[] points = l.getPoints();
                        for (Point2D p : points) {
                            if (Math.abs(p.getX() - CLICK_POINT_SIZE / 2 - event.getX()) < CLICK_POINT_SIZE
                                    && Math.abs(p.getY() - CLICK_POINT_SIZE / 2 - event.getY()) < CLICK_POINT_SIZE) {
                                clickedPoint = p;
                                break;
                            }
                        }

                        if (clickedPoint != null) {
                            _draggableLabel = l;

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
                    }
                    break;
                case Tetragon:
                    _selectedLabels.clear();

                    if (app.currentTetragon.points.size() == 4) {
                        app.currentTetragon.points.clear();
                    }

                    app.currentTetragon.points.add(new Point2D(event.getX(), event.getY()));

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
                case Tetragon:
                    break;
            }
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
                case Tetragon:
                    break;
            }

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
            _state = MainState.Tetragon;
        }
    }

    public void onKeyPressed(KeyEvent ev) {
        switch (ev.getCode()) {
            case ESCAPE:
                _selectedLabels.clear();
                repaintCanvas();
                break;
            case DELETE:
            case BACK_SPACE:
                app.boxLabels.removeAll(_selectedLabels);
                repaintCanvas();
                break;
            default:
        }
    }
}
