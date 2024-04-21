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

import java.util.function.BiConsumer;
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

        StackPane stackPane = new StackPane();
        stackPane.setId("next-tile");

        // ImageView, tile to place
        ImageView view = new ImageView();
        view.setImage(ImageLoader.largeImageForTile(tileO.getValue().id()));
        view.setFitHeight(ImageLoader.LARGE_TILE_FIT_SIZE);
        view.setFitWidth(ImageLoader.LARGE_TILE_FIT_SIZE);
        // Text, occupy tile (only visible if textToDisplay is not empty)
        Text text = new Text();
        text.setOnMouseClicked(_ -> onOccupantClick.accept(null));
        text.textProperty().bind(textToDisplay);
        text.visibleProperty().bind(text.textProperty().isNotEmpty());
        text.setWrappingWidth(0.8 * ImageLoader.LARGE_TILE_FIT_SIZE);
        stackPane.getChildren().setAll(view, text);

        tileO.addListener((_, _, newValue) -> view.setImage(ImageLoader.largeImageForTile(newValue.id())));

        Node menhirNode = getDeckNode("MENHIR", leftMenhirTilesO);
        Node normalNode = getDeckNode("NORMAL", leftNormalTilesO);
        HBox hBox = new HBox(menhirNode, normalNode);

        VBox vBox = new VBox(hBox, stackPane);
        vBox.getStylesheets().add("/decks.css");
        hBox.setId("decks");

        return vBox;
    }

    private static Node getDeckNode(String name, ObservableValue<Integer> leftTiles) {
        ImageView image = new ImageView();
        image.setId(name);
        image.setFitHeight(ImageLoader.NORMAL_TILE_FIT_SIZE);
        image.setFitWidth(ImageLoader.NORMAL_TILE_FIT_SIZE);
        Text text = new Text();
        text.textProperty().bind(leftTiles.map(String::valueOf));
        return new StackPane(image, text);
    }

}
