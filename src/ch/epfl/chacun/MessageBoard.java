package ch.epfl.chacun;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
//TODO METS LE AUTHORS
/**
 * Represents the message board of the game.
 * @param textMaker the text maker used to generate the content of the messages
 * @param messages the ordered messages on the message board, from the oldest to the newest
 */
public record MessageBoard(TextMaker textMaker, List<Message> messages) {

    /**
     * Constructor for MessageBoard, validating the parameters
     * @param textMaker the text maker used to generate the content of the messages
     * @param messages the ordered messages on the message board, from the oldest to the newest
     */
    public MessageBoard {
        // the list is copied to ensure immutability
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
            return Points.forMeadow(
                points.getOrDefault(Animal.Kind.MAMMOTH, 0),
                points.getOrDefault(Animal.Kind.AUROCHS, 0),
                points.getOrDefault(Animal.Kind.DEER, 0)
            );
    }

    private MessageBoard withNewMessage(String text, int count, Set<PlayerColor> scorers, Set<Integer> tileIds) {
        // we instantiate this as an array list because we will only add messages at the end
        List<Message> newMessages = new ArrayList<>(messages);
        newMessages.add(new Message(text, count, scorers, tileIds));
        return new MessageBoard(textMaker, newMessages);
    }

    /**
     * Returns a map matching the scorers to the points
     * they got from the messages on the message board
     * @return a map matching the scorers to the points they got from the messages on the message board
     */
    public Map<PlayerColor, Integer> points() {
        Map<PlayerColor, Integer> playerPoints = new HashMap<>();
        for (Message message: messages) {
            for (PlayerColor player: message.scorers()) {
                playerPoints.put(player, playerPoints.getOrDefault(player, 0) + message.points());
            }
        }
        return playerPoints;
    }

    /**
     * If the river area is occupied,
     * the closure of the river getting some player some points,
     * the method returns a new message board with the message of the event added
     * and the points added to the scorers
     * If the forest isn't occupied (no scorers), returns the same message board
     * @param forest the forest that has been closed
     * @return a new message board with the message of the event,
     *                  the same message board if the forest isn't occupied
     */
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

    /**
     * Returns a new message board with the message of the event added
     * signaling that the player can place another tile because the forest
     * he has closed contains one or more menhirs.
     * @param player the player who placed the tile
     * @param forest the forest area that has been closed
     * @return a new message board with the message of the event added
     *
     */
    public MessageBoard withClosedForestWithMenhir(PlayerColor player, Area<Zone.Forest> forest) {
        return withNewMessage(
                textMaker.playerClosedForestWithMenhir(player),
                0,
                Set.of(),
                forest.tileIds()
        );
    }

    /**
     * If the river area is occupied,
     * the closure of the river getting some player some points,
     * the method returns a new message board with the message of the event added
     * and the points added to the scorers
     * If the river isn't occupied (no scorers), returns the same message board
     * @param river the river that has been closed
     * @return a new message board with the message of the event,
     *                  the same message board if the river isn't occupied
     */
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

    /**
     * If placing the hunting trap got the player some points,
     * the method returns a new message board with the message of the event added
     * @param scorer the player who placed the hunting trap
     * @param adjacentMeadow the meadow area adjacent to the hunting trap,
     *                      containing the meadows surrounding the placed hunting trap
     * @return a new message board with the message of the event added,
     *                 the same message board if the hunting trap didn't get any point
     */
    public MessageBoard withScoredHuntingTrap(PlayerColor scorer, Area<Zone.Meadow> adjacentMeadow) {
        // important to understand
        // adjacentMeadow is an area created specifically for the hunting trap
        // therefore it contains the right zones
        // the ones from the main meadow, and the ones from the 8 neighboring tiles
        Set<Animal> animals = Area.animals(adjacentMeadow, Set.of());
        int points = forMeadowTotalAnimals(animals);
        // if there is no animal the hunting trap won't give the placer any point
        if (points == 0) return this;
        return withNewMessage(
                textMaker.playerScoredHuntingTrap(scorer, points, forMeadowAnimalPoints(animals)),
                points,
                Set.of(scorer),
                adjacentMeadow.tileIds()
        );
    }

    /**
     * Returns a new message board with the message of the event added,
     * signaling that the player got some points from placing the logboat
     * on the given river system
     * @param scorer the player who placed the logboat
     * @param riverSystem the river system the logboat has been placed on
     * @return a new message board with the message of the event added
     */
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

    /**
     * If the meadow area is occupied and the scored points (before cancelling animals) are positive,
     * the method returns a new message board with the message of the event added
     * @param meadow
     * @param cancelledAnimals
     * @return
     */
    public MessageBoard withScoredMeadow(Area<Zone.Meadow> meadow, Set<Animal> cancelledAnimals) {
        if (!meadow.isOccupied()) return this;
        Set<Animal> animals = Area.animals(meadow, cancelledAnimals);
        int points = forMeadowTotalAnimals(animals);
        if (points == 0) return this;
        Set<PlayerColor> majorityOccupants = meadow.majorityOccupants();
        return withNewMessage(
                textMaker.playersScoredMeadow(majorityOccupants, points, forMeadowAnimalPoints(animals)),
                points,
                majorityOccupants,
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
        Set<PlayerColor> majorityOccupants = adjacentMeadow.majorityOccupants();
        return withNewMessage(
                textMaker.playersScoredPitTrap(majorityOccupants, points, forMeadowAnimalPoints(animals)),
                points,
                majorityOccupants,
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

    /**
     * Represents a message on the message board.
     * @param text the text of the message, non-null
     * @param points the points the scorers get from the event triggering this message, a non-negative integer
     * @param scorers the players who will get the points
     * @param tileIds the ids of the tiles involved in the event triggering this message
     */
    public record Message(String text, int points, Set<PlayerColor> scorers, Set<Integer> tileIds) {
        /**
         * Constructor for Message, validating the parameters
         * the text has to be non-null
         * the points have to be non-negative
         * the scorers and the tileIds may be empty
         * (if the event triggering the message doesn't get any point)
         */
        public Message {
            Objects.requireNonNull(text);
            Preconditions.checkArgument(points >= 0);
            // the sets are copied to ensure immutability,
            // PlayerColors and Integers are immutable
            scorers = Set.copyOf(scorers);
            tileIds = Set.copyOf(tileIds);
        }
    }
}
