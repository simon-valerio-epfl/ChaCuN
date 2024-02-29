package ch.epfl.chacun;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record ZonePartition<Z extends Zone>(Set<Area<Z>> areas) {

    public ZonePartition {
        areas = Set.copyOf(areas);
    }

    public ZonePartition () {
        this(Set.of());
    }

    public Area<Z> areaContaining(Z zone) {
        for (Area<Z> area: areas) {
            if (area.zones().contains(zone)) {
                return area;
            }
        }
        throw new IllegalArgumentException();
    }

    public final class Builder<Z extends Zone> {

        private Set<Area<Z>> areas;

        public Builder(ZonePartition<Z> partition) {
            areas = partition.areas;
        }

        void addSingleton (Z zone, int openConnections) {
            areas.add(new Area<>(Set.of(zone), List.of(), openConnections));
        }

        void addInitialOccupant (Z zone, PlayerColor color) {
            boolean areaFound = false;
            for (Area<Z> area: areas) {
                if (area.zones().contains(zone)) {
                    if (!area.occupants().isEmpty()) {
                        throw new IllegalArgumentException();
                    }
                    // todo find a better solution?
                    List<PlayerColor> occupants = new ArrayList<>(area.occupants());
                    occupants.add(color);
                    Area<Z> newArea = new Area<>(area.zones(), occupants, area.openConnections());
                    areas.add(newArea);
                    areas.remove(area);
                    areaFound = true;
                }
            }
            if (!areaFound) throw new IllegalArgumentException();
        }

        public void removeOccupant(Z zone, PlayerColor color) {
            boolean areaFound = false;
            for (Area<Z> area: areas) {
                if (area.zones().contains(zone)) {
                    if (!area.occupants().contains(color)) {
                        throw new IllegalArgumentException();
                    }
                    List<PlayerColor> occupants = new ArrayList<>(area.occupants());
                    occupants.remove(color);
                    Area<Z> newArea = new Area<>(area.zones(), occupants, area.openConnections());
                    areas.add(newArea);
                    areas.remove(area);
                    areaFound = true;
                }
            }
            if (!areaFound) throw new IllegalArgumentException();
        }

        public void removeAllOccupantsOf(Area<Z> area) {
            boolean areaIsFound = areas.remove(area);
            if (!areaIsFound) throw new IllegalArgumentException();
            areas.add(new Area<>(area.zones(), List.of(), area.openConnections()));
        }

        public void union(Z zone1, Z zone2) {
            for (Area<Z> area1: areas) {
                if (area1.zones().contains(zone1)) {
                    for (Area<Z> area2: areas) {
                        if (area2.zones().contains(zone2)) {
                            Area<Z> newBiggerArea = area1.connectTo(area2);
                            areas.remove(area1);
                            areas.remove(area2);
                            areas.add(newBiggerArea);
                        }
                    }
                }
            }
        }

        public ZonePartition<Z> build() {
            return new ZonePartition<>(areas);
        }

    }


}
