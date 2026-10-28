package model.raster;

import model.objects.Point;
import model.objects.Polygon;

public class PolygonRasterizer {

    private final Liner liner;

    public PolygonRasterizer(Liner liner) {
        this.liner = liner;
    }

    public void rasterize(Polygon polygon) {
        if (polygon == null || polygon.isEmpty()) {
            return;
        }

        if (polygon.size() == 1) {
            Point point = polygon.get(0);
            liner.drawLine(point, point);
            return;
        }

        if (polygon.size() == 2) {
            liner.drawLine(polygon.get(0), polygon.get(1));
            return;
        }

        for (int i = 0; i < polygon.size(); i++) {
            Point a = polygon.get(i);
            Point b = polygon.get((i + 1) % polygon.size());
            liner.drawLine(a, b);
        }
    }
}
