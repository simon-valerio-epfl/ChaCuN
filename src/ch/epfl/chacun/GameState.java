package ch.epfl.chacun;

import javax.swing.*;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public record GameState (
        List<PlayerColor> players,
        TileDecks tileDecks,
        Tile tileToPlace,
        Board board,
        Action nextAction,
        MessageBoard messageBoard
) {

    final static int MAX_PAWN_PER_PLAYER = 5;
    final static int MAX_HUT_PER_PLAYER = 3;

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
        return switch (kind) {
            case PAWN -> MAX_PAWN_PER_PLAYER - board.occupantCount(player, kind);
            case HUT -> MAX_HUT_PER_PLAYER - board.occupantCount(player, kind);
        };
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

    public GameState withPlacedTile(PlacedTile tile) {
        if (nextAction != Action.PLACE_TILE) throw new IllegalArgumentException();

    }

    public enum Action {
        START_GAME,
        PLACE_TILE,
        RETAKE_PAWN,
        OCCUPY_TILE,
        END_GAME
    }

}
