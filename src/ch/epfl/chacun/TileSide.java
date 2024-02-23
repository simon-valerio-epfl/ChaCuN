package ch.epfl.chacun;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * Represents the border of a tile in the game.
 */
public sealed interface TileSide {

    /**
     * Gets the zones of the tile side
     * @return the zones of the tile side
     */
    List<Zone> zones();

    /**
     * Checks if this tile side has the same kind as another one
     * @param that the other tile side
     * @return true if the tile side is of the same kind as the other tile side
     */
    boolean isSameKindAs(TileSide that);


    /**
     * Represents a forest tile side
     * @param forest the forest zone
     */
    record Forest (Zone.Forest forest) implements TileSide {

        /**
         * Gets the forest zone of the tile side (there can only be one)
         * @return the zone of the tile side
         */
        @Override
        public List<Zone> zones() {
            return Collections.singletonList(forest);
        }

        @Override
        public boolean isSameKindAs(TileSide that) {
            return that instanceof Forest;
        }
    }

    /**
     * Represents a meadow tile side
     * @param meadow a meadow zone
     */
    record Meadow (Zone.Meadow meadow) implements TileSide {

        /**
         * Gets the meadow zone of the tile side (there can only be one)
         * @return the zone of the tile side
         */
        @Override
        public List<Zone> zones() {
            return Collections.singletonList(meadow);
        }

        @Override
        public boolean isSameKindAs(TileSide that) {
            return that instanceof Meadow;
        }
    }


    /**
     * Represents a river tile side
     * @param meadow1 the first meadow zone bordering the river (ClockWise order)
     * @param river the river zone
     * @param meadow2 the second meadow zone bordering the river (CW order)
     */
    record River (Zone.Meadow meadow1, Zone.River river, Zone.Meadow meadow2) implements TileSide {

        /**
         * Gets the zones of the tile side: the first meadow,
         * the river and the second meadow, in CW order
         * @return the zones of the tile side
         */
        @Override
        public List<Zone> zones() {
            return new ArrayList<>(Arrays.asList(meadow1, river, meadow2));
        }

        @Override
        public boolean isSameKindAs(TileSide that) {
            return that instanceof River;
        }
    }
}
