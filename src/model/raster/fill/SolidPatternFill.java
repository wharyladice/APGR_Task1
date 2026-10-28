package model.raster.fill;

public final class SolidPatternFill implements PatternFill {

    private final int color;

    public SolidPatternFill(int color) {
        this.color = color & 0xFFFFFF;
    }

    @Override
    public int colorAt(int x, int y) {
        return color;
    }
}
