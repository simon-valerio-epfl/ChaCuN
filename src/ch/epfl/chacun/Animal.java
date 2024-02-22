package ch.epfl.chacun;


public record Animal (int id, Kind kind) {

    public int tileId () {
        return Zone.tileId(id / 10);
    }

    public enum Kind {
        MAMMOTH, AUROCHS, DEER, TIGER;
    }
}

















