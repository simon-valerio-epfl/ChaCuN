package ch.epfl.chacun.gui;

import ch.epfl.chacun.*;
import ch.epfl.chacun.audio.SoundManager;
import ch.epfl.chacun.net.WSClient;
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

/**
 * The main class of the game, it creates the GUI and all the logic used to play the game.
 *
 * @author Valerio De Santis (373247)
 * @author Simon Lefort (371918)
 */
public final class Main extends Application {
    /**
     * The width of the window graphically showing the game, in pixels
     */
    private static final int WINDOW_WIDTH = 1440;
    /**
     * The height of the window graphically showing the game, in pixels
     */
    private static final int WINDOW_HEIGHT = 1080;
    /**
     * The title of the window
     */
    private static final String WINDOW_NAME = "ChaCuN";
    /**
     * The minimum number of players allowed in the game
     */
    private static final int MINIMUM_PLAYERS = 2;
    /**
     * The maximum number of players allowed in the game
     */
    private static final int MAXIMUM_PLAYERS = 5;

    /**
     * The main method of the game, it launches the GUI.
     *
     * @param args the arguments of the main method, the names of the players (minimum 2, maximum 5)
     *             and optionally the seed of the random generator used to shuffle the tiles
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Saves the state of the game after an action has been performed.
     *
     * @param stateAction the state action to save
     * @param gameStateO  the observable game state
     * @param actionsO    the observable list of all actions since the start of the game
     *                    <p>
     *                    Note: can also be used to dispatch the action to the server when the game is played online
     */
    private void saveState(
            ActionEncoder.StateAction stateAction,
            ObjectProperty<GameState> gameStateO,
            ObjectProperty<List<String>> actionsO,
            SoundManager soundManager
    ) {
        SoundManager.Sound sound = stateAction.gameState().nextSound();
        if (sound != null) soundManager.play(sound);
        gameStateO.setValue(stateAction.gameState());
        List<String> newActions = new ArrayList<>(actionsO.getValue());
        newActions.add(stateAction.action());
        // make the actions immutable, every object stored in an ObjectProperty should be immutable
        actionsO.setValue(Collections.unmodifiableList(newActions));
    }

    private void saveStateDispatchPlaySound(
            ActionEncoder.StateAction stateAction,
            ObjectProperty<GameState> gameStateO,
            ObjectProperty<Boolean> selfValidatedActionO,
            ObjectProperty<List<String>> actionsO,
            WSClient wsClient,
            SoundManager soundManager
    ) {
        saveState(stateAction, gameStateO, actionsO, soundManager);
        wsClient.sendAction(stateAction.action());
        selfValidatedActionO.setValue(false);
    }

    /**
     * Returns the shuffled tile decks, ensuring that the same seed always produces the same shuffled decks
     *
     * @param seed the seed of the random generator used to shuffle the tiles, if null the tiles are shuffled randomly
     * @return the shuffled tile decks
     */
    private TileDecks getShuffledTileDecks(Long seed) {
        List<Tile> tiles = new ArrayList<>(Tiles.TILES);
        if (seed != null) {
            RandomGeneratorFactory<RandomGenerator> factory = RandomGeneratorFactory.getDefault();
            RandomGenerator generator = factory.create(seed);
            Collections.shuffle(tiles, generator);
        } else Collections.shuffle(tiles);
        Map<Tile.Kind, List<Tile>> groupedTiles = tiles.stream().collect(Collectors.groupingBy(Tile::kind));
        // we make it possible to play without menhir tiles. On the other hand,
        // playing without normal tiles is not permitted
        List<Tile> menhirTiles = groupedTiles.getOrDefault(Tile.Kind.MENHIR, List.of());

        return new TileDecks(
                groupedTiles.get(Tile.Kind.START),
                groupedTiles.get(Tile.Kind.NORMAL),
                menhirTiles
        );
    }

