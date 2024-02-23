package ch.epfl.chacun;

import java.util.Objects;
import java.util.HashSet;
import java.util.Set;

public record PlacedTile (Tile tile, PlayerColor placer, Rotation rotation, Pos pos, Occupant occupant) {

    public PlacedTile {
        Objects.requireNonNull(tile);
        Objects.requireNonNull(rotation);
        Objects.requireNonNull(pos);
    }
    public PlacedTile (Tile tile, PlayerColor placer, Rotation rotation, Pos pos) {
        this(tile, placer, rotation, pos, null);
    }

    public int id () {
        return tile.id();
    }

    public Tile.Kind kind() {
        return tile.kind();
    }

    public TileSide side(Direction direction) {
        return tile.sides().get(direction.rotated(rotation).ordinal());
    }

    public Zone zoneWithId (int id) {
        for (Zone zone: tile.zones()) {
            if (zone.id() == id) {
                return zone;
            }
        }
        throw new IllegalArgumentException();
    }

    public Zone specialPowerZone(){
        for (Zone zone: tile.zones()) {
            if (zone.specialPower() != null) {
                return zone;
            }
        }
        return null;
    }

    public Set<Zone.Forest> forestZones(){
        Set<Zone.Forest> forestZones = new HashSet<>();

        for (Zone zone: tile.zones()) {
            if (zone instanceof Zone.Forest forest) {
                forestZones.add(forest);
            }
        }
        return forestZones;
    }

    public Set<Zone.River> riverZones () {
        Set<Zone.River> riverZones = new HashSet<>();
        for (Zone zone: tile.zones()) {
            if (zone instanceof Zone.River river) {
                riverZones.add(river);
            }
        }
        return riverZones;
    }

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
