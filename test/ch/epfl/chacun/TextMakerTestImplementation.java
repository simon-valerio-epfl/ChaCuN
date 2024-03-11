package ch.epfl.chacun;

import java.util.Map;
import java.util.Set;

public class TextMakerTestImplementation implements TextMaker {

    @Override
    public String playerName(PlayerColor playerColor) {
        return "Player " + playerColor.toString();
    }

    @Override
    public String points(int points) {
        return points + " points";
    }

    @Override
    public String playerClosedForestWithMenhir(PlayerColor player) {
        return playerName(player) + " has closed a forest with a menhir.";
    }

    @Override
    public String playersScoredForest(Set<PlayerColor> scorers, int points, int mushroomGroupCount, int tileCount) {
        return "Majority occupants " + scorers + " of a newly closed forest consisting of " + tileCount +
                " tiles and containing " + mushroomGroupCount + " mushroom groups have won " + points(points) + ".";
    }

    @Override
    public String playersScoredRiver(Set<PlayerColor> scorers, int points, int fishCount, int tileCount) {
        return "Majority occupants " + scorers + " of a newly closed river consisting of " + tileCount +
                " tiles and containing " + fishCount + " fish have won " + points(points) + ".";
    }

    @Override
    public String playerScoredHuntingTrap(PlayerColor scorer, int points, Map<Animal.Kind, Integer> animals) {
        return playerName(scorer) + " has placed a hunting trap in a meadow containing certain animals and won " + points(points) + ". Animals" + animals.keySet();
    }

    @Override
    public String playerScoredLogboat(PlayerColor scorer, int points, int lakeCount) {
        return playerName(scorer) + " has deployed a logboat in a water network comprising " + lakeCount + " lakes and won " + points(points) + ".";
    }

    @Override
    public String playersScoredMeadow(Set<PlayerColor> scorers, int points, Map<Animal.Kind, Integer> animals) {
        return "Majority occupants " + scorers + " of a meadow containing certain animals have won " + points(points) + ".";
    }

    @Override
    public String playersScoredRiverSystem(Set<PlayerColor> scorers, int points, int fishCount) {
        return "Majority occupants " + scorers + " of a water network containing " + fishCount + " fish have won " + points(points) + ".";
    }

    @Override
    public String playersScoredPitTrap(Set<PlayerColor> scorers, int points, Map<Animal.Kind, Integer> animals) {
        return "Majority occupants " + scorers + " of a meadow containing the great pit trap and certain animals on neighboring tiles have won " + points(points) + ".";
    }

    @Override
    public String playersScoredRaft(Set<PlayerColor> scorers, int points, int lakeCount) {
        return "Majority occupants " + scorers + " of a water network containing the raft across " + lakeCount + " lakes have won " + points(points) + ".";
    }

    @Override
    public String playersWon(Set<PlayerColor> winners, int points) {
        return "The winners are " + winners + " with " + points(points) + ".";
    }

    @Override
    public String clickToOccupy() {
        return "Please click on the occupant you wish to place, or on this message if you do not wish to place any occupant.";
    }

    @Override
    public String clickToUnoccupy() {
        return "Please click on the token you wish to take back, or on this message if you do not wish to take back any token.";
    }
}

