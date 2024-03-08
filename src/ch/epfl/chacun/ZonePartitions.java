package ch.epfl.chacun;

/**
 * Represents the partitions of the areas of the game
 * @param forests the partition of the forests
 * @param meadows the partition of the meadows
 * @param rivers the partition of the rivers
 * @param riverSystems the partition of the river systems
 *
 * @author Valerio De Santis (373247)
 * @author Simon Lefort (371918)
 */
public record ZonePartitions (
        // we don't copy the zone partitions we take as arguments
        // because they're already immutable
        ZonePartition<Zone.Forest> forests,
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

    /**
     * Represents a builder for ZonePartitions
     *
     * @author Valerio De Santis (373247)
     * @author Simon Lefort (371918)
     */
    public final static class Builder {
        private final ZonePartition.Builder<Zone.Forest> forests;
        private final ZonePartition.Builder<Zone.Meadow> meadows;
        private final ZonePartition.Builder<Zone.River> rivers;
        private final ZonePartition.Builder<Zone.Water> riverSystems;

        /**
         * Creates a new builder for ZonePartitions
         * @param initial the initial zone partitions
         */
        public Builder (ZonePartitions initial) {
            forests = new ZonePartition.Builder<>(initial.forests);
            meadows = new ZonePartition.Builder<>(initial.meadows);
            rivers = new ZonePartition.Builder<>(initial.rivers);
            riverSystems = new ZonePartition.Builder<>(initial.riverSystems);
        }

        /**
         * Adds a tile to the zone partitions
         * by creating new areas for each zone of the tile
         * @param tile the tile to add
         */
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

        /**
         * Connects two sides together, updating the partitions accordingly
         * @param s1 the first side
         * @param s2 the second side
         */
        public void connectSides(TileSide s1, TileSide s2) {
            switch(s1) {
                case TileSide.Meadow(Zone.Meadow m1)
                    when s2 instanceof TileSide.Meadow(Zone.Meadow m2) -> meadows.union(m1, m2);
                case TileSide.Forest(Zone.Forest f1)
                    when s2 instanceof TileSide.Forest(Zone.Forest f2) -> forests.union(f1, f2);
                case TileSide.River(Zone.Meadow m1, Zone.River r2, Zone.Meadow m2)
                    when s2 instanceof TileSide.River(Zone.Meadow m3, Zone.River r4, Zone.Meadow m4) -> {
                    rivers.union(r2, r4);
                    meadows.union(m1, m3);
                    meadows.union(m2, m4);
                }
                default -> throw new IllegalArgumentException();
            }
        }

        /**
         * Adds an initial occupant to the zone partitions
         * occupant given lets us know which partition to modify
         * huts can only occupy river systems, not rivers
         * they can be placed on rivers, but then occupy the river system containing the river
         * @param occupant the color of the initial occupant
         * @param occupantKind the kind of the initial occupant (HUT or PAWN)
         * @param occupiedZone the zone where the initial occupant is
         */
        public void addInitialOccupant(PlayerColor occupant, Occupant.Kind occupantKind, Zone occupiedZone) {
            switch (occupiedZone) {
                case Zone.Meadow meadow when occupantKind.equals(Occupant.Kind.PAWN) ->
                    meadows.addInitialOccupant(meadow, occupant);
                case Zone.Forest forest when occupantKind.equals(Occupant.Kind.PAWN) ->
                    forests.addInitialOccupant(forest, occupant);
                case Zone.River river when occupantKind.equals(Occupant.Kind.PAWN) ->
                    rivers.addInitialOccupant(river, occupant);
                // we regroup the cases lake and river under the water type
                case Zone.Water water when occupantKind.equals(Occupant.Kind.HUT) ->
                    riverSystems.addInitialOccupant(water, occupant);
                default -> throw new IllegalArgumentException();
            }
        }

        /**
         * removes a pawn of the given player color from the given zone
         * throws an exception if the zone is not of the right type, namely if it's a lake,
         * or if the given zone does not contain a pawn of the given player color
         * @param occupant the player color of the pawn to remove
         * @param occupiedZone the zone to remove the pawn from
         */
        public void removePawn(PlayerColor occupant, Zone occupiedZone) {
            switch (occupiedZone) {
                case Zone.Meadow meadow -> meadows.removeOccupant(meadow, occupant);
                case Zone.Forest forest -> forests.removeOccupant(forest, occupant);
                // we can safely remove an occupant from the 'rivers' zone partition,
                // the huts only being in the 'riverSystems' zone partition
                case Zone.River river -> rivers.removeOccupant(river, occupant);
                default -> throw new IllegalArgumentException();
            }
        }

        /**
         * removes all the gatherers from the given forest
         * @param forest the forest to remove the gatherers from
         */
        public void clearGatherers(Area<Zone.Forest> forest) {
            forests.removeAllOccupantsOf(forest);
        }

        /**
         * removes all the fishers from the given river
         * @param river the river to remove the fishers from
         */
        public void clearFishers(Area<Zone.River> river) {
            rivers.removeAllOccupantsOf(river);
        }

        /**
         * Builds the zone partitions, returning an immutable zonePartitions
         * @return the zone partitions
         */
        public ZonePartitions build() {
            return new ZonePartitions(forests.build(), meadows.build(), rivers.build(), riverSystems.build());
        }

    }
}
