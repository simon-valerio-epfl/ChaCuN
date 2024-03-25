package ch.epfl.chacun;

import ch.epfl.chacun.tile.Tiles;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
        List<Tile> normalTiles = List.of(
            Tiles.TILES.get(67),
            Tiles.TILES.get(25)
        );
        List<Tile> menhirTiles = List.of(
            Tiles.TILES.get(88)
        );

        TileDecks startingTileDecks = new TileDecks(startingTiles, normalTiles, menhirTiles);

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
        assertEquals(normalTiles.size() - 1, game.tileDecks().normalTiles().size());
        game = game.withPlacedTile(pt1);

        Occupant occupant67F = new Occupant(Occupant.Kind.HUT, Tiles.TILES.get(67).sideZones().stream().findFirst().get().id());
        GameState finalGame1 = game;
        assertThrows(IllegalArgumentException.class, () -> finalGame1.withNewOccupant(occupant67F));

        Zone.Forest occupiedZone = (Zone.Forest) Tiles.TILES.get(67).zones().stream().filter(z -> z.id() == 670).findFirst().get();
        Occupant occupant67T = new Occupant(Occupant.Kind.PAWN, occupiedZone.id());
        game = game.withNewOccupant(occupant67T);

        assertEquals(1, game.board().occupantCount(PlayerColor.RED, Occupant.Kind.PAWN));
        // check if board
        assertTrue(game.board().occupants().contains(occupant67T));
        // check if tile
        assert game.board().lastPlacedTile() != null;
        assertEquals(occupant67T, game.board().lastPlacedTile().occupant());
        // check if area
        Area<Zone.Forest> forestArea = game.board().forestArea(occupiedZone);
        assertEquals(1, forestArea.occupants().size());

        assertEquals(GameState.Action.PLACE_TILE, game.nextAction());
        assertEquals(PlayerColor.BLUE, game.currentPlayer());
        assertEquals(normalTiles.size() - 2, game.tileDecks().normalTiles().size());

        game = game.withPlacedTile(new PlacedTile(
                Tiles.TILES.get(25),
                PlayerColor.BLUE,
                Rotation.RIGHT,
                new Pos(1, 0)
        ));

        game = game.withNewOccupant(null);

        assertEquals(GameState.Action.PLACE_TILE, game.nextAction());
        assertEquals(PlayerColor.BLUE, game.currentPlayer());
        assertEquals(Tile.Kind.MENHIR, game.tileToPlace().kind());

        assertEquals(Set.of(PlayerColor.RED), game.messageBoard().messages().getFirst().scorers());

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