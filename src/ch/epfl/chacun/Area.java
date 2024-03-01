package ch.epfl.chacun;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents an area in the game.
 *
 * @param zones the zones forming the area
 * @param occupants the players having put an occupant in one of the area's zones
 * @param openConnections non-negative, the number of open connections the area has
 * @param <Z> the type of the zones forming the area
 *
 * @author Valerio De Santis (373247)
 * @author Simon Lefort (371918)
 */
public record Area<Z extends Zone> (Set<Z> zones, List<PlayerColor> occupants, int openConnections) {

    /**
     * Checks that the given openConnections is non-negative, and initializes the area
     * with the given zones,occupants and openConnections.
     *
     * @param zones the zones forming the area
     * @param occupants the players having put an occupant in one of the area's zones
     * @param openConnections non-negative, the number of open connections the area has
     */
    public Area  {
        Preconditions.checkArgument(openConnections >= 0);
        zones = Set.copyOf(zones);
        occupants = List.copyOf(sortOccupants(occupants));
    }

    /**
     * Sorts the given list of players basing on the order of their colours and
     * returns a new list containing the same players, but sorted.
     *
     * @param occupants the list of players to sort
     */
    private List<PlayerColor> sortOccupants(List<PlayerColor> occupants) {
        List<PlayerColor> sortedOccupants = new ArrayList<>(occupants);
        Collections.sort(sortedOccupants);
        return sortedOccupants;
    }

    /**
     * Checks whether a certain forest area contains a zone with a menhir.
     * @param forest the forest area to check
     * @return whether the given area has a menhir
     */
    public static boolean hasMenhir(Area<Zone.Forest> forest) {
        // creates a stream containing the zones in the Forest area and checks whether one of them has a menhir
        return forest.zones()
                .stream()
                // we return whether in the given area
                // there is a forest zone containing a menhir or not
                .anyMatch(zone -> zone.kind() == Zone.Forest.Kind.WITH_MENHIR);
    }

    /**
     * Counts the number of mushroom groups in a certain forest area.
     * @param forest the forest area to check
     * @return the number of mushroom groups in the given area
     */
    public static int mushroomGroupCount(Area<Zone.Forest> forest){
        // by default, Stream.count() returns a long value
         return (int) forest.zones()
                 .stream()
                 //we count the number of zones containing mushroom groups
                 //in the given area
                 .filter(zone -> zone.kind() == Zone.Forest.Kind.WITH_MUSHROOMS)
                 .count();
    }
    /**
     * Generates a set containing the animals in a certain meadow area,
     *  excluding the ones in the given set,
     * (which are considered to be eaten in the game this class was thought for).
     *
     * @param meadow the meadow area to check
     * @param cancelledAnimals the set of animals to exclude
     * @return the set of animals in the given area, excluding the ones who have been cancelled
     */
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
    /**
     * Counts the number of fishes in a certain river area and in the lakes connected to it.
     * @param river the river area to check
     * @return the number of fishes in the given river area and in the lakes connected to it
     */
    public static int riverFishCount(Area<Zone.River> river) {
        Set<Zone.Lake> addedLakes = new HashSet<Zone.Lake>();
        return river.zones()
                .stream()
                // we map every zone to the number of fishes we will add
                // because of it
                .mapToInt((zone) ->
                        // we sum the fishes in the river...
                        zone.fishCount() +
                        // ...and the fishes in the neighbouring lakes (only once!)
                        (zone.hasLake() && addedLakes.add(zone.lake())
                                ? zone.lake().fishCount()
                                : 0
                        ))
                // we sum the number of fishes we got in every zone
                .sum();
    }

    /**
     * Checks whether the current instance of area is closed.
     * @return  whether the current instance of area is closed
     */
    public boolean isClosed() {
        return openConnections == 0;
    }
    /**
     * Checks whether there is some occupant in the current instance of area.
     * @return  whether the current instance of area is occupied
     */
    public boolean isOccupied() {
        return !occupants.isEmpty();
    }

    /**
     * Returns the set of players having the majority of occupants
     * in the current instance of area.
     *
     * @return the set of players having the majority of occupants in this area
     */
    public Set<PlayerColor> majorityOccupants() {
        int[] count = new int[PlayerColor.values().length];
        int maxOccupantCount = 0;

        Set<PlayerColor> majority = new HashSet<>();

        for (PlayerColor occupant : occupants) {
            count[occupant.ordinal()]++;
            // we found a player with more occupants
            if (count[occupant.ordinal()] > maxOccupantCount) {
                majority.clear();
                maxOccupantCount = count[occupant.ordinal()];
                // we don't do majority.add(occupant) here
                // as we'll do it in the next if block anyway
            }
            // the current player is one of those having the highest occupant count
            // (for now)
            if (count[occupant.ordinal()] == maxOccupantCount) {
                majority.add(occupant);
            }
        }
        return majority;
    }


    /**
     * Connects the current instance of area to the given one.
     * @param that the area to connect the current instance of area to
     * @return the area obtained by connecting the current instance of area to the given one
     */
    public Area<Z> connectTo(Area<Z> that) {
        //we create a new set containing all the zones in the two areas
        Set<Z> connectedZones = new HashSet<>(Set.copyOf(this.zones));
        connectedZones.addAll(that.zones);
        //we create a new list containing all the occupants in the two areas
        List<PlayerColor> connectedOccupants = new ArrayList<>(List.copyOf(this.occupants));
        connectedOccupants.addAll(that.occupants);
        //we calculate the number of open connections in the new area
        //by subtracting 2 from the sum of the open connections in the two areas if they are different,
        //or by subtracting 2 from the open connections in the current area if it is the same area
        //(we compare their references to know if they are the same area)
        int connectedOpenConnections = this == that
                ? this.openConnections() - 2
                : this.openConnections() + that.openConnections() - 2;
        return new Area<>(connectedZones, connectedOccupants, connectedOpenConnections);
    }

    public Zone zoneWithSpecialPower (Zone.SpecialPower specialPower) {
        for (Zone zone: zones) {
            if (zone.specialPower().equals(specialPower)) {
                return zone;
            }
        }
        return null;
    }

    public Area<Z> withInitialOccupant (PlayerColor occupant) {
        return new Area<>(this.zones, List.of(occupant), this.openConnections);
    }

    public Area<Z> withoutOccupant (PlayerColor occupant) {
        List<PlayerColor> newOccupants = new ArrayList<>(occupants);
        newOccupants.remove(occupant);
        return new Area<>(this.zones, newOccupants, this.openConnections);
    }

    public Area<Z> withoutOccupants () {
        return new Area<>(this.zones, List.of(), this.openConnections);
    }

    public Set<Integer> tileIds() {
        return this.zones.stream().map(Zone::tileId).collect(Collectors.toSet());
    }

}
