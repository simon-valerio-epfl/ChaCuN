package ch.epfl.chacun;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A text maker that provides French text for the game's messages
 *
 * @author Valerio De Santis (373247)
 * @author Simon Lefort (371918)
 */
public final class TextMakerFr implements TextMaker {

    /**
     * A map associating each animal kind to its French name,
     * ordered by the points they give, in descending order
     */
    private static final Map<Animal.Kind, String> animalFrenchNames = Map.of(
            Animal.Kind.MAMMOTH, "mammouth",
            Animal.Kind.AUROCHS, "auroch",
            Animal.Kind.DEER, "cerf",
            Animal.Kind.TIGER, "tigre"
    );

    /**
     * A map associating each player to their name
     */
    private final Map<PlayerColor, String> names;

    /**
     * Enum representing the different game items, with their French name
     *
     * @author Valerio De Santis (373247)
     * @author Simon Lefort (371918)
     */
    enum GameItem {
        MUSHROOM_GROUP("groupe"),
        FISH("poisson"),
        LAKE("lac"),
        TILE("tuile");

        /**
         * The French name of the game item
         */
        private final String frenchName;

        /**
         * Returns the French name of the current game item
         *
         * @return the French name of the game item
         */
        public String getFrenchName() {
            return frenchName;
        }

        /**
         * Creates a new game item with the given French name
         *
         * @param frenchName the French name of the game item
         */
        GameItem(String frenchName) {
            this.frenchName = frenchName;
        }
    }

    /**
     * Creates a new TextMakerFr with the given player names
     *
     * @param names the names of the players of this match
     */
    public TextMakerFr(Map<PlayerColor, String> names) {
        this.names = Map.copyOf(names);
    }

    /**
     * Creates a string representing the given game item in the given count,
     * according to the French language
     *
     * @param item  the game item, should have a French name whose plural is regular
     * @param count the count of the game item, if it indicates a plural quantity, the item name will be pluralized
     * @return a string representing the given game item in the given count
     */
    private String pluralizeGameItems(GameItem item, int count) {
        return STR."\{count} \{pluralize(item.getFrenchName(), count)}";
    }

    /**
     * Takes an ordered list, and returns a string representing the items in the list
     * in a human-readable way
     *
     * @param items an ordered list of items
     * @return a string representing the items in the list in a human-readable way
     */
    private String itemsToString(List<String> items) {
        Preconditions.checkArgument(!items.isEmpty());
        StringBuilder sb = new StringBuilder();
        int itemsSize = items.size();
        for (int i = 0; i < itemsSize; i++) {
            sb.append(items.get(i));

            boolean secondToLast = i == itemsSize - 2;
            boolean beforeSecondToLast = i < itemsSize - 2;

            if (beforeSecondToLast) sb.append(", ");
            else if (secondToLast) sb.append(" et ");
        }
        return sb.toString();
    }

    /**
     * Orders the given set of player colors and returns a string representing them, telling they earned (some points)
     * Example: Alice, Edgar et Bruno ont remporté (X points en tant qu'occupant-e-s majoritaires)
     *
     * @param scorers the set of player colors that scored
     */
    private String earnMessage(Set<PlayerColor> scorers) {
        Preconditions.checkArgument(!scorers.isEmpty());
        if (scorers.size() == 1)
            return STR."\{playerName(scorers.stream().findFirst().get())} a remporté";

        List<String> sortedPlayerNames = scorers.stream()
                .sorted()
                .map(this::playerName)
                .toList();
        //the players are now ordered
        String playersToString = itemsToString(sortedPlayerNames);
        return STR."\{playersToString} ont remporté";

    }

    /**
     * Returns a string representing the given set of player colors and the given points
     *
     * @param scorers the set of player colors that scored
     * @param points  the points they scored
     * @return a string representing the given set of player colors and the given points
     */
    private String earnMessagePoints(Set<PlayerColor> scorers, int points) {
        // 10 points
        // 1 point
        return STR."\{earnMessage(scorers)} \{points(points)}";
    }

