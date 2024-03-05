package ch.epfl.chacun;

import java.util.*;
import java.util.stream.Collectors;

public record MessageBoard(TextMaker textMaker, List<Message> messages) {

    public MessageBoard {
        Objects.requireNonNull(textMaker); // todo: not specified, should we check for null?
        messages = List.copyOf(messages);
    }

    Map<PlayerColor, Integer> points() {
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

    MessageBoard withScoredForest(Area<Zone.Forest> forest) {
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

    MessageBoard withClosedForestWithMenhir(PlayerColor player, Area<Zone.Forest> forest) {
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

    MessageBoard withScoredRiver(Area<Zone.River> river) {
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

    MessageBoard withScoredHuntingTrap(PlayerColor scorer, Area<Zone.Meadow> adjacentMeadow) {
        if (!adjacentMeadow.isOccupied()) return this;
        List<Message> newMessages = new ArrayList<>(messages);
        Area.animals(adjacentMeadow, Set.of());
        // todo: comment gérer 8 tuiles adjacentes ?
        // todo: comment gérer les cancelled animals ?
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
