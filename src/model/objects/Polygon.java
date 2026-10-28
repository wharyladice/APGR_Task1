package model.objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Polygon {

    private final List<Point> points = new ArrayList<>();

    public void add(Point point) {
        points.add(point);
    }

    public void addMore(Point... newPoints) {
        Collections.addAll(points, newPoints);
    }

    public void clear() {
        points.clear();
    }

    public boolean isEmpty() {
        return points.isEmpty();
    }

    public int size() {
        return points.size();
    }

    public Point get(int index) {
        return points.get(index);
    }

    public void set(int index, Point point) {
        points.set(index, point);
    }

    public Point remove(int index) {
        return points.remove(index);
    }

    public void reverse() {
        Collections.reverse(points);
    }

    public List<Point> getPoints() {
        return List.copyOf(points);
    }

    public List<Line> toEdges() {
        List<Line> edges = new ArrayList<>();
        if (points.size() < 2) {
            return edges;
        }

        if (points.size() == 2) {
            edges.add(new Line(points.get(0), points.get(1)));
            return edges;
        }


        for (int i = 0; i < points.size(); i++) {
            Point a = points.get(i);
            Point b = points.get((i + 1) % points.size());
            edges.add(new Line(a, b));
        }

        return edges;
    }

    public Polygon copy() {
        Polygon copy = new Polygon();
        copy.points.addAll(points);
        return copy;
    }

    public double signedArea() {
        if (points.size() < 3) {
            return 0.0;
        }

        long sum = 0;
        for (int i = 0; i < points.size(); i++) {
            Point a = points.get(i);
            Point b = points.get((i + 1) % points.size());
            sum += (long) a.x() * b.y() - (long) b.x() * a.y();
        }
        return sum / 2.0;
    }

    public boolean isConvex() {
        if (points.size() < 3) {
            return false;
        }

        int sign = 0;
        int n = points.size();
        for (int i = 0; i < n; i++) {
            Point a = points.get(i);
            Point b = points.get((i + 1) % n);
            Point c = points.get((i + 2) % n);

            long cross = (long) (b.x() - a.x()) * (c.y() - b.y())
                    - (long) (b.y() - a.y()) * (c.x() - b.x());

            if (cross == 0) {
                continue;
            }

            int currentSign = cross > 0 ? 1 : -1;
            if (sign == 0) {
                sign = currentSign;
            } else if (sign != currentSign) {
                return false;
            }
        }

        return sign != 0;
    }
}
