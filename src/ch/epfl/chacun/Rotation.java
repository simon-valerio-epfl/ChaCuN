package ch.epfl.chacun;

import java.util.List;

public enum Rotation {

    NONE,
    RIGHT,
    HALF_TURN,
    LEFT;

    public static final List<Rotation> ALL = List.of(Rotation.values());


    public int degreesCW(){
        return this.ordinal()*90;
    }
    public Rotation add(Rotation that){
        return ALL.get((this.ordinal() + that.ordinal()) % COUNT);
    }

    public static final int COUNT = ALL.size();


    //We use the mod 4 operation to ensure that this.ordinal being 0
    //works as expected
    public Rotation negated(){
        return ALL.get((COUNT - this.ordinal()) % 4);
    }

    public int quarterTurnsCW(){
        return this.ordinal();
    }

}
