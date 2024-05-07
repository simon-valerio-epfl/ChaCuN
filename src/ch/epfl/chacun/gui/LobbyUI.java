package ch.epfl.chacun.gui;

import ch.epfl.chacun.GameState;
import ch.epfl.chacun.Occupant;
import ch.epfl.chacun.PlayerColor;
import ch.epfl.chacun.TextMaker;
import ch.epfl.chacun.net.WSPlayer;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class LobbyUI {
    /**
     * This class is not instantiable
     */
    private LobbyUI() {
    }

    public static Node create() {

        BorderPane borderPane = new BorderPane();
        borderPane.getStylesheets().add("lobby.css");

        // create title
        Text title = new Text("Bienvenue dans ChaCuN !");
        title.getStyleClass().add("title");

        // create title container to center it
        VBox titleContainer = new VBox();
        titleContainer.getChildren().add(title);
        titleContainer.getStyleClass().add("title-container");
        titleContainer.setAlignment(Pos.CENTER);

        borderPane.setTop(titleContainer);

        VBox playerVbox = new VBox();
        playerVbox.setAlignment(Pos.CENTER);

        List<WSPlayer> players = new ArrayList<>(List.of(
            new WSPlayer("Bernard", false),
            new WSPlayer("Jean-Jacques", true)
        ));

        int longestNameLength = players.stream().map(WSPlayer::getUsername).mapToInt(String::length).max().orElse(0);

        for (WSPlayer player : players) {
            HBox hBox = new HBox();
            hBox.setAlignment(Pos.CENTER);
            hBox.getStyleClass().add("player");
            TextFlow textFlow = new TextFlow();
            // pad with spaces
            Text text = new Text(player.getUsername() + " ".repeat(longestNameLength - player.getUsername().length()));
            textFlow.getChildren().add(text);
            hBox.getChildren().add(textFlow);
            Circle circle = new Circle(5);
            circle.getStyleClass().add("status");
            circle.getStyleClass().add("ready");
            hBox.getChildren().add(circle);
            playerVbox.getChildren().add(hBox);
        }

        borderPane.setCenter(playerVbox);

        VBox buttonContainer = new VBox();
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.getStyleClass().add("start-button-container");

        Text startButton = new Text("Je suis prêt !");
        startButton.getStyleClass().add("start-button");
        buttonContainer.getChildren().add(startButton);

        borderPane.setBottom(buttonContainer);

        return borderPane;

    }

}
