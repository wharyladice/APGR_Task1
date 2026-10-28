package model.raster.fill;

public final class CheckerPatternFill implements PatternFill {

    private final int firstColor;
    private final int secondColor;
    private final int cellSize;

    public CheckerPatternFill(int firstColor, int secondColor, int cellSize) {
        if (cellSize <= 0) {
            throw new IllegalArgumentException("Pattern cell size must be positive.");
        }

        this.firstColor = firstColor & 0xFFFFFF;
        this.secondColor = secondColor & 0xFFFFFF;
        this.cellSize = cellSize;
    }

    @Override
    public int colorAt(int x, int y) {
        int cellX = Math.floorDiv(x, cellSize);
        int cellY = Math.floorDiv(y, cellSize);
        return ((cellX + cellY) & 1) == 0 ? firstColor : secondColor;
    }
}
