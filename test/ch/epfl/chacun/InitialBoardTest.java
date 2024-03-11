package ch.epfl.chacun;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

public class InitialBoardTest {

    @Test
    void testClassIsFinal() {
        assertTrue(Modifier.isFinal(Board.class.getModifiers()));
    }
}
