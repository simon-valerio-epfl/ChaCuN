package ch.epfl.chacun;

import ch.epfl.chacun.gui.*;
import ch.epfl.chacun.tile.Tiles;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class GameDecksUI extends Application {

    public static void main(String[] args) { launch(args); }

    @Override
    public void start(Stage primaryStage) {

        var positions = Map.ofEntries(
                Map.entry(34, new Pos(-3, -1)),
                Map.entry(67, new Pos(-2, -1)),
                Map.entry(31, new Pos(-1, -1)),
                Map.entry(61, new Pos(0, -1)),
                Map.entry(62, new Pos(-3, 0)),
                Map.entry(18, new Pos(-2, 0)),
                Map.entry(51, new Pos(-1, 0)),
                Map.entry(1, new Pos(-3, 1)),
                Map.entry(3, new Pos(-2, 1)),
                Map.entry(49, new Pos(-1, 1)),
                Map.entry(55, new Pos(0, 1)));

        var occupants = Map.of(
                61, new Occupant(Occupant.Kind.PAWN, 61_0), // hunter (RED)
                55, new Occupant(Occupant.Kind.PAWN, 55_3), // fisher (BLUE)
                51, new Occupant(Occupant.Kind.PAWN, 51_1), // fisher (GREEN)
                18, new Occupant(Occupant.Kind.PAWN, 18_2), // hunter (YELLOW)
                1, new Occupant(Occupant.Kind.HUT, 1_8), // fisher's hut (RED)
                34, new Occupant(Occupant.Kind.PAWN, 34_1), // hunter (BLUE)
                3, new Occupant(Occupant.Kind.PAWN, 3_5), // fisher (PURPLE)
                49, new Occupant(Occupant.Kind.PAWN, 49_2) // hunter (RED)
        );

        var unoccupyableTiles = Set.of(62);

        var normalTilesIds = List.of(61, 55, 51, 18, 62, 1, 34, 67, 31, 3, 49);
        var state = initialGameState(playerNames, normalTilesIds, List.of());
        var gameStateO = new SimpleObjectProperty<>(state);

        gameStateO.setValue(truncateDeck(gameStateO.getValue(), Tile.Kind.NORMAL, normalTilesIds.size() - 1));

        var players = gameStateO.getValue().players();
        var playersIt = Stream.generate(() -> players)
                .flatMap(Collection::stream)
                .iterator();

        var nextPlacedTile = (Function<GameState, PlacedTile>) s -> {
            var t = s.tileToPlace();
            var placer = switch (t.kind()) {
                case NORMAL -> playersIt.next();
                case MENHIR -> s.board().lastPlacedTile().placer();
                default -> throw new Error();
            };
            return new PlacedTile(t, placer, Rotation.NONE, positions.get(t.id()));
        };


        var playersNode = PlayersUI.create(gameStateO, new TextMakerFr(playerNames));
        var observableMessages = gameStateO.map(g -> g.messageBoard().messages());
        SimpleObjectProperty<Set<Integer>> highlightedTiles = new SimpleObjectProperty<>(Set.of());
        var messagesNode = MessageBoardUI.create(observableMessages, highlightedTiles);

        var tileToPlace = gameStateO.map(GameState::tileToPlace);
        var leftNormalTiles = gameStateO.map(g -> g.tileDecks().normalTiles().size());
        var leftMenhirTiles = gameStateO.map(g -> g.tileDecks().menhirTiles().size());
        var textToDisplay = gameStateO.map(g -> g.nextAction() == GameState.Action.OCCUPY_TILE ? textMakerFr.clickToOccupy() : null);
        Consumer<Occupant> onOccupantClick = (Occupant a) -> { };
        var decksNode = DecksUI.create(tileToPlace, leftNormalTiles, leftMenhirTiles, textToDisplay, onOccupantClick);
        SimpleObjectProperty<List<String>> actions = new SimpleObjectProperty<>(new ArrayList<>());
        var actionsNode = ActionsUI.create(actions, (String action) -> {
            ActionEncoder.StateAction newSt = ActionEncoder.decodeAndApply(gameStateO.getValue(), action);
            if (newSt != null) {
                gameStateO.setValue(newSt.gameState());
            }
        });

        SimpleObjectProperty<Rotation> nextRotation = new SimpleObjectProperty<>(Rotation.NONE);
        var boardNode = (ScrollPane) BoardUI.create(12, gameStateO, nextRotation, new SimpleObjectProperty<>(Set.of()), highlightedTiles, r -> {
            nextRotation.setValue(nextRotation.getValue().add(r));
        }, p -> {}, o -> {});

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
        primaryStage.setTitle("ChaCuN test");
        primaryStage.show();

        var tlS = 1;
        var tl = new Timeline();

        for (int i = 0; i < positions.size(); i++) {
            // create keyframe
            var keyFrame = new KeyFrame(javafx.util.Duration.seconds(tlS * (i + 1)), e -> {
                var placedTile = nextPlacedTile.apply(gameStateO.getValue());
                ActionEncoder.StateAction newSt = ActionEncoder.withPlacedTile(gameStateO.getValue(), placedTile);
                gameStateO.setValue(newSt.gameState());
                List<String> newActions = new ArrayList<>(actions.getValue());
                newActions.add(newSt.action());
                actions.setValue(newActions);
                if (!unoccupyableTiles.contains(placedTile.id())) {
                    ActionEncoder.StateAction newStO = ActionEncoder.withNewOccupant(gameStateO.getValue(),
                            occupants.get(placedTile.id()));
                    gameStateO.setValue(newStO.gameState());
                    List<String> newActionsO = new ArrayList<>(actions.getValue());
                    newActionsO.add(newStO.action());
                    actions.setValue(newActionsO);
                }
            });

            tl.getKeyFrames().add(keyFrame);
        }

        Platform.runLater(tl::play);
    }

    private static GameState truncateDeck(GameState state, Tile.Kind deckKind, int deckSize) {
        var decks1 = switch (state.tileDecks()) {
            case TileDecks(List<Tile> s, List<Tile> n, List<Tile> m) when deckKind == Tile.Kind.NORMAL ->
                    new TileDecks(s, n.subList(0, deckSize), m);
            case TileDecks(List<Tile> s, List<Tile> n, List<Tile> m) when deckKind == Tile.Kind.MENHIR ->
                    new TileDecks(s, n, m.subList(0, deckSize));
            default -> throw new Error("cannot truncate that deck");
        };
        return new GameState(
                state.players(),
                decks1,
                state.tileToPlace(),
                state.board(),
                state.nextAction(),
                state.messageBoard());
    }

    private static List<Tile> allTiles() {
        ArrayList<Tile> tiles = new ArrayList<>();
        {   // Tile 0
            var l1 = new Zone.Lake(/*0_*/8, 2, null);
            var z0 = new Zone.Meadow(/*0_*/0, List.of(), null);
            var z1 = new Zone.River(/*0_*/1, 0, l1);
            var z2 = new Zone.Meadow(/*0_*/2, List.of(), null);
            var z3 = new Zone.Forest(/*0_*/3, Zone.Forest.Kind.PLAIN);
            var z4 = new Zone.Forest(/*0_*/4, Zone.Forest.Kind.PLAIN);
            var sN = new TileSide.River(z0, z1, z2);
            var sE = new TileSide.Meadow(z2);
            var sS = new TileSide.Forest(z3);
            var sW = new TileSide.Forest(z4);
            tiles.add(new Tile(0, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 1
            var l1 = new Zone.Lake(1_8, 3, null);
            var z0 = new Zone.Meadow(1_0, List.of(), null);
            var z1 = new Zone.River(1_1, 0, l1);
            var z2 = new Zone.Meadow(1_2, List.of(), null);
            var z3 = new Zone.Forest(1_3, Zone.Forest.Kind.PLAIN);
            var z4 = new Zone.Meadow(1_4, List.of(), null);
            var z5 = new Zone.River(1_5, 0, l1);
            var sN = new TileSide.Meadow(z0);
            var sE = new TileSide.River(z0, z1, z2);
            var sS = new TileSide.Forest(z3);
            var sW = new TileSide.River(z4, z5, z0);
            tiles.add(new Tile(1, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 2
            var l1 = new Zone.Lake(2_8, 1, null);
            var z0 = new Zone.Meadow(2_0, List.of(), null);
            var z1 = new Zone.River(2_1, 0, l1);
            var z2 = new Zone.Meadow(2_2, List.of(), null);
            var z3 = new Zone.Forest(2_3, Zone.Forest.Kind.WITH_MENHIR);
            var sN = new TileSide.Meadow(z0);
            var sE = new TileSide.River(z0, z1, z2);
            var sS = new TileSide.Forest(z3);
            var sW = new TileSide.Forest(z3);
            tiles.add(new Tile(2, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 3
            var l1 = new Zone.Lake(3_8, 1, null);
            var z0 = new Zone.Meadow(3_0, List.of(), null);
            var z1 = new Zone.River(3_1, 0, l1);
            var z2 = new Zone.Meadow(3_2, List.of(), null);
            var z3 = new Zone.River(3_3, 0, l1);
            var z4 = new Zone.Meadow(3_4, List.of(), null);
            var z5 = new Zone.River(3_5, 0, l1);
            var sN = new TileSide.River(z0, z1, z2);
            var sE = new TileSide.River(z2, z3, z4);
            var sS = new TileSide.Meadow(z4);
            var sW = new TileSide.River(z4, z5, z0);
            tiles.add(new Tile(3, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 4
            var l1 = new Zone.Lake(4_8, 1, null);
            var z0 = new Zone.Meadow(4_0, List.of(), null);
            var z1 = new Zone.River(4_1, 0, l1);
            var a2_0 = new Animal(4_2_0, Animal.Kind.DEER);
            var z2 = new Zone.Meadow(4_2, List.of(a2_0), null);
            var z3 = new Zone.Forest(4_3, Zone.Forest.Kind.PLAIN);
            var z4 = new Zone.Meadow(4_4, List.of(), null);
            var z5 = new Zone.River(4_5, 0, l1);
            var sN = new TileSide.River(z0, z1, z2);
            var sE = new TileSide.Forest(z3);
            var sS = new TileSide.River(z4, z5, z0);
            var sW = new TileSide.Meadow(z0);
            tiles.add(new Tile(4, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 5
            var l1 = new Zone.Lake(5_8, 1, null);
            var a0_0 = new Animal(5_0_0, Animal.Kind.AUROCHS);
            var z0 = new Zone.Meadow(5_0, List.of(a0_0), null);
            var z1 = new Zone.River(5_1, 0, l1);
            var z2 = new Zone.Meadow(5_2, List.of(), null);
            var z3 = new Zone.River(5_3, 0, l1);
            var z4 = new Zone.Meadow(5_4, List.of(), null);
            var z5 = new Zone.River(5_5, 0, l1);
            var sN = new TileSide.Meadow(z0);
            var sE = new TileSide.River(z0, z1, z2);
            var sS = new TileSide.River(z2, z3, z4);
            var sW = new TileSide.River(z4, z5, z0);
            tiles.add(new Tile(5, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 6
            var l1 = new Zone.Lake(6_8, 1, null);
            var z0 = new Zone.Meadow(6_0, List.of(), null);
            var z1 = new Zone.River(6_1, 0, l1);
            var z2 = new Zone.Meadow(6_2, List.of(), null);
            var z3 = new Zone.River(6_3, 0, l1);
            var a4_0 = new Animal(6_4_0, Animal.Kind.DEER);
            var z4 = new Zone.Meadow(6_4, List.of(a4_0), null);
            var z5 = new Zone.River(6_5, 0, l1);
            var sN = new TileSide.River(z0, z1, z2);
            var sE = new TileSide.River(z2, z3, z4);
            var sS = new TileSide.Meadow(z4);
            var sW = new TileSide.River(z4, z5, z0);
            tiles.add(new Tile(6, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 7
            var l1 = new Zone.Lake(7_8, 1, null);
            var a0_0 = new Animal(7_0_0, Animal.Kind.TIGER);
            var z0 = new Zone.Meadow(7_0, List.of(a0_0), null);
            var z1 = new Zone.River(7_1, 0, l1);
            var z2 = new Zone.Meadow(7_2, List.of(), null);
            var z3 = new Zone.River(7_3, 0, l1);
            var z4 = new Zone.Meadow(7_4, List.of(), null);
            var z5 = new Zone.River(7_5, 0, l1);
            var sN = new TileSide.River(z0, z1, z2);
            var sE = new TileSide.River(z2, z3, z4);
            var sS = new TileSide.River(z4, z5, z0);
            var sW = new TileSide.Meadow(z0);
            tiles.add(new Tile(7, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 8
            var l1 = new Zone.Lake(8_8, 1, null);
            var a0_0 = new Animal(8_0_0, Animal.Kind.MAMMOTH);
            var z0 = new Zone.Meadow(8_0, List.of(a0_0), null);
            var z1 = new Zone.River(8_1, 0, l1);
            var a2_0 = new Animal(8_2_0, Animal.Kind.DEER);
            var z2 = new Zone.Meadow(8_2, List.of(a2_0), null);
            var z3 = new Zone.River(8_3, 0, l1);
            var z4 = new Zone.Meadow(8_4, List.of(), null);
            var z5 = new Zone.River(8_5, 0, l1);
            var sN = new TileSide.River(z0, z1, z2);
            var sE = new TileSide.River(z2, z3, z4);
            var sS = new TileSide.Meadow(z4);
            var sW = new TileSide.River(z4, z5, z0);
            tiles.add(new Tile(8, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 9
            var l1 = new Zone.Lake(9_8, 2, null);
            var z0 = new Zone.Meadow(9_0, List.of(), null);
            var z1 = new Zone.River(9_1, 0, l1);
            var z2 = new Zone.Meadow(9_2, List.of(), null);
            var z3 = new Zone.River(9_3, 0, l1);
            var z4 = new Zone.Meadow(9_4, List.of(), null);
            var z5 = new Zone.River(9_5, 0, l1);
            var sN = new TileSide.River(z0, z1, z2);
            var sE = new TileSide.River(z2, z3, z4);
            var sS = new TileSide.River(z4, z5, z0);
            var sW = new TileSide.Meadow(z0);
            tiles.add(new Tile(9, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 10
            var l1 = new Zone.Lake(10_8, 1, null);
            var z0 = new Zone.Meadow(10_0, List.of(), null);
            var z1 = new Zone.River(10_1, 0, l1);
            var z2 = new Zone.Meadow(10_2, List.of(), null);
            var z3 = new Zone.River(10_3, 0, l1);
            var a4_0 = new Animal(10_4_0, Animal.Kind.DEER);
            var z4 = new Zone.Meadow(10_4, List.of(a4_0), null);
            var z5 = new Zone.River(10_5, 0, l1);
            var sN = new TileSide.River(z0, z1, z2);
            var sE = new TileSide.River(z2, z3, z4);
            var sS = new TileSide.Meadow(z4);
            var sW = new TileSide.River(z4, z5, z0);
            tiles.add(new Tile(10, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 11
            var l1 = new Zone.Lake(11_8, 1, null);
            var z0 = new Zone.Meadow(11_0, List.of(), null);
            var z1 = new Zone.River(11_1, 0, l1);
            var z2 = new Zone.Meadow(11_2, List.of(), null);
            var z3 = new Zone.River(11_3, 0, l1);
            var z4 = new Zone.Meadow(11_4, List.of(), null);
            var z5 = new Zone.Forest(11_5, Zone.Forest.Kind.PLAIN);
            var z6 = new Zone.Meadow(11_6, List.of(), null);
            var z7 = new Zone.River(11_7, 0, l1);
            var sN = new TileSide.River(z0, z1, z2);
            var sE = new TileSide.River(z2, z3, z4);
            var sS = new TileSide.Forest(z5);
            var sW = new TileSide.River(z6, z7, z0);
            tiles.add(new Tile(11, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 12
            var l1 = new Zone.Lake(12_8, 1, null);
            var z0 = new Zone.Meadow(12_0, List.of(), null);
            var z1 = new Zone.River(12_1, 0, l1);
            var z2 = new Zone.Meadow(12_2, List.of(), null);
            var z3 = new Zone.River(12_3, 0, l1);
            var a4_0 = new Animal(12_4_0, Animal.Kind.DEER);
            var z4 = new Zone.Meadow(12_4, List.of(a4_0), null);
            var z5 = new Zone.River(12_5, 0, l1);
            var sN = new TileSide.River(z0, z1, z2);
            var sE = new TileSide.Meadow(z2);
            var sS = new TileSide.River(z2, z3, z4);
            var sW = new TileSide.River(z4, z5, z0);
            tiles.add(new Tile(12, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 13
            var l1 = new Zone.Lake(13_8, 2, null);
            var a0_0 = new Animal(13_0_0, Animal.Kind.DEER);
            var z0 = new Zone.Meadow(13_0, List.of(a0_0), null);
            var z1 = new Zone.River(13_1, 0, l1);
            var z2 = new Zone.Meadow(13_2, List.of(), null);
            var z3 = new Zone.River(13_3, 0, l1);
            var z4 = new Zone.Meadow(13_4, List.of(), null);
            var z5 = new Zone.River(13_5, 0, l1);
            var a6_0 = new Animal(13_6_0, Animal.Kind.DEER);
            var z6 = new Zone.Meadow(13_6, List.of(a6_0), null);
            var z7 = new Zone.River(13_7, 0, l1);
            var sN = new TileSide.River(z0, z1, z2);
            var sE = new TileSide.River(z2, z3, z4);
            var sS = new TileSide.River(z4, z5, z6);
            var sW = new TileSide.River(z6, z7, z0);
            tiles.add(new Tile(13, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 14
            var z0 = new Zone.Meadow(14_0, List.of(), null);
            var z1 = new Zone.River(14_1, 0, null);
            var a2_0 = new Animal(14_2_0, Animal.Kind.TIGER);
            var z2 = new Zone.Meadow(14_2, List.of(a2_0), null);
            var z3 = new Zone.Forest(14_3, Zone.Forest.Kind.PLAIN);
            var z4 = new Zone.Forest(14_4, Zone.Forest.Kind.PLAIN);
            var z5 = new Zone.Forest(14_5, Zone.Forest.Kind.PLAIN);
            var sN = new TileSide.River(z0, z1, z2);
            var sE = new TileSide.Forest(z3);
            var sS = new TileSide.Forest(z4);
            var sW = new TileSide.Forest(z5);
            tiles.add(new Tile(14, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 15
            var a0_0 = new Animal(15_0_0, Animal.Kind.DEER);
            var z0 = new Zone.Meadow(15_0, List.of(a0_0), null);
            var z1 = new Zone.River(15_1, 0, null);
            var z2 = new Zone.Meadow(15_2, List.of(), null);
            var sN = new TileSide.Meadow(z0);
            var sE = new TileSide.River(z0, z1, z2);
            var sS = new TileSide.River(z2, z1, z0);
            var sW = new TileSide.Meadow(z0);
            tiles.add(new Tile(15, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 16
            var z0 = new Zone.Meadow(16_0, List.of(), null);
            var z1 = new Zone.River(16_1, 0, null);
            var a2_0 = new Animal(16_2_0, Animal.Kind.DEER);
            var z2 = new Zone.Meadow(16_2, List.of(a2_0), null);
            var sN = new TileSide.Meadow(z0);
            var sE = new TileSide.River(z0, z1, z2);
            var sS = new TileSide.River(z2, z1, z0);
            var sW = new TileSide.Meadow(z0);
            tiles.add(new Tile(16, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 17
            var z0 = new Zone.Meadow(17_0, List.of(), null);
            var z1 = new Zone.River(17_1, 0, null);
            var a2_0 = new Animal(17_2_0, Animal.Kind.DEER);
            var z2 = new Zone.Meadow(17_2, List.of(a2_0), null);
            var z3 = new Zone.River(17_3, 0, null);
            var a4_0 = new Animal(17_4_0, Animal.Kind.TIGER);
            var z4 = new Zone.Meadow(17_4, List.of(a4_0), null);
            var sN = new TileSide.River(z0, z1, z2);
            var sE = new TileSide.River(z2, z1, z0);
            var sS = new TileSide.River(z0, z3, z4);
            var sW = new TileSide.River(z4, z3, z0);
            tiles.add(new Tile(17, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 18
            var a0_0 = new Animal(18_0_0, Animal.Kind.TIGER);
            var z0 = new Zone.Meadow(18_0, List.of(a0_0), null);
            var z1 = new Zone.River(18_1, 0, null);
            var z2 = new Zone.Meadow(18_2, List.of(), null);
            var sN = new TileSide.Meadow(z0);
            var sE = new TileSide.River(z0, z1, z2);
            var sS = new TileSide.River(z2, z1, z0);
            var sW = new TileSide.Meadow(z0);
            tiles.add(new Tile(18, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 19
            var a0_0 = new Animal(19_0_0, Animal.Kind.DEER);
            var z0 = new Zone.Meadow(19_0, List.of(a0_0), null);
            var z1 = new Zone.River(19_1, 1, null);
            var z2 = new Zone.Meadow(19_2, List.of(), null);
            var sN = new TileSide.Meadow(z0);
            var sE = new TileSide.River(z0, z1, z2);
            var sS = new TileSide.River(z2, z1, z0);
            var sW = new TileSide.Meadow(z0);
            tiles.add(new Tile(19, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 20
            var a0_0 = new Animal(20_0_0, Animal.Kind.AUROCHS);
            var z0 = new Zone.Meadow(20_0, List.of(a0_0), null);
            var z1 = new Zone.River(20_1, 2, null);
            var z2 = new Zone.Meadow(20_2, List.of(), null);
            var z3 = new Zone.Meadow(20_3, List.of(), null);
            var z4 = new Zone.Forest(20_4, Zone.Forest.Kind.PLAIN);
            var sN = new TileSide.River(z0, z1, z2);
            var sE = new TileSide.River(z2, z1, z3);
            var sS = new TileSide.Forest(z4);
            var sW = new TileSide.Meadow(z0);
            tiles.add(new Tile(20, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 21
            var z0 = new Zone.Meadow(21_0, List.of(), null);
            var z1 = new Zone.River(21_1, 0, null);
            var a2_0 = new Animal(21_2_0, Animal.Kind.AUROCHS);
            var z2 = new Zone.Meadow(21_2, List.of(a2_0), null);
            var sN = new TileSide.Meadow(z0);
            var sE = new TileSide.River(z0, z1, z2);
            var sS = new TileSide.River(z2, z1, z0);
            var sW = new TileSide.Meadow(z0);
            tiles.add(new Tile(21, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 22
            var z0 = new Zone.Meadow(22_0, List.of(), null);
            var z1 = new Zone.River(22_1, 0, null);
            var a2_0 = new Animal(22_2_0, Animal.Kind.AUROCHS);
            var z2 = new Zone.Meadow(22_2, List.of(a2_0), null);
            var z3 = new Zone.Forest(22_3, Zone.Forest.Kind.PLAIN);
            var sN = new TileSide.River(z0, z1, z2);
            var sE = new TileSide.Meadow(z2);
            var sS = new TileSide.Forest(z3);
            var sW = new TileSide.River(z2, z1, z0);
            tiles.add(new Tile(22, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 23
            var a0_0 = new Animal(23_0_0, Animal.Kind.AUROCHS);
            var z0 = new Zone.Meadow(23_0, List.of(a0_0), null);
            var z1 = new Zone.River(23_1, 0, null);
            var z2 = new Zone.Meadow(23_2, List.of(), null);
            var z3 = new Zone.Forest(23_3, Zone.Forest.Kind.WITH_MENHIR);
            var sN = new TileSide.River(z0, z1, z2);
            var sE = new TileSide.River(z2, z1, z0);
            var sS = new TileSide.Forest(z3);
            var sW = new TileSide.Forest(z3);
            tiles.add(new Tile(23, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 24
            var l1 = new Zone.Lake(24_8, 1, null);
            var z0 = new Zone.Meadow(24_0, List.of(), null);
            var z1 = new Zone.River(24_1, 0, l1);
            var a2_0 = new Animal(24_2_0, Animal.Kind.AUROCHS);
            var z2 = new Zone.Meadow(24_2, List.of(a2_0), null);
            var z3 = new Zone.Forest(24_3, Zone.Forest.Kind.PLAIN);
            var z4 = new Zone.Meadow(24_4, List.of(), null);
            var z5 = new Zone.River(24_5, 0, l1);
            var sN = new TileSide.River(z0, z1, z2);
            var sE = new TileSide.Meadow(z2);
            var sS = new TileSide.Forest(z3);
            var sW = new TileSide.River(z4, z5, z0);
            tiles.add(new Tile(24, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 25
            var z0 = new Zone.Meadow(25_0, List.of(), null);
            var z1 = new Zone.River(25_1, 0, null);
            var a2_0 = new Animal(25_2_0, Animal.Kind.DEER);
            var z2 = new Zone.Meadow(25_2, List.of(a2_0), null);
            var z3 = new Zone.Forest(25_3, Zone.Forest.Kind.PLAIN);
            var sN = new TileSide.River(z0, z1, z2);
            var sE = new TileSide.River(z2, z1, z0);
            var sS = new TileSide.Forest(z3);
            var sW = new TileSide.Meadow(z0);
            tiles.add(new Tile(25, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 26
            var z0 = new Zone.Meadow(26_0, List.of(), null);
            var z1 = new Zone.River(26_1, 0, null);
            var z2 = new Zone.Meadow(26_2, List.of(), null);
            var z3 = new Zone.Forest(26_3, Zone.Forest.Kind.PLAIN);
            var z4 = new Zone.Forest(26_4, Zone.Forest.Kind.PLAIN);
            var sN = new TileSide.River(z0, z1, z2);
            var sE = new TileSide.River(z2, z1, z0);
            var sS = new TileSide.Forest(z3);
            var sW = new TileSide.Forest(z4);
            tiles.add(new Tile(26, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 27
            var z0 = new Zone.Meadow(27_0, List.of(), null);
            var z1 = new Zone.River(27_1, 0, null);
            var a2_0 = new Animal(27_2_0, Animal.Kind.DEER);
            var z2 = new Zone.Meadow(27_2, List.of(a2_0), null);
            var z3 = new Zone.Forest(27_3, Zone.Forest.Kind.PLAIN);
            var sN = new TileSide.Meadow(z0);
            var sE = new TileSide.River(z0, z1, z2);
            var sS = new TileSide.River(z2, z1, z0);
            var sW = new TileSide.Forest(z3);
            tiles.add(new Tile(27, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 28
            var z0 = new Zone.Meadow(28_0, List.of(), null);
            var z1 = new Zone.River(28_1, 1, null);
            var z2 = new Zone.Meadow(28_2, List.of(), null);
            var z3 = new Zone.Forest(28_3, Zone.Forest.Kind.PLAIN);
            var sN = new TileSide.River(z0, z1, z2);
            var sE = new TileSide.Meadow(z2);
            var sS = new TileSide.Forest(z3);
            var sW = new TileSide.River(z2, z1, z0);
            tiles.add(new Tile(28, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 29
            var z0 = new Zone.Forest(29_0, Zone.Forest.Kind.WITH_MENHIR);
            var z1 = new Zone.Meadow(29_1, List.of(), null);
            var z2 = new Zone.River(29_2, 0, null);
            var z3 = new Zone.Meadow(29_3, List.of(), null);
            var sN = new TileSide.Forest(z0);
            var sE = new TileSide.River(z1, z2, z3);
            var sS = new TileSide.River(z3, z2, z1);
            var sW = new TileSide.Forest(z0);
            tiles.add(new Tile(29, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 30
            var a0_0 = new Animal(30_0_0, Animal.Kind.DEER);
            var z0 = new Zone.Meadow(30_0, List.of(a0_0), null);
            var z1 = new Zone.Forest(30_1, Zone.Forest.Kind.WITH_MENHIR);
            var sN = new TileSide.Meadow(z0);
            var sE = new TileSide.Meadow(z0);
            var sS = new TileSide.Forest(z1);
            var sW = new TileSide.Forest(z1);
            tiles.add(new Tile(30, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 31
            var z0 = new Zone.Forest(31_0, Zone.Forest.Kind.WITH_MENHIR);
            var a1_0 = new Animal(31_1_0, Animal.Kind.TIGER);
            var z1 = new Zone.Meadow(31_1, List.of(a1_0), null);
            var sN = new TileSide.Forest(z0);
            var sE = new TileSide.Meadow(z1);
            var sS = new TileSide.Meadow(z1);
            var sW = new TileSide.Forest(z0);
            tiles.add(new Tile(31, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 32
            var z0 = new Zone.Forest(32_0, Zone.Forest.Kind.WITH_MENHIR);
            var a1_0 = new Animal(32_1_0, Animal.Kind.TIGER);
            var z1 = new Zone.Meadow(32_1, List.of(a1_0), null);
            var sN = new TileSide.Forest(z0);
            var sE = new TileSide.Meadow(z1);
            var sS = new TileSide.Meadow(z1);
            var sW = new TileSide.Forest(z0);
            tiles.add(new Tile(32, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 33
            var z0 = new Zone.Forest(33_0, Zone.Forest.Kind.WITH_MENHIR);
            var z1 = new Zone.Meadow(33_1, List.of(), null);
            var sN = new TileSide.Forest(z0);
            var sE = new TileSide.Meadow(z1);
            var sS = new TileSide.Meadow(z1);
            var sW = new TileSide.Forest(z0);
            tiles.add(new Tile(33, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 34
            var z0 = new Zone.Meadow(34_0, List.of(), null);
            var z1 = new Zone.Meadow(34_1, List.of(), null);
            var a2_0 = new Animal(34_2_0, Animal.Kind.DEER);
            var z2 = new Zone.Meadow(34_2, List.of(a2_0), null);
            var z3 = new Zone.Forest(34_3, Zone.Forest.Kind.PLAIN);
            var sN = new TileSide.Meadow(z0);
            var sE = new TileSide.Meadow(z1);
            var sS = new TileSide.Meadow(z2);
            var sW = new TileSide.Forest(z3);
            tiles.add(new Tile(34, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 35
            var a0_0 = new Animal(35_0_0, Animal.Kind.MAMMOTH);
            var z0 = new Zone.Meadow(35_0, List.of(a0_0), null);
            var z1 = new Zone.Forest(35_1, Zone.Forest.Kind.PLAIN);
            var sN = new TileSide.Meadow(z0);
            var sE = new TileSide.Forest(z1);
            var sS = new TileSide.Meadow(z0);
            var sW = new TileSide.Meadow(z0);
            tiles.add(new Tile(35, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 36
            var a0_0 = new Animal(36_0_0, Animal.Kind.AUROCHS);
            var z0 = new Zone.Meadow(36_0, List.of(a0_0), null);
            var z1 = new Zone.Forest(36_1, Zone.Forest.Kind.PLAIN);
            var sN = new TileSide.Meadow(z0);
            var sE = new TileSide.Meadow(z0);
            var sS = new TileSide.Forest(z1);
            var sW = new TileSide.Meadow(z0);
            tiles.add(new Tile(36, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 37
            var z0 = new Zone.Forest(37_0, Zone.Forest.Kind.PLAIN);
            var a1_0 = new Animal(37_1_0, Animal.Kind.DEER);
            var z1 = new Zone.Meadow(37_1, List.of(a1_0), null);
            var sN = new TileSide.Forest(z0);
            var sE = new TileSide.Meadow(z1);
            var sS = new TileSide.Meadow(z1);
            var sW = new TileSide.Meadow(z1);
            tiles.add(new Tile(37, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 38
            var a0_0 = new Animal(38_0_0, Animal.Kind.DEER);
            var z0 = new Zone.Meadow(38_0, List.of(a0_0), null);
            var z1 = new Zone.Forest(38_1, Zone.Forest.Kind.WITH_MENHIR);
            var z2 = new Zone.Meadow(38_2, List.of(), null);
            var sN = new TileSide.Meadow(z0);
            var sE = new TileSide.Forest(z1);
            var sS = new TileSide.Meadow(z2);
            var sW = new TileSide.Forest(z1);
            tiles.add(new Tile(38, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 39
            var z0 = new Zone.Meadow(39_0, List.of(), null);
            var z1 = new Zone.Forest(39_1, Zone.Forest.Kind.WITH_MENHIR);
            var z2 = new Zone.Meadow(39_2, List.of(), null);
            var sN = new TileSide.Meadow(z0);
            var sE = new TileSide.Forest(z1);
            var sS = new TileSide.Meadow(z2);
            var sW = new TileSide.Forest(z1);
            tiles.add(new Tile(39, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 40
            var z0 = new Zone.Meadow(40_0, List.of(), null);
            var z1 = new Zone.Forest(40_1, Zone.Forest.Kind.WITH_MENHIR);
            var z2 = new Zone.Meadow(40_2, List.of(), null);
            var sN = new TileSide.Meadow(z0);
            var sE = new TileSide.Forest(z1);
            var sS = new TileSide.Meadow(z2);
            var sW = new TileSide.Forest(z1);
            tiles.add(new Tile(40, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 41
            var a0_0 = new Animal(41_0_0, Animal.Kind.DEER);
            var z0 = new Zone.Meadow(41_0, List.of(a0_0), null);
            var z1 = new Zone.Forest(41_1, Zone.Forest.Kind.PLAIN);
            var z2 = new Zone.Forest(41_2, Zone.Forest.Kind.PLAIN);
            var sN = new TileSide.Meadow(z0);
            var sE = new TileSide.Forest(z1);
            var sS = new TileSide.Meadow(z0);
            var sW = new TileSide.Forest(z2);
            tiles.add(new Tile(41, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 42
            var z0 = new Zone.Forest(42_0, Zone.Forest.Kind.WITH_MENHIR);
            var z1 = new Zone.Meadow(42_1, List.of(), null);
            var z2 = new Zone.Meadow(42_2, List.of(), null);
            var sN = new TileSide.Forest(z0);
            var sE = new TileSide.Forest(z0);
            var sS = new TileSide.Meadow(z1);
            var sW = new TileSide.Meadow(z2);
            tiles.add(new Tile(42, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 43
            var z0 = new Zone.Forest(43_0, Zone.Forest.Kind.WITH_MENHIR);
            var z1 = new Zone.Forest(43_1, Zone.Forest.Kind.PLAIN);
            var z2 = new Zone.Meadow(43_2, List.of(), null);
            var sN = new TileSide.Forest(z0);
            var sE = new TileSide.Forest(z0);
            var sS = new TileSide.Forest(z1);
            var sW = new TileSide.Meadow(z2);
            tiles.add(new Tile(43, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 44
            var z0 = new Zone.Forest(44_0, Zone.Forest.Kind.WITH_MENHIR);
            var z1 = new Zone.Forest(44_1, Zone.Forest.Kind.PLAIN);
            var a2_0 = new Animal(44_2_0, Animal.Kind.DEER);
            var z2 = new Zone.Meadow(44_2, List.of(a2_0), null);
            var sN = new TileSide.Forest(z0);
            var sE = new TileSide.Forest(z1);
            var sS = new TileSide.Meadow(z2);
            var sW = new TileSide.Forest(z0);
            tiles.add(new Tile(44, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 45
            var z0 = new Zone.Meadow(45_0, List.of(), null);
            var z1 = new Zone.River(45_1, 1, null);
            var z2 = new Zone.Meadow(45_2, List.of(), null);
            var z3 = new Zone.Forest(45_3, Zone.Forest.Kind.PLAIN);
            var a4_0 = new Animal(45_4_0, Animal.Kind.DEER);
            var z4 = new Zone.Meadow(45_4, List.of(a4_0), null);
            var z5 = new Zone.Meadow(45_5, List.of(), null);
            var z6 = new Zone.Forest(45_6, Zone.Forest.Kind.PLAIN);
            var sN = new TileSide.River(z0, z1, z2);
            var sE = new TileSide.Forest(z3);
            var sS = new TileSide.River(z4, z1, z5);
            var sW = new TileSide.Forest(z6);
            tiles.add(new Tile(45, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 46
            var z0 = new Zone.Meadow(46_0, List.of(), null);
            var z1 = new Zone.River(46_1, 0, null);
            var z2 = new Zone.Meadow(46_2, List.of(), null);
            var z3 = new Zone.Forest(46_3, Zone.Forest.Kind.PLAIN);
            var sN = new TileSide.Meadow(z0);
            var sE = new TileSide.River(z0, z1, z2);
            var sS = new TileSide.Forest(z3);
            var sW = new TileSide.River(z2, z1, z0);
            tiles.add(new Tile(46, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 47
            var z0 = new Zone.Meadow(47_0, List.of(), null);
            var z1 = new Zone.River(47_1, 1, null);
            var a2_0 = new Animal(47_2_0, Animal.Kind.DEER);
            var z2 = new Zone.Meadow(47_2, List.of(a2_0), null);
            var z3 = new Zone.Forest(47_3, Zone.Forest.Kind.PLAIN);
            var sN = new TileSide.Meadow(z0);
            var sE = new TileSide.River(z0, z1, z2);
            var sS = new TileSide.Forest(z3);
            var sW = new TileSide.River(z2, z1, z0);
            tiles.add(new Tile(47, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 48
            var z0 = new Zone.Meadow(48_0, List.of(), null);
            var z1 = new Zone.River(48_1, 0, null);
            var a2_0 = new Animal(48_2_0, Animal.Kind.TIGER);
            var z2 = new Zone.Meadow(48_2, List.of(a2_0), null);
            var sN = new TileSide.Meadow(z0);
            var sE = new TileSide.River(z0, z1, z2);
            var sS = new TileSide.Meadow(z2);
            var sW = new TileSide.River(z2, z1, z0);
            tiles.add(new Tile(48, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 49
            var a0_0 = new Animal(49_0_0, Animal.Kind.DEER);
            var z0 = new Zone.Meadow(49_0, List.of(a0_0), null);
            var z1 = new Zone.River(49_1, 0, null);
            var a2_0 = new Animal(49_2_0, Animal.Kind.TIGER);
            var z2 = new Zone.Meadow(49_2, List.of(a2_0), null);
            var sN = new TileSide.Meadow(z0);
            var sE = new TileSide.River(z0, z1, z2);
            var sS = new TileSide.Meadow(z2);
            var sW = new TileSide.River(z2, z1, z0);
            tiles.add(new Tile(49, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 50
            var a0_0 = new Animal(50_0_0, Animal.Kind.DEER);
            var z0 = new Zone.Meadow(50_0, List.of(a0_0), null);
            var z1 = new Zone.River(50_1, 0, null);
            var z2 = new Zone.Meadow(50_2, List.of(), null);
            var sN = new TileSide.River(z0, z1, z2);
            var sE = new TileSide.Meadow(z2);
            var sS = new TileSide.River(z2, z1, z0);
            var sW = new TileSide.Meadow(z0);
            tiles.add(new Tile(50, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 51
            var a0_0 = new Animal(51_0_0, Animal.Kind.AUROCHS);
            var z0 = new Zone.Meadow(51_0, List.of(a0_0), null);
            var z1 = new Zone.River(51_1, 0, null);
            var z2 = new Zone.Meadow(51_2, List.of(), null);
            var sN = new TileSide.Meadow(z0);
            var sE = new TileSide.River(z0, z1, z2);
            var sS = new TileSide.Meadow(z2);
            var sW = new TileSide.River(z2, z1, z0);
            tiles.add(new Tile(51, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 52
            var a0_0 = new Animal(52_0_0, Animal.Kind.DEER);
            var z0 = new Zone.Meadow(52_0, List.of(a0_0), null);
            var z1 = new Zone.River(52_1, 0, null);
            var z2 = new Zone.Meadow(52_2, List.of(), null);
            var sN = new TileSide.Meadow(z0);
            var sE = new TileSide.River(z0, z1, z2);
            var sS = new TileSide.Meadow(z2);
            var sW = new TileSide.River(z2, z1, z0);
            tiles.add(new Tile(52, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 53
            var z0 = new Zone.Meadow(53_0, List.of(), null);
            var z1 = new Zone.River(53_1, 0, null);
            var a2_0 = new Animal(53_2_0, Animal.Kind.MAMMOTH);
            var z2 = new Zone.Meadow(53_2, List.of(a2_0), null);
            var sN = new TileSide.Meadow(z0);
            var sE = new TileSide.River(z0, z1, z2);
            var sS = new TileSide.Meadow(z2);
            var sW = new TileSide.River(z2, z1, z0);
            tiles.add(new Tile(53, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 54
            var z0 = new Zone.Forest(54_0, Zone.Forest.Kind.WITH_MENHIR);
            var z1 = new Zone.Meadow(54_1, List.of(), null);
            var z2 = new Zone.River(54_2, 0, null);
            var a3_0 = new Animal(54_3_0, Animal.Kind.DEER);
            var z3 = new Zone.Meadow(54_3, List.of(a3_0), null);
            var z4 = new Zone.Meadow(54_4, List.of(), null);
            var sN = new TileSide.Forest(z0);
            var sE = new TileSide.River(z1, z2, z3);
            var sS = new TileSide.River(z3, z2, z4);
            var sW = new TileSide.Forest(z0);
            tiles.add(new Tile(54, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 55
            var z0 = new Zone.Forest(55_0, Zone.Forest.Kind.WITH_MENHIR);
            var z1 = new Zone.Forest(55_1, Zone.Forest.Kind.PLAIN);
            var z2 = new Zone.Meadow(55_2, List.of(), null);
            var z3 = new Zone.River(55_3, 0, null);
            var z4 = new Zone.Meadow(55_4, List.of(), null);
            var sN = new TileSide.Forest(z0);
            var sE = new TileSide.Forest(z0);
            var sS = new TileSide.Forest(z1);
            var sW = new TileSide.River(z2, z3, z4);
            tiles.add(new Tile(55, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 56
            var l1 = new Zone.Lake(56_8, 1, null);
            var a0_0 = new Animal(56_0_0, Animal.Kind.AUROCHS);
            var z0 = new Zone.Meadow(56_0, List.of(a0_0), null);
            var z1 = new Zone.Forest(56_1, Zone.Forest.Kind.WITH_MENHIR);
            var z2 = new Zone.Meadow(56_2, List.of(), null);
            var z3 = new Zone.River(56_3, 0, l1);
            var sN = new TileSide.Meadow(z0);
            var sE = new TileSide.Forest(z1);
            var sS = new TileSide.Forest(z1);
            var sW = new TileSide.River(z2, z3, z0);
            tiles.add(new Tile(56, Tile.Kind.START, sN, sE, sS, sW));
        }
        {   // Tile 57
            var l1 = new Zone.Lake(57_8, 2, null);
            var z0 = new Zone.Meadow(57_0, List.of(), null);
            var z1 = new Zone.River(57_1, 0, l1);
            var z2 = new Zone.Meadow(57_2, List.of(), null);
            var z3 = new Zone.Forest(57_3, Zone.Forest.Kind.PLAIN);
            var z4 = new Zone.Forest(57_4, Zone.Forest.Kind.WITH_MENHIR);
            var sN = new TileSide.River(z0, z1, z2);
            var sE = new TileSide.Forest(z3);
            var sS = new TileSide.Forest(z4);
            var sW = new TileSide.Forest(z4);
            tiles.add(new Tile(57, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 58
            var l1 = new Zone.Lake(58_8, 1, null);
            var z0 = new Zone.Forest(58_0, Zone.Forest.Kind.WITH_MENHIR);
            var z1 = new Zone.Meadow(58_1, List.of(), null);
            var z2 = new Zone.River(58_2, 0, l1);
            var z3 = new Zone.Meadow(58_3, List.of(), null);
            var sN = new TileSide.Forest(z0);
            var sE = new TileSide.Forest(z0);
            var sS = new TileSide.River(z1, z2, z3);
            var sW = new TileSide.Forest(z0);
            tiles.add(new Tile(58, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 59
            var l1 = new Zone.Lake(59_8, 1, null);
            var z0 = new Zone.Meadow(59_0, List.of(), null);
            var z1 = new Zone.River(59_1, 0, l1);
            var a2_0 = new Animal(59_2_0, Animal.Kind.TIGER);
            var z2 = new Zone.Meadow(59_2, List.of(a2_0), null);
            var z3 = new Zone.Forest(59_3, Zone.Forest.Kind.PLAIN);
            var sN = new TileSide.River(z0, z1, z2);
            var sE = new TileSide.Meadow(z2);
            var sS = new TileSide.Forest(z3);
            var sW = new TileSide.Meadow(z0);
            tiles.add(new Tile(59, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 60
            var z0 = new Zone.Meadow(60_0, List.of(), null);
            var z1 = new Zone.Forest(60_1, Zone.Forest.Kind.WITH_MENHIR);
            var a2_0 = new Animal(60_2_0, Animal.Kind.DEER);
            var z2 = new Zone.Meadow(60_2, List.of(a2_0), null);
            var sN = new TileSide.Meadow(z0);
            var sE = new TileSide.Forest(z1);
            var sS = new TileSide.Meadow(z2);
            var sW = new TileSide.Forest(z1);
            tiles.add(new Tile(60, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 61
            var a0_0 = new Animal(61_0_0, Animal.Kind.MAMMOTH);
            var z0 = new Zone.Meadow(61_0, List.of(a0_0), null);
            var sN = new TileSide.Meadow(z0);
            var sE = new TileSide.Meadow(z0);
            var sS = new TileSide.Meadow(z0);
            var sW = new TileSide.Meadow(z0);
            tiles.add(new Tile(61, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 62
            var a0_0 = new Animal(62_0_0, Animal.Kind.DEER);
            var z0 = new Zone.Meadow(62_0, List.of(a0_0), null);
            var sN = new TileSide.Meadow(z0);
            var sE = new TileSide.Meadow(z0);
            var sS = new TileSide.Meadow(z0);
            var sW = new TileSide.Meadow(z0);
            tiles.add(new Tile(62, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 63
            var z0 = new Zone.Forest(63_0, Zone.Forest.Kind.WITH_MENHIR);
            var z1 = new Zone.Meadow(63_1, List.of(), null);
            var sN = new TileSide.Forest(z0);
            var sE = new TileSide.Forest(z0);
            var sS = new TileSide.Meadow(z1);
            var sW = new TileSide.Forest(z0);
            tiles.add(new Tile(63, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 64
            var z0 = new Zone.Forest(64_0, Zone.Forest.Kind.WITH_MENHIR);
            var z1 = new Zone.Meadow(64_1, List.of(), null);
            var z2 = new Zone.Forest(64_2, Zone.Forest.Kind.PLAIN);
            var sN = new TileSide.Forest(z0);
            var sE = new TileSide.Forest(z0);
            var sS = new TileSide.Meadow(z1);
            var sW = new TileSide.Forest(z2);
            tiles.add(new Tile(64, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 65
            var z0 = new Zone.Forest(65_0, Zone.Forest.Kind.WITH_MENHIR);
            var a1_0 = new Animal(65_1_0, Animal.Kind.TIGER);
            var z1 = new Zone.Meadow(65_1, List.of(a1_0), null);
            var sN = new TileSide.Forest(z0);
            var sE = new TileSide.Forest(z0);
            var sS = new TileSide.Meadow(z1);
            var sW = new TileSide.Forest(z0);
            tiles.add(new Tile(65, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 66
            var z0 = new Zone.Forest(66_0, Zone.Forest.Kind.WITH_MENHIR);
            var z1 = new Zone.Meadow(66_1, List.of(), null);
            var z2 = new Zone.River(66_2, 0, null);
            var z3 = new Zone.Meadow(66_3, List.of(), null);
            var z4 = new Zone.Meadow(66_4, List.of(), null);
            var sN = new TileSide.Forest(z0);
            var sE = new TileSide.Forest(z0);
            var sS = new TileSide.River(z1, z2, z3);
            var sW = new TileSide.Meadow(z4);
            tiles.add(new Tile(66, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 67
            var z0 = new Zone.Forest(67_0, Zone.Forest.Kind.PLAIN);
            var z1 = new Zone.Forest(67_1, Zone.Forest.Kind.PLAIN);
            var a2_0 = new Animal(67_2_0, Animal.Kind.DEER);
            var z2 = new Zone.Meadow(67_2, List.of(a2_0), null);
            var sN = new TileSide.Forest(z0);
            var sE = new TileSide.Forest(z1);
            var sS = new TileSide.Meadow(z2);
            var sW = new TileSide.Meadow(z2);
            tiles.add(new Tile(67, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 68
            var z0 = new Zone.Forest(68_0, Zone.Forest.Kind.PLAIN);
            var z1 = new Zone.Meadow(68_1, List.of(), null);
            var z2 = new Zone.River(68_2, 0, null);
            var z3 = new Zone.Meadow(68_3, List.of(), null);
            var sN = new TileSide.Forest(z0);
            var sE = new TileSide.Meadow(z1);
            var sS = new TileSide.River(z1, z2, z3);
            var sW = new TileSide.Meadow(z3);
            tiles.add(new Tile(68, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 69
            var z0 = new Zone.Forest(69_0, Zone.Forest.Kind.WITH_MENHIR);
            var z1 = new Zone.Meadow(69_1, List.of(), null);
            var z2 = new Zone.River(69_2, 0, null);
            var z3 = new Zone.Meadow(69_3, List.of(), null);
            var sN = new TileSide.Forest(z0);
            var sE = new TileSide.River(z1, z2, z3);
            var sS = new TileSide.River(z3, z2, z1);
            var sW = new TileSide.Forest(z0);
            tiles.add(new Tile(69, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 70
            var z0 = new Zone.Meadow(70_0, List.of(), null);
            var z1 = new Zone.River(70_1, 0, null);
            var z2 = new Zone.Meadow(70_2, List.of(), null);
            var z3 = new Zone.River(70_3, 0, null);
            var z4 = new Zone.Meadow(70_4, List.of(), null);
            var z5 = new Zone.Forest(70_5, Zone.Forest.Kind.WITH_MENHIR);
            var sN = new TileSide.River(z0, z1, z2);
            var sE = new TileSide.River(z2, z3, z4);
            var sS = new TileSide.Forest(z5);
            var sW = new TileSide.Forest(z5);
            tiles.add(new Tile(70, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 71
            var l1 = new Zone.Lake(71_8, 2, null);
            var z0 = new Zone.Meadow(71_0, List.of(), null);
            var z1 = new Zone.River(71_1, 0, null);
            var z2 = new Zone.Meadow(71_2, List.of(), null);
            var z3 = new Zone.Forest(71_3, Zone.Forest.Kind.WITH_MENHIR);
            var z4 = new Zone.Meadow(71_4, List.of(), null);
            var z5 = new Zone.River(71_5, 0, l1);
            var z6 = new Zone.Meadow(71_6, List.of(), null);
            var sN = new TileSide.River(z0, z1, z2);
            var sE = new TileSide.Forest(z3);
            var sS = new TileSide.River(z4, z5, z6);
            var sW = new TileSide.Forest(z3);
            tiles.add(new Tile(71, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 72
            var a0_0 = new Animal(72_0_0, Animal.Kind.TIGER);
            var z0 = new Zone.Meadow(72_0, List.of(a0_0), null);
            var z1 = new Zone.River(72_1, 0, null);
            var z2 = new Zone.Meadow(72_2, List.of(), null);
            var z3 = new Zone.Forest(72_3, Zone.Forest.Kind.PLAIN);
            var z4 = new Zone.Forest(72_4, Zone.Forest.Kind.PLAIN);
            var sN = new TileSide.River(z0, z1, z2);
            var sE = new TileSide.Forest(z3);
            var sS = new TileSide.River(z2, z1, z0);
            var sW = new TileSide.Forest(z4);
            tiles.add(new Tile(72, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 73
            var z0 = new Zone.Meadow(73_0, List.of(), null);
            var z1 = new Zone.River(73_1, 0, null);
            var a2_0 = new Animal(73_2_0, Animal.Kind.DEER);
            var z2 = new Zone.Meadow(73_2, List.of(a2_0), null);
            var z3 = new Zone.Forest(73_3, Zone.Forest.Kind.PLAIN);
            var z4 = new Zone.Forest(73_4, Zone.Forest.Kind.PLAIN);
            var sN = new TileSide.River(z0, z1, z2);
            var sE = new TileSide.Forest(z3);
            var sS = new TileSide.River(z2, z1, z0);
            var sW = new TileSide.Forest(z4);
            tiles.add(new Tile(73, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 74
            var z0 = new Zone.Meadow(74_0, List.of(), null);
            var z1 = new Zone.River(74_1, 1, null);
            var z2 = new Zone.Meadow(74_2, List.of(), null);
            var z3 = new Zone.Forest(74_3, Zone.Forest.Kind.PLAIN);
            var z4 = new Zone.Forest(74_4, Zone.Forest.Kind.PLAIN);
            var sN = new TileSide.River(z0, z1, z2);
            var sE = new TileSide.Forest(z3);
            var sS = new TileSide.River(z2, z1, z0);
            var sW = new TileSide.Forest(z4);
            tiles.add(new Tile(74, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 75
            var z0 = new Zone.Meadow(75_0, List.of(), null);
            var z1 = new Zone.Forest(75_1, Zone.Forest.Kind.WITH_MENHIR);
            var sN = new TileSide.Meadow(z0);
            var sE = new TileSide.Forest(z1);
            var sS = new TileSide.Forest(z1);
            var sW = new TileSide.Meadow(z0);
            tiles.add(new Tile(75, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 76
            var a0_0 = new Animal(76_0_0, Animal.Kind.AUROCHS);
            var z0 = new Zone.Meadow(76_0, List.of(a0_0), null);
            var z1 = new Zone.Forest(76_1, Zone.Forest.Kind.WITH_MENHIR);
            var sN = new TileSide.Meadow(z0);
            var sE = new TileSide.Meadow(z0);
            var sS = new TileSide.Forest(z1);
            var sW = new TileSide.Forest(z1);
            tiles.add(new Tile(76, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 77
            var z0 = new Zone.Meadow(77_0, List.of(), null);
            var z1 = new Zone.Forest(77_1, Zone.Forest.Kind.WITH_MENHIR);
            var sN = new TileSide.Meadow(z0);
            var sE = new TileSide.Forest(z1);
            var sS = new TileSide.Forest(z1);
            var sW = new TileSide.Meadow(z0);
            tiles.add(new Tile(77, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 78
            var z0 = new Zone.Forest(78_0, Zone.Forest.Kind.WITH_MENHIR);
            var a1_0 = new Animal(78_1_0, Animal.Kind.DEER);
            var z1 = new Zone.Meadow(78_1, List.of(a1_0), null);
            var sN = new TileSide.Forest(z0);
            var sE = new TileSide.Forest(z0);
            var sS = new TileSide.Meadow(z1);
            var sW = new TileSide.Meadow(z1);
            tiles.add(new Tile(78, Tile.Kind.NORMAL, sN, sE, sS, sW));
        }
        {   // Tile 79
            var z0 = new Zone.Meadow(79_0, List.of(), null);
            var z1 = new Zone.Forest(79_1, Zone.Forest.Kind.WITH_MUSHROOMS);
            var z2 = new Zone.Meadow(79_2, List.of(), null);
            var z3 = new Zone.River(79_3, 0, null);
            var z4 = new Zone.Meadow(79_4, List.of(), null);
            var sN = new TileSide.Meadow(z0);
            var sE = new TileSide.Forest(z1);
            var sS = new TileSide.River(z2, z3, z4);
            var sW = new TileSide.Forest(z1);
            tiles.add(new Tile(79, Tile.Kind.MENHIR, sN, sE, sS, sW));
        }
        {   // Tile 80
            var l1 = new Zone.Lake(80_8, 1, null);
            var z0 = new Zone.Meadow(80_0, List.of(), null);
            var z1 = new Zone.River(80_1, 0, l1);
            var a2_0 = new Animal(80_2_0, Animal.Kind.MAMMOTH);
            var a2_1 = new Animal(80_2_1, Animal.Kind.AUROCHS);
            var z2 = new Zone.Meadow(80_2, List.of(a2_0, a2_1), null);
            var z3 = new Zone.River(80_3, 0, l1);
            var sN = new TileSide.River(z0, z1, z2);
            var sE = new TileSide.Meadow(z2);
            var sS = new TileSide.Meadow(z2);
            var sW = new TileSide.River(z2, z3, z0);
            tiles.add(new Tile(80, Tile.Kind.MENHIR, sN, sE, sS, sW));
        }
        {   // Tile 81
            var a0_0 = new Animal(81_0_0, Animal.Kind.MAMMOTH);
            var a0_1 = new Animal(81_0_1, Animal.Kind.TIGER);
            var z0 = new Zone.Meadow(81_0, List.of(a0_0, a0_1), null);
            var z1 = new Zone.Forest(81_1, Zone.Forest.Kind.WITH_MENHIR);
            var sN = new TileSide.Meadow(z0);
            var sE = new TileSide.Forest(z1);
            var sS = new TileSide.Forest(z1);
            var sW = new TileSide.Meadow(z0);
            tiles.add(new Tile(81, Tile.Kind.MENHIR, sN, sE, sS, sW));
        }
        {   // Tile 82
            var l1 = new Zone.Lake(82_8, 2, null);
            var z0 = new Zone.Meadow(82_0, List.of(), null);
            var z1 = new Zone.River(82_1, 0, l1);
            var a2_0 = new Animal(82_2_0, Animal.Kind.DEER);
            var z2 = new Zone.Meadow(82_2, List.of(a2_0), null);
            var z3 = new Zone.Forest(82_3, Zone.Forest.Kind.PLAIN);
            var sN = new TileSide.River(z0, z1, z2);
            var sE = new TileSide.Meadow(z2);
            var sS = new TileSide.Meadow(z2);
            var sW = new TileSide.Forest(z3);
            tiles.add(new Tile(82, Tile.Kind.MENHIR, sN, sE, sS, sW));
        }
        {   // Tile 83
            var l1 = new Zone.Lake(83_8, 2, null);
            var l2 = new Zone.Lake(83_9, 2, null);
            var a0_0 = new Animal(83_0_0, Animal.Kind.DEER);
            var a0_1 = new Animal(83_0_1, Animal.Kind.DEER);
            var z0 = new Zone.Meadow(83_0, List.of(a0_0, a0_1), null);
            var z1 = new Zone.River(83_1, 0, l1);
            var z2 = new Zone.Meadow(83_2, List.of(), null);
            var z3 = new Zone.River(83_3, 0, l1);
            var z4 = new Zone.River(83_4, 0, l2);
            var z5 = new Zone.Meadow(83_5, List.of(), null);
            var z6 = new Zone.River(83_6, 0, l2);
            var sN = new TileSide.River(z0, z1, z2);
            var sE = new TileSide.River(z2, z3, z0);
            var sS = new TileSide.River(z0, z4, z5);
            var sW = new TileSide.River(z5, z6, z0);
            tiles.add(new Tile(83, Tile.Kind.MENHIR, sN, sE, sS, sW));
        }
        {   // Tile 84
            var z0 = new Zone.Meadow(84_0, List.of(), null);
            var z1 = new Zone.River(84_1, 0, null);
            var a2_0 = new Animal(84_2_0, Animal.Kind.MAMMOTH);
            var z2 = new Zone.Meadow(84_2, List.of(a2_0), null);
            var z3 = new Zone.Forest(84_3, Zone.Forest.Kind.PLAIN);
            var sN = new TileSide.River(z0, z1, z2);
            var sE = new TileSide.River(z2, z1, z0);
            var sS = new TileSide.Forest(z3);
            var sW = new TileSide.Meadow(z0);
            tiles.add(new Tile(84, Tile.Kind.MENHIR, sN, sE, sS, sW));
        }
        {   // Tile 85
            var z0 = new Zone.Meadow(85_0, List.of(), Zone.SpecialPower.WILD_FIRE);
            var sN = new TileSide.Meadow(z0);
            var sE = new TileSide.Meadow(z0);
            var sS = new TileSide.Meadow(z0);
            var sW = new TileSide.Meadow(z0);
            tiles.add(new Tile(85, Tile.Kind.MENHIR, sN, sE, sS, sW));
        }
        {   // Tile 86
            var l1 = new Zone.Lake(86_8, 1, null);
            var a0_0 = new Animal(86_0_0, Animal.Kind.AUROCHS);
            var z0 = new Zone.Meadow(86_0, List.of(a0_0), null);
            var z1 = new Zone.River(86_1, 0, l1);
            var z2 = new Zone.Meadow(86_2, List.of(), null);
            var z3 = new Zone.River(86_3, 0, l1);
            var a4_0 = new Animal(86_4_0, Animal.Kind.TIGER);
            var z4 = new Zone.Meadow(86_4, List.of(a4_0), null);
            var z5 = new Zone.River(86_5, 0, l1);
            var sN = new TileSide.River(z0, z1, z2);
            var sE = new TileSide.River(z2, z3, z4);
            var sS = new TileSide.River(z4, z5, z0);
            var sW = new TileSide.Meadow(z0);
            tiles.add(new Tile(86, Tile.Kind.MENHIR, sN, sE, sS, sW));
        }
        {   // Tile 87
            var l1 = new Zone.Lake(87_8, 2, null);
            var z0 = new Zone.Meadow(87_0, List.of(), null);
            var z1 = new Zone.Forest(87_1, Zone.Forest.Kind.WITH_MUSHROOMS);
            var z2 = new Zone.Meadow(87_2, List.of(), null);
            var z3 = new Zone.River(87_3, 0, l1);
            var sN = new TileSide.Meadow(z0);
            var sE = new TileSide.Forest(z1);
            var sS = new TileSide.Forest(z1);
            var sW = new TileSide.River(z2, z3, z0);
            tiles.add(new Tile(87, Tile.Kind.MENHIR, sN, sE, sS, sW));
        }
        {   // Tile 88
            var z0 = new Zone.Meadow(88_0, List.of(), Zone.SpecialPower.SHAMAN);
            var z1 = new Zone.River(88_1, 1, null);
            var z2 = new Zone.Meadow(88_2, List.of(), null);
            var z3 = new Zone.Forest(88_3, Zone.Forest.Kind.PLAIN);
            var sN = new TileSide.River(z0, z1, z2);
            var sE = new TileSide.River(z2, z1, z0);
            var sS = new TileSide.Forest(z3);
            var sW = new TileSide.Meadow(z0);
            tiles.add(new Tile(88, Tile.Kind.MENHIR, sN, sE, sS, sW));
        }
        {   // Tile 89
            var l1 = new Zone.Lake(89_8, 3, null);
            var z0 = new Zone.Meadow(89_0, List.of(), null);
            var z1 = new Zone.River(89_1, 0, l1);
            var z2 = new Zone.Meadow(89_2, List.of(), null);
            var z3 = new Zone.Forest(89_3, Zone.Forest.Kind.PLAIN);
            var z4 = new Zone.Meadow(89_4, List.of(), null);
            var z5 = new Zone.River(89_5, 0, l1);
            var sN = new TileSide.River(z0, z1, z2);
            var sE = new TileSide.Forest(z3);
            var sS = new TileSide.Forest(z3);
            var sW = new TileSide.River(z4, z5, z0);
            tiles.add(new Tile(89, Tile.Kind.MENHIR, sN, sE, sS, sW));
        }
        {   // Tile 90
            var a0_0 = new Animal(90_0_0, Animal.Kind.MAMMOTH);
            var a0_1 = new Animal(90_0_1, Animal.Kind.AUROCHS);
            var z0 = new Zone.Meadow(90_0, List.of(a0_0, a0_1), null);
            var z1 = new Zone.Forest(90_1, Zone.Forest.Kind.PLAIN);
            var sN = new TileSide.Meadow(z0);
            var sE = new TileSide.Meadow(z0);
            var sS = new TileSide.Forest(z1);
            var sW = new TileSide.Meadow(z0);
            tiles.add(new Tile(90, Tile.Kind.MENHIR, sN, sE, sS, sW));
        }
        {   // Tile 91
            var l1 = new Zone.Lake(91_8, 1, Zone.SpecialPower.RAFT);
            var z0 = new Zone.Meadow(91_0, List.of(), null);
            var z1 = new Zone.River(91_1, 0, l1);
            var z2 = new Zone.Meadow(91_2, List.of(), null);
            var z3 = new Zone.River(91_3, 0, l1);
            var z4 = new Zone.Meadow(91_4, List.of(), null);
            var z5 = new Zone.River(91_5, 0, l1);
            var sN = new TileSide.River(z0, z1, z2);
            var sE = new TileSide.River(z2, z3, z4);
            var sS = new TileSide.Meadow(z4);
            var sW = new TileSide.River(z4, z5, z0);
            tiles.add(new Tile(91, Tile.Kind.MENHIR, sN, sE, sS, sW));
        }
        {   // Tile 92
            var z0 = new Zone.Forest(92_0, Zone.Forest.Kind.PLAIN);
            var z1 = new Zone.Meadow(92_1, List.of(), Zone.SpecialPower.PIT_TRAP);
            var sN = new TileSide.Forest(z0);
            var sE = new TileSide.Forest(z0);
            var sS = new TileSide.Meadow(z1);
            var sW = new TileSide.Meadow(z1);
            tiles.add(new Tile(92, Tile.Kind.MENHIR, sN, sE, sS, sW));
        }
        {   // Tile 93
            var l1 = new Zone.Lake(93_8, 1, Zone.SpecialPower.LOGBOAT);
            var z0 = new Zone.Meadow(93_0, List.of(), null);
            var z1 = new Zone.River(93_1, 0, l1);
            var z2 = new Zone.Meadow(93_2, List.of(), null);
            var z3 = new Zone.River(93_3, 0, l1);
            var z4 = new Zone.Meadow(93_4, List.of(), null);
            var z5 = new Zone.River(93_5, 0, l1);
            var sN = new TileSide.River(z0, z1, z2);
            var sE = new TileSide.River(z2, z3, z4);
            var sS = new TileSide.Meadow(z4);
            var sW = new TileSide.River(z4, z5, z0);
            tiles.add(new Tile(93, Tile.Kind.MENHIR, sN, sE, sS, sW));
        }
        {   // Tile 94
            var z0 = new Zone.Forest(94_0, Zone.Forest.Kind.PLAIN);
            var z1 = new Zone.Meadow(94_1, List.of(), Zone.SpecialPower.HUNTING_TRAP);
            var sN = new TileSide.Forest(z0);
            var sE = new TileSide.Meadow(z1);
            var sS = new TileSide.Meadow(z1);
            var sW = new TileSide.Meadow(z1);
            tiles.add(new Tile(94, Tile.Kind.MENHIR, sN, sE, sS, sW));
        }
        tiles.trimToSize();
        return Collections.unmodifiableList(tiles);
    }

    private static List<Tile> moveTilesToFront(List<Tile> tiles, List<Integer> tileIds) {
        var reorderedTiles = new ArrayList<Tile>(Collections.nCopies(tileIds.size(), null));
        for (var t : tiles) {
            var i = tileIds.indexOf(t.id());
            if (i == -1)
                reorderedTiles.add(t);
            else {
                var oldTile = reorderedTiles.set(i, t);
                assert oldTile == null;
            }
        }
        return List.copyOf(reorderedTiles);
    }

    private static Map<PlayerColor, String> playerNames = Map.of(
            PlayerColor.RED, "Alice",
            PlayerColor.GREEN, "Bob",
            PlayerColor.BLUE, "Cecile",
            PlayerColor.YELLOW, "David",
            PlayerColor.PURPLE, "Eve"
    );
    private static TextMaker textMakerFr = new TextMakerFr(playerNames);

    private static TileDecks tileDecks(List<Integer> firstNormalTiles, List<Integer> firstMenhirTiles) {
        var partitionedTiles = allTiles().stream()
                .collect(Collectors.groupingBy(Tile::kind));

        var normalTiles = moveTilesToFront(partitionedTiles.get(Tile.Kind.NORMAL), firstNormalTiles);
        var menhirTiles = moveTilesToFront(partitionedTiles.get(Tile.Kind.MENHIR), firstMenhirTiles);

        return new TileDecks(
                List.copyOf(partitionedTiles.get(Tile.Kind.START)),
                List.copyOf(normalTiles),
                List.copyOf(menhirTiles));
    }

    private static GameState initialGameState(Map<PlayerColor, String> playerNames, List<Integer> firstNormalTiles, List<Integer> firstMenhirTiles) {
        return initialGameState(playerNames, List.of(PlayerColor.values()), firstNormalTiles, firstMenhirTiles);
    }

    private static GameState initialGameState(
                                            Map<PlayerColor, String> playerNames,
                                            List<PlayerColor> players,
                                          List<Integer> firstNormalTiles,
                                          List<Integer> firstMenhirTiles) {
        var tileDecks = tileDecks(firstNormalTiles, firstMenhirTiles);

        var startingTile = tileDecks.topTile(Tile.Kind.START);
        var firstTileToPlace = tileDecks.topTile(Tile.Kind.NORMAL);
        var tileDecks1 = tileDecks
                .withTopTileDrawn(Tile.Kind.START)
                .withTopTileDrawn(Tile.Kind.NORMAL);
        var placedStartingTile = new PlacedTile(startingTile, null, Rotation.NONE, Pos.ORIGIN);
        var board = Board.EMPTY.withNewTile(placedStartingTile);
        var messageBoard = new MessageBoard(textMakerFr, List.of());
        return new GameState(players, tileDecks1, firstTileToPlace, board, GameState.Action.PLACE_TILE, messageBoard);
    }
}

