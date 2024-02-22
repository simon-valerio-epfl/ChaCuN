package ch.epfl.chacun;

import java.util.Objects;

public record Occupant(Kind kind, int zoneId) {

    public static final int PAWN_COUNT = 5;
    public static final int HUT_COUNT = 3;

    public Occupant {
        Objects.requireNonNull(kind);
        Preconditions.checkArgument(zoneId >= 0);
    }

    public static int occupantsCount (Kind kind) {
        return switch (kind) {
            case Kind.PAWN -> PAWN_COUNT;
            case Kind.HUT -> HUT_COUNT;
        };
    }


    public enum Kind {
        PAWN, HUT;
    }

}
