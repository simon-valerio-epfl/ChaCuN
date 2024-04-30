package ch.epfl.chacun.gui;

import ch.epfl.chacun.Base32;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Consumer;

public class ActionsUI {
    private ActionsUI(){}
    private final static int NUMBER_OF_ACTIONS = 4;

    public static Node create(ObservableValue<List<String>> actionsO, Consumer<String> handler){
        HBox hbox = new HBox();
        hbox.setId("actions");
        hbox.getStylesheets().add("actions.css");

        Text text = new Text();
        text.textProperty().bind(actionsO.map(actions -> actions.isEmpty() ? "" : textRepresentation(actions)));

        TextField textField = new TextField();
        textField.setTextFormatter(new TextFormatter<>(change -> {
            change.setText(cleanupInput(change.getText()));
            return change;
        }));
        textField.onKeyPressedProperty().setValue(event -> {
            if (Objects.equals(event.getCharacter(), "ENTER")) {
                handler.accept(textField.getText());
                textField.setText("");
            }
        });
        return hbox;
    }

    private static String cleanupInput(String input) {
        return input.toUpperCase()
            .chars()
            .filter(c -> Base32.isValid(String.valueOf(c)))
            .toString();
    }

    private static String textRepresentation(List<String> actions){
        StringJoiner sj = new StringJoiner(", ");
        List<String> lastActions = actions.subList(actions.size()-NUMBER_OF_ACTIONS, actions.size());
        lastActions.forEach(a -> sj.add(STR."\{actions.indexOf(a)}:\{a}"));
        return sj.toString();
    }

}
