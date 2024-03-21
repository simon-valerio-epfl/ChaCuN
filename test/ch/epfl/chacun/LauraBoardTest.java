package ch.epfl.chacun;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class LauraBoardTest {
    @Test
    void publicAttributeCorrect() {
        assertEquals(Board.REACH, 12);
    }

    @Test
    void tileAtWorksOnEmptyAndOutOfBound() {
        assertNull(Board.EMPTY.tileAt(new Pos(0, 0)));
        assertNull(Board.EMPTY.tileAt(new Pos(-11, 12)));
        assertNull(Board.EMPTY.tileAt(new Pos(15, -26)));
    }

    @Test
    void tileAtWorks() {
        Tile tile = TileReader.readTileFromCSV(56);
        PlacedTile startTile = new PlacedTile(tile, null, Rotation.NONE, new Pos(0, 0));
        Board board = Board.EMPTY.withNewTile(startTile);
        PlacedTile randomTile = new PlacedTile(TileReader.readTileFromCSV(46), PlayerColor.RED, Rotation.HALF_TURN, new Pos(-1, 0));
        board = board.withNewTile(randomTile);
        PlacedTile randomTile2 = new PlacedTile(TileReader.readTileFromCSV(39), PlayerColor.GREEN, Rotation.RIGHT, new Pos(0, 1));
        board = board.withNewTile(randomTile2);
        assertEquals(startTile, board.tileAt(new Pos(0, 0)));
        assertEquals(randomTile, board.tileAt(new Pos(-1, 0)));
        assertEquals(randomTile2, board.tileAt(new Pos(0, 1)));
    }

    @Test
    void tileWithIdThrowsIAE() {
        assertThrows(IllegalArgumentException.class, () -> Board.EMPTY.tileWithId(10));

        Tile tile = TileReader.readTileFromCSV(56);
        PlacedTile startTile = new PlacedTile(tile, null, Rotation.NONE, new Pos(0, 0));
        Board board = Board.EMPTY.withNewTile(startTile);
        PlacedTile randomTile = new PlacedTile(TileReader.readTileFromCSV(46), PlayerColor.RED, Rotation.HALF_TURN, new Pos(-1, 0));
        board = board.withNewTile(randomTile);
        PlacedTile randomTile2 = new PlacedTile(TileReader.readTileFromCSV(39), PlayerColor.GREEN, Rotation.RIGHT, new Pos(0, 1));
        board = board.withNewTile(randomTile2);

        Board finalBoard = board;
        assertThrows(IllegalArgumentException.class, () -> finalBoard.tileWithId(40));
        assertThrows(IllegalArgumentException.class, () -> finalBoard.tileWithId(65));
    }

    @Test
    void tileWithIdWorks() {
        Tile tile = TileReader.readTileFromCSV(56);
        PlacedTile startTile = new PlacedTile(tile, null, Rotation.NONE, new Pos(0, 0));
        Board board = Board.EMPTY.withNewTile(startTile);
        PlacedTile randomTile = new PlacedTile(TileReader.readTileFromCSV(46), PlayerColor.RED, Rotation.HALF_TURN, new Pos(-1, 0));
        board = board.withNewTile(randomTile);
        PlacedTile randomTile2 = new PlacedTile(TileReader.readTileFromCSV(39), PlayerColor.GREEN, Rotation.RIGHT, new Pos(0, 1));
        board = board.withNewTile(randomTile2);

        assertEquals(startTile, board.tileWithId(56));
        assertEquals(randomTile, board.tileWithId(46));
        assertEquals(randomTile2, board.tileWithId(39));
    }

    @Test
    void cancelledAnimalsWorks() {
        assertEquals(Set.of(), Board.EMPTY.cancelledAnimals());
        Set<Animal> cancelled = new HashSet<>();
        cancelled.add(new Animal(5600, Animal.Kind.DEER));
        cancelled.add(new Animal(5611, Animal.Kind.AUROCHS));
        cancelled.add(new Animal(5622, Animal.Kind.TIGER));
        Board board = Board.EMPTY.withMoreCancelledAnimals(Set.copyOf(cancelled));
        assertEquals(cancelled, board.cancelledAnimals());
    }

    @Test
    void cancelledAnimalsIsImmutable() {
        Set<Animal> cancelled = new HashSet<>();
        cancelled.add(new Animal(5600, Animal.Kind.DEER));
        cancelled.add(new Animal(5611, Animal.Kind.AUROCHS));
        cancelled.add(new Animal(5622, Animal.Kind.TIGER));
        Board board = Board.EMPTY.withMoreCancelledAnimals(Set.copyOf(cancelled));
        Set<Animal> cancelled2 = board.cancelledAnimals();
        assertThrows(UnsupportedOperationException.class, () -> cancelled2.add(new Animal(5630, Animal.Kind.TIGER)));
    }

    @Test
    void occupantWorks() {
        assertEquals(Set.of(), Board.EMPTY.occupants());

        Tile tile = TileReader.readTileFromCSV(56);
        PlacedTile startTile = new PlacedTile(tile, null, Rotation.NONE, new Pos(0, 0));
        Board board = Board.EMPTY.withNewTile(startTile);
        PlacedTile randomTile = new PlacedTile(TileReader.readTileFromCSV(46), PlayerColor.RED, Rotation.HALF_TURN, new Pos(-1, 0));
        board = board.withNewTile(randomTile);
        PlacedTile randomTile2 = new PlacedTile(TileReader.readTileFromCSV(39), PlayerColor.GREEN, Rotation.RIGHT, new Pos(0, 1));
        board = board.withNewTile(randomTile2);
        PlacedTile randomTile3 = new PlacedTile(TileReader.readTileFromCSV(9), PlayerColor.GREEN, Rotation.LEFT, new Pos(-2, 0));
        board = board.withNewTile(randomTile3);

        board = board.withOccupant(new Occupant(Occupant.Kind.PAWN, 392));
        board = board.withOccupant(new Occupant(Occupant.Kind.HUT, 98));
        board = board.withOccupant(new Occupant(Occupant.Kind.PAWN, 461));
        Set<Occupant> expected = new HashSet<>();

        expected.add(new Occupant(Occupant.Kind.HUT, 98));
        expected.add(new Occupant(Occupant.Kind.PAWN, 461));
        expected.add(new Occupant(Occupant.Kind.PAWN, 392));
        assertEquals(expected, board.occupants());

        PlacedTile randomTile4 = new PlacedTile(TileReader.readTileFromCSV(22), PlayerColor.GREEN, Rotation.LEFT, new Pos(-2, 1), new Occupant(Occupant.Kind.HUT, 221));
        board = board.withNewTile(randomTile4);
        expected.add(new Occupant(Occupant.Kind.HUT, 221));
        assertEquals(expected, board.occupants());
    }

    @Test
    void insertionPositionsWorks() {
        assertEquals(Set.of(), Board.EMPTY.insertionPositions());
        Set<Pos> posExpected = new HashSet<>();
        Tile tile = TileReader.readTileFromCSV(56);
        PlacedTile startTile = new PlacedTile(tile, null, Rotation.NONE, new Pos(0, 0));
        Board board = Board.EMPTY.withNewTile(startTile);
        posExpected.add(new Pos(0, 1));
        posExpected.add(new Pos(1, 0));
        posExpected.add(new Pos(-1, 0));
        posExpected.add(new Pos(0, -1));
        assertEquals(Set.copyOf(posExpected), board.insertionPositions());
        PlacedTile randomTile = new PlacedTile(TileReader.readTileFromCSV(46), PlayerColor.RED, Rotation.HALF_TURN, new Pos(-1, 0));
        board = board.withNewTile(randomTile);
        posExpected.remove(new Pos(-1, 0));
        posExpected.add(new Pos(-2, 0));
        posExpected.add(new Pos(-1, 1));
        posExpected.add(new Pos(-1, -1));
        assertEquals(Set.copyOf(posExpected), board.insertionPositions());
        PlacedTile randomTile2 = new PlacedTile(TileReader.readTileFromCSV(39), PlayerColor.GREEN, Rotation.RIGHT, new Pos(0, 1));
        board = board.withNewTile(randomTile2);
        posExpected.remove(new Pos(0, 1));
        posExpected.add(new Pos(0,2));
        posExpected.add(new Pos(1,1));
        assertEquals(Set.copyOf(posExpected), board.insertionPositions());
        PlacedTile randomTile3 = new PlacedTile(TileReader.readTileFromCSV(9), PlayerColor.GREEN, Rotation.LEFT, new Pos(-2, 0));
        board = board.withNewTile(randomTile3);
        posExpected.remove(new Pos(-2, 0));
        posExpected.add(new Pos(-3, 0));
        posExpected.add(new Pos(-2, 1));
        posExpected.add(new Pos(-2, -1));
        assertEquals(Set.copyOf(posExpected), board.insertionPositions());
        PlacedTile randomTile4 = new PlacedTile(TileReader.readTileFromCSV(22), PlayerColor.GREEN, Rotation.LEFT, new Pos(-2, 1), new Occupant(Occupant.Kind.HUT, 221));
        board = board.withNewTile(randomTile4);
        posExpected.remove(new Pos(-2, 1));
        posExpected.add(new Pos(-2, 2));
        posExpected.add(new Pos(-3, 1));
        assertEquals(Set.copyOf(posExpected), board.insertionPositions());
    }

    @Test
    void lastPlacedTileWorks() {
        assertNull(Board.EMPTY.lastPlacedTile());
        Tile tile = TileReader.readTileFromCSV(56);
        PlacedTile startTile = new PlacedTile(tile, null, Rotation.NONE, new Pos(0, 0));
        Board board = Board.EMPTY.withNewTile(startTile);
        assertEquals(startTile, board.lastPlacedTile());
        PlacedTile randomTile = new PlacedTile(TileReader.readTileFromCSV(46), PlayerColor.RED, Rotation.HALF_TURN, new Pos(-1, 0));
        board = board.withNewTile(randomTile);
        assertEquals(randomTile, board.lastPlacedTile());
        PlacedTile randomTile2 = new PlacedTile(TileReader.readTileFromCSV(39), PlayerColor.GREEN, Rotation.RIGHT, new Pos(0, 1));
        board = board.withNewTile(randomTile2);
        assertEquals(randomTile2, board.lastPlacedTile());
        PlacedTile randomTile3 = new PlacedTile(TileReader.readTileFromCSV(9), PlayerColor.GREEN, Rotation.LEFT, new Pos(-2, 0));
        board = board.withNewTile(randomTile3);
        assertEquals(randomTile3, board.lastPlacedTile());
        PlacedTile randomTile4 = new PlacedTile(TileReader.readTileFromCSV(22), PlayerColor.GREEN, Rotation.LEFT, new Pos(-2, 1), new Occupant(Occupant.Kind.HUT, 221));
        board = board.withNewTile(randomTile4);
        assertEquals(randomTile4, board.lastPlacedTile());
    }

    @Test
    void forestsClosedByLastTileWorks() {
        assertEquals(Set.of(), Board.EMPTY.forestsClosedByLastTile());
        PlacedTile tile29 = new PlacedTile(TileReader.readTileFromCSV(29), PlayerColor.RED, Rotation.NONE, new Pos(0, 0));
        PlacedTile tile30 = new PlacedTile(TileReader.readTileFromCSV(30), PlayerColor.RED, Rotation.NONE, new Pos(0, -1));
        PlacedTile tile31 = new PlacedTile(TileReader.readTileFromCSV(31), PlayerColor.RED, Rotation.HALF_TURN, new Pos(-1, -1));
        PlacedTile tile32 = new PlacedTile(TileReader.readTileFromCSV(32), PlayerColor.RED, Rotation.RIGHT, new Pos(-1, 0));
        Zone.Forest forest29 = (Zone.Forest) tile29.zoneWithId(290);
        Zone.Forest forest30 = (Zone.Forest) tile30.zoneWithId(301);
        Zone.Forest forest31 = (Zone.Forest) tile31.zoneWithId(310);
        Zone.Forest forest32 = (Zone.Forest) tile32.zoneWithId(320);

        Area<Zone.Forest> area1 = new Area<>(Set.of(forest29, forest30, forest31, forest32), List.of(), 0);
        Set<Area<Zone.Forest>> expected = new HashSet<>();
        expected.add(area1);
        Board board = Board.EMPTY.withNewTile(tile29);
        assertEquals(Set.of(), board.forestsClosedByLastTile());
        board = board.withNewTile(tile30);
        assertEquals(Set.of(), board.forestsClosedByLastTile());
        board = board.withNewTile(tile31);
        assertEquals(Set.of(), board.forestsClosedByLastTile());
        board = board.withNewTile(tile32);
        assertEquals(expected, board.forestsClosedByLastTile());

        PlacedTile tile43 = new PlacedTile(TileReader.readTileFromCSV(43), PlayerColor.RED, Rotation.NONE, new Pos(-1, 0));
        PlacedTile tile45 = new PlacedTile(TileReader.readTileFromCSV(45), PlayerColor.RED, Rotation.NONE, new Pos(0, 1));
        PlacedTile tile55 = new PlacedTile(TileReader.readTileFromCSV(55), PlayerColor.RED, Rotation.NONE, new Pos(-1, 1));

        Zone.Forest forest430 = (Zone.Forest) tile43.zoneWithId(430);
        Zone.Forest forest431 = (Zone.Forest) tile43.zoneWithId(431);
        Zone.Forest forest45 = (Zone.Forest) tile45.zoneWithId(456);
        Zone.Forest forest55 = (Zone.Forest) tile55.zoneWithId(550);

        Area<Zone.Forest> area2 = new Area<>(Set.of(forest29, forest30, forest31, forest430), List.of(), 0);
        Area<Zone.Forest> area3 = new Area<>(Set.of(forest431, forest45, forest55), List.of(), 0);
        expected.clear();
        expected.add(area2);
        expected.add(area3);
        board = Board.EMPTY.withNewTile(tile29).withNewTile(tile30).withNewTile(tile31).withNewTile(tile45).withNewTile(tile55).withNewTile(tile43);
        assertEquals(expected, board.forestsClosedByLastTile());
    }

    @Test
    void riversClosedByLastTileWorks() {
        assertEquals(Set.of(), Board.EMPTY.riversClosedByLastTile());
        PlacedTile tile0 = new PlacedTile(TileReader.readTileFromCSV(0), PlayerColor.RED, Rotation.NONE, new Pos(0, 0));
        PlacedTile tile1 = new PlacedTile(TileReader.readTileFromCSV(1), PlayerColor.RED, Rotation.RIGHT, new Pos(0, -1));
        Board board = Board.EMPTY.withNewTile(tile0);
        assertEquals(Set.of(), board.riversClosedByLastTile());
        board = board.withNewTile(tile1);
        Zone.River river0 = (Zone.River) tile0.zoneWithId(1);
        Zone.River river1 = (Zone.River) tile1.zoneWithId(11);
        Area<Zone.River> area1 = new Area<>(Set.of(river0, river1), List.of(), 0);
        Set<Area<Zone.River>> expected = new HashSet<>();
        expected.add(area1);
        assertEquals(expected, board.riversClosedByLastTile());

        PlacedTile tile6 = new PlacedTile(TileReader.readTileFromCSV(6), PlayerColor.RED, Rotation.NONE, new Pos(0, 0));
        PlacedTile tile21 = new PlacedTile(TileReader.readTileFromCSV(21), PlayerColor.RED, Rotation.NONE, new Pos(0, -1));
        PlacedTile tile22 = new PlacedTile(TileReader.readTileFromCSV(22), PlayerColor.RED, Rotation.NONE, new Pos(1, 0));
        PlacedTile tile5 = new PlacedTile(TileReader.readTileFromCSV(5), PlayerColor.RED, Rotation.NONE, new Pos(1, -1));

        Zone.River river61 = (Zone.River) tile6.zoneWithId(61);
        Zone.River river63 = (Zone.River) tile6.zoneWithId(63);
        Zone.River river21 = (Zone.River) tile21.zoneWithId(211);
        Zone.River river22 = (Zone.River) tile22.zoneWithId(221);
        Zone.River river53 = (Zone.River) tile5.zoneWithId(53);
        Zone.River river55 = (Zone.River) tile5.zoneWithId(55);
        Area<Zone.River> area2 = new Area<>(Set.of(river61, river21, river55), List.of(), 0);
        Area<Zone.River> area3 = new Area<>(Set.of(river63, river22, river53), List.of(), 0);
        expected.clear();
        expected.add(area2);
        expected.add(area3);
        board = Board.EMPTY.withNewTile(tile6).withNewTile(tile21).withNewTile(tile22).withNewTile(tile5);
        assertEquals(expected, board.riversClosedByLastTile());

        tile5 = new PlacedTile(TileReader.readTileFromCSV(5), PlayerColor.RED, Rotation.NONE, new Pos(0, 0));
        PlacedTile tile10 = new PlacedTile(TileReader.readTileFromCSV(10), PlayerColor.RED, Rotation.NONE, new Pos(0, 1));
        PlacedTile tile23 = new PlacedTile(TileReader.readTileFromCSV(23), PlayerColor.RED, Rotation.NONE, new Pos(-1, 1));
        PlacedTile tile16 = new PlacedTile(TileReader.readTileFromCSV(16), PlayerColor.RED, Rotation.NONE, new Pos(-1, 0));
        tile22 = new PlacedTile(TileReader.readTileFromCSV(22), PlayerColor.RED, Rotation.NONE, new Pos(1, 1));
        PlacedTile tile17 = new PlacedTile(TileReader.readTileFromCSV(17), PlayerColor.RED, Rotation.NONE, new Pos(1, 0));

        Zone.River river51 = (Zone.River) tile5.zoneWithId(51);
        Zone.River river101 = (Zone.River) tile10.zoneWithId(101);
        Zone.River river103 = (Zone.River) tile10.zoneWithId(103);
        Zone.River river105 = (Zone.River) tile10.zoneWithId(105);
        Zone.River river23 = (Zone.River) tile23.zoneWithId(231);
        Zone.River river161 = (Zone.River) tile16.zoneWithId(161);
        Zone.River river173 = (Zone.River) tile17.zoneWithId(173);

        Area<Zone.River> area4 = new Area<>(Set.of(river51, river103, river173, river22), List.of(), 0);
        Area<Zone.River> area5 = new Area<>(Set.of(river101, river53), List.of(), 0);
        Area<Zone.River> area6 = new Area<>(Set.of(river55, river161, river23, river105), List.of(), 0);
        expected.clear();
        expected.add(area4);
        expected.add(area5);
        expected.add(area6);
        board = Board.EMPTY.withNewTile(tile10).withNewTile(tile23).withNewTile(tile16).withNewTile(tile22).withNewTile(tile17).withNewTile(tile5);
        assertEquals(expected, board.riversClosedByLastTile());

        tile6 = new PlacedTile(TileReader.readTileFromCSV(6), PlayerColor.RED, Rotation.LEFT, new Pos(1, 0));
        Zone.River river65 = (Zone.River) tile6.zoneWithId(65);
        Area<Zone.River> area7 = new Area<>(Set.of(river61, river51), List.of(), 0);
        Area<Zone.River> area8 = new Area<>(Set.of(river65, river103, river22), List.of(), 0);
        expected.clear();
        expected.add(area7);
        expected.add(area6);
        expected.add(area5);
        board = Board.EMPTY.withNewTile(tile10).withNewTile(tile23).withNewTile(tile16).withNewTile(tile22).withNewTile(tile6);
        assertEquals(Set.of(area8), board.riversClosedByLastTile());
        board = board.withNewTile(tile5);
        assertEquals(expected, board.riversClosedByLastTile());
    }

    @Test
    void canAddTileWorks() {
        PlacedTile startTile = new PlacedTile(TileReader.readTileFromCSV(56), PlayerColor.RED, Rotation.NONE, new Pos(0, 0));
        assertFalse(Board.EMPTY.canAddTile(startTile));
        Board board = Board.EMPTY.withNewTile(startTile);

        PlacedTile randomTile = new PlacedTile(TileReader.readTileFromCSV(46), PlayerColor.RED, Rotation.HALF_TURN, new Pos(-1, 0));
        assertTrue(board.canAddTile(randomTile));
        board = board.withNewTile(randomTile);
        PlacedTile randomTile2 = new PlacedTile(TileReader.readTileFromCSV(32), PlayerColor.GREEN, Rotation.NONE, new Pos(0, 1));
        assertTrue(board.canAddTile(randomTile2));
        board = board.withNewTile(randomTile2);
        PlacedTile randomTile3 = new PlacedTile(TileReader.readTileFromCSV(9), PlayerColor.GREEN, Rotation.LEFT, new Pos(-2, 0));
        assertTrue(board.canAddTile(randomTile3));
        board = board.withNewTile(randomTile3);
        PlacedTile randomTile4 = new PlacedTile(TileReader.readTileFromCSV(22), PlayerColor.GREEN, Rotation.LEFT, new Pos(-2, 0));
        assertFalse(board.canAddTile(randomTile4));
        randomTile4 = new PlacedTile(TileReader.readTileFromCSV(22), PlayerColor.GREEN, Rotation.LEFT, new Pos(-2, 1));
        assertTrue(board.canAddTile(randomTile4));

        PlacedTile tile37 = new PlacedTile(TileReader.readTileFromCSV(37), PlayerColor.RED, Rotation.NONE, new Pos(-1, -1));
        assertFalse(board.canAddTile(tile37));
        tile37 = new PlacedTile(TileReader.readTileFromCSV(37), PlayerColor.RED, Rotation.HALF_TURN, new Pos(-1, -1));
        assertTrue(board.canAddTile(tile37));

        PlacedTile tile40 = new PlacedTile(TileReader.readTileFromCSV(35), PlayerColor.RED, Rotation.NONE, new Pos(0, 2));
        board = board.withNewTile(tile40);

        PlacedTile tile41 = new PlacedTile(TileReader.readTileFromCSV(41), PlayerColor.RED, Rotation.RIGHT, new Pos(-1, 2));
        board = board.withNewTile(tile41);
        PlacedTile tile37_2 = new PlacedTile(TileReader.readTileFromCSV(37), PlayerColor.RED, Rotation.NONE, new Pos(-2, 2));
        board = board.withNewTile(tile37_2);
        PlacedTile tile45 = new PlacedTile(TileReader.readTileFromCSV(47), PlayerColor.RED, Rotation.NONE, new Pos(-2, 1));
        board = board.withNewTile(tile45);
        PlacedTile tile66 = new PlacedTile(TileReader.readTileFromCSV(66), PlayerColor.RED, Rotation.RIGHT, new Pos(-1, 1));
        assertTrue(board.canAddTile(tile66));
    }

    @Test
    void couldPlaceTileWorks() {
        assertFalse(Board.EMPTY.couldPlaceTile(TileReader.readTileFromCSV(56)));
        PlacedTile startTile = new PlacedTile(TileReader.readTileFromCSV(56), PlayerColor.RED, Rotation.NONE, new Pos(0, 0));
        Board board = Board.EMPTY.withNewTile(startTile);

        PlacedTile randomTile = new PlacedTile(TileReader.readTileFromCSV(46), PlayerColor.RED, Rotation.HALF_TURN, new Pos(-1, 0));
        assertTrue(board.couldPlaceTile(randomTile.tile()));
        board = board.withNewTile(randomTile);
        PlacedTile randomTile2 = new PlacedTile(TileReader.readTileFromCSV(32), PlayerColor.GREEN, Rotation.NONE, new Pos(0, 1));
        assertTrue(board.couldPlaceTile(randomTile2.tile()));
        board = board.withNewTile(randomTile2);
        PlacedTile randomTile3 = new PlacedTile(TileReader.readTileFromCSV(9), PlayerColor.GREEN, Rotation.LEFT, new Pos(-2, 0));
        assertTrue(board.couldPlaceTile(randomTile3.tile()));
        board = board.withNewTile(randomTile3);

        Board board1 = Board.EMPTY.withNewTile(randomTile2);
        PlacedTile tile13 = new PlacedTile(TileReader.readTileFromCSV(13), PlayerColor.RED, Rotation.NONE, new Pos(0, 2));
        assertFalse(board1.couldPlaceTile(tile13.tile()));


        PlacedTile tile43 = new PlacedTile(TileReader.readTileFromCSV(43), PlayerColor.RED, Rotation.LEFT, new Pos(-1, 0));
        PlacedTile tile44 = new PlacedTile(TileReader.readTileFromCSV(44), PlayerColor.RED, Rotation.HALF_TURN, new Pos(-1, 1));

        board = Board.EMPTY.withNewTile(tile43);
        assertTrue(board.couldPlaceTile(tile44.tile()));
        board = board.withNewTile(tile44);
        assertFalse(board.couldPlaceTile(randomTile3.tile()));
        assertFalse(board.couldPlaceTile(TileReader.readTileFromCSV(61)));
        assertFalse(board.couldPlaceTile(TileReader.readTileFromCSV(62)));
    }

    @Test
    void withNewTileThrows() {

        PlacedTile tile43 = new PlacedTile(TileReader.readTileFromCSV(43), PlayerColor.RED, Rotation.LEFT, new Pos(-1, 0));
        PlacedTile tile44 = new PlacedTile(TileReader.readTileFromCSV(44), PlayerColor.RED, Rotation.HALF_TURN, new Pos(-1, 1));

        Board board = Board.EMPTY.withNewTile(tile43);
        board = board.withNewTile(tile44);

        Board finalBoard = board;
        assertThrows(IllegalArgumentException.class, () -> finalBoard.withNewTile(tile44));
        PlacedTile randomTile3 = new PlacedTile(TileReader.readTileFromCSV(9), PlayerColor.GREEN, Rotation.LEFT, new Pos(-2, 0));
        assertThrows(IllegalArgumentException.class, () -> finalBoard.withNewTile(randomTile3));

        PlacedTile tile61 = new PlacedTile(TileReader.readTileFromCSV(61), PlayerColor.RED, Rotation.NONE, new Pos(-2, 1));
        PlacedTile tile62 = new PlacedTile(TileReader.readTileFromCSV(62), PlayerColor.RED, Rotation.NONE, new Pos(-2, 2));

        assertThrows(IllegalArgumentException.class, () -> finalBoard.withNewTile(tile61));
        assertThrows(IllegalArgumentException.class, () -> finalBoard.withNewTile(tile62));

        PlacedTile startTile = new PlacedTile(TileReader.readTileFromCSV(56), PlayerColor.RED, Rotation.NONE, new Pos(0, 0));
        assertFalse(Board.EMPTY.canAddTile(startTile));
        board = Board.EMPTY.withNewTile(startTile);

        PlacedTile randomTile = new PlacedTile(TileReader.readTileFromCSV(46), PlayerColor.RED, Rotation.RIGHT, new Pos(-1, 0));
        Board finalBoard1 = board;
        PlacedTile finalRandomTile = randomTile;
        assertThrows(IllegalArgumentException.class, () -> finalBoard1.withNewTile(finalRandomTile));
        randomTile = new PlacedTile(TileReader.readTileFromCSV(46), PlayerColor.RED, Rotation.HALF_TURN, new Pos(-1, 0));
        board = board.withNewTile(randomTile);

        PlacedTile randomTile2 = new PlacedTile(TileReader.readTileFromCSV(32), PlayerColor.GREEN, Rotation.HALF_TURN, new Pos(0, 1));
        Board finalBoard2 = board;
        PlacedTile finalRandomTile1 = randomTile2;
        assertThrows(IllegalArgumentException.class, () -> finalBoard2.withNewTile(finalRandomTile1));
        randomTile2 = new PlacedTile(TileReader.readTileFromCSV(32), PlayerColor.GREEN, Rotation.NONE, new Pos(0, 1));
        board = board.withNewTile(randomTile2);

        PlacedTile randomTile5 = new PlacedTile(TileReader.readTileFromCSV(9), PlayerColor.GREEN, Rotation.HALF_TURN, new Pos(-2, 0));
        Board finalBoard4 = board;
        PlacedTile finalRandomTile3 = randomTile5;
        assertThrows(IllegalArgumentException.class, () -> finalBoard4.withNewTile(finalRandomTile3));
        randomTile5 = new PlacedTile(TileReader.readTileFromCSV(9), PlayerColor.GREEN, Rotation.LEFT, new Pos(-2, 0));
        board = board.withNewTile(randomTile5);

        PlacedTile finalRandomTile2 = new PlacedTile(TileReader.readTileFromCSV(22), PlayerColor.GREEN, Rotation.LEFT, new Pos(-2, 0));
        Board finalBoard3 = board;
        assertThrows(IllegalArgumentException.class, () -> finalBoard3.withNewTile(finalRandomTile2));


        PlacedTile tile37 = new PlacedTile(TileReader.readTileFromCSV(37), PlayerColor.RED, Rotation.NONE, new Pos(-1, -1));
        PlacedTile finalTile3 = tile37;
        assertThrows(IllegalArgumentException.class, () -> finalBoard3.withNewTile(finalTile3));
        tile37 = new PlacedTile(TileReader.readTileFromCSV(37), PlayerColor.RED, Rotation.HALF_TURN, new Pos(-1, -1));
        PlacedTile finalTile31 = tile37;
        assertDoesNotThrow(() -> finalBoard3.withNewTile(finalTile31));

        PlacedTile tile35 = new PlacedTile(TileReader.readTileFromCSV(35), PlayerColor.RED, Rotation.LEFT, new Pos(0, 2));
        PlacedTile finalTile4 = tile35;
        assertThrows(IllegalArgumentException.class, () -> finalBoard3.withNewTile(finalTile4));
        tile35 = new PlacedTile(TileReader.readTileFromCSV(35), PlayerColor.RED, Rotation.NONE, new Pos(0, 2));
        board = board.withNewTile(tile35);

        PlacedTile tile41 = new PlacedTile(TileReader.readTileFromCSV(41), PlayerColor.RED, Rotation.RIGHT, new Pos(-1, 2));
        board = board.withNewTile(tile41);

        PlacedTile tile37_2 = new PlacedTile(TileReader.readTileFromCSV(37), PlayerColor.RED, Rotation.RIGHT, new Pos(-2, 2));
        PlacedTile finalTile37_ = tile37_2;
        Board finalBoard5 = board;
        assertThrows(IllegalArgumentException.class, () -> finalBoard5.withNewTile(finalTile37_));
        tile37_2 = new PlacedTile(TileReader.readTileFromCSV(37), PlayerColor.RED, Rotation.NONE, new Pos(-2, 2));
        board = board.withNewTile(tile37_2);

        PlacedTile tile45 = new PlacedTile(TileReader.readTileFromCSV(47), PlayerColor.RED, Rotation.RIGHT, new Pos(-2, 1));
        Board finalBoard6 = board;
        PlacedTile finalTile41 = tile45;
        assertThrows(IllegalArgumentException.class, () -> finalBoard6.withNewTile(finalTile41));
        tile45 = new PlacedTile(TileReader.readTileFromCSV(47), PlayerColor.RED, Rotation.NONE, new Pos(-2, 1));
        board = board.withNewTile(tile45);

        PlacedTile tile66 = new PlacedTile(TileReader.readTileFromCSV(66), PlayerColor.RED, Rotation.HALF_TURN, new Pos(-1, 1));
        Board finalBoard7 = board;
        PlacedTile finalTile6 = tile66;
        assertThrows(IllegalArgumentException.class, () -> finalBoard7.withNewTile(finalTile6));
        tile66 = new PlacedTile(TileReader.readTileFromCSV(66), PlayerColor.RED, Rotation.RIGHT, new Pos(-1, 1));
        PlacedTile finalTile61 = tile66;
        Board finalBoard8 = board;
        assertDoesNotThrow(() -> finalBoard8.withNewTile(finalTile61));
    }

    @Test
    void withOccupantThrows() {
        Tile tile = TileReader.readTileFromCSV(56);
        PlacedTile startTile = new PlacedTile(tile, null, Rotation.NONE, new Pos(0, 0));
        Board board = Board.EMPTY.withNewTile(startTile);
        PlacedTile randomTile = new PlacedTile(TileReader.readTileFromCSV(46), PlayerColor.RED, Rotation.HALF_TURN, new Pos(-1, 0));
        board = board.withNewTile(randomTile);
        PlacedTile randomTile2 = new PlacedTile(TileReader.readTileFromCSV(39), PlayerColor.GREEN, Rotation.RIGHT, new Pos(0, 1));
        board = board.withNewTile(randomTile2);
        PlacedTile randomTile3 = new PlacedTile(TileReader.readTileFromCSV(9), PlayerColor.GREEN, Rotation.LEFT, new Pos(-2, 0));
        board = board.withNewTile(randomTile3);

        board = board.withOccupant(new Occupant(Occupant.Kind.PAWN, 392));
        board = board.withOccupant(new Occupant(Occupant.Kind.HUT, 98));
        board = board.withOccupant(new Occupant(Occupant.Kind.PAWN, 461));
        Board finalBoard = board;
        assertThrows(IllegalArgumentException.class, () -> finalBoard.withOccupant(new Occupant(Occupant.Kind.PAWN, 392)));
        assertThrows(IllegalArgumentException.class, () -> finalBoard.withOccupant(new Occupant(Occupant.Kind.HUT, 98)));
        assertThrows(IllegalArgumentException.class, () -> finalBoard.withOccupant(new Occupant(Occupant.Kind.PAWN, 461)));


        PlacedTile randomTile4 = new PlacedTile(TileReader.readTileFromCSV(22), PlayerColor.GREEN, Rotation.LEFT, new Pos(-2, 1), new Occupant(Occupant.Kind.HUT, 221));
        board = board.withNewTile(randomTile4);
        Board finalBoard1 = board;
        assertThrows(IllegalArgumentException.class, () -> finalBoard1.withOccupant(new Occupant(Occupant.Kind.HUT, 221)));
    }

    @Test
    void withoutOccupantWorks() {
        Tile tile = TileReader.readTileFromCSV(56);
        PlacedTile startTile = new PlacedTile(tile, null, Rotation.NONE, new Pos(0, 0));
        Board board = Board.EMPTY.withNewTile(startTile);
        PlacedTile randomTile = new PlacedTile(TileReader.readTileFromCSV(46), PlayerColor.RED, Rotation.HALF_TURN, new Pos(-1, 0));
        board = board.withNewTile(randomTile);
        PlacedTile randomTile2 = new PlacedTile(TileReader.readTileFromCSV(39), PlayerColor.GREEN, Rotation.RIGHT, new Pos(0, 1));
        board = board.withNewTile(randomTile2);
        PlacedTile randomTile3 = new PlacedTile(TileReader.readTileFromCSV(9), PlayerColor.GREEN, Rotation.LEFT, new Pos(-2, 0));
        board = board.withNewTile(randomTile3);

        board = board.withOccupant(new Occupant(Occupant.Kind.PAWN, 392));
        board = board.withOccupant(new Occupant(Occupant.Kind.PAWN, 94));
        board = board.withOccupant(new Occupant(Occupant.Kind.PAWN, 461));
        Set<Occupant> expected = new HashSet<>();

        expected.add(new Occupant(Occupant.Kind.PAWN, 94));
        expected.add(new Occupant(Occupant.Kind.PAWN, 461));
        expected.add(new Occupant(Occupant.Kind.PAWN, 392));
        assertEquals(expected, board.occupants());
        ;

        board = board.withoutOccupant(new Occupant(Occupant.Kind.PAWN, 392));
        expected.remove(new Occupant(Occupant.Kind.PAWN, 392));
        assertEquals(expected, board.occupants());
        board = board.withoutOccupant(new Occupant(Occupant.Kind.PAWN, 461));
        expected.remove(new Occupant(Occupant.Kind.PAWN, 461));
        assertEquals(expected, board.occupants());
        board = board.withoutOccupant(new Occupant(Occupant.Kind.HUT, 94));
        expected.remove(new Occupant(Occupant.Kind.PAWN, 94));
        assertEquals(expected, board.occupants());
        assertEquals(Set.of(), board.occupants());
    }

    @Test
    void withoutGatherersOrFishersInWorks() {
        PlacedTile startTile = new PlacedTile(TileReader.readTileFromCSV(56), PlayerColor.RED, Rotation.NONE, new Pos(0, 0));
        Board board = Board.EMPTY.withNewTile(startTile);
        PlacedTile tile38 = new PlacedTile(TileReader.readTileFromCSV(38), PlayerColor.RED, Rotation.NONE, new Pos(1, 0), new Occupant(Occupant.Kind.PAWN, 381));
        board = board.withNewTile(tile38);
        PlacedTile tile41 = new PlacedTile(TileReader.readTileFromCSV(41), PlayerColor.RED, Rotation.NONE, new Pos(2, 0), new Occupant(Occupant.Kind.PAWN, 412));
        board = board.withNewTile(tile41);
        PlacedTile tile35 = new PlacedTile(TileReader.readTileFromCSV(35), PlayerColor.RED, Rotation.HALF_TURN, new Pos(3, 0), new Occupant(Occupant.Kind.PAWN, 351));
        board = board.withNewTile(tile35);
        PlacedTile tile67 = new PlacedTile(TileReader.readTileFromCSV(67), PlayerColor.RED, Rotation.NONE, new Pos(0, 1), new Occupant(Occupant.Kind.PAWN, 670));
        board = board.withNewTile(tile67);
        PlacedTile tile51 = new PlacedTile(TileReader.readTileFromCSV(51), PlayerColor.RED, Rotation.NONE, new Pos(-1, 0), new Occupant(Occupant.Kind.PAWN, 511));
        board = board.withNewTile(tile51);
        PlacedTile tile1 = new PlacedTile(TileReader.readTileFromCSV(1), PlayerColor.RED, Rotation.NONE, new Pos(-2, 0), new Occupant(Occupant.Kind.PAWN, 11));
        board = board.withNewTile(tile1);
        PlacedTile tile17 = new PlacedTile(TileReader.readTileFromCSV(17), PlayerColor.RED, Rotation.NONE, new Pos(-3, 0), new Occupant(Occupant.Kind.PAWN, 171));
        board = board.withNewTile(tile17);
        PlacedTile tile21 = new PlacedTile(TileReader.readTileFromCSV(21), PlayerColor.RED, Rotation.NONE, new Pos(-3, -1), new Occupant(Occupant.Kind.PAWN, 211));
        board = board.withNewTile(tile21);
        PlacedTile tile37 = new PlacedTile(TileReader.readTileFromCSV(37), PlayerColor.RED, Rotation.NONE, new Pos(0, -1), new Occupant(Occupant.Kind.PAWN, 370));
        board = board.withNewTile(tile37);
        PlacedTile tile25 = new PlacedTile(TileReader.readTileFromCSV(25), PlayerColor.RED, Rotation.NONE, new Pos(0, -2), new Occupant(Occupant.Kind.PAWN, 253));
        board = board.withNewTile(tile25);
        PlacedTile tile22 = new PlacedTile(TileReader.readTileFromCSV(22), PlayerColor.RED, Rotation.HALF_TURN, new Pos(-2, 1), new Occupant(Occupant.Kind.PAWN, 223));
        board = board.withNewTile(tile22);

        Zone.Forest forest56 = (Zone.Forest) startTile.zoneWithId(561);
        Zone.Forest forest38 = (Zone.Forest) tile38.zoneWithId(381);
        Zone.Forest forest412 = (Zone.Forest) tile41.zoneWithId(412);
        Zone.Forest forest670 = (Zone.Forest) tile67.zoneWithId(670);

        Zone.Forest forest41 = (Zone.Forest) tile41.zoneWithId(411);
        Zone.Forest forest35 = (Zone.Forest) tile35.zoneWithId(351);

        Zone.Forest forest25 = (Zone.Forest) tile25.zoneWithId(253);
        Zone.Forest forest37 = (Zone.Forest) tile37.zoneWithId(370);

        Zone.River river56 = (Zone.River) startTile.zoneWithId(563);
        Zone.River river51 = (Zone.River) tile51.zoneWithId(511);
        Zone.River river11 = (Zone.River) tile1.zoneWithId(11);

        Zone.River river15 = (Zone.River) tile1.zoneWithId(15);
        Zone.River river17 = (Zone.River) tile17.zoneWithId(171);
        Zone.River river211 = (Zone.River) tile21.zoneWithId(211);

        Area<Zone.Forest> forestArea1 = new Area<>(Set.of(forest56, forest38, forest412, forest670), List.of(), 0);
        Area<Zone.Forest> forestArea2 = new Area<>(Set.of(forest41, forest35), List.of(), 0);
        Area<Zone.Forest> forestArea3 = new Area<>(Set.of(forest25, forest37), List.of(), 0);

        Area<Zone.River> riverArea1 = new Area<>(Set.of(river56, river51, river11), List.of(), 0);
        Area<Zone.River> riverArea2 = new Area<>(Set.of(river15, river17, river211), List.of(), 1);

        Set<Area<Zone.Forest>> expectedForests = new HashSet<>();
        expectedForests.add(forestArea1);
        expectedForests.add(forestArea2);
        expectedForests.add(forestArea3);
        Set<Area<Zone.River>> expectedRivers = new HashSet<>();
        expectedRivers.add(riverArea1);
        expectedRivers.add(riverArea2);
        board = board.withoutGatherersOrFishersIn(expectedForests, expectedRivers);
        assertEquals(Set.of(new Occupant(Occupant.Kind.PAWN, 223)), board.occupants());
    }

    @Test
    void withMoreCancelledAnimalsWorks() {
        Set<Animal> cancelled = new HashSet<>();
        cancelled.add(new Animal(5600, Animal.Kind.DEER));
        cancelled.add(new Animal(5611, Animal.Kind.AUROCHS));
        cancelled.add(new Animal(5622, Animal.Kind.TIGER));
        Board board = Board.EMPTY.withMoreCancelledAnimals(Set.copyOf(cancelled));
        assertEquals(cancelled, board.cancelledAnimals());
    }

    @Test
    void forestAreaWorks() {
        PlacedTile startTile = new PlacedTile(TileReader.readTileFromCSV(56), PlayerColor.RED, Rotation.NONE, new Pos(0, 0));
        Board board = Board.EMPTY.withNewTile(startTile);
        PlacedTile tile38 = new PlacedTile(TileReader.readTileFromCSV(38), PlayerColor.RED, Rotation.NONE, new Pos(1, 0), new Occupant(Occupant.Kind.PAWN, 381));
        board = board.withNewTile(tile38);
        PlacedTile tile41 = new PlacedTile(TileReader.readTileFromCSV(41), PlayerColor.RED, Rotation.NONE, new Pos(2, 0), new Occupant(Occupant.Kind.PAWN, 412));
        board = board.withNewTile(tile41);
        PlacedTile tile35 = new PlacedTile(TileReader.readTileFromCSV(35), PlayerColor.RED, Rotation.HALF_TURN, new Pos(3, 0), new Occupant(Occupant.Kind.PAWN, 351));
        board = board.withNewTile(tile35);
        PlacedTile tile67 = new PlacedTile(TileReader.readTileFromCSV(67), PlayerColor.RED, Rotation.NONE, new Pos(0, 1), new Occupant(Occupant.Kind.PAWN, 670));
        board = board.withNewTile(tile67);
        PlacedTile tile51 = new PlacedTile(TileReader.readTileFromCSV(51), PlayerColor.RED, Rotation.NONE, new Pos(-1, 0), new Occupant(Occupant.Kind.PAWN, 511));
        board = board.withNewTile(tile51);
        PlacedTile tile1 = new PlacedTile(TileReader.readTileFromCSV(1), PlayerColor.RED, Rotation.NONE, new Pos(-2, 0), new Occupant(Occupant.Kind.PAWN, 11));
        board = board.withNewTile(tile1);
        PlacedTile tile17 = new PlacedTile(TileReader.readTileFromCSV(17), PlayerColor.RED, Rotation.NONE, new Pos(-3, 0), new Occupant(Occupant.Kind.PAWN, 171));
        board = board.withNewTile(tile17);
        PlacedTile tile21 = new PlacedTile(TileReader.readTileFromCSV(21), PlayerColor.RED, Rotation.NONE, new Pos(-3, -1), new Occupant(Occupant.Kind.PAWN, 211));
        board = board.withNewTile(tile21);
        PlacedTile tile37 = new PlacedTile(TileReader.readTileFromCSV(37), PlayerColor.RED, Rotation.NONE, new Pos(0, -1), new Occupant(Occupant.Kind.PAWN, 370));
        board = board.withNewTile(tile37);
        PlacedTile tile25 = new PlacedTile(TileReader.readTileFromCSV(25), PlayerColor.RED, Rotation.NONE, new Pos(0, -2), new Occupant(Occupant.Kind.PAWN, 253));
        board = board.withNewTile(tile25);
        PlacedTile tile22 = new PlacedTile(TileReader.readTileFromCSV(22), PlayerColor.RED, Rotation.HALF_TURN, new Pos(-2, 1), new Occupant(Occupant.Kind.PAWN, 223));
        board = board.withNewTile(tile22);

        Zone.Forest forest56 = (Zone.Forest) startTile.zoneWithId(561);
        Zone.Forest forest38 = (Zone.Forest) tile38.zoneWithId(381);
        Zone.Forest forest412 = (Zone.Forest) tile41.zoneWithId(412);
        Zone.Forest forest670 = (Zone.Forest) tile67.zoneWithId(670);

        Zone.Forest forest41 = (Zone.Forest) tile41.zoneWithId(411);
        Zone.Forest forest35 = (Zone.Forest) tile35.zoneWithId(351);

        Zone.Forest forest25 = (Zone.Forest) tile25.zoneWithId(253);
        Zone.Forest forest37 = (Zone.Forest) tile37.zoneWithId(370);


        Area<Zone.Forest> forestArea1 = new Area<>(Set.of(forest56, forest38, forest412, forest670), List.of(), 0);
        Area<Zone.Forest> forestArea2 = new Area<>(Set.of(forest41, forest35), List.of(), 0);
        Area<Zone.Forest> forestArea3 = new Area<>(Set.of(forest25, forest37), List.of(), 0);

        assertEquals(forestArea1, board.forestArea(forest56));
        assertEquals(forestArea1, board.forestArea(forest38));
        assertEquals(forestArea1, board.forestArea(forest412));
        assertEquals(forestArea1, board.forestArea(forest670));
        assertEquals(forestArea2, board.forestArea(forest41));
        assertEquals(forestArea2, board.forestArea(forest35));
        assertEquals(forestArea3, board.forestArea(forest25));
        assertEquals(forestArea3, board.forestArea(forest37));

    }

    @Test
    void forestAreaThrows() {
        PlacedTile startTile = new PlacedTile(TileReader.readTileFromCSV(56), PlayerColor.RED, Rotation.NONE, new Pos(0, 0));
        Board board = Board.EMPTY.withNewTile(startTile);
        PlacedTile tile38 = new PlacedTile(TileReader.readTileFromCSV(38), PlayerColor.RED, Rotation.NONE, new Pos(1, 0), new Occupant(Occupant.Kind.PAWN, 381));
        board = board.withNewTile(tile38);
        PlacedTile tile41 = new PlacedTile(TileReader.readTileFromCSV(41), PlayerColor.RED, Rotation.NONE, new Pos(2, 0), new Occupant(Occupant.Kind.PAWN, 412));
        board = board.withNewTile(tile41);
        PlacedTile tile35 = new PlacedTile(TileReader.readTileFromCSV(35), PlayerColor.RED, Rotation.HALF_TURN, new Pos(3, 0), new Occupant(Occupant.Kind.PAWN, 351));
        board = board.withNewTile(tile35);

        PlacedTile tile67 = new PlacedTile(TileReader.readTileFromCSV(67), PlayerColor.RED, Rotation.NONE, new Pos(0, 1), new Occupant(Occupant.Kind.PAWN, 670));
        PlacedTile tile51 = new PlacedTile(TileReader.readTileFromCSV(51), PlayerColor.RED, Rotation.NONE, new Pos(-1, 0), new Occupant(Occupant.Kind.PAWN, 511));
        PlacedTile tile1 = new PlacedTile(TileReader.readTileFromCSV(1), PlayerColor.RED, Rotation.NONE, new Pos(-2, 0), new Occupant(Occupant.Kind.PAWN, 11));
        PlacedTile tile17 = new PlacedTile(TileReader.readTileFromCSV(17), PlayerColor.RED, Rotation.NONE, new Pos(-3, 0), new Occupant(Occupant.Kind.PAWN, 171));
        PlacedTile tile21 = new PlacedTile(TileReader.readTileFromCSV(21), PlayerColor.RED, Rotation.NONE, new Pos(-3, -1), new Occupant(Occupant.Kind.PAWN, 211));
        PlacedTile tile37 = new PlacedTile(TileReader.readTileFromCSV(37), PlayerColor.RED, Rotation.NONE, new Pos(0, -1), new Occupant(Occupant.Kind.PAWN, 370));
        PlacedTile tile25 = new PlacedTile(TileReader.readTileFromCSV(25), PlayerColor.RED, Rotation.NONE, new Pos(0, -2), new Occupant(Occupant.Kind.PAWN, 253));
        PlacedTile tile22 = new PlacedTile(TileReader.readTileFromCSV(22), PlayerColor.RED, Rotation.HALF_TURN, new Pos(-2, 1), new Occupant(Occupant.Kind.PAWN, 223));


        Zone.Forest forest56 = (Zone.Forest) startTile.zoneWithId(561);
        Zone.Forest forest38 = (Zone.Forest) tile38.zoneWithId(381);
        Zone.Forest forest412 = (Zone.Forest) tile41.zoneWithId(412);
        Zone.Forest forest670 = (Zone.Forest) tile67.zoneWithId(670);

        Zone.Forest forest41 = (Zone.Forest) tile41.zoneWithId(411);
        Zone.Forest forest35 = (Zone.Forest) tile35.zoneWithId(351);

        Zone.Forest forest25 = (Zone.Forest) tile25.zoneWithId(253);
        Zone.Forest forest37 = (Zone.Forest) tile37.zoneWithId(370);

        Board finalBoard = board;
        assertThrows(IllegalArgumentException.class, () -> finalBoard.forestArea(forest25));
        assertThrows(IllegalArgumentException.class, () -> finalBoard.forestArea(forest37));
        assertThrows(IllegalArgumentException.class, () -> finalBoard.forestArea(forest670));
    }

    @Test
    void riverAreaWorks() {
        PlacedTile startTile = new PlacedTile(TileReader.readTileFromCSV(56), PlayerColor.RED, Rotation.NONE, new Pos(0, 0));
        Board board = Board.EMPTY.withNewTile(startTile);
        PlacedTile tile38 = new PlacedTile(TileReader.readTileFromCSV(38), PlayerColor.RED, Rotation.NONE, new Pos(1, 0), new Occupant(Occupant.Kind.PAWN, 381));
        board = board.withNewTile(tile38);
        PlacedTile tile41 = new PlacedTile(TileReader.readTileFromCSV(41), PlayerColor.RED, Rotation.NONE, new Pos(2, 0), new Occupant(Occupant.Kind.PAWN, 412));
        board = board.withNewTile(tile41);
        PlacedTile tile35 = new PlacedTile(TileReader.readTileFromCSV(35), PlayerColor.RED, Rotation.HALF_TURN, new Pos(3, 0), new Occupant(Occupant.Kind.PAWN, 351));
        board = board.withNewTile(tile35);
        PlacedTile tile67 = new PlacedTile(TileReader.readTileFromCSV(67), PlayerColor.RED, Rotation.NONE, new Pos(0, 1), new Occupant(Occupant.Kind.PAWN, 670));
        board = board.withNewTile(tile67);
        PlacedTile tile51 = new PlacedTile(TileReader.readTileFromCSV(51), PlayerColor.RED, Rotation.NONE, new Pos(-1, 0), new Occupant(Occupant.Kind.PAWN, 511));
        board = board.withNewTile(tile51);
        PlacedTile tile1 = new PlacedTile(TileReader.readTileFromCSV(1), PlayerColor.RED, Rotation.NONE, new Pos(-2, 0), new Occupant(Occupant.Kind.PAWN, 11));
        board = board.withNewTile(tile1);
        PlacedTile tile17 = new PlacedTile(TileReader.readTileFromCSV(17), PlayerColor.RED, Rotation.NONE, new Pos(-3, 0), new Occupant(Occupant.Kind.PAWN, 171));
        board = board.withNewTile(tile17);
        PlacedTile tile21 = new PlacedTile(TileReader.readTileFromCSV(21), PlayerColor.RED, Rotation.NONE, new Pos(-3, -1), new Occupant(Occupant.Kind.PAWN, 211));
        board = board.withNewTile(tile21);
        PlacedTile tile37 = new PlacedTile(TileReader.readTileFromCSV(37), PlayerColor.RED, Rotation.NONE, new Pos(0, -1), new Occupant(Occupant.Kind.PAWN, 370));
        board = board.withNewTile(tile37);
        PlacedTile tile25 = new PlacedTile(TileReader.readTileFromCSV(25), PlayerColor.RED, Rotation.NONE, new Pos(0, -2), new Occupant(Occupant.Kind.PAWN, 253));
        board = board.withNewTile(tile25);
        PlacedTile tile22 = new PlacedTile(TileReader.readTileFromCSV(22), PlayerColor.RED, Rotation.HALF_TURN, new Pos(-2, 1), new Occupant(Occupant.Kind.PAWN, 223));
        board = board.withNewTile(tile22);


        Zone.River river56 = (Zone.River) startTile.zoneWithId(563);
        Zone.River river51 = (Zone.River) tile51.zoneWithId(511);
        Zone.River river11 = (Zone.River) tile1.zoneWithId(11);

        Zone.River river15 = (Zone.River) tile1.zoneWithId(15);
        Zone.River river17 = (Zone.River) tile17.zoneWithId(171);
        Zone.River river211 = (Zone.River) tile21.zoneWithId(211);

        Zone.River river251 = (Zone.River) tile25.zoneWithId(251);
        Zone.River river221 = (Zone.River) tile22.zoneWithId(221);
        Zone.River river173 = (Zone.River) tile17.zoneWithId(173);


        Area<Zone.River> riverArea1 = new Area<>(Set.of(river56, river51, river11), List.of(), 0);
        Area<Zone.River> riverArea2 = new Area<>(Set.of(river15, river17, river211), List.of(), 1);

        Area<Zone.River> riverArea3 = new Area<>(Set.of(river251), List.of(), 2);
        Area<Zone.River> riverArea4 = new Area<>(Set.of(river221), List.of(), 2);
        Area<Zone.River> riverArea5 = new Area<>(Set.of(river173), List.of(), 2);

        assertEquals(riverArea1, board.riverArea(river56));
        assertEquals(riverArea1, board.riverArea(river51));
        assertEquals(riverArea1, board.riverArea(river11));
        assertEquals(riverArea2, board.riverArea(river15));
        assertEquals(riverArea2, board.riverArea(river17));
        assertEquals(riverArea2, board.riverArea(river211));
        assertEquals(riverArea3, board.riverArea(river251));
        assertEquals(riverArea4, board.riverArea(river221));
        assertEquals(riverArea5, board.riverArea(river173));
    }

    @Test
    void riverAreaThrows() {
        PlacedTile startTile = new PlacedTile(TileReader.readTileFromCSV(56), PlayerColor.RED, Rotation.NONE, new Pos(0, 0));
        Board board = Board.EMPTY.withNewTile(startTile);
        PlacedTile tile38 = new PlacedTile(TileReader.readTileFromCSV(38), PlayerColor.RED, Rotation.NONE, new Pos(1, 0), new Occupant(Occupant.Kind.PAWN, 381));
        board = board.withNewTile(tile38);
        PlacedTile tile41 = new PlacedTile(TileReader.readTileFromCSV(41), PlayerColor.RED, Rotation.NONE, new Pos(2, 0), new Occupant(Occupant.Kind.PAWN, 412));
        board = board.withNewTile(tile41);
        PlacedTile tile35 = new PlacedTile(TileReader.readTileFromCSV(35), PlayerColor.RED, Rotation.HALF_TURN, new Pos(3, 0), new Occupant(Occupant.Kind.PAWN, 351));
        board = board.withNewTile(tile35);
        PlacedTile tile67 = new PlacedTile(TileReader.readTileFromCSV(67), PlayerColor.RED, Rotation.NONE, new Pos(0, 1), new Occupant(Occupant.Kind.PAWN, 670));
        board = board.withNewTile(tile67);
        PlacedTile tile51 = new PlacedTile(TileReader.readTileFromCSV(51), PlayerColor.RED, Rotation.NONE, new Pos(-1, 0), new Occupant(Occupant.Kind.PAWN, 511));
        board = board.withNewTile(tile51);

        PlacedTile tile1 = new PlacedTile(TileReader.readTileFromCSV(1), PlayerColor.RED, Rotation.NONE, new Pos(-2, 0), new Occupant(Occupant.Kind.PAWN, 11));
        PlacedTile tile17 = new PlacedTile(TileReader.readTileFromCSV(17), PlayerColor.RED, Rotation.NONE, new Pos(-3, 0), new Occupant(Occupant.Kind.PAWN, 171));
        PlacedTile tile21 = new PlacedTile(TileReader.readTileFromCSV(21), PlayerColor.RED, Rotation.NONE, new Pos(-3, -1), new Occupant(Occupant.Kind.PAWN, 211));
        PlacedTile tile37 = new PlacedTile(TileReader.readTileFromCSV(37), PlayerColor.RED, Rotation.NONE, new Pos(0, -1), new Occupant(Occupant.Kind.PAWN, 370));
        PlacedTile tile25 = new PlacedTile(TileReader.readTileFromCSV(25), PlayerColor.RED, Rotation.NONE, new Pos(0, -2), new Occupant(Occupant.Kind.PAWN, 253));
        PlacedTile tile22 = new PlacedTile(TileReader.readTileFromCSV(22), PlayerColor.RED, Rotation.HALF_TURN, new Pos(-2, 1), new Occupant(Occupant.Kind.PAWN, 223));


        Zone.River river56 = (Zone.River) startTile.zoneWithId(563);
        Zone.River river51 = (Zone.River) tile51.zoneWithId(511);
        Zone.River river11 = (Zone.River) tile1.zoneWithId(11);

        Zone.River river15 = (Zone.River) tile1.zoneWithId(15);
        Zone.River river17 = (Zone.River) tile17.zoneWithId(171);
        Zone.River river211 = (Zone.River) tile21.zoneWithId(211);

        Zone.River river251 = (Zone.River) tile25.zoneWithId(251);
        Zone.River river221 = (Zone.River) tile22.zoneWithId(221);
        Zone.River river173 = (Zone.River) tile17.zoneWithId(173);

        Board finalBoard = board;
        assertThrows(IllegalArgumentException.class, () -> finalBoard.riverArea(river251));
        assertThrows(IllegalArgumentException.class, () -> finalBoard.riverArea(river221));
        assertThrows(IllegalArgumentException.class, () -> finalBoard.riverArea(river173));
        assertThrows(IllegalArgumentException.class, () -> finalBoard.riverArea(river11));
        assertThrows(IllegalArgumentException.class, () -> finalBoard.riverArea(river17));
        assertThrows(IllegalArgumentException.class, () -> finalBoard.riverArea(river211));
    }
    @Test
    void meadowAreaWorks() {
        PlacedTile startTile = new PlacedTile(TileReader.readTileFromCSV(56), PlayerColor.RED, Rotation.NONE, new Pos(0, 0));
        Board board = Board.EMPTY.withNewTile(startTile);
        PlacedTile tile38 = new PlacedTile(TileReader.readTileFromCSV(38), PlayerColor.RED, Rotation.NONE, new Pos(1, 0), new Occupant(Occupant.Kind.PAWN, 381));
        board = board.withNewTile(tile38);
        PlacedTile tile41 = new PlacedTile(TileReader.readTileFromCSV(41), PlayerColor.RED, Rotation.NONE, new Pos(2, 0), new Occupant(Occupant.Kind.PAWN, 412));
        board = board.withNewTile(tile41);
        PlacedTile tile35 = new PlacedTile(TileReader.readTileFromCSV(35), PlayerColor.RED, Rotation.HALF_TURN, new Pos(3, 0), new Occupant(Occupant.Kind.PAWN, 351));
        board = board.withNewTile(tile35);
        PlacedTile tile67 = new PlacedTile(TileReader.readTileFromCSV(67), PlayerColor.RED, Rotation.NONE, new Pos(0, 1), new Occupant(Occupant.Kind.PAWN, 670));
        board = board.withNewTile(tile67);
        PlacedTile tile51 = new PlacedTile(TileReader.readTileFromCSV(51), PlayerColor.RED, Rotation.NONE, new Pos(-1, 0), new Occupant(Occupant.Kind.PAWN, 511));
        board = board.withNewTile(tile51);
        PlacedTile tile1 = new PlacedTile(TileReader.readTileFromCSV(1), PlayerColor.RED, Rotation.NONE, new Pos(-2, 0), new Occupant(Occupant.Kind.PAWN, 11));
        board = board.withNewTile(tile1);
        PlacedTile tile17 = new PlacedTile(TileReader.readTileFromCSV(17), PlayerColor.RED, Rotation.NONE, new Pos(-3, 0), new Occupant(Occupant.Kind.PAWN, 171));
        board = board.withNewTile(tile17);
        PlacedTile tile21 = new PlacedTile(TileReader.readTileFromCSV(21), PlayerColor.RED, Rotation.NONE, new Pos(-3, -1), new Occupant(Occupant.Kind.PAWN, 211));
        board = board.withNewTile(tile21);
        PlacedTile tile37 = new PlacedTile(TileReader.readTileFromCSV(37), PlayerColor.RED, Rotation.NONE, new Pos(0, -1), new Occupant(Occupant.Kind.PAWN, 370));
        board = board.withNewTile(tile37);
        PlacedTile tile25 = new PlacedTile(TileReader.readTileFromCSV(25), PlayerColor.RED, Rotation.NONE, new Pos(0, -2), new Occupant(Occupant.Kind.PAWN, 253));
        board = board.withNewTile(tile25);
        PlacedTile tile22 = new PlacedTile(TileReader.readTileFromCSV(22), PlayerColor.RED, Rotation.HALF_TURN, new Pos(-2, 1), new Occupant(Occupant.Kind.PAWN, 223));
        board = board.withNewTile(tile22);

        Zone.Meadow meadow560 = (Zone.Meadow) startTile.zoneWithId(560);
        Zone.Meadow meadow562 = (Zone.Meadow) startTile.zoneWithId(562);

        Zone.Meadow meadow380 = (Zone.Meadow) tile38.zoneWithId(380);
        Zone.Meadow meadow382 = (Zone.Meadow) tile38.zoneWithId(382);
        Zone.Meadow meadow410 = (Zone.Meadow) tile41.zoneWithId(410);
        Zone.Meadow meadow350 = (Zone.Meadow) tile35.zoneWithId(350);
        Zone.Meadow meadow672 = (Zone.Meadow) tile67.zoneWithId(672);
        Zone.Meadow meadow510 = (Zone.Meadow) tile51.zoneWithId(510);
        Zone.Meadow meadow512 = (Zone.Meadow) tile51.zoneWithId(512);
        Zone.Meadow meadow10 = (Zone.Meadow) tile1.zoneWithId(10);
        Zone.Meadow meadow12 = (Zone.Meadow) tile1.zoneWithId(12);
        Zone.Meadow meadow14 = (Zone.Meadow) tile1.zoneWithId(14);
        Zone.Meadow meadow170 = (Zone.Meadow) tile17.zoneWithId(170);
        Zone.Meadow meadow172 = (Zone.Meadow) tile17.zoneWithId(172);
        Zone.Meadow meadow174 = (Zone.Meadow) tile17.zoneWithId(174);
        Zone.Meadow meadow210 = (Zone.Meadow) tile21.zoneWithId(210);
        Zone.Meadow meadow212 = (Zone.Meadow) tile21.zoneWithId(212);
        Zone.Meadow meadow371 = (Zone.Meadow) tile37.zoneWithId(371);
        Zone.Meadow meadow250 = (Zone.Meadow) tile25.zoneWithId(250);
        Zone.Meadow meadow252 = (Zone.Meadow) tile25.zoneWithId(252);
        Zone.Meadow meadow220 = (Zone.Meadow) tile22.zoneWithId(220);
        Zone.Meadow meadow222 = (Zone.Meadow) tile22.zoneWithId(222);

        Area<Zone.Meadow> meadowArea1 = new Area<>(Set.of(meadow560,meadow371,meadow510, meadow10, meadow172, meadow212), List.of(), 5);
        Area<Zone.Meadow> meadowArea2 = new Area<>(Set.of(meadow562, meadow512, meadow12), List.of(), 1);
        Area<Zone.Meadow> meadowArea3 = new Area<>(Set.of(meadow14, meadow170, meadow210), List.of(), 5);
        Area<Zone.Meadow> meadowArea4 = new Area<>(Set.of(meadow250), List.of(), 3);
        Area<Zone.Meadow> meadowArea5 = new Area<>(Set.of(meadow252), List.of(), 2);
        Area<Zone.Meadow> meadowArea6 = new Area<>(Set.of(meadow380), List.of(), 1);
        Area<Zone.Meadow> meadowArea7 = new Area<>(Set.of(meadow382), List.of(), 1);
        Area<Zone.Meadow> meadowArea8 = new Area<>(Set.of(meadow410), List.of(), 2);
        Area<Zone.Meadow> meadowArea9 = new Area<>(Set.of(meadow350), List.of(), 3);
        Area<Zone.Meadow> meadowArea10 = new Area<>(Set.of(meadow672), List.of(), 2);
        Area<Zone.Meadow> meadowArea11 = new Area<>(Set.of(meadow220), List.of(), 2);
        Area<Zone.Meadow> meadowArea12 = new Area<>(Set.of(meadow222), List.of(), 3);
        Area<Zone.Meadow> meadowArea13 = new Area<>(Set.of(meadow174), List.of(), 2);

        assertEquals(meadowArea1, board.meadowArea(meadow560));
        assertEquals(meadowArea1, board.meadowArea(meadow371));
        assertEquals(meadowArea1, board.meadowArea(meadow510));
        assertEquals(meadowArea1, board.meadowArea(meadow10));
        assertEquals(meadowArea1, board.meadowArea(meadow172));
        assertEquals(meadowArea1, board.meadowArea(meadow212));
        assertEquals(meadowArea2, board.meadowArea(meadow562));
        assertEquals(meadowArea2, board.meadowArea(meadow512));
        assertEquals(meadowArea2, board.meadowArea(meadow12));
        assertEquals(meadowArea3, board.meadowArea(meadow14));
        assertEquals(meadowArea3, board.meadowArea(meadow170));
        assertEquals(meadowArea3, board.meadowArea(meadow210));
        assertEquals(meadowArea4, board.meadowArea(meadow250));
        assertEquals(meadowArea5, board.meadowArea(meadow252));
        assertEquals(meadowArea6, board.meadowArea(meadow380));
        assertEquals(meadowArea7, board.meadowArea(meadow382));
        assertEquals(meadowArea8, board.meadowArea(meadow410));
        assertEquals(meadowArea9, board.meadowArea(meadow350));
        assertEquals(meadowArea10, board.meadowArea(meadow672));
        assertEquals(meadowArea11, board.meadowArea(meadow220));
        assertEquals(meadowArea12, board.meadowArea(meadow222));
        assertEquals(meadowArea13, board.meadowArea(meadow174));
    }

    @Test
    void meadowAreaThrows() {
        PlacedTile startTile = new PlacedTile(TileReader.readTileFromCSV(56), PlayerColor.RED, Rotation.NONE, new Pos(0, 0));
        Board board = Board.EMPTY.withNewTile(startTile);
        PlacedTile tile38 = new PlacedTile(TileReader.readTileFromCSV(38), PlayerColor.RED, Rotation.NONE, new Pos(1, 0), new Occupant(Occupant.Kind.PAWN, 381));
        board = board.withNewTile(tile38);
        PlacedTile tile41 = new PlacedTile(TileReader.readTileFromCSV(41), PlayerColor.RED, Rotation.NONE, new Pos(2, 0), new Occupant(Occupant.Kind.PAWN, 412));
        board = board.withNewTile(tile41);
        PlacedTile tile35 = new PlacedTile(TileReader.readTileFromCSV(35), PlayerColor.RED, Rotation.HALF_TURN, new Pos(3, 0), new Occupant(Occupant.Kind.PAWN, 351));
        board = board.withNewTile(tile35);
        PlacedTile tile67 = new PlacedTile(TileReader.readTileFromCSV(67), PlayerColor.RED, Rotation.NONE, new Pos(0, 1), new Occupant(Occupant.Kind.PAWN, 670));
        board = board.withNewTile(tile67);
        PlacedTile tile51 = new PlacedTile(TileReader.readTileFromCSV(51), PlayerColor.RED, Rotation.NONE, new Pos(-1, 0), new Occupant(Occupant.Kind.PAWN, 511));
        board = board.withNewTile(tile51);

        PlacedTile tile1 = new PlacedTile(TileReader.readTileFromCSV(1), PlayerColor.RED, Rotation.NONE, new Pos(-2, 0), new Occupant(Occupant.Kind.PAWN, 11));
        PlacedTile tile17 = new PlacedTile(TileReader.readTileFromCSV(17), PlayerColor.RED, Rotation.NONE, new Pos(-3, 0), new Occupant(Occupant.Kind.PAWN, 171));
        PlacedTile tile21 = new PlacedTile(TileReader.readTileFromCSV(21), PlayerColor.RED, Rotation.NONE, new Pos(-3, -1), new Occupant(Occupant.Kind.PAWN, 211));
        PlacedTile tile37 = new PlacedTile(TileReader.readTileFromCSV(37), PlayerColor.RED, Rotation.NONE, new Pos(0, -1), new Occupant(Occupant.Kind.PAWN, 370));
        PlacedTile tile25 = new PlacedTile(TileReader.readTileFromCSV(25), PlayerColor.RED, Rotation.NONE, new Pos(0, -2), new Occupant(Occupant.Kind.PAWN, 253));
        PlacedTile tile22 = new PlacedTile(TileReader.readTileFromCSV(22), PlayerColor.RED, Rotation.HALF_TURN, new Pos(-2, 1), new Occupant(Occupant.Kind.PAWN, 223));

        Zone.Meadow meadow560 = (Zone.Meadow) startTile.zoneWithId(560);
        Zone.Meadow meadow562 = (Zone.Meadow) startTile.zoneWithId(562);
        Zone.Meadow meadow380 = (Zone.Meadow) tile38.zoneWithId(380);
        Zone.Meadow meadow382 = (Zone.Meadow) tile38.zoneWithId(382);
        Zone.Meadow meadow410 = (Zone.Meadow) tile41.zoneWithId(410);
        Zone.Meadow meadow350 = (Zone.Meadow) tile35.zoneWithId(350);
        Zone.Meadow meadow672 = (Zone.Meadow) tile67.zoneWithId(672);
        Zone.Meadow meadow510 = (Zone.Meadow) tile51.zoneWithId(510);
        Zone.Meadow meadow512 = (Zone.Meadow) tile51.zoneWithId(512);

        Zone.Meadow meadow10 = (Zone.Meadow) tile1.zoneWithId(10);
        Zone.Meadow meadow12 = (Zone.Meadow) tile1.zoneWithId(12);
        Zone.Meadow meadow14 = (Zone.Meadow) tile1.zoneWithId(14);
        Zone.Meadow meadow170 = (Zone.Meadow) tile17.zoneWithId(170);
        Zone.Meadow meadow172 = (Zone.Meadow) tile17.zoneWithId(172);
        Zone.Meadow meadow174 = (Zone.Meadow) tile17.zoneWithId(174);
        Zone.Meadow meadow210 = (Zone.Meadow) tile21.zoneWithId(210);
        Zone.Meadow meadow212 = (Zone.Meadow) tile21.zoneWithId(212);
        Zone.Meadow meadow371 = (Zone.Meadow) tile37.zoneWithId(371);
        Zone.Meadow meadow250 = (Zone.Meadow) tile25.zoneWithId(250);
        Zone.Meadow meadow252 = (Zone.Meadow) tile25.zoneWithId(252);
        Zone.Meadow meadow220 = (Zone.Meadow) tile22.zoneWithId(220);
        Zone.Meadow meadow222 = (Zone.Meadow) tile22.zoneWithId(222);

        Board finalBoard = board;
        assertThrows(IllegalArgumentException.class, () -> finalBoard.meadowArea(meadow10));
        assertThrows(IllegalArgumentException.class, () -> finalBoard.meadowArea(meadow12));
        assertThrows(IllegalArgumentException.class, () -> finalBoard.meadowArea(meadow14));
        assertThrows(IllegalArgumentException.class, () -> finalBoard.meadowArea(meadow170));
        assertThrows(IllegalArgumentException.class, () -> finalBoard.meadowArea(meadow172));
        assertThrows(IllegalArgumentException.class, () -> finalBoard.meadowArea(meadow174));
        assertThrows(IllegalArgumentException.class, () -> finalBoard.meadowArea(meadow210));
        assertThrows(IllegalArgumentException.class, () -> finalBoard.meadowArea(meadow212));
        assertThrows(IllegalArgumentException.class, () -> finalBoard.meadowArea(meadow371));
        assertThrows(IllegalArgumentException.class, () -> finalBoard.meadowArea(meadow250));
        assertThrows(IllegalArgumentException.class, () -> finalBoard.meadowArea(meadow252));
        assertThrows(IllegalArgumentException.class, () -> finalBoard.meadowArea(meadow220));
        assertThrows(IllegalArgumentException.class, () -> finalBoard.meadowArea(meadow222));


    }
    @Test
    void riverSystemAreaWorks() {
        PlacedTile startTile = new PlacedTile(TileReader.readTileFromCSV(56), PlayerColor.RED, Rotation.NONE, new Pos(0, 0));
        Board board = Board.EMPTY.withNewTile(startTile);
        PlacedTile tile38 = new PlacedTile(TileReader.readTileFromCSV(38), PlayerColor.RED, Rotation.NONE, new Pos(1, 0), new Occupant(Occupant.Kind.PAWN, 381));
        board = board.withNewTile(tile38);
        PlacedTile tile41 = new PlacedTile(TileReader.readTileFromCSV(41), PlayerColor.RED, Rotation.NONE, new Pos(2, 0), new Occupant(Occupant.Kind.PAWN, 412));
        board = board.withNewTile(tile41);
        PlacedTile tile35 = new PlacedTile(TileReader.readTileFromCSV(35), PlayerColor.RED, Rotation.HALF_TURN, new Pos(3, 0), new Occupant(Occupant.Kind.PAWN, 351));
        board = board.withNewTile(tile35);
        PlacedTile tile67 = new PlacedTile(TileReader.readTileFromCSV(67), PlayerColor.RED, Rotation.NONE, new Pos(0, 1), new Occupant(Occupant.Kind.PAWN, 670));
        board = board.withNewTile(tile67);
        PlacedTile tile51 = new PlacedTile(TileReader.readTileFromCSV(51), PlayerColor.RED, Rotation.NONE, new Pos(-1, 0), new Occupant(Occupant.Kind.PAWN, 511));
        board = board.withNewTile(tile51);
        PlacedTile tile1 = new PlacedTile(TileReader.readTileFromCSV(1), PlayerColor.RED, Rotation.NONE, new Pos(-2, 0), new Occupant(Occupant.Kind.PAWN, 11));
        board = board.withNewTile(tile1);
        PlacedTile tile17 = new PlacedTile(TileReader.readTileFromCSV(17), PlayerColor.RED, Rotation.NONE, new Pos(-3, 0), new Occupant(Occupant.Kind.PAWN, 171));
        board = board.withNewTile(tile17);
        PlacedTile tile21 = new PlacedTile(TileReader.readTileFromCSV(21), PlayerColor.RED, Rotation.NONE, new Pos(-3, -1), new Occupant(Occupant.Kind.PAWN, 211));
        board = board.withNewTile(tile21);
        PlacedTile tile37 = new PlacedTile(TileReader.readTileFromCSV(37), PlayerColor.RED, Rotation.NONE, new Pos(0, -1), new Occupant(Occupant.Kind.PAWN, 370));
        board = board.withNewTile(tile37);
        PlacedTile tile25 = new PlacedTile(TileReader.readTileFromCSV(25), PlayerColor.RED, Rotation.NONE, new Pos(0, -2), new Occupant(Occupant.Kind.PAWN, 253));
        board = board.withNewTile(tile25);
        PlacedTile tile22 = new PlacedTile(TileReader.readTileFromCSV(22), PlayerColor.RED, Rotation.HALF_TURN, new Pos(-2, 1), new Occupant(Occupant.Kind.PAWN, 223));
        board = board.withNewTile(tile22);


        Zone.Water river56 = (Zone.River) startTile.zoneWithId(563);
        Zone.Water river51 = (Zone.River) tile51.zoneWithId(511);
        Zone.Water river11 = (Zone.River) tile1.zoneWithId(11);

        Zone.Water river15 = (Zone.River) tile1.zoneWithId(15);
        Zone.Water river17 = (Zone.River) tile17.zoneWithId(171);
        Zone.Water river211 = (Zone.River) tile21.zoneWithId(211);

        Zone.Water river251 = (Zone.River) tile25.zoneWithId(251);
        Zone.Water river221 = (Zone.River) tile22.zoneWithId(221);
        Zone.Water river173 = (Zone.River) tile17.zoneWithId(173);

        Zone.Water lake568 = (Zone.Lake) startTile.zoneWithId(568);
        Zone.Water lake18 = (Zone.Lake) tile1.zoneWithId(18);

        Area<Zone.Water> riverSystemArea1 = new Area<>(Set.of(river56, river51, river11, river15, river17, river211, lake18, lake568), List.of(), 1);
        Area<Zone.Water> riverSystemArea2 = new Area<>(Set.of(river251), List.of(), 2);
        Area<Zone.Water> riverSystemArea3 = new Area<>(Set.of(river221), List.of(), 2);
        Area<Zone.Water> riverSystemArea4 = new Area<>(Set.of(river173), List.of(), 2);

        assertEquals(riverSystemArea1, board.riverSystemArea(river56));
        assertEquals(riverSystemArea1, board.riverSystemArea(river51));
        assertEquals(riverSystemArea1, board.riverSystemArea(river11));
        assertEquals(riverSystemArea1, board.riverSystemArea(river15));
        assertEquals(riverSystemArea1, board.riverSystemArea(river17));
        assertEquals(riverSystemArea1, board.riverSystemArea(river211));
        assertEquals(riverSystemArea1, board.riverSystemArea(lake18));
        assertEquals(riverSystemArea1, board.riverSystemArea(lake568));
        assertEquals(riverSystemArea2, board.riverSystemArea(river251));
        assertEquals(riverSystemArea3, board.riverSystemArea(river221));
        assertEquals(riverSystemArea4, board.riverSystemArea(river173));
    }

    @Test
    void riverSystemAreaThrows() {
        PlacedTile startTile = new PlacedTile(TileReader.readTileFromCSV(56), PlayerColor.RED, Rotation.NONE, new Pos(0, 0));
        Board board = Board.EMPTY.withNewTile(startTile);
        PlacedTile tile38 = new PlacedTile(TileReader.readTileFromCSV(38), PlayerColor.RED, Rotation.NONE, new Pos(1, 0), new Occupant(Occupant.Kind.PAWN, 381));
        board = board.withNewTile(tile38);
        PlacedTile tile41 = new PlacedTile(TileReader.readTileFromCSV(41), PlayerColor.RED, Rotation.NONE, new Pos(2, 0), new Occupant(Occupant.Kind.PAWN, 412));
        board = board.withNewTile(tile41);
        PlacedTile tile35 = new PlacedTile(TileReader.readTileFromCSV(35), PlayerColor.RED, Rotation.HALF_TURN, new Pos(3, 0), new Occupant(Occupant.Kind.PAWN, 351));
        board = board.withNewTile(tile35);
        PlacedTile tile67 = new PlacedTile(TileReader.readTileFromCSV(67), PlayerColor.RED, Rotation.NONE, new Pos(0, 1), new Occupant(Occupant.Kind.PAWN, 670));
        board = board.withNewTile(tile67);
        PlacedTile tile51 = new PlacedTile(TileReader.readTileFromCSV(51), PlayerColor.RED, Rotation.NONE, new Pos(-1, 0), new Occupant(Occupant.Kind.PAWN, 511));
        board = board.withNewTile(tile51);

        PlacedTile tile1 = new PlacedTile(TileReader.readTileFromCSV(1), PlayerColor.RED, Rotation.NONE, new Pos(-2, 0), new Occupant(Occupant.Kind.PAWN, 11));
        PlacedTile tile17 = new PlacedTile(TileReader.readTileFromCSV(17), PlayerColor.RED, Rotation.NONE, new Pos(-3, 0), new Occupant(Occupant.Kind.PAWN, 171));
        PlacedTile tile21 = new PlacedTile(TileReader.readTileFromCSV(21), PlayerColor.RED, Rotation.NONE, new Pos(-3, -1), new Occupant(Occupant.Kind.PAWN, 211));
        PlacedTile tile37 = new PlacedTile(TileReader.readTileFromCSV(37), PlayerColor.RED, Rotation.NONE, new Pos(0, -1), new Occupant(Occupant.Kind.PAWN, 370));
        PlacedTile tile25 = new PlacedTile(TileReader.readTileFromCSV(25), PlayerColor.RED, Rotation.NONE, new Pos(0, -2), new Occupant(Occupant.Kind.PAWN, 253));
        PlacedTile tile22 = new PlacedTile(TileReader.readTileFromCSV(22), PlayerColor.RED, Rotation.HALF_TURN, new Pos(-2, 1), new Occupant(Occupant.Kind.PAWN, 223));



        Zone.Water river56 = (Zone.River) startTile.zoneWithId(563);
        Zone.Water river51 = (Zone.River) tile51.zoneWithId(511);
        Zone.Water river173 = (Zone.River) tile17.zoneWithId(173);
        Zone.Water lake568 = (Zone.Lake) startTile.zoneWithId(568);

        Zone.Water river11 = (Zone.River) tile1.zoneWithId(11);
        Zone.Water river15 = (Zone.River) tile1.zoneWithId(15);
        Zone.Water lake18 = (Zone.Lake) tile1.zoneWithId(18);
        Zone.Water river17 = (Zone.River) tile17.zoneWithId(171);
        Zone.Water river211 = (Zone.River) tile21.zoneWithId(211);
        Zone.Water river251 = (Zone.River) tile25.zoneWithId(251);
        Zone.Water river221 = (Zone.River) tile22.zoneWithId(221);

        Board finalBoard = board;
        assertThrows(IllegalArgumentException.class, () -> finalBoard.riverSystemArea(river11));
        assertThrows(IllegalArgumentException.class, () -> finalBoard.riverSystemArea(river15));
        assertThrows(IllegalArgumentException.class, () -> finalBoard.riverSystemArea(lake18));
        assertThrows(IllegalArgumentException.class, () -> finalBoard.riverSystemArea(river17));
        assertThrows(IllegalArgumentException.class, () -> finalBoard.riverSystemArea(river211));
        assertThrows(IllegalArgumentException.class, () -> finalBoard.riverSystemArea(river251));
        assertThrows(IllegalArgumentException.class, () -> finalBoard.riverSystemArea(river221));


    }
    @Test
    void meadowsAreasWorks() {
        PlacedTile startTile = new PlacedTile(TileReader.readTileFromCSV(56), PlayerColor.RED, Rotation.NONE, new Pos(0, 0));
        Board board = Board.EMPTY.withNewTile(startTile);
        PlacedTile tile38 = new PlacedTile(TileReader.readTileFromCSV(38), PlayerColor.RED, Rotation.NONE, new Pos(1, 0), new Occupant(Occupant.Kind.PAWN, 381));
        board = board.withNewTile(tile38);
        PlacedTile tile41 = new PlacedTile(TileReader.readTileFromCSV(41), PlayerColor.RED, Rotation.NONE, new Pos(2, 0), new Occupant(Occupant.Kind.PAWN, 412));
        board = board.withNewTile(tile41);
        PlacedTile tile35 = new PlacedTile(TileReader.readTileFromCSV(35), PlayerColor.RED, Rotation.HALF_TURN, new Pos(3, 0), new Occupant(Occupant.Kind.PAWN, 351));
        board = board.withNewTile(tile35);
        PlacedTile tile67 = new PlacedTile(TileReader.readTileFromCSV(67), PlayerColor.RED, Rotation.NONE, new Pos(0, 1), new Occupant(Occupant.Kind.PAWN, 670));
        board = board.withNewTile(tile67);
        PlacedTile tile51 = new PlacedTile(TileReader.readTileFromCSV(51), PlayerColor.RED, Rotation.NONE, new Pos(-1, 0), new Occupant(Occupant.Kind.PAWN, 511));
        board = board.withNewTile(tile51);
        PlacedTile tile1 = new PlacedTile(TileReader.readTileFromCSV(1), PlayerColor.RED, Rotation.NONE, new Pos(-2, 0), new Occupant(Occupant.Kind.PAWN, 11));
        board = board.withNewTile(tile1);
        PlacedTile tile17 = new PlacedTile(TileReader.readTileFromCSV(17), PlayerColor.RED, Rotation.NONE, new Pos(-3, 0), new Occupant(Occupant.Kind.PAWN, 171));
        board = board.withNewTile(tile17);
        PlacedTile tile21 = new PlacedTile(TileReader.readTileFromCSV(21), PlayerColor.RED, Rotation.NONE, new Pos(-3, -1), new Occupant(Occupant.Kind.PAWN, 211));
        board = board.withNewTile(tile21);
        PlacedTile tile37 = new PlacedTile(TileReader.readTileFromCSV(37), PlayerColor.RED, Rotation.NONE, new Pos(0, -1), new Occupant(Occupant.Kind.PAWN, 370));
        board = board.withNewTile(tile37);
        PlacedTile tile25 = new PlacedTile(TileReader.readTileFromCSV(25), PlayerColor.RED, Rotation.NONE, new Pos(0, -2), new Occupant(Occupant.Kind.PAWN, 253));
        board = board.withNewTile(tile25);
        PlacedTile tile22 = new PlacedTile(TileReader.readTileFromCSV(22), PlayerColor.RED, Rotation.HALF_TURN, new Pos(-2, 1), new Occupant(Occupant.Kind.PAWN, 223));
        board = board.withNewTile(tile22);

        Zone.Meadow meadow560 = (Zone.Meadow) startTile.zoneWithId(560);
        Zone.Meadow meadow562 = (Zone.Meadow) startTile.zoneWithId(562);

        Zone.Meadow meadow380 = (Zone.Meadow) tile38.zoneWithId(380);
        Zone.Meadow meadow382 = (Zone.Meadow) tile38.zoneWithId(382);
        Zone.Meadow meadow410 = (Zone.Meadow) tile41.zoneWithId(410);
        Zone.Meadow meadow350 = (Zone.Meadow) tile35.zoneWithId(350);
        Zone.Meadow meadow672 = (Zone.Meadow) tile67.zoneWithId(672);
        Zone.Meadow meadow510 = (Zone.Meadow) tile51.zoneWithId(510);
        Zone.Meadow meadow512 = (Zone.Meadow) tile51.zoneWithId(512);
        Zone.Meadow meadow10 = (Zone.Meadow) tile1.zoneWithId(10);
        Zone.Meadow meadow12 = (Zone.Meadow) tile1.zoneWithId(12);
        Zone.Meadow meadow14 = (Zone.Meadow) tile1.zoneWithId(14);
        Zone.Meadow meadow170 = (Zone.Meadow) tile17.zoneWithId(170);
        Zone.Meadow meadow172 = (Zone.Meadow) tile17.zoneWithId(172);
        Zone.Meadow meadow174 = (Zone.Meadow) tile17.zoneWithId(174);
        Zone.Meadow meadow210 = (Zone.Meadow) tile21.zoneWithId(210);
        Zone.Meadow meadow212 = (Zone.Meadow) tile21.zoneWithId(212);
        Zone.Meadow meadow371 = (Zone.Meadow) tile37.zoneWithId(371);
        Zone.Meadow meadow250 = (Zone.Meadow) tile25.zoneWithId(250);
        Zone.Meadow meadow252 = (Zone.Meadow) tile25.zoneWithId(252);
        Zone.Meadow meadow220 = (Zone.Meadow) tile22.zoneWithId(220);
        Zone.Meadow meadow222 = (Zone.Meadow) tile22.zoneWithId(222);

        Area<Zone.Meadow> meadowArea1 = new Area<>(Set.of(meadow560,meadow371,meadow510, meadow10, meadow172, meadow212), List.of(), 5);
        Area<Zone.Meadow> meadowArea2 = new Area<>(Set.of(meadow562, meadow512, meadow12), List.of(), 1);
        Area<Zone.Meadow> meadowArea3 = new Area<>(Set.of(meadow14, meadow170, meadow210), List.of(), 5);
        Area<Zone.Meadow> meadowArea4 = new Area<>(Set.of(meadow250), List.of(), 3);
        Area<Zone.Meadow> meadowArea5 = new Area<>(Set.of(meadow252), List.of(), 2);
        Area<Zone.Meadow> meadowArea6 = new Area<>(Set.of(meadow380), List.of(), 1);
        Area<Zone.Meadow> meadowArea7 = new Area<>(Set.of(meadow382), List.of(), 1);
        Area<Zone.Meadow> meadowArea8 = new Area<>(Set.of(meadow410), List.of(), 2);
        Area<Zone.Meadow> meadowArea9 = new Area<>(Set.of(meadow350), List.of(), 3);
        Area<Zone.Meadow> meadowArea10 = new Area<>(Set.of(meadow672), List.of(), 2);
        Area<Zone.Meadow> meadowArea11 = new Area<>(Set.of(meadow220), List.of(), 2);
        Area<Zone.Meadow> meadowArea12 = new Area<>(Set.of(meadow222), List.of(), 3);
        Area<Zone.Meadow> meadowArea13 = new Area<>(Set.of(meadow174), List.of(), 2);

        Set<Area<Zone.Meadow>> expected = Set.of(meadowArea1, meadowArea2, meadowArea3, meadowArea4, meadowArea5, meadowArea6, meadowArea7, meadowArea8, meadowArea9, meadowArea10, meadowArea11, meadowArea12, meadowArea13);
        assertEquals(expected, board.meadowAreas());
    }

    @Test
    void riverSystemAreasWorks() {
        PlacedTile startTile = new PlacedTile(TileReader.readTileFromCSV(56), PlayerColor.RED, Rotation.NONE, new Pos(0, 0));
        Board board = Board.EMPTY.withNewTile(startTile);
        PlacedTile tile38 = new PlacedTile(TileReader.readTileFromCSV(38), PlayerColor.RED, Rotation.NONE, new Pos(1, 0), new Occupant(Occupant.Kind.PAWN, 381));
        board = board.withNewTile(tile38);
        PlacedTile tile41 = new PlacedTile(TileReader.readTileFromCSV(41), PlayerColor.RED, Rotation.NONE, new Pos(2, 0), new Occupant(Occupant.Kind.PAWN, 412));
        board = board.withNewTile(tile41);
        PlacedTile tile35 = new PlacedTile(TileReader.readTileFromCSV(35), PlayerColor.RED, Rotation.HALF_TURN, new Pos(3, 0), new Occupant(Occupant.Kind.PAWN, 351));
        board = board.withNewTile(tile35);
        PlacedTile tile67 = new PlacedTile(TileReader.readTileFromCSV(67), PlayerColor.RED, Rotation.NONE, new Pos(0, 1), new Occupant(Occupant.Kind.PAWN, 670));
        board = board.withNewTile(tile67);
        PlacedTile tile51 = new PlacedTile(TileReader.readTileFromCSV(51), PlayerColor.RED, Rotation.NONE, new Pos(-1, 0), new Occupant(Occupant.Kind.PAWN, 511));
        board = board.withNewTile(tile51);
        PlacedTile tile1 = new PlacedTile(TileReader.readTileFromCSV(1), PlayerColor.RED, Rotation.NONE, new Pos(-2, 0), new Occupant(Occupant.Kind.PAWN, 11));
        board = board.withNewTile(tile1);
        PlacedTile tile17 = new PlacedTile(TileReader.readTileFromCSV(17), PlayerColor.RED, Rotation.NONE, new Pos(-3, 0), new Occupant(Occupant.Kind.PAWN, 171));
        board = board.withNewTile(tile17);
        PlacedTile tile21 = new PlacedTile(TileReader.readTileFromCSV(21), PlayerColor.RED, Rotation.NONE, new Pos(-3, -1), new Occupant(Occupant.Kind.PAWN, 211));
        board = board.withNewTile(tile21);
        PlacedTile tile37 = new PlacedTile(TileReader.readTileFromCSV(37), PlayerColor.RED, Rotation.NONE, new Pos(0, -1), new Occupant(Occupant.Kind.PAWN, 370));
        board = board.withNewTile(tile37);
        PlacedTile tile25 = new PlacedTile(TileReader.readTileFromCSV(25), PlayerColor.RED, Rotation.NONE, new Pos(0, -2), new Occupant(Occupant.Kind.PAWN, 253));
        board = board.withNewTile(tile25);
        PlacedTile tile22 = new PlacedTile(TileReader.readTileFromCSV(22), PlayerColor.RED, Rotation.HALF_TURN, new Pos(-2, 1), new Occupant(Occupant.Kind.PAWN, 223));
        board = board.withNewTile(tile22);


        Zone.Water river56 = (Zone.River) startTile.zoneWithId(563);
        Zone.Water river51 = (Zone.River) tile51.zoneWithId(511);
        Zone.Water river11 = (Zone.River) tile1.zoneWithId(11);

        Zone.Water river15 = (Zone.River) tile1.zoneWithId(15);
        Zone.Water river17 = (Zone.River) tile17.zoneWithId(171);
        Zone.Water river211 = (Zone.River) tile21.zoneWithId(211);

        Zone.Water river251 = (Zone.River) tile25.zoneWithId(251);
        Zone.Water river221 = (Zone.River) tile22.zoneWithId(221);
        Zone.Water river173 = (Zone.River) tile17.zoneWithId(173);

        Zone.Water lake568 = (Zone.Lake) startTile.zoneWithId(568);
        Zone.Water lake18 = (Zone.Lake) tile1.zoneWithId(18);

        Area<Zone.Water> riverSystemArea1 = new Area<>(Set.of(river56, river51, river11, river15, river17, river211, lake18, lake568), List.of(), 1);
        Area<Zone.Water> riverSystemArea2 = new Area<>(Set.of(river251), List.of(), 2);
        Area<Zone.Water> riverSystemArea3 = new Area<>(Set.of(river221), List.of(), 2);
        Area<Zone.Water> riverSystemArea4 = new Area<>(Set.of(river173), List.of(), 2);

        Set<Area<Zone.Water>> expected = Set.of(riverSystemArea1, riverSystemArea2, riverSystemArea3, riverSystemArea4);
        assertEquals(expected, board.riverSystemAreas());
    }

    @Test
    void adjacentMeadowWorks() {
        PlacedTile startTile = new PlacedTile(TileReader.readTileFromCSV(56), PlayerColor.RED, Rotation.NONE, new Pos(0, 0));
        Board board = Board.EMPTY.withNewTile(startTile);
        PlacedTile tile38 = new PlacedTile(TileReader.readTileFromCSV(38), PlayerColor.BLUE, Rotation.NONE, new Pos(1, 0));
        board = board.withNewTile(tile38);
        board = board.withOccupant( new Occupant(Occupant.Kind.PAWN, 382));
        PlacedTile tile41 = new PlacedTile(TileReader.readTileFromCSV(41), PlayerColor.GREEN, Rotation.NONE, new Pos(2, 0));
        board = board.withNewTile(tile41);
        board = board.withOccupant( new Occupant(Occupant.Kind.PAWN, 410));
        PlacedTile tile35 = new PlacedTile(TileReader.readTileFromCSV(35), PlayerColor.YELLOW, Rotation.HALF_TURN, new Pos(3, 0));
        board = board.withNewTile(tile35);
        board = board.withOccupant( new Occupant(Occupant.Kind.PAWN, 350));
        PlacedTile tile67 = new PlacedTile(TileReader.readTileFromCSV(67), PlayerColor.PURPLE, Rotation.NONE, new Pos(0, 1));
        board = board.withNewTile(tile67);
        board = board.withOccupant( new Occupant(Occupant.Kind.PAWN, 672));
        PlacedTile tile51 = new PlacedTile(TileReader.readTileFromCSV(51), PlayerColor.RED, Rotation.NONE, new Pos(-1, 0));
        board = board.withNewTile(tile51);
        board = board.withOccupant( new Occupant(Occupant.Kind.PAWN, 512));
        PlacedTile tile1 = new PlacedTile(TileReader.readTileFromCSV(1), PlayerColor.BLUE, Rotation.NONE, new Pos(-2, 0));
        board = board.withNewTile(tile1);
        board = board.withOccupant( new Occupant(Occupant.Kind.PAWN, 14));
        PlacedTile tile17 = new PlacedTile(TileReader.readTileFromCSV(17), PlayerColor.GREEN, Rotation.NONE, new Pos(-3, 0));
        board = board.withNewTile(tile17);
        board = board.withOccupant(new Occupant(Occupant.Kind.PAWN, 174));
        PlacedTile tile21 = new PlacedTile(TileReader.readTileFromCSV(21), PlayerColor.YELLOW, Rotation.NONE, new Pos(-3, -1));
        board = board.withNewTile(tile21);
        PlacedTile tile37 = new PlacedTile(TileReader.readTileFromCSV(37), PlayerColor.PURPLE, Rotation.NONE, new Pos(0, -1));
        board = board.withNewTile(tile37);
        board = board.withOccupant( new Occupant(Occupant.Kind.PAWN, 371));
        PlacedTile tile25 = new PlacedTile(TileReader.readTileFromCSV(25), PlayerColor.RED, Rotation.NONE, new Pos(0, -2));
        board = board.withNewTile(tile25);
        board = board.withOccupant( new Occupant(Occupant.Kind.PAWN, 252));
        PlacedTile tile22 = new PlacedTile(TileReader.readTileFromCSV(22), PlayerColor.BLUE, Rotation.HALF_TURN, new Pos(-2, 1));
        board = board.withNewTile(tile22);
        board = board.withOccupant( new Occupant(Occupant.Kind.PAWN, 220));

        Zone.Meadow meadow560 = (Zone.Meadow) startTile.zoneWithId(560);
        Zone.Meadow meadow562 = (Zone.Meadow) startTile.zoneWithId(562);

        Zone.Meadow meadow380 = (Zone.Meadow) tile38.zoneWithId(380);
        Zone.Meadow meadow382 = (Zone.Meadow) tile38.zoneWithId(382);
        Zone.Meadow meadow410 = (Zone.Meadow) tile41.zoneWithId(410);
        Zone.Meadow meadow350 = (Zone.Meadow) tile35.zoneWithId(350);
        Zone.Meadow meadow672 = (Zone.Meadow) tile67.zoneWithId(672);
        Zone.Meadow meadow510 = (Zone.Meadow) tile51.zoneWithId(510);
        Zone.Meadow meadow512 = (Zone.Meadow) tile51.zoneWithId(512);
        Zone.Meadow meadow10 = (Zone.Meadow) tile1.zoneWithId(10);
        Zone.Meadow meadow12 = (Zone.Meadow) tile1.zoneWithId(12);
        Zone.Meadow meadow14 = (Zone.Meadow) tile1.zoneWithId(14);
        Zone.Meadow meadow170 = (Zone.Meadow) tile17.zoneWithId(170);
        Zone.Meadow meadow172 = (Zone.Meadow) tile17.zoneWithId(172);
        Zone.Meadow meadow174 = (Zone.Meadow) tile17.zoneWithId(174);
        Zone.Meadow meadow210 = (Zone.Meadow) tile21.zoneWithId(210);
        Zone.Meadow meadow212 = (Zone.Meadow) tile21.zoneWithId(212);
        Zone.Meadow meadow371 = (Zone.Meadow) tile37.zoneWithId(371);
        Zone.Meadow meadow250 = (Zone.Meadow) tile25.zoneWithId(250);
        Zone.Meadow meadow252 = (Zone.Meadow) tile25.zoneWithId(252);
        Zone.Meadow meadow220 = (Zone.Meadow) tile22.zoneWithId(220);
        Zone.Meadow meadow222 = (Zone.Meadow) tile22.zoneWithId(222);

        Area<Zone.Meadow> meadowArea1 = new Area<>(Set.of(meadow560,meadow371,meadow510, meadow10, meadow172, meadow212), List.of(), 0);
        Area<Zone.Meadow> meadowArea2 = new Area<>(Set.of(meadow562, meadow512, meadow12), List.of(), 0);
        Area<Zone.Meadow> meadowArea3 = new Area<>(Set.of(meadow14, meadow170, meadow210), List.of(), 0);
        Area<Zone.Meadow> meadowArea4 = new Area<>(Set.of(meadow250), List.of(), 0);
        Area<Zone.Meadow> meadowArea5 = new Area<>(Set.of(meadow252), List.of(PlayerColor.RED), 0);
        Area<Zone.Meadow> meadowArea6 = new Area<>(Set.of(meadow380), List.of(), 0);
        Area<Zone.Meadow> meadowArea7 = new Area<>(Set.of(meadow382), List.of(PlayerColor.BLUE), 0);
        Area<Zone.Meadow> meadowArea8 = new Area<>(Set.of(meadow410), List.of(PlayerColor.GREEN), 0);
        Area<Zone.Meadow> meadowArea9 = new Area<>(Set.of(meadow350), List.of(PlayerColor.YELLOW), 0);
        Area<Zone.Meadow> meadowArea10 = new Area<>(Set.of(meadow672), List.of(PlayerColor.PURPLE), 0);
        Area<Zone.Meadow> meadowArea11 = new Area<>(Set.of(meadow220), List.of(PlayerColor.BLUE), 0);
        Area<Zone.Meadow> meadowArea12 = new Area<>(Set.of(meadow222), List.of(), 0);
        Area<Zone.Meadow> meadowArea13 = new Area<>(Set.of(meadow174), List.of(PlayerColor.GREEN), 0);

        assertEquals(meadowArea4, board.adjacentMeadow(new Pos(0, -2),meadow250));
        assertEquals(meadowArea5, board.adjacentMeadow(new Pos(0, -2),meadow252));
        assertEquals(meadowArea6, board.adjacentMeadow(new Pos(1, 0),meadow380));
        assertEquals(meadowArea7, board.adjacentMeadow(new Pos(1, 0),meadow382));
        assertEquals(meadowArea8, board.adjacentMeadow(new Pos(2, 0),meadow410));
        assertEquals(meadowArea9, board.adjacentMeadow(new Pos(3, 0),meadow350));
        assertEquals(meadowArea10, board.adjacentMeadow(new Pos(0, 1),meadow672));
        assertEquals(meadowArea11, board.adjacentMeadow(new Pos(-2, 1),meadow220));
        assertEquals(meadowArea12, board.adjacentMeadow(new Pos(-2, 1),meadow222));
        assertEquals(meadowArea13, board.adjacentMeadow(new Pos(-3, 0),meadow174));

        Area<Zone.Meadow> adjacentArea1 = new Area<>(Set.of(meadow10, meadow172, meadow212), List.of(PlayerColor.PURPLE), 0);
        assertEquals(adjacentArea1, board.adjacentMeadow(new Pos(-3, -1),meadow212));
        Area<Zone.Meadow> adjacentArea2 = new Area<>(Set.of(meadow560,meadow371,meadow510), List.of(PlayerColor.PURPLE), 0);
        assertEquals(adjacentArea2, board.adjacentMeadow(new Pos(0, 0),meadow560));
        Area<Zone.Meadow> adjacentArea3 = new Area<>(Set.of(meadow562, meadow512, meadow12), List.of(PlayerColor.RED), 0);
        assertEquals(adjacentArea3, board.adjacentMeadow(new Pos(-1, 0),meadow512));
        Area<Zone.Meadow> adjacentArea4 = new Area<>(Set.of(meadow14, meadow170, meadow210), List.of(PlayerColor.BLUE), 0);
        assertEquals(adjacentArea4, board.adjacentMeadow(new Pos(-3, 0),meadow170));

    }

    @Test
    void occupantCountWorks() {
        PlacedTile startTile = new PlacedTile(TileReader.readTileFromCSV(56), PlayerColor.RED, Rotation.NONE, new Pos(0, 0));
        Board board = Board.EMPTY.withNewTile(startTile);
        PlacedTile tile38 = new PlacedTile(TileReader.readTileFromCSV(38), PlayerColor.BLUE, Rotation.NONE, new Pos(1, 0));
        board = board.withNewTile(tile38);
        board = board.withOccupant( new Occupant(Occupant.Kind.PAWN, 382));
        PlacedTile tile41 = new PlacedTile(TileReader.readTileFromCSV(41), PlayerColor.GREEN, Rotation.NONE, new Pos(2, 0));
        board = board.withNewTile(tile41);
        board = board.withOccupant( new Occupant(Occupant.Kind.PAWN, 410));
        PlacedTile tile35 = new PlacedTile(TileReader.readTileFromCSV(35), PlayerColor.YELLOW, Rotation.HALF_TURN, new Pos(3, 0));
        board = board.withNewTile(tile35);
        board = board.withOccupant( new Occupant(Occupant.Kind.PAWN, 350));
        PlacedTile tile67 = new PlacedTile(TileReader.readTileFromCSV(67), PlayerColor.PURPLE, Rotation.NONE, new Pos(0, 1));
        board = board.withNewTile(tile67);
        board = board.withOccupant( new Occupant(Occupant.Kind.PAWN, 672));
        PlacedTile tile51 = new PlacedTile(TileReader.readTileFromCSV(51), PlayerColor.RED, Rotation.NONE, new Pos(-1, 0));
        board = board.withNewTile(tile51);
        board = board.withOccupant( new Occupant(Occupant.Kind.HUT, 511));
        PlacedTile tile1 = new PlacedTile(TileReader.readTileFromCSV(1), PlayerColor.BLUE, Rotation.NONE, new Pos(-2, 0));
        board = board.withNewTile(tile1);
        board = board.withOccupant( new Occupant(Occupant.Kind.PAWN, 14));
        PlacedTile tile17 = new PlacedTile(TileReader.readTileFromCSV(17), PlayerColor.GREEN, Rotation.NONE, new Pos(-3, 0));
        board = board.withNewTile(tile17);
        board = board.withOccupant(new Occupant(Occupant.Kind.PAWN, 173));
        PlacedTile tile21 = new PlacedTile(TileReader.readTileFromCSV(21), PlayerColor.YELLOW, Rotation.NONE, new Pos(-3, -1));
        board = board.withNewTile(tile21);
        PlacedTile tile37 = new PlacedTile(TileReader.readTileFromCSV(37), PlayerColor.PURPLE, Rotation.NONE, new Pos(0, -1));
        board = board.withNewTile(tile37);
        board = board.withOccupant( new Occupant(Occupant.Kind.PAWN, 371));
        PlacedTile tile25 = new PlacedTile(TileReader.readTileFromCSV(25), PlayerColor.RED, Rotation.NONE, new Pos(0, -2));
        board = board.withNewTile(tile25);
        board = board.withOccupant( new Occupant(Occupant.Kind.HUT, 251));
        PlacedTile tile22 = new PlacedTile(TileReader.readTileFromCSV(22), PlayerColor.BLUE, Rotation.HALF_TURN, new Pos(-2, 1));
        board = board.withNewTile(tile22);
        board = board.withOccupant( new Occupant(Occupant.Kind.HUT, 221));

        assertEquals(0, board.occupantCount(PlayerColor.RED, Occupant.Kind.PAWN));
        assertEquals(2, board.occupantCount(PlayerColor.RED, Occupant.Kind.HUT));
        assertEquals(2, board.occupantCount(PlayerColor.BLUE, Occupant.Kind.PAWN));
        assertEquals(1, board.occupantCount(PlayerColor.BLUE, Occupant.Kind.HUT));
        assertEquals(2, board.occupantCount(PlayerColor.GREEN, Occupant.Kind.PAWN));
        assertEquals(0, board.occupantCount(PlayerColor.GREEN, Occupant.Kind.HUT));
        assertEquals(1, board.occupantCount(PlayerColor.YELLOW, Occupant.Kind.PAWN));
        assertEquals(0, board.occupantCount(PlayerColor.YELLOW, Occupant.Kind.HUT));
        assertEquals(2, board.occupantCount(PlayerColor.PURPLE, Occupant.Kind.PAWN));
        assertEquals(0, board.occupantCount(PlayerColor.PURPLE, Occupant.Kind.HUT));
    }

    @Test
    void equalsWorks() {
        PlacedTile startTile = new PlacedTile(TileReader.readTileFromCSV(56), PlayerColor.RED, Rotation.NONE, new Pos(0, 0));
        Board board = Board.EMPTY.withNewTile(startTile);
        PlacedTile tile38 = new PlacedTile(TileReader.readTileFromCSV(38), PlayerColor.BLUE, Rotation.NONE, new Pos(1, 0));
        board = board.withNewTile(tile38);
        board = board.withOccupant( new Occupant(Occupant.Kind.PAWN, 382));
        PlacedTile tile41 = new PlacedTile(TileReader.readTileFromCSV(41), PlayerColor.GREEN, Rotation.NONE, new Pos(2, 0));
        board = board.withNewTile(tile41);
        board = board.withOccupant( new Occupant(Occupant.Kind.PAWN, 410));
        PlacedTile tile35 = new PlacedTile(TileReader.readTileFromCSV(35), PlayerColor.YELLOW, Rotation.HALF_TURN, new Pos(3, 0));
        board = board.withNewTile(tile35);
        board = board.withOccupant( new Occupant(Occupant.Kind.PAWN, 350));
        PlacedTile tile67 = new PlacedTile(TileReader.readTileFromCSV(67), PlayerColor.PURPLE, Rotation.NONE, new Pos(0, 1));
        board = board.withNewTile(tile67);
        board = board.withOccupant( new Occupant(Occupant.Kind.PAWN, 672));
        PlacedTile tile51 = new PlacedTile(TileReader.readTileFromCSV(51), PlayerColor.RED, Rotation.NONE, new Pos(-1, 0));
        board = board.withNewTile(tile51);
        board = board.withOccupant( new Occupant(Occupant.Kind.HUT, 511));
        PlacedTile tile1 = new PlacedTile(TileReader.readTileFromCSV(1), PlayerColor.BLUE, Rotation.NONE, new Pos(-2, 0));
        board = board.withNewTile(tile1);
        board = board.withOccupant( new Occupant(Occupant.Kind.PAWN, 14));
        PlacedTile tile17 = new PlacedTile(TileReader.readTileFromCSV(17), PlayerColor.GREEN, Rotation.NONE, new Pos(-3, 0));
        board = board.withNewTile(tile17);
        board = board.withOccupant(new Occupant(Occupant.Kind.PAWN, 173));
        PlacedTile tile21 = new PlacedTile(TileReader.readTileFromCSV(21), PlayerColor.YELLOW, Rotation.NONE, new Pos(-3, -1));
        board = board.withNewTile(tile21);
        PlacedTile tile37 = new PlacedTile(TileReader.readTileFromCSV(37), PlayerColor.PURPLE, Rotation.NONE, new Pos(0, -1));
        board = board.withNewTile(tile37);
        board = board.withOccupant( new Occupant(Occupant.Kind.PAWN, 371));
        PlacedTile tile25 = new PlacedTile(TileReader.readTileFromCSV(25), PlayerColor.RED, Rotation.NONE, new Pos(0, -2));
        board = board.withNewTile(tile25);
        board = board.withOccupant( new Occupant(Occupant.Kind.HUT, 251));
        PlacedTile tile22 = new PlacedTile(TileReader.readTileFromCSV(22), PlayerColor.BLUE, Rotation.HALF_TURN, new Pos(-2, 1));
        board = board.withNewTile(tile22);
        board = board.withOccupant( new Occupant(Occupant.Kind.HUT, 221));

        startTile = new PlacedTile(TileReader.readTileFromCSV(56), PlayerColor.RED, Rotation.NONE, new Pos(0, 0));
        Board board2 = Board.EMPTY.withNewTile(startTile);
        tile38 = new PlacedTile(TileReader.readTileFromCSV(38), PlayerColor.BLUE, Rotation.NONE, new Pos(1, 0));
        board2 = board2.withNewTile(tile38);
        board2 = board2.withOccupant( new Occupant(Occupant.Kind.PAWN, 382));
        tile41 = new PlacedTile(TileReader.readTileFromCSV(41), PlayerColor.GREEN, Rotation.NONE, new Pos(2, 0));
        board2 = board2.withNewTile(tile41);
        board2 = board2.withOccupant( new Occupant(Occupant.Kind.PAWN, 410));
        tile35 = new PlacedTile(TileReader.readTileFromCSV(35), PlayerColor.YELLOW, Rotation.HALF_TURN, new Pos(3, 0));
        board2 = board2.withNewTile(tile35);
        board2 = board2.withOccupant( new Occupant(Occupant.Kind.PAWN, 350));
        tile67 = new PlacedTile(TileReader.readTileFromCSV(67), PlayerColor.PURPLE, Rotation.NONE, new Pos(0, 1));
        board2 = board2.withNewTile(tile67);
        board2 = board2.withOccupant( new Occupant(Occupant.Kind.PAWN, 672));
        tile51 = new PlacedTile(TileReader.readTileFromCSV(51), PlayerColor.RED, Rotation.NONE, new Pos(-1, 0));
        board2 = board2.withNewTile(tile51);
        board2 = board2.withOccupant( new Occupant(Occupant.Kind.HUT, 511));
        tile1 = new PlacedTile(TileReader.readTileFromCSV(1), PlayerColor.BLUE, Rotation.NONE, new Pos(-2, 0));
        board2 = board2.withNewTile(tile1);
        board2 = board2.withOccupant( new Occupant(Occupant.Kind.PAWN, 14));
        tile17 = new PlacedTile(TileReader.readTileFromCSV(17), PlayerColor.GREEN, Rotation.NONE, new Pos(-3, 0));
        board2 = board2.withNewTile(tile17);
        board2 = board2.withOccupant(new Occupant(Occupant.Kind.PAWN, 173));
        tile21 = new PlacedTile(TileReader.readTileFromCSV(21), PlayerColor.YELLOW, Rotation.NONE, new Pos(-3, -1));
        board2 = board2.withNewTile(tile21);
        tile37 = new PlacedTile(TileReader.readTileFromCSV(37), PlayerColor.PURPLE, Rotation.NONE, new Pos(0, -1));
        board2 = board2.withNewTile(tile37);
        board2 = board2.withOccupant( new Occupant(Occupant.Kind.PAWN, 371));
        tile25 = new PlacedTile(TileReader.readTileFromCSV(25), PlayerColor.RED, Rotation.NONE, new Pos(0, -2));
        board2 = board2.withNewTile(tile25);
        board2 = board2.withOccupant( new Occupant(Occupant.Kind.HUT, 251));
        tile22 = new PlacedTile(TileReader.readTileFromCSV(22), PlayerColor.BLUE, Rotation.HALF_TURN, new Pos(-2, 1));
        board2 = board2.withNewTile(tile22);
        board2 = board2.withOccupant( new Occupant(Occupant.Kind.HUT, 221));

        assertTrue(board.equals(board2));
    }

    @Test
    void equalsWorks2() {
        PlacedTile startTile = new PlacedTile(TileReader.readTileFromCSV(56), PlayerColor.RED, Rotation.NONE, new Pos(0, 0));
        Board board = Board.EMPTY.withNewTile(startTile);
        PlacedTile tile38 = new PlacedTile(TileReader.readTileFromCSV(38), PlayerColor.BLUE, Rotation.NONE, new Pos(1, 0));
        board = board.withNewTile(tile38);
        board = board.withOccupant( new Occupant(Occupant.Kind.PAWN, 382));
        PlacedTile tile41 = new PlacedTile(TileReader.readTileFromCSV(41), PlayerColor.GREEN, Rotation.NONE, new Pos(2, 0));
        board = board.withNewTile(tile41);
        board = board.withOccupant( new Occupant(Occupant.Kind.PAWN, 410));
        PlacedTile tile35 = new PlacedTile(TileReader.readTileFromCSV(35), PlayerColor.YELLOW, Rotation.HALF_TURN, new Pos(3, 0));
        board = board.withNewTile(tile35);
        board = board.withOccupant( new Occupant(Occupant.Kind.PAWN, 350));
        PlacedTile tile67 = new PlacedTile(TileReader.readTileFromCSV(67), PlayerColor.PURPLE, Rotation.NONE, new Pos(0, 1));
        board = board.withNewTile(tile67);
        board = board.withOccupant( new Occupant(Occupant.Kind.PAWN, 672));
        PlacedTile tile51 = new PlacedTile(TileReader.readTileFromCSV(51), PlayerColor.RED, Rotation.NONE, new Pos(-1, 0));
        board = board.withNewTile(tile51);
        board = board.withOccupant( new Occupant(Occupant.Kind.HUT, 511));
        PlacedTile tile1 = new PlacedTile(TileReader.readTileFromCSV(1), PlayerColor.BLUE, Rotation.NONE, new Pos(-2, 0));
        board = board.withNewTile(tile1);
        board = board.withOccupant( new Occupant(Occupant.Kind.PAWN, 14));
        PlacedTile tile17 = new PlacedTile(TileReader.readTileFromCSV(17), PlayerColor.GREEN, Rotation.NONE, new Pos(-3, 0));
        board = board.withNewTile(tile17);
        board = board.withOccupant(new Occupant(Occupant.Kind.PAWN, 173));
        PlacedTile tile21 = new PlacedTile(TileReader.readTileFromCSV(21), PlayerColor.YELLOW, Rotation.NONE, new Pos(-3, -1));
        board = board.withNewTile(tile21);
        PlacedTile tile37 = new PlacedTile(TileReader.readTileFromCSV(37), PlayerColor.PURPLE, Rotation.NONE, new Pos(0, -1));
        board = board.withNewTile(tile37);
        board = board.withOccupant( new Occupant(Occupant.Kind.PAWN, 371));
        PlacedTile tile25 = new PlacedTile(TileReader.readTileFromCSV(25), PlayerColor.RED, Rotation.NONE, new Pos(0, -2));
        board = board.withNewTile(tile25);
        board = board.withOccupant( new Occupant(Occupant.Kind.HUT, 251));
        PlacedTile tile22 = new PlacedTile(TileReader.readTileFromCSV(22), PlayerColor.BLUE, Rotation.HALF_TURN, new Pos(-2, 1));
        board = board.withNewTile(tile22);
        board = board.withOccupant( new Occupant(Occupant.Kind.HUT, 221));

        startTile = new PlacedTile(TileReader.readTileFromCSV(56), PlayerColor.RED, Rotation.NONE, new Pos(0, 0));
        Board board2 = Board.EMPTY.withNewTile(startTile);
        tile38 = new PlacedTile(TileReader.readTileFromCSV(38), PlayerColor.BLUE, Rotation.NONE, new Pos(1, 0));
        board2 = board2.withNewTile(tile38);
        board2 = board2.withOccupant( new Occupant(Occupant.Kind.PAWN, 382));
        tile41 = new PlacedTile(TileReader.readTileFromCSV(41), PlayerColor.GREEN, Rotation.NONE, new Pos(2, 0));
        board2 = board2.withNewTile(tile41);
        board2 = board2.withOccupant( new Occupant(Occupant.Kind.PAWN, 410));
        tile35 = new PlacedTile(TileReader.readTileFromCSV(35), PlayerColor.YELLOW, Rotation.HALF_TURN, new Pos(3, 0));
        board2 = board2.withNewTile(tile35);
        board2 = board2.withOccupant( new Occupant(Occupant.Kind.PAWN, 350));
        tile67 = new PlacedTile(TileReader.readTileFromCSV(67), PlayerColor.PURPLE, Rotation.NONE, new Pos(0, 1));
        board2 = board2.withNewTile(tile67);
        board2 = board2.withOccupant( new Occupant(Occupant.Kind.PAWN, 672));
        tile51 = new PlacedTile(TileReader.readTileFromCSV(51), PlayerColor.RED, Rotation.NONE, new Pos(-1, 0));
        board2 = board2.withNewTile(tile51);
        board2 = board2.withOccupant( new Occupant(Occupant.Kind.HUT, 511));
        tile1 = new PlacedTile(TileReader.readTileFromCSV(1), PlayerColor.BLUE, Rotation.NONE, new Pos(-2, 0));
        board2 = board2.withNewTile(tile1);
        board2 = board2.withOccupant( new Occupant(Occupant.Kind.PAWN, 14));
        tile17 = new PlacedTile(TileReader.readTileFromCSV(17), PlayerColor.GREEN, Rotation.NONE, new Pos(-3, 0));
        board2 = board2.withNewTile(tile17);
        board2 = board2.withOccupant(new Occupant(Occupant.Kind.PAWN, 173));
        tile21 = new PlacedTile(TileReader.readTileFromCSV(21), PlayerColor.YELLOW, Rotation.NONE, new Pos(-3, -1));
        board2 = board2.withNewTile(tile21);
        tile37 = new PlacedTile(TileReader.readTileFromCSV(37), PlayerColor.PURPLE, Rotation.NONE, new Pos(0, -1));
        board2 = board2.withNewTile(tile37);
        board2 = board2.withOccupant( new Occupant(Occupant.Kind.PAWN, 371));
        tile25 = new PlacedTile(TileReader.readTileFromCSV(25), PlayerColor.RED, Rotation.NONE, new Pos(0, -2));
        board2 = board2.withNewTile(tile25);
        board2 = board2.withOccupant( new Occupant(Occupant.Kind.PAWN, 253));
        tile22 = new PlacedTile(TileReader.readTileFromCSV(22), PlayerColor.BLUE, Rotation.HALF_TURN, new Pos(-2, 1));
        board2 = board2.withNewTile(tile22);
        board2 = board2.withOccupant( new Occupant(Occupant.Kind.HUT, 221));

        assertFalse(board.equals(board2));
    }

}
