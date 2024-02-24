package ch.epfl.chacun;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

/**
 * Represents the decks of tiles in the game
 * @param startTiles the start tiles deck (the first card of the game)
 * @param normalTiles the normal tiles deck
 * @param menhirTiles the menhir tiles deck
 */
public record TileDecks (List<Tile> startTiles, List<Tile> normalTiles, List<Tile> menhirTiles) {

    /**
     * Constructor for TileDecks
     * @param startTiles the start tiles deck (the first card of the game)
     * @param normalTiles the normal tiles deck
     * @param menhirTiles the menhir tiles deck
     */
    public TileDecks {
        startTiles = List.copyOf(startTiles);
        normalTiles = List.copyOf(normalTiles);
        menhirTiles = List.copyOf(menhirTiles);
    }

    /**
     * returns the size of the deck of the given kind
     * @param kind the kind of deck whose size is to be returned
     * @return the size of the deck of the given kind
     */
    public int deckSize(Tile.Kind kind) {
        return switch (kind) {
            case START -> startTiles.size();
            case NORMAL -> normalTiles.size();
            case MENHIR -> menhirTiles.size();
        };
    }

    /**
     * Gets the first card of a deck or null if the latter is empty
     * @param deck the deck to get the first card from
     * @return the first card of a deck or null if the latter is empty
     */
    private Tile getFirstCard(List<Tile> deck) {
        return deck.isEmpty() ? null : deck.getFirst();
    }

    /**
     * Draws a card from a deck if the latter is not empty, throws an exception otherwise
     * @param deck the deck to draw the card from
     * @return the deck without the first card
     */
    private List<Tile> drawCardFromDeck (List<Tile> deck) {
        if (deck.isEmpty()) {
            throw new IllegalArgumentException();
        }
        List<Tile> newDeck = List.copyOf(deck);
        newDeck.removeFirst();
        return newDeck;
    }
    /**
     * Gets the top tile of the deck of the given kind, returning the first card of the deck
     * @param kind the kind of deck to get the top tile from
     * @return the first card of the deck of the given kind
     */
    public Tile topTile (Tile.Kind kind) {
        return switch (kind) {
            case START -> getFirstCard(startTiles);
            case NORMAL -> getFirstCard(normalTiles);
            case MENHIR -> getFirstCard(menhirTiles);
        };
    }

    /**
     * Draws the top tile of the deck of the given kind returning the deck without the first card
     * @param kind the kind of deck to draw the top tile from
     * @return the deck without the top tile
     */
    public TileDecks withTopTileDrawn (Tile.Kind kind) {
        return switch (kind) {
            case START -> new TileDecks(drawCardFromDeck(startTiles), normalTiles, menhirTiles);
            case NORMAL -> new TileDecks(startTiles, drawCardFromDeck(normalTiles), menhirTiles);
            case MENHIR -> new TileDecks(startTiles, normalTiles, drawCardFromDeck(menhirTiles));
        };
    }

    /**
     * Draws the top tile of the deck of the given kind until a predicate is satisfied
     * @param kind the kind of deck to draw the top tile from
     * @param predicate the predicate to satisfy by the first card of the specified deck
     * @return the tile decks, with the necessary tiles drawn
     */
    public TileDecks withTopTileDrawnUntil (Tile.Kind kind, Predicate<Tile> predicate) {
        TileDecks verifiedDecks = this;
        while (!predicate.test(verifiedDecks.topTile(kind))) {
            verifiedDecks = withTopTileDrawn(kind);
        }
        return verifiedDecks;
    }

}
