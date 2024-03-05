package ch.epfl.chacun;

import java.util.*;
import java.util.stream.Collectors;

interface MeadowMessageFunction {
    String apply(Set<PlayerColor> scorers, Integer points, Map<Animal.Kind, Integer> animalPoints);
}

public record MessageBoard(TextMaker textMaker, List<Message> messages) {

    public MessageBoard {
        Objects.requireNonNull(textMaker); // todo: not specified, should we check for null?
        messages = List.copyOf(messages);
    }

    private Map<Animal.Kind, Integer> forMeadowAnimalPoints (Set<Animal> animals) {
        int mammothCount = (int) animals.stream().filter(a -> a.kind() == Animal.Kind.MAMMOTH).count();
        int aurochsCount = (int) animals.stream().filter(a -> a.kind() == Animal.Kind.AUROCHS).count();
        int deerCount = (int) animals.stream().filter(a -> a.kind() == Animal.Kind.DEER).count();
        return Map.of(
                Animal.Kind.MAMMOTH, mammothCount,
                Animal.Kind.AUROCHS, aurochsCount,
                Animal.Kind.DEER, deerCount
        );
    }
    private int forMeadowTotalPoints (Set<Animal> animals) {
        Map<Animal.Kind, Integer> points = forMeadowAnimalPoints(animals);
        return Points.forMeadow(points.get(Animal.Kind.MAMMOTH), points.get(Animal.Kind.AUROCHS), points.get(Animal.Kind.DEER));
    }

    private MessageBoard withScoredMeadowGeneric (
            Area<Zone.Meadow> meadow,
            Set<Animal> cancelledAnimals,
            Set<PlayerColor> scorers,
            MeadowMessageFunction getMessage
    ) {
        if (!meadow.isOccupied()) return this;
        List<Message> newMessages = new ArrayList<>(messages);
        Set<Animal> animals = Area.animals(meadow, cancelledAnimals);
        int points = forMeadowTotalPoints(animals);
        if (points == 0) return this;
        newMessages.add(
                new Message(
                        getMessage.apply(scorers, points, forMeadowAnimalPoints(animals)),
                        points,
                        scorers,
                        meadow.tileIds()
                )
        );
        return new MessageBoard(textMaker, newMessages);
    }

