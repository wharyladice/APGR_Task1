package model.objects;

public record Line(Point a, Point b) {

    public boolean isHorizontal() {
        return a.y() == b.y();
    }

    public boolean isVertical() {
        return a.x() == b.x();
    }

    public Line orientate() {
        return a.y() <= b.y() ? this : new Line(b, a);
    }

    public boolean isInside(Point point) {
        long dx = b.x() - a.x();
        long dy = b.y() - a.y();
        long px = point.x() - a.x();
        long py = point.y() - a.y();
        return dx * py - dy * px >= 0;
    }

    public Point calculateIntersection(Point p1, Point p2) {
        double x1 = a.x();
        double y1 = a.y();
        double x2 = b.x();
        double y2 = b.y();
        double x3 = p1.x();
        double y3 = p1.y();
        double x4 = p2.x();
        double y4 = p2.y();

        double denominator = (x1 - x2) * (y3 - y4)
                - (y1 - y2) * (x3 - x4);

        if (Math.abs(denominator) < 1e-9) {
            return null;
        }

        double determinant1 = x1 * y2 - y1 * x2;
        double determinant2 = x3 * y4 - y3 * x4;

        double x = (determinant1 * (x3 - x4)
                - (x1 - x2) * determinant2) / denominator;
        double y = (determinant1 * (y3 - y4)
                - (y1 - y2) * determinant2) / denominator;

        return new Point((int) Math.round(x), (int) Math.round(y));
    }
}
