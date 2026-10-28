package model.raster;

import model.objects.Line;
import model.objects.Point;

public abstract class Liner {

    protected final Raster raster;
    protected int color;

    public Liner(Raster raster) {
        this.raster = raster;
        color = 0xFFFFFF;
    }

    protected abstract void rasterize(int x1, int y1, int x2, int y2);

    public void drawLine(int x1, int y1, int x2, int y2) {
        rasterize(x1, y1, x2, y2);
    }

    public void drawLine(Point a, Point b) {
        rasterize(a.x(), a.y(), b.x(), b.y());
    }

    public void drawLine(Line line) {
        drawLine(line.a(), line.b());
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color & 0xFFFFFF;
    }
}