    private SortedMap<PlayerColor, String> getPlayersMap(String playerNames) {
        String[] names = playerNames.split(",");
        SortedMap<PlayerColor, String> playersMap = new TreeMap<>();
        for (int i = 0; i < names.length; i++) {
            playersMap.put(PlayerColor.ALL.get(i), names[i]);
        }
        return playersMap;
    }

    /**
     * Starts the ChaCuN game, creating the graphical nodes representing its components
     *
     * @param primaryStage the primary stage for this application, onto which
     *                     the application scene can be set.
     *                     Applications may create other stages, if needed, but they will not be
     *                     primary stages.
     */
    @Override
    public void start(Stage primaryStage) {

        SoundManager soundManager = new SoundManager();

        Parameters parameters = getParameters();
        boolean debug = parameters.getUnnamed().contains("debug");

        int randomTwoDigits = new Random().nextInt(100);
        String localPlayerName = debug
                ? parameters.getNamed().get("player") + randomTwoDigits
                : parameters.getNamed().get("player");
        String gameName = parameters.getNamed().get("game");

        WSClient wsClient = new WSClient(gameName, localPlayerName);
        TileDecks tileDecks = getShuffledTileDecks((long) gameName.hashCode());

        ObjectProperty<SortedMap<PlayerColor, String>> playerNamesO = new SimpleObjectProperty<>(new TreeMap<>());
        ObservableValue<List<PlayerColor>> playerColorsO = playerNamesO.map(map -> new ArrayList<>(map.keySet()));
        playerNamesO.setValue(getPlayersMap(localPlayerName));

        // we create the text maker (the messages will be in French)
        // and the initial game state, as well as the observable value of the latter
        ObservableValue<TextMaker> textMakerO = playerNamesO.map(TextMakerFr::new);
        GameState gameState = GameState.initial(playerColorsO.getValue(), tileDecks, textMakerO.getValue());
        ObjectProperty<GameState> gameStateO = new SimpleObjectProperty<>(gameState);

        ObservableValue<List<MessageBoard.Message>> observableMessagesO = gameStateO.map(
                gState -> gState.messageBoard().messages()
        );

        ObservableValue<TileDecks> tileDecksO = gameStateO.map(GameState::tileDecks);
        ObservableValue<Integer> leftNormalTilesO = tileDecksO.map(tDecks -> tDecks.normalTiles().size());
        ObservableValue<Integer> leftMenhirTilesO = tileDecksO.map(tDecks -> tDecks.menhirTiles().size());

        // the text to display depends on the next action to do
        ObservableValue<String> textToDisplayO = gameStateO.map(gState -> switch (gState.nextAction()) {
            case GameState.Action.OCCUPY_TILE -> textMakerO.getValue().clickToOccupy();
            case GameState.Action.RETAKE_PAWN -> textMakerO.getValue().clickToUnoccupy();
            default -> "";
        });

        // the next tile to place
        ObservableValue<Tile> tileToPlaceO = gameStateO.map(GameState::tileToPlace);
        // the list of actions done since the beginning of the game
        ObjectProperty<List<String>> actionsO = new SimpleObjectProperty<>(List.of());

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
                        && gState.players().size() > 1
        );

