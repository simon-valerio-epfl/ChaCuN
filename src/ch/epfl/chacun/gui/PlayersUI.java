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
import java.util.Objects;

/**
 * This class represents the graphical representation
 * of a node containing the occupants of the game with their names, their colours,
 * their points and available occupants, with the current player highlighted
 *
 * @author Valerio De Santis (373247)
 * @author Simon Lefort (371918)
 */
public final class PlayersUI {
    /**
     * The opacity of an occupant when it is placed on the board
     */
    private static final double PLACED_OCCUPANT_OPACITY = .1;
    /**
     * The opacity of an occupant when it is still held by the player
     */
    private static final double HELD_OCCUPANT_OPACITY = 1;
    /**
     * The radius of the circle representing the colour of the player
     */
    private static final int PLAYER_CIRCLE_RADIUS = 5;

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
     * @param textMakerO the observable value of the text maker used to generate the text for the players' names and points
     * @return a graphical node containing the occupants of the game with their names, their colours,
     * their points and available occupants, with the current player highlighted
     */
    public static Node create(ObservableValue<GameState> gameStateO, ObservableValue<TextMaker> textMakerO) {

        ObservableValue<Map<PlayerColor, Integer>> pointsO = gameStateO.map(gState -> gState.messageBoard().points());

        // create a new vbox that will be used to align elements (like players)
        VBox vBox = new VBox();
        vBox.getStylesheets().add("players.css");
        vBox.setId("players");

        gameStateO.map(GameState::players).addListener((_, __, newValue) -> {
            vBox.getChildren().clear();
            TextMaker textMaker = textMakerO.getValue();
            List<TextFlow> players = createPlayers(newValue, gameStateO, pointsO, textMaker);
            vBox.getChildren().addAll(players);
        });

        vBox.getChildren().addAll(createPlayers(
                gameStateO.getValue().players(),
                gameStateO,
                pointsO,
                textMakerO.getValue()
        ));

        return vBox;
    }

    private static List<TextFlow> createPlayers(
            List<PlayerColor> players,
            ObservableValue<GameState> gameStateO,
            ObservableValue<Map<PlayerColor, Integer>> pointsO,
            TextMaker textMaker
    ) {
        return players.stream().map(playerColor -> {
            String name = textMaker.playerName(playerColor);

            TextFlow textFlow = new TextFlow();
            textFlow.getStyleClass().add("player");

            // we update here the current player
            ObservableValue<Boolean> isCurrentPlayer = gameStateO.map(gState -> gState.currentPlayer() == playerColor);
            isCurrentPlayer.addListener((_, _, newValue) -> {
                if (newValue) textFlow.getStyleClass().add("current");
                else textFlow.getStyleClass().remove("current");
            });
            if (isCurrentPlayer.getValue()) textFlow.getStyleClass().add("current");

            Circle circle = new Circle(PLAYER_CIRCLE_RADIUS, ColorMap.fillColor(playerColor));

            ObservableValue<String> pointsTextO = pointsO.map(points ->
                    STR." \{name} : \{textMaker.points(points.getOrDefault(playerColor, 0))}\n"
            );

            Text pointsText = new Text();
            pointsText.textProperty().bind(pointsTextO);

            textFlow.getChildren().addAll(circle, pointsText);

            textFlow.getChildren().addAll(getOccupants(playerColor, Occupant.Kind.HUT, gameStateO));
            textFlow.getChildren().add(new Text("   "));
            textFlow.getChildren().addAll(getOccupants(playerColor, Occupant.Kind.PAWN, gameStateO));

            return textFlow;
        }).toList();
    }

    /**
     * Returns a list of nodes representing each the occupants of a player,
     * with the opacity of each node bound to the number of used and available occupants
     *
     * @param playerColor the color of the player owning this list of occupants
     * @param kind        the kind of the occupants to represent
     * @param gameStateO  the observable current state of a game
     * @return a list of nodes representing each the occupants of a player,
     * with the opacity of each node bound to the number of used and available occupants
     */
    private static List<Node> getOccupants(
            PlayerColor playerColor, Occupant.Kind kind,
            ObservableValue<GameState> gameStateO
    ) {
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < Occupant.occupantsCount(kind); i++) {
            Node occupantNode = Icon.newFor(playerColor, kind);
            // bind opacity to the number of used occupants
            int finalI = i;
            occupantNode.opacityProperty().bind(
                    gameStateO.map(gState ->
                            gState.freeOccupantsCount(playerColor, kind) > finalI
                                    ? HELD_OCCUPANT_OPACITY : PLACED_OCCUPANT_OPACITY
                    )
            );
            nodes.add(occupantNode);
        }
        return nodes;
    }

}
