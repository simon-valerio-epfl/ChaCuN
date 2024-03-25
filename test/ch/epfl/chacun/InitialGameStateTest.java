package ch.epfl.chacun;

import ch.epfl.chacun.tile.Tiles;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InitialGameStateTest {

    PlacedTile getRandomPlacedTile() {
        return new PlacedTile(
                Tiles.TILES.get(67),
                PlayerColor.RED,
                Rotation.NONE,
                new Pos(0, 0)
        );
    }

    @Test
    void testClassFinal() {
        assertTrue(Modifier.isFinal(GameState.class.getModifiers()));
    }

    @Test
    void testConstructor() {
        assertThrows(IllegalArgumentException.class, () ->
                GameState.initial(
                        List.of(PlayerColor.RED),
                        new TileDecks(List.of(), List.of(), List.of()),
                        new TextMakerTestImplementation()
                )
        );
        assertThrows(IllegalArgumentException.class, () ->
                new GameState(
                        List.of(PlayerColor.RED, PlayerColor.BLUE, PlayerColor.GREEN, PlayerColor.YELLOW),
                        new TileDecks(List.of(), List.of(), List.of()),
                        null,
                        Board.EMPTY,
                        GameState.Action.PLACE_TILE,
                        new MessageBoard(new TextMakerTestImplementation(), List.of())
                )
        );
        assertThrows(IllegalArgumentException.class, () ->
                new GameState(
                        List.of(PlayerColor.RED, PlayerColor.BLUE, PlayerColor.GREEN, PlayerColor.YELLOW),
                        new TileDecks(List.of(), List.of(), List.of()),
                        Tiles.TILES.get(1),
                        Board.EMPTY,
                        GameState.Action.OCCUPY_TILE,
                        new MessageBoard(new TextMakerTestImplementation(), List.of())
                )
        );
    }

    @Test
    void playersAreCopied() {
        List<PlayerColor> initialPlayers = List.of(PlayerColor.RED, PlayerColor.BLUE, PlayerColor.GREEN, PlayerColor.YELLOW);
        List<PlayerColor> players = new ArrayList<>(initialPlayers);
        GameState game = GameState.initial(
                players,
                new TileDecks(List.of(), List.of(), List.of()),
                new TextMakerTestImplementation()
        );
        try {
            game.players().clear();
        } catch (UnsupportedOperationException e) {
            // expected
        }
        assertEquals(initialPlayers, game.players());
    }

    @Test
    void withStartingTilePlaced() {
        GameState game = new GameState(
                List.of(PlayerColor.RED, PlayerColor.BLUE, PlayerColor.GREEN, PlayerColor.YELLOW),
                new TileDecks(List.of(), List.of(), List.of()),
                null,
                Board.EMPTY,
                GameState.Action.OCCUPY_TILE,
                new MessageBoard(new TextMakerTestImplementation(), List.of())
        );
        assertThrows(IllegalArgumentException.class, game::withStartingTilePlaced);
    }

    @Test
    void testGame() {

        List<Tile> startingTiles = List.of(Tiles.TILES.get(56));
        List<Tile> normalTiles = Tiles.TILES.stream().filter(t -> t.kind() == Tile.Kind.NORMAL).toList().subList(0, 3);
        List<Tile> menhirTiles = Tiles.TILES.stream().filter(t -> t.kind() == Tile.Kind.MENHIR).toList().subList(0, 3);

        TileDecks startingTileDecks  =new TileDecks(startingTiles, normalTiles, menhirTiles);

        GameState game = GameState.initial(
                List.of(PlayerColor.RED, PlayerColor.BLUE, PlayerColor.GREEN, PlayerColor.YELLOW),
                startingTileDecks,
                new TextMakerTestImplementation()
        );

        assertEquals(GameState.Action.START_GAME, game.nextAction());

        // the game has not started yet
        assertNull(game.currentPlayer());
        assertThrows(IllegalArgumentException.class, game::lastTilePotentialOccupants);
        GameState finalGame = game;
        assertThrows(IllegalArgumentException.class, () -> finalGame.withPlacedTile(getRandomPlacedTile()));

        game = game.withStartingTilePlaced();

        assertEquals(
                startingTileDecks.withTopTileDrawn(Tile.Kind.START).withTopTileDrawn(Tile.Kind.NORMAL),
                game.tileDecks()
        );

        assertEquals(PlayerColor.RED, game.currentPlayer());
        assertEquals(new Pos(0, 0), game.board().lastPlacedTile().pos());

        PlacedTile pt1 = new PlacedTile(
                Tiles.TILES.get(67),
                PlayerColor.RED,
                Rotation.NONE,
                new Pos(0, 1)
        );
        game = game.withPlacedTile(pt1);

        // assertEquals(PlayerColor.BLUE, game.currentPlayer());

        // at the end of the game
        // assert currentPlayer is null
        // assert free occupants count (standard situation + after closing a forest/river)


    }

    @Test
    void habibNoAddAction() {
        assertEquals(5, GameState.Action.values().length);
    }
}