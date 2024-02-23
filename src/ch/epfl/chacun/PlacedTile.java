package ch.epfl.chacun;

import java.util.Objects;
import java.util.HashSet;
import java.util.Set;


/**
 * Represents a placed tile in the game.
 */
public record PlacedTile (Tile tile, PlayerColor placer, Rotation rotation, Pos pos, Occupant occupant) {

    /**
     * Constructor for PlacedTile
     * @param tile non null the tile to place
     * @param placer the player placing the tile
     * @param rotation non null the rotation of the tile when placed
     * @param pos non null the position of the tile when placed on the board
     * @param occupant an occupant on the tile, null if the player decided not to place any
     */
    public PlacedTile {
        Objects.requireNonNull(tile);
        Objects.requireNonNull(rotation);
        Objects.requireNonNull(pos);
    }

    /**
     * Constructor for PlacedTile, if the player decides not to place any occupant
     * it will be null by default
     * @param tile non null the tile to place
     * @param placer the player placing the tile
     * @param rotation non null the rotation of the tile when placed
     * @param pos non null the position of the tile when placed on the board
     */
    public PlacedTile (Tile tile, PlayerColor placer, Rotation rotation, Pos pos) {
        this(tile, placer, rotation, pos, null);
    }

    /**
     * Gets the id of the placed tile
     * @return the id of the placed tile
     */
    public int id () {
        return tile.id();
    }

    /**
     * Gets the kind of the placed tile
     * @return the kind of the placed tile
     */
    public Tile.Kind kind() {
        return tile.kind();
    }

    /**
     * Gets the side of the placed tile in the given cardinal direction,
     * knowing that a placed tile is rotated at its instantiation
     * @param direction the wanted direction
     * @return the side of the tile in the given direction
     */
    public TileSide side(Direction direction) {
        return tile.sides().get(direction.rotated(rotation.negated()).ordinal());
    }

    /**
     * Gets the zone of the placed tile with the given id
     * @param id the id of the zone to recuperate
     * @return the zone of the placed tile with the given id if present, throws an exception otherwise
     */
    public Zone zoneWithId (int id) {
        for (Zone zone: tile.zones()) {
            if (zone.id() == id) {
                return zone;
            }
        }
        throw new IllegalArgumentException();
    }

    /**
     * Gets the zone of the placed tile having a special power (there may be only one at most)
     * @return the zone of the placed tile having a special power if present, null otherwise
     */
    public Zone specialPowerZone(){
        for (Zone zone: tile.zones()) {
            if (zone.specialPower() != null) {
                return zone;
            }
        }
        return null;
    }

    /**
     * Gets the set of forest zones of the placed tile, returning an empty set if there are none
     * @return the set of forest zones of the placed tile (empty if there are none)
     */
    public Set<Zone.Forest> forestZones(){
        Set<Zone.Forest> forestZones = new HashSet<>();
        for (Zone zone: tile.zones()) {
            if (zone instanceof Zone.Forest forest) {
                forestZones.add(forest);
            }
        }
        return forestZones;
    }

    /**
     * Gets the set of river zones of the placed tile, returning an empty set if there are none
     * @return the set of river zones of the placed tile (empty if there are none)
     */
    public Set<Zone.River> riverZones () {
        Set<Zone.River> riverZones = new HashSet<>();
        for (Zone zone: tile.zones()) {
            if (zone instanceof Zone.River river) {
                riverZones.add(river);
            }
        }
        return riverZones;
    }
    /**
     * Gets the set of meadow zones of the placed tile, returning an empty set if there are none
     * @return the set of meadow zones of the placed tile (empty if there are none)
     */
    public Set<Zone.Meadow> meadowZones() {
        Set<Zone.Meadow> meadowZones = new HashSet<>();
        for (Zone zone: tile.zones()) {
            if (zone instanceof Zone.Meadow meadow) {
                meadowZones.add(meadow);
            }
        }
        return meadowZones;
    }

    public Set<Occupant> potentialOccupants() {
        Set<Occupant> potentialOccupants = new HashSet<>();
        if (placer == null) {
            return potentialOccupants;
        }
        for (Zone zone: this.tile.sideZones()) {
            Occupant potentialOccupant = new Occupant(Occupant.Kind.PAWN, zone.id());
            potentialOccupants.add(potentialOccupant);
            if (zone instanceof Zone.River river) {
                if (!river.hasLake()) {
                    Occupant potentialRiverOccupant = new Occupant(Occupant.Kind.HUT, zone.id());
                    potentialOccupants.add(potentialRiverOccupant);
                }
            }
        }
        for (Zone zone: this.tile.zones()) {
            if (zone instanceof Zone.Lake lake) {
                Occupant potentialLakeOccupant = new Occupant(Occupant.Kind.HUT, zone.id());
                potentialOccupants.add(potentialLakeOccupant);
            }
        }
         return potentialOccupants;
    }

    public PlacedTile withOccupant(Occupant occupant){
        if (this.occupant == null) {
            return new PlacedTile(tile, placer, rotation, pos, occupant);
        }
        throw new IllegalArgumentException();
    }

    public PlacedTile withNoOccupant () {
        return new PlacedTile(tile, placer, rotation, pos);
    }

    public int idOfZoneOccupiedBy (Occupant.Kind occupantKind) {
        if (occupant != null && occupantKind.equals(occupant.kind())) {
            return occupant.zoneId();
        }
        return -1;
    }
}