    /**
     * Returns a string representing the given set of player colors and the given points,
     * telling they earned the points as majority occupants
     *
     * @param scorers the set of player colors that scored
     * @param points  the points they scored
     * @return a string representing the given set of player colors and the given points,
     * telling they earned the points as majority occupants
     */
    private String earnMessageMajorityOccupants(Set<PlayerColor> scorers, int points) {
        Preconditions.checkArgument(!scorers.isEmpty());
        Preconditions.checkArgument(points > 0);
        int scorersSize = scorers.size();
        return STR."\{
                earnMessagePoints(scorers, points)
                } en tant qu'occupant·e\{
                scorersSize > 1 ? "·s" : ""
                } \{
                pluralize("majoritaire", scorersSize)
                }";
    }

    /**
     * Pluralizes the given word if the count is greater than 1,
     * according to the French language. The word has to be a regular one in French
     *
     * @param word  it has to be a French regular word
     * @param count the count of the word
     * @return the pluralized word if the count is greater than 1, the word otherwise
     */
    private String pluralize(String word, int count) {
        return count > 1 ? STR."\{word}s" : word;
    }

    /**
     * Returns a string representing the given map of animals and their counts
     *
     * @param animals the map of animals to represent as a string
     * @return a string representing the given map of animals and their counts
     */
    private String animalsToString(Map<Animal.Kind, Integer> animals) {
        List<String> animalsList = animals.entrySet().stream()
                .filter(animalEntry -> animalEntry.getValue() > 0)
                // the animals are ordered by their points
                .sorted(Map.Entry.comparingByKey())
                .map(animalEntry -> {
                    int value = animalEntry.getValue();
                    return STR."\{value} \{pluralize(animalFrenchNames.get(animalEntry.getKey()), value)}";
                })
                .toList();
        return itemsToString(animalsList);
    }


    @Override
    public String playerName(PlayerColor playerColor) {
        Preconditions.checkArgument(names.containsKey(playerColor));
        return names.get(playerColor);
    }

    @Override
    public String points(int points) {
        return STR."\{points} \{pluralize("point", points)}";
    }

    @Override
    public String playerClosedForestWithMenhir(PlayerColor player) {
        return STR."\{playerName(player)} a fermé une forêt contenant un menhir et peut donc placer une tuile menhir.";
    }

    @Override
    public String playersScoredForest(Set<PlayerColor> scorers, int points, int mushroomGroupCount, int tileCount) {
        return STR."\{
                earnMessageMajorityOccupants(scorers, points)
                } d'une forêt composée de \{
                pluralizeGameItems(GameItem.TILE, tileCount)
                }\{
                mushroomGroupCount > 0
                        ? STR." et de \{pluralizeGameItems(GameItem.MUSHROOM_GROUP, mushroomGroupCount)} de champignons."
                        : "."
                }";
    }

    @Override
    public String playersScoredRiver(Set<PlayerColor> scorers, int points, int fishCount, int tileCount) {
        return STR."\{
                earnMessageMajorityOccupants(scorers, points)
                } d'une rivière composée de \{
                pluralizeGameItems(GameItem.TILE, tileCount)
                }\{
                fishCount > 0
                        ? STR." et contenant \{pluralizeGameItems(GameItem.FISH, fishCount)}."
                        : "."
                }";
    }

    @Override
    public String playerScoredHuntingTrap(PlayerColor scorer, int points, Map<Animal.Kind, Integer> animals) {
        return STR."\{
                earnMessagePoints(Set.of(scorer), points)
                } en plaçant la fosse à pieux dans un pré dans lequel elle est entourée de \{
                animalsToString(animals)
                }.";
    }

    @Override
    public String playerScoredLogboat(PlayerColor scorer, int points, int lakeCount) {
        return STR."\{
                earnMessagePoints(Set.of(scorer), points)
                } en plaçant la pirogue dans un réseau hydrographique contenant \{
                pluralizeGameItems(GameItem.LAKE, lakeCount)
                }.";
    }

    @Override
    public String playersScoredMeadow(Set<PlayerColor> scorers, int points, Map<Animal.Kind, Integer> animals) {
        return STR."\{earnMessageMajorityOccupants(scorers, points)} d'un pré contenant \{animalsToString(animals)}.";
    }

    @Override
    public String playersScoredRiverSystem(Set<PlayerColor> scorers, int points, int fishCount) {
        return STR."\{
                earnMessageMajorityOccupants(scorers, points)
                } d'un réseau hydrographique contenant \{
                pluralizeGameItems(GameItem.FISH, fishCount)
                }.";
    }

    @Override
    public String playersScoredPitTrap(Set<PlayerColor> scorers, int points, Map<Animal.Kind, Integer> animals) {
        return STR."\{
                earnMessageMajorityOccupants(scorers, points)
                } d'un pré contenant la grande fosse à pieux entourée de \{
                animalsToString(animals)
                }.";
    }

    @Override
    public String playersScoredRaft(Set<PlayerColor> scorers, int points, int lakeCount) {
        return STR."\{
                earnMessageMajorityOccupants(scorers, points)
                } d'un réseau hydrographique contenant le radeau et \{
                pluralizeGameItems(GameItem.LAKE, lakeCount)
                }.";
    }

    @Override
    public String playersWon(Set<PlayerColor> winners, int points) {
        return STR."\{earnMessage(winners)} la partie avec \{points(points)} !";
    }

    @Override
    public String clickToOccupy() {
        return "Cliquez sur le pion ou la hutte que vous désirez placer, ou ici pour ne pas en placer.";
    }

    @Override
    public String clickToUnoccupy() {
        return "Cliquez sur le pion que vous désirez reprendre, ou ici pour ne pas en reprendre.";
    }
}
