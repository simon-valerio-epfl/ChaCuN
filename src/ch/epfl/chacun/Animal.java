package ch.epfl.chacun;

/**
 * Represents an animal in the game
 * @param id non-negative, the id of the animal
 * @param kind non-null, the kind of animal
 *
 * @author Valerio De Santis (373247)
 * @author Simon Lefort (371918)
 */
public record Animal (int id, Kind kind) {

    // todo maybe valerio can find a better name
    private static final int COUNT_PER_TILE = 10;

    /**
     * @return the id of the tile where the animal is
     */
    public int tileId () {
        return Zone.tileId(id / COUNT_PER_TILE);
    }

    /**
     * Represents the different kinds of animal
     *
     * @author Valerio De Santis (373247)
     * @author Simon Lefort (371918)
     */
    public enum Kind {
        MAMMOTH, AUROCHS, DEER, TIGER
    }
}