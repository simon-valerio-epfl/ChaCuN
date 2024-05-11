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

public final class WSClient implements WebSocket.Listener {

    private enum WSAction {
        GAMEJOIN_ACCEPT,
        GAMEJOIN_NEWCOMER,
        GAMEACTION,
        GAMEMSG,
        PING
    }

    private final String gameName;
    private final String username;
    private WebSocket ws;

    private boolean connected = false;

    private Consumer<String> onGameJoinAccept;
    private Consumer<String> onGamePlayerJoin;
    private Consumer<String> onPlayerAction;
    private BiConsumer<String, String> onGameChatMessage;

    public WSClient(String gameName, String username) {
        this.gameName = gameName;
        this.username = username;

        this.onGameJoinAccept = (data) -> {};
        this.onGamePlayerJoin = (data) -> {};
        this.onPlayerAction = (data) -> {};
        this.onGameChatMessage = (msgUsername, msgContent) -> {};
    }

    private static String connectURI(String gameName, String username) {
        return STR."wss://cs108-chacun-multiplayer.sys.polysource.ch/?gameName=\{gameName}&username=\{username}";
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
            case GAMEJOIN_ACCEPT -> onGameJoinAccept.accept(data);
            case GAMEJOIN_NEWCOMER -> onGamePlayerJoin.accept(data);
            case GAMEACTION -> onPlayerAction.accept(data);
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

    public void setOnGameJoinAccept(Consumer<String> onGameJoinAccept) {
        this.onGameJoinAccept = onGameJoinAccept;
    }

    public void setOnGamePlayerJoin(Consumer<String> onGamePlayerJoin) {
        this.onGamePlayerJoin = onGamePlayerJoin;
    }

    public void setOnPlayerAction(Consumer<String> onPlayerAction) {
        this.onPlayerAction = onPlayerAction;
    }

    public void setOnGameChatMessage(BiConsumer<String, String> onGameChatMessage) {
        this.onGameChatMessage = onGameChatMessage;
    }

    public void connect() {
        Preconditions.checkArgument(!connected);
        ws = HttpClient
                .newHttpClient()
                .newWebSocketBuilder()
                .buildAsync(URI.create(WSClient.connectURI(gameName, username)), this)
                .join();
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

    public void sendAction(String message) {
        ws.sendText(STR."GAMEACTION.\{message}", true);
    }

    public void sendMessage(String message) {
        ws.sendText(STR."GAMEMSG.\{java.net.URLEncoder.encode(message, StandardCharsets.UTF_8)}", true);
    }

    public void dispatchGameStarted() {
        ws.sendText("gameStarted", true);
    }

}
