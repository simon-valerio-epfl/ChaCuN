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

        VBox vBox = new VBox();
        vBox.getStyleClass().add("decks");
        vBox.setId("decks");

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

        HBox hBox = new HBox();

        StackPane menhirStackPane = getStackPane("MENHIR", leftMenhirTilesO);
        StackPane normalStackPane = getStackPane("NORMAL", leftNormalTilesO);

        hBox.getChildren().addAll(menhirStackPane, normalStackPane);
        vBox.getChildren().addAll(hBox, tileToPlacePane);

        return null;
    }

    private static StackPane getStackPane(String name, ObservableValue<Integer> leftTiles) {
        StackPane stackPane = new StackPane();
        ImageView image = new ImageView();
        // todo is that ok to do that?
        image.setId(name);
        Text text = new Text();
        text.textProperty().bind(leftTiles.map(String::valueOf));
        stackPane.getChildren().addAll(image, text);
        return stackPane;
    }

}
