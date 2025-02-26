package server_level2.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import server_level2.server.ChatClientHandler;
import server_level2.protocol.StatusResult;
import server_level2.protocol.payloads.*;
import server_level2.shared.Printer;

import java.util.function.Consumer;

public class PmRequestHandler implements Consumer<String> {

    private final ChatClientHandler chatClientHandler;

    public PmRequestHandler(ChatClientHandler chatClientHandler) {
        this.chatClientHandler = chatClientHandler;
    }

    @Override
    public void accept(String json) {
        try {
            PrivateMessage message = chatClientHandler.getMapper().readValue(json, PrivateMessage.class);
            if(chatClientHandler.getUser() != null) {
                if (chatClientHandler.getServer().isUserLoggedIn(message.username())) {
                    Printer.printLineColour(chatClientHandler.getUser(), "Forwarding PM \"" + message.message() + "\" to user " + message.username(), Printer.ConsoleColour.GREEN);
                    String sender = chatClientHandler.getUser().username();
                    chatClientHandler.getServer().sendToUser(message.username(), new PrivateMessage(sender, message.message()));
                    chatClientHandler.sendToClient(new PrivateMessageResponse(StatusResult.OK, null));
                } else {
                    Printer.printLineColour( "Received PM request for user \"" + message.username() + "\", but that user is not logged in.", Printer.ConsoleColour.RED);
                    chatClientHandler.sendToClient(new PrivateMessageResponse(StatusResult.ERROR, 9001));
                }
            } else {
                Printer.printLineColour( "Received PM request \"" + message.message() + "\", but user is not logged in", Printer.ConsoleColour.RED);
                chatClientHandler.sendToClient(new PrivateMessageResponse(StatusResult.ERROR, 9000));
            }

        } catch (JsonProcessingException e) {
            chatClientHandler.sendParseError();
        }
    }
}
