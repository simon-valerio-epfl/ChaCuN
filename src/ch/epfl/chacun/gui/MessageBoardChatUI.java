package ch.epfl.chacun.gui;

import ch.epfl.chacun.MessageBoard;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public final class MessageBoardChatUI {
    private MessageBoardChatUI() {
    }

    public static Node create(Consumer<String> sendMessage) {
        HBox hBox = new HBox();

        hBox.setAlignment(Pos.CENTER);

        TextField textField = new TextField();
        textField.setPromptText("Type your message here");

        textField.setOnAction(e -> {
            sendMessage.accept(textField.getText());
            textField.clear();
        });

        hBox.getChildren().add(textField);

        return hBox;
    }

}
