package ch.epfl.chacun;

import java.util.List;
import java.util.Set;

public record ZonePartitions (
        // we don't copy the zone partitions we take as arguments
        // because they're already immutable
        ZonePartition<Zone.Forest> forest,
        ZonePartition<Zone.Meadow> meadows,
        ZonePartition<Zone.River> rivers,
        ZonePartition<Zone.Water> riverSystems
) {
    public final static ZonePartitions EMPTY = new ZonePartitions(
            new ZonePartition<>(),
            new ZonePartition<>(),
            new ZonePartition<>(),
            new ZonePartition<>()
    );

    public static class Builder {
        private final ZonePartition.Builder<Zone.Forest> forests;
        private final ZonePartition.Builder<Zone.Meadow> meadows;
        private final ZonePartition.Builder<Zone.River> rivers;
        private final ZonePartition.Builder<Zone.Water> riverSystems;

        public Builder (ZonePartitions initial) {
            forests = new ZonePartition.Builder<>(initial.forest);
            meadows = new ZonePartition.Builder<>(initial.meadows);
            rivers = new ZonePartition.Builder<>(initial.rivers);
            riverSystems = new ZonePartition.Builder<>(initial.riverSystems);
        }

        public void addTile(Tile tile) {

            // indexed by local zoneId
            int[] openConnectionsPerZone = new int[10];

            for (TileSide side: tile.sides()) {
                for (Zone zone: side.zones()) {
                    int localId = zone.localId();
                    openConnectionsPerZone[localId]++;

                    // for each river containing a lake
                    // consider that there is one open connection between the lake and the river
                    // (+1 for both)
                    if (zone instanceof Zone.River river) {
                        if (river.hasLake()) {
                            openConnectionsPerZone[river.lake().localId()]++;
                            openConnectionsPerZone[localId]++;
                        }
                    }
                }
            }

            /*
            for the starting tile, the partitions would be this way:
            meadows : {{560}[2], {562}[1]},
            forests : {{561}[2]},
            rivers : {{563}[1]}, (therefore we have to do -1 to open connection if a river has a lake,
                                      because this connection has been closed by the lake)
            river systems : {{563}[2], {568}[1]}
             */
            for (Zone zone: tile.zones()) {
                int localId = zone.localId();
                int openConnectionCount = openConnectionsPerZone[localId];
                switch (zone) {
                    case Zone.Forest forest -> forests.addSingleton(forest, openConnectionCount);
                    case Zone.Meadow meadow -> meadows.addSingleton(meadow, openConnectionCount);
                    case Zone.River river -> {
                        rivers.addSingleton(river, river.hasLake() ? openConnectionCount - 1 : openConnectionCount);
                        riverSystems.addSingleton(river, openConnectionCount);
                    }
                    case Zone.Lake lake -> riverSystems.addSingleton(lake, openConnectionCount);
                }
            }

            // connect each river to its lake
            // from this river systems partition (id: open connection count): {{563}[2], {568}[1]}
            // to this one: {{563,568}[1]}
            for (Zone zone: tile.zones()) {
                if (zone instanceof Zone.River river) {
                    if (river.hasLake()) {
                        riverSystems.union(river, river.lake());
                    }
                }
            }

        }
    }
}
