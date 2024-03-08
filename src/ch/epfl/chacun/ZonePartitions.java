package ch.epfl.chacun;

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

    public static class Builder {
        private final ZonePartition.Builder<Zone.Forest> forests;
        private final ZonePartition.Builder<Zone.Meadow> meadows;
        private final ZonePartition.Builder<Zone.River> rivers;
        private final ZonePartition.Builder<Zone.Water> riverSystems;

        public Builder (ZonePartitions initial) {
            forests = new ZonePartition.Builder<>(initial.forests);
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

        // todo: shouldn't this use potentialOccupants() ? will it be done before?
        public void addInitialOccupant(PlayerColor occupant, Occupant.Kind occupantKind, Zone occupiedZone) {
            switch (occupiedZone) {
                case Zone.Meadow meadow when occupantKind.equals(Occupant.Kind.PAWN) ->
                    meadows.addInitialOccupant(meadow, occupant);
                case Zone.Forest forest when occupantKind.equals(Occupant.Kind.PAWN) ->
                    forests.addInitialOccupant(forest, occupant);
                case Zone.River river when occupantKind.equals(Occupant.Kind.PAWN) ->
                    rivers.addInitialOccupant(river, occupant);
                /*
                teacher comment to understand:

                The type of occupant given lets you know which partition to modify,
                pawns can only occupy rivers, not river systems,
                and huts can only occupy river systems, not rivers.
                (They can be placed on rivers, but then occupy the river system containing the river.
                the hydrographic network containing the river, not the river itself).
                 */
                case Zone.Lake lake when occupantKind.equals(Occupant.Kind.HUT) ->
                    riverSystems.addInitialOccupant(lake, occupant);
                case Zone.River river when occupantKind.equals(Occupant.Kind.HUT) ->
                    riverSystems.addInitialOccupant(river, occupant);
                default -> throw new IllegalArgumentException();
            }
        }

        public void removePawn(PlayerColor occupant, Zone occupiedZone) {
            switch (occupiedZone) {
                case Zone.Meadow meadow -> meadows.removeOccupant(meadow, occupant);
                case Zone.Forest forest -> forests.removeOccupant(forest, occupant);
                case Zone.River river -> rivers.removeOccupant(river, occupant);
                default -> throw new IllegalArgumentException();
            }
        }

        public void clearGatherers(Area<Zone.Forest> forest) {
            forests.removeAllOccupantsOf(forest);
        }

        public void clearFishers(Area<Zone.River> river) {
            rivers.removeAllOccupantsOf(river);
        }

        public ZonePartitions build() {
            return new ZonePartitions(forests.build(), meadows.build(), rivers.build(), riverSystems.build());
        }

    }
}
