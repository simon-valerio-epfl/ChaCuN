package ch.epfl.chacun.gui;

import ch.epfl.chacun.Base32;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class represents the graphical representation
 * of a node containing the last actions of the game state and
 * a field where one may insert a new action
 *
 * @author Valerio De Santis (373247)
 * @author Simon Lefort (371918)
 */
public final class ActionUI {
    /**
     * The maximum number of actions to represent on the screen
     */
    private static final int NUMBER_OF_ACTIONS = 4;

    /**
     * This is a utility class, it's not instantiable
     */
    private ActionUI() {
    }

    /**
     * Creates the node containing the last actions of the game state and
     * a field where one may insert a new action
     *
     * @param actionsO the observable list of all actions since the start of the game
     * @param handler  an event handler whose method has to be applied when the player
     *                 inserts a new action in the field
     * @return a node containing the last actions of the game state and a field where one may insert a new action
     */
    public static Node create(ObservableValue<List<String>> actionsO, Consumer<String> handler) {

        Text text = new Text();
        text.textProperty().bind(actionsO.map(ActionUI::actionsTextRepresentation));

        TextField textField = new TextField();
        textField.setId("action-field");
        textField.setTextFormatter(new TextFormatter<>(change -> {
            change.setText(cleanInput(change.getText()));
            return change;
        }));
        textField.setOnAction(_ -> {
            handler.accept(textField.getText());
            textField.clear();
        });

        HBox hbox = new HBox(text, textField);
        hbox.setId("actions");
        hbox.getStylesheets().add("actions.css");

        return hbox;
    }

    /**
     * Associates the last actions to show to their index from the beginning of the list
     *
     * @param actions the list of actions, whose last elements have to be shown
     * @return the textual representation of the last indexed actions
     */
    private static String actionsTextRepresentation(List<String> actions) {
        int actionSize = actions.size();
        int actionIdxFirst = Math.max(0, actionSize - NUMBER_OF_ACTIONS);
        return IntStream.range(actionIdxFirst, actionSize)
                .mapToObj(i -> STR."\{i + 1}:\{actions.get(i)}")
                .collect(Collectors.joining(", "));
    }

    /**
     * Converts a string to a standard format which is valid for the actions' graphical representation
     *
     * @param input the string to format
     * @return the valid formatted string
     */
    private static String cleanInput(String input) {
        StringBuilder cleaned = new StringBuilder();
        input.toUpperCase().chars()
                .filter(character -> Base32.ALPHABET.indexOf(character) != -1)
                .forEach(character -> cleaned.append((char) character));
        return cleaned.toString();

    }

}
