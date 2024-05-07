package ch.epfl.chacun;

import ch.epfl.chacun.Tiles;

import ch.epfl.chacun.gui.*;
import com.sun.source.tree.Tree;
import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayNameGenerator;

import java.util.*;
import java.util.function.Consumer;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    private void saveState(
        ActionEncoder.StateAction stateAction,
        SimpleObjectProperty<GameState> gameStateO,
        SimpleObjectProperty<List<String>> actionsO
    ) {
        gameStateO.setValue(stateAction.gameState());
        List<String> newActions = new ArrayList<>(actionsO.getValue());
        newActions.add(stateAction.action());
        actionsO.setValue(newActions);
    }

    private TileDecks getShuffledTileDecks(Long seed) {
        List<Tile> tiles = new ArrayList<>(Tiles.TILES);
        if (seed != null) {
            RandomGeneratorFactory<RandomGenerator> factory = RandomGeneratorFactory.getDefault();
            RandomGenerator generator = factory.create(seed);
            Collections.shuffle(tiles, generator);
        } else Collections.shuffle(tiles);
        Map<Tile.Kind, List<Tile>> groupedTiles = tiles.stream().collect(Collectors.groupingBy(Tile::kind));
        return new TileDecks(
            groupedTiles.get(Tile.Kind.START),
            groupedTiles.get(Tile.Kind.NORMAL),
            groupedTiles.get(Tile.Kind.MENHIR)
        );
    }

    @Override
    public void start(Stage primaryStage) {

        Parameters parameters = getParameters();
        Map<String, String> namedParameters = parameters.getNamed();

        Long seed = namedParameters.containsKey("seed") ? Long.parseUnsignedLong(namedParameters.get("seed")) : null;
        TileDecks tileDecks = getShuffledTileDecks(seed);

        List<String> players = parameters.getUnnamed();
        int playersSize = players.size();
        Preconditions.checkArgument(playersSize >= 2 && playersSize <= 5);

        List<PlayerColor> playerColors = PlayerColor.ALL.subList(0, playersSize);
        Map<PlayerColor, String> playersNames = new TreeMap<>();
        for (int i = 0; i < playersSize; i++) {
            playersNames.put(playerColors.get(i), players.get(i));
        }

        TextMaker textMaker = new TextMakerFr(playersNames);

        GameState gameState = GameState.initial(
            playerColors,
            tileDecks,
            textMaker
        );

        SimpleObjectProperty<GameState> gameStateO = new SimpleObjectProperty<>(gameState);
        ObservableValue<List<MessageBoard.Message>> observableMessagesO = gameStateO.map(
            gState -> gState.messageBoard().messages()
        );
        ObjectProperty<Set<Integer>> highlightedTilesO = new SimpleObjectProperty<>(Set.of());
        ObservableValue<Tile> tileToPlaceO = gameStateO.map(GameState::tileToPlace);

        ObservableValue<TileDecks> tileDecksO = gameStateO.map(GameState::tileDecks);
        ObservableValue<Integer> leftNormalTilesO = tileDecksO.map(tDecks -> tDecks.normalTiles().size());
        ObservableValue<Integer> leftMenhirTilesO = tileDecksO.map(tDecks -> tDecks.menhirTiles().size());
        ObservableValue<String> textToDisplayO = gameStateO.map(gState ->
            switch (gState.nextAction()){
                case GameState.Action.OCCUPY_TILE -> textMaker.clickToOccupy();
                case GameState.Action.RETAKE_PAWN -> textMaker.clickToUnoccupy();
                default -> "";
            }
        );

        SimpleObjectProperty<List<String>> actionsO = new SimpleObjectProperty<>(List.of());

        Consumer<Occupant> onOccupantClick = occupant -> {
            // todo handle things
            GameState currentGameState = gameStateO.getValue();
            if (currentGameState.nextAction() == GameState.Action.OCCUPY_TILE) {
                assert currentGameState.board().lastPlacedTile() != null;
                int lastPlacedTileId = currentGameState.board().lastPlacedTile().id();
                if (occupant != null && Zone.tileId(occupant.zoneId()) != lastPlacedTileId) return;
                ActionEncoder.StateAction stateAction = ActionEncoder.withNewOccupant(currentGameState, occupant);
                saveState(stateAction, gameStateO, actionsO);
            }
            else if (currentGameState.nextAction() == GameState.Action.RETAKE_PAWN) {
                // todo check owner stuff etc
                ActionEncoder.StateAction stateAction = ActionEncoder.withOccupantRemoved(currentGameState, occupant);
                saveState(stateAction, gameStateO, actionsO);
            }
        };

        Consumer<String> onEnteredAction = action -> {
            ActionEncoder.StateAction newSt = ActionEncoder.decodeAndApply(gameStateO.getValue(), action);
            if (newSt != null) {
                saveState(newSt, gameStateO, actionsO);
            }
        };

        Node playersNode = PlayersUI.create(gameStateO, new TextMakerFr(playersNames));
        Node messagesNode = MessageBoardUI.create(observableMessagesO, highlightedTilesO);
        Node decksNode = DecksUI.create(tileToPlaceO, leftNormalTilesO, leftMenhirTilesO, textToDisplayO, onOccupantClick);
        Node actionsNode = ActionsUI.create(actionsO, onEnteredAction);

        SimpleObjectProperty<Rotation> nextRotationO = new SimpleObjectProperty<>(Rotation.NONE);
        Consumer<Rotation> onRotationClick = r -> {
            nextRotationO.setValue(nextRotationO.getValue().add(r));
        };

        Consumer<Pos> onPosClick = pos -> {
            // todo checker l'action ?
            // todo checker le placement ?
            GameState currentGameState = gameStateO.getValue();
            if (currentGameState.nextAction() != GameState.Action.PLACE_TILE) return;
            Tile tileToPlace = currentGameState.tileToPlace();
            PlacedTile placedTile = new PlacedTile(
                tileToPlace,
                currentGameState.currentPlayer(),
                nextRotationO.getValue(),
                pos
            );
            ActionEncoder.StateAction stateAction = ActionEncoder.withPlacedTile(currentGameState, placedTile);
            saveState(stateAction, gameStateO, actionsO);
        };

        ObservableValue<Set<Occupant>> visibleOccupants = gameStateO.map(gState -> {
            Set<Occupant> occupants = new HashSet<>(gState.board().occupants());
            if (gState.nextAction() == GameState.Action.OCCUPY_TILE) {
                occupants.addAll(gState.lastTilePotentialOccupants());
            }
            return occupants;
        });

        ScrollPane boardNode = (ScrollPane) BoardUI.create(
            Board.REACH,
            gameStateO,
            nextRotationO,
            visibleOccupants,
            highlightedTilesO,

            onRotationClick,
            onPosClick,
            onOccupantClick
        );

        var sideBar = new VBox(playersNode, actionsNode, messagesNode, decksNode);
        VBox.setVgrow(messagesNode, Priority.ALWAYS);

        boardNode.setFitToHeight(true);
        boardNode.setFitToWidth(true);
        boardNode.setMinWidth(720);
        boardNode.maxHeightProperty().bind(primaryStage.heightProperty());
        boardNode.maxWidthProperty().bind(primaryStage.widthProperty().map(w -> w.doubleValue() - sideBar.getWidth()));
        boardNode.setHvalue(.5);
        boardNode.setVvalue(.5);

        var rootNode = new HBox(boardNode, sideBar);
        HBox.setHgrow(boardNode, Priority.ALWAYS);
        primaryStage.setScene(new Scene(rootNode));
        primaryStage.setTitle("ChaCuN");
        primaryStage.show();

        gameStateO.setValue(gameStateO.getValue().withStartingTilePlaced());

    }
}
