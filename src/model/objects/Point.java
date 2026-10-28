package model.objects;

public record Point(int x, int y) {

    public Point withX(int x) {
        return new Point(x, y);
    }

    public Point withY(int y) {
        return new Point(x, y);
    }

    public Point withXY(int x, int y) {
        return new Point(x, y);
    }

    public double distanceTo(Point other) {
        return Math.hypot(other.x - x, other.y - y);
    }
}
