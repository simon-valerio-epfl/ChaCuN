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

}
