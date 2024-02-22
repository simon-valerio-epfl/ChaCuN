package ch.epfl.chacun;


/**
 * Represents an animal in the game
 * An animal is defined by its id and its kind
 * @param id
 * @param kind
 */
public record Animal (int id, Kind kind) {
    /**
     * @return the id of the tile where the animal is
     */
    public int tileId () {
        return Zone.tileId(id / 10);
    }

    /**
     * Represents the different kinds of animal
     */
    public enum Kind {
        MAMMOTH, AUROCHS, DEER, TIGER;
    }
}