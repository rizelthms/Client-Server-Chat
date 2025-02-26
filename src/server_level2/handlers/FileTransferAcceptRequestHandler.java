package server_level2.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import server_level2.server.ChatClientHandler;
import server_level2.server.ServerFileTransferRequest;
import server_level2.protocol.StatusResult;
import server_level2.protocol.payloads.*;
import server_level2.shared.Printer;

import java.util.function.Consumer;

public class FileTransferAcceptRequestHandler implements Consumer<String> {

    private final ChatClientHandler chatClientHandler;

    public FileTransferAcceptRequestHandler(ChatClientHandler chatClientHandler) {
        this.chatClientHandler = chatClientHandler;
    }

    @Override
    public void accept(String json) {
        try {
            UUIDPayload uuidPayload = chatClientHandler.getMapper().readValue(json, UUIDPayload.class);
            ServerFileTransferRequest request = chatClientHandler.getServer().getUnansweredFtr().get(uuidPayload.uuid());

            if (chatClientHandler.getUser() == null) {
                sendError(6000);
                return;
            }

            if (request == null) {
                sendError(11004);
                return;
            }

            if (!chatClientHandler.getServer().isUserLoggedIn(request.getSenderUsername())) {
                sendError(11005);
                chatClientHandler.getServer().getUnansweredFtr().remove(uuidPayload.uuid());
                return;
            }

            // Move to ongoing file transfers
            chatClientHandler.getServer().getUnansweredFtr().remove(uuidPayload.uuid());
            chatClientHandler.getServer().getOngoingFileTransfers().add(request);

            // Send accept to sender
            FileTransferAccept acceptPayload = new FileTransferAccept(request.getReceiverUsername(), request.getChecksum(), request.getSenderUUID());
            chatClientHandler.getServer().sendToUser(request.getSenderUsername(), acceptPayload);

            // reply to receiver
            chatClientHandler.sendToClient(new FileTransferAcceptResponse(StatusResult.OK, null, uuidPayload.uuid()));

        } catch (JsonProcessingException e) {
            Printer.printLineColour("Invalid Json: " + json, Printer.ConsoleColour.RED);
            chatClientHandler.sendParseError();
        }
    }

    private void sendError(int errorCode) {
        chatClientHandler.sendToClient(new FileTransferAcceptResponse(StatusResult.ERROR, errorCode, null));
    }
}