package model;

import javafx.geometry.Point2D;

public class BoxLabel {
    public int classNumber;
    public double x;
    public double y;
    public double w;
    public double h;

    public BoxLabel(int classNumber, double x, double y, double w, double h) {
        this.classNumber = classNumber;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public BoxLabel fixNegativeSize() {
        double w = Math.abs(this.w);
        double h = Math.abs(this.h);
        double startX = this.w > 0 ? this.x : this.x - w;
        double startY = this.h > 0 ? this.y : this.y - h;

        return new BoxLabel(classNumber, startX, startY, w, h);
    }

    public BoxLabel copy() {
        return new BoxLabel(classNumber, x, y, w, h);
    }

    public Point2D[] getPoints() {
        return new Point2D[] {
                new Point2D(this.x, this.y),
                new Point2D(this.x, this.y + this.h),
                new Point2D(this.x + this.w, this.y),
                new Point2D(this.x + this.w, this.y + this.h)
        };
    }

    public void copyBoundsFrom(BoxLabel l) {
        this.x = l.x;
        this.y = l.y;
        this.w = l.w;
        this.h = l.h;
    }

    public BoxLabel[] generateGrid(int columns, int rows, double spacing) {
        BoxLabel[] labels = new BoxLabel[columns * rows];

        double cellsW = this.w - spacing * (columns - 1);
        double cellsH = this.h - spacing * (rows - 1);
        BoxLabel cell = new BoxLabel(this.classNumber, this.x, this.y, cellsW / columns, cellsH / rows);
        for (int col = 0; col < columns; col++) {
            for (int row = 0; row < rows; row++) {
                BoxLabel l = cell.copy();
                l.x += l.w * col + spacing * col;
                l.y += l.h * row + spacing * row;

                labels[row * columns + col] = l;
            }
        }

        return labels;
    }
}
