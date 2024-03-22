package ch.epfl.chacun;

import java.util.*;

/**
 * @author Valerio De Santis (373247)
 * @author Simon Lefort (371918)
 * represents the board of the game,
 * containing the tiles that have already been placed
 */
public final class Board {

    private final PlacedTile[] placedTiles;
    private final int[] orderedTileIndexes;
    private final ZonePartitions zonePartitions;
    private final Set<Animal> cancelledAnimals;
    // REACH represents the maximum distance from the origin
    public final static int REACH = 12;
    // size represents the size of the board's columns and lines:
    // there are REACH numbers at the left and REACH numbers at the right
    // of the origin, and the matrix is a square
    private final static int SIZE = REACH * 2 + 1;
    // the empty board contains no placed tiles, no ordered tile indexes,
    // no zone partitions and no cancelled animals
    public final static Board EMPTY = new Board(
            new PlacedTile[SIZE*SIZE],
            new int[0],
            ZonePartitions.EMPTY,
            Set.of()
    );
    /**
     * Creates a new board with the given placed tiles, ordered tile indexes,
     * zone partitions and cancelled animals.
     * Copies the set of cancelled animals to prevent external modification
      */

    private Board(PlacedTile[] placedTiles, int[] orderedTileIndexes, ZonePartitions zonePartitions, Set<Animal> cancelledAnimals) {
        this.placedTiles = placedTiles;
        this.orderedTileIndexes = orderedTileIndexes;
        this.zonePartitions = zonePartitions;
        this.cancelledAnimals = cancelledAnimals;
    }

    /**
     * Returns the index of the tile at the given position
     * @param pos the position of the tile
     * @return the index of the tile at the given position
     */
    private int getTileIndexFromPos(Pos pos) {
        // we get the index using the row-major index
        return (pos.y() + REACH) * SIZE + (pos.x() + REACH);
    }

    /**
     * Returns whether the given index is in the range of the board
     * @param idx the index to test
     * @return whether the given index is in the range of the board
     */
    private boolean isIndexInRange(int idx) {
        // asserts that the given index is
        // on the board
        return idx >= 0 && idx < SIZE*SIZE;
    }

    /**
     * Returns the tile placed at the given position,
     * null if there is no tile at the given position or
     * if the position is out of the board
     * @param pos the position of the tile to get
     * @return the tile placed at the given position,
     * null if there is no tile at the given position or if the position is out of the board
     */
    public PlacedTile tileAt(Pos pos) {
        int index = getTileIndexFromPos(pos);
        return isIndexInRange(index) ? placedTiles[index] : null;
    }

    /**
     * Returns the placed tile with the given id,
     * with a linear search over the tiles that
     * have already been placed
     * @param tileId the id of the tile to get
     * @return the placed tile with the given id
     */
    public PlacedTile tileWithId(int tileId) {
        for (int index: orderedTileIndexes) {
            PlacedTile tile = placedTiles[index];
            if (tile.id() == tileId) return tile;
        }
        throw new IllegalArgumentException();
    }

    /**
     * Returns the unmodifiable set of animals that have been cancelled
     * @return the unmodifiable set of animals that have been cancelled
     */
    public Set<Animal> cancelledAnimals() {
        return cancelledAnimals;
    }

    /**
     * Returns the set of all occupants on the board
     * @return the set of all occupants on the board
     */
    public Set<Occupant> occupants() {
        Set<Occupant> occupants = new HashSet<>();
        for (int index : orderedTileIndexes) {
            PlacedTile placedTile = placedTiles[index];
            if (placedTile.occupant() != null) {
                occupants.add(placedTile.occupant());
            }
        }
        return occupants;
    }
    /**
     * Returns the area of the forest containing the given forest zone
     * throws an exception if the zone is not in any area
     * @param forest the forest zone to get the area of
     * @return the area of the forest containing the given forest zone
     */
    public Area<Zone.Forest> forestArea(Zone.Forest forest) {
        return zonePartitions.forests().areaContaining(forest);
    }
    /**
     * Returns the area of the meadow containing the given meadow zone
     * throws an exception if the zone is not in any area
     * @param meadow the meadow zone to get the area of
     * @return the area of the meadow containing the given meadow zone
     */
    public Area<Zone.Meadow> meadowArea(Zone.Meadow meadow) {
        return zonePartitions.meadows().areaContaining(meadow);
    }

