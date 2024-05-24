package ch.epfl.chacun.gui;

import ch.epfl.chacun.Preconditions;
import javafx.scene.image.Image;

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
    /**
     * The size of the large tiles in pixels, as they should be displayed in the decks node (next tile to place)
     */
    public static final int LARGE_TILE_FIT_SIZE = LARGE_TILE_PIXEL_SIZE / 2;
    /**
     * The size of the normal tiles in pixels
     */
    public static final int NORMAL_TILE_PIXEL_SIZE = 256;
    /**
     * The size of the normal tiles in pixels,
     * as they should be displayed in the board node (placed tiles) and in the decks node (left tiles)
     */
    public static final int NORMAL_TILE_FIT_SIZE = NORMAL_TILE_PIXEL_SIZE / 2;
    /**
     * The size of the markers in pixels
     */
    public static final int MARKER_PIXEL_SIZE = 96;
    /**
     * The size of the markers in pixels, as they should be displayed in the board node
     */
    public static final int MARKER_FIT_SIZE = MARKER_PIXEL_SIZE / 2;

    /**
     * This is a utility class and therefore is not instantiable
     */
    private ImageLoader() {
    }

    /**
     * Returns the image corresponding to the tile with the given tile id and pixel size
     *
     * @param tileId    the id of the tile
     * @param pixelSize the size of the image in pixels
     * @return the image corresponding to the tile with the given tile id and pixel size
     */
    private static Image imageForTile(int tileId, int pixelSize) {
        // the ids of the tiles to charge have 2 digits
        return new Image(FormatProcessor.FMT."/\{pixelSize}/%02d\{tileId}.jpg");
    }

    /**
     * Returns the normal image for the tile with the given tile id
     *
     * @param tileId the id of the tile whose image has to be returned
     * @return the normal image for the tile with the given tile id
     */
    public static Image normalImageForTile(int tileId) {
        Preconditions.checkArgument(tileId >= 0);
        return imageForTile(tileId, NORMAL_TILE_PIXEL_SIZE);
    }

    /**
     * Returns the large image for the tile with the given tile id
     *
     * @param tileId the id of the tile whose image has to be returned
     * @return the large image for the tile with the given tile id
     */
    public static Image largeImageForTile(int tileId) {
        Preconditions.checkArgument(tileId >= 0);
        return imageForTile(tileId, LARGE_TILE_PIXEL_SIZE);
    }

}
