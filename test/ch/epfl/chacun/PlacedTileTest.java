package ch.epfl.chacun;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class PlacedTileTest {

    @Test
    public void testSideReturnsCorrectValue() {
        Zone.Meadow meadow = new Zone.Meadow(613, List.of(new Animal(6131, Animal.Kind.AUROCHS)), Zone.Meadow.SpecialPower.HUNTING_TRAP);
        Zone.Meadow meadow2 = new Zone.Meadow(614, List.of(new Animal(6141, Animal.Kind.MAMMOTH)), null);
        Zone.Forest forest2 = new Zone.Forest(615, Zone.Forest.Kind.PLAIN);
        Zone.Forest forest = new Zone.Forest(612, Zone.Forest.Kind.WITH_MENHIR);
        TileSide forestSide = new TileSide.Forest(forest);
        TileSide meadowSide = new TileSide.Meadow(meadow);
        TileSide forestSide2 = new TileSide.Forest(forest2);
        TileSide meadowSide2 = new TileSide.Meadow(meadow2);
        Tile tile = new Tile(1, Tile.Kind.START, forestSide, meadowSide, forestSide2, meadowSide2);
        PlayerColor Habib = PlayerColor.RED;

        PlacedTile placedTileRight = new PlacedTile(tile, Habib, Rotation.RIGHT, new Pos(0, 0));
        assertEquals(meadowSide2, placedTileRight.side(Direction.N));
        assertEquals(forestSide, placedTileRight.side(Direction.E));

        PlacedTile placedTileLeft = new PlacedTile(tile, Habib, Rotation.LEFT, new Pos(0, 0));
        assertEquals(meadowSide, placedTileLeft.side(Direction.N));
    }

    @Test
    public void testZoneWithIdReturnsCorrectZone() {
        Zone.Meadow meadow = new Zone.Meadow(613, List.of(new Animal(6131, Animal.Kind.AUROCHS)), Zone.Meadow.SpecialPower.HUNTING_TRAP);
        Zone.Meadow meadow2 = new Zone.Meadow(614, List.of(new Animal(6141, Animal.Kind.MAMMOTH)), null);
        Zone.Forest forest2 = new Zone.Forest(615, Zone.Forest.Kind.PLAIN);
        Zone.Forest forest = new Zone.Forest(612, Zone.Forest.Kind.WITH_MENHIR);
        TileSide forestSide = new TileSide.Forest(forest);
        TileSide meadowSide = new TileSide.Meadow(meadow);
        TileSide forestSide2 = new TileSide.Forest(forest2);
        TileSide meadowSide2 = new TileSide.Meadow(meadow2);
        Tile tile = new Tile(1, Tile.Kind.START, forestSide, meadowSide, forestSide2, meadowSide2);
        PlayerColor Habib = PlayerColor.RED;

        PlacedTile placedTile = new PlacedTile(tile, Habib, Rotation.RIGHT, new Pos(0, 0));

        assertEquals(forest, placedTile.zoneWithId(612));
        assertThrows(IllegalArgumentException.class, () -> {
            placedTile.zoneWithId(100000);
        });
    }

    @Test
    public void testTypeZonesReturnsCorrectZones() {
        Zone.Meadow meadow = new Zone.Meadow(613, List.of(new Animal(6131, Animal.Kind.AUROCHS)), Zone.Meadow.SpecialPower.HUNTING_TRAP);
        Zone.Meadow meadow2 = new Zone.Meadow(614, List.of(new Animal(6141, Animal.Kind.MAMMOTH)), null);
        Zone.Forest forest2 = new Zone.Forest(615, Zone.Forest.Kind.PLAIN);
        Zone.Forest forest = new Zone.Forest(612, Zone.Forest.Kind.WITH_MENHIR);
        TileSide forestSide = new TileSide.Forest(forest);
        TileSide meadowSide = new TileSide.Meadow(meadow);
        TileSide forestSide2 = new TileSide.Forest(forest2);
        TileSide meadowSide2 = new TileSide.Meadow(meadow2);
        Tile tile = new Tile(1, Tile.Kind.START, forestSide, meadowSide, forestSide2, meadowSide2);
        PlayerColor Habib = PlayerColor.RED;

        PlacedTile placedTile = new PlacedTile(tile, Habib, Rotation.RIGHT, new Pos(0, 0));

        final Set<Zone> expectedForestSet = new HashSet<>(List.of(new Zone[]{forest, forest2}));
        assertEquals(expectedForestSet, placedTile.forestZones());

        final Set<Zone> expectedMeadowSet = new HashSet<>(List.of(new Zone[]{meadow2, meadow}));
        assertEquals(expectedMeadowSet, placedTile.meadowZones());

        assertEquals(new HashSet<>(), placedTile.riverZones());
    }

    @Test
    public void testSpecialPowerZoneReturnsCorrectZone() {
        Zone.Meadow meadow = new Zone.Meadow(613, List.of(new Animal(6131, Animal.Kind.AUROCHS)), Zone.Meadow.SpecialPower.HUNTING_TRAP);
        Zone.Meadow meadow2 = new Zone.Meadow(614, List.of(new Animal(6141, Animal.Kind.MAMMOTH)), null);
        Zone.Forest forest2 = new Zone.Forest(615, Zone.Forest.Kind.PLAIN);
        Zone.Forest forest = new Zone.Forest(612, Zone.Forest.Kind.WITH_MENHIR);
        TileSide forestSide = new TileSide.Forest(forest);
        TileSide meadowSide = new TileSide.Meadow(meadow);
        TileSide forestSide2 = new TileSide.Forest(forest2);
        TileSide meadowSide2 = new TileSide.Meadow(meadow2);
        Tile tile = new Tile(1, Tile.Kind.START, forestSide, meadowSide, forestSide2, meadowSide2);
        PlayerColor Habib = PlayerColor.RED;

        PlacedTile placedTile = new PlacedTile(tile, Habib, Rotation.RIGHT, new Pos(0, 0));

        assertEquals(meadow, placedTile.specialPowerZone());

        Zone.Meadow meadow3 = new Zone.Meadow(613, List.of(new Animal(6131, Animal.Kind.AUROCHS)), null);
        TileSide meadowSide3 = new TileSide.Meadow(meadow3);
        Tile tile2 = new Tile(1, Tile.Kind.START, forestSide, meadowSide3, forestSide2, meadowSide2);

        PlacedTile placedTile2 = new PlacedTile(tile2, Habib, Rotation.RIGHT, new Pos(0, 0));
        assertNull(placedTile2.specialPowerZone());
    }

    @Test
    public void testPotentialOccupantsReturnsCorrectValue() {
        Zone.Meadow meadow = new Zone.Meadow(613, List.of(new Animal(6131, Animal.Kind.AUROCHS)), Zone.Meadow.SpecialPower.HUNTING_TRAP);
        Zone.Meadow meadow2 = new Zone.Meadow(614, List.of(new Animal(6141, Animal.Kind.MAMMOTH)), null);
        Zone.Forest forest2 = new Zone.Forest(615, Zone.Forest.Kind.PLAIN);
        Zone.Forest forest = new Zone.Forest(612, Zone.Forest.Kind.WITH_MENHIR);
        TileSide forestSide = new TileSide.Forest(forest);
        TileSide meadowSide = new TileSide.Meadow(meadow);
        TileSide forestSide2 = new TileSide.Forest(forest2);
        TileSide meadowSide2 = new TileSide.Meadow(meadow2);
        Tile tile = new Tile(1, Tile.Kind.NORMAL, forestSide, meadowSide, forestSide2, meadowSide2);
        PlayerColor Habib = PlayerColor.RED;

        PlacedTile placedTile = new PlacedTile(tile, Habib, Rotation.RIGHT, new Pos(0, 0));

        Set<Occupant> set = new HashSet<>();
        set.add(new Occupant(Occupant.Kind.PAWN, 613));
        set.add(new Occupant(Occupant.Kind.PAWN, 614));
        set.add(new Occupant(Occupant.Kind.PAWN, 615));
        set.add(new Occupant(Occupant.Kind.PAWN, 612));

        assertEquals(set, placedTile.potentialOccupants());

        PlacedTile placedTile2 = new PlacedTile(tile, null, Rotation.RIGHT, new Pos(0, 0));

        assertEquals(new HashSet<>(), placedTile2.potentialOccupants());

        Zone.River river = new Zone.River(623, 3, null);
        Zone.Lake lake = new Zone.Lake(628, 0, Zone.SpecialPower.LOGBOAT);
        Zone.River river2 = new Zone.River(624, 2, lake);

        TileSide riverSide1 = new TileSide.River(meadow, river, meadow2);
        TileSide riverSide2 = new TileSide.River(meadow2, river2, meadow);

        Tile tile2 = new Tile(1, Tile.Kind.NORMAL, forestSide, riverSide1, riverSide2, meadowSide2);
        PlacedTile placedTile3 = new PlacedTile(tile2, Habib, Rotation.RIGHT, new Pos(0, 0));

        Set<Occupant> set2 = new HashSet<>();
        set2.add(new Occupant(Occupant.Kind.PAWN, 623));
        set2.add(new Occupant(Occupant.Kind.PAWN, 624));
        set2.add(new Occupant(Occupant.Kind.HUT, 628));
        set2.add(new Occupant(Occupant.Kind.HUT, 623));

        set2.add(new Occupant(Occupant.Kind.PAWN, 613));
        set2.add(new Occupant(Occupant.Kind.PAWN, 614));
        set2.add(new Occupant(Occupant.Kind.PAWN, 612));

        assertEquals(set2, placedTile3.potentialOccupants());
    }

    @Test
    public void testWithOccupantWorks() {
        Zone.Meadow meadow = new Zone.Meadow(613, List.of(new Animal(6131, Animal.Kind.AUROCHS)), Zone.Meadow.SpecialPower.HUNTING_TRAP);
        Zone.Meadow meadow2 = new Zone.Meadow(614, List.of(new Animal(6141, Animal.Kind.MAMMOTH)), null);
        Zone.Forest forest2 = new Zone.Forest(615, Zone.Forest.Kind.PLAIN);
        Zone.Forest forest = new Zone.Forest(612, Zone.Forest.Kind.WITH_MENHIR);
        TileSide forestSide = new TileSide.Forest(forest);
        TileSide meadowSide = new TileSide.Meadow(meadow);
        TileSide forestSide2 = new TileSide.Forest(forest2);
        TileSide meadowSide2 = new TileSide.Meadow(meadow2);
        Tile tile = new Tile(1, Tile.Kind.NORMAL, forestSide, meadowSide, forestSide2, meadowSide2);
        PlayerColor Habib = PlayerColor.RED;

        PlacedTile placedTile = new PlacedTile(tile, Habib, Rotation.RIGHT, new Pos(0, 0));

        Occupant occupant = new Occupant(Occupant.Kind.PAWN, 613);
        assertThrows(IllegalArgumentException.class, () -> {
            PlacedTile withOccupant = placedTile.withOccupant(occupant);
            withOccupant.withOccupant(new Occupant(Occupant.Kind.PAWN, 613));
        });

        PlacedTile withOccupant = placedTile.withOccupant(new Occupant(Occupant.Kind.PAWN, 613));
        assertEquals(occupant, withOccupant.occupant());

        assertEquals(613, withOccupant.idOfZoneOccupiedBy(Occupant.Kind.PAWN));
        assertEquals(-1, withOccupant.idOfZoneOccupiedBy(Occupant.Kind.HUT));

    }

}
