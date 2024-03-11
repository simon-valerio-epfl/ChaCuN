package ch.epfl.chacun;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class InitialMessageBoardTest {

    @Test
    void testWithScoredForestAndPoints() {
        TextMaker textMaker = new TextMakerTestImplementation();
        MessageBoard messageBoard = new MessageBoard(textMaker, List.of());
        Zone.Forest forest1 = new Zone.Forest(120, Zone.Forest.Kind.WITH_MUSHROOMS);
        Zone.Forest forest2 = new Zone.Forest(200, Zone.Forest.Kind.PLAIN);

        PlayerColor valerio = PlayerColor.RED;
        PlayerColor simon = PlayerColor.BLUE;

        Area<Zone.Forest> area = new Area<>(Set.of(forest1, forest2), List.of(valerio, valerio, simon), 10);
        Area<Zone.Forest> area2 = new Area<>(Set.of(forest1, forest2), List.of(valerio, valerio, simon, simon), 10);

        MessageBoard newMessageBoard = messageBoard.withScoredForest(area);
        MessageBoard newMessageBoard2 = newMessageBoard.withScoredForest(area2);

        assertEquals(
                "Majority occupants [RED] of a newly closed forest consisting of 2 tiles and containing 1 mushroom groups have won 7 points.",
                newMessageBoard.messages().getFirst().text());

        assertEquals(Map.of(valerio, 14, simon, 7), newMessageBoard2.points());
    }

    @Test
    void testScoredForestReturnsZero() {
        TextMaker textMaker = new TextMakerTestImplementation();
        MessageBoard board = new MessageBoard(textMaker, List.of());
        Area<Zone.Forest> forestArea = new Area<>(Set.of(new Zone.Forest(120, Zone.Forest.Kind.PLAIN)), List.of(), 10);
        assertEquals(0, board.withClosedForestWithMenhir(PlayerColor.RED, forestArea).messages().getFirst().points());
    }

    @Test
    void testAnimalsIncludeTiger() {
        TextMaker textMaker = new TextMakerTestImplementation();
        MessageBoard messageBoard = new MessageBoard(textMaker, List.of());
        Area<Zone.Meadow> meadowArea = new Area<>(Set.of(new Zone.Meadow(0, List.of(new Animal(10, Animal.Kind.TIGER), new Animal(10, Animal.Kind.AUROCHS)), null)), List.of(), 10);
        assertTrue(messageBoard.withScoredHuntingTrap(PlayerColor.RED, meadowArea).messages().getFirst().text().contains("TIGER"));
    }

}