package ch.epfl.chacun.net;

public final class WSPlayer {

    private final boolean isReady;
    private final String username;

    public WSPlayer (String username, boolean isReady) {
        this.isReady = isReady;
        this.username = username;
    }

    public boolean isReady() {
        return isReady;
    }

    public String getUsername() {
        return username;
    }
}
