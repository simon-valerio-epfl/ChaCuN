package ch.epfl.chacun;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a zone partition
 * @param areas the areas in the zone partition
 * @param <Z> the type of the areas
 */
public record ZonePartition<Z extends Zone>(Set<Area<Z>> areas) {

    /**
     * Creates a new zone partition and does a defensive copy
     * of the set of areas (to make it unmodifiable)
     * @param areas the areas in the zone partition
     */
    public ZonePartition {
        areas = Set.copyOf(areas);
    }

    /**
     * Creates a zone partition without any area
     */
    public ZonePartition () {
        this(Set.of());
    }

    private static <Z extends Zone> Area<Z> areaContaining(Z zone, Set<Area<Z>> areas) {
        for (Area<Z> area: areas) {
            if (area.zones().contains(zone)) {
                return area;
            }
        }
        throw new IllegalArgumentException();
    }

    /**
     * Returns the area that contains the specified zone
     * @param zone the zone we are looking for
     * @return the area that contains the specified zone
     */
    public Area<Z> areaContaining(Z zone) {
        return ZonePartition.areaContaining(zone, areas);
    }

    public static final class Builder<Z extends Zone> {

        private final Set<Area<Z>> areas = new HashSet<>();

        public Builder(ZonePartition<Z> partition) {
            // ZonePartition is immutable, so we do not need to
            // call Set.copyOf() here as the areas of partitions will never change
            // we don't use the areas = partition.areas() syntax because we
            // want 'areas' to refer to a modifiable object
            areas.addAll(partition.areas());
        }

        public void addSingleton (Z zone, int openConnections) {
            areas.add(new Area<>(Set.of(zone), List.of(), openConnections));
        }

        public void addInitialOccupant (Z zone, PlayerColor color) {
            Area<Z> area = ZonePartition.areaContaining(zone, areas);
            areas.add(area.withInitialOccupant(color));
            areas.remove(area);
        }

        public void removeOccupant(Z zone, PlayerColor color) {
            Area<Z> area = ZonePartition.areaContaining(zone, areas);
            areas.add(area.withoutOccupant(color));
            areas.remove(area);
        }

        public void removeAllOccupantsOf(Area<Z> area) {
            boolean areaIsFound = areas.remove(area);
            if (!areaIsFound) throw new IllegalArgumentException();
            areas.add(area.withoutOccupants());
        }

        public void union(Z zone1, Z zone2) {
            Area<Z> area1 = ZonePartition.areaContaining(zone1, areas);
            Area<Z> area2 = ZonePartition.areaContaining(zone2, areas);
            Area<Z> newBiggerArea = area1.connectTo(area2);
            areas.remove(area1);
            areas.remove(area2);
            areas.add(newBiggerArea);
        }

        public ZonePartition<Z> build() {
            return new ZonePartition<>(areas);
        }

    }


}
