package ch.epfl.chacun;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class InitialBoardTest {

    @Test
    void testClassIsFinal() {
        assertTrue(Modifier.isFinal(Board.class.getModifiers()));
    }

    @Test
    void testWithNewTile() {
        Zone.River river = new Zone.River(563, 0, null);
        Zone.Meadow meadow = new Zone.Meadow(560, List.of(), null);
        Zone.Meadow meadow1 = new Zone.Meadow(562, List.of(), null);

        TileSide.River riverSide = new TileSide.River(meadow1, river, meadow);
        TileSide.Meadow meadowSide = new TileSide.Meadow(meadow);
        TileSide.Meadow meadowSide1 = new TileSide.Meadow(meadow1);

        Tile tile = new Tile(56, Tile.Kind.NORMAL, riverSide, meadowSide, meadowSide1, meadowSide1);
        PlacedTile placedTile = new PlacedTile(tile, PlayerColor.RED, Rotation.NONE, new Pos(0, 0));

        Board board = Board.EMPTY;

        assertEquals(56, board.withNewTile(placedTile).tileAt(new Pos(0, 0)).id());
    }

    @Test
    void testInsertionPositions() {
        Tile tile56 = TileReader.readTileFromCSV(56);

        PlacedTile placedTile56 = new PlacedTile(tile56, null, Rotation.NONE, new Pos(0, 0));
        Board board = Board.EMPTY.withNewTile(placedTile56);

        assertEquals(4, board.insertionPositions().size());
        assertEquals(Set.of(
                new Pos(0, 1),
                new Pos(1, 0),
                new Pos(0, -1),
                new Pos(-1, 0)
        ), board.insertionPositions());

        // try out of bounds insertion positions
        Tile tile10 = TileReader.readTileFromCSV(10);
        PlacedTile placedTile10 = new PlacedTile(tile10, null, Rotation.NONE, new Pos(-12, -12));
        Board newBoard = Board.EMPTY.withNewTile(placedTile10);

        assertEquals(2, newBoard.insertionPositions().size());
    }

    @Test
    void testCouldPlaceTile(){
        Tile tile29 = TileReader.readTileFromCSV(29);
        Tile tile62 = TileReader.readTileFromCSV(62);
        Tile tile17 = TileReader.readTileFromCSV(17);

        PlacedTile placedTile29 = new PlacedTile(tile29, null, Rotation.NONE, new Pos(0, 0));
        Board board = Board.EMPTY.withNewTile(placedTile29);

        assertFalse(board.couldPlaceTile(tile62));
        assertTrue(board.couldPlaceTile(tile17));
    }

    @Test
    void testAdjacentMeadow() {
        Tile tile62 = TileReader.readTileFromCSV(62);
        Tile tile61 = TileReader.readTileFromCSV(61);
        Tile tile60 = TileReader.readTileFromCSV(60);
        Tile tile94 = TileReader.readTileFromCSV(94);
        Tile tile49 = TileReader.readTileFromCSV(49);
        Tile tile27 = TileReader.readTileFromCSV(27);
        Tile tile42 = TileReader.readTileFromCSV(42);
        Tile tile56 = TileReader.readTileFromCSV(56);

        PlacedTile placedTile62 = new PlacedTile(tile62, PlayerColor.RED, Rotation.NONE, new Pos(0, 0));
        PlacedTile placedTile27 = new PlacedTile(tile27, PlayerColor.RED, Rotation.NONE, new Pos(0, 1));
        PlacedTile placedTile61 = new PlacedTile(tile61, PlayerColor.RED, Rotation.NONE, new Pos(1, 0));
        PlacedTile placedTile49 = new PlacedTile(tile49, PlayerColor.RED, Rotation.NONE, new Pos(1, 1));
        PlacedTile placedTile94 = new PlacedTile(tile94, PlayerColor.RED, Rotation.NONE, new Pos(2, 0));
        PlacedTile placedTile56 = new PlacedTile(tile56, PlayerColor.RED, Rotation.NONE, new Pos(2, 1));
        PlacedTile placedTile60 = new PlacedTile(tile60, PlayerColor.RED, Rotation.RIGHT, new Pos(3, 0));
        PlacedTile placedTile42 = new PlacedTile(tile42, PlayerColor.RED, Rotation.LEFT, new Pos(3, 1));

        Board board = Board.EMPTY;

        board = board
                .withNewTile(placedTile62)
                .withOccupant(new Occupant(Occupant.Kind.PAWN, tile62.zones().stream().filter(zone -> zone.localId() == 0).findFirst().get().id()))
                .withNewTile(placedTile27)
                .withOccupant(new Occupant(Occupant.Kind.PAWN, tile27.zones().stream().filter(zone -> zone.localId() == 2).findFirst().get().id()))
                .withNewTile(placedTile61)
                .withNewTile(placedTile49)
                .withNewTile(placedTile94)
                .withNewTile(placedTile56)
                .withNewTile(placedTile60)
                .withNewTile(placedTile42);

        assertEquals(5, board.adjacentMeadow(new Pos(2, 0), (Zone.Meadow) tile94.zones().stream().filter(zone -> zone.localId() == 1).findFirst().get()).tileIds().size());

        assertEquals(2, board.occupantCount(PlayerColor.RED, Occupant.Kind.PAWN));

        assertEquals(Set.of(
                new Occupant(Occupant.Kind.PAWN, tile27.zones().stream().filter(zone -> zone.localId() == 2).findFirst().get().id()),
                new Occupant(Occupant.Kind.PAWN, tile62.zones().stream().filter(zone -> zone.localId() == 0).findFirst().get().id())
        ), board.occupants());

        board = board.withoutOccupant(new Occupant(Occupant.Kind.PAWN, tile62.zones().stream().filter(zone -> zone.localId() == 0).findFirst().get().id()));
        assertEquals(1, board.occupantCount(PlayerColor.RED, Occupant.Kind.PAWN));

        assertEquals(12, board.insertionPositions().size());

        assertEquals(placedTile42, board.lastPlacedTile());

        assertEquals(Set.of(
                new Occupant(Occupant.Kind.PAWN, tile27.zones().stream().filter(zone -> zone.localId() == 2).findFirst().get().id())
        ), board.occupants());

    }

    @Test
    void testLastPlacedTileOnEmptyBoard() {
        assertNull(Board.EMPTY.lastPlacedTile());
    }

    @Test
    void testUnknownTileAt() {
        assertNull(Board.EMPTY.tileAt(new Pos(0, 0)));
        Board board = Board.EMPTY.withNewTile(new PlacedTile(TileReader.readTileFromCSV(56), PlayerColor.RED, Rotation.NONE, new Pos(0, 0)));
        assertNull(board.tileAt(new Pos(1, 0)));
        assertNull(board.tileAt(new Pos(-10000, 1000)));
    }

    @Test
    void testTileWithId() {
        assertThrows(IllegalArgumentException.class, () -> Board.EMPTY.tileWithId(56));
        Board board = Board.EMPTY.withNewTile(new PlacedTile(TileReader.readTileFromCSV(56), PlayerColor.RED, Rotation.NONE, new Pos(0, 0)));
        assertEquals(56, board.tileWithId(56).id());
        assertThrows(IllegalArgumentException.class, () -> board.tileWithId(100000));
    }

    @Test
    void cancelledAnimals() {
        Board board = Board.EMPTY;
        board = board.withMoreCancelledAnimals(Set.of(new Animal(10, Animal.Kind.DEER)));
        assertEquals(1, board.cancelledAnimals().size());
        // test immutability
        try {
            board.cancelledAnimals().add(new Animal(10, Animal.Kind.MAMMOTH));
        } catch (UnsupportedOperationException e) {
            // expected
        }
        assertEquals(1, board.cancelledAnimals().size());
    }

}
