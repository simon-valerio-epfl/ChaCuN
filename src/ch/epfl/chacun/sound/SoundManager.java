package ch.epfl.chacun.sound;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;

public final class SoundManager {

    private static MediaPlayer mediaPlayer;

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
