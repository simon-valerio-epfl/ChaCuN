package ch.epfl.chacun.audio;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;

/**
 * This class is responsible for playing sounds in the game
 *
 * @author Valerio De Santis (373247)
 * @author Simon Lefort (371918)
 */
public final class SoundManager {

    private static MediaPlayer mediaPlayer;

    /**
     * Plays a sound (high priority, stops the current sound if any)
     *
     * @param sound the sound to play
     */
    public void play(Sound sound) {

        if (sound == Sound.SILENT) {
            mediaPlayer.stop();
            return;
        }

        String name = STR."/sounds/\{sound.toString().toLowerCase()}.mp3";
        URL url = getClass().getResource(name);
        assert url != null;
        Media media = new Media(url.toString());

        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setAutoPlay(true);
    }

    /**
     * The sounds of our game, ordered by priority in concurrent occasions
     */
    public enum Sound {
        PLACED_TILE,
        GAINED_POINTS,
        MENHIR_CLOSED,
        SILENT
    }


}