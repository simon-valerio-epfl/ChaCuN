package ch.epfl.chacun.gui;

import ch.epfl.chacun.Occupant;
import ch.epfl.chacun.PlayerColor;
import javafx.scene.Node;
import javafx.scene.shape.SVGPath;

/**
 * This class provides utility methods to create occupants' graphical representations
 *
 * @author Valerio De Santis (373247)
 * @author Simon Lefort (371918)
 */
public final class Icon {
    /**
     * This class is not instantiable
     */
    private Icon() {
    }

    /**
     * The SVG path for a pawn
     */
    private final static String PAWN_SVG = "M -10 10 H -4 L 0 2 L 6 10 H 12 L 5 0 L 12 -2 L 12 -4 L 6 -6 L 6 -10 L 0 -10 L -2 -4 L -6 -2 L -8 -10 L -12 -10 L -8 6 Z";
    /**
     * The SVG path for a hut
     */
    private final static String HUT_SVG = "M -8 10 H 8 V 2 H 12 L 0 -10 L -12 2 H -8 Z";

    /**
     * Creates a new node representing an occupant of the given kind and player
     *
     * @param playerColor the color of the player owning the occupant
     * @param kind        the kind of the occupant
     * @return a new node representing the occupant
     */
    public static Node newFor(PlayerColor playerColor, Occupant.Kind kind) {
        SVGPath svgPath = new SVGPath();
        svgPath.setContent(switch (kind) {
            case PAWN -> PAWN_SVG;
            case HUT -> HUT_SVG;
        });
        svgPath.setFill(ColorMap.fillColor(playerColor));
        svgPath.setStroke(ColorMap.strokeColor(playerColor));
        return svgPath;
    }

}
