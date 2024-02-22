package ch.epfl.chacun;

/**
 * Util class to check if arguments are correct.
 *
 * @author Valerio De Santis (373247)
 * @author Simon Lefort (371918)
 */
public final class Preconditions {
    private Preconditions() {}

    /**
     * Throws an IllegalArgumentException if the given boolean is false.
     * @param shouldBeTrue the boolean to check
     */
    public static void checkArgument (boolean shouldBeTrue) {
        if (!shouldBeTrue) throw new IllegalArgumentException();
    }
}

