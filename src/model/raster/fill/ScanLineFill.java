package model.raster.fill;

import model.objects.Line;
import model.objects.Point;
import model.objects.Polygon;
import model.raster.BresenhamLiner;
import model.raster.Liner;
import model.raster.PolygonRasterizer;
import model.raster.RasterBI;

import java.util.ArrayList;
import java.util.List;

public class ScanLineFill {

    private final Polygon polygon;

    public ScanLineFill(Polygon polygon) {
        this.polygon = polygon.copy();
    }

    public void fill(RasterBI image, int fillColor, int boundaryColor) {
        fill(image, new SolidPatternFill(fillColor), boundaryColor);
    }

    public void fill(RasterBI image, PatternFill patternFill, int boundaryColor) {
        if (polygon.size() < 3 || patternFill == null) {
            return;
        }

        List<Line> edges = new ArrayList<>();
        int yMin = Integer.MAX_VALUE;
        int yMax = Integer.MIN_VALUE;

        for (Line edge : polygon.toEdges()) {
            yMin = Math.min(yMin, Math.min(edge.a().y(), edge.b().y()));
            yMax = Math.max(yMax, Math.max(edge.a().y(), edge.b().y()));
            if (!edge.isHorizontal()) {
                edges.add(edge.orientate());
            }
        }

        int firstY = Math.max(0, yMin);
        int lastY = Math.min(image.getHeight() - 1, yMax);

        for (int y = firstY; y <= lastY; y++) {
            List<Double> intersections = new ArrayList<>();

            for (Line edge : edges) {
                Point a = edge.a();
                Point b = edge.b();

                if (y >= a.y() && y < b.y()) {
                    double x = a.x()
                            + (double) (y - a.y()) * (b.x() - a.x())
                            / (b.y() - a.y());
                    intersections.add(x);
                }
            }

            sortIntersections(intersections);

            for (int i = 0; i + 1 < intersections.size(); i += 2) {
                int startX = Math.max(0, (int) Math.ceil(intersections.get(i)));
                int endX = Math.min(
                        image.getWidth() - 1,
                        (int) Math.floor(intersections.get(i + 1))
                );

                for (int x = startX; x <= endX; x++) {
                    image.setPixel(x, y, patternFill.colorAt(x, y));
                }
            }
        }

        Liner boundaryLiner = new BresenhamLiner(image);
        boundaryLiner.setColor(boundaryColor);
        new PolygonRasterizer(boundaryLiner).rasterize(polygon);
    }

    // Insertion sort keeps the scan-line intersection ordering explicit in this implementation.
    private void sortIntersections(List<Double> intersections) {
        for (int i = 1; i < intersections.size(); i++) {
            double value = intersections.get(i);
            int j = i - 1;
            while (j >= 0 && intersections.get(j) > value) {
                intersections.set(j + 1, intersections.get(j));
                j--;
            }
            intersections.set(j + 1, value);
        }
    }
}
