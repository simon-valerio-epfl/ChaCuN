package ch.epfl.chacun.gui;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.FormatProcessor;

/**
 * This class is a utility class that provides methods to load images
 *
 * @author Valerio De Santis (373247)
 * @author Simon Lefort (371918)
 */
public final class ImageLoader {
    /**
     * The size of the large tiles in pixels
     */
    public static final int LARGE_TILE_PIXEL_SIZE = 512;
    // todo what is this "fit"
    public static final int LARGE_TILE_FIT_SIZE = 256;
    /**
     * The size of the normal tiles in pixels
     */
    public static final int NORMAL_TILE_PIXEL_SIZE = 256;
    public static final int NORMAL_TILE_FIT_SIZE = 128;
    /**
     * The size of the markers in pixels
     */
    public static final int MARKER_PIXEL_SIZE = 96;
    public static final int MARKER_FIT_SIZE = 48;
    /**
     * An empty image, graphically represented by a grey square
     */
    public static final Image EMPTY_IMAGE;
    static {
        WritableImage writableImage = new WritableImage(1, 1);
        writableImage.getPixelWriter().setColor(0, 0, Color.gray(0.98));
        EMPTY_IMAGE = writableImage;
    }

    /**
     * This is a utility class and therefore is not instantiable
     */
    private ImageLoader() {}

    // todo what is fmt

    /**
     * Returns the image corresponding to the tile with the given tile id and pixel size
     * @param tileId the id of the tile
     * @param pixelSize the size of the image in pixels
     * @return the image corresponding to the tile with the given tile id and pixel size
     */
    private static Image imageForTile(int tileId, int pixelSize) {
        // todo il n'existe pas un %2d en java ?
        // the ids of the tiles to charge have 2 digits
        return new Image(FormatProcessor.FMT."\{pixelSize}/%02d\{tileId}.jpg");
        // todo check if the line above works , else return new Image(FormatProcessor.FMT."\{pixelSize}/\{tileId < 10 ? STR."0\{tileId}" : tileId}.jpg");
    }

    /**
     * Returns the normal image for the tile with the given tile id
     * @param tileId the id of the tile whose image has to be returned
     * @return the normal image for the tile with the given tile id
     */
    public static Image normalImageForTile(int tileId) {
        return imageForTile(tileId, NORMAL_TILE_PIXEL_SIZE);
    }

    /**
     * Returns the large image for the tile with the given tile id
     * @param tileId the id of the tile whose image has to be returned
     * @return the large image for the tile with the given tile id
     */
    public static Image largeImageForTile(int tileId) {
        return imageForTile(tileId, LARGE_TILE_PIXEL_SIZE);
    }

}
