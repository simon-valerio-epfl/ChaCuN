package ch.epfl.chacun.gui;

import ch.epfl.chacun.PlayerColor;
import javafx.scene.paint.Color;

import java.util.Objects;


/**
 * This class provides utility methods to get colors for players
 *
 * @author Valerio De Santis (373247)
 * @author Simon Lefort (371918)
 */
public final class ColorMap {
    /**
     * The factor to reduce the brightness of the stroke color
     */
    private static final double STROKE_BRIGHTNESS_FACTOR = 0.6;

    /**
     * This class is not instantiable
     */
    private ColorMap() {
    }

    /**
     * Derives the stroke color for the given player color, modifying its brightness
     *
     * @param playerColor the player color
     * @return the stroke color for the given player color
     */
    private static Color deriveStrokePlayerColor(PlayerColor playerColor) {
        return fillColor(playerColor).deriveColor(0, 1, STROKE_BRIGHTNESS_FACTOR, 1);
    }

    /**
     * Returns the fill color for the given player color
     *
     * @param playerColor the player color
     * @return the fill color for the given player color
     */
    public static Color fillColor(PlayerColor playerColor) {
        Objects.requireNonNull(playerColor);
        return switch (playerColor) {
            case RED -> Color.RED;
            case BLUE -> Color.BLUE;
            case GREEN -> Color.LIME;
            case YELLOW -> Color.YELLOW;
            case PURPLE -> Color.PURPLE;
        };
    }

    /**
     * Returns the stroke color for the given player color
     *
     * @param playerColor the player color
     * @return the stroke color for the given player color
     */
    public static Color strokeColor(PlayerColor playerColor) {
        Objects.requireNonNull(playerColor);
        return switch (playerColor) {
            case RED, BLUE, PURPLE -> Color.WHITE;
            case GREEN, YELLOW -> deriveStrokePlayerColor(playerColor);
        };
    }

}
