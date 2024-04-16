package ch.epfl.chacun.gui;

import ch.epfl.chacun.GameState;
import ch.epfl.chacun.Occupant;
import ch.epfl.chacun.PlayerColor;
import ch.epfl.chacun.TextMaker;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class PlayersUI {

    public static Node create(ObservableValue<GameState> gameStateO, TextMaker textMaker) {

        ObservableValue<Map<PlayerColor, Integer>> pointsO = gameStateO.map(gState -> gState.messageBoard().points());

        // create a new vbox that will be used to align elements (like players)
        VBox vBox = new VBox();
        vBox.getStyleClass().add("players");
        vBox.setId("players");

        // for each player color, create a text flow
        PlayerColor.ALL.forEach(playerColor -> {
            String name = textMaker.playerName(playerColor);

            if (name != null) {
                TextFlow textFlow = new TextFlow();
                textFlow.getStyleClass().add("player");
                ObservableValue<Boolean> isCurrentPlayer = gameStateO.map(gState -> gState.currentPlayer() == playerColor);
                isCurrentPlayer.addListener((observable, oldValue, newValue) -> {
                    if (newValue) textFlow.getStyleClass().add("current");
                    else textFlow.getStyleClass().remove("current");
                });

                Circle circle = new Circle(5);
                circle.setFill(ColorMap.fillColor(playerColor));

                ObservableValue<String> pointsTextO = pointsO.map(points -> STR."\{name} : \{points.getOrDefault(playerColor, 0)} points");
                ObservableValue<Map<Occupant.Kind, Integer>> occupantsO = gameStateO
                    .map(gState -> Map.of(
                        Occupant.Kind.PAWN, gState.freeOccupantsCount(playerColor, Occupant.Kind.PAWN),
                        Occupant.Kind.HUT, gState.freeOccupantsCount(playerColor, Occupant.Kind.HUT)
                    ));

                Text pointsText = new Text();
                pointsText.textProperty().bind(pointsTextO);

                textFlow.getChildren().addAll(circle, pointsText);

                // todo should we add a separator here?
                textFlow.getChildren().addAll(getOccupants(playerColor, Occupant.Kind.PAWN, occupantsO));
                textFlow.getChildren().add(new Text("   "));
                textFlow.getChildren().addAll(getOccupants(playerColor, Occupant.Kind.HUT, occupantsO));

                vBox.getChildren().add(textFlow);

            }
        });

        return vBox;
    }

    private static List<Node> getOccupants(
            PlayerColor playerColor, Occupant.Kind kind,
            ObservableValue<Map<Occupant.Kind, Integer>> occupantsO
    ) {
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < Occupant.occupantsCount(kind); i++) {
            Node occupantNode = Icon.newFor(playerColor, kind);
            // bind opacity to the number of used occupants
            int finalI = i;
            occupantNode.opacityProperty().bind(occupantsO.map(occupants -> occupants.get(kind) > finalI ? 1.0 : 0.1));
            nodes.add(occupantNode);
        }
        return nodes;
    }

}
