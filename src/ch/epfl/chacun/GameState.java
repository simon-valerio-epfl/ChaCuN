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
        Preconditions.checkArgument(tile != null);
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

        MessageBoard newMessageBoard = messageBoard;
        Board newBoard = board.withNewTile(tile);

        Zone specialPowerZone = tile.specialPowerZone();
        if (specialPowerZone != null) {
            switch (specialPowerZone.specialPower()) {
                case SHAMAN -> {
                    // todo can we use when
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
                    newMessageBoard = newMessageBoard.withScoredHuntingTrap(currentPlayer(), adjacentMeadow);
                }
            }
        }

        return new GameState(players, tileDecks, null, newBoard, Action.OCCUPY_TILE, messageBoard)
                .withTurnFinishedIfOccupationImpossible();
    }

    private GameState withTurnFinishedIfOccupationImpossible() {
        return canOccupyTile(lastTilePotentialOccupants(), currentPlayer())
                ? this : withTurnFinished();
    }

    /*

    déterminer les forêts et rivières fermées par la pose de la dernière tuile, et attribuer les points correspondants à leurs occupants majoritaires,
    déterminer si le joueur courant devrait pouvoir jouer un second tour, car il a fermé au moins une forêt contenant un menhir au moyen d'une tuile normale,
    éliminer du sommet du tas contenant la prochaine tuile à jouer la totalité de celles qu'il n'est pas possible de placer sur le plateau, s'il y en a,
    passer la main au prochain joueur si le joueur courant n'a pas le droit ou la possibilité de jouer une tuile menhir,
    terminer la partie si le joueur courant a terminé son ou ses tour(s) et qu'il ne reste plus de tuile normale jouable.

     */
    private GameState withTurnFinished () {

        Preconditions.checkArgument(board.lastPlacedTile() != null);

        MessageBoard newMessageBoard = messageBoard;
        Board newBoard = board;
        TileDecks newTileDecks = tileDecks;
        List<PlayerColor> newPlayers = players;

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
        // todo demander à fabrice si le warning est important
        boolean isLastTileNormal = newBoard.lastPlacedTile().kind() == Tile.Kind.NORMAL;

        boolean menhirDoubleTour = false;

        if (forestClosedMenhir != null && isLastTileNormal) {
            newTileDecks = newTileDecks.withTopTileDrawnUntil(Tile.Kind.MENHIR, newBoard::couldPlaceTile);
            boolean couldPlaceMenhirTile = newTileDecks.deckSize(Tile.Kind.MENHIR) > 0;
            if (couldPlaceMenhirTile) {
                menhirDoubleTour = true;
                newMessageBoard = newMessageBoard.withClosedForestWithMenhir(currentPlayer(), forestClosedMenhir);
            }
        }

        newPlayers = menhirDoubleTour ? players : withNextPlayer();
        Tile.Kind nextKind = menhirDoubleTour ? Tile.Kind.MENHIR : Tile.Kind.NORMAL;

        newTileDecks = newTileDecks.withTopTileDrawnUntil(nextKind, newBoard::couldPlaceTile);
        boolean couldPlaceTile = newTileDecks.deckSize(nextKind) > 0;

        if (couldPlaceTile) {
            return new GameState(newPlayers, newTileDecks.withTopTileDrawn(nextKind), newTileDecks.topTile(nextKind), newBoard, Action.PLACE_TILE, newMessageBoard);
        } else {

            // end game
            // todo compter raft, etc.

            SimpleEntry<Integer, Set<PlayerColor>> winners = getWinnersPoints(newMessageBoard.points());
            newMessageBoard = newMessageBoard.withWinners(winners.getValue(), winners.getKey());
            return new GameState(players, tileDecks, null, newBoard, Action.END_GAME, newMessageBoard);

        }

    }


    private boolean canOccupyTile(Set<Occupant> potentialOccupants, PlayerColor player) {
        return potentialOccupants
            .stream()
            .anyMatch(potentialOccupant -> freeOccupantsCount(player, potentialOccupant.kind()) > 0);
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
        return new GameState(players, tileDecks, null, occupant != null ? board.withoutOccupant(occupant) : board, Action.OCCUPY_TILE, messageBoard)
                .withTurnFinishedIfOccupationImpossible();
    }

    public GameState withNewOccupant(Occupant occupant) {
        return withTurnFinished();
    }

    public enum Action {
        START_GAME,
        PLACE_TILE,
        RETAKE_PAWN,
        OCCUPY_TILE,
        END_GAME
    }

}
