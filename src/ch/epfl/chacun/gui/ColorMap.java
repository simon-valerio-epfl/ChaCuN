package ch.epfl.chacun.gui;

import ch.epfl.chacun.PlayerColor;
import javafx.scene.paint.Color;

import java.util.Map;

/**
 * This class provides utility methods to get colors for players
 *
 * @author Valerio De Santis (373247)
 * @author Simon Lefort (371918)
 */
final public class ColorMap {
    private final static double STROKE_BRIGHTNESS_FACTOR = 0.6;
    private ColorMap () {}

    /**
     * A map associating each player color to its fill color
     */
    private final static Map<PlayerColor, Color> fillColorMap = Map.of(
        PlayerColor.RED, Color.RED,
        PlayerColor.BLUE, Color.BLUE,
        PlayerColor.GREEN, Color.LIME,
        PlayerColor.YELLOW, Color.YELLOW,
        PlayerColor.PURPLE, Color.PURPLE
    );
    /**
     * A map associating each player color to its stroke color
     */
    private final static Map<PlayerColor, Color> strokeColorMap = Map.of(
        PlayerColor.RED, Color.WHITE,
        PlayerColor.BLUE, Color.WHITE,
        PlayerColor.PURPLE, Color.WHITE,
        PlayerColor.GREEN, fillColor(PlayerColor.GREEN).deriveColor(
                0, 1, STROKE_BRIGHTNESS_FACTOR, 1
            ),
        PlayerColor.YELLOW, fillColor(PlayerColor.YELLOW).deriveColor(
                0, 1, STROKE_BRIGHTNESS_FACTOR, 1
            )
    );

    /**
     * Returns the fill color for the given player color
     * @param playerColor the player color
     * @return the fill color for the given player color
     */
    public static Color fillColor(PlayerColor playerColor) {
        return fillColorMap.get(playerColor);
    }

    /**
     * Returns the stroke color for the given player color
     * @param playerColor the player color
     * @return the stroke color for the given player color
     */
    public static Color strokeColor (PlayerColor playerColor) {
        return strokeColorMap.get(playerColor);
    }

}
