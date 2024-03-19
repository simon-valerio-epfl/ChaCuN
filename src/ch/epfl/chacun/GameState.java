package ch.epfl.chacun;

import javax.swing.*;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.stream.Collectors;

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
        if (tile == null) throw new IllegalArgumentException();
        return tile.potentialOccupants();
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

    private int animalCountOfKind (Area<Zone.Meadow> area, Animal.Kind kind) {
        return (int) Area.animals(area, Set.of()).stream().filter(a -> a.kind() == kind).count();
    }

    public GameState withPlacedTile(PlacedTile tile) {
        Preconditions.checkArgument(nextAction == Action.PLACE_TILE);

        Board newBoard = board.withNewTile(tile);
        Action newNextAction;
        MessageBoard newMessageBoard = messageBoard;

        Zone specialPowerZone = tile.specialPowerZone();
        if (specialPowerZone != null) {
            switch (specialPowerZone.specialPower()) {
                case HUNTING_TRAP -> {
                    Area<Zone.Meadow> adjacentMeadow = newBoard.adjacentMeadow(tile.pos(), (Zone.Meadow) specialPowerZone);
                    newMessageBoard = newMessageBoard.withScoredHuntingTrap(currentPlayer(), adjacentMeadow);
                }
                case SHAMAN -> {
                    if (board.occupantCount(currentPlayer(), Occupant.Kind.PAWN) > 0) {
                        newNextAction = Action.RETAKE_PAWN;
                        return new GameState(players, tileDecks, null, newBoard, newNextAction, newMessageBoard);
                    }
                }
                case LOGBOAT -> {
                    Area<Zone.Water> riverSystem = newBoard.riverSystemArea((Zone.Water) specialPowerZone);
                    newMessageBoard = newMessageBoard.withScoredLogboat(currentPlayer(), riverSystem);
                }
                case PIT_TRAP -> {
                    Area<Zone.Meadow> adjacentMeadow = newBoard.adjacentMeadow(tile.pos(), (Zone.Meadow) specialPowerZone);
                    newMessageBoard = newMessageBoard.withScoredPitTrap(adjacentMeadow, board.cancelledAnimals());
                }
                case WILD_FIRE -> {
                    // todo pré complet ?
                    Area<Zone.Meadow> completeMeadow = newBoard.meadowArea((Zone.Meadow) specialPowerZone);
                    Set<Animal> animals = Area.animals(completeMeadow, board.cancelledAnimals());
                    Set<Animal> cancelledTigers = animals.stream().filter(animal -> animal.kind() == Animal.Kind.TIGER).collect(Collectors.toSet());
                    newBoard = newBoard.withMoreCancelledAnimals(cancelledTigers);
                }
                case RAFT -> {
                    Area<Zone.Water> riverSystem = newBoard.riverSystemArea((Zone.Water) specialPowerZone);
                    newMessageBoard = newMessageBoard.withScoredRaft(riverSystem);
                }
            }
        }

        return withNewBoard(newBoard)
                .withNewMessageBoard(newMessageBoard)
                .withOccupyOrPlaced();
    }

    private boolean canOccupyTile(Set<Occupant> potentialOccupants, PlayerColor player) {
        return potentialOccupants
            .stream()
            .anyMatch(potentialOccupant -> freeOccupantsCount(player, potentialOccupant.kind()) > 0);
    }

    private GameState withOccupyOrPlaced() {

        Action newNextAction;
        List<PlayerColor> newPlayers = players;
        TileDecks newTileDecks = tileDecks;
        Tile newTileToPlace = null;
        MessageBoard newMessageBoard = messageBoard;

        if (canOccupyTile(lastTilePotentialOccupants(), currentPlayer())) {
            newNextAction = Action.OCCUPY_TILE;
        }

        else if (
                board.forestsClosedByLastTile()
                        .stream()
                        .anyMatch(Area::hasMenhir)
                && tileDecks
                    .withTopTileDrawnUntil(Tile.Kind.MENHIR, board::couldPlaceTile)
                    .deckSize(Tile.Kind.MENHIR) > 0
        ) {
            for (Area<Zone.Forest> forest: board.forestsClosedByLastTile()) {
                newMessageBoard = newMessageBoard.withClosedForestWithMenhir(currentPlayer(), forest);
            }
            newNextAction = Action.PLACE_TILE;
            newTileDecks = tileDecks.withTopTileDrawnUntil(Tile.Kind.MENHIR, board::couldPlaceTile);
            newTileToPlace = tileDecks.topTile(Tile.Kind.MENHIR);
            newTileDecks = newTileDecks.withTopTileDrawn(Tile.Kind.MENHIR);
        }

        else if (
                tileDecks
                    .withTopTileDrawnUntil(Tile.Kind.NORMAL, board::couldPlaceTile)
                    .deckSize(Tile.Kind.NORMAL) > 0
        ) {
            newNextAction = Action.PLACE_TILE;
            newPlayers = new LinkedList<>(players);
            PlayerColor placer = newPlayers.removeFirst();
            newPlayers.addLast(placer);
        }

        else {
            SimpleEntry<Integer, Set<PlayerColor>> winners = getWinnersPoints(newMessageBoard.points());
            newMessageBoard = newMessageBoard.withWinners(winners.getValue(), winners.getKey());
            newNextAction = Action.END_GAME;
        }

        return new GameState(newPlayers, newTileDecks, newTileToPlace, board, newNextAction, newMessageBoard);

    }

    // cette méthode est appelée quand la personne pose une tuile menhir
    // et que cette tuile menhir contient le chaman
    // alors AVANT qu'il ne puisse décider s'il veut occuper la tuile
    // on lui propose de retirer un pion
    public GameState withOccupantRemoved(Occupant occupant){
        Preconditions.checkArgument(nextAction == Action.RETAKE_PAWN);
        Preconditions.checkArgument(occupant == null || occupant.kind() == Occupant.Kind.PAWN);
        if (occupant != null) return withNewBoard(board.withoutOccupant(occupant)).withNewAction(Action.OCCUPY_TILE);
        return withNewAction(Action.OCCUPY_TILE);
    }

    private GameState withNewBoard(Board newBoard) {
        return new GameState(players, tileDecks, tileToPlace, newBoard, nextAction, messageBoard);
    }

    private GameState withNewAction(Action newAction) {
        return new GameState(players, tileDecks, tileToPlace, board, newAction, messageBoard);
    }

    private GameState withNewMessageBoard(MessageBoard newMessageBoard) {
        return new GameState(players, tileDecks, tileToPlace, board, nextAction, newMessageBoard);
    }

    public enum Action {
        START_GAME,
        PLACE_TILE,
        RETAKE_PAWN,
        OCCUPY_TILE,
        END_GAME
    }

}
