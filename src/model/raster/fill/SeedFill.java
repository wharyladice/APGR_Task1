package model.raster.fill;

import model.objects.Point;
import model.raster.RasterBI;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class SeedFill {

    public void fill(RasterBI image, int x, int y, int fillColor) {
        fillBackground(image, x, y, fillColor, image.getClearColor());
    }

    public List<Point> findClosedBackgroundRegion(
            RasterBI image,
            int x,
            int y,
            int backgroundColor
    ) {
        backgroundColor &= 0xFFFFFF;

        if (!image.isInside(x, y) || image.getPixel(x, y) != backgroundColor) {
            return List.of();
        }

        int finalBackgroundColor = backgroundColor;
        return findClosedRegion(
                image,
                x,
                y,
                color -> color == finalBackgroundColor
        );
    }

    public List<Point> findClosedBoundaryRegion(
            RasterBI image,
            int x,
            int y,
            int boundaryColor
    ) {
        boundaryColor &= 0xFFFFFF;

        if (!image.isInside(x, y) || image.getPixel(x, y) == boundaryColor) {
            return List.of();
        }

        int finalBoundaryColor = boundaryColor;
        return findClosedRegion(
                image,
                x,
                y,
                color -> color != finalBoundaryColor
        );
    }

    public List<Point> findClosedBoundaryRegion(
            RasterBI image,
            int x,
            int y,
            int boundaryColor,
            int fillColor
    ) {
        return findClosedBoundaryRegion(image, x, y, boundaryColor);
    }

    public void applyPattern(
            RasterBI image,
            List<Point> region,
            PatternFill patternFill
    ) {
        if (patternFill == null) {
            return;
        }

        for (Point point : region) {
            image.setPixel(point.x(), point.y(), patternFill.colorAt(point.x(), point.y()));
        }
    }

    public void fillBackground(
            RasterBI image,
            int x,
            int y,
            int fillColor,
            int backgroundColor
    ) {
        fillColor &= 0xFFFFFF;
        backgroundColor &= 0xFFFFFF;

        if (!image.isInside(x, y)
                || image.getPixel(x, y) != backgroundColor
                || fillColor == backgroundColor) {
            return;
        }

        Deque<Point> stack = new ArrayDeque<>();
        stack.push(new Point(x, y));

        while (!stack.isEmpty()) {
            Point point = stack.pop();
            int px = point.x();
            int py = point.y();

            if (!image.isInside(px, py)
                    || image.getPixel(px, py) != backgroundColor) {
                continue;
            }

            image.setPixel(px, py, fillColor);
            stack.push(new Point(px + 1, py));
            stack.push(new Point(px - 1, py));
            stack.push(new Point(px, py + 1));
            stack.push(new Point(px, py - 1));
        }
    }

    public void fillBoundary(
            RasterBI image,
            int x,
            int y,
            int fillColor,
            int boundaryColor
    ) {
        fillColor &= 0xFFFFFF;
        boundaryColor &= 0xFFFFFF;

        if (!image.isInside(x, y)
                || image.getPixel(x, y) == boundaryColor
                || image.getPixel(x, y) == fillColor
                || fillColor == boundaryColor) {
            return;
        }

        Deque<Point> stack = new ArrayDeque<>();
        stack.push(new Point(x, y));

        while (!stack.isEmpty()) {
            Point point = stack.pop();
            int px = point.x();
            int py = point.y();

            if (!image.isInside(px, py)) {
                continue;
            }

            int currentColor = image.getPixel(px, py);
            if (currentColor == boundaryColor || currentColor == fillColor) {
                continue;
            }

            image.setPixel(px, py, fillColor);
            stack.push(new Point(px + 1, py));
            stack.push(new Point(px - 1, py));
            stack.push(new Point(px, py + 1));
            stack.push(new Point(px, py - 1));
        }
    }

    private List<Point> findClosedRegion(
            RasterBI image,
            int startX,
            int startY,
            PixelCondition condition
    ) {
        int width = image.getWidth();
        int height = image.getHeight();
        boolean[] visited = new boolean[width * height];
        Deque<Point> stack = new ArrayDeque<>();
        List<Point> region = new ArrayList<>();
        stack.push(new Point(startX, startY));

        while (!stack.isEmpty()) {
            Point point = stack.pop();
            int x = point.x();
            int y = point.y();

            if (!image.isInside(x, y)) {
                continue;
            }

            int index = y * width + x;
            if (visited[index]) {
                continue;
            }
            visited[index] = true;

            if (!condition.accept(image.getPixel(x, y))) {
                continue;
            }

            if (x == 0 || y == 0 || x == width - 1 || y == height - 1) {
                return List.of();
            }

            region.add(point);
            stack.push(new Point(x + 1, y));
            stack.push(new Point(x - 1, y));
            stack.push(new Point(x, y + 1));
            stack.push(new Point(x, y - 1));
        }

        return region;
    }

    @FunctionalInterface
    private interface PixelCondition {
        boolean accept(int color);
    }
}
