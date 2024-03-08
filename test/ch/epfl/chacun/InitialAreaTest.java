package ch.epfl.chacun;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InitialAreaTest {

    @Test
    public void areaOccupantsSorted() {
        List<PlayerColor> occupants = List.of(PlayerColor.BLUE, PlayerColor.RED, PlayerColor.GREEN);
        Area<Zone.Forest> area = new Area<>(Set.of(), occupants, 0);
        assertEquals(List.of(PlayerColor.RED, PlayerColor.BLUE, PlayerColor.GREEN), area.occupants());
    }

    @Test
    public void areaOccupantsUnmodifiable() {
        List<PlayerColor> occupants = List.of(PlayerColor.BLUE, PlayerColor.RED, PlayerColor.GREEN);
        Area<Zone.Forest> area = new Area<>(Set.of(), occupants, 0);
        assertThrows(UnsupportedOperationException.class, () -> area.occupants().add(PlayerColor.YELLOW));
    }

    @Test
    public void areaIsOccupied() {
        List<PlayerColor> occupants = List.of(PlayerColor.BLUE, PlayerColor.RED, PlayerColor.GREEN);
        Area<Zone.Forest> area = new Area<>(Set.of(), occupants, 0);
        assertTrue(area.isOccupied());
        Area<Zone.Forest> area2 = new Area<>(Set.of(), Collections.emptyList(), 0);
        assertFalse(area2.isOccupied());
    }

    @Test
    public void areaHasMenhir() {
        List<PlayerColor> occupants = List.of(PlayerColor.BLUE, PlayerColor.RED, PlayerColor.GREEN);
        Zone.Forest forestWith = new Zone.Forest(0, Zone.Forest.Kind.WITH_MENHIR);
        Area<Zone.Forest> areaWith = new Area<>(Set.of(forestWith), occupants, 0);
        assertTrue(Area.hasMenhir(areaWith));
        Zone.Forest forestWithout = new Zone.Forest(0, Zone.Forest.Kind.PLAIN);
        Area<Zone.Forest> areaWithout = new Area<>(Set.of(forestWithout), occupants, 0);
        assertFalse(Area.hasMenhir(areaWithout));
    }

    @Test
    public void areaMajorityOccupants() {
        List<PlayerColor> redMajority = List.of(PlayerColor.BLUE, PlayerColor.RED, PlayerColor.RED, PlayerColor.GREEN);
        Area<Zone.Forest> area = new Area<>(Set.of(), redMajority, 0);
        assertEquals(Set.of(PlayerColor.RED), area.majorityOccupants());
        List<PlayerColor> redAndBlueMajority = List.of(PlayerColor.BLUE, PlayerColor.RED, PlayerColor.RED, PlayerColor.BLUE, PlayerColor.GREEN);
        Area<Zone.Forest> area2 = new Area<>(Set.of(), redAndBlueMajority, 0);
        assertEquals(Set.of(PlayerColor.RED, PlayerColor.BLUE), area2.majorityOccupants());
        List<PlayerColor> emptyMajority = List.of();
        Area<Zone.Forest> area3 = new Area<>(Set.of(), emptyMajority, 0);
        assertEquals(Set.of(), area3.majorityOccupants());
    }

    @Test
    public void areaIsClosed() {
        Area<Zone.Forest> area = new Area<>(Set.of(), List.of(), 0);
        assertTrue(area.isClosed());
        Area<Zone.Forest> area2 = new Area<>(Set.of(), List.of(), 10);
        assertFalse(area2.isClosed());
        assertThrows(IllegalArgumentException.class, () -> new Area<>(Set.of(), List.of(), -1));
    }

    @Test
    public void areaZonesImmutable() {
        Zone.Forest forest = new Zone.Forest(0, Zone.Forest.Kind.PLAIN);
        HashSet<Zone.Forest> zones = new HashSet<>(Set.of(forest));
        Area<Zone.Forest> area = new Area<>(zones, List.of(), 0);
        assertThrows(UnsupportedOperationException.class, () -> area.zones().add(forest));
        zones.clear();
        assertEquals(Set.of(forest), area.zones());
    }

    @Test
    public void areaMushroomsGroupCount() {
        Zone.Forest forest = new Zone.Forest(0, Zone.Forest.Kind.WITH_MUSHROOMS);
        Zone.Forest forest2 = new Zone.Forest(1, Zone.Forest.Kind.WITH_MUSHROOMS);
        Area<Zone.Forest> area = new Area<>(Set.of(forest, forest2), List.of(), 0);
        assertEquals(2, Area.mushroomGroupCount(area));
        Zone.Forest forest3 = new Zone.Forest(2, Zone.Forest.Kind.PLAIN);
        Area<Zone.Forest> area2 = new Area<>(Set.of(forest3), List.of(), 0);
        assertEquals(0, Area.mushroomGroupCount(area2));
    }

    @Test
    public void areaTestAnimals() {
        Animal a1 = new Animal(1, Animal.Kind.AUROCHS);
        Animal a2 = new Animal(2, Animal.Kind.MAMMOTH);
        Zone.Meadow meadow = new Zone.Meadow(0, List.of(a1, a2), null);
        Area<Zone.Meadow> area = new Area<>(Set.of(meadow), List.of(), 0);
        assertEquals(Set.of(a1, a2), Area.animals(area, Set.of()));
        assertEquals(Set.of(a1), Area.animals(area, Set.of(a2)));
        assertEquals(Set.of(), Area.animals(area, Set.of(a1, a2)));
        Zone.Meadow meadow2 = new Zone.Meadow(1, List.of(), null);
        Area<Zone.Meadow> area2 = new Area<>(Set.of(meadow2), List.of(), 0);
        assertEquals(Set.of(), Area.animals(area2, Set.of()));
    }

    @Test
    public void areaRiverFishCount() {
        Zone.River river = new Zone.River(0, 3, null);
        Area<Zone.River> area = new Area<>(Set.of(river), List.of(), 0);
        assertEquals(3, Area.riverFishCount(area));
        Zone.River river2 = new Zone.River(1, 0, null);
        Area<Zone.River> area2 = new Area<>(Set.of(river2), List.of(), 0);
        assertEquals(0, Area.riverFishCount(area2));
        Zone.Lake lake = new Zone.Lake(2, 5, null);
        Zone.River river3 = new Zone.River(3, 0, lake);
        Area<Zone.River> area3 = new Area<>(Set.of(river3), List.of(), 0);
        assertEquals(5, Area.riverFishCount(area3));
        Zone.River river4 = new Zone.River(4, 0, null);
        Area<Zone.River> area4 = new Area<>(Set.of(river4), List.of(), 0);
        assertEquals(0, Area.riverFishCount(area4));
        Area<Zone.River> area5 = new Area<>(Set.of(), List.of(), 0);
        assertEquals(0, Area.riverFishCount(area5));
    }

    @Test
    public void areaLakeCount() {
        Zone.Lake lake = new Zone.Lake(0, 3, null);
        Area<Zone.Water> area = new Area<>(Set.of(lake), List.of(), 0);
        assertEquals(1, Area.lakeCount(area));
    }

    @Test
    public void areaRiverSystemFishCount() {
        Zone.Lake lake = new Zone.Lake(0, 3, null);
        Zone.River river = new Zone.River(1, 2, lake);
        Zone.River river2 = new Zone.River(2, 1, lake);
        Area<Zone.Water> area = new Area<>(Set.of(river, river2), List.of(), 0);
        assertEquals(3, Area.riverSystemFishCount(area));
        // IMPORTANT! should not be 6!
    }

    @Test
    public void areaConnectTo() {
        Zone.Lake lake = new Zone.Lake(0, 3, null);
        Zone.River river = new Zone.River(1, 2, lake);
        Zone.River river2 = new Zone.River(2, 1, lake);
        Area<Zone.Water> area = new Area<>(Set.of(river, river2), List.of(), 0);
        Zone.Lake lake2 = new Zone.Lake(3, 3, null);
        Area<Zone.Water> area2 = new Area<>(Set.of(lake2), List.of(), 0);
        assertThrows(IllegalArgumentException.class, () -> area.connectTo(area2)); // no open connections

        Zone.River river3 = new Zone.River(4, 2, lake2);
        Area<Zone.Water> area3 = new Area<>(Set.of(river3), List.of(), 1);
        Area<Zone.Water> area4 = new Area<>(Set.of(lake2), List.of(), 2);
        Area<Zone.Water> area5 = area3.connectTo(area4);
        assertEquals(Set.of(river3, lake2), area5.zones());
        assertEquals(1, area5.openConnections());

        Area<Zone.Water> area6 = new Area<>(Set.of(river3), List.of(PlayerColor.BLUE), 2);
        Area<Zone.Water> area7 = area6.connectTo(area6);
        assertEquals(List.of(PlayerColor.BLUE), area7.occupants());
        assertEquals(0, area7.openConnections());
    }

    @Test
    public void areaSpecialPower() {
        Zone.Meadow meadow = new Zone.Meadow(0, List.of(), Zone.Meadow.SpecialPower.HUNTING_TRAP);
        Area<Zone.Meadow> area = new Area<>(Set.of(meadow), List.of(), 0);
        assertEquals(meadow, area.zoneWithSpecialPower(Zone.Meadow.SpecialPower.HUNTING_TRAP));

        Zone.Lake lake = new Zone.Lake(1, 3, null);
        Area<Zone.Water> area2 = new Area<>(Set.of(lake), List.of(), 0);
        assertNull(area2.zoneWithSpecialPower(Zone.Meadow.SpecialPower.HUNTING_TRAP));
    }

    @Test
    public void areaWithInitialOccupant() {
        Area<Zone.Forest> area = new Area<>(Set.of(), List.of(), 0);
        Area<Zone.Forest> area2 = area.withInitialOccupant(PlayerColor.BLUE);
        assertEquals(List.of(PlayerColor.BLUE), area2.occupants());
        assertThrows(IllegalArgumentException.class, () -> area2.withInitialOccupant(PlayerColor.BLUE));
    }

    @Test
    public void areaWithoutOccupant() {
        Area<Zone.Forest> area = new Area<>(Set.of(), List.of(PlayerColor.BLUE), 0);
        Area<Zone.Forest> area2 = area.withoutOccupant(PlayerColor.BLUE);
        assertEquals(List.of(), area2.occupants());
        assertThrows(IllegalArgumentException.class, () -> area2.withoutOccupant(PlayerColor.BLUE));
        Area<Zone.Forest> area3 = new Area<>(Set.of(), List.of(PlayerColor.BLUE, PlayerColor.BLUE), 0);
        Area<Zone.Forest> area4 = area3.withoutOccupant(PlayerColor.BLUE);
        assertEquals(List.of(PlayerColor.BLUE), area4.occupants());
    }

    @Test
    public void areaWithoutOccupants() {
        Area<Zone.Forest> area = new Area<>(Set.of(), List.of(PlayerColor.BLUE), 0);
        Area<Zone.Forest> area2 = area.withoutOccupants();
        assertEquals(List.of(), area2.occupants());
        assertEquals(area2, area2.withoutOccupants());
    }

    @Test
    public void areaTileIds() {
        Zone.Forest forest = new Zone.Forest(0, Zone.Forest.Kind.PLAIN);
        Area<Zone.Forest> area = new Area<>(Set.of(forest), List.of(), 0);
        assertEquals(Set.of(0), area.tileIds());
        Area<Zone.Forest> area2 = new Area<>(Set.of(), List.of(), 0);
        assertEquals(Set.of(), area2.tileIds());
    }
}
