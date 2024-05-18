package ch.epfl.chacun.net;

import ch.epfl.chacun.Preconditions;
import javafx.application.Platform;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Represents a WebSocket client for the multiplayer game
 *
 * @author Valerio De Santis (373247)
 * @author Simon Lefort (371918)
 */
public final class WSClient implements WebSocket.Listener {

    private enum WSAction {
        GAMEJOIN_ACCEPT,
        GAMEJOIN_NEWCOMER,
        GAMELEAVE,
        GAMEACTION_ACCEPT,
        GAMEACTION_DENY,
        GAMEMSG,
        PING
    }

    private static final String SERVER_CONNECT_ENDPOINT = "wss://cs108-chacun-multiplayer-v2.sys.polysource.ch/";

    private final String gameName;
    private final String username;

    private WebSocket ws;
    private boolean connected = false;

    private Consumer<String> onGamePlayersUpdate;
    private Consumer<String> onPlayerAction;
    private Consumer<String> onLocalPlayerActionReject;
    private BiConsumer<String, String> onGameChatMessage;

    /**
     * Creates a new WebSocket client for the given game and player
     *
     * @param gameName the name of the game
     * @param username the username of the player
     */
    public WSClient(String gameName, String username) {
        this.gameName = gameName;
        this.username = username;

        this.onGamePlayersUpdate = (data) -> {};
        this.onPlayerAction = (data) -> {};
        this.onLocalPlayerActionReject = (data) -> {};
        this.onGameChatMessage = (msgUsername, msgContent) -> {};
    }

    private void acknowledgePing() {
        Preconditions.checkArgument(connected);
        ws.sendText("PONG", true);
    }

    private void handleMessage(String message) {
        System.out.println(message);
        String[] messageParts = message.split("\\.");
        String action = messageParts[0];
        String data = messageParts[1];
        WSAction wsAction = WSAction.valueOf(action);
        switch (wsAction) {
            case GAMEJOIN_ACCEPT, GAMEJOIN_NEWCOMER, GAMELEAVE -> this.onGamePlayersUpdate.accept(data);
            case GAMEACTION_ACCEPT -> onPlayerAction.accept(data);
            case GAMEACTION_DENY -> onLocalPlayerActionReject.accept(data);
            case GAMEMSG -> {
                // data = {username=content}
                // data is encoded with encodeURI
                String content = java.net.URLDecoder.decode(data, StandardCharsets.UTF_8);
                String username = content.split("=")[0];
                String chatMessage = java.net.URLDecoder.decode(content.split("=")[1], StandardCharsets.UTF_8);
                onGameChatMessage.accept(username, chatMessage);
            }
            case PING -> acknowledgePing();
        }
    }

    /**
     * Sets the consumer to be called when the game players list is updated
     *
     * @param onGamePlayersUpdate the consumer to be called
     */
    public void setOnGamePlayersUpdate(Consumer<String> onGamePlayersUpdate) {
        this.onGamePlayersUpdate = onGamePlayersUpdate;
    }

    /**
     * Sets the consumer to be called when a player action is received
     *
     * @param onPlayerAction the consumer to be called
     */
    public void setOnPlayerAction(Consumer<String> onPlayerAction) {
        this.onPlayerAction = onPlayerAction;
    }

    /**
     * Sets the consumer to be called when a new chat message is received
     *
     * @param onGameChatMessage the consumer to be called
     */
    public void setOnGameChatMessage(BiConsumer<String, String> onGameChatMessage) {
        this.onGameChatMessage = onGameChatMessage;
    }

    /**
     * Sets the consumer to be called when the local player action is rejected
     *
     * @param onLocalPlayerActionReject the consumer to be called
     */
    public void setOnLocalPlayerActionReject(Consumer<String> onLocalPlayerActionReject) {
        this.onLocalPlayerActionReject = onLocalPlayerActionReject;
    }

    /**
     * Connects to the WebSocket server
     */
    public void connect() {
        Preconditions.checkArgument(!connected);
        ws = HttpClient
                .newHttpClient()
                .newWebSocketBuilder()
                .buildAsync(URI.create(SERVER_CONNECT_ENDPOINT), this)
                .join();
    }

    /**
     * Joins the game
     */
    public void joinGame() {
        ws.sendText(STR."GAMEJOIN.\{gameName},\{username}", true);
    }

    /**
     * Sends an action to the game
     * @param message the base32 encoded action to send
     */
    public void sendAction(String message) {
        ws.sendText(STR."GAMEACTION.\{message}", true);
    }

    /**
     * Sends a chat message to the game
     * @param message the message to send
     */
    public void sendChatMessage(String message) {
        ws.sendText(STR."GAMEMSG.\{java.net.URLEncoder.encode(message, StandardCharsets.UTF_8)}", true);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        Platform.runLater(() -> handleMessage(data.toString()));
        return WebSocket.Listener.super.onText(webSocket, data, last);
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        connected = true;
        WebSocket.Listener.super.onOpen(webSocket);
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        connected = false;
        throw new IllegalStateException("Unexpected WS connection closed");
    }

}
