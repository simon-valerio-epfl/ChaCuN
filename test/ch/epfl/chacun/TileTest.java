package ch.epfl.chacun;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TileTest {

    @Test
    public void testTileZones() {
        Zone.Meadow meadow = new Zone.Meadow(613, List.of(new Animal(6131, Animal.Kind.AUROCHS)), Zone.Meadow.SpecialPower.HUNTING_TRAP);
        Zone.Meadow meadow2 = new Zone.Meadow(614, List.of(new Animal(6141, Animal.Kind.MAMMOTH)), null);
        Zone.Forest forest2 = new Zone.Forest(615, Zone.Forest.Kind.PLAIN);
        Zone.Forest forest = new Zone.Forest(612, Zone.Forest.Kind.WITH_MENHIR);
        TileSide forestSide = new TileSide.Forest(forest);
        TileSide meadowSide = new TileSide.Meadow(meadow);
        TileSide forestSide2 = new TileSide.Forest(forest2);
        TileSide meadowSide2 = new TileSide.Meadow(meadow2);
        Tile tile = new Tile(1, Tile.Kind.START, forestSide, meadowSide, forestSide2, meadowSide2);


        Set<Zone> expectedSizeZones = new HashSet<>();
        expectedSizeZones.add(forest);
        expectedSizeZones.add(forest2);
        expectedSizeZones.add(meadow2);
        expectedSizeZones.add(meadow);
        assertEquals(expectedSizeZones, tile.sideZones());
        assertEquals(expectedSizeZones, tile.zones());
        Zone forestZone = new Zone.Forest(1, Zone.Forest.Kind.PLAIN);

        Zone.Lake lakeZone = new Zone.Lake(3, 12, null);
        Zone.River riverZone = new Zone.River(2, 12, new Zone.Lake(3, 12, null));
        Zone.Forest jungleZone = new Zone.Forest(4, Zone.Forest.Kind.WITH_MUSHROOMS);

        TileSide riverSide = new TileSide.River(meadow, riverZone, meadow2);
        TileSide jungleSide = new TileSide.Forest(jungleZone);
        Tile tile2 = new Tile(1, Tile.Kind.START, forestSide, riverSide, forestSide2, jungleSide);

        Set<Zone> expectedSizeZones2 = new HashSet<>();
        expectedSizeZones2.add(riverZone);
        expectedSizeZones2.add(meadow);
        expectedSizeZones2.add(meadow2);
        expectedSizeZones2.add(forest);
        expectedSizeZones2.add(forest2);
        expectedSizeZones2.add(jungleZone);

        assertEquals(expectedSizeZones2, tile2.sideZones());

        expectedSizeZones2.add(lakeZone);

        assertEquals(expectedSizeZones2, tile2.zones());
    }

}
