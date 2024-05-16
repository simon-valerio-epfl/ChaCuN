package ch.epfl.chacun.gui;

import ch.epfl.chacun.*;
import ch.epfl.chacun.net.WSClient;
import ch.epfl.chacun.sound.SoundManager;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.util.*;
import java.util.function.Consumer;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import java.util.stream.Collectors;

public final class Main extends Application {

    private final static int WINDOW_WIDTH = 1440;
    private final static int WINDOW_HEIGHT = 1080;

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

    private void saveStateDispatchPlaySound(
            ActionEncoder.StateAction stateAction,
            ObjectProperty<GameState> gameStateO,
            ObjectProperty<List<String>> actionsO,
            WSClient wsClient,
            SoundManager soundManager
    ) {
        SoundManager.Sound sound = stateAction.gameState().nextSound();
        saveState(stateAction, gameStateO, actionsO);
        wsClient.sendAction(stateAction.action());
        if (sound != null) soundManager.play(sound);
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

    private Map<PlayerColor, String> getPlayersMap(String playerNames) {
        String[] names = playerNames.split(",");
        Map<PlayerColor, String> playersMap = new TreeMap<>();
        for (int i = 0; i < names.length; i++) {
            playersMap.put(PlayerColor.ALL.get(i), names[i]);
        }
        return playersMap;
    }

    @Override
    public void start(Stage primaryStage) {

        Text title = new Text("ChaCuN Lobby");
        title.getStyleClass().add("title");

        VBox vBox = new VBox(title);
        vBox.getStyleClass().add("container");
        for (int i = 0; i < 5; i++) {
            TextField field = new TextField("Pedro");
            field.setDisable(true);
            Circle circle = new Circle(15);
            circle.setFill(Color.RED);
            HBox hBox = new HBox(field, circle);
            hBox.setSpacing(15);
            hBox.setAlignment(Pos.CENTER);
            vBox.getChildren().add(hBox);
            hBox.setAlignment(Pos.CENTER_LEFT);
        }
        vBox.getStylesheets().add("lobby.css");

        Text seedTitle  = new Text("Seed");
        TextField seedField = new TextField();
        VBox seedBox = new VBox(seedTitle, seedField);
        Text gameTitle  = new Text("ID Partie");
        TextField gameField = new TextField("123456");
        gameField.setDisable(true);
        VBox gameBox = new VBox(gameTitle, gameField);

        HBox seedAndGameId = new HBox(seedBox, gameBox);
        seedAndGameId.setSpacing(15);

        Button button = new Button("Start Game");
        vBox.getChildren().addAll(seedAndGameId, button);

        /*
        SoundManager soundManager = new SoundManager();

        Parameters parameters = getParameters();
        boolean debug = parameters.getUnnamed().contains("debug");

        int randomTwoDigits = new Random().nextInt(100);
        String localPlayerName = debug
                ? parameters.getNamed().get("player") + randomTwoDigits
                : parameters.getNamed().get("player");
        String gameId = parameters.getNamed().get("game");


        WSClient wsClient = new WSClient(
                gameId,
                localPlayerName
        );

        TileDecks tileDecks = getShuffledTileDecks((long) gameId.hashCode());

        ObjectProperty<Map<PlayerColor, String>> playerNamesO = new SimpleObjectProperty<>(new TreeMap<>());
        ObservableValue<List<PlayerColor>> playerColorsO = playerNamesO.map(map -> new ArrayList<>(map.keySet()));
        playerNamesO.setValue(getPlayersMap(localPlayerName));

        ObservableValue<TextMaker> textMakerO = playerNamesO.map(TextMakerFr::new);

        GameState gameState = GameState.initial(playerColorsO.getValue(), tileDecks, textMakerO.getValue());
        ObjectProperty<GameState> gameStateO = new SimpleObjectProperty<>(gameState);
        ObjectProperty<List<String>> actionsO = new SimpleObjectProperty<>(List.of());

        wsClient.setOnGamePlayerJoin(newPlayerNames -> {
            playerNamesO.setValue(getPlayersMap(newPlayerNames));
            gameStateO.setValue(
                    gameStateO.getValue()
                            .withPlayers(playerColorsO.getValue())
                            .withTextMaker(textMakerO.getValue())
            );
        });

        wsClient.setOnGameChatMessage((username, content) -> {
            String displayMessage = STR."\{username}: \{content}";
            gameStateO.setValue(gameStateO.getValue().withGameChatMessage(displayMessage));
        });
        wsClient.setOnPlayerAction(action -> {
            ActionEncoder.StateAction stateAction = ActionEncoder.decodeAndApply(gameStateO.getValue(), action);
            if (stateAction == null) throw new IllegalStateException(STR."Invalid action: \{action}");
            else saveState(stateAction, gameStateO, actionsO);
        });

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

        ObservableValue<String> localPlayerColorO = playerNamesO.map(playerName -> {
            PlayerColor local = null;
            for (Map.Entry<PlayerColor, String> entry : playerName.entrySet()) {
                if (entry.getValue().equals(localPlayerName)) {
                    local = entry.getKey();
                    break;
                }
            }
            assert local != null;
            return local.toString();
        });

        ObservableValue<Boolean> isLocalPlayerCurrentPlayerO = gameStateO.map(
                gState -> gState.currentPlayer() == PlayerColor.valueOf(localPlayerColorO.getValue())
        );

        Consumer<Occupant> onOccupantClick = occupant -> {
            if (!isLocalPlayerCurrentPlayerO.getValue()) return;
            GameState currentGameState = gameStateO.getValue();
            Board board = currentGameState.board();
            int tileId = occupant != null ? Zone.tileId(occupant.zoneId()) : -1;
            switch (currentGameState.nextAction()) {
                case OCCUPY_TILE -> {
                    assert board.lastPlacedTile() != null;
                    if (occupant != null && tileId != board.lastPlacedTile().id()) return;
                    saveStateDispatchPlaySound(ActionEncoder.withNewOccupant(currentGameState, occupant), gameStateO, actionsO, wsClient, soundManager);
                }
                case RETAKE_PAWN -> {
                    if (
                            occupant != null &&
                                    (occupant.kind() != Occupant.Kind.PAWN || (currentGameState.currentPlayer() != board.tileWithId(tileId).placer()))
                    ) return;
                    saveStateDispatchPlaySound(ActionEncoder.withOccupantRemoved(currentGameState, occupant), gameStateO, actionsO, wsClient, soundManager);
                }
            }
        };

        Consumer<String> onEnteredAction = action -> {
            ActionEncoder.StateAction newState = ActionEncoder.decodeAndApply(gameStateO.getValue(), action);
            if (newState != null) saveStateDispatchPlaySound(newState, gameStateO, actionsO, wsClient, soundManager);
        };

        Node playersNode = PlayersUI.create(gameStateO, textMakerO);
        Node messagesNode = MessageBoardUI.create(observableMessagesO, highlightedTilesO);
        Node decksNode = DecksUI.create(tileToPlaceO, leftNormalTilesO, leftMenhirTilesO, textToDisplayO, onOccupantClick);
        Node actionsNode = ActionsUI.create(actionsO, onEnteredAction, isLocalPlayerCurrentPlayerO);
        Node messagesChatNode = MessageBoardChatUI.create(wsClient::sendMessage);

        ObjectProperty<Rotation> nextRotationO = new SimpleObjectProperty<>(Rotation.NONE);
        Consumer<Rotation> onRotationClick = r -> {
            nextRotationO.setValue(nextRotationO.getValue().add(r));
        };

        Consumer<Pos> onPosClick = pos -> {
            if (!isLocalPlayerCurrentPlayerO.getValue()) return;
            GameState currentGameState = gameStateO.getValue();
            if (currentGameState.nextAction() != GameState.Action.PLACE_TILE) return;
            Tile tileToPlace = currentGameState.tileToPlace();
            PlacedTile placedTile = new PlacedTile(
                    tileToPlace, currentGameState.currentPlayer(),
                    nextRotationO.getValue(), pos
            );
            if (!currentGameState.board().canAddTile(placedTile)) return;
            saveStateDispatchPlaySound(ActionEncoder.withPlacedTile(currentGameState, placedTile), gameStateO, actionsO, wsClient, soundManager);
            nextRotationO.setValue(Rotation.NONE);
        };

        ObservableValue<Set<Occupant>> visibleOccupants = gameStateO.map(gState -> {
            Set<Occupant> occupants = new HashSet<>(gState.board().occupants());
            if (gState.nextAction() == GameState.Action.OCCUPY_TILE) {
                occupants.addAll(gState.lastTilePotentialOccupants());
            }
            return occupants;
        });

        Node boardNode = BoardUI.create(
                Board.REACH, gameStateO, nextRotationO, visibleOccupants, highlightedTilesO, isLocalPlayerCurrentPlayerO,
                // consumers
                onRotationClick, onPosClick, onOccupantClick
        );

        // actions and decks border pane
        VBox actionsAndDecksBox = new VBox();
        actionsAndDecksBox.getChildren().addAll(messagesChatNode, actionsNode, decksNode);

        // side border pane
        BorderPane sideBorderPane = new BorderPane();
        sideBorderPane.setCenter(messagesNode);
        sideBorderPane.setTop(playersNode);
        sideBorderPane.setBottom(actionsAndDecksBox);

        // main border pane
        BorderPane mainBorderPane = new BorderPane();
        mainBorderPane.setCenter(boardNode);
        mainBorderPane.setRight(sideBorderPane);

        primaryStage.setWidth(WINDOW_WIDTH);
        primaryStage.setHeight(WINDOW_HEIGHT);

        primaryStage.setScene(new Scene(mainBorderPane));
        primaryStage.setTitle("ChaCuN");
        primaryStage.show();

        gameStateO.setValue(gameStateO.getValue().withStartingTilePlaced());

        wsClient.connect();
         */

        primaryStage.setScene(new Scene(vBox));
        primaryStage.setTitle("ChaCuN");
        primaryStage.show();

    }
}