    public Map<PlayerColor, Integer> points() {
        // todo: make sure to test this
        // todo: might need improvements
        return messages.stream()
                .flatMap(m -> m.scorers().stream().map(s -> Map.entry(s, m.points())))
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                Integer::sum
                        )
                );
    }

    public MessageBoard withScoredForest(Area<Zone.Forest> forest) {
        if (!forest.isOccupied()) return this;
        List<Message> newMessages = new ArrayList<>(messages);
        int tileCount = forest.tileIds().size();
        int mushroomCount = Area.mushroomGroupCount(forest);
        int points = Points.forClosedForest(tileCount, mushroomCount);
        Set<PlayerColor> majorityOccupants = forest.majorityOccupants();
        newMessages.add(
                new Message(
                        textMaker.playersScoredForest(majorityOccupants, points, mushroomCount, tileCount),
                        points,
                        majorityOccupants,
                        forest.tileIds()
                )
        );
        return new MessageBoard(textMaker, newMessages);
    }

    public MessageBoard withClosedForestWithMenhir(PlayerColor player, Area<Zone.Forest> forest) {
        List<Message> newMessages = new ArrayList<>(messages);
        newMessages.add(
                new Message(
                        textMaker.playerClosedForestWithMenhir(player),
                        0,
                        Set.of(player),
                        forest.tileIds()
                )
        );
        return new MessageBoard(textMaker, newMessages);
    }

    public MessageBoard withScoredRiver(Area<Zone.River> river) {
        if (!river.isOccupied()) return this;
        List<Message> newMessages = new ArrayList<>(messages);
        int tileCount = river.tileIds().size();
        int fishCount = Area.riverFishCount(river);
        int points = Points.forClosedRiver(tileCount, fishCount);
        Set<PlayerColor> majorityOccupants = river.majorityOccupants();
        newMessages.add(
                new Message(
                        textMaker.playersScoredRiver(majorityOccupants, points, fishCount, tileCount),
                        points,
                        majorityOccupants,
                        river.tileIds()
                )
        );
        return new MessageBoard(textMaker, newMessages);
    }

    public MessageBoard withScoredHuntingTrap(PlayerColor scorer, Area<Zone.Meadow> adjacentMeadow) {
        // important to understand
        // adjacentMeadow is an area created specifically for the hunting trap
        // therefore it contains the right zones
        // the ones from the main meadow, and the ones from the 8 neighboring tiles
        return withScoredMeadowGeneric(
                adjacentMeadow,
                Set.of(),
                Set.of(scorer),
                (Set<PlayerColor> scorers, Integer points, Map<Animal.Kind, Integer> animalPoints) ->
                        textMaker.playerScoredHuntingTrap(scorer, points, animalPoints)
        );
    }

    public MessageBoard withScoredLogboat(PlayerColor scorer, Area<Zone.Water> riverSystem) {
        if (!riverSystem.isOccupied()) return this;
        List<Message> newMessages = new ArrayList<>(messages);
        int lakeCount = Area.lakeCount(riverSystem);
        int points = Points.forLogboat(lakeCount);
        newMessages.add(
                new Message(
                        textMaker.playerScoredLogboat(scorer, points, lakeCount),
                        points,
                        Set.of(scorer),
                        riverSystem.tileIds()
                )
        );
        return new MessageBoard(textMaker, newMessages);
    }

    public MessageBoard withScoredMeadow(Area<Zone.Meadow> meadow, Set<Animal> cancelledAnimals) {
        return withScoredMeadowGeneric(
                meadow,
                cancelledAnimals,
                meadow.majorityOccupants(),
                textMaker::playersScoredMeadow
        );
    }

    public MessageBoard withScoredRiverSystem(Area<Zone.Water> riverSystem) {
        if (!riverSystem.isOccupied()) return this;
        List<Message> newMessages = new ArrayList<>(messages);
        int fishCount = Area.riverSystemFishCount(riverSystem);
        int points = Points.forRiverSystem(fishCount);
        if (points == 0) return this;
        Set<PlayerColor> majorityOccupants = riverSystem.majorityOccupants();
        newMessages.add(
                new Message(
                        textMaker.playersScoredRiverSystem(majorityOccupants, points, fishCount),
                        points,
                        majorityOccupants,
                        riverSystem.tileIds()
                )
        );
        return new MessageBoard(textMaker, newMessages);
    }

    public MessageBoard withScoredPitTrap(Area<Zone.Meadow> adjacentMeadow, Set<Animal> cancelledAnimals) {
        return withScoredMeadowGeneric(
                adjacentMeadow,
                cancelledAnimals,
                adjacentMeadow.majorityOccupants(),
                textMaker::playersScoredPitTrap
        );
    }

    public MessageBoard withScoredRaft(Area<Zone.Water> riverSystem) {
        if (!riverSystem.isOccupied()) return this;
        List<Message> newMessages = new ArrayList<>(messages);
        int lakeCount = Area.lakeCount(riverSystem);
        int points = Points.forRaft(lakeCount);
        Set<PlayerColor> majorityOccupants = riverSystem.majorityOccupants();
        newMessages.add(
                new Message(
                        textMaker.playersScoredRaft(majorityOccupants, points, lakeCount),
                        points,
                        majorityOccupants,
                        riverSystem.tileIds()
                )
        );
        return new MessageBoard(textMaker, newMessages);
    }

    public MessageBoard withWinners(Set<PlayerColor> winners, int points) {
        List<Message> newMessages = new ArrayList<>(messages);
        newMessages.add(
                new Message(
                        textMaker.playersWon(winners, points),
                        points,
                        winners,
                        Set.of()
                )
        );
        return new MessageBoard(textMaker, newMessages);
    }

    public record Message(String text, int points, Set<PlayerColor> scorers, Set<Integer> tileIds) {

        public Message {
            Objects.requireNonNull(text);
            Preconditions.checkArgument(points >= 0);
            scorers = Set.copyOf(scorers);
            tileIds = Set.copyOf(tileIds);
        }

    }
}
