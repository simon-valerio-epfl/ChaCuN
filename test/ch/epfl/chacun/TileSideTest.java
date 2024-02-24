package ch.epfl.chacun;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TileSideTest {

    @Test
    public void testIsSameKind () {

        Zone.Forest forest = new Zone.Forest(98383893, Zone.Forest.Kind.WITH_MENHIR);
        TileSide tileSideN = new TileSide.Forest(forest);
        assertTrue(tileSideN.isSameKindAs(tileSideN));
    }

}
