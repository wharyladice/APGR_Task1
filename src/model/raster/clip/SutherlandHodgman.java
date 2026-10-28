package model.raster.clip;

import model.objects.Point;
import model.objects.Polygon;

import java.util.ArrayList;
import java.util.List;

public class SutherlandHodgman {

    private static final double EPSILON = 1e-9;

    public Polygon clip(Polygon originalPolygon, Polygon clippingPolygon) {
        if (originalPolygon == null || originalPolygon.size() < 3) {
            throw new IllegalArgumentException("The polygon to clip must have at least 3 vertices.");
        }
        if (clippingPolygon == null || clippingPolygon.size() < 3) {
            throw new IllegalArgumentException("The clipping polygon must have at least 3 vertices.");
        }
        if (!clippingPolygon.isConvex()) {
            throw new IllegalArgumentException("The clipping polygon must be convex.");
        }

        double orientation = clippingPolygon.signedArea();
        if (Math.abs(orientation) < EPSILON) {
            throw new IllegalArgumentException("The clipping polygon has zero area.");
        }

        List<DPoint> output = new ArrayList<>();
        for (Point point : originalPolygon.getPoints()) {
            output.add(new DPoint(point.x(), point.y()));
        }


        for (int i = 0; i < clippingPolygon.size(); i++) {
            Point clipA = clippingPolygon.get(i);
            Point clipB = clippingPolygon.get((i + 1) % clippingPolygon.size());

            List<DPoint> input = output;
            output = new ArrayList<>();

            if (input.isEmpty()) {
                break;
            }

            DPoint previous = input.get(input.size() - 1);
            boolean previousInside = isInside(previous, clipA, clipB, orientation);

            for (DPoint current : input) {
                boolean currentInside = isInside(current, clipA, clipB, orientation);


                if (currentInside) {
                    if (!previousInside) {
                        output.add(intersection(previous, current, clipA, clipB));
                    }
                    output.add(current);
                } else if (previousInside) {
                    output.add(intersection(previous, current, clipA, clipB));
                }

                previous = current;
                previousInside = currentInside;
            }
        }

        Polygon result = new Polygon();
        Point previousPoint = null;

        for (DPoint point : output) {
            Point rounded = new Point(
                    (int) Math.round(point.x),
                    (int) Math.round(point.y)
            );

            if (!rounded.equals(previousPoint)) {
                result.add(rounded);
                previousPoint = rounded;
            }
        }

        if (result.size() > 1
                && result.get(0).equals(result.get(result.size() - 1))) {
            Polygon withoutDuplicateEnd = new Polygon();
            for (int i = 0; i < result.size() - 1; i++) {
                withoutDuplicateEnd.add(result.get(i));
            }
            return withoutDuplicateEnd;
        }

        return result;
    }

    private boolean isInside(
            DPoint point,
            Point edgeA,
            Point edgeB,
            double orientation
    ) {
        double cross = (edgeB.x() - edgeA.x()) * (point.y - edgeA.y())
                - (edgeB.y() - edgeA.y()) * (point.x - edgeA.x());


        return orientation > 0
                ? cross >= -EPSILON
                : cross <= EPSILON;
    }

    private DPoint intersection(
            DPoint segmentA,
            DPoint segmentB,
            Point clipA,
            Point clipB
    ) {
        double rx = segmentB.x - segmentA.x;
        double ry = segmentB.y - segmentA.y;
        double sx = clipB.x() - clipA.x();
        double sy = clipB.y() - clipA.y();

        double denominator = cross(rx, ry, sx, sy);
        if (Math.abs(denominator) < EPSILON) {
            return segmentB;
        }

        double qpx = clipA.x() - segmentA.x;
        double qpy = clipA.y() - segmentA.y;
        double t = cross(qpx, qpy, sx, sy) / denominator;

        return new DPoint(
                segmentA.x + t * rx,
                segmentA.y + t * ry
        );
    }

    private double cross(double ax, double ay, double bx, double by) {
        return ax * by - ay * bx;
    }

    private record DPoint(double x, double y) {
    }
}
