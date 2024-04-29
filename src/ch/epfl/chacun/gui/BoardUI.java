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
                // cell data non mostra nulla allo schermo, calcola e prende alcuni valori della tile,
                // che possono così essere osservati nel resto del programma
                ObservableValue<CellData> cellDataO = Bindings.createObjectBinding(() -> {
                    // trigger quand :
                    // - la souris passe sur la tuile
                    // - la frange change

                    boolean isInFringe = fringeTilesO.getValue().contains(pos);
                    PlacedTile placedTile = placedTileO.getValue();
                    boolean isAlreadyPlaced = placedTile != null;
                    Set<Integer> highlightedTiles = highlightedTilesO.getValue();
                    boolean isNotHighlighted = !highlightedTiles.isEmpty() && (placedTile == null
                            || !highlightedTiles.contains(placedTileO.getValue().id()));

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
                            if (e.getButton() == MouseButton.PRIMARY) posConsumer.accept(pos);
                        });
                        PlayerColor currentPlayer = gameStateO.getValue().currentPlayer();
                        // if the mouse is currently on this tile
                        if (group.isHover()) {
                            Rotation willBeRotated = rotationO.getValue();
                            PlacedTile willBePlacedTile = new PlacedTile(
                                gameStateO.getValue().tileToPlace(),
                                currentPlayer, willBeRotated, pos
                            );
                            image = cachedImages.computeIfAbsent(willBePlacedTile.id(), ImageLoader::normalImageForTile);
                            rotation = rotationO.getValue();
                            if (!boardO.getValue().canAddTile(willBePlacedTile)) veilColor = Color.WHITE;
                        } else veilColor = ColorMap.fillColor(currentPlayer);
                    }
                    if (isNotHighlighted) veilColor = Color.BLACK;

                    return new CellData(image, rotation, veilColor);
                    // questi argomenti sono la sensibility del codice,
                    // ogni volta che uno di loro cambia, il codice viene reeseguito
                }, fringeTilesO, group.hoverProperty(), rotationO, highlightedTilesO);

                group.rotateProperty().bind(cellDataO.map(cellData -> cellData.tileRotation().degreesCW()));
                imageView.imageProperty().bind(cellDataO.map(CellData::tileImage));
                group.effectProperty().bind(cellDataO.map(BoardUI::getBlendVeilEffect));

                // we add range in order to translate our tile to the left corner
                grid.add(group, x + range, y + range);

                placedTileO.addListener((_, oldPlacedTile, placedTile) -> {
                    if (oldPlacedTile != null || placedTile == null) return;
                    // handle "jeton d'annulation"
                    List<Node> cancelledAnimalsNodes = placedTile.meadowZones().stream()
                        .flatMap(z -> z.animals().stream())
                        .map(animal -> getCancelledAnimalNode(animal, cancelledAnimalsO))
                        .toList();
                    group.getChildren().addAll(cancelledAnimalsNodes);
                    // handle occupants
                    List<Node> occupantsNodes = placedTile.potentialOccupants()
                        .stream()
                        .map(occupant -> getOccupantNode(placedTile.placer(), occupant, occupantsO, occupantConsumer))
                        .toList();
                    group.getChildren().addAll(occupantsNodes);
                });
            }
        }

        scrollPane.setContent(grid);
        return scrollPane;
    }

    private record CellData (Image tileImage, Rotation tileRotation, Color veilColor) {}

    private static Node getCancelledAnimalNode(Animal animal, ObservableValue<Set<Animal>> cancelledAnimalsO) {
        ImageView cancelledAnimalView = new ImageView();
        cancelledAnimalView.visibleProperty().bind(cancelledAnimalsO.map(animals -> !animals.contains(animal)));
        cancelledAnimalView.setId(STR."marker_\{animal.id()}");
        cancelledAnimalView.getStyleClass().add("marker");
        cancelledAnimalView.setFitHeight(ImageLoader.MARKER_FIT_SIZE);
        cancelledAnimalView.setFitWidth(ImageLoader.MARKER_FIT_SIZE);
        return cancelledAnimalView;
    }

    private static Node getOccupantNode(
            PlayerColor playerColor, Occupant occupant,
            ObservableValue<Set<Occupant>> occupantsO, Consumer<Occupant> occupantConsumer
    ) {
        Node occupantSvg = Icon.newFor(playerColor, occupant.kind());
        occupantSvg.setId(STR."\{occupant.kind().toString().toLowerCase()}_\{occupant.zoneId()}");
        occupantSvg.setOnMouseClicked((_) -> occupantConsumer.accept(occupant));
        occupantSvg.visibleProperty().bind(occupantsO.map(occupants -> occupants.contains(occupant)));
        return occupantSvg;
    }

    private static Blend getBlendVeilEffect(CellData cellData) {
        Blend blend = new Blend();
        blend.setMode(BlendMode.SRC_OVER);
        blend.setTopInput(cellData.veilColor == null ? null : new ColorInput(
            0, 0, ImageLoader.NORMAL_TILE_FIT_SIZE, ImageLoader.NORMAL_TILE_FIT_SIZE, cellData.veilColor
        ));
        blend.setOpacity(0.5);
        blend.setBottomInput(null);
        return blend;
    }
}
