package ch.epfl.chacun;

import java.util.*;
/**
 * Represents a tile in the game
 * @param id the id of the tile
 * @param kind the kind of the tile (start, normal, menhir)
 * @param n the north side of the tile
 * @param e the east side of the tile
 * @param s the south side of the tile
 * @param w the west side of the tile
 *
 * @author Valerio De Santis (373247)
 * @author Simon Lefort (371918)
 */
public record Tile(int id, Kind kind, TileSide n, TileSide e, TileSide s, TileSide w) {

    public List<TileSide> sides () {
        return List.of(n, e, s, w);
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
