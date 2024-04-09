package ch.epfl.chacun.gui;

import ch.epfl.chacun.PlayerColor;
import javafx.scene.paint.Color;

import java.util.Map;

public class ColorMap {

    private ColorMap() {}


    private final static Map<PlayerColor, Color> fillColorMap = Map.of(
        PlayerColor.RED, Color.RED,
        PlayerColor.BLUE, Color.BLUE,
        PlayerColor.GREEN, Color.LIME,
        PlayerColor.YELLOW, Color.YELLOW
    );

    private final static Map<PlayerColor, Color> strokeColorMap = Map.of(
        PlayerColor.RED, Color.WHITE,
        PlayerColor.BLUE, Color.WHITE,
        PlayerColor.GREEN, fillColor(PlayerColor.GREEN).deriveColor(0, 1, 0.6, 1),
        PlayerColor.YELLOW, fillColor(PlayerColor.YELLOW).deriveColor(0, 1, 0.6, 1)
    );

    public static Color fillColor(PlayerColor playerColor) {
        return fillColorMap.get(playerColor);
    }

    public static Color strokeColor (PlayerColor playerColor) {
        return strokeColorMap.get(playerColor);
    }

}
