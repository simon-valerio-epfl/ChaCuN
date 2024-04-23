package ch.epfl.chacun.gui;

import ch.epfl.chacun.MessageBoard;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

public final class MessageBoardUI {
    /**
     * This is a utilitarian class, therefore it is not instantiable
     */
    private MessageBoardUI() {}

    /**
     * Creates a node containing the graphical representation of the message board,
     * with the property of highlighting the tiles related to a message whenever the mouse passes on it.
     * @param messagesO the list of updating messages to show
     * @param tileIds the set of indexes of the tiles to highlight because they're related to some message
     * @return a node containing the graphical representation of the message board, with the
     * property of highlighting the tiles related to a message whenever the mouse passes on it
     */
    public static Node create(
            ObservableValue<List<MessageBoard.Message>> messagesO,
            ObjectProperty<Set<Integer>> tileIds
    ) {
        //todo, normally we have to set the id and the style of the scrollPane, not of the vBox
        VBox vBox = new VBox();
        //vBox.getStylesheets().add("message-board.css");
        //vBox.setId("message-board");

        ScrollPane scrollPane = new ScrollPane(vBox);
        scrollPane.getStylesheets().add("message-board.css");
        scrollPane.setId("message-board");

        messagesO.addListener((_, oldValue, newValue) -> {
            newValue.stream()
                    // we know that the new message board will not modify any pre-existing message
                    .skip(oldValue.size())
                    .forEach(message -> {
                        Text text = new Text(message.text());
                        text.setWrappingWidth(ImageLoader.LARGE_TILE_FIT_SIZE);
                        // we bind the tiles related to a message whenever the mouse passes on it
                        text.setOnMouseEntered(_ -> tileIds.setValue(message.tileIds()));
                        // whenever the mouse exits a message, the highlighted tiles are reset
                        // it will work when we pass from a message to another one because
                        // the exiting event happens before the entering one
                        text.setOnMouseExited(_ -> tileIds.setValue(Set.of()));

                        vBox.getChildren().add(text);
                    });

            Platform.runLater(() -> scrollPane.setVvalue(1));
        });

        return scrollPane;
    }

}
