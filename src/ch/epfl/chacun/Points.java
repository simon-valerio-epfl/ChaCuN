package ch.epfl.chacun;

public final class Points {

    public static final int CLOSED_FOREST_TILE_POINTS = 2;
    public static final int CLOSED_FOREST_MUSHROOM_POINTS = 3;
    public static final int CLOSED_RIVER_TILE_POINTS = 1;
    public static final int CLOSED_RIVER_FISH_POINTS = 1;
    public static final int MAMMOTH_POINTS = 3;
    public static final int AUROCHS_POINTS = 2;
    public static final int DEER_POINTS = 1;
    public static final int LOGBOAT_POINTS = 2;
    public static final int RAFT_POINTS = 1;
    public static final int FISHER_HUT_POINTS = 1;

    public static int forClosedForest(int tileCount, int mushroomGroupCount){
        Preconditions.checkArgument(tileCount > 1);
        Preconditions.checkArgument(mushroomGroupCount >= 0);
        return tileCount*CLOSED_FOREST_TILE_POINTS + mushroomGroupCount*CLOSED_FOREST_MUSHROOM_POINTS;
    }
    public static int forClosedRiver(int tileCount, int fishCount){
        Preconditions.checkArgument(tileCount > 1);
        return tileCount*CLOSED_RIVER_TILE_POINTS + fishCount*CLOSED_RIVER_FISH_POINTS;
    }
    public static int forMeadow(int mammothCount, int aurochsCount, int deerCount){
        Preconditions.checkArgument(mammothCount >= 0);
        Preconditions.checkArgument(aurochsCount >= 0);
        Preconditions.checkArgument(deerCount >= 0);
        return mammothCount*MAMMOTH_POINTS + aurochsCount*AUROCHS_POINTS + deerCount*DEER_POINTS;
    }
    public static int forRiverSystem(int fishCount) {
        Preconditions.checkArgument(fishCount >= 0);
        return fishCount*FISHER_HUT_POINTS;
    }

    public static int forLogboat(int lakeCount) {
        Preconditions.checkArgument(lakeCount > 0);
        return lakeCount*LOGBOAT_POINTS;
    }
    public static int forRaft(int lakeCount) {
        Preconditions.checkArgument(lakeCount > 0);
        return lakeCount*RAFT_POINTS;
    }
}
