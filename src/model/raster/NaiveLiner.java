package model.raster;

public class NaiveLiner extends Liner {

    public NaiveLiner(Raster raster) {
        super(raster);
    }

    @Override
    protected void rasterize(int x1, int y1, int x2, int y2) {
        int dx = x2 - x1;
        int dy = y2 - y1;
        int steps = Math.max(Math.abs(dx), Math.abs(dy));

        if (steps == 0) {
            raster.setPixel(x1, y1, color);
            return;
        }

        double xIncrement = dx / (double) steps;
        double yIncrement = dy / (double) steps;
        double x = x1;
        double y = y1;

        for (int i = 0; i <= steps; i++) {
            raster.setPixel((int) Math.round(x), (int) Math.round(y), color);
            x += xIncrement;
            y += yIncrement;
        }
    }
}
