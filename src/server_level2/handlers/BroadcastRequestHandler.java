package server_level2.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import server_level2.server.ChatClientHandler;
import server_level2.protocol.StatusResult;
import server_level2.protocol.payloads.Broadcast;
import server_level2.protocol.payloads.BroadcastResponse;
import server_level2.protocol.payloads.Message;
import server_level2.shared.Printer;

import java.util.function.Consumer;

public class BroadcastRequestHandler implements Consumer<String> {

    private final ChatClientHandler chatClientHandler;

    public BroadcastRequestHandler(ChatClientHandler chatClientHandler) {
        this.chatClientHandler = chatClientHandler;
    }

    @Override
    public void accept(String json) {
        try {
            Message message = chatClientHandler.getMapper().readValue(json, Message.class);
            if(chatClientHandler.getUser() != null) {
                Printer.printLineColour(chatClientHandler.getUser(), "Received Message: \"" + message.message() + "\"", Printer.ConsoleColour.GREEN);
                String username = chatClientHandler.getUser().username();
                chatClientHandler.getServer().sendToAllExcept(username, new Broadcast(username, message.message()));
                chatClientHandler.sendToClient(new BroadcastResponse(StatusResult.OK, null));
            } else {
                Printer.printLineColour( "Received Message \"" + message.message() + "\", but user is not logged in", Printer.ConsoleColour.RED);
                chatClientHandler.sendToClient(new BroadcastResponse(StatusResult.ERROR, 6000));
            }

        } catch (JsonProcessingException e) {
            chatClientHandler.sendParseError();
        }
    }
}