    /**
     * Returns the area of the river containing the given river zone,
     * throws an exception if the zone is not in any area
     * @param river the river zone to get the area of
     * @return the area of the river containing the given river zone
     */
    public Area<Zone.River> riverArea(Zone.River river) {
        return zonePartitions.rivers().areaContaining(river);
    }

    /**
     * Returns the area of the river system containing the given water zone,
     * throws an exception if the zone is not in any area
     * @param water the water zone to get the area of
     * @return the area of the river system containing the given water zone
     */
    public Area<Zone.Water> riverSystemArea(Zone.Water water) {
        return zonePartitions.riverSystems().areaContaining(water);
    }

    /**
     * Returns the set of meadow areas on the board
     * @return the set of meadow areas on the board
     */
    public Set<Area<Zone.Meadow>> meadowAreas() {
        return zonePartitions.meadows().areas();
    }
    /**
     * Returns the set of river system areas on the board
     * @return the set of river system areas on the board
     */
    public Set<Area<Zone.Water>> riverSystemAreas() {
        return zonePartitions.riverSystems().areas();
    }

    /**
     * Returns whether the given tile is adjacent to the tile at the given position
     * @param centerPos the position of the tile to test adjacency to
     * @param toTest the tile to test adjacency of
     * @return whether the given tile is adjacent to the tile at the given position
     */
    private boolean isTileAdjacentTo(Pos centerPos, PlacedTile toTest) {
        int w = Math.abs(centerPos.y() - toTest.pos().y());
        int h = Math.abs(centerPos.x() - toTest.pos().x());
        return w <= 1 && h <= 1;
    }

    /**
     * Returns the area of the meadow containing the given meadow zone
     * and the meadow zones surrounding the tile where the given zone
     * the returned area contains all the animals of the entire meadow,
     * and has 0 open connections
     * @param pos the position of the tile containing the meadow zone
     * @param meadowZone the meadow zone to get the surrounding area from
     * @return the area of the meadow containing the given meadow zone and the
     * surrounding meadow zones, with all the animals of the original meadow area
     * and 0 open connections
     */
    public Area<Zone.Meadow> adjacentMeadow(Pos pos, Zone.Meadow meadowZone) {
        Area<Zone.Meadow> area = meadowArea(meadowZone);
        Set<Zone.Meadow> adjacentZones = new HashSet<>();
        for (Zone.Meadow meadow: area.zones()) {
            PlacedTile tile = tileWithId(meadow.tileId());
            if (isTileAdjacentTo(pos, tile)) {
                adjacentZones.add(meadow);
            }
        }
        return new Area<>(adjacentZones, area.occupants(), 0);
    }

    /**
     * Returns the number of occupants of the given kind and the given player
     * present on the board
     * @param player the player to count the occupants of
     * @param occupantKind the kind of occupant to count
     * @return the number of occupants of the given kind and the given player present on the board
     */
    public int occupantCount(PlayerColor player, Occupant.Kind occupantKind) {
        int count = 0;
        for (int index: orderedTileIndexes) {
            PlacedTile placedTile = placedTiles[index];
            if (
                placedTile.occupant() != null
                && placedTile.occupant().kind() == occupantKind
                && placedTile.placer() == player
            ) {
                count++;
            }
        }
        return count;
    }

    /**
     * Returns the set of positions where the next tile may be placed
     * @return the set of positions where the next tile may be placed
     */
    public Set<Pos> insertionPositions() {
        Set<Pos> insertionPositions = new HashSet<>();
        // we loop over the tiles that we have already placed
        for (int idx: orderedTileIndexes) {
            Pos tilePos = placedTiles[idx].pos();
            // loop over N, E, S, W
            for (Direction direction: Direction.ALL) {
                // get the new position in the direction we want to test
                Pos neighbouringPosition = tilePos.neighbor(direction);
                if (isPosInBoard(neighbouringPosition) && (tileAt(neighbouringPosition)) == null) {
                    insertionPositions.add(neighbouringPosition);
                }
            }
        }
        return insertionPositions;
    }

    /**
     * Checks if the given position is in the board
     * @param pos the position to check
     * @return whether the given position is in the board
     */
    private boolean isPosInBoard(Pos pos) {
        return Math.abs(pos.x()) <= 12 && Math.abs(pos.y()) <= 12;
    }

    /**
     * Returns the last placed tile on the board, null if no tile has
     * been placed yet
     * @return the last placed tile on the board, null if no tile has been placed yet
     */
    public PlacedTile lastPlacedTile() {
        return !isEmpty()
            ? placedTiles[orderedTileIndexes[orderedTileIndexes.length - 1]]
            : null;
    }

