package ch.epfl.chacun;

import java.util.*;
import java.util.stream.Collectors;

public record MessageBoard(TextMaker textMaker, List<Message> messages) {

    public MessageBoard {
        messages = List.copyOf(messages);
    }

    private Map<Animal.Kind, Integer> forMeadowAnimalPoints (Set<Animal> animals) {
        // see https://edstem.org/eu/courses/1101/discussion/93404?comment=175157
        Map<Animal.Kind, Integer> count = new HashMap<>();
        for (Animal animal: animals) {
            count.put(animal.kind(), count.getOrDefault(animal.kind(), 0) + 1);
        }
        return count;
    }

    private int forMeadowTotalAnimals (Set<Animal> animals) {
        Map<Animal.Kind, Integer> points = forMeadowAnimalPoints(animals);
        return Points.forMeadow(points.get(Animal.Kind.MAMMOTH), points.get(Animal.Kind.AUROCHS), points.get(Animal.Kind.DEER));
    }

    private MessageBoard withNewMessage(String text, int count, Set<PlayerColor> scorers, Set<Integer> tileIds) {
        // we instantiate this as an array list because we will only add messages at the end
        List<Message> newMessages = new ArrayList<>(messages);
        newMessages.add(new Message(text, count, scorers, tileIds));
        return new MessageBoard(textMaker, newMessages);
    }

    public Map<PlayerColor, Integer> points() {
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
        int tileCount = forest.tileIds().size();
        int mushroomCount = Area.mushroomGroupCount(forest);
        int points = Points.forClosedForest(tileCount, mushroomCount);
        Set<PlayerColor> majorityOccupants = forest.majorityOccupants();
        return withNewMessage(
                textMaker.playersScoredForest(majorityOccupants, points, mushroomCount, tileCount),
                points,
                majorityOccupants,
                forest.tileIds()
        );
    }

    public MessageBoard withClosedForestWithMenhir(PlayerColor player, Area<Zone.Forest> forest) {
        return withNewMessage(
                textMaker.playerClosedForestWithMenhir(player),
                0,
                Set.of(player),
                forest.tileIds()
        );
    }

    public MessageBoard withScoredRiver(Area<Zone.River> river) {
        if (!river.isOccupied()) return this;
        int tileCount = river.tileIds().size();
        int fishCount = Area.riverFishCount(river);
        int points = Points.forClosedRiver(tileCount, fishCount);
        Set<PlayerColor> majorityOccupants = river.majorityOccupants();
        return withNewMessage(
                textMaker.playersScoredRiver(majorityOccupants, points, fishCount, tileCount),
                points,
                majorityOccupants,
                river.tileIds()
        );
    }

    public MessageBoard withScoredHuntingTrap(PlayerColor scorer, Area<Zone.Meadow> adjacentMeadow) {
        // important to understand
        // adjacentMeadow is an area created specifically for the hunting trap
        // therefore it contains the right zones
        // the ones from the main meadow, and the ones from the 8 neighboring tiles
        Set<Animal> animals = Area.animals(adjacentMeadow, Set.of());
        int points = forMeadowTotalAnimals(animals);
        if (points == 0) return this;
        return withNewMessage(
                textMaker.playerScoredHuntingTrap(scorer, points, forMeadowAnimalPoints(animals)),
                points,
                Set.of(scorer),
                adjacentMeadow.tileIds()
        );
    }

    public MessageBoard withScoredLogboat(PlayerColor scorer, Area<Zone.Water> riverSystem) {
        if (!riverSystem.isOccupied()) return this;
        int lakeCount = Area.lakeCount(riverSystem);
        int points = Points.forLogboat(lakeCount);
        return withNewMessage(
                textMaker.playerScoredLogboat(scorer, points, lakeCount),
                points,
                Set.of(scorer),
                riverSystem.tileIds()
        );
    }

    public MessageBoard withScoredMeadow(Area<Zone.Meadow> meadow, Set<Animal> cancelledAnimals) {
        if (!meadow.isOccupied()) return this;
        Set<Animal> animals = Area.animals(meadow, cancelledAnimals);
        int points = forMeadowTotalAnimals(animals);
        if (points == 0) return this;
        return withNewMessage(
                textMaker.playersScoredMeadow(meadow.majorityOccupants(), points, forMeadowAnimalPoints(animals)),
                points,
                meadow.majorityOccupants(),
                meadow.tileIds()
        );
    }

    public MessageBoard withScoredRiverSystem(Area<Zone.Water> riverSystem) {
        if (!riverSystem.isOccupied()) return this;
        int fishCount = Area.riverSystemFishCount(riverSystem);
        int points = Points.forRiverSystem(fishCount);
        if (points == 0) return this;
        Set<PlayerColor> majorityOccupants = riverSystem.majorityOccupants();
        return withNewMessage(
                textMaker.playersScoredRiverSystem(majorityOccupants, points, fishCount),
                points,
                majorityOccupants,
                riverSystem.tileIds()
        );
    }

    public MessageBoard withScoredPitTrap(Area<Zone.Meadow> adjacentMeadow, Set<Animal> cancelledAnimals) {
        if (!adjacentMeadow.isOccupied()) return this;
        Set<Animal> animals = Area.animals(adjacentMeadow, cancelledAnimals);
        int points = forMeadowTotalAnimals(animals);
        if (points == 0) return this;
        return withNewMessage(
                textMaker.playersScoredPitTrap(adjacentMeadow.majorityOccupants(), points, forMeadowAnimalPoints(animals)),
                points,
                adjacentMeadow.majorityOccupants(),
                adjacentMeadow.tileIds()
        );
    }

    public MessageBoard withScoredRaft(Area<Zone.Water> riverSystem) {
        if (!riverSystem.isOccupied()) return this;
        int lakeCount = Area.lakeCount(riverSystem);
        int points = Points.forRaft(lakeCount);
        Set<PlayerColor> majorityOccupants = riverSystem.majorityOccupants();
        return withNewMessage(
                textMaker.playersScoredRaft(majorityOccupants, points, lakeCount),
                points,
                majorityOccupants,
                riverSystem.tileIds()
        );
    }

    public MessageBoard withWinners(Set<PlayerColor> winners, int points) {
        return withNewMessage(
                textMaker.playersWon(winners, points),
                // see https://edstem.org/eu/courses/1101/discussion/93737?answer=175785
                0,
                winners,
                Set.of()
        );
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
