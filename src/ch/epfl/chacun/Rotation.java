package ch.epfl.chacun;

import java.util.List;

/**
 * Represents the rotation of a tile.
 *
 * @author Valerio De Santis (373247)
 * @author Simon Lefort (371918)
 */
public enum Rotation {
    NONE,
    RIGHT,
    HALF_TURN,
    LEFT;

    public static final List<Rotation> ALL = List.of(Rotation.values());
    public static final int COUNT = ALL.size();

    /**
     * Returns the number of degrees of the rotation.
     * @return the number of degrees of the rotation
     */
    public int degreesCW(){
        return this.ordinal()*90;
    }

    /**
     * Returns the rotation obtained by adding the given rotation to this one.
     * @param that the rotation to add
     * @return the rotation obtained by adding the given rotation to this one
     */
    public Rotation add(Rotation that) {
        return ALL.get((this.ordinal() + that.ordinal()) % COUNT);
    }

    /**
     * Returns the rotation obtained by negating this one.
     * @return the rotation obtained by negating this one
     */
    public Rotation negated(){
        //The mod 4 operation is used to ensure that this.ordinal being 0
        //works as expected
        return ALL.get((COUNT - this.ordinal()) % 4);
    }

    /**
     * Returns the number of quarter turns of the rotation.
     * @return the number of quarter turns of the rotation
     */
    public int quarterTurnsCW(){
        return this.ordinal();
    }

}
