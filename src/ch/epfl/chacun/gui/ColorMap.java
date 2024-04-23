package ch.epfl.chacun.gui;

import ch.epfl.chacun.PlayerColor;
import javafx.scene.paint.Color;

import java.util.Map;

final public class ColorMap {
    private final static double STROKE_BRIGHTNESS_FACTOR = 0.6;
    private ColorMap () {}

    private final static Map<PlayerColor, Color> fillColorMap = Map.of(
        PlayerColor.RED, Color.RED,
        PlayerColor.BLUE, Color.BLUE,
        PlayerColor.GREEN, Color.LIME,
        PlayerColor.YELLOW, Color.YELLOW,
        PlayerColor.PURPLE, Color.PURPLE
    );

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

    public static Color fillColor(PlayerColor playerColor) {
        return fillColorMap.get(playerColor);
    }

    public static Color strokeColor (PlayerColor playerColor) {
        return strokeColorMap.get(playerColor);
    }

}
