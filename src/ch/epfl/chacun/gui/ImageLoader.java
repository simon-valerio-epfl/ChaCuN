package ch.epfl.chacun.gui;

import javafx.scene.image.Image;

public final class ImageLoader {

    public static final int LARGE_TILE_PIXEL_SIZE = 512;
    public static final int LARGE_TILE_FIT_SIZE = 256;
    public static final int NORMAL_TILE_PIXEL_SIZE = 256;
    public static final int NORMAL_TILE_FIT_SIZE = 128;
    public static final int MARKER_PIXEL_SIZE = 96;
    public static final int MARKER_FIT_SIZE = 48;

    private ImageLoader() {}

    private static Image imageForTile(int tileId, int pixelSize) {
        return new Image(STR."\{pixelSize}/\{tileId < 10 ? STR."0\{tileId}" : tileId}.jpg");
    }

    public static Image normalImageForTile(int tileId) {
        return imageForTile(tileId, NORMAL_TILE_PIXEL_SIZE);
    }

    public static Image largeImageForTile(int tileId) {
        return imageForTile(tileId, LARGE_TILE_PIXEL_SIZE);
    }

}
