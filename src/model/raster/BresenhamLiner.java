package model.raster;

public class BresenhamLiner extends Liner {

    public BresenhamLiner(Raster raster) {
        super(raster);
    }

    @Override
    protected void rasterize(int x1, int y1, int x2, int y2) {

        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int error = dx - dy;

        int x = x1;
        int y = y1;

        while (true) {
            raster.setPixel(x, y, color);

            if (x == x2 && y == y2) {
                break;
            }

            int doubledError = 2 * error;
            if (doubledError > -dy) {
                error -= dy;
                x += sx;
            }
            if (doubledError < dx) {
                error += dx;
                y += sy;
            }
        }
    }
}