        wsClient.setOnGamePlayersUpdate(newPlayerNames -> {
            playerNamesO.setValue(getPlayersMap(newPlayerNames));
            gameStateO.setValue(
                    gameStateO.getValue()
                            .withPlayers(playerColorsO.getValue())
                            .withTextMaker(textMakerO.getValue())
            );
            if (playerNamesO.getValue().size() == 1) {
                gameStateO.setValue(gameStateO.getValue().withGameChatMessage(textMakerO.getValue().emptyGame(gameName)));
            } else {
                String lastPlayerName = playerNamesO.getValue().lastEntry().getValue();
                if (!localPlayerName.equals(lastPlayerName)) {
                    gameStateO.setValue(gameStateO.getValue().withGameChatMessage(textMakerO.getValue().playerJoined(lastPlayerName)));
                }
            }
        });
        wsClient.setOnGameChatMessage((username, content) -> {
            String displayMessage = STR."\{username}: \{content}";
            gameStateO.setValue(gameStateO.getValue().withGameChatMessage(displayMessage));
        });
        ObjectProperty<Boolean> selfValidatedActionO = new SimpleObjectProperty<>(true);
        wsClient.setOnPlayerAction(action -> {
            if (!selfValidatedActionO.getValue()) {
                selfValidatedActionO.setValue(true);
            }
            else {
                ActionEncoder.StateAction stateAction = ActionEncoder.decodeAndApply(gameStateO.getValue(), action);
                if (stateAction == null) throw new IllegalStateException(STR."Invalid action: \{action}");
                else saveState(stateAction, gameStateO, actionsO, soundManager);
            }
        });
        wsClient.setOnLocalPlayerActionReject(reason -> {
            System.out.println(STR."Local player action rejected: \{reason}. Rollback needed.");
        });
        wsClient.setOnGamePlayerLeave(_ -> {
            // if the game is not started we do not care about resetting the game
            if (actionsO.getValue().isEmpty()) return;
            GameState newGameState = GameState
                    .initial(playerColorsO.getValue(), tileDecks, textMakerO.getValue())
                    .withGameChatMessage(textMakerO.getValue().gameLeaveReset());
            gameStateO.setValue(newGameState);
            actionsO.setValue(List.of());
            gameStateO.setValue(newGameState.withStartingTilePlaced());
        });
        wsClient.setOnGameEnd(_ -> {
            gameStateO.setValue(gameStateO.getValue().withGameChatMessage(textMakerO.getValue().gameEnded()));
        });

        // the consumer to handle the click on an occupant, or the click on the text to pass on the action
        Consumer<Occupant> onOccupantClick = occupant -> {
            if (!isLocalPlayerCurrentPlayerO.getValue()) return;
            GameState currentGameState = gameStateO.getValue();
            Board board = currentGameState.board();
            // if the occupant is null, it means that the player does not want to place or retake an occupant
            int tileId = occupant != null ? Zone.tileId(occupant.zoneId()) : -1;
            switch (currentGameState.nextAction()) {
                case OCCUPY_TILE -> {
                    assert board.lastPlacedTile() != null;
                    // the occupant can only be placed on the last placed tile
                    if (occupant != null && tileId != board.lastPlacedTile().id()) return;
                    // we update the state of the game and the list of actions
                    saveStateDispatchPlaySound(
                            ActionEncoder.withNewOccupant(currentGameState, occupant),
                            gameStateO,
                            selfValidatedActionO,
                            actionsO,
                            wsClient,
                            soundManager
                    );
                }
                case RETAKE_PAWN -> {
                    // the player can only retake a pawn, and he must be the one who placed it
                    if (occupant != null &&
                            (occupant.kind() != Occupant.Kind.PAWN ||
                                    (currentGameState.currentPlayer() != board.tileWithId(tileId).placer()))
                    ) return;
                    // we update the state of the game and the list of actions
                    saveStateDispatchPlaySound(
                            ActionEncoder.withOccupantRemoved(currentGameState, occupant),
                            gameStateO,
                            selfValidatedActionO,
                            actionsO,
                            wsClient,
                            soundManager
                    );
                }
                default -> {
                }
            }
        };
        // the rotation of the next tile to place
        ObjectProperty<Rotation> tileToPlaceRotationO = new SimpleObjectProperty<>(Rotation.NONE);

        // the consumer to handle the action entered by the player
        Consumer<String> onEnteredAction = action -> {
            ActionEncoder.StateAction newState = ActionEncoder.decodeAndApply(gameStateO.getValue(), action);
            // the new state is null if the action is not valid
            if (newState != null) saveStateDispatchPlaySound(
                    newState,
                    gameStateO,
                    selfValidatedActionO,
                    actionsO,
                    wsClient,
                    soundManager
            );
        };
        // the consumers to handle the rotation and the position of the next tile to place
        Consumer<Rotation> onRotationClick = newRotation -> {
            tileToPlaceRotationO.setValue(tileToPlaceRotationO.getValue().add(newRotation));
        };

