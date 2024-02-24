package ch.epfl.chacun;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

class TileDecksTest {

    private final class TileHasRiverZone implements Predicate<Tile> {
        @Override
        public boolean test(Tile tile) {
            boolean hasRiver = false;
            for (Zone zone: tile.zones()) {
                if (zone instanceof Zone.River river) {
                    hasRiver = true;
                }
            }
            return hasRiver;
        }
    }

    private final class FakePredicate implements Predicate<Tile> {
        @Override
        public boolean test(Tile tile) {
            return true;
        }
    }

    private final class FakeImpossiblePredicate implements Predicate<Tile> {
        @Override
        public boolean test(Tile tile) {
            return false;
        }
    }

    @Test
    public void testDrawUntil() {
        Zone.Meadow meadow = new Zone.Meadow(613, List.of(new Animal(6131, Animal.Kind.AUROCHS)), Zone.Meadow.SpecialPower.HUNTING_TRAP);
        Zone.Meadow meadow2 = new Zone.Meadow(614, List.of(new Animal(6141, Animal.Kind.MAMMOTH)), null);
        Zone.Forest forest2 = new Zone.Forest(615, Zone.Forest.Kind.PLAIN);
        Zone.Forest forest = new Zone.Forest(612, Zone.Forest.Kind.WITH_MENHIR);
        TileSide forestSide = new TileSide.Forest(forest);
        TileSide meadowSide = new TileSide.Meadow(meadow);
        TileSide forestSide2 = new TileSide.Forest(forest2);
        TileSide meadowSide2 = new TileSide.Meadow(meadow2);
        Tile tile = new Tile(1, Tile.Kind.START, forestSide, meadowSide, forestSide2, meadowSide2);


        Set<Zone> expectedSizeZones = new HashSet<>();
        expectedSizeZones.add(forest);
        expectedSizeZones.add(forest2);
        expectedSizeZones.add(meadow2);
        expectedSizeZones.add(meadow);
        assertEquals(expectedSizeZones, tile.sideZones());
        assertEquals(expectedSizeZones, tile.zones());
        Zone forestZone = new Zone.Forest(1, Zone.Forest.Kind.PLAIN);

        Zone.Lake lakeZone = new Zone.Lake(3, 12, null);
        Zone.River riverZone = new Zone.River(2, 12, new Zone.Lake(3, 12, null));
        Zone.Forest jungleZone = new Zone.Forest(4, Zone.Forest.Kind.WITH_MUSHROOMS);

        TileSide riverSide = new TileSide.River(meadow, riverZone, meadow2);
        TileSide jungleSide = new TileSide.Forest(jungleZone);
        Tile tile2 = new Tile(1, Tile.Kind.START, forestSide, riverSide, forestSide2, jungleSide);

        List<Tile> startingTiles = List.of(tile, tile2);
        List<Tile> normalTiles = List.of(tile, tile2);
        List<Tile> menhirTiles = List.of(tile, tile2);

        TileDecks decks = new TileDecks(startingTiles, normalTiles, menhirTiles);
        TileDecks newDecks1 = decks.withTopTileDrawnUntil(Tile.Kind.NORMAL, new TileHasRiverZone());
        assertEquals(1, newDecks1.deckSize(Tile.Kind.NORMAL));

        TileDecks newDecks2 = decks.withTopTileDrawnUntil(Tile.Kind.MENHIR, new FakePredicate());
        assertEquals(2, newDecks2.deckSize(Tile.Kind.MENHIR));

        TileDecks newDecks3 = decks.withTopTileDrawnUntil(Tile.Kind.MENHIR, new FakeImpossiblePredicate());
        assertEquals(0, newDecks3.deckSize(Tile.Kind.MENHIR));
    }

}