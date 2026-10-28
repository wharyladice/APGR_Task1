package model.raster.fill;

@FunctionalInterface
public interface PatternFill {
    int colorAt(int x, int y);
}
