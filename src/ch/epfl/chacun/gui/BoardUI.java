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

    private BoardUI() {}

    // todo change that later to "Node"
    public static ScrollPane create(
            int range,
            ObservableValue<GameState> gameStateO,
            ObservableValue<Rotation> rotationO,
            ObservableValue<Set<Occupant>> occupantO,
            ObservableValue<Set<Integer>> highlightedTilesO,

            Consumer<Rotation> rotationConsumer,
            Consumer<Pos> posConsumer,
            Consumer<Occupant> occupantConsumer
    ) {

        Map<Integer, Image> cachedImages = new HashMap<>();

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
                    boolean isAlreadyPlaced = placedTile != null;

                    Image image = ImageLoader.EMPTY_IMAGE;
                    Rotation rotation = Rotation.NONE;
                    Color veilColor = null;

                    // si la tuile est déjà placée OU qu'on est en hover, juste on l'affiche normalement
                    if (isAlreadyPlaced) {
                        image = cachedImages.computeIfAbsent(placedTile.id(), ImageLoader::normalImageForTile);
                        rotation = placedTile.rotation();
                    }

                    if (isInFringe) {
                        group.onMouseClickedProperty().setValue(e -> {
                            if (e.getButton() == MouseButton.SECONDARY) {
                                rotationConsumer.accept(e.isAltDown() ? Rotation.RIGHT : Rotation.LEFT);
                            }
                        });
                        if (group.isHover()) {
                            image = cachedImages.computeIfAbsent(gameStateO.getValue().tileToPlace().id(), ImageLoader::normalImageForTile);
                            rotation = rotationO.getValue();
                        } else {
                            veilColor = ColorMap.fillColor(gameStateO.getValue().currentPlayer());
                        }
                    }

                    return new CellData(image, rotation, veilColor);
                }, fringeTilesO, group.hoverProperty(), rotationO);

                group.rotateProperty().bind(cellDataO.map(e -> e.tileRotation().degreesCW()));
                imageView.imageProperty().bind(cellDataO.map(CellData::tileImage));
/*
                placedTileO.addListener((_, oldPlacedTile, placedTile) -> {
                    if (oldPlacedTile != null || placedTile == null) return;
                    // todo ça va de faire comme ça ?
                    group.rotateProperty().bind(placedTileO.map(e -> e.rotation().degreesCW()));
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
                    placedTile.potentialOccupants()
                        .forEach(occupant -> {
                            Node occupantSvg = Icon.newFor(placedTile.placer(), occupant.kind());
                            occupantSvg.setId(STR."\{occupant.kind()}_\{occupant.zoneId()}");
                            occupantSvg.setOnMouseClicked((_) -> occupantConsumer.accept(occupant));
                            // visible only if occupants
                            group.getChildren().add(occupantSvg);
                        });
                });*/
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
