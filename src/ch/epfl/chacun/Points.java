package ch.epfl.chacun;

/**
 * Util class to calculate points.
 *
 * @author Valerio De Santis (373247)
 * @author Simon Lefort (371918)
 */
public final class Points {

    /**
     * Number of gained points per tile when a forest is closed.
     */
    public static final int CLOSED_FOREST_TILE_POINTS = 2;
    /**
     * Number of gained points per mushroom group when a forest is closed.
     */
    public static final int CLOSED_FOREST_MUSHROOM_POINTS = 3;
    /**
     * Number of gained points per tile when a river is closed.
     */
    public static final int CLOSED_RIVER_TILE_POINTS = 1;
    /**
     * Number of gained points per fish when a river is closed.
     */
    public static final int CLOSED_RIVER_FISH_POINTS = 1;
    /**
     * Number of gained points per mammoth.
     */
    public static final int MAMMOTH_POINTS = 3;
    /**
     * Number of gained points per aurochs.
     */
    public static final int AUROCHS_POINTS = 2;
    /**
     * Number of gained points per deer.
     */
    public static final int DEER_POINTS = 1;
    /**
     * Number of gained points per logboat.
     */
    public static final int LOGBOAT_POINTS = 2;
    /**
     * Number of gained points per raft.
     */
    public static final int RAFT_POINTS = 1;
    /**
     * Number of gained points per fisher hut.
     */
    public static final int FISHER_HUT_POINTS = 1;

    /**
     * Returns the number of points gained by a player when a forest is closed.
     * @param tileCount the number of tiles in the forest
     * @param mushroomGroupCount the number of mushroom groups in the forest
     * @return the number of points gained by a player when a forest is closed
     */
    public static int forClosedForest(int tileCount, int mushroomGroupCount){
        Preconditions.checkArgument(tileCount > 1);
        Preconditions.checkArgument(mushroomGroupCount >= 0);
        return tileCount*CLOSED_FOREST_TILE_POINTS + mushroomGroupCount*CLOSED_FOREST_MUSHROOM_POINTS;
    }

    /**
     * Returns the number of points gained by a player when a river is closed.
     * @param tileCount the number of tiles in the river
     * @param fishCount the number of fish in the river
     * @return the number of points gained by a player when a river is closed
     */
    public static int forClosedRiver(int tileCount, int fishCount){
        Preconditions.checkArgument(tileCount > 1);
        return tileCount*CLOSED_RIVER_TILE_POINTS + fishCount*CLOSED_RIVER_FISH_POINTS;
    }

    /**
     * Returns the number of points gained by a player per meadow
     * @param mammothCount the number of mammoths in the meadow
     * @param aurochsCount the number of aurochs in the meadow
     * @param deerCount the number of deers in the meadow
     * @return the number of points gained by a player per meadow
     */
    public static int forMeadow(int mammothCount, int aurochsCount, int deerCount){
        Preconditions.checkArgument(mammothCount >= 0);
        Preconditions.checkArgument(aurochsCount >= 0);
        Preconditions.checkArgument(deerCount >= 0);
        return mammothCount*MAMMOTH_POINTS + aurochsCount*AUROCHS_POINTS + deerCount*DEER_POINTS;
    }

    // TODO: check comments below??

    /**
     * Returns the number of points gained by a player per river system
     * @param fishCount the number of fish in the lake
     * @return the number of points gained by a player per river system
     */
    public static int forRiverSystem(int fishCount) {
        Preconditions.checkArgument(fishCount >= 0);
        return fishCount*FISHER_HUT_POINTS;
    }

    /**
     * Returns the number of points gained by a player per logboat
     * @param lakeCount the number of lakes
     * @return the number of points gained by a player per logboat
     */
    public static int forLogboat(int lakeCount) {
        Preconditions.checkArgument(lakeCount > 0);
        return lakeCount*LOGBOAT_POINTS;
    }

    /**
     * Returns the number of points gained by a player per raft
     * @param lakeCount the number of lakes
     * @return the number of points gained by a player per raft
     */
    public static int forRaft(int lakeCount) {
        Preconditions.checkArgument(lakeCount > 0);
        return lakeCount*RAFT_POINTS;
    }


}