        Consumer<Pos> onPosClick = pos -> {
            if (!isLocalPlayerCurrentPlayerO.getValue()) return;
            GameState currentGameState = gameStateO.getValue();
            // the player can only occupy a tile if it corresponds to the next action to do
            if (currentGameState.nextAction() != GameState.Action.PLACE_TILE) return;
            Tile tileToPlace = currentGameState.tileToPlace();
            PlacedTile placedTile = new PlacedTile(
                    tileToPlace, currentGameState.currentPlayer(),
                    tileToPlaceRotationO.getValue(), pos
            );
            if (!currentGameState.board().canAddTile(placedTile)) return;
            // if the player can place the tile, we update the state of the game and the list of actions
            saveStateDispatchPlaySound(
                    ActionEncoder.withPlacedTile(currentGameState, placedTile),
                    gameStateO,
                    selfValidatedActionO,
                    actionsO,
                    wsClient,
                    soundManager
            );
            // we reset the rotation of the following tile to place
            tileToPlaceRotationO.setValue(Rotation.NONE);
        };

        // the observable value of the visible occupants depends on the next action to do,
        // if the player has to occupy a tile, the potential occupants of the latter are also visible
        ObservableValue<Set<Occupant>> visibleOccupants = gameStateO.map(gState -> {
            Set<Occupant> placedOccupants = gState.board().occupants();
            if (gState.nextAction() == GameState.Action.OCCUPY_TILE) {
                return Stream.concat(
                        placedOccupants.stream(),
                        gState.lastTilePotentialOccupants().stream()
                ).collect(Collectors.toSet());
            } else return placedOccupants;
        });

        // the highlighted tiles are the ones to which the message the player is hovering over refers
        ObjectProperty<Set<Integer>> highlightedTilesO = new SimpleObjectProperty<>(Set.of());

        // we create the graphical node representing the board of the game
        Node boardNode = BoardUI.create(
                Board.REACH, gameStateO, tileToPlaceRotationO, visibleOccupants, highlightedTilesO, isLocalPlayerCurrentPlayerO,
                // consumers
                onRotationClick, onPosClick, onOccupantClick
        );
        // we create the graphical nodes representing the players, the messages, the decks and the actions
        Node playersNode = PlayersUI.create(gameStateO, textMakerO);
        Node messagesNode = MessageBoardUI.create(observableMessagesO, highlightedTilesO);
        Node decksNode = DecksUI.create(
                tileToPlaceO, leftNormalTilesO, leftMenhirTilesO, textToDisplayO, onOccupantClick
        );
        Node actionsNode = ActionUI.create(actionsO, onEnteredAction, isLocalPlayerCurrentPlayerO);
        Node messagesChatNode = MessageBoardChatUI.create(wsClient::sendChatMessage, textMakerO.getValue());

        // the box containing the actions and the tile decks
        VBox actionsAndDecksBox = new VBox(messagesChatNode, actionsNode, decksNode);

        // the right side border pane, containing the players, the messages and the actions and decks box
        BorderPane sideBorderPane = new BorderPane();
        sideBorderPane.setTop(playersNode);
        sideBorderPane.setCenter(messagesNode);
        sideBorderPane.setBottom(actionsAndDecksBox);

        // main border pane
        BorderPane mainBorderPane = new BorderPane(boardNode);
        mainBorderPane.setRight(sideBorderPane);

        // we set the size of the window and the title
        primaryStage.setScene(new Scene(mainBorderPane, WINDOW_WIDTH, WINDOW_HEIGHT));
        primaryStage.setTitle(WINDOW_NAME);
        primaryStage.show();

        // we update the state of the game to place the starting tile, launching the game
        gameStateO.setValue(gameStateO.getValue().withStartingTilePlaced());

        wsClient.connect();
        wsClient.joinGame();

    }
}
