package ch.epfl.chacun;


public record Animal (int id, Kind kind) {

    public int tileId () {
        // todo: will be done when zone is created
        /*zoneId=id/10;
        tileId = Zone.tileId(zoneId);*/
        return Zone.tileId(id / 10);
    }

    public enum Kind {
        MAMMOTH, AUROCHS, DEER, TIGER;
    }
}
