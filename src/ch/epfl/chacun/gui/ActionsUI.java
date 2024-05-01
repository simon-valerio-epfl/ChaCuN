package ch.epfl.chacun.gui;

import ch.epfl.chacun.Base32;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import java.util.List;
import java.util.StringJoiner;
import java.util.function.Consumer;

/**
 * This class represents the graphical representation
 * of a node containing the last actions of the game state and
 * a field where one may insert a new action
 */
public final class ActionsUI {
    /**
     * This is a utility class, it's not instantiable
     */
    private ActionsUI() {}

    /**
     * The number of actions to represent on the screen
     */
    private final static int NUMBER_OF_ACTIONS = 4;

    /**
     * Creates the node containing the last actions of the game state and
     * a field where one may insert a new action
     * @param actionsO the observable list of all actions since the start of the game
     * @param handler an event handler whose method has to be applied when the player
     *                inserts a new action in the field
     * @return a node containing the last actions of the game state and a field where one may insert a new action
     */
    public static Node create(ObservableValue<List<String>> actionsO, Consumer<String> handler) {
        HBox hbox = new HBox();
        hbox.setId("actions");
        hbox.getStylesheets().add("actions.css");

        Text text = new Text();
        text.textProperty().bind(actionsO.map(ActionsUI::textRepresentation));

        TextField textField = new TextField();
        textField.setTextFormatter(new TextFormatter<>(change -> {
            change.setText(cleanupInput(change.getText()));
            return change;
        }));
        textField.setOnAction(_ -> {
            handler.accept(textField.getText());
            textField.clear();
        });

        hbox.getChildren().addAll(text, textField);

        return hbox;
    }

    /**
     * Converts a string to a standard format which is valid for the actions' graphical representation
     * @param input the string to format
     * @return the valid formatted string
     */
    private static String cleanupInput(String input) {
        return input.toUpperCase()
            .chars()
            .filter(c -> Base32.isValid(Character.toString(c)))
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString();
    }

    /**
     * Associates the last actions to show to their index from the beginning of the list
     * @param actions the list of actions, whose last elements have to be shown
     * @return the textual representation of the last indexed actions
     */
    private static String textRepresentation(List<String> actions) {
        StringJoiner sj = new StringJoiner(", ");
        int startingIdx = Math.max(0, actions.size() - NUMBER_OF_ACTIONS);
        List<String> lastActions = actions.subList(startingIdx, actions.size());
        for (int i = 0; i < lastActions.size(); i++) {
            sj.add(STR."\{startingIdx + i}:\{lastActions.get(i)}");
        }
        return sj.toString();
    }

}
