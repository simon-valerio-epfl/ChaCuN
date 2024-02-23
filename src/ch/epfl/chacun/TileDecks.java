package ch.epfl.chacun;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public record TileDecks (List<Tile> startTiles, List<Tile> normalTiles, List<Tile> menhirTiles) {

    public TileDecks {
        startTiles = List.copyOf(startTiles);
        normalTiles = List.copyOf(normalTiles);
        menhirTiles = List.copyOf(menhirTiles);
    }

    public int deckSize(Tile.Kind kind) {
        return switch (kind) {
            case START -> startTiles.size();
            case NORMAL -> normalTiles.size();
            case MENHIR -> menhirTiles.size();
        };
    }

    private Tile getLastCardOrNull(List<Tile> deck) {
        return deck.isEmpty() ? null : deck.getFirst();
    }

    private List<Tile> drawCardFromDeck (List<Tile> deck) {
        if (deck.isEmpty()) {
            throw new IllegalArgumentException();
        }
        List<Tile> newDeck = List.copyOf(deck);
        newDeck.removeFirst();
        return newDeck;
    }

    public Tile topTile (Tile.Kind kind) {
        return switch (kind) {
            case START -> getLastCardOrNull(startTiles);
            case NORMAL -> getLastCardOrNull(normalTiles);
            case MENHIR -> getLastCardOrNull(menhirTiles);
        };
    }

    /**
     *
     * @param kind
     * @return
     *
     * @throws IllegalArgumentException if the deck is empty
     */
    public TileDecks withTopTileDrawn (Tile.Kind kind) {
        return switch (kind) {
            case START -> new TileDecks(drawCardFromDeck(startTiles), normalTiles, menhirTiles);
            case NORMAL -> new TileDecks(startTiles, drawCardFromDeck(normalTiles), menhirTiles);
            case MENHIR -> new TileDecks(startTiles, normalTiles, drawCardFromDeck(menhirTiles));
        };
    }

    public TileDecks withTopTileDrawnUntil (Tile.Kind kind, Predicate<Tile> predicate) {
        TileDecks verifiedDecks = this;
        while (!predicate.test(verifiedDecks.topTile(kind))) {
            verifiedDecks = withTopTileDrawn(kind);
        }
        return verifiedDecks;
    }

}
