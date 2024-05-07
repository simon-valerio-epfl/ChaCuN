package ch.epfl.chacun;

import ch.epfl.chacun.gui.*;
import ch.epfl.chacun.net.WSManager;
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

    private void saveStateAndDispatch(
            ActionEncoder.StateAction stateAction,
            SimpleObjectProperty<GameState> gameStateO,
            SimpleObjectProperty<List<String>> actionsO,
            WSManager wsManager
    ) {
        saveState(stateAction, gameStateO, actionsO);
        wsManager.sendAction(stateAction.action());
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
    public void start(Stage primaryStage) throws InterruptedException {

        String gameName = "androzGame";

        WSManager wsManager = new WSManager(
            gameName,
            "Androz" + new Random().nextInt(1000)
        );

        TileDecks tileDecks = getShuffledTileDecks((long) gameName.hashCode());

        SimpleObjectProperty<TextMaker> textMakerO = new SimpleObjectProperty<>(new TextMakerFr(Map.of()));

        GameState gameState = GameState.initial(List.of(), tileDecks, textMakerO.getValue());
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
                switch (gState.nextAction()) {
                    case GameState.Action.OCCUPY_TILE -> textMakerO.getValue().clickToOccupy();
                    case GameState.Action.RETAKE_PAWN -> textMakerO.getValue().clickToUnoccupy();
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
                saveStateAndDispatch(stateAction, gameStateO, actionsO, wsManager);
            } else if (currentGameState.nextAction() == GameState.Action.RETAKE_PAWN) {
                // todo check owner stuff etc
                ActionEncoder.StateAction stateAction = ActionEncoder.withOccupantRemoved(currentGameState, occupant);
                saveStateAndDispatch(stateAction, gameStateO, actionsO, wsManager);
            }
        };

        Consumer<String> onEnteredAction = action -> {
            ActionEncoder.StateAction newSt = ActionEncoder.decodeAndApply(gameStateO.getValue(), action);
            if (newSt != null) saveState(newSt, gameStateO, actionsO);
        };

        Node playersNode = PlayersUI.create(gameStateO, textMakerO);
        Node messagesNode = MessageBoardUI.create(observableMessagesO, highlightedTilesO);
        Node decksNode = DecksUI.create(tileToPlaceO, leftNormalTilesO, leftMenhirTilesO, textToDisplayO, onOccupantClick);
        Node actionsNode = ActionsUI.create(actionsO, onEnteredAction);

        SimpleObjectProperty<Rotation> nextRotationO = new SimpleObjectProperty<>(Rotation.NONE);
        Consumer<Rotation> onRotationClick = r -> {
            nextRotationO.setValue(nextRotationO.getValue().add(r));
        };

        Consumer<Pos> onPosClick = pos -> {
            GameState currentGameState = gameStateO.getValue();
            if (currentGameState.nextAction() != GameState.Action.PLACE_TILE) return;
            Tile tileToPlace = currentGameState.tileToPlace();
            PlacedTile placedTile = new PlacedTile(
                    tileToPlace, currentGameState.currentPlayer(),
                    nextRotationO.getValue(), pos
            );
            if (!currentGameState.board().canAddTile(placedTile)) return;
            ActionEncoder.StateAction stateAction = ActionEncoder.withPlacedTile(currentGameState, placedTile);
            saveStateAndDispatch(stateAction, gameStateO, actionsO, wsManager);
        };

        ObservableValue<Set<Occupant>> visibleOccupants = gameStateO.map(gState -> {
            Set<Occupant> occupants = new HashSet<>(gState.board().occupants());
            if (gState.nextAction() == GameState.Action.OCCUPY_TILE) {
                occupants.addAll(gState.lastTilePotentialOccupants());
            }
            return occupants;
        });

        Node boardNode = BoardUI.create(
                Board.REACH, gameStateO, nextRotationO, visibleOccupants, highlightedTilesO,
                // consumers
                onRotationClick, onPosClick, onOccupantClick
        );

        List<String> existingPlayers = new ArrayList<>();
        wsManager.connect(onEnteredAction, (playerName, isMyself) -> {
            if (existingPlayers.contains(playerName)) return;
            int playersSize = gameStateO.getValue().players().size() + 1;
            existingPlayers.add(playerName);
            List<PlayerColor> playerColors = PlayerColor.ALL.subList(0, playersSize);
            Map<PlayerColor, String> playersNames = new TreeMap<>();
            for (int i = 0; i < playersSize; i++) {
                playersNames.put(playerColors.get(i), i == playersSize - 1 ? playerName : existingPlayers.get(i));
            }
            textMakerO.setValue(new TextMakerFr(playersNames));
            gameStateO.setValue(gameStateO.getValue().withNewPlayers(playerColors, textMakerO.getValue()));
            if (!isMyself) {
                wsManager.sayHello();
            }
        });

        wsManager.sayHello();

        // actions and decks border pane
        VBox actionsAndDecksBox = new VBox();
        actionsAndDecksBox.getChildren().addAll(actionsNode, decksNode);

        // side border pane
        BorderPane sideBorderPane = new BorderPane();
        sideBorderPane.setCenter(messagesNode);
        sideBorderPane.setTop(playersNode);
        sideBorderPane.setBottom(actionsAndDecksBox);

        // main border pane
        BorderPane mainBorderPane = new BorderPane();
        mainBorderPane.setCenter(boardNode);
        mainBorderPane.setRight(sideBorderPane);

        primaryStage.setWidth(1440);
        primaryStage.setHeight(1080);

        primaryStage.setScene(new Scene(mainBorderPane));
        primaryStage.setTitle("ChaCuN");
        primaryStage.show();

        gameStateO.setValue(gameStateO.getValue().withStartingTilePlaced());

        /*BorderPane lobby = (BorderPane) LobbyUI.create();

        primaryStage.setWidth(1440);
        primaryStage.setHeight(1080);

        primaryStage.setScene(new Scene(lobby));
        primaryStage.setTitle("ChaCuN");
        primaryStage.show();*/
    }
}
