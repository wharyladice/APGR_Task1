package model.raster;

public interface Raster {

    void clear();

    void setPixel(int x, int y, int color);

    int getPixel(int x, int y);

    int getWidth();

    int getHeight();

    boolean isInside(int x, int y);
}
