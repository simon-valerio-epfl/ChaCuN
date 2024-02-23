package ch.epfl.chacun;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions.*;

class Tile {

    @Test
    public void testIsSameKind () {

        Zone.Forest forest = new Zone.Forest(98383893, Zone.Forest.Kind.WITH_MENHIR);
        TileSide tileSideN = new TileSide.Forest(forest);
        assertTrue(tileSideN.isSameKindAs(tileSideN));
    }

}
