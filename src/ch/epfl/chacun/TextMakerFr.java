package ch.epfl.chacun;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

final public class TextMakerFr implements TextMaker {

    private final Map<PlayerColor, String> names;

    public TextMakerFr(Map<PlayerColor, String> names) {
        this.names = names;
    }

    private void formatMiscElements(Map<String, Integer> elements) {
        // on veut trier la map (en fonction des key), et afficher
        // 1 element1, 1 element2, 3 élement3s, etc.
        return elements.keySet().stream().sorted()
            .map(el -> {
                int value = elements.get(el);
                return value + " " + pluralize(el, )
            })
    }

    /**
     * Alice, Edgar et Bruno ont remporté X points en tant qu'occupant-e-s majoritaires
     */
    private String earnMessage (Set<PlayerColor> scorers) {
        Preconditions.checkArgument(!scorers.isEmpty());
        // Alice|Edgar|Bruno
        List<String> sortedPlayerNames = scorers.stream().sorted().map(this::playerName).toList();
        // Alice, Edgar et Bruno ont
        // Alice et Bruno ont
        // Alice a
        String names = scorers.size() == 1 ? STR."\{sortedPlayerNames.getFirst()} a"
            : STR."\{
                sortedPlayerNames.stream().limit(sortedPlayerNames.size() - 1).collect(Collectors.joining(", "))
            } et \{
                // we know this will always be defined because players.size() > 1
                sortedPlayerNames.getLast()
            } ont";
        return STR."\{names} remporté ";
    }

    private String earnMessageMajorityOccupants (Set<PlayerColor> scorers, int points) {
        // 10 points
        // 1 point
        String pts = points + pluralize("point", points);
        return earnMessage(scorers) + pts + STR." en tant qu'\{pluralizeWithSuffix("occupant·e", scorers.size(), "·")} \{pluralize("majoritaire", scorers.size())}";
    }

    @Override
    public String playerName(PlayerColor playerColor) {
        return names.get(playerColor);
    }

    private String selectPluralForm (String singularWord, String pluralWord, int count) {
        return count == 1 ? singularWord : pluralWord;
    }

    private String pluralizeWithSuffix(String word, int count, String suffix) {
        return selectPluralForm(word, STR."\{word}\{suffix}s", count);
    }

    private String pluralize(String word, int count) {
        return pluralizeWithSuffix(word, count, "");
    }

    @Override
    public String points(int points) {
        return "";
    }

    @Override
    public String playerClosedForestWithMenhir(PlayerColor player) {
        return STR."\{playerName(player)} a fermé une forêt contenant un menhir et peut donc placer une tuile menhir. ";
    }

    @Override
    public String playersScoredForest(Set<PlayerColor> scorers, int points, int mushroomGroupCount, int tileCount) {
        return STR."\{earnMessageMajorityOccupants(scorers, points)} d'une forêt composée de \{tileCount} \{pluralize("tuile", tileCount)} \{
                mushroomGroupCount>0
                ? STR."et de \{mushroomGroupCount} \{pluralize("groupe", mushroomGroupCount)} de champignons."
                : "."}";
    }

    @Override
    public String playersScoredRiver(Set<PlayerColor> scorers, int points, int fishCount, int tileCount) {
        return "";
    }

    @Override
    public String playerScoredHuntingTrap(PlayerColor scorer, int points, Map<Animal.Kind, Integer> animals) {
        return "";
    }

    @Override
    public String playerScoredLogboat(PlayerColor scorer, int points, int lakeCount) {
        return "";
    }

    @Override
    public String playersScoredMeadow(Set<PlayerColor> scorers, int points, Map<Animal.Kind, Integer> animals) {
        return "";
    }

    @Override
    public String playersScoredRiverSystem(Set<PlayerColor> scorers, int points, int fishCount) {
        return "";
    }

    @Override
    public String playersScoredPitTrap(Set<PlayerColor> scorers, int points, Map<Animal.Kind, Integer> animals) {
        return STR."\{earnMessageMajorityOccupants(scorers, points)} d'un pré contenant la grande fosse à pieux entourés de "
    }

    @Override
    public String playersScoredRaft(Set<PlayerColor> scorers, int points, int lakeCount) {
        return STR."\{
                formatPlayerNames(scorers)
            } \{
                selectPluralForm("a", "ont", scorers.size())
            } remporté \{
                pluralize("point", points)
            } en tant qu'\{
                pluralizeWithSuffix("occupant·e", scorers.size(), "·")
            } \{pluralize("majoritaire", scorers.size())
                } d'un réseau hydrographique contenant le radeau et \{
                lakeCount
            } \{
                pluralize("lac", lakeCount)
            }.";
    }

    @Override
    public String playersWon(Set<PlayerColor> winners, int points) {
        String verb = winners.size() == 1 ? "a" : "ont";
        return STR."\{formatPlayerNames(winners)} \{verb} gagné avec \{points} points !";
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
