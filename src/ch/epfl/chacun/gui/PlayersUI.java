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
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class represents the graphical representation
 * of a node containing the occupants of the game with their names, their colours,
 * their points and available occupants, with the current player highlighted
 *
 * @author Valerio De Santis (373247)
 * @author Simon Lefort (371918)
 */
public final class PlayersUI {

    private static final double PLACED_OCCUPANT_OPACITY = .1;
    private static final double HELD_OCCUPANT_OPACITY = 1;

    /**
     * This class is not instantiable
     */
    private PlayersUI() {
    }

    /**
     * Creates the node containing the occupants of the game with their names, their colours,
     * their points and available occupants, with the current player highlighted
     *
     * @param gameStateO the observable current state of a game
     * @param textMakerO  the observable value of the text maker used to generate the text for the players' names and points
     * @return a graphical node containing the occupants of the game with their names, their colours,
     * their points and available occupants, with the current player highlighted
     */
    public static Node create(
            ObservableValue<GameState> gameStateO,
            ObservableValue<TextMaker> textMakerO
    ) {

        ObservableValue<Map<PlayerColor, Integer>> pointsO = gameStateO.map(gState -> gState.messageBoard().points());

        // create a new vbox that will be used to align elements (like players)
        VBox vBox = new VBox();
        vBox.getStylesheets().add("players.css");
        vBox.setId("players");

        ObservableValue<List<PlayerColor>> players = gameStateO.map(gState ->
            gState.players().stream().sorted().toList()
        );

        Function<List<PlayerColor>, Boolean> addPlayersNodes = (newPlayers) -> {
            vBox.getChildren().clear();
            newPlayers
                    .forEach(playerColor -> {
                        String name = textMakerO.getValue().playerName(playerColor);

                        if (name == null) return;

                        TextFlow textFlow = new TextFlow();
                        textFlow.getStyleClass().add("player");

                        // we update here the current player
                        ObservableValue<Boolean> isCurrentPlayer = gameStateO.map(gState -> gState.currentPlayer() == playerColor);
                        isCurrentPlayer.addListener((_, _, newValue) -> {
                            if (newValue) textFlow.getStyleClass().add("current");
                            else textFlow.getStyleClass().remove("current");
                        });

                        Circle circle = new Circle(5);
                        circle.setFill(ColorMap.fillColor(playerColor));

                        ObservableValue<String> pointsTextO = pointsO.map(points ->
                                STR." \{name} : \{
                                    textMakerO.getValue().points(points.getOrDefault(playerColor, 0))
                                }\n"
                        );
                        ObservableValue<Map<Occupant.Kind, Integer>> occupantsO = gameStateO
                                .map(gState -> Map.of(
                                        Occupant.Kind.PAWN, gState.freeOccupantsCount(playerColor, Occupant.Kind.PAWN),
                                        Occupant.Kind.HUT, gState.freeOccupantsCount(playerColor, Occupant.Kind.HUT)
                                ));

                        Text pointsText = new Text();
                        pointsText.textProperty().bind(pointsTextO);

                        textFlow.getChildren().addAll(circle, pointsText);

                        textFlow.getChildren().addAll(getOccupants(playerColor, Occupant.Kind.HUT, occupantsO));
                        textFlow.getChildren().add(new Text("   "));
                        textFlow.getChildren().addAll(getOccupants(playerColor, Occupant.Kind.PAWN, occupantsO));

                        vBox.getChildren().add(textFlow);

                    });
            return true;
        };

        players.addListener((_, _, newPlayers) -> addPlayersNodes.apply(newPlayers));
        addPlayersNodes.apply(players.getValue());

        return vBox;
    }

    /**
     * Returns a list of nodes representing each the occupants of a player,
     * with the opacity of each node bound to the number of used and available occupants
     *
     * @param playerColor the color of the player owning this list of occupants
     * @param kind        the kind of the occupants to represent
     * @param occupantsO  the observable map of the number of used occupants
     * @return a list of nodes representing each the occupants of a player,
     * with the opacity of each node bound to the number of used and available occupants
     */
    private static List<Node> getOccupants(
            PlayerColor playerColor, Occupant.Kind kind,
            ObservableValue<Map<Occupant.Kind, Integer>> occupantsO
    ) {
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < Occupant.occupantsCount(kind); i++) {
            Node occupantNode = Icon.newFor(playerColor, kind);
            // bind opacity to the number of used occupants
            int finalI = i;
            occupantNode.opacityProperty().bind(
                    occupantsO.map(occupants -> occupants.get(kind) > finalI ? HELD_OCCUPANT_OPACITY : PLACED_OCCUPANT_OPACITY)
            );
            nodes.add(occupantNode);
        }
        return nodes;
    }

}
