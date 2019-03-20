package model;

import javafx.geometry.Point2D;
import javafx.scene.shape.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;

public class Tetragon {
    public ArrayList<Point2D> points = new ArrayList<>();

    /**
     * @return top, right, bottom, left
     */
    public Line[] getEdges() {
        Line[] lines = new Line[4];

        ArrayList<Point2D> sortedPointsByY = new ArrayList<>(points);
        sortedPointsByY.sort(Comparator.comparingDouble(Point2D::getY));
        ArrayList<Point2D> sortedPointsByX = new ArrayList<>(points);
        sortedPointsByX.sort(Comparator.comparingDouble(Point2D::getX));

        int pointsSize = points.size();
        for (int i = 0; i < pointsSize; i++) {
            Point2D p1 = points.get(i);
            Point2D p2 = points.get(i + 1 == pointsSize ? 0 : i + 1);

            double deltaX = Math.abs(p1.getX() - p2.getX());
            double deltaY = Math.abs(p1.getY() - p2.getY());

            if (deltaX > deltaY) {
                // horizontal

                // from left to right
                Point2D start = p1, end = p2;
                if (p1.getX() > p2.getX()) {
                    start = p2;
                    end = p1;
                }

                Line line = new Line(start.getX(), start.getY(), end.getX(), end.getY());

                Point2D topPoint = sortedPointsByY.get(0);
                if (p1 == topPoint || p2 == topPoint) {
                    lines[0] = line;
                } else {
                    lines[2] = line;
                }
            } else {
                // vertical

                // from top to bottom
                Point2D start = p1, end = p2;
                if (p1.getY() > p2.getY()) {
                    start = p2;
                    end = p1;
                }

                Line line = new Line(start.getX(), start.getY(), end.getX(), end.getY());

                Point2D leftPoint = sortedPointsByX.get(0);
                if (p1 == leftPoint || p2 == leftPoint) {
                    lines[3] = line;
                } else {
                    lines[1] = line;
                }
            }
        }

        return lines;
    }

    public BoxLabel getBox() {
        Path p = new Path(new MoveTo(points.get(0).getX(), points.get(0).getY()));
        int size = points.size();

        for (int i = 1; i < size; i++) {
            p.getElements().add(new LineTo(points.get(i).getX(), points.get(i).getY()));
        }

        p.getElements().add(new ClosePath());

        ArrayList<Point> intersections = new ArrayList<>();
        for (Point2D point : points) {
            Path s = (Path) Path.intersect(p, new Line(point.getX() - 10000, point.getY(), point.getX() + 10000, point.getY()));

            ArrayList<Point> lineIntersections = new ArrayList<>();

            for (PathElement el : s.getElements()) {
                if (el.getClass() == MoveTo.class) {
                    MoveTo move = (MoveTo) el;
                    lineIntersections.add(new Point((int) move.getX(), (int) move.getY()));
                }
            }

            if (lineIntersections.size() == 2 && intersections.size() < 4) {
                intersections.addAll(lineIntersections);
            }
        }

        ArrayList<Point> intersections2 = new ArrayList<>();
        for (Point2D point : points) {
            Path s = (Path) Path.intersect(p, new Line(point.getX(), point.getY() - 10000, point.getX(), point.getY() + 10000));

            ArrayList<Point> lineIntersections = new ArrayList<>();

            for (PathElement el : s.getElements()) {
                if (el.getClass() == MoveTo.class) {
                    MoveTo move = (MoveTo) el;
                    lineIntersections.add(new Point((int) move.getX(), (int) move.getY()));
                }
            }

            if (lineIntersections.size() == 2 && intersections2.size() < 4) {
                intersections2.addAll(lineIntersections);
            }
        }

        if (intersections.size() == 4 && intersections2.size() == 4) {
            Path p1 = new Path(new MoveTo(intersections.get(0).getX(), intersections.get(0).getY()));
            Path p2 = new Path(new MoveTo(intersections2.get(0).getX(), intersections2.get(0).getY()));

            for (int i = 1; i < 4; i++) {
                p1.getElements().add(new LineTo(intersections.get(i).getX(), intersections.get(i).getY()));
                p2.getElements().add(new LineTo(intersections2.get(i).getX(), intersections2.get(i).getY()));
            }

            p1.getElements().add(new ClosePath());
            p2.getElements().add(new ClosePath());

            Path innerIntersect = (Path) Path.intersect(p1, p2);

            ArrayList<Point2D> lineIntersections = new ArrayList<>();

            for (PathElement el : innerIntersect.getElements()) {
                if (el.getClass() == MoveTo.class) {
                    MoveTo move = (MoveTo) el;
                    lineIntersections.add(new Point2D(move.getX(), move.getY()));
                }
            }

            System.out.println(lineIntersections.size());
        }

        return null;
    }

    public double[] xPoints() {
        int size = points.size();
        double[] xPoints = new double[size];

        for (int i = 0; i < size; i++) {
            Point2D p = points.get(i);
            xPoints[i] = p.getX();
        }

        return xPoints;
    }

    public double[] yPoints() {
        int size = points.size();
        double[] yPoints = new double[size];

        for (int i = 0; i < size; i++) {
            Point2D p = points.get(i);
            yPoints[i] = p.getY();
        }

        return yPoints;
    }
}
