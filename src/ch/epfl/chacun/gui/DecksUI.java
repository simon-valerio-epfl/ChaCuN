package ch.epfl.chacun.gui;

import ch.epfl.chacun.Occupant;
import ch.epfl.chacun.Tile;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public final class DecksUI {

    private DecksUI() {}

    public static Node create(
            ObservableValue<Tile> tileO,
            ObservableValue<Integer> leftNormalTilesO,
            ObservableValue<Integer> leftMenhirTilesO,
            Consumer<Occupant> onOccupantClick
    ) {

        VBox vBox = new VBox();
        vBox.getStyleClass().add("decks");
        vBox.setId("decks");

        return null;
    }

}
