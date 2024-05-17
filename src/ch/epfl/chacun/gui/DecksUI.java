package ch.epfl.chacun.gui;

import ch.epfl.chacun.Occupant;
import ch.epfl.chacun.Tile;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.function.Consumer;

/**
 * This class represents the graphical representation of the updating tile decks of a ChaCuN game
 *
 * @author Valerio De Santis (373247)
 * @author Simon Lefort (371918)
 */
public final class DecksUI {
    private final static double WRAPPING_WIDTH = 0.8;

    /**
     * This is a utility class, therefore it is not instantiable
     */
    private DecksUI() {
    }

    /**
     * @param tileO            the updating tile to place
     * @param leftNormalTilesO the updating number of normal tiles left in the deck
     * @param leftMenhirTilesO the updating number of menhir tiles left in the deck
     * @param textToDisplay    the text to display when the player can place or retake an occupant,
     *                         to let him pass on this action
     * @param onOccupantClick  the action to do when the player does not want to place or retake an occupant,
     *                         clicking on the text
     * @return a node representing the decks of the game, with the next tile to place and
     * *                        the number of tiles left for each kind
     */
    public static Node create(
            ObservableValue<Tile> tileO,
            ObservableValue<Integer> leftNormalTilesO,
            ObservableValue<Integer> leftMenhirTilesO,
            ObservableValue<String> textToDisplay,
            Consumer<Occupant> onOccupantClick
    ) {


        // ImageView of the next tile to place, which has to be shown in large size
        ImageView view = new ImageView();
        // we bind the graphical view of the tile to place to the tile itself
        view.imageProperty().bind(tileO.map(tile -> ImageLoader.largeImageForTile(tile.id())));
        view.setFitHeight(ImageLoader.LARGE_TILE_FIT_SIZE);
        view.setFitWidth(ImageLoader.LARGE_TILE_FIT_SIZE);

        // Text, occupy tile (only visible if textToDisplay is not empty,
        // meaning that the player does not want to do some action)
        Text text = new Text();
        text.textProperty().bind(textToDisplay);
        // we make the text visible only when the message it contains is not empty
        text.visibleProperty().bind(text.textProperty().isNotEmpty());
        text.setWrappingWidth(WRAPPING_WIDTH * ImageLoader.LARGE_TILE_FIT_SIZE);

        // Here we handle the next tile to place
        StackPane stackPane = new StackPane(view, text);
        stackPane.setId("next-tile");
        // if the player clicks on the text, it means that he does not want to place or retake an occupant
        stackPane.setOnMouseClicked(_ -> onOccupantClick.accept(null));

        // here we handle the decks containing the remaining cards
        Node normalNode = getDeckNode(Tile.Kind.NORMAL, leftNormalTilesO);
        Node menhirNode = getDeckNode(Tile.Kind.MENHIR, leftMenhirTilesO);
        HBox hBox = new HBox(normalNode, menhirNode);
        hBox.setAlignment(Pos.CENTER);

        VBox vBox = new VBox(hBox, stackPane);
        vBox.getStylesheets().add("decks.css");
        hBox.setId("decks");

        return vBox;
    }

    /**
     * This method creates a node representing a deck containing the remaining tiles
     *
     * @param kind      the kind of the deck, which represents the kind of tiles it contains
     * @param leftTiles the updating number of tiles left in the deck
     * @return a node representing the deck
     */
    private static Node getDeckNode(Tile.Kind kind, ObservableValue<Integer> leftTiles) {
        ImageView image = new ImageView();
        image.setId(kind.toString());
        image.setFitHeight(ImageLoader.NORMAL_TILE_FIT_SIZE);
        image.setFitWidth(ImageLoader.NORMAL_TILE_FIT_SIZE);
        Text text = new Text();
        text.textProperty().bind(leftTiles.map(String::valueOf));
        return new StackPane(image, text);
    }

}
