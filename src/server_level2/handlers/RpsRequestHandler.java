package server_level2.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import server_level2.server.ChatClientHandler;
import server_level2.protocol.StatusResult;
import server_level2.protocol.payloads.RpsRequestResponse;
import server_level2.protocol.payloads.User;
import server_level2.shared.Printer;

import java.util.function.Consumer;

public class RpsRequestHandler implements Consumer<String> {

    private final ChatClientHandler chatClientHandler;

    public RpsRequestHandler(ChatClientHandler chatClientHandler) {
        this.chatClientHandler = chatClientHandler;
    }

    @Override
    public void accept(String json) {
        try {
            User otherUser = chatClientHandler.getMapper().readValue(json, User.class);

            if (chatClientHandler.getUser() == null) {
                sendError(6000);
                return;
            }

            if(!chatClientHandler.getServer().isUserLoggedIn(otherUser.username())) {
                sendError(10001);
                return;
            }

            if (chatClientHandler.getServer().isUserInGame(otherUser.username())) {
                sendError(10002);
                return;
            }

            if (chatClientHandler.getServer().isUserInGame(chatClientHandler.getUser().username())) {
                sendError(10003);
                return;
            }

            if (otherUser.username().equals(chatClientHandler.getUser().username())) {
                sendError(10004);
                return;
            }

            chatClientHandler.getServer().sendRpsInvite(chatClientHandler.getUser().username(), otherUser.username());
            chatClientHandler.sendToClient(new RpsRequestResponse(StatusResult.OK, null));
        } catch (JsonProcessingException e) {
            Printer.printLineColour("Invalid Json: " + json, Printer.ConsoleColour.RED);
            chatClientHandler.sendParseError();
        }
    }

    private void sendError(int errorCode) {
        chatClientHandler.sendToClient(new RpsRequestResponse(StatusResult.ERROR, errorCode));
    }
}
