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
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * This class is responsible for creating the graphical representation of the board of a ChaCuN game,
 * handling the tiles that are placed, the animals on them (also the cancelled ones), and the
 * occupants of each tile. It also handles the highlighting of the tiles that are responsible
 * for a certain message when the player hovers over it. It also handles some graphical effects to
 * render the positions where a tile can be placed in the next turn and the mouse interactions.
 *
 * @author Valerio De Santis (373247)
 * @author Simon Lefort (371918)
 */
public final class BoardUI {
    /**
     * The center of the horizontal scroll bar
     */
    private static final double H_SCROLL_CENTER = .5;
    /**
     * The center of the vertical scroll bar
     */
    private static final double V_SCROLL_CENTER = .5;
    /**
     * The opacity of the veil that is applied to the tiles that are not highlighted
     */
    private static final double VEIl_OPACITY = .5;

    /**
     * This is a utility class and therefore is not instantiable
     */
    private BoardUI() {
    }

    /**
     * This method creates the graphical representation of the board of a ChaCuN game.
     * It handles the tiles that are placed, the animals on them (also the cancelled ones), and the
     * occupants of each tile. It also handles the highlighting of the tiles that are responsible
     * for a certain message when the player hovers over it. It also handles some graphical effects to
     * render the positions where a tile can be placed in the next turn and the mouse interactions.
     *
     * @param reach             the reach of the board (the distance from the center to the borders),
     *                          the board will be a square of size (2*range+1)²
     * @param gameStateO        the observable value of the current game state
     * @param rotationO         the observable value of the current rotation of the tile to be placed
     * @param occupantsO        the observable value of the set containing the occupants on the board to show
     * @param highlightedTilesO the observable value of the set containing the tiles to highlight
     * @param rotationConsumer  the consumer that will be called when the player rotates the tile to be placed (right click)
     * @param posConsumer       the consumer that will be called when the player places the tile (left click)
     * @param occupantConsumer  the consumer that will be called when the player clicks on an occupant
     * @return a graphical node representing the board of the game
     */
    public static Node create(
            int reach,
            ObservableValue<GameState> gameStateO,
            ObservableValue<Rotation> rotationO,
            ObservableValue<Set<Occupant>> occupantsO,
            ObservableValue<Set<Integer>> highlightedTilesO,

            Consumer<Rotation> rotationConsumer,
            Consumer<Pos> posConsumer,
            Consumer<Occupant> occupantConsumer
    ) {

        Preconditions.checkArgument(reach > 0);

        GridPane grid = new GridPane();
        grid.setId("board-grid");

        ObservableValue<Board> boardO = gameStateO.map(GameState::board);
        ObservableValue<Set<Animal>> cancelledAnimalsO = boardO.map(Board::cancelledAnimals);
        // the fringe only exists when the next action is to place a tile
        ObservableValue<Set<Pos>> fringeTilesO = gameStateO.map(
                gState -> gState.nextAction() == GameState.Action.PLACE_TILE
                        // we can not use boardO.getValue() here!
                        // because this map may be triggered before boardO gets updated!
                        // therefore we would be using the old board
                        ? gState.board().insertionPositions()
                        : Set.of()
        );

        for (int x = -reach; x <= reach; x++) {
            for (int y = -reach; y <= reach; y++) {
                //each cell of the grid contains a tile
                ImageView imageView = new ImageView();
                imageView.setFitWidth(ImageLoader.NORMAL_TILE_FIT_SIZE);
                imageView.setFitHeight(ImageLoader.NORMAL_TILE_FIT_SIZE);
                Blend blend = new Blend();
                blend.setMode(BlendMode.SRC_OVER);
                blend.setOpacity(VEIl_OPACITY);
                blend.setBottomInput(null);

                Group group = new Group(imageView);
                group.setEffect(blend);
                Pos pos = new Pos(x, y);

                // cell data does not show anything to the screen, it takes and computes some values from the tile,
                // which can thus be observed in the rest of the program

                // we create the observables that will be used to calculate the cell data
                ObservableValue<PlacedTile> placedTileO = boardO.map(board -> board.tileAt(pos));
                ObservableValue<Boolean> isInFringeO = fringeTilesO.map(fringe -> fringe.contains(pos));
                // if there are some tiles to highlight, we darken the others
                ObservableValue<Boolean> darkVeilEnabledO = highlightedTilesO.map(hTiles -> {
                    PlacedTile currentPlacedTile = placedTileO.getValue();
                    return !hTiles.isEmpty() && !hTiles.contains(currentPlacedTile.id());
                });

                // here we handle the mouse interactions
                group.setOnMouseClicked((mouseEvent) -> {
                    // to handle the case of a player wanting to scroll on the board without placing a tile
                    if (mouseEvent.isStillSincePress() && isInFringeO.getValue()) {
                        switch (mouseEvent.getButton()) {
                            case SECONDARY ->
                                // if the player right-clicks on a tile pressing the ALT key, the tile is rotated right,
                                // otherwise it is rotated left
                                    rotationConsumer.accept(mouseEvent.isAltDown() ? Rotation.RIGHT : Rotation.LEFT);
                            case PRIMARY ->
                                // if the player left-clicks on a tile, the tile is placed
                                    posConsumer.accept(pos);
                        }
                    }
                });

                // triggered when the fringe changes or when the mouse passes over the tile,
                // the player rotates it, the tile's highlighting state changes or when the tile is placed
                ObservableValue<CellData> cellDataO = Bindings.createObjectBinding(() -> {

                    PlacedTile placedTile = placedTileO.getValue();
                    boolean isAlreadyPlaced = placedTile != null;

                    // if the tile is already placed
                    // just display it normally, eventually with a dark veil
                    if (isAlreadyPlaced) return new CellData(placedTile,
                            darkVeilEnabledO.getValue() ? Color.BLACK : Color.TRANSPARENT);
                    // else, if the tile is not placed yet, nor in the fringe, display it as an empty image
                    if (!isInFringeO.getValue()) return new CellData(Color.TRANSPARENT);

                    GameState currentGameState = gameStateO.getValue();
                    PlayerColor currentPlayer = currentGameState.currentPlayer();

                    // if the mouse is currently on this tile (in the fringe) we display it normally
                    // if it can be placed there with its current position, and with a white veil otherwise
                    if (group.isHover()) {
                        PlacedTile willBePlacedTile = new PlacedTile(
                                currentGameState.tileToPlace(), currentPlayer, rotationO.getValue(), pos
                        );
                        return new CellData(willBePlacedTile,
                                boardO.getValue().canAddTile(willBePlacedTile) ? Color.TRANSPARENT : Color.WHITE
                        );
                    }
                    // finally, if the tile is in the fringe but the mouse is not on it,
                    // we display it with a veil of the current player's color

                    // this can not be null because if we are at that point, the game state action is OCCUPY_TILE
                    assert currentPlayer != null;

                    return new CellData(ColorMap.fillColor(currentPlayer));
                    // these arguments are the sensibility of the code,
                    // every time one of them changes, the code is re-executed
                }, isInFringeO, group.hoverProperty(), rotationO, darkVeilEnabledO, placedTileO);

                // we bind the graphical properties of the group to the cell data's values
                group.rotateProperty().bind(cellDataO.map(cellData -> cellData.tileRotation().degreesCW()));
                imageView.imageProperty().bind(cellDataO.map(CellData::tileImage));
                blend.topInputProperty().bind(cellDataO.map(CellData::blendTopInput));

                // we add range and position in order to translate our tile from the left corner to the center
                grid.add(group, x + reach, y + reach);

                // when a tile is placed, we add the animals and the occupants on it
                placedTileO.addListener((_, oldPlacedTile, placedTile) -> {
                    // if the tile is already placed or is not yet, we do not have to add the animals and the occupants
                    if (oldPlacedTile != null || placedTile == null) return;

                    double negatedTileRotation = placedTile.rotation().negated().degreesCW();

                    // handle "jeton d'annulation", a marker that signals that an animal is cancelled
                    List<Node> cancelledAnimalsNodes = placedTile.meadowZones().stream()
                            .flatMap(meadow -> meadow.animals().stream())
                            .map(animal -> getCancelledAnimalNode(animal, cancelledAnimalsO, negatedTileRotation))
                            .toList();
                    group.getChildren().addAll(cancelledAnimalsNodes);
                    // here we handle the graphical representation of the occupants
                    List<Node> potentialOccupantsNodes = placedTile.potentialOccupants()
                            .stream()
                            .map(occupant -> getOccupantNode(
                                    placedTile.placer(), occupant,
                                    occupantsO, occupantConsumer, negatedTileRotation
                            ))
                            .toList();

                    group.getChildren().addAll(potentialOccupantsNodes);
                });
            }
        }

        ScrollPane scrollPane = new ScrollPane(grid);

        scrollPane.setId("board-scroll-pane");
        scrollPane.getStylesheets().add("/board.css");
        scrollPane.setHvalue(H_SCROLL_CENTER);
        scrollPane.setVvalue(V_SCROLL_CENTER);

        return scrollPane;
    }