    /**
     * Returns the set of all forest areas that have been closed
     * by the last placed tile, or an empty set if no tile has been placed yet
     * @return the set of all forest areas that have been closed by the last placed tile,
     * or an empty set if no tile has been placed yet
     */
    public Set<Area<Zone.Forest>> forestsClosedByLastTile() {
        if (lastPlacedTile() == null) return Set.of();
        Set<Area<Zone.Forest>> areas = new HashSet<>();
        for (Zone.Forest forestZone: lastPlacedTile().forestZones()) {
            Area<Zone.Forest> area = forestArea(forestZone);
            if (area.isClosed()) areas.add(area);
        }
        return areas;
    }

    /**
     * Returns the set of all river areas that have been closed
     * by the last placed tile, or an empty set if no tile has been placed yet
     * @return the set of all river areas that have been closed by the last placed tile,
     * or an empty set if no tile has been placed yet
     */
    public Set<Area<Zone.River>> riversClosedByLastTile() {
        if (lastPlacedTile() == null) return Set.of();
        Set<Area<Zone.River>> areas = new HashSet<>();
        for (Zone.River riverZone: lastPlacedTile().riverZones()) {
            // this is correct because there is always a river before each lake
            // closing a rivers area, the lake merely being an internal zone
            Area<Zone.River> area = riverArea(riverZone);
            if (area.isClosed()) areas.add(area);
        }
        return areas;
    }

    /**
     * Returns whether the given placed tile can be put on the board at its position
     * @param tile the tile to test
     * @return whether the given placed tile can be put on the board at its position
     */
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

    /**
     * Returns whether the given tile can be placed on the board at any position
     * @param tile the tile to test
     * @return whether the given tile can be placed on the board at any position
     */
    public boolean couldPlaceTile(Tile tile) {
        for (Pos pos : insertionPositions()) {
            for (Rotation rotation : Rotation.ALL) {
                PlacedTile placedTile = new PlacedTile(tile, null, rotation, pos);
                if (canAddTile(placedTile)) return true;
            }
        }
        return false;
    }

    /**
     * Checks if the board has at least one tile, to prevent
     * unwanted operations on an empty board
     * @return whether the board has at least one tile
     */
    private boolean isEmpty(){
        return orderedTileIndexes.length == 0;
    }

    /**
     * Returns a new board with the given tile placed on it
     * @param tile the tile to place
     * @return a new board with the given tile placed on it
     */
    public Board withNewTile(PlacedTile tile){
        Preconditions.checkArgument(isEmpty() || canAddTile(tile));
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
            // if the position is out of range, the neighbouring tile is null
            if (neighbouringTile != null) {
                TileSide sideOfNeighbour = neighbouringTile.side(direction.opposite());
                TileSide sideOfTile = tile.side(direction);
                zonePartitionsBuilder.connectSides(sideOfNeighbour, sideOfTile);
            }
        }

