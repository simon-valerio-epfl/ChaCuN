package ch.epfl.chacun;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class InitialZonePartitionsTest {

    @Test
    void testProfAddTileChickenAttack() {

        Zone.Meadow meadow = new Zone.Meadow(560, List.of(), null);
        Zone.Meadow meadow1 = new Zone.Meadow(562, List.of(), null);
        Zone.Forest forest = new Zone.Forest(561, Zone.Forest.Kind.WITH_MENHIR);
        Zone.Lake lake = new Zone.Lake(568, 1, null);
        Zone.River river = new Zone.River(563, 0, lake);

        TileSide.Meadow meadowSide = new TileSide.Meadow(meadow);
        TileSide.Forest forestSide = new TileSide.Forest(forest);
        TileSide.Forest forestSide1 = new TileSide.Forest(forest);
        TileSide.River riverSide = new TileSide.River(meadow1, river, meadow);

        Tile tile = new Tile(0, Tile.Kind.NORMAL, meadowSide, forestSide, forestSide1, riverSide);

        ZonePartitions zonePartitions = ZonePartitions.EMPTY;
        ZonePartitions.Builder builder = new ZonePartitions.Builder(zonePartitions);
        builder.addTile(tile);

        ZonePartitions zonePartitions1 = builder.build();

        // deux près  {{560}[2], {562}[1]}
        assertEquals(2, zonePartitions1.meadows().areas().size());
        assertEquals(2, zonePartitions1.meadows().areaContaining(meadow).openConnections());
        assertEquals(1, zonePartitions1.meadows().areaContaining(meadow1).openConnections());
        // une forêt {{561}[2]},
        assertEquals(1, zonePartitions1.forests().areas().size());
        assertEquals(2, zonePartitions1.forests().areaContaining(forest).openConnections());
        // une rivière {{563}[1]},
        assertEquals(1, zonePartitions1.rivers().areas().size());
        assertEquals(1, zonePartitions1.rivers().areaContaining(river).openConnections());
        // zone aquatique {{563,568}[1]},
        assertEquals(1, zonePartitions1.riverSystems().areas().size());
        assertEquals(1, zonePartitions1.riverSystems().areas().stream().findFirst().get().openConnections());
    }

    @Test
    void testConnectSides() {

        Zone.Meadow meadow = new Zone.Meadow(560, List.of(), null);
        Zone.Meadow meadow1 = new Zone.Meadow(562, List.of(), null);

        Zone.Forest forest = new Zone.Forest(561, Zone.Forest.Kind.WITH_MENHIR);
        Zone.Lake lake = new Zone.Lake(568, 1, null);
        Zone.River river = new Zone.River(563, 0, lake);

        TileSide.Meadow meadowSide = new TileSide.Meadow(meadow);
        TileSide.Forest forestSide = new TileSide.Forest(forest);
        TileSide.Forest forestSide1 = new TileSide.Forest(forest);
        TileSide.River riverSide = new TileSide.River(meadow1, river, meadow);

        Tile tile = new Tile(0, Tile.Kind.NORMAL, meadowSide, forestSide, forestSide1, riverSide);

        ZonePartitions zonePartitions = ZonePartitions.EMPTY;
        ZonePartitions.Builder builder = new ZonePartitions.Builder(zonePartitions);
        builder.addTile(tile);

        // là on créé une nouvelle tuile à connecter

        Zone.Meadow meadowOtherZone = new Zone.Meadow(780, List.of(), null);

        TileSide.Meadow meadowSideOther1 = new TileSide.Meadow(meadowOtherZone);
        TileSide.Meadow meadowSideOther2 = new TileSide.Meadow(meadowOtherZone);
        TileSide.Meadow meadowSideOther3 = new TileSide.Meadow(meadowOtherZone);
        TileSide.Meadow meadowSideOther4 = new TileSide.Meadow(meadowOtherZone);

        Tile otherTile = new Tile(1, Tile.Kind.NORMAL, meadowSideOther1, meadowSideOther2, meadowSideOther3, meadowSideOther4);
        builder.addTile(otherTile);

        // ici on devrait avoir 3 aires de meadows, meadow1, meadow2 et le meadow de la nouvelle tuile
        assertEquals(3, builder.build().meadows().areas().size());
        builder.connectSides(meadowSideOther3, meadowSide);
        assertEquals(2, builder.build().meadows().areas().size());
    }

    @Test
    void testConnectSidesRivers() {

        Zone.Meadow meadow = new Zone.Meadow(560, List.of(), null);
        Zone.Meadow meadow1 = new Zone.Meadow(562, List.of(), null);

        Zone.Forest forest = new Zone.Forest(561, Zone.Forest.Kind.WITH_MENHIR);
        Zone.Lake lake = new Zone.Lake(568, 1, null);
        Zone.River river = new Zone.River(563, 0, lake);

        TileSide.Meadow meadowSide = new TileSide.Meadow(meadow);
        TileSide.Forest forestSide = new TileSide.Forest(forest);
        TileSide.Forest forestSide1 = new TileSide.Forest(forest);
        TileSide.River riverSide = new TileSide.River(meadow1, river, meadow);

        Tile tile = new Tile(0, Tile.Kind.NORMAL, meadowSide, forestSide, forestSide1, riverSide);

        ZonePartitions zonePartitions = ZonePartitions.EMPTY;
        ZonePartitions.Builder builder = new ZonePartitions.Builder(zonePartitions);
        builder.addTile(tile);

        // là on créé une nouvelle tuile à connecter

        Zone.Meadow meadowOtherZone = new Zone.Meadow(780, List.of(), null);
        Zone.River riverOther = new Zone.River(781, 0, null);

        TileSide.Meadow meadowSideOther1 = new TileSide.Meadow(meadowOtherZone);
        TileSide.Meadow meadowSideOther2 = new TileSide.Meadow(meadowOtherZone);
        TileSide.Meadow meadowSideOther3 = new TileSide.Meadow(meadowOtherZone);
        TileSide.River riverSideOther = new TileSide.River(meadowOtherZone, riverOther, meadowOtherZone);

        Tile otherTile = new Tile(1, Tile.Kind.NORMAL, meadowSideOther1, meadowSideOther2, meadowSideOther3, riverSideOther);
        builder.addTile(otherTile);

        assertEquals(2, builder.build().rivers().areas().size());
        builder.connectSides(riverSideOther, riverSide);
        assertEquals(1, builder.build().rivers().areas().size());

    }


    @Test
    void testAddInitialOccupant() {
        Zone.Meadow meadow = new Zone.Meadow(560, List.of(), null);
        Zone.Meadow meadow1 = new Zone.Meadow(562, List.of(), null);

        Zone.Forest forest = new Zone.Forest(561, Zone.Forest.Kind.WITH_MENHIR);
        Zone.Lake lake = new Zone.Lake(568, 1, null);
        Zone.River river = new Zone.River(563, 0, lake);

        TileSide.Meadow meadowSide = new TileSide.Meadow(meadow);
        TileSide.Forest forestSide = new TileSide.Forest(forest);
        TileSide.Forest forestSide1 = new TileSide.Forest(forest);
        TileSide.River riverSide = new TileSide.River(meadow1, river, meadow);

        Tile tile = new Tile(0, Tile.Kind.NORMAL, meadowSide, forestSide, forestSide1, riverSide);

        ZonePartitions zonePartitions = ZonePartitions.EMPTY;
        ZonePartitions.Builder builder = new ZonePartitions.Builder(zonePartitions);

        builder.addTile(tile);

        builder.addInitialOccupant(PlayerColor.RED, Occupant.Kind.HUT, river);
        assertEquals(1, builder.build().riverSystems().areaContaining(river).occupants().size());
    }

}
