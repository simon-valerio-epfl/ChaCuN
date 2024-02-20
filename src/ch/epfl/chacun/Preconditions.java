package ch.epfl.chacun;

/**
 * Util class to check if arguments are correct.
 *
 * Valerio De Santis (373247)
 * Simon Lefort (371918)
 */
public final class Preconditions {
    private Preconditions() {}

    public static void checkArgument (boolean shouldBeTrue) {
        if (!shouldBeTrue) throw new IllegalArgumentException();
    }
}