    private static Node getCancelledAnimalNode(
            Animal animal, ObservableValue<Set<Animal>> cancelledAnimalsO,
            double negatedTileRotation
    ) {
        ImageView imageView = new ImageView();
        imageView.visibleProperty().bind(cancelledAnimalsO.map(
                cancelledAnimals -> cancelledAnimals.contains(animal)
        ));
        imageView.setId(STR."marker_\{animal.id()}");
        imageView.getStyleClass().add("marker");
        imageView.setFitHeight(ImageLoader.MARKER_FIT_SIZE);
        imageView.setFitWidth(ImageLoader.MARKER_FIT_SIZE);
        imageView.setRotate(negatedTileRotation);
        return imageView;
    }

    private static Node getOccupantNode(
            PlayerColor playerColor, Occupant occupant,
            ObservableValue<Set<Occupant>> occupantsO,
            Consumer<Occupant> occupantConsumer,
            double negatedTileRotation
    ) {
        Occupant.Kind occupantKind = occupant.kind();
        Node occupantSvg = Icon.newFor(playerColor, occupantKind);
        occupantSvg.setId(STR."\{occupantKind.toString().toLowerCase()}_\{occupant.zoneId()}");
        occupantSvg.setOnMouseClicked((event) -> {
            event.consume();
            if (event.isStillSincePress()) occupantConsumer.accept(occupant);
        });
        occupantSvg.visibleProperty().bind(occupantsO.map(occupants -> occupants.contains(occupant)));
        occupantSvg.setRotate(negatedTileRotation);
        return occupantSvg;
    }

