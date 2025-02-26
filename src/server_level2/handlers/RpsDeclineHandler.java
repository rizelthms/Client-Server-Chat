package server_level2.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import server_level2.server.ChatClientHandler;
import server_level2.protocol.StatusResult;
import server_level2.protocol.payloads.*;
import server_level2.shared.Printer;

import java.util.function.Consumer;

public class RpsDeclineHandler implements Consumer<String> {

    private final ChatClientHandler chatClientHandler;

    public RpsDeclineHandler(ChatClientHandler chatClientHandler) {
        this.chatClientHandler = chatClientHandler;
    }

    @Override
    public void accept(String json) {
        try {
            User user = chatClientHandler.getMapper().readValue(json, User.class);

            if (chatClientHandler.getUser() == null) {
                sendError(6000);
                return;
            }

            if (!chatClientHandler.getServer().doesInviteExist(user.username(), chatClientHandler.getUser().username())) {
                sendError(10011);
                return;
            }

            chatClientHandler.getServer().removeInvite(user.username(), chatClientHandler.getUser().username());
            chatClientHandler.sendToClient(new RpsDeclineResponse(StatusResult.OK, null));
            chatClientHandler.getServer().sendToUser(user.username(), new RpsDecline(chatClientHandler.getUser().username()));
        } catch (JsonProcessingException e) {
            Printer.printLineColour("Invalid Json: " + json, Printer.ConsoleColour.RED);
            chatClientHandler.sendParseError();
        }
    }

    private void sendError(int errorCode) {
        chatClientHandler.sendToClient(new RpsDeclineResponse(StatusResult.ERROR, errorCode));
    }
}