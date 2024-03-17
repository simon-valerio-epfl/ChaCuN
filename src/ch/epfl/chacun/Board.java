package ch.epfl.chacun;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author Valerio De Santis (373247)
 * @author Simon Lefort (371918)
 */
public final class Board {

    private final PlacedTile[] placedTiles;
    private final int[] orderedTileIndexes;
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

    public PlacedTile tileAt(Pos pos) {
        int idx = getTileIndexFromPos(pos);
        return isIndexInRange(idx) ? placedTiles[idx] : null;
    }

    public PlacedTile tileWithId(int tileId) {
        for (int idx : orderedTileIndexes) {
            PlacedTile tile = placedTiles[idx];
            if (tile.id() == tileId) return tile;
        }
        throw new IllegalArgumentException();
    }

    public Set<Animal> cancelledAnimals() {
        return cancelledAnimals;
    }

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

    public Area<Zone.Forest> forestArea(Zone.Forest forest) {
        return zonePartitions.forests().areaContaining(forest);
    }

    public Area<Zone.Meadow> meadowArea(Zone.Meadow meadow) {
        return zonePartitions.meadows().areaContaining(meadow);
    }

    public Area<Zone.River> riverArea(Zone.River river) {
        return zonePartitions.rivers().areaContaining(river);
    }

    public Area<Zone.Water> riverSystemArea(Zone.Water water) {
        return zonePartitions.riverSystems().areaContaining(water);
    }

    public Set<Area<Zone.Meadow>> meadowAreas() {
        return zonePartitions.meadows().areas();
    }

    public Set<Area<Zone.Water>> riverSystemAreas() {
        return zonePartitions.riverSystems().areas();
    }

    private boolean isTileAdjacentTo(Pos centerPos, PlacedTile toTest) {
        int w = Math.abs(centerPos.y() - toTest.pos().y());
        int h = Math.abs(centerPos.x() - toTest.pos().x());
        return w <= 1 && h <= 1;
    }

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

    public Set<Pos> insertionPositions() {
        Set<Pos> insertionPositions = new HashSet<>();
        // we loop over the tiles that we have already placed
        for (int idx: orderedTileIndexes) {
            Pos tilePos = placedTiles[idx].pos();
            // loop over N, E, S, W
            for (Direction direction: Direction.ALL) {
                // get the new position in the direction we want to test
                Pos neighbouringPosition = tilePos.neighbor(direction);
                if (isIndexInRange(getTileIndexFromPos(neighbouringPosition)) && tileAt(neighbouringPosition) == null) insertionPositions.add(neighbouringPosition);
            }
        }
        return insertionPositions;
    }

    public PlacedTile lastPlacedTile() {
        return hasAtLeastOneTile()
            ? placedTiles[orderedTileIndexes[orderedTileIndexes.length - 1]]
            : null;
    }

    public Set<Area<Zone.Forest>> forestsClosedByLastTile() {
        if (lastPlacedTile() == null) return Set.of();
        Set<Area<Zone.Forest>> areas = new HashSet<>();
        for (Zone.Forest forestZone: lastPlacedTile().forestZones()) {
            Area<Zone.Forest> area = forestArea(forestZone);
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
            Area<Zone.River> area = riverArea(riverZone);
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

    private boolean hasAtLeastOneTile(){
        return orderedTileIndexes.length > 0;
    }

    public Board withNewTile(PlacedTile tile){
        if (hasAtLeastOneTile() && !canAddTile(tile)) throw new IllegalArgumentException();
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

        return new Board(newPlacedTiles, orderedTileIndexes.clone(), zonePartitionsBuilder.build(), cancelledAnimals);
    }

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

    public Board withMoreCancelledAnimals(Set<Animal> newlyCancelledAnimals) {
        Set<Animal> newCancelledAnimals = new HashSet<>(cancelledAnimals);
        newCancelledAnimals.addAll(newlyCancelledAnimals);
        return new Board(placedTiles, orderedTileIndexes, zonePartitions, newCancelledAnimals);
    }

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
            // todo: really make sure to test that
            Board thatBoard = (Board) that;
            return Arrays.equals(thatBoard.orderedTileIndexes, orderedTileIndexes)
                    && Arrays.equals(thatBoard.placedTiles, placedTiles)
                    //todo: to test
                    && cancelledAnimals.equals(thatBoard.cancelledAnimals)
                    //todo: to test
                    && zonePartitions.equals(thatBoard.zonePartitions);
        }
    }

    @Override
    public int hashCode() {
        int placedTilesHash = Arrays.hashCode(placedTiles);
        int orderedTileIndexesHash = Arrays.hashCode(orderedTileIndexes);
        return Objects.hash(placedTilesHash, orderedTileIndexesHash, zonePartitions, cancelledAnimals);
    }
}
