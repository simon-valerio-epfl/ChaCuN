package ch.epfl.chacun;

import java.util.List;

/**
 * List all possible colors.
 *
 * Valerio De Santis (373247)
 * Simon Lefort (371918)
 */
public enum PlayerColor {
    RED, BLUE, GREEN, YELLOW, PURPLE;

    public static final List<PlayerColor> ALL = List.of(PlayerColor.values());
}
