package ch.epfl.chacun;

public final class Base32 {
    /**
     * This is a utility class. It is not instantiable
     */
    private Base32() {}

    private final static String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private final static int MASK_5_BITS = 0b11111;
    private final static int BASE_32_BIT_SIZE = 5;

    public static boolean isValid(String encoded) {
        return encoded.chars().allMatch(c -> ALPHABET.indexOf(c) != -1);
    }

    public static String encodeBits5(int toEncode) {
        return String.valueOf(ALPHABET.charAt(toEncode & MASK_5_BITS));
    }

    public static String encodeBits10(int toEncode) {
        return encodeBits5(toEncode >> BASE_32_BIT_SIZE) + encodeBits5(toEncode);
    }

    public static int decode(String encoded) {
        Preconditions.checkArgument(isValid(encoded));
        int decoded = 0;
        for (int i = 0; i < encoded.length(); i++) {
            decoded = decoded << BASE_32_BIT_SIZE | ALPHABET.indexOf(encoded.charAt(i));
        }
        return decoded;
    }

}
