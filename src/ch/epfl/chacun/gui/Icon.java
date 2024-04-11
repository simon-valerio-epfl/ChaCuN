package ch.epfl.chacun.gui;

import ch.epfl.chacun.Occupant;
import ch.epfl.chacun.PlayerColor;
import javafx.scene.Node;
import javafx.scene.shape.SVGPath;

import java.util.Map;

public class Icon {

    private final static String PAWN_SVG = "M -10 10 H -4 L 0 2 L 6 10 H 12 L 5 0 L 12 -2 L 12 -4 L 6 -6 L 6 -10 L 0 -10 L -2 -4 L -6 -2 L -8 -10 L -12 -10 L -8 6 Z";
    private final static String HUT_SVG = "M -8 10 H 8 V 2 H 12 L 0 -10 L -12 2 H -8 Z";
    private final static Map<Occupant.Kind, String> SVG_MAP = Map.of(
        Occupant.Kind.PAWN, PAWN_SVG,
        Occupant.Kind.HUT, HUT_SVG
    );

    public Node newFor(PlayerColor playerColor, Occupant.Kind kind) {
        SVGPath svgPath = new SVGPath();
        svgPath.setContent(SVG_MAP.get(kind));
        svgPath.setFill(ColorMap.fillColor(playerColor));
        svgPath.setStroke(ColorMap.strokeColor(playerColor));
        return svgPath;
    }

}
