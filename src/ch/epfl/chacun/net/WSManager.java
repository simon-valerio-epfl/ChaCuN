package ch.epfl.chacun.net;

import javafx.application.Platform;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class WSManager {

    private final String gameName;
    private final String username;
    private WebSocket ws;
    private boolean gameStarted = false;

    private BiConsumer<String, Boolean> onPlayerJoined;

    public WSManager(String gameName, String username) {
        this.gameName = gameName;
        this.username = username;
    }

    public void connect(
            Consumer<String> onActionReceived,
            BiConsumer<String, Boolean> onPlayerJoined
    ) {
        this.onPlayerJoined = onPlayerJoined;
        String url = STR."wss://cs108-chacun-multiplayer.sys.polysource.ch/?gameName=\{gameName}&username=\{username}";
        ws = HttpClient
            .newHttpClient()
            .newWebSocketBuilder()
            .buildAsync(URI.create(url), new WebSocketClient(text -> {
                if (gameStarted) {
                    onActionReceived.accept(text);
                } else {
                    if (text.equals("gameStarted")) {
                        gameStarted = true;
                    } else {
                        onPlayerJoined.accept(text, false);
                    }
                }
            }))
            .join();
    }

    public void sendAction(String message) {
        if (!gameStarted) dispatchGameStarted();
        ws.sendText(message, true);
    }

    public void sayHello() {
        ws.sendText(this.username, true);
        onPlayerJoined.accept(this.username, true);
    }

    public void dispatchGameStarted() {
        ws.sendText("gameStarted", true);
        gameStarted = true;
    }

    private record WebSocketClient(Consumer<String> onText) implements WebSocket.Listener {

        @Override
        public void onOpen(WebSocket webSocket) {
            System.out.println(STR."onOpen using subprotocol \{webSocket.getSubprotocol()}");
            WebSocket.Listener.super.onOpen(webSocket);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            System.out.println(STR."onText received \{data}");
            // javafx stuff has to run in its dedicated thread
            Platform.runLater(() -> onText.accept(data.toString()));
            return WebSocket.Listener.super.onText(webSocket, data, last);
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            System.out.println(STR."Bad day! \{webSocket.toString()}");
            WebSocket.Listener.super.onError(webSocket, error);
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            System.out.println("closed");
            return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
        }
    }
}