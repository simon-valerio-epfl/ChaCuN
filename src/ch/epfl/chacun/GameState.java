package ch.epfl.chacun;

import javax.swing.*;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;

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

    public GameState withPlacedTile(PlacedTile tile) {
        if (nextAction != Action.PLACE_TILE) throw new IllegalArgumentException();

        Board newBoard = board.withNewTile(tile);
        Action newNextAction;
        List<PlayerColor> newPlayers = players;
        TileDecks newTileDecks = tileDecks;
        Tile newTileToPlace = null;
        MessageBoard newMessageBoard = messageBoard;

        Zone specialPowerZone = tile.specialPowerZone();
        if (
                specialPowerZone != null && specialPowerZone.specialPower() != null
                && specialPowerZone.specialPower().equals(Zone.SpecialPower.SHAMAN)
                && board.occupantCount(tile.placer(), Occupant.Kind.PAWN) > 0
        ) {
            newNextAction = Action.RETAKE_PAWN;
        }

        else if (
                // todo: should we use lastTilePotentialOccupants()
                tile.potentialOccupants()
                        .stream()
                        .anyMatch(potentialOccupant ->
                                freeOccupantsCount(tile.placer(), potentialOccupant.kind()) >= 0
                        )
        ) {
            newNextAction = Action.OCCUPY_TILE;
        }

        else if (
                newBoard.forestsClosedByLastTile()
                        .stream()
                        .anyMatch(Area::hasMenhir)
                && tileDecks
                        .withTopTileDrawnUntil( Tile.Kind.MENHIR, newBoard::couldPlaceTile)
                        .deckSize(Tile.Kind.MENHIR) > 0
        ) {
            for (Area<Zone.Forest> forest: newBoard.forestsClosedByLastTile()) {
                newMessageBoard = newMessageBoard.withClosedForestWithMenhir(tile.placer(), forest);
            }
            newNextAction = Action.PLACE_TILE;
            newTileDecks = tileDecks.withTopTileDrawnUntil(Tile.Kind.MENHIR, newBoard::couldPlaceTile);
            newTileToPlace = tileDecks.topTile(Tile.Kind.MENHIR);
            newTileDecks = newTileDecks.withTopTileDrawn(Tile.Kind.MENHIR);
        }

        else if (
                tileDecks
                    .withTopTileDrawnUntil(Tile.Kind.NORMAL, newBoard::couldPlaceTile)
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

        return new GameState(newPlayers, newTileDecks, newTileToPlace, newBoard, newNextAction, newMessageBoard);
    }

    public enum Action {
        START_GAME,
        PLACE_TILE,
        RETAKE_PAWN,
        OCCUPY_TILE,
        END_GAME
    }

}