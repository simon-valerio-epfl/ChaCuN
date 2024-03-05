package ch.epfl.chacun;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ZonePartitionTest {

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
    public void

}