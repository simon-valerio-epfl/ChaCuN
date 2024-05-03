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
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public final class BoardUI {

    private static final Map<Integer, Image> cachedImages = new HashMap<>();

    /**
     * This is a utility class and therefore is not instantiable
     */
    private BoardUI() {}

    public static Node create(
            int range,
            ObservableValue<GameState> gameStateO,
            ObservableValue<Rotation> rotationO,
            ObservableValue<Set<Occupant>> occupantsO,
            ObservableValue<Set<Integer>> highlightedTilesO,

            Consumer<Rotation> rotationConsumer,
            Consumer<Pos> posConsumer,
            Consumer<Occupant> occupantConsumer
    ) {

        Preconditions.checkArgument(range > 0);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setId("board-scroll-pane");
        scrollPane.getStylesheets().add("board.css");

        GridPane grid = new GridPane();
        grid.setId("board-grid");

        ObservableValue<Board> boardO = gameStateO.map(GameState::board);
        ObservableValue<Set<Animal>> cancelledAnimalsO = boardO.map(Board::cancelledAnimals);
        ObservableValue<Set<Pos>> fringeTilesO = gameStateO.map(
            state -> state.nextAction() == GameState.Action.PLACE_TILE ? boardO.getValue().insertionPositions() : Set.of()
        );

        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                ImageView imageView = new ImageView();
                imageView.setFitWidth(ImageLoader.NORMAL_TILE_FIT_SIZE);
                imageView.setFitHeight(ImageLoader.NORMAL_TILE_FIT_SIZE);
                Group group = new Group(imageView);
                Pos pos = new Pos(x, y);

                group.setOnMouseClicked(e -> {
                    switch (e.getButton()) {
                        case SECONDARY -> rotationConsumer.accept(e.isAltDown() ? Rotation.RIGHT : Rotation.LEFT);
                        case PRIMARY -> posConsumer.accept(pos);
                    }
                });

                // cell data does not show anything to the screen, it calculates and takes some values from the tile,
                // which can thus be observed in the rest of the program
                ObservableValue<PlacedTile> placedTileO = boardO.map(b -> b.tileAt(pos));
                ObservableValue<Boolean> isInFringeO = fringeTilesO.map(fringe -> fringe.contains(pos));
                ObservableValue<Boolean> darkVeilEnabledO = highlightedTilesO.map(h ->
                    !h.isEmpty() && (placedTileO.getValue() == null || !h.contains(placedTileO.getValue().id()))
                );

                // triggered when the fringe changes or when the mouse is hover the group
                ObservableValue<CellData> cellDataO = Bindings.createObjectBinding(() -> {

                    PlacedTile placedTile = placedTileO.getValue();
                    boolean isAlreadyPlaced = placedTile != null;

                    // if the tile is already placed OR you're hovering, just display it normally
                    if (isAlreadyPlaced) return new CellData(placedTile,
                            darkVeilEnabledO.getValue() ? Color.BLACK : Color.TRANSPARENT);
                    if (!isInFringeO.getValue()) return new CellData(Color.TRANSPARENT);

                    PlayerColor currentPlayer = gameStateO.getValue().currentPlayer();

                    // if the mouse is currently on this tile
                    if (group.isHover()) {
                        PlacedTile willBePlacedTile = new PlacedTile(
                            gameStateO.getValue().tileToPlace(), currentPlayer, rotationO.getValue(), pos
                        );
                        return new CellData(willBePlacedTile,
                            boardO.getValue().canAddTile(willBePlacedTile) ? Color.TRANSPARENT : Color.WHITE
                        );
                    }

                    return new CellData(ColorMap.fillColor(currentPlayer));
                    // these arguments are the sensibility of the code,
                    // every time one of them changes, the code is re-executed
                }, isInFringeO, group.hoverProperty(), rotationO, darkVeilEnabledO, placedTileO);

                group.rotateProperty().bind(cellDataO.map(cellData -> cellData.tileRotation().degreesCW()));
                imageView.imageProperty().bind(cellDataO.map(CellData::tileImage));
                group.effectProperty().bind(cellDataO.map(CellData::getBlendEffect));

                // we add range in order to translate our tile to the left corner
                grid.add(group, x + range, y + range);

                placedTileO.addListener((_, oldPlacedTile, placedTile) -> {
                    if (oldPlacedTile != null || placedTile == null) return;
                    // handle "jeton d'annulation"
                    placedTile.meadowZones().stream()
                        .flatMap(z -> z.animals().stream())
                        .forEach(animal -> {
                            ImageView cancelledAnimalView = new ImageView();
                            cancelledAnimalView.visibleProperty().bind(cancelledAnimalsO.map(animals -> animals.contains(animal)));
                            cancelledAnimalView.setId(STR."marker_\{animal.id()}");
                            cancelledAnimalView.getStyleClass().add("marker");
                            cancelledAnimalView.setFitHeight(ImageLoader.MARKER_FIT_SIZE);
                            cancelledAnimalView.setFitWidth(ImageLoader.MARKER_FIT_SIZE);
                            group.getChildren().add(cancelledAnimalView);
                        });
                    // handle occupants
                    placedTile.potentialOccupants()
                        .forEach(occupant -> {
                            Node occupantSvg = Icon.newFor(placedTile.placer(), occupant.kind());
                            occupantSvg.setId(STR."\{occupant.kind().toString().toLowerCase()}_\{occupant.zoneId()}");
                            occupantSvg.setOnMouseClicked((_) -> occupantConsumer.accept(occupant));
                            occupantSvg.visibleProperty().bind(occupantsO.map(occupants -> occupants.contains(occupant)));
                            group.getChildren().add(occupantSvg);
                        });
                });
            }
        }

        scrollPane.setContent(grid);
        return scrollPane;
    }

    private record CellData (Image tileImage, Rotation tileRotation, Color veilColor) {
        public CellData(PlacedTile placedTile, Color veilColor) {
            this(
                cachedImages.computeIfAbsent(placedTile.id(), ImageLoader::normalImageForTile),
                placedTile.rotation(), veilColor
            );
        }
        public CellData(Color veilColor) {
            this(ImageLoader.EMPTY_IMAGE, Rotation.NONE, veilColor);
        }
        public Blend getBlendEffect() {
            Blend blend = new Blend();
            blend.setMode(BlendMode.SRC_OVER);
            blend.setTopInput(this.veilColor == null ? null : new ColorInput(
                    0, 0, ImageLoader.NORMAL_TILE_FIT_SIZE, ImageLoader.NORMAL_TILE_FIT_SIZE, this.veilColor
            ));
            blend.setOpacity(0.5);
            blend.setBottomInput(null);
            return blend;
        }
    }
}
