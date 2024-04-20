package ch.epfl.chacun.gui;

import ch.epfl.chacun.Occupant;
import ch.epfl.chacun.Tile;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.function.Consumer;

public final class DecksUI {

    private DecksUI() {}

    public static Node create(
            ObservableValue<Tile> tileO,
            ObservableValue<Integer> leftNormalTilesO,
            ObservableValue<Integer> leftMenhirTilesO,
            ObservableValue<String> textToDisplay,
            Consumer<Occupant> onOccupantClick
    ) {

        StackPane tileToPlacePane = new StackPane();

        tileO.addListener((_, _, newValue) -> {

            if (newValue != null) {
                tileToPlacePane.getChildren().setAll(new ImageView(ImageLoader.largeImageForTile(tileO.getValue().id())));
            } else {
                Text text = new Text();
                text.textProperty().bind(textToDisplay);
                tileToPlacePane.getChildren().setAll(text);
                text.setOnMouseClicked(_ -> onOccupantClick.accept(null));
            }

        });

        Node menhirNode = getDeckNode("MENHIR", leftMenhirTilesO);
        Node normalNode = getDeckNode("NORMAL", leftNormalTilesO);
        HBox hBox = new HBox(menhirNode, normalNode);

        VBox vBox = new VBox(hBox, tileToPlacePane);
        vBox.getStyleClass().add("decks");
        vBox.setId("decks");

        return null;
    }

    private static Node getDeckNode(String name, ObservableValue<Integer> leftTiles) {
        ImageView image = new ImageView();
        image.setId(name);
        Text text = new Text();
        text.textProperty().bind(leftTiles.map(String::valueOf));
        return new StackPane(image, text);
    }

}
