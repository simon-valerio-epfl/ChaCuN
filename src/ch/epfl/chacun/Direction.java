package ch.epfl.chacun;

import java.util.List;

public enum Direction {

    N,
    E,
    S,
    W;

    public static final List<Direction> ALL = List.of(Direction.values());
    public static final int COUNT = ALL.size();

    public final Direction rotated(Rotation rotation) {
        return ALL.get((this.ordinal() + rotation.quarterTurnsCW()) % COUNT);
    }

    public final Direction opposite() {
        return this.rotated(Rotation.HALF_TURN);
    }

}
