package ch.epfl.chacun.gui;

import ch.epfl.chacun.*;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

public final class BoardUI {

    Map<Integer, Image> cachedImages = new HashMap<>();

    private BoardUI() {}

    public Node create(
            int range,
            ObservableValue<GameState> gameStateO,
            ObservableValue<Rotation> rotationO,
            ObservableValue<Set<Occupant>> occupantO,
            ObservableValue<Set<Integer>> highlightedTilesO,

            Consumer<Rotation> rotationConsumer,
            Consumer<Pos> posConsumer,
            Consumer<Occupant> occupantConsumer
    ) {
        Preconditions.checkArgument(range > 0);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setId("board-scroll-pane");
        scrollPane.getStylesheets().add("board.css");

        GridPane pane = new GridPane();
        pane.setId("board-grid");

        ObservableValue<Board> boardO = gameStateO.map(GameState::board);
        ObservableValue<Set<Animal>> cancelledAnimalsO = boardO.map(Board::cancelledAnimals);
        ObservableValue<Boolean> isFringeO = gameStateO.map(gState -> gState.nextAction() == GameState.Action.PLACE_TILE);
        // todo ask antoine
        ObservableValue<Set<Pos>> fringeTilesO = isFringeO.map(
            isFringe -> isFringe ? boardO.getValue().insertionPositions() : Set.of()
        );

        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                ImageView imageView = new ImageView();
                Group group = new Group(imageView);
                Pos pos = new Pos(x, y);

                ObservableValue<PlacedTile> placedTileO = boardO.map(b -> b.tileAt(pos));

                ObservableValue<CellData> cellDataO = Bindings.createObjectBinding(() -> {
                    // trigger quand :
                    // - la souris passe sur la tuile
                    // - la frange change

                    boolean isInFringe = fringeTilesO.getValue().contains(pos);
                    PlacedTile placedTile = placedTileO.getValue();

                    Rotation rotation;
                    Image image;
                    if (placedTileO.getValue() != null) {
                        image = cachedImages.computeIfAbsent(placedTile.id(), ImageLoader::normalImageForTile);
                        rotation = placedTileO.getValue().rotation();
                    } else {
                        WritableImage emptyTileImage = new WritableImage(1, 1);
                        emptyTileImage.getPixelWriter().setColor(0, 0, Color.gray(0.98));
                        image = emptyTileImage;
                        rotation = rotationO.getValue();
                    }

                    // todo handle veil
                    Color veilColor = isInFringe ? gameStateO.getValue().currentPlayer();

                    return new CellData(image, veilColor);
                }, fringeTilesO, group.hoverProperty());

                group.rotateProperty().bind(cellDataO.map(e -> e.tileRotation().degreesCW()));

                ObservableValue<Boolean> isInFringe = fringeTilesO.map(fringeTiles -> fringeTiles.contains(pos));
                isInFringe.addListener((_, _, isInFringeValue) -> {
                    if (isInFringeValue) {
                        group.onMouseClickedProperty().setValue(e -> {
                            if (e.getButton() == MouseButton.SECONDARY) {
                                rotationConsumer.accept(e.isAltDown() ? Rotation.RIGHT : Rotation.LEFT);
                            }
                        });
                    }
                });

                placedTileO.addListener((_, oldPlacedTile, placedTile) -> {
                    if (oldPlacedTile != null || placedTile == null) return;
                    // todo ça va de faire comme ça ?
                    group.rotateProperty().set(placedTile.rotation().degreesCW());
                    // change (and cache) the image
                    cachedImages.computeIfAbsent(placedTile.id(), ImageLoader::normalImageForTile);
                    Image image = cachedImages.get(placedTile.id());
                    // todo on peut faire ça au lieu de imageProperty?
                    imageView.setImage(image);
                    // add occupants
                    placedTile.meadowZones().stream()
                        .flatMap(z -> z.animals().stream())
                        .forEach(animal -> {
                            SVGPath animalSvg = new SVGPath();
                            animalSvg.visibleProperty().bind(cancelledAnimalsO.map(animals -> !animals.contains(animal)));
                            animalSvg.setId(STR."marker_\{animal.id()}");
                            group.getChildren().add(animalSvg);
                        });
                    // todo, this should be a getValue right?
                    // or placedTile.potentialOccupants-)
                    gameStateO.getValue()
                        .lastTilePotentialOccupants()
                        .forEach(occupant -> {
                            Node occupantSvg = Icon.newFor(placedTile.placer(), occupant.kind());
                            occupantSvg.setId(STR."\{occupant.kind()}_\{occupant.zoneId()}");
                            occupantSvg.setOnMouseClicked((_) -> occupantConsumer.accept(occupant));
                            group.getChildren().add(occupantSvg);
                        });
                });
            }
        }

        return scrollPane;
    }

    private record CellData (Image tileImage, Rotation tileRotation, Color veilColor) {}

    // todo comment créer une méthode privée
    private SVGPath getEntitySVG () {
        return null;
    }

}
