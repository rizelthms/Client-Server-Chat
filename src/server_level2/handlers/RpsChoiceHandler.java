package server_level2.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import server_level2.server.ChatClientHandler;
import server_level2.protocol.StatusResult;
import server_level2.protocol.payloads.*;
import server_level2.shared.Printer;

import java.util.function.Consumer;

public class RpsChoiceHandler implements Consumer<String> {

    private final ChatClientHandler chatClientHandler;

    public RpsChoiceHandler(ChatClientHandler chatClientHandler) {
        this.chatClientHandler = chatClientHandler;
    }

    @Override
    public void accept(String json) {
        try {
            RpsChoice choice = chatClientHandler.getMapper().readValue(json, RpsChoice.class);

            if (chatClientHandler.getUser() == null) {
                sendError(6000);
                return;
            }

            if (chatClientHandler.getRpsGamePartner() == null) {
                sendError(10013);
                return;
            }

            chatClientHandler.setRpsChoice(choice.choice());
            chatClientHandler.sendToClient(new RpsChoiceResponse(StatusResult.OK, null));
            chatClientHandler.getServer().handleChoiceDone(chatClientHandler);
        } catch (JsonProcessingException e) {
            Printer.printLineColour("Invalid Json: " + json, Printer.ConsoleColour.RED);
            if (e instanceof InvalidFormatException) {
                sendError(10014);
            } else {
                chatClientHandler.sendParseError();
            }
        }
    }

    private void sendError(int errorCode) {
        chatClientHandler.sendToClient(new RpsChoiceResponse(StatusResult.ERROR, errorCode));
    }
}