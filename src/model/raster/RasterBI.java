package model.raster;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public final class RasterBI implements Raster {

    private final BufferedImage image;
    private int clearColor;

    public RasterBI(int width, int height) {
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        clearColor = 0x161633;
        clear();
    }

    @Override
    public void clear() {
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(new Color(clearColor));
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
        graphics.dispose();
    }

    @Override
    public void setPixel(int x, int y, int color) {
        if (!isInside(x, y)) {
            return;
        }
        image.setRGB(x, y, color & 0xFFFFFF);
    }

    @Override
    public int getPixel(int x, int y) {
        if (!isInside(x, y)) {
            return -1;
        }
        return image.getRGB(x, y) & 0xFFFFFF;
    }

    @Override
    public int getWidth() {
        return image.getWidth();
    }

    @Override
    public int getHeight() {
        return image.getHeight();
    }

    @Override
    public boolean isInside(int x, int y) {
        return x >= 0 && y >= 0 && x < image.getWidth() && y < image.getHeight();
    }

    public BufferedImage getImage() {
        return image;
    }

    public int getClearColor() {
        return clearColor;
    }

    public void setClearColor(int clearColor) {
        this.clearColor = clearColor & 0xFFFFFF;
    }
}
