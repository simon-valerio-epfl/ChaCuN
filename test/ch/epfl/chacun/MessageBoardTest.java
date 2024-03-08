package ch.epfl.chacun;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MessageBoardTest {

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

}