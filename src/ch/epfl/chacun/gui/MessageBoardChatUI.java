package ch.epfl.chacun.gui;

import ch.epfl.chacun.TextMaker;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.util.function.Consumer;

/**
 * Represents the graphical representation of a chat input
 *
 * @author Valerio De Santis (373247)
 * @author Simon Lefort (371918)
 */
public final class MessageBoardChatUI {
    private MessageBoardChatUI() {
    }

    /**
     * Creates the node containing the chat input
     *
     * @param sendMessage the consumer that receives a message
     * @param textMaker   the text maker used to generate the text for the chat input
     * @return a graphical node containing the chat input
     */
    public static Node create(Consumer<String> sendMessage, TextMaker textMaker) {
        HBox hBox = new HBox();

        hBox.setAlignment(Pos.CENTER);

        TextField textField = new TextField();
        textField.setPromptText(textMaker.enterChatMessage());

        textField.setOnAction(e -> {
            if (textField.getText().isBlank()) return;
            sendMessage.accept(textField.getText());
            textField.clear();
        });

        hBox.getChildren().add(textField);

        return hBox;
    }

}