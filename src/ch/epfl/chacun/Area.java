package ch.epfl.chacun;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents an area in the game.
 *
 * @param zones non-null, the zones in the area
 * @param occupants non-null, the occupants in the area
 * @param openConnections non-negative, the number of open connections in the area
 * @param <Z> the type of zone in the area
 *
 * @author Valerio De Santis (373247)
 * @author Simon Lefort (371918)
 */
public record Area<Z extends Zone> (Set<Z> zones, List<PlayerColor> occupants, int openConnections) {

    public Area  {
        Preconditions.checkArgument(openConnections >= 0);
        zones = Set.copyOf(zones);
        occupants = List.copyOf(sortOccupants(occupants));
    }

    private List<PlayerColor> sortOccupants(List<PlayerColor> occupants) {
        List<PlayerColor> sortedOccupants = new ArrayList<>(occupants);
        Collections.sort(sortedOccupants);
        return sortedOccupants;
    }

    public static boolean hasMenhir(Area<Zone.Forest> forest) {
        // creates a stream containing the zones in the Forest area and checks whether one of them has a menhir
        return forest.zones()
                .stream()
                // we return whether in the given area
                // there is a forest zone containing a menhir or not
                .anyMatch(zone -> zone.kind() == Zone.Forest.Kind.WITH_MENHIR);
    }

    public static int mushroomGroupCount(Area<Zone.Forest> forest){
        // by default, Stream.count() returns a long value
         return (int) forest.zones()
                 .stream()
                 //we count the number of zones containing mushroom groups
                 //in the given area
                 .filter(zone -> zone.kind() == Zone.Forest.Kind.WITH_MUSHROOMS)
                 .count();
    }

    public static Set<Animal> animals(Area<Zone.Meadow> meadow, Set<Animal> cancelledAnimals) {
        return meadow.zones()
                .stream()
                // we generate a stream containing several streams (one for each zone),
                // which contains the animals in each zone,
                // then we flatten the stream of streams into a stream (flatMap())

                //we map every zone to the stream of animals it contains
                .flatMap(zone -> zone.animals().stream())
                // we remove all the animals that have been eaten
                // when the lambda returns false (i.e. the animal is in the cancelledAnimals set)
                // the animal is not kept in the stream

                .filter(animal -> !cancelledAnimals.contains(animal))
                // convert the stream to the set we will return, that will
                // therefore contain all the animals living in the area
                .collect(Collectors.toSet());
    }

    public static int riverFishCount(Area<Zone.River> river) {
        Set<Zone.Lake> addedLakes = new HashSet<Zone.Lake>();
        return river.zones()
                .stream()
                // we map every zone to the nu
                .mapToInt((zone) ->
                        // fishes in the river
                        zone.fishCount() +
                        // fishes in the lakes IFF the lake has not been added yet
                        (zone.hasLake() && addedLakes.add(zone.lake())
                                ? zone.lake().fishCount()
                                : 0
                        ))
                .sum();
    }

    public boolean isClosed() {
        return openConnections == 0;
    }

    public boolean isOccupied() {
        return !occupants.isEmpty();
    }

    public Set<PlayerColor> majorityOccupants() {

    }

    public Area<Z> connectTo(Area<Z> that) {

    }

}
