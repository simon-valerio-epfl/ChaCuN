package ch.epfl.chacun;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InitialBoardTest {

    @Test
    void testClassIsFinal() {
        assertTrue(Modifier.isFinal(Board.class.getModifiers()));
    }

    @Test
    void testWithNewTile() {
        Zone.River river = new Zone.River(563, 0, null);
        Zone.Meadow meadow = new Zone.Meadow(560, List.of(), null);
        Zone.Meadow meadow1 = new Zone.Meadow(562, List.of(), null);

        TileSide.River riverSide = new TileSide.River(meadow1, river, meadow);
        TileSide.Meadow meadowSide = new TileSide.Meadow(meadow);
        TileSide.Meadow meadowSide1 = new TileSide.Meadow(meadow1);

        Tile tile = new Tile(56, Tile.Kind.NORMAL, riverSide, meadowSide, meadowSide1, meadowSide1);
        PlacedTile placedTile = new PlacedTile(tile, PlayerColor.RED, Rotation.NONE, new Pos(0, 0));

        Board board = Board.EMPTY;

        assertEquals(56, board.withNewTile(placedTile).tileAt(new Pos(0, 0)).id());
    }

}