        return new Board(newPlacedTiles, newOrderedTileIndexes, zonePartitionsBuilder.build(), cancelledAnimals);
    }

    /**
     * Returns a new board with the given occupant placed
     * @param occupant the occupant to place
     * @return a new board with the given occupant placed
     */
    public Board withOccupant(Occupant occupant) {
        int zoneId = occupant.zoneId();
        int tileId = Zone.tileId(zoneId);

        PlacedTile tile = tileWithId(tileId);
        // throws an IllegalArgumentException if the tile is already occupied
        PlacedTile occupiedTile = tile.withOccupant(occupant);
        PlacedTile[] newPlacedTiles = placedTiles.clone();
        newPlacedTiles[getTileIndexFromPos(tile.pos())] = occupiedTile;

        Zone zone = tile.zoneWithId(zoneId);
        ZonePartitions.Builder zonePartitionsBuilder = new ZonePartitions.Builder(zonePartitions);
        // is the occupant we're adding to the zonePartitions an initial occupant?
        // if we had already connected the tile, this would erase previous occupants
        zonePartitionsBuilder.addInitialOccupant(tile.placer(), occupant.kind(), zone);

        return new Board(newPlacedTiles, orderedTileIndexes, zonePartitionsBuilder.build(), cancelledAnimals);
    }

    /**
     * Returns a new board with the given occupant removed
     * @param occupant the occupant to remove
     * @return a new board with the given occupant removed
     */
    public Board withoutOccupant(Occupant occupant) {
        int zoneId = occupant.zoneId();
        int tileId = Zone.tileId(zoneId);
        // see https://edstem.org/eu/courses/1101/discussion/95048?answer=178339
        PlacedTile tile = tileWithId(tileId);
        // throws an IllegalArgumentException if the tile is already occupied
        PlacedTile clearedTile = tile.withNoOccupant();
        PlacedTile[] newPlacedTiles = placedTiles.clone();
        newPlacedTiles[getTileIndexFromPos(tile.pos())] = clearedTile;

        Zone zone = tile.zoneWithId(zoneId);
        ZonePartitions.Builder zonePartitionsBuilder = new ZonePartitions.Builder(zonePartitions);
        zonePartitionsBuilder.removePawn(tile.placer(), zone);

        return new Board(newPlacedTiles, orderedTileIndexes, zonePartitionsBuilder.build(), cancelledAnimals);
    }

    /**
     * Returns a new board, identical to the former one,
     * but with the given forests and rivers cleared of gatherers and fishers
     * @param forests the forests to clear of gatherers
     * @param rivers the rivers to clear of fishers
     * @return a new board with the given forests and rivers cleared of gatherers and fishers
     */
    public Board withoutGatherersOrFishersIn(Set<Area<Zone.Forest>> forests, Set<Area<Zone.River>> rivers) {
        ZonePartitions.Builder zonePartitionsBuilder = new ZonePartitions.Builder(zonePartitions);
        PlacedTile[] newPlacedTiles = placedTiles.clone();

        for (int index: orderedTileIndexes) {
            PlacedTile placedTile = placedTiles[index];
            if (placedTile.occupant() == null || placedTile.occupant().kind() != Occupant.Kind.PAWN) continue;
            Occupant occupant = placedTile.occupant();
            Zone occupiedZone = placedTile.zoneWithId(occupant.zoneId());
            if (occupiedZone instanceof Zone.Forest forest) {
                if (forests.contains(forestArea(forest))) {
                    newPlacedTiles[index] = placedTile.withNoOccupant();
                }
            } else if (occupiedZone instanceof Zone.River river) {
                if (rivers.contains(riverArea(river))) {
                    newPlacedTiles[index] = placedTile.withNoOccupant();
                }
            }
        }

        for (Area<Zone.River> river: rivers) zonePartitionsBuilder.clearFishers(river);
        for (Area<Zone.Forest> forest: forests) zonePartitionsBuilder.clearGatherers(forest);

        return new Board(newPlacedTiles, orderedTileIndexes, zonePartitionsBuilder.build(), cancelledAnimals);
    }

    /**
     * Returns a new board, identical to the former one,
     * but with the given animals cancelled
     * @param newlyCancelledAnimals the animals to cancel
     * @return a new board with the given animals cancelled
     */
    public Board withMoreCancelledAnimals(Set<Animal> newlyCancelledAnimals) {
        Set<Animal> newCancelledAnimals = new HashSet<>(cancelledAnimals);
        newCancelledAnimals.addAll(newlyCancelledAnimals);
        return new Board(placedTiles, orderedTileIndexes, zonePartitions, Collections.unmodifiableSet(newCancelledAnimals));
    }

    /**
     * Returns whether some other object is equal to this,
     * returning false if the object isn't of the right type
     * @param that the object to compare to
     * @return whether some other object is equal to this board, false if the object isn't of the right type
     */
    @Override
    public boolean equals(Object that) {
        if (that == null || that.getClass() != getClass()) {
            // we check that the other object is of the right type
            // before casting it to a board

            // getClass is slightly better than instanceof
            // it guarantees that if in the future this class won't be final anymore
            // the method call on a subclass will return false
            return false;
        } else {
            Board thatBoard = (Board) that;
            return Arrays.equals(thatBoard.orderedTileIndexes, orderedTileIndexes)
                    && Arrays.equals(thatBoard.placedTiles, placedTiles)
                    && cancelledAnimals.equals(thatBoard.cancelledAnimals)
                    && zonePartitions.equals(thatBoard.zonePartitions);
        }
    }

    /**
     * Returns the hash code of the board
     * @return the hash code of the board
     */
    @Override
    public int hashCode() {
        int placedTilesHash = Arrays.hashCode(placedTiles);
        int orderedTileIndexesHash = Arrays.hashCode(orderedTileIndexes);
        return Objects.hash(placedTilesHash, orderedTileIndexesHash, zonePartitions, cancelledAnimals);
    }
}
