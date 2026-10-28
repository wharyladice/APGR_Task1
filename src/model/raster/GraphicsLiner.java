package model.raster;

import java.awt.Color;
import java.awt.Graphics2D;

public class GraphicsLiner extends Liner {

    public GraphicsLiner(Raster raster) {
        super(raster);
    }

    @Override
    protected void rasterize(int x1, int y1, int x2, int y2) {
        if (!(raster instanceof RasterBI rasterBI)) {
            throw new IllegalStateException("GraphicsLiner requires RasterBI");
        }

        Graphics2D graphics = rasterBI.getImage().createGraphics();
        graphics.setColor(new Color(color));
        graphics.drawLine(x1, y1, x2, y2);
        graphics.dispose();
    }
}
