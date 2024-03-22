package ch.epfl.chacun;

import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record GameState (
        List<PlayerColor> players,
        TileDecks tileDecks,
        Tile tileToPlace,
        Board board,
        Action nextAction,
        MessageBoard messageBoard
) {

    public GameState {

        Objects.requireNonNull(tileDecks);
        Objects.requireNonNull(board);
        Objects.requireNonNull(nextAction);
        Objects.requireNonNull(messageBoard);

        Objects.requireNonNull(players);
        players = List.copyOf(players);
        Preconditions.checkArgument(players.size() >= 2);

        if (nextAction == Action.PLACE_TILE) Objects.requireNonNull(tileToPlace);
        else Preconditions.checkArgument(tileToPlace == null);
    }

    public static GameState initial(List<PlayerColor> players, TileDecks tileDecks, TextMaker textMaker) {
        return new GameState(
                players,
                tileDecks,
                null,
                Board.EMPTY,
                Action.START_GAME,
                new MessageBoard(textMaker, List.of())
        );
    }

    public PlayerColor currentPlayer() {
        return nextAction == Action.START_GAME || nextAction == Action.END_GAME
                ? null
                : players.getFirst();
    }

    public int freeOccupantsCount(PlayerColor player, Occupant.Kind kind) {
        return Occupant.occupantsCount(kind) - board.occupantCount(player, kind);
    }

    public Set<Occupant> lastTilePotentialOccupants() {
        PlacedTile tile = board.lastPlacedTile();
        // if the board is empty, lastPlacedTile will return null
        Preconditions.checkArgument(tile != null);
        Set<Integer> occupiedPawnZonesIds = new HashSet<>();
        Set<Integer> occupiedHutZonesIds = new HashSet<>();
        for (Zone zone: tile.tile().zones()) {
            switch (zone) {
                case Zone.Forest forestZone -> {
                    if (!board.forestArea(forestZone).occupants().isEmpty()) {
                        occupiedPawnZonesIds.add(forestZone.id());
                    }
                }
                case Zone.Meadow meadowZone -> {
                    if (!board.meadowArea(meadowZone).occupants().isEmpty()) {
                        occupiedPawnZonesIds.add(meadowZone.id());
                    }
                }
                case Zone.River riverZone -> {
                    if (!board.riverArea(riverZone).occupants().isEmpty()) {
                        occupiedPawnZonesIds.add(riverZone.id());
                    }
                }
                case Zone.Water waterZone -> {
                    if (!board.riverSystemArea(waterZone).occupants().isEmpty()) {
                        occupiedHutZonesIds.add(waterZone.id());
                    }
                }
            }
        }
        return tile.potentialOccupants()
            .stream()
            .filter(occupant ->
                switch (occupant.kind()) {
                    case PAWN -> !occupiedPawnZonesIds.contains(occupant.zoneId());
                    case HUT ->  !occupiedHutZonesIds.contains(occupant.zoneId());
                }
            ).collect(Collectors.toSet());
    }

    public GameState withStartingTilePlaced() {
        if (nextAction != Action.START_GAME) throw new IllegalArgumentException();
        // pioche la carte de départ et la place sur le jeu
        Tile startingTile = tileDecks.topTile(Tile.Kind.START);
        PlacedTile startingPlacedTile = new PlacedTile(startingTile, null, Rotation.NONE, new Pos(0, 0));
        TileDecks newDecks = tileDecks.withTopTileDrawn(Tile.Kind.START);
        Board newBoard = board.withNewTile(startingPlacedTile);
        // pioche la prochaine carte normale et la donne au prochain game state
        Tile tileToPlace = newDecks.topTile(Tile.Kind.NORMAL);
        newDecks = newDecks.withTopTileDrawn(Tile.Kind.NORMAL);
        return new GameState(players, newDecks, tileToPlace, newBoard, Action.PLACE_TILE, messageBoard);
    }

    private SimpleEntry<Integer, Set<PlayerColor>> getWinnersPoints(Map<PlayerColor, Integer> scorersToPoints) {
        Set<PlayerColor> winners = new HashSet<>();
        int max = -1;
        for (Map.Entry<PlayerColor, Integer> scorerToPoints: scorersToPoints.entrySet()) {
            int scorerPoints = scorerToPoints.getValue();
            if (scorerPoints > max) {
                max = scorerPoints;
                winners.clear();
            }
            if (scorerPoints == max) winners.add(scorerToPoints.getKey());
        }
        return new SimpleEntry<>(max, winners);
    }

    public GameState withPlacedTile(PlacedTile tile) {
        Preconditions.checkArgument(nextAction == Action.PLACE_TILE);

        MessageBoard newMessageBoard = messageBoard;
        Board newBoard = board.withNewTile(tile);

        Zone specialPowerZone = tile.specialPowerZone();
        if (specialPowerZone != null) {
            switch (specialPowerZone.specialPower()) {
                case SHAMAN -> {
                    if (board.occupantCount(currentPlayer(), Occupant.Kind.PAWN) > 0) {
                        return new GameState(players, tileDecks, null, newBoard, Action.RETAKE_PAWN, messageBoard);
                    }
                }
                case LOGBOAT -> {
                    Area<Zone.Water> riverSystem = newBoard.riverSystemArea((Zone.Water) specialPowerZone);
                    newMessageBoard = newMessageBoard.withScoredLogboat(currentPlayer(), riverSystem);
                }
                case HUNTING_TRAP -> {
                    Area<Zone.Meadow> adjacentMeadow = newBoard.adjacentMeadow(tile.pos(), (Zone.Meadow) specialPowerZone);
                    Set<Animal> animals = Area.animals(adjacentMeadow, newBoard.cancelledAnimals());
                    int deerCount = animalsOfKind(animals, Animal.Kind.DEER).size();
                    int tigerCount = animalsOfKind(animals, Animal.Kind.TIGER).size();
                    int deerCountToCancel = tigerCount > deerCount ? 0 : deerCount - tigerCount;
                    // todo calculer les cerfs
                    newBoard = newBoard.withMoreCancelledAnimals(animals);
                    newMessageBoard = newMessageBoard.withScoredHuntingTrap(currentPlayer(), adjacentMeadow);
                }
            }
        }

        return new GameState(players, tileDecks, null, newBoard, Action.OCCUPY_TILE, newMessageBoard)
                .withTurnFinishedIfOccupationImpossible();
    }

    private GameState withTurnFinishedIfOccupationImpossible() {
        Preconditions.checkArgument(nextAction == Action.OCCUPY_TILE);
        return canOccupyTile(lastTilePotentialOccupants(), currentPlayer())
                ? this : withTurnFinished();
    }

    private GameState withTurnFinished () {

        Preconditions.checkArgument(nextAction == Action.OCCUPY_TILE);
        Preconditions.checkArgument(board.lastPlacedTile() != null);

        MessageBoard newMessageBoard = messageBoard;
        Board newBoard = board;
        TileDecks newTileDecks = tileDecks;

        // gestion de la fermeture des rivières et forêts
        Set<Area<Zone.Forest>> closedForests = newBoard.forestsClosedByLastTile();
        for (Area<Zone.Forest> forest: closedForests) newMessageBoard = newMessageBoard.withScoredForest(forest);
        Set<Area<Zone.River>> closedRivers = newBoard.riversClosedByLastTile();
        for (Area<Zone.River> river: closedRivers) newMessageBoard = newMessageBoard.withScoredRiver(river);
        newBoard = newBoard.withoutGatherersOrFishersIn(closedForests, closedRivers);

        // on regarde s'il existe une forêt menhir fermée
        Area<Zone.Forest> forestClosedMenhir = closedForests.stream()
                .filter(Area::hasMenhir)
                .findFirst()
                .orElse(null);


        assert newBoard.lastPlacedTile() != null;
        if (forestClosedMenhir != null && newBoard.lastPlacedTile().kind() == Tile.Kind.NORMAL) {
            newTileDecks = newTileDecks.withTopTileDrawnUntil(Tile.Kind.MENHIR, newBoard::couldPlaceTile);
            boolean couldPlaceMenhirTile = newTileDecks.deckSize(Tile.Kind.MENHIR) > 0;
            if (couldPlaceMenhirTile) {
                newMessageBoard = newMessageBoard.withClosedForestWithMenhir(currentPlayer(), forestClosedMenhir);
                return new GameState(players, newTileDecks.withTopTileDrawn(Tile.Kind.MENHIR),
                        newTileDecks.topTile(Tile.Kind.MENHIR),
                        newBoard, Action.PLACE_TILE, newMessageBoard
                );
            }
        }

        newTileDecks = newTileDecks.withTopTileDrawnUntil(Tile.Kind.NORMAL, newBoard::couldPlaceTile);

        if (newTileDecks.deckSize(Tile.Kind.NORMAL) > 0) {
            return new GameState(withNextPlayer(), newTileDecks.withTopTileDrawn(Tile.Kind.NORMAL),
                    newTileDecks.topTile(Tile.Kind.NORMAL),
                    newBoard, Action.PLACE_TILE, newMessageBoard
            );
        } else {
            return new GameState(players, tileDecks, null, newBoard, Action.END_GAME, newMessageBoard)
                .withFinalPointsCounted();
        }

    }

    private Set<Animal> animalsOfKind(Set<Animal> animals, Animal.Kind kind) {
        return animals.stream().filter(animal -> animal.kind() == kind).collect(Collectors.toSet());
    }

    private GameState withFinalPointsCounted () {

        Board newBoard = board;
        MessageBoard newMessageBoard = messageBoard;

        // gestion du pit trap
        for (Area<Zone.Meadow> meadowArea: newBoard.meadowAreas()) {

            boolean hasWildFireZone = meadowArea.zones().stream()
                    .anyMatch(z -> z.specialPower() == Zone.SpecialPower.WILD_FIRE);
            Zone.Meadow pitTrapZone = meadowArea.zones().stream()
                    .filter(z -> z.specialPower() == Zone.SpecialPower.PIT_TRAP).findAny().orElse(null);

            if (!hasWildFireZone) {

                Set<Animal> allAnimals = Area.animals(meadowArea, newBoard.cancelledAnimals());
                Set<Animal> deers = animalsOfKind(allAnimals, Animal.Kind.DEER);
                Set<Animal> tigers = animalsOfKind(allAnimals, Animal.Kind.TIGER);
                int toCancelCount = Math.min(tigers.size(), deers.size());

                if (pitTrapZone != null) {
                    PlacedTile pitTrapTile = newBoard.tileWithId(pitTrapZone.tileId());
                    Area<Zone.Meadow> adjacentMeadow = newBoard.adjacentMeadow(pitTrapTile.pos(), pitTrapZone);
                    Set<Animal> adjacentAnimals = Area.animals(adjacentMeadow, newBoard.cancelledAnimals());
                    Set<Animal> adjacentDeers = animalsOfKind(adjacentAnimals, Animal.Kind.DEER);
                    Set<Animal> farAwayDeers = deers.stream()
                            .filter(deer -> !adjacentDeers.contains(deer)).collect(Collectors.toSet());

                    newBoard = newBoard.withMoreCancelledAnimals(
                            Stream.concat(farAwayDeers.stream(), deers.stream())
                            .distinct()
                            .limit(toCancelCount)
                            .collect(Collectors.toSet())
                    );

                    newMessageBoard = newMessageBoard.withScoredPitTrap(adjacentMeadow, newBoard.cancelledAnimals());

                } else newBoard = newBoard.withMoreCancelledAnimals(
                        deers.stream().limit(toCancelCount).collect(Collectors.toSet())
                );

            }

            newMessageBoard = newMessageBoard.withScoredMeadow(meadowArea, newBoard.cancelledAnimals());

        }

        for (Area<Zone.Water> waterArea: newBoard.riverSystemAreas()) {

            newMessageBoard = newMessageBoard.withScoredRiverSystem(waterArea);
            boolean hasRaft = waterArea.zoneWithSpecialPower(Zone.SpecialPower.RAFT) != null;
            if (hasRaft) newMessageBoard = newMessageBoard.withScoredRaft(waterArea);

        }

        SimpleEntry<Integer, Set<PlayerColor>> winners = getWinnersPoints(newMessageBoard.points());
        newMessageBoard = newMessageBoard.withWinners(winners.getValue(), winners.getKey());

        return new GameState(players, tileDecks, null, newBoard, nextAction, newMessageBoard);
    }

    private boolean canOccupyTile(Set<Occupant> potentialOccupants, PlayerColor player) {
        return potentialOccupants
            .stream().anyMatch(potentialOccupant -> freeOccupantsCount(player, potentialOccupant.kind()) > 0);
    }

    private List<PlayerColor> withNextPlayer () {
        List<PlayerColor> newPlayers = new LinkedList<>(players);
        PlayerColor placer = newPlayers.removeFirst();
        newPlayers.addLast(placer);
        return newPlayers;
    }

    // cette méthode est appelée quand la personne pose une tuile menhir
    // et que cette tuile menhir contient le chaman
    // alors AVANT qu'il ne puisse décider s'il veut occuper la tuile
    // on lui propose de retirer un pion
    public GameState withOccupantRemoved(Occupant occupant){
        Preconditions.checkArgument(nextAction == Action.RETAKE_PAWN);
        Preconditions.checkArgument(occupant == null || occupant.kind() == Occupant.Kind.PAWN);
        return new GameState(players, tileDecks, null,
            occupant != null ? board.withoutOccupant(occupant) : board,
            Action.OCCUPY_TILE, messageBoard)
        .withTurnFinishedIfOccupationImpossible();
    }

    public GameState withNewOccupant(Occupant occupant) {
        Preconditions.checkArgument(nextAction == Action.OCCUPY_TILE);
        Preconditions.checkArgument(board.lastPlacedTile() != null);
        return new GameState(players, tileDecks, tileToPlace,
            occupant != null ? board.withOccupant(occupant) : board,
            Action.OCCUPY_TILE, messageBoard)
        .withTurnFinished();
    }

    public enum Action {
        START_GAME,
        PLACE_TILE,
        RETAKE_PAWN,
        OCCUPY_TILE,
        END_GAME
    }

}
