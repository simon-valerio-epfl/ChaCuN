package ch.epfl.chacun.gui;

import ch.epfl.chacun.*;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.*;
import java.util.function.Consumer;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Main extends Application {

    private final static int WINDOW_WIDTH = 1440;
    private final static int WINDOW_HEIGHT = 1080;
    private final static String WINDOW_NAME = "ChaCuN";
    private final static int MINIMUM_PLAYERS = 2;
    private final static int MAXIMUM_PLAYERS = 5;

    public static void main(String[] args) {
        launch(args);
    }

    private void saveState(
            ActionEncoder.StateAction stateAction,
            ObjectProperty<GameState> gameStateO,
            ObjectProperty<List<String>> actionsO
    ) {
        gameStateO.setValue(stateAction.gameState());
        List<String> newActions = new ArrayList<>(actionsO.getValue());
        newActions.add(stateAction.action());
        // make the actions immutable, every object stored in an ObjectProperty should be immutable
        actionsO.setValue(Collections.unmodifiableList(newActions));
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
        Preconditions.checkArgument(playersSize >= MINIMUM_PLAYERS && playersSize <= MAXIMUM_PLAYERS);

        List<PlayerColor> playerColors = PlayerColor.ALL.subList(0, playersSize);
        Map<PlayerColor, String> playersNames = new TreeMap<>();
        for (int i = 0; i < playersSize; i++) {
            playersNames.put(playerColors.get(i), players.get(i));
        }

        TextMaker textMaker = new TextMakerFr(playersNames);
        GameState gameState = GameState.initial(playerColors, tileDecks, textMaker);
        ObjectProperty<GameState> gameStateO = new SimpleObjectProperty<>(gameState);

        ObservableValue<List<MessageBoard.Message>> observableMessagesO = gameStateO.map(
                gState -> gState.messageBoard().messages()
        );
        ObjectProperty<Set<Integer>> highlightedTilesO = new SimpleObjectProperty<>(Set.of());
        ObservableValue<Tile> tileToPlaceO = gameStateO.map(GameState::tileToPlace);

        ObservableValue<TileDecks> tileDecksO = gameStateO.map(GameState::tileDecks);
        ObservableValue<Integer> leftNormalTilesO = tileDecksO.map(tDecks -> tDecks.normalTiles().size());
        ObservableValue<Integer> leftMenhirTilesO = tileDecksO.map(tDecks -> tDecks.menhirTiles().size());
        ObservableValue<String> textToDisplayO = gameStateO.map(gState -> switch (gState.nextAction()) {
                case GameState.Action.OCCUPY_TILE -> textMaker.clickToOccupy();
                case GameState.Action.RETAKE_PAWN -> textMaker.clickToUnoccupy();
                default -> "";
            });

        ObjectProperty<Rotation> tileToPlaceRotationO = new SimpleObjectProperty<>(Rotation.NONE);
        ObjectProperty<List<String>> actionsO = new SimpleObjectProperty<>(List.of());

        Consumer<Occupant> onOccupantClick = occupant -> {
            GameState currentGameState = gameStateO.getValue();
            Board board = currentGameState.board();
            int tileId = occupant != null ? Zone.tileId(occupant.zoneId()) : -1;
            switch (currentGameState.nextAction()) {
                case OCCUPY_TILE -> {
                    assert board.lastPlacedTile() != null;
                    if (occupant != null && tileId != board.lastPlacedTile().id()) return;
                    saveState(ActionEncoder.withNewOccupant(currentGameState, occupant), gameStateO, actionsO);
                }
                case RETAKE_PAWN -> {
                    if (occupant != null &&
                            (occupant.kind() != Occupant.Kind.PAWN ||
                                    (currentGameState.currentPlayer() != board.tileWithId(tileId).placer()))
                    ) return;
                    saveState(ActionEncoder.withOccupantRemoved(currentGameState, occupant), gameStateO, actionsO);
                }
            }
        };

        Consumer<String> onEnteredAction = action -> {
            ActionEncoder.StateAction newState = ActionEncoder.decodeAndApply(gameStateO.getValue(), action);
            if (newState != null) saveState(newState, gameStateO, actionsO);
        };

        Consumer<Rotation> onRotationClick = newRotation -> {
            tileToPlaceRotationO.setValue(tileToPlaceRotationO.getValue().add(newRotation));
        };

        Consumer<Pos> onPosClick = pos -> {
            GameState currentGameState = gameStateO.getValue();
            if (currentGameState.nextAction() != GameState.Action.PLACE_TILE) return;
            Tile tileToPlace = currentGameState.tileToPlace();
            PlacedTile placedTile = new PlacedTile(
                    tileToPlace, currentGameState.currentPlayer(),
                    tileToPlaceRotationO.getValue(), pos
            );
            if (!currentGameState.board().canAddTile(placedTile)) return;
            saveState(ActionEncoder.withPlacedTile(currentGameState, placedTile), gameStateO, actionsO);
            tileToPlaceRotationO.setValue(Rotation.NONE);
        };

        ObservableValue<Set<Occupant>> visibleOccupants = gameStateO.map(gState -> {
            if (gState.nextAction() == GameState.Action.OCCUPY_TILE) {
                return Stream.concat(
                        gState.board().occupants().stream(),
                        gState.lastTilePotentialOccupants().stream()
                ).collect(Collectors.toSet());
            } else return gState.board().occupants();
        });

        Node boardNode = BoardUI.create(
                Board.REACH, gameStateO, tileToPlaceRotationO, visibleOccupants, highlightedTilesO,
                // consumers
                onRotationClick, onPosClick, onOccupantClick
        );
        Node playersNode = PlayersUI.create(gameStateO, new TextMakerFr(playersNames));
        Node messagesNode = MessageBoardUI.create(observableMessagesO, highlightedTilesO);
        Node decksNode = DecksUI.create(
                tileToPlaceO, leftNormalTilesO, leftMenhirTilesO, textToDisplayO, onOccupantClick
        );
        Node actionsNode = ActionsUI.create(actionsO, onEnteredAction);

        // actions and decks border pane
        VBox actionsAndDecksBox = new VBox(actionsNode, decksNode);

        // side border pane
        BorderPane sideBorderPane = new BorderPane();
        sideBorderPane.setTop(playersNode);
        sideBorderPane.setCenter(messagesNode);
        sideBorderPane.setBottom(actionsAndDecksBox);

        // main border pane
        BorderPane mainBorderPane = new BorderPane();
        mainBorderPane.setCenter(boardNode);
        mainBorderPane.setRight(sideBorderPane);

        primaryStage.setWidth(WINDOW_WIDTH);
        primaryStage.setHeight(WINDOW_HEIGHT);

        primaryStage.setScene(new Scene(mainBorderPane));
        primaryStage.setTitle(WINDOW_NAME);
        primaryStage.show();

        gameStateO.setValue(gameStateO.getValue().withStartingTilePlaced());

    }
}