    /**
     * This class is used to store the data of a cell of the board, which is the image of the tile,
     * its rotation and the color of the veil that is applied to the tile.
     *
     * @param tileImage    the image of the tile
     * @param tileRotation the rotation of the tile
     * @param veilColor    the color of the veil that has to be applied to the tile
     */
    private record CellData(Image tileImage, Rotation tileRotation, Color veilColor) {
        /**
         * This map is used to save in a cache the images that have already been loaded once,
         * in order to avoid loading them again and again when they are needed to be shown.
         */
        private static final Map<Integer, Image> cachedImages = new HashMap<>();

        /**
         * An empty image, graphically represented by a grey square
         */
        public static final Image EMPTY_IMAGE;

        static {
            WritableImage writableImage = new WritableImage(1, 1);
            writableImage.getPixelWriter().setColor(0, 0, Color.gray(0.98));
            EMPTY_IMAGE = writableImage;
        }

        /**
         * This constructor creates a CellData object from a placed tile and a veil color,
         * computing its rotation and image from the placed tile (the image will have normal size).
         *
         * @param placedTile the tile from which to take the image and the rotation
         * @param veilColor  the color of the veil that has to be applied to the tile
         */
        public CellData(PlacedTile placedTile, Color veilColor) {
            this(
                    cachedImages.computeIfAbsent(placedTile.id(), ImageLoader::normalImageForTile),
                    placedTile.rotation(), veilColor
            );
        }

        /**
         * This constructor creates a CellData object from a veil color,
         * using the empty image and no rotation, representing an empty cell.
         *
         * @param veilColor the color of the veil that has to be applied to the tile
         */
        public CellData(Color veilColor) {
            this(EMPTY_IMAGE, Rotation.NONE, veilColor);
        }

        /**
         * Returns the color input that has to be applied to the tile to show the veil, or null
         * if there is no veil to apply.
         *
         * @return the color input that has to be applied to the tile to show the veil, or null
         * if there is no veil to apply
         */
        public ColorInput blendTopInput() {
            if (this.veilColor == null) return null;
            return new ColorInput(
                    0, 0, ImageLoader.NORMAL_TILE_FIT_SIZE, ImageLoader.NORMAL_TILE_FIT_SIZE, this.veilColor
            );
        }
    }
}
