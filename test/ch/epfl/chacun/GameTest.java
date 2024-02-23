package ch.epfl.chacun;

import java.util.List;

public class GameTest {

        //Zone forestZone = new Zone.Forest(1, Zone.Forest.Kind.PLAIN);

        //Zone lakeZone = new Zone.Lake(3, 12, null);
        //Zone riverZone = new Zone.River(2, 12, new Zone.Lake(3, 12, null));
        //Zone jungleZone = new Zone.Forest(4, Zone.Forest.Kind.CORNER);
        Zone.Meadow meadow = new Zone.Meadow(613, List.of(new Animal(6131, Animal.Kind.AUROCHS)), Zone.Meadow.SpecialPower.HUNTING_TRAP);
        Zone.Meadow meadow2 = new Zone.Meadow(614, List.of(new Animal(6141, Animal.Kind.MAMMOTH)), null);
        Zone.Forest forest2 = new Zone.Forest(615, Zone.Forest.Kind.PLAIN);
        Zone.Forest forest = new Zone.Forest(612, Zone.Forest.Kind.WITH_MENHIR);
        TileSide forestSide = new TileSide.Forest(forest);
        TileSide meadowSide = new TileSide.Meadow(meadow);
        TileSide forestSide2 = new TileSide.Forest(forest2);
        TileSide meadowSide2 = new TileSide.Meadow(meadow2);
        Tile tile = new Tile(1, Tile.Kind.START, forestSide, meadowSide, forestSide2, meadowSide2);
        PlayerColor Habib = PlayerColor.RED;
        PlacedTile placedTile = new PlacedTile(tile, Habib, Rotation.RIGHT, new Pos(0, 0));



    

}
