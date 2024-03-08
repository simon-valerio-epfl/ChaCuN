package ch.epfl.chacun;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class InitialZonePartitionTest {

    @Test
    public void testAreaContaining() {
        Zone.Meadow meadow = new Zone.Meadow(0, List.of(),null);
        Zone.Meadow meadow1 = new Zone.Meadow(1, List.of(),null);
        Area<Zone.Meadow> area = new Area<>(Set.of(meadow), List.of(), 0);
        Area<Zone.Meadow> area1 = new Area<>(Set.of(meadow1), List.of(), 0);
        ZonePartition<Zone.Meadow> zonePartition = new ZonePartition<>(Set.of(area, area1));
        assertEquals(area, zonePartition.areaContaining(meadow));
        Zone.Meadow meadow3 = new Zone.Meadow(3, List.of(),null);
        assertThrows(IllegalArgumentException.class, () -> zonePartition.areaContaining(meadow3));
    }

    @Test
    public void testZoneUnion () {
        Zone.Meadow meadow = new Zone.Meadow(0, List.of(),null);
        Zone.Meadow meadow1 = new Zone.Meadow(1, List.of(),null);
        Area<Zone.Meadow> area = new Area<>(Set.of(meadow), List.of(), 2);

        ZonePartition.Builder<Zone.Meadow> builder = new ZonePartition.Builder<>(new ZonePartition<>(Set.of(area)));
        builder.addSingleton(meadow1, 2);

        builder.union(meadow, meadow1);
        ZonePartition<Zone.Meadow> zonePartition = builder.build();
        assertEquals(1, zonePartition.areas().size());
        assertEquals(2, zonePartition.areas().stream().findFirst().get().openConnections());

        Zone.Meadow meadow2 = new Zone.Meadow(2, List.of(),null);
        builder.union(meadow, meadow1);
        assertEquals(1, zonePartition.areas().size());
    }

    @Test
    public void testAddInitialOccupant() {
        Zone.Meadow meadow = new Zone.Meadow(0, List.of(),null);
        Zone.Meadow meadow1 = new Zone.Meadow(1, List.of(),null);
        Area<Zone.Meadow> area = new Area<>(Set.of(meadow), List.of(), 2);

        ZonePartition.Builder<Zone.Meadow> builder = new ZonePartition.Builder<>(new ZonePartition<>(Set.of(area)));
        builder.addSingleton(meadow1, 2);

        builder.addInitialOccupant(meadow, PlayerColor.RED);
        ZonePartition<Zone.Meadow> zonePartition = builder.build();
        assertEquals(1, zonePartition.areaContaining(meadow).occupants().size());
        assertEquals(PlayerColor.RED, zonePartition.areaContaining(meadow).occupants().stream().findFirst().get());
        assertThrows(IllegalArgumentException.class, () -> builder.addInitialOccupant(meadow, PlayerColor.RED));
    }

    @Test
    public void testRemoveAllOccupantsOf() {
        Zone.Meadow meadow = new Zone.Meadow(0, List.of(),null);
        Zone.Meadow meadow1 = new Zone.Meadow(1, List.of(),null);
        Area<Zone.Meadow> area = new Area<>(Set.of(meadow), List.of(), 2);

        ZonePartition.Builder<Zone.Meadow> builder = new ZonePartition.Builder<>(new ZonePartition<>(Set.of(area)));
        builder.addSingleton(meadow1, 2);

        assertEquals(2, builder.build().areas().size());
        builder.addInitialOccupant(meadow, PlayerColor.RED);
        assertEquals(1, builder.build().areaContaining(meadow).occupants().size());
        builder.removeAllOccupantsOf(builder.build().areaContaining(meadow));
        ZonePartition<Zone.Meadow> zonePartition = builder.build();
        assertEquals(0, zonePartition.areaContaining(meadow).occupants().size());
    }

    @Test
    public void testUnion() {
        Zone.Meadow meadow = new Zone.Meadow(0, List.of(),null);
        Zone.Meadow meadow1 = new Zone.Meadow(1, List.of(),null);
        Area<Zone.Meadow> area = new Area<>(Set.of(meadow), List.of(), 2);

        ZonePartition.Builder<Zone.Meadow> builder = new ZonePartition.Builder<>(new ZonePartition<>(Set.of(area)));
        builder.addSingleton(meadow1, 2);

        builder.union(meadow, meadow1);
        ZonePartition<Zone.Meadow> zonePartition = builder.build();
        assertEquals(1, zonePartition.areas().size());
        assertEquals(2, zonePartition.areas().stream().findFirst().get().openConnections());
    }

}