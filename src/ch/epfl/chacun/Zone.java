package ch.epfl.chacun;

import java.time.ZoneId;

public interface Zone {




    static int tileId(int zoneId) {
        return zoneId / 10;
    }

    static int localId (int zoneId) {
        return zoneId % 10;
    }

    int id ();

    default int tileId() {
        return tileId(this.id());
    }

    default int localId () {
        return localId(this.id());
    }

    default SpecialPower specialPower () {
        return null;
    }

    enum SpecialPower {
        SHAMAN, LOGBOAT, HUNTING_TRAP, PIT_TRAP, WILD_FIRE, RAFT;
    }
}
