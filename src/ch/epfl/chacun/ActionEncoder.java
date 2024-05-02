package ch.epfl.chacun;

import java.util.Comparator;
import java.util.List;

public final class ActionEncoder {

    private static class InvalidActionException extends Exception {
        public InvalidActionException() {}
    }

    private static final int WITH_NO_OCCUPANT = 0b11111;
    // format: K-O-O-O
    private static final int WITH_NEW_OCCUPANT_ACTION_LENGTH = 1;
    private static final int WITH_NEW_OCCUPANT_KIND_SHIFT = 4;
    private static final int WITH_NEW_OCCUPANT_ZONE_MASK = 0b111;
    // format: P-P-P-P-P-P-P-P-R-R
    private static final int WITH_PLACED_TILE_ACTION_LENGTH = 2;
    private static final int WITH_PLACED_TILE_IDX_SHIFT = 2;
    private static final int WITH_PLACED_TILE_ROTATION_MASK = 0b11;
    // format: O-O-O-O
    private static final int WITH_OCCUPANT_REMOVED_ACTION_LENGTH = 1;

    private ActionEncoder() {}

    private static List<Pos> fringeIndexes(GameState gameState) {
        Board board = gameState.board();
        return board.insertionPositions().stream()
            .sorted(Comparator.comparing(Pos::x).thenComparing(Pos::y))
            .toList();
    }

    public static StateAction withPlacedTile(GameState gameState, PlacedTile tile){
        List<Pos> fringeIndexes = fringeIndexes(gameState);
        int indexToEncode = fringeIndexes.indexOf(tile.pos()); // a number between 0 and 189
        int rotationToEncode = tile.rotation().ordinal(); // a number between 0 and 3
        int toEncode = indexToEncode << WITH_PLACED_TILE_IDX_SHIFT | rotationToEncode;
        // our action will have 7 bits at most
        return new StateAction(gameState.withPlacedTile(tile), Base32.encodeBits10(toEncode));
    }

    public static StateAction withNewOccupant(GameState gameState, Occupant occupant) {
        if (occupant == null) return new StateAction(gameState.withNewOccupant(null), Base32.encodeBits5(WITH_NO_OCCUPANT));
        int kindToEncode = occupant.kind().ordinal(); // a number between 0 and 1
        int zoneToEncode = Zone.localId(occupant.zoneId());
        int toEncode = kindToEncode << WITH_NEW_OCCUPANT_KIND_SHIFT | zoneToEncode;
        return new StateAction(gameState.withNewOccupant(occupant), Base32.encodeBits5(toEncode));
    }

    public static StateAction withOccupantRemoved(GameState gameState, Occupant occupant) {
        // todo doit-on vérifier ici que l'occupant appartient bien au gameState current player ?
        // comme dans le decodeAndApply?
        if (occupant == null) return new StateAction(gameState.withOccupantRemoved(null), Base32.encodeBits5(WITH_NO_OCCUPANT));
        List<Occupant> occupants = gameState.board().occupants().stream()
            .sorted(Comparator.comparingInt(Occupant::zoneId)).toList();
        int indexToEncode = occupants.indexOf(occupant); // a number between 0 and 24
        return new StateAction(gameState.withOccupantRemoved(occupant), Base32.encodeBits5(indexToEncode));
    }

    private static StateAction decodeAndApplyWithException(GameState gameState, String action)
            throws InvalidActionException {
        if (!Base32.isValid(action)) throw new InvalidActionException();
        return switch (gameState.nextAction()) {
            case PLACE_TILE -> {
                if (action.length() != WITH_PLACED_TILE_ACTION_LENGTH) throw new InvalidActionException();
                int decoded = Base32.decode(action);
                int tileInFringeIdx = decoded >> WITH_PLACED_TILE_IDX_SHIFT;
                int rotationIdx = decoded & WITH_PLACED_TILE_ROTATION_MASK;
                List<Pos> fringeIndexes = fringeIndexes(gameState);
                if (fringeIndexes.size() <= tileInFringeIdx) throw new InvalidActionException();
                Pos pos = fringeIndexes.get(tileInFringeIdx);
                System.out.println(tileInFringeIdx);
                System.out.println(pos);
                System.out.println(Rotation.ALL.get(rotationIdx));
                Tile tile = gameState.tileToPlace();
                PlacedTile placedTile = new PlacedTile(
                    tile, gameState.currentPlayer(),
                    Rotation.ALL.get(rotationIdx), pos
                );
                yield new StateAction(gameState.withPlacedTile(placedTile), action);
            }
            case OCCUPY_TILE -> {
                if (action.length() != WITH_NEW_OCCUPANT_ACTION_LENGTH) throw new InvalidActionException();
                int decoded = Base32.decode(action);
                if (decoded == WITH_NO_OCCUPANT) yield new StateAction(gameState.withNewOccupant(null), action);
                int kindIdx = decoded >> WITH_NEW_OCCUPANT_KIND_SHIFT;
                int localId = decoded & WITH_NEW_OCCUPANT_ZONE_MASK;
                Occupant.Kind kind = Occupant.Kind.ALL.get(kindIdx);
                Occupant occupant = gameState.lastTilePotentialOccupants().stream()
                    .filter(occ -> occ.kind() == kind && Zone.localId(occ.zoneId()) == localId)
                    .findFirst()
                    .orElseThrow(InvalidActionException::new);
                yield new StateAction(gameState.withNewOccupant(occupant), action);
            }
            case RETAKE_PAWN -> {
                if (action.length() != WITH_OCCUPANT_REMOVED_ACTION_LENGTH) throw new InvalidActionException();
                int decoded = Base32.decode(action);
                if (decoded == WITH_NO_OCCUPANT) yield new StateAction(gameState.withOccupantRemoved(null), action);
                List<Occupant> occupants = gameState.board().occupants()
                    .stream()
                    .sorted(Comparator.comparingInt(Occupant::zoneId))
                    .toList();
                if (occupants.size() <= decoded) throw new InvalidActionException();
                Occupant occupant = occupants.get(decoded);
                PlayerColor currentPlayer = gameState.currentPlayer();
                PlayerColor occupantPlacer = gameState.board().tileWithId(Zone.tileId(occupant.zoneId())).placer();
                if (currentPlayer != occupantPlacer) throw new InvalidActionException();
                yield new StateAction(gameState.withOccupantRemoved(occupant), action);
            }
            // todo on renvoie null ?
            default -> throw new IllegalStateException();
        };
    }

    public static StateAction decodeAndApply(GameState gameState, String action) {
        try {
            return decodeAndApplyWithException(gameState, action);
        } catch (InvalidActionException e) {
            return null;
        }
    }

    public record StateAction(GameState gameState, String action) {}
}
