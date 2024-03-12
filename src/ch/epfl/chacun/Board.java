package ch.epfl.chacun;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Board {

    private final PlacedTile[] placedTiles;
    private final int[] orderedTileIndexes;
    //dear Valerio, maybe it's not final
    private final ZonePartitions zonePartitions;
    private final Set<Animal> cancelledAnimals;
    public final static int REACH = 12;
    // size represents the size of the board's columns and lines:
    // there are REACH numbers at the left and REACH numbers at the right
    // of the origin, and the matrix is a square
    private final static int SIZE = REACH * 2 + 1;
    public final static Board EMPTY = new Board(
            new PlacedTile[SIZE*SIZE],
            new int[0],
            ZonePartitions.EMPTY,
            Set.of()
    );

    private Board(PlacedTile[] placedTiles, int[] orderedTileIndexes, ZonePartitions zonePartitions, Set<Animal> cancelledAnimals) {
        this.placedTiles = placedTiles;
        this.orderedTileIndexes = orderedTileIndexes;
        this.zonePartitions = zonePartitions;
        this.cancelledAnimals = Collections.unmodifiableSet(cancelledAnimals);
    }

    private int getTileIndexFromPos(Pos pos) {
        // we get the index using the row-major index
        return (pos.y() + REACH) * SIZE + (pos.x() + REACH);
    }

    private boolean isIndexInRange(int idx) {
        // asserts that the given index is
        // on the board
        return idx >= 0 && idx < SIZE*SIZE;
    }

    PlacedTile tileAt(Pos pos) {
        int idx = getTileIndexFromPos(pos);
        // todo make sure it always return null if placedTiles[idx] is not defined
        // boolean tileAlreadyPosed = isIndexInRange(idx) &&
        //       Arrays.stream(orderedTileIndexes).anyMatch(integer -> integer==idx);
        return isIndexInRange(idx) ? placedTiles[idx] : null;
    }

    PlacedTile tileWithId(int tileId) {
        for (PlacedTile placedTile : placedTiles) {
            if (placedTile != null && placedTile.id() == tileId) {
                return placedTile;
            }
        }
        throw new IllegalArgumentException();
    }

    Set<Animal> cancelledAnimals() {
        return cancelledAnimals;
    }

    Set<Occupant> occupants() {
        Set<Occupant> occupants = new HashSet<>();
        for (PlacedTile placedTile : placedTiles) {
            if (placedTile != null && placedTile.occupant() != null) {
                occupants.add(placedTile.occupant());
            }
        }
        return occupants;
    }

    Area<Zone.Forest> forestArea(Zone.Forest forest) {
        return zonePartitions.forests().areaContaining(forest);
    }

    Area<Zone.Meadow> meadowArea(Zone.Meadow meadow) {
        return zonePartitions.meadows().areaContaining(meadow);
    }

    Area<Zone.River> riverArea(Zone.River river) {
        return zonePartitions.rivers().areaContaining(river);
    }

    Area<Zone.Water> riverSystemArea(Zone.Lake lake) {
        return zonePartitions.riverSystems().areaContaining(lake);
    }

    Set<Area<Zone.Meadow>> meadowAreas() {
        return zonePartitions.meadows().areas();
    }

    Set<Area<Zone.Water>> riverSystemAreas() {
        return zonePartitions.riverSystems().areas();
    }

    Area<Zone.Meadow> adjacentMeadow(Pos pos, Zone.Meadow meadowZone) {
        Area<Zone.Meadow> area = zonePartitions.meadows().areaContaining(meadowZone);
        List<PlayerColor> occupants = area.occupants();

        // todo ask fabrice
        // todo
        List<Pos> adjacentPositions = List.of(
                pos.neighbor(Direction.N).neighbor(Direction.E),
                pos.neighbor(Direction.N).neighbor(Direction.W),
                pos.neighbor(Direction.S).neighbor(Direction.E),
                pos.neighbor(Direction.S).neighbor(Direction.W),
                pos.neighbor(Direction.N),
                pos.neighbor(Direction.S),
                pos.neighbor(Direction.E),
                pos.neighbor(Direction.W)
        );

        Set<Zone.Meadow> adjacentMeadowZones = adjacentPositions.stream()
                // we get the stream of adjacent tiles
                .map(this::tileAt)
                // we discard the null ones
                .filter(Objects::nonNull)
                // we get the meadow zones of the remaining tiles,
                // flattening them in a stream,
                .flatMap(placedTile -> placedTile.meadowZones().stream())
                // check that their areas are connected to
                // the one containing our zone
                .filter(zone -> area.zones().contains(zone))
                // and finally collect the stream into a set
                .collect(Collectors.toSet());

        return new Area<>(adjacentMeadowZones, occupants, 0);

    }

    int occupantCount(PlayerColor player, Occupant.Kind occupantKind) {
        return switch (occupantKind) {
            // if it's a pawn we do a stream containing the areas (so a stream containing sets)
            // of the zone partitions whose occupants are pawns
            case PAWN -> Stream.of(
                            zonePartitions.meadows().areas(),
                            zonePartitions.forests().areas(),
                            zonePartitions.rivers().areas()
                    )   // we convert the stream of sets into a unique stream
                    .flatMap(Set::stream)
                    // we count the occupants the given player has in each area,
                    // then sum them up, casting the result into an int,
                    // count returning a long
                    .mapToInt(area ->
                            (int) area.occupants()
                                    .stream()
                                    .filter(occupant -> occupant.equals(player))
                                    .count()
                    )
                    .sum();
            case HUT -> zonePartitions.riverSystems().areas().stream()
                    .mapToInt(area ->
                            (int) area.occupants()
                                    .stream()
                                    .filter(occupant -> occupant.equals(player))
                                    .count()
                    )
                    .sum();
        };
    }

    private boolean isPositionAvailableToPlaceATile(Pos pos) {
        int idx = getTileIndexFromPos(pos);
        return tileAt(pos) == null && isIndexInRange(idx);
    }

    public Set<Pos> insertionPositions() {
        Set<Pos> insertionPositions = new HashSet<>();
        // we loop over the tiles that we have already placed
        for (int idx: orderedTileIndexes) {
            PlacedTile tile = placedTiles[idx];
            // loop over N, E, S, W
            for (Direction direction: Direction.ALL) {
                // get the new position in the direction we want to test
                Pos neighbouringPosition = tile.pos().neighbor(direction);
                if (isPositionAvailableToPlaceATile(neighbouringPosition)) insertionPositions.add(neighbouringPosition);
            }
        }
        return insertionPositions;
    }

    public PlacedTile lastPlacedTile() {
        return !isEmpty()
                ? placedTiles[orderedTileIndexes[orderedTileIndexes.length - 1]]
                : null;
    }

    public Set<Area<Zone.Forest>> forestsClosedByLastTile() {
        if (lastPlacedTile() == null) return Set.of();
        Set<Area<Zone.Forest>> areas = new HashSet<>();
        for (Zone.Forest forestZone: lastPlacedTile().forestZones()) {
            Area<Zone.Forest> area = zonePartitions.forests().areaContaining(forestZone);
            if (area.isClosed()) areas.add(area);
        }
        return areas;
    }

    public Set<Area<Zone.River>> riversClosedByLastTile() {
        if (lastPlacedTile() == null) return Set.of();
        Set<Area<Zone.River>> areas = new HashSet<>();
        for (Zone.River riverZone: lastPlacedTile().riverZones()) {
            // this is correct because there is always a river before each lake
            // closing a rivers area, the lake merely being an internal zone
            Area<Zone.River> area = zonePartitions.rivers().areaContaining(riverZone);
            if (area.isClosed()) areas.add(area);
        }
        return areas;
    }

    public boolean canAddTile(PlacedTile tile) {
        if (!insertionPositions().contains(tile.pos())) return false;
        for (Direction direction: Direction.ALL) {
            Pos neighbouringPosition = tile.pos().neighbor(direction);
            PlacedTile neighbouringTile = tileAt(neighbouringPosition);
            if (neighbouringTile != null) {
                // we check that every border shared by the placing tile
                // with any already placed neighbouring tile
                // have corresponding kinds
                TileSide sideOfNeighbour = neighbouringTile.side(direction.opposite());
                TileSide sideOfTile = tile.side(direction);
                if (!sideOfTile.isSameKindAs(sideOfNeighbour)) return false;
            }
        }
        return true;
    }

    public boolean couldPlaceTile(Tile tile) {
        for (Pos pos : insertionPositions()) {
            for (Rotation rotation : Rotation.ALL) {
                PlacedTile placedTile = new PlacedTile(tile, null, rotation, pos);
                if (canAddTile(placedTile)) return true;
            }
        }
        return false;
    }
    // todo maybe change this
    private boolean isEmpty(){
        return orderedTileIndexes.length == 0;
    }

    public Board withNewTile(PlacedTile tile){
        if (!isEmpty() && !canAddTile(tile)) throw new IllegalArgumentException();
        int indexOfNewTile = getTileIndexFromPos(tile.pos());

        PlacedTile[] newPlacedTiles = placedTiles.clone();
        newPlacedTiles[indexOfNewTile] = tile;

        int[] newOrderedTileIndexes = Arrays.copyOf(orderedTileIndexes, orderedTileIndexes.length + 1);
        newOrderedTileIndexes[newOrderedTileIndexes.length - 1] = indexOfNewTile;

        ZonePartitions.Builder zonePartitionsBuilder = new ZonePartitions.Builder(zonePartitions);
        zonePartitionsBuilder.addTile(tile.tile());

        for (Direction direction: Direction.ALL) {
            Pos neighbouringPosition = tile.pos().neighbor(direction);
            PlacedTile neighbouringTile = tileAt(neighbouringPosition);
            if (neighbouringTile != null) {
                TileSide sideOfNeighbour = neighbouringTile.side(direction.opposite());
                TileSide sideOfTile = tile.side(direction);
                zonePartitionsBuilder.connectSides(sideOfNeighbour, sideOfTile);
            }
        }

        return new Board(newPlacedTiles, newOrderedTileIndexes, zonePartitionsBuilder.build(), cancelledAnimals);
    }

    public Board withOccupant(Occupant occupant) {
        occupant.zoneId();

    }

}
