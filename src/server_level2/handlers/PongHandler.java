package server_level2.handlers;

import server_level2.server.ChatClientHandler;
import server_level2.protocol.payloads.PongError;
import server_level2.shared.Printer;

import java.util.function.Consumer;

public class PongHandler implements Consumer<String> {

    private final ChatClientHandler chatClientHandler;

    public PongHandler(ChatClientHandler chatClientHandler) {
        this.chatClientHandler = chatClientHandler;
    }

    @Override
    public void accept(String json) {
        if (chatClientHandler.isAwaitingPong()) {
            Printer.printLineColour(chatClientHandler.getUser(), "Received Pong", Printer.ConsoleColour.YELLOW);
            chatClientHandler.pongReceived();
        } else {
            Printer.printLineColour(chatClientHandler.getUser(), "Received Pong without Ping", Printer.ConsoleColour.RED);
            chatClientHandler.sendToClient(new PongError(8000));
        }

    }
}
