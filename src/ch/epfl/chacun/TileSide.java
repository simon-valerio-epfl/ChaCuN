package ch.epfl.chacun;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public sealed interface TileSide {

    List<Zone> zones();
    boolean isSameKindAs(TileSide that);

    record Forest (Zone.Forest forest) implements TileSide {
        @Override
        public List<Zone> zones() {
            return Collections.singletonList(forest);
        }

        @Override
        public boolean isSameKindAs(TileSide that) {
            return that instanceof Forest;
        }
    }

    record Meadow (Zone.Meadow meadow) implements TileSide {
        @Override
        public List<Zone> zones() {
            return Collections.singletonList(meadow);
        }

        @Override
        public boolean isSameKindAs(TileSide that) {
            return that instanceof Meadow;
        }
    }

    record River (Zone.Meadow meadow1, Zone.River river, Zone.Meadow meadow2) implements TileSide {
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
