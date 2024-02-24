package ch.epfl.chacun;

import java.util.*;
/**
 * Represents a tile in the game
 * @param id the id of the tile
 */
public record Tile(int id, Kind kind, TileSide n, TileSide e, TileSide s, TileSide w) {

    public List<TileSide> sides () {
        return new ArrayList<>(Arrays.asList(n, e, s, w));
    }

    public Set<Zone> sideZones() {
        Set<Zone> sideZones = new HashSet<>();
        for (TileSide side: this.sides()) {
            sideZones.addAll(side.zones());
        }
        return sideZones;
    }

    public Set<Zone> zones() {
        Set<Zone> zones = new HashSet<>(sideZones());
        for (Zone sideZone : sideZones()) {
            if (sideZone instanceof Zone.River river) {
                if (river.hasLake()) zones.add(river.lake());
            }
        }
        return zones;
    }

    public enum Kind {
        START, NORMAL, MENHIR;
    }
}
