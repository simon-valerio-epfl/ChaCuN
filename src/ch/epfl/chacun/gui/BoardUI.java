package ch.epfl.chacun.gui;

import ch.epfl.chacun.*;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorInput;
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
    /**
     * This is a utility class and therefore is not instantiable
     */
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

        GridPane grid = new GridPane();
        grid.setId("board-grid");

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
                imageView.setFitWidth(ImageLoader.NORMAL_TILE_FIT_SIZE);
                imageView.setFitHeight(ImageLoader.NORMAL_TILE_FIT_SIZE);
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
                    Set<Integer> highlitedTiles = highlightedTilesO.getValue();
                    boolean isNotHighlighted = !highlitedTiles.isEmpty() && (placedTile == null
                            || !highlitedTiles.contains(placedTileO.getValue().id()));

                    Image image = ImageLoader.EMPTY_IMAGE;
                    Rotation rotation = Rotation.NONE;
                    Color veilColor = Color.TRANSPARENT;

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
                            image = cachedImages.computeIfAbsent(
                                gameStateO.getValue().tileToPlace().id(),
                                ImageLoader::normalImageForTile
                            );
                            rotation = rotationO.getValue();
                        } else {
                            veilColor = ColorMap.fillColor(gameStateO.getValue().currentPlayer());
                        }
                    }
                    if (isNotHighlighted) veilColor = Color.BLACK;

                    return new CellData(image, rotation, veilColor);
                }, fringeTilesO, group.hoverProperty(), rotationO, highlightedTilesO);

                group.rotateProperty().bind(cellDataO.map(cellData -> cellData.tileRotation().degreesCW()));
                imageView.imageProperty().bind(cellDataO.map(CellData::tileImage));
                group.effectProperty().bind(cellDataO.map(cellData -> {
                    Blend blend = new Blend();
                    blend.setMode(BlendMode.SRC_OVER);
                    blend.setTopInput(cellData.veilColor == null ? null : new ColorInput(
                        0, 0,
                        ImageLoader.NORMAL_TILE_FIT_SIZE, ImageLoader.NORMAL_TILE_FIT_SIZE,
                        cellData.veilColor
                    ));
                    blend.setOpacity(0.5);
                    blend.setBottomInput(null);
                    return blend;
                }));

                grid.add(group, x + range, y + range);

                placedTileO.addListener((_, oldPlacedTile, placedTile) -> {
                    if (oldPlacedTile != null || placedTile == null) return;
                    // add occupants
                    placedTile.meadowZones().stream()
                        .flatMap(z -> z.animals().stream())
                        .forEach(animal -> {
                            ImageView cancelledAnimalView = new ImageView();
                            //cancelledAnimalView.visibleProperty().bind(cancelledAnimalsO.map(animals -> !animals.contains(animal)));
                            cancelledAnimalView.setId(STR."marker_\{animal.id()}");
                            cancelledAnimalView.getStyleClass().add("marker");
                            cancelledAnimalView.setFitHeight(ImageLoader.MARKER_FIT_SIZE);
                            cancelledAnimalView.setFitWidth(ImageLoader.MARKER_FIT_SIZE);
                            group.getChildren().add(cancelledAnimalView);
                        });
                    // todo, this should be a getValue right?
                    placedTile.potentialOccupants()
                        .forEach(occupant -> {
                            Node occupantSvg = Icon.newFor(placedTile.placer(), occupant.kind());
                            occupantSvg.setId(STR."\{occupant.kind().toString().toLowerCase()}_\{occupant.zoneId()}");
                            occupantSvg.setOnMouseClicked((_) -> occupantConsumer.accept(occupant));
                            // visible only if there are occupants
                            occupantSvg.visibleProperty().bind(occupantO.map(occupants -> occupants.contains(occupant)));
                            // create an image view from the svg
                            group.getChildren().add(occupantSvg);
                        });
                });
            }
        }

        scrollPane.setContent(grid);
        return scrollPane;
    }

    private record CellData (Image tileImage, Rotation tileRotation, Color veilColor) {}

    // todo comment créer une méthode privée
    private SVGPath getEntitySVG () {
        return null;
    }

}
