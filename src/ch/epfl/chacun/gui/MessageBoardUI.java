package ch.epfl.chacun.gui;

import ch.epfl.chacun.MessageBoard;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class MessageBoardUI {

    private MessageBoardUI() {}

    public static Node create(ObservableValue<List<MessageBoard.Message>> messagesO, ObjectProperty<Set<Integer>> tileIds) {

        VBox vBox = new VBox();
        vBox.setId("message-board");
        vBox.getStyleClass().add("message-board");

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(vBox);

        Platform.runLater(() -> scrollPane.setVvalue(1));

        messagesO.addListener((observable, oldValue, newValue) -> {

            // append new messages to the message board
            newValue.stream()
                .skip(oldValue.size())
                .forEach(message -> {

                    Text text = new Text(message.text());
                    text.setWrappingWidth(ImageLoader.LARGE_TILE_FIT_SIZE);
                    text.setOnMouseEntered(e -> tileIds.setValue(message.tileIds()));
                    text.setOnMouseExited(e -> tileIds.setValue(Set.of()));

                    vBox.getChildren().add(text);

                });
        });

        return scrollPane;
    }

}
