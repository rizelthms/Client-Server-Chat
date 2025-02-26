package server_level2.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import server_level2.server.ChatClientHandler;
import server_level2.protocol.StatusResult;
import server_level2.protocol.payloads.RpsAcceptResponse;
import server_level2.protocol.payloads.User;
import server_level2.shared.Printer;

import java.util.function.Consumer;

public class RpsAcceptHandler implements Consumer<String> {

    private final ChatClientHandler chatClientHandler;

    public RpsAcceptHandler(ChatClientHandler chatClientHandler) {
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
                sendError(10006);
                return;
            }

            if (!chatClientHandler.getServer().isUserLoggedIn(user.username())) {
                sendError(10007);
                return;
            }

            if (chatClientHandler.getServer().isUserInGame(user.username())) {
                sendError(10008);
                return;
            }

            if (chatClientHandler.getRpsGamePartner() != null) {
                sendError(10003);
                return;
            }

            chatClientHandler.sendToClient(new RpsAcceptResponse(StatusResult.OK, null));
            chatClientHandler.getServer().startRpsGame(chatClientHandler.getUser().username(), user.username());
        } catch (JsonProcessingException e) {
            Printer.printLineColour("Invalid Json: " + json, Printer.ConsoleColour.RED);
            chatClientHandler.sendParseError();
        }
    }

    private void sendError(int errorCode) {
        chatClientHandler.sendToClient(new RpsAcceptResponse(StatusResult.ERROR, errorCode));
    }
}