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
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class MessageBoardUI {

    private MessageBoardUI() {}

    public static Node create(ObservableValue<List<MessageBoard.Message>> messagesO, ObjectProperty<Set<Integer>> tileIds) {

        VBox vBox = new VBox();
        vBox.setId("message-board");
        vBox.getStyleClass().add("message-board");

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(vBox);

        BiConsumer<Integer, List<MessageBoard.Message>> addMessages = (previousMessageCount, messages) -> {
            messages.stream()
                    .skip(previousMessageCount)
                    .forEach(message -> {

                        Text text = new Text(message.text());
                        text.setWrappingWidth(ImageLoader.LARGE_TILE_FIT_SIZE);
                        text.setOnMouseEntered(_ -> tileIds.setValue(message.tileIds()));
                        text.setOnMouseExited(_ -> tileIds.setValue(Set.of()));

                        vBox.getChildren().add(text);

                    });

            Platform.runLater(() -> scrollPane.setVvalue(1));
        };

        // todo should we initialize before the addListener?
        messagesO.addListener((_, oldValue, newValue) -> addMessages.accept(vBox.getChildren().size(), newValue));
        addMessages.accept(0, messagesO.getValue());

        return scrollPane;
    }

}
